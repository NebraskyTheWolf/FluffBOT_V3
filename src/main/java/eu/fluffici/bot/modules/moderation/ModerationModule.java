/*
---------------------------------------------------------------------------------
File Name : ModerationModule

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 13/06/2024
Last Modified : 13/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.modules.moderation;

import com.google.common.eventbus.Subscribe;
import com.google.gson.Gson;
import eu.fluffici.bot.FluffBOT;
import eu.fluffici.bot.api.UrlExtractor;
import eu.fluffici.bot.api.events.MessageEvent;
import eu.fluffici.bot.api.module.Category;
import eu.fluffici.bot.api.module.Module;
import eu.fluffici.bot.api.offence.ActionType;
import eu.fluffici.bot.api.offence.OffenceType;
import eu.fluffici.bot.database.request.RequestModerationCheck;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static eu.fluffici.bot.api.IconRegistry.ICON_ALERT;
import static eu.fluffici.bot.api.IconRegistry.ICON_CIRCLE_SLASHED;
import static net.dv8tion.jda.internal.utils.Helpers.listOf;

// Jděte do hajzlu, podvodníci =)

/**
 * @deprecated Sees FurRaidDB source.
 */
@Deprecated(forRemoval = true)
public class ModerationModule extends Module {

    private final ConcurrentHashMap<String, UserMessageInfo> userMessageInfos = new ConcurrentHashMap<>();
    private final LinkedHashMap<UserSnowflake, AtomicInteger> userStrikes = new LinkedHashMap<>();
    private final Pattern LINK_PATTERN = Pattern.compile("(https?|ftp|http)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
    private static final Pattern DISCORD_HYPERLINK_PATTERN = Pattern.compile("\\[(.*?)\\]\\((https?://\\S+)\\)");
    private static final Pattern DISCORD_INVITE_PATTERN = Pattern.compile("https?://discord\\.gg/\\S+|https?://discord\\.com/invite/\\S+");
    private final UrlExtractor urlExtractor = new UrlExtractor();

    // Keywords commonly found in scam messages
    private static final String[] SCAM_KEYWORDS = {
            "teen porn", "latina nudes", "onlyfans leaks", "hot girls", "hot pussys", "e-girls porn", "hentai porn", "porn", "nudes", "leaks", "teen",
            "free sex", "live cam", "sex tape", "adult dating", "nude selfies", "xxx videos", "amateur porn", "milf nudes", "teen sex", "erotic photos",
            "sex chat", "webcam sex", "nude models", "explicit content", "adult videos", "uncensored", "private snaps", "camgirls", "leaked nudes",
            "sexy videos", "cam sex", "sex clips", "xxx pics", "live sex", "bdsm porn", "taboo porn", "gay porn", "lesbian porn", "celebrity nudes",
            "hot sex", "incest porn", "fetish porn", "mature porn", "anime porn", "hardcore porn", "porn gif", "adult movies", "vr porn", "sex videos",
            "erotic videos", "nude photos", "porn links", "adult chat", "dirty chat", "live nude", "webcam models", "hot babes", "xxx stream", "adult site",
            "free porn", "sex stories", "naughty pics", "sex cams", "cam girls", "gift cards", "Free 18+", "hot milf", "stock", "trading", "market investment",
            "$tock", "trade", "free", "win", "click here", "claim", "winner", "prize", "giveaway", "money", "cash", "lottery", "free nudes"
    };

    private static final Pattern URL_PATTERN = Pattern.compile(
            "(https?://[\\w.-]+(/[\\w.-]*)?|\\[.*?\\]\\(https?://[\\w.-]+(/[\\w.-]*)?\\))"
    );

    private static final Pattern MENTION_PATTERN = Pattern.compile(
            "@everyone|@here"
    );

    private final OkHttpClient client = new OkHttpClient();

    private final FluffBOT instance;

    public ModerationModule(FluffBOT instance) {
        super("moderation", "Auto Moderation", "Handling chat offences", "1.0.0", "Vakea", Category.MODERATION);

        this.instance = instance;
    }

    @Override
    public void onEnable() {
        this.instance.getLogger().info("Loading Auto Moderation module..");
        this.instance.getEventBus().register(this);
        this.instance.getExecutorMonoThread().scheduleAtFixedRate(this.userStrikes::clear, 10, 10, TimeUnit.MINUTES);
    }

    @Override
    public void onDisable() {
        this.userMessageInfos.clear();
        this.userStrikes.clear();
        this.instance.getEventBus().unregister(this);

        this.instance.getLogger().info("Disabling Auto Moderation module..");
    }

    @Subscribe
    public void onMessageEvent(MessageEvent event) {
        String userId = event.getMessage().getAuthor().getId();
        String messageContent = event.getMessage().getContentRaw();
        long currentTime = System.currentTimeMillis();

        UserMessageInfo messageInfo = userMessageInfos.getOrDefault(userId, new UserMessageInfo(0L, 0));

        checkOffence(event, messageInfo, OffenceType.SPAM, currentTime, messageContent);
        // checkOffence(event, messageInfo, OffenceType.MALICIOUS_LINK, currentTime, messageContent);
        checkOffence(event, messageInfo, OffenceType.SCAM_BOT, currentTime, messageContent);

        userMessageInfos.put(userId, messageInfo);
    }

    @SneakyThrows
    private void checkOffence(MessageEvent event, UserMessageInfo messageInfo, OffenceType offenceType, long currentTime, String messageContent) {
        switch (offenceType) {
            case SPAM -> {
                if (currentTime - messageInfo.getLastMessageTime() <= offenceType.getTimeThreshold()) {
                    messageInfo.setMessageCount(messageInfo.getMessageCount() + 1);

                    if (messageInfo.getMessageCount() > offenceType.getThreshold()) {
                        handleOffence(event, offenceType);
                        messageInfo.setMessageCount(0);
                    }
                } else {
                    messageInfo.setMessageCount(1);
                }
            }
            case MALICIOUS_LINK -> {
                Pair<Boolean, List<String>> linkCheck = containsLink(messageContent);
                if (linkCheck.getLeft()) {
                    for (String url : linkCheck.getRight()) {
                        if (this.isScamURL(url.replaceAll("https://", "").replaceAll("/", ""))) {
                            this.handleOffence(event, offenceType);
                        }
                    }
                }
            }
            case SCAM_BOT -> {
                if (containsScamContent(messageContent) || isScamMessage(messageContent)) {
                    handleOffence(event, offenceType);
                }
            }
        }

        messageInfo.setLastMessageTime(currentTime);
    }

    @SneakyThrows
    private boolean isScamURL(@NotNull String url) {
        Request request = new Request.Builder()
                .url("https://api.fluffici.eu/api/moderation/scam-detection/link?link=".concat(url.replaceAll("/", "")))
                .get()
                .build();

        Response response = this.client.newCall(request).execute();
        if (response.isSuccessful()) {
            try {
                RequestModerationCheck check = new Gson().fromJson(response.body().string(), RequestModerationCheck.class);

                System.out.println(check.isStatus());
                System.out.println(check.isScam());

                if (check.isStatus()) {
                    if (check.isScam()) {
                        return true;
                    } else {
                        instance.getLogger().warn("URL: %s is cleared. this URL is not flagged in the registry.", url);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            instance.getLogger().warn("A error occurred while checking potential malicious-url: %s (%s)", response.message(), response.code());
            instance.getLogger().warn("Potential malicious-url: %s", url);
        }

        return false;
    }

    @SuppressWarnings("All")
    private void handleOffence(@NotNull MessageEvent event, @NotNull OffenceType offenceType) {
        Member member = event.getMessage().getMember();
        String reason = "User " + member.getId() + " committed an offence: " + offenceType.name();

        List<MessageEmbed> messageEmbeds = new ArrayList<>();
        UserMessageInfo userMessageInfo = this.userMessageInfos.get(member.getUser().getId());

        for (ActionType actionType : offenceType.getActionTypes()) {
            switch (actionType) {
                case BLOCK -> {
                    event.getMessage().delete().reason(reason).queue();
                }
                case ALERT -> this.instance.getJda().getGuildById(this.instance.getDefaultConfig().getProperty("main.guild"))
                        .getTextChannelById(this.instance.getChannelConfig().getProperty("channel.staff"))
                        .sendMessageEmbeds(this.instance.getEmbed()
                                .simpleAuthoredEmbed()
                                .setAuthor(this.instance.getLanguageManager().get("module.moderation.alert.".concat(offenceType.name().toLowerCase()).concat(".type")), "https://fluffici.eu", ICON_ALERT)
                                .setThumbnail(member.getUser().getAvatarUrl())
                                .setDescription(this.instance.getLanguageManager().get("module.moderation.alert.".concat(offenceType.name().toLowerCase()).concat(".description"), member.getAsMention(), userMessageInfo.messageCount, offenceType.getTimeThreshold() / 1000, member.getAsMention()))
                                .setColor(Color.RED)
                                .setFooter(this.instance.getLanguageManager().get("module.moderation.alert.footer"))
                                .build()
                        ).addActionRow(Button.link(String.format("https://discord.com/channels/%s/%s/%s", event.getMessage().getGuildId(), event.getMessage().getChannelId(), event.getMessage().getId()), "Message")).queue();
            }
        }

        AtomicInteger strikes = this.userStrikes.getOrDefault(event.getMessage().getAuthor(), new AtomicInteger(0));

        for (ActionType actionType : offenceType.getStrikesAction()) {
            switch (actionType) {
                case TIMEOUT -> {
                    if (strikes.get() >= offenceType.getStrikeThreshold()) {
                        member.timeoutFor(10, TimeUnit.MINUTES);
                        this.userStrikes.remove(event.getMessage().getAuthor());
                    }
                }
                case SOFT_WARN -> {
                    if (strikes.get() >= offenceType.getStrikeThreshold()) {
                        messageEmbeds.add(this.instance.getEmbed()
                                .simpleAuthoredEmbed()
                                .setAuthor(this.instance.getLanguageManager().get("module.moderation.warn.".concat(offenceType.name().toLowerCase()).concat(".title")), "https://fluffici.eu", ICON_ALERT)
                                .setDescription(this.instance.getLanguageManager().get("module.moderation.warn.".concat(offenceType.name().toLowerCase()).concat(".description")))
                                .setColor(Color.RED)
                                .setFooter(this.instance.getLanguageManager().get("module.moderation.warn.".concat(offenceType.name().toLowerCase()).concat(".footer")))
                                .build());
                    }
                }
            }
        }

        MessageCreateAction messageCreateAction = event.getMessage()
                .replyEmbeds(messageEmbeds)
                .addActionRow(Button.link("https://discord.com/channels/606534136806637589/606556413183000671","Pravidla"))
                .setContent(member.getAsMention());

        if (offenceType == OffenceType.SCAM_BOT) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy HH:mm:ss").withZone(ZoneId.of("Europe/Prague"));
            Guild guild = this.instance.getJda().getGuildById(this.instance.getDefaultConfig().getProperty("main.guild"));

            StringSelectMenu.Builder actions = StringSelectMenu.create("select:restore-access-action");
            actions.addOption("Restore Access", "RESTORE:".concat(member.getId()), Emoji.fromCustom("clipboardcheck", 1216864649396486374L, false));
            actions.addOption("Kick", "KICK:".concat(member.getId()), Emoji.fromCustom("clipboardx", 1216864650914824242L, false));
            actions.addOption("Ban", "BAN:".concat(member.getId()), Emoji.fromCustom("ban", 1216864274253746268L, false));

            guild.getTextChannelById(this.instance.getChannelConfig().getProperty("channel.staff"))
                    .sendMessageEmbeds(this.instance.getEmbed()
                            .simpleAuthoredEmbed()
                            .setAuthor(this.instance.getLanguageManager().get("module.moderation.quarantined.content.title", member.getUser().getGlobalName()), "https://fluffici.eu", ICON_CIRCLE_SLASHED)
                            .setDescription(
                                """
                                **Message content**:
                                ```
                                %s
                                ```
                                
                                **Intercepted in**: #%s (%s)
                                """.formatted(event.getMessage().getContentRaw(), event.getMessage().getChannel().getName(), event.getMessage().getChannel().getId())
                            )
                            .setColor(Color.ORANGE)
                            .addField("Joined At", member.getTimeJoined().format(formatter), false)
                            .addField("Account Age", member.getUser().getTimeCreated().format(formatter), false)
                            .addField("Mention", member.getUser().getAsMention(), false)
                            .addField("ID", member.getUser().getId(), false)
                            .setTimestamp(Instant.now())
                            .build())
                    .addActionRow(actions.build())
                    .queue();

            this.instance.getGameServiceManager().quarantineUser(member);

            this.instance.getJda().getGuildById(this.instance.getDefaultConfig().getProperty("main.guild"))
                    .removeRoleFromMember(member, this.instance.getJda().getRoleById(this.instance.getDefaultConfig().getProperty("roles.verified")));
            this.instance.getJda().getGuildById(this.instance.getDefaultConfig().getProperty("main.guild"))
                    .addRoleToMember(member, this.instance.getJda().getRoleById(this.instance.getDefaultConfig().getProperty("roles.unverified")));
        }

        Message message = messageCreateAction.complete();

        this.instance.getExecutorMonoThread().schedule(() -> message.delete(), 60, TimeUnit.SECONDS);
        this.userStrikes.put(event.getMessage().getAuthor(), this.userStrikes.getOrDefault(event.getMessage().getAuthor(),  new AtomicInteger(1)));
    }

    /**
     * Checks if a given message content contains a link.
     *
     * @param messageContent The content of the message to check.
     * @return A pair containing a boolean indicating whether a link is present and a list of extracted URLs.
     */
    @NotNull
    private Pair<Boolean, List<String>> containsLink(String messageContent) {
        List<String> urls = urlExtractor.extractUrls(messageContent);

        if (!urls.isEmpty()) {
            return Pair.of(true, urls);
        }

        Matcher matcherNormal = LINK_PATTERN.matcher(messageContent);
        while (matcherNormal.find()) {
            String url = matcherNormal.group(0).split("/")[0];
            if (!urlExtractor.extractUrls(url).isEmpty()) {
                urls.add(url);
            }
        }

        Matcher matcher = DISCORD_HYPERLINK_PATTERN.matcher(messageContent);
        while (matcher.find()) {
            String url = matcher.group(2).split("/")[0];
            if (!urlExtractor.extractUrls(url).isEmpty()) {
                urls.add(url);
            }
        }

        return Pair.of(false, listOf());
    }

    /**
     * Checks if a given message content contains scam content.
     *
     * @param messageContent The content of the message to check.
     * @return True if the message contains scam content, false otherwise.
     */
    private boolean containsScamContent(@NotNull String messageContent) {
        Matcher inviteMatcher = DISCORD_INVITE_PATTERN.matcher(messageContent);
        boolean hasInvite = inviteMatcher.find();

        boolean hasKeyword = false;
        for (String keyword : SCAM_KEYWORDS) {
            if (messageContent.toLowerCase().contains(keyword.toLowerCase())) {
                hasKeyword = true;
                break;
            }
        }

        boolean hasSafeguardKeyword = isHasKeywordsSafe(messageContent);

        Matcher mentionMatcher = MENTION_PATTERN.matcher(messageContent);
        boolean hasMention = mentionMatcher.find();

        return hasInvite && hasKeyword || hasSafeguardKeyword && hasMention;
    }

    /**
     * Checks if a given message is a scam message.
     *
     * @param message The message content to check.
     * @return true if the message is a scam message, false otherwise.
     */
    public boolean isScamMessage(String message) {
        Matcher urlMatcher = URL_PATTERN.matcher(message);
        boolean hasUrl = urlMatcher.find();

        String extractedUrl = "google.com";

        if (hasUrl) {
            extractedUrl = urlMatcher.group();
            extractedUrl = extractedUrl.replaceFirst("https?://", "").replaceFirst("/.*", "");
        }

        boolean isScamURL = this.isScamURL(extractedUrl);

        Matcher mentionMatcher = MENTION_PATTERN.matcher(message);
        boolean hasMention = mentionMatcher.find();

        boolean hasKeywords = false;
        for (String keyword : SCAM_KEYWORDS) {
            if (message.toLowerCase().contains(keyword.toLowerCase())) {
                hasKeywords = true;
                break;
            }
        }

        boolean hasKeywordsSafe = isHasKeywordsSafe(message);

        return hasUrl && isScamURL && hasMention && hasKeywords && hasKeywordsSafe;
    }

    /**
     * Checks if a given message contains safeguard keywords.
     *
     * @param message The message content to check.
     * @return true if the message contains safe keywords, false otherwise.
     */
    private static boolean isHasKeywordsSafe(@NotNull String message) {
        boolean hasKeywordsSafe = false;
        // Safeguard!
        String[] words = message.toLowerCase().split("\\s+");
        for (String keyword : SCAM_KEYWORDS) {
            String[] keywordParts = keyword.toLowerCase().split("\\s+");
            for (String word : words) {
                for (String part : keywordParts) {
                    if (word.equals(part)) {
                        hasKeywordsSafe = true;
                        break;
                    }
                }
            }
        }
        return hasKeywordsSafe;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class UserMessageInfo {
        private long lastMessageTime;
        private int messageCount;

        @Override
        public String toString() {
            return "UserMessageInfo{" +
                    "lastMessageTime=" + lastMessageTime +
                    ", messageCount=" + messageCount +
                    '}';
        }
    }
}