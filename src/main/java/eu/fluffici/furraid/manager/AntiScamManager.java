/*
---------------------------------------------------------------------------------
File Name : AntiScamManager

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 04/07/2024
Last Modified : 04/07/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.furraid.manager;

import com.google.gson.Gson;
import eu.fluffici.bot.api.UrlExtractor;
import eu.fluffici.bot.api.beans.furraid.FurRaidConfig;
import eu.fluffici.bot.api.beans.furraid.GuildSettings;
import eu.fluffici.bot.api.events.MessageEvent;
import eu.fluffici.bot.database.request.RequestModerationCheck;
import eu.fluffici.furraid.FurRaidDB;
import lombok.NonNull;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static eu.fluffici.bot.api.IconRegistry.ICON_CIRCLE_SLASHED;
import static net.dv8tion.jda.internal.utils.Helpers.listOf;

@SuppressWarnings("All")
public class AntiScamManager extends ListenerAdapter {
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
            "$tock", "trade", "win", "click here", "claim", "winner", "prize", "giveaway", "money", "cash", "lottery", "free nudes"
    };

    private static final Pattern URL_PATTERN = Pattern.compile(
            "(https?://[\\w.-]+(/[\\w.-]*)?|\\[.*?\\]\\(https?://[\\w.-]+(/[\\w.-]*)?\\))"
    );

    private static final Pattern MENTION_PATTERN = Pattern.compile(
            "@everyone|@here"
    );

    private final OkHttpClient client = new OkHttpClient();

    @Override
    public void onMessageReceived(@NonNull MessageReceivedEvent event)  {
        Guild guild = event.getGuild();
        GuildSettings guildSettings = FurRaidDB.getInstance().getBlacklistManager().fetchGuildSettings(guild);
        FurRaidConfig.AntiScamFeature antiScamFeature = guildSettings.getConfig().getFeatures().getAntiScamFeature();

        if (!antiScamFeature.isEnabled())
            return;
        if (antiScamFeature.getSettings().getQuarantinedRole() == null
                || antiScamFeature.getSettings().getLoggingChannel() == null)
            return;

        if (guildSettings.getConfig().getSettings().getExemptedChannels().contains(event.getChannel().getId()))
            return;

        AtomicBoolean isExempted = new AtomicBoolean(false);
        event.getMember().getRoles().forEach(role -> {
            if (guildSettings.getConfig().getSettings().getExemptedRoles().contains(role.getId())) isExempted.set(true);
        }); if (isExempted.get()) return;


        String userId = event.getMessage().getAuthor().getId();
        Member member = event.getMessage().getMember();
        String messageContent = event.getMessage().getContentRaw();

        if (FurRaidDB.getInstance().getGameServiceManager().isFQuarantined(guild, member))
            return;

        if (isScamMessage(messageContent) || containsScamContent(messageContent)) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy HH:mm:ss").withZone(ZoneId.of("Europe/Prague"));

            StringSelectMenu.Builder actions = StringSelectMenu.create("select:restore-access-action");
            actions.addOption("Restore Access", "RESTORE:".concat(userId), Emoji.fromCustom("clipboardcheck", 1216864649396486374L, false));
            actions.addOption("Kick", "KICK:".concat(userId), Emoji.fromCustom("clipboardx", 1216864650914824242L, false));
            actions.addOption("Ban", "BAN:".concat(userId), Emoji.fromCustom("ban", 1216864274253746268L, false));

            guild.getTextChannelById(antiScamFeature.getSettings().getLoggingChannel())
                    .sendMessageEmbeds(FurRaidDB.getInstance().getEmbed()
                            .simpleAuthoredEmbed()
                            .setAuthor(FurRaidDB.getInstance().getLanguageManager().get("module.moderation.quarantined.content.title", member.getEffectiveName()), "https://frdb.fluffici.eu", ICON_CIRCLE_SLASHED)
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

            FurRaidDB.getInstance().getGameServiceManager().quarantineFUser(guild, member);

            Role role = guild.getRoleById(antiScamFeature.getSettings().getQuarantinedRole());
            if (role == null || role.canInteract(guild.getRoleByBot(guild.getJDA().getSelfUser())))
                return;
            Role verifiedRole = guild.getRoleById(guildSettings.getConfig().getFeatures().getVerification().getSettings().getVerifiedRole());
            if (verifiedRole == null || verifiedRole.canInteract(guild.getRoleByBot(guild.getJDA().getSelfUser())))
                return;

            guild.addRoleToMember(member, role).queue();
            guild.removeRoleFromMember(member, role).queue();

            event.getMessage().delete().queue();
            member.timeoutFor(7, TimeUnit.DAYS).reason("Quanrantined").queue();
        }
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
                if (check.isStatus()) {
                    if (check.isScam()) {
                        return true;
                    } else {
                        FurRaidDB.getInstance().getLogger().debug("URL: %s is cleared. this URL is not flagged in the registry.", url);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            FurRaidDB.getInstance().getLogger().debug("A error occurred while checking potential malicious-url: %s (%s)", response.message(), response.code());
            FurRaidDB.getInstance().getLogger().debug("Potential malicious-url: %s", url);
        }

        return false;
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

        return hasInvite && hasKeyword && hasSafeguardKeyword && hasMention;
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
}