package eu.fluffici.furraid.manager;

import eu.fluffici.bot.api.beans.furraid.FurRaidConfig;
import eu.fluffici.bot.api.beans.furraid.GuildSettings;
import eu.fluffici.bot.api.offence.furraid.OffenceType;
import eu.fluffici.furraid.FurRaidDB;
import lombok.NonNull;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static eu.fluffici.bot.api.IconRegistry.ICON_ALERT;

public class AutoModManager extends ListenerAdapter {
    private static final Pattern URL_PATTERN = Pattern.compile(
            "(https?://[\\w.-]+(/[\\w.-]*)?|\\[.*?\\]\\(https?://[\\w.-]+(/[\\w.-]*)?\\))"
    );

    private final OkHttpClient client = new OkHttpClient();
    private final Map<String, List<Message>> messageHistoryMap = new HashMap<>();
    private final Map<String, List<Message>> messageEmojiHistoryMap = new HashMap<>();
    private final Map<String, List<Message>> messageRepeatedHistoryMap = new HashMap<>();
    private final Map<String, List<Message>> messageAttachmentHistoryMap = new HashMap<>();
    private final Map<String, List<Message>> messageMentionsHistoryMap = new HashMap<>();

    private final Map<String, Integer> userStrikes = new ConcurrentHashMap<>();
    private final FurRaidDB instance;

    public AutoModManager(FurRaidDB instance) {
        this.instance = instance;
    }

    @Override
    public void onMessageReceived(@NonNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot() || event.getAuthor().isSystem()) return;

        GuildSettings guildSettings = instance.getBlacklistManager().fetchGuildSettings(event.getGuild());
        FurRaidConfig.AutoModerationFeature autoModerationFeature = guildSettings.getConfig().getFeatures().getAutoModeration();

        if (!autoModerationFeature.isEnabled()) return;

        if (guildSettings.getConfig().getSettings().getExemptedChannels().contains(event.getChannel().getId()))
            return;

        AtomicBoolean isExempted = new AtomicBoolean(false);
        event.getMember().getRoles().forEach(role -> {
            if (guildSettings.getConfig().getSettings().getExemptedRoles().contains(role.getId())) isExempted.set(true);
        }); if (isExempted.get()) return;


        List<FurRaidConfig.Module> modules = autoModerationFeature.getSettings().getModules();
        if (modules.isEmpty()) return;

        modules.forEach(module -> {
            OffenceType.OffenceDetails offenceType = OffenceType.valueOf(module.getSensitivity()).getOffenceType(module.getSlug());
            if (module.isEnabled()) {
                switch (module.getSlug()) {
                    case "spam" -> handleSpam(offenceType, event.getMessage());
                    case "emoji_spam" -> handleEmojiSpam(offenceType, event.getMessage());
                    case "mass_mentions" -> handleMassMentions(offenceType, event.getMessage());
                    case "link_protection" -> handleLinkProtection(offenceType, event.getMessage());
                    case "attachment_spam" -> handleAttachmentsSpam(offenceType, event.getMessage());
                    case "repeated_messages" -> handleRepeatedMessage(offenceType, event.getMessage());
                }
            }
        });
    }

    private void handleSpam(OffenceType.OffenceDetails offenceType, @NotNull Message message) {
        handleGenericOffence(offenceType, message, messageHistoryMap, Message::getContentDisplay);
    }

    private void handleEmojiSpam(OffenceType.OffenceDetails offenceType, @NotNull Message message) {
        handleGenericOffence(offenceType, message, messageEmojiHistoryMap, msg ->
                String.valueOf(Arrays.stream(msg.getContentDisplay().split(""))
                        .filter(this::isEmoji).count()));
    }

    private void handleMassMentions(OffenceType.OffenceDetails offenceType, @NotNull Message message) {
        handleGenericOffence(offenceType, message, messageMentionsHistoryMap, msg ->
                String.valueOf(msg.getMentions().getMentions(
                        Message.MentionType.CHANNEL,
                        Message.MentionType.HERE,
                        Message.MentionType.ROLE,
                        Message.MentionType.EVERYONE,
                        Message.MentionType.USER).size()));
    }

    @SneakyThrows
    private void handleLinkProtection(OffenceType.OffenceDetails offenceType, Message message) {
        Matcher urlMatcher = URL_PATTERN.matcher(message.getContentRaw());
        boolean hasUrl = urlMatcher.find();

        if (hasUrl) {
            String extractedUrl = urlMatcher.group();
            Request request = new Request.Builder()
                    .url("https://api.fluffici.eu/api/moderation/scam-detection/link?link=" + extractedUrl.replaceFirst("https?://", "").replaceFirst("/.*", ""))
                    .get()
                    .build();

            Response response = this.client.newCall(request).execute();
            if (response.isSuccessful()) {
                try {
                    /** TODO: Need workaround, the private API has been deleted.
                     *  RequestModerationCheck check = new Gson().fromJson(response.body().string(), RequestModerationCheck.class);
                     *                 if (check.isStatus()) {
                     *                     if (check.isScam()) {
                     *                         this.applyAction(offenceType, message);
                     *                     }
                     *                 }
                     */
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void handleAttachmentsSpam(OffenceType.OffenceDetails offenceType, @NotNull Message message) {
        handleGenericOffence(offenceType, message, messageAttachmentHistoryMap, msg ->
                String.valueOf(msg.getAttachments().size()));
    }

    private void handleRepeatedMessage(OffenceType.OffenceDetails offenceType, @NotNull Message message) {
        handleGenericOffence(offenceType, message, messageRepeatedHistoryMap, Message::getContentDisplay);
    }

    private void handleGenericOffence(@NotNull OffenceType.OffenceDetails offenceType, @NotNull Message message,
                                      @NotNull Map<String, List<Message>> messageHistory, Function<Message, String> propertyExtractor) {
        String userId = message.getAuthor().getId();

        int maxThreshold = offenceType.getThreshold();
        int timeFrame = offenceType.getTimeThreshold();

        List<Message> userMessages = messageHistory.getOrDefault(userId, new ArrayList<>());
        cleanupMessages(userMessages, timeFrame);
        userMessages.add(message);

        long count = userMessages.stream()
                .filter(m -> propertyExtractor.apply(m).equals(propertyExtractor.apply(message)))
                .count();

        if (count >= maxThreshold) {
            addStrike(offenceType, message);
        }

        messageHistory.put(userId, userMessages);
    }

    private void addStrike(@NotNull OffenceType.OffenceDetails offenceType, @NotNull Message message) {
        String userId = message.getAuthor().getId();
        int currentStrikes = userStrikes.getOrDefault(userId, 0) + 1;

        userStrikes.put(userId, currentStrikes);
        if (currentStrikes >= offenceType.getStrikeThreshold()) {
            applyAction(offenceType, message);
            userStrikes.put(userId, 0);
        }
    }

    private void applyAction(@NotNull OffenceType.OffenceDetails offenceDetails, @NotNull Message message) {
        Member member = message.getMember();
        if (member == null) return;

        String reason = "User " + member.getId() + " committed an offence: " + offenceDetails.getName();
        FurRaidConfig.AutoModerationFeature autoModerationFeature = instance.getBlacklistManager().fetchGuildSettings(message.getGuild())
                .getConfig().getFeatures().getAutoModeration();

        List<MessageEmbed> messageEmbeds = new ArrayList<>();

        offenceDetails.getActionTypes().forEach(actionType -> {
            switch (actionType) {
                case BLOCK -> {
                    message.delete().reason(reason).queue();
                    messageEmbeds.add(createEmbed("block", offenceDetails.getName().toLowerCase()));
                }
                case ALERT -> {
                    var loggingChannel = message.getGuild().getTextChannelById(autoModerationFeature.getSettings().getLoggingChannel());
                    if (loggingChannel != null) {
                        loggingChannel.sendMessageEmbeds(createAlertEmbed(offenceDetails, member))
                                .addActionRow(Button.link(String.format("https://discord.com/channels/%s/%s/%s", message.getGuildId(), message.getChannelId(), message.getId()), "Message"))
                                .queue();
                    }
                }
            }
        });

        offenceDetails.getStrikesAction().forEach(actionType -> {
            switch (actionType) {
                case TIMEOUT -> member.timeoutFor(48, TimeUnit.HOURS).reason(reason).queue();
                case SOFT_WARN -> messageEmbeds.add(createEmbed("soft_warn", offenceDetails.getName().toLowerCase()));
                case HARD_WARN -> messageEmbeds.add(createEmbed("hard_warn", offenceDetails.getName().toLowerCase()));
                case KICK -> member.kick().reason(reason).queue();
            }
        });

        sendUserNotification(message, member, messageEmbeds);
    }

    private void sendUserNotification(@NotNull Message message, Member member, List<MessageEmbed> messageEmbeds) {
        MessageCreateAction messageCreateAction = message.getChannel().sendMessageEmbeds(messageEmbeds)
                .setContent(member.getAsMention());

        Optional.ofNullable(message.getGuild().getRulesChannel()).ifPresent(rulesChannel ->
                messageCreateAction.setComponents(ActionRow.of(
                        Button.link(rulesChannel.getJumpUrl(), "Rules")
                ))
        );

        Message replyMessage = messageCreateAction.complete();
        instance.getExecutorMonoThread().schedule(replyMessage::delete, 60, TimeUnit.SECONDS);
    }

    private MessageEmbed createAlertEmbed(@NotNull OffenceType.OffenceDetails offenceDetails, Member member) {
        return instance.getEmbed()
                .simpleAuthoredEmbed()
                .setAuthor(instance.getLanguageManager().get("module.moderation.alert." + offenceDetails.getName().toLowerCase() + ".type"), "https://frdb.fluffici.eu", ICON_ALERT.getUrl())
                .setThumbnail(member.getUser().getAvatarUrl())
                .setDescription(instance.getLanguageManager().get("module.moderation.alert." + offenceDetails.getName().toLowerCase() + ".description", member.getAsMention(), offenceDetails.getThreshold(), offenceDetails.getTimeThreshold() / 1000, member.getAsMention()))
                .setColor(Color.RED)
                .setFooter(instance.getLanguageManager().get("module.moderation.alert.footer"))
                .build();
    }

    private MessageEmbed createEmbed(String type, String offenceName) {
        return instance.getEmbed()
                .simpleAuthoredEmbed()
                .setAuthor(instance.getLanguageManager().get("module.moderation." + type + "." + offenceName + ".title"), "https://frdb.fluffici.eu", ICON_ALERT.getUrl())
                .setDescription(instance.getLanguageManager().get("module.moderation." + type + "." + offenceName + ".description"))
                .setColor(Color.RED)
                .setFooter(instance.getLanguageManager().get("module.moderation." + type + "." + offenceName + ".footer"))
                .build();
    }

    private void cleanupMessages(@NotNull List<Message> messages, int timeFrame) {
        messages.removeIf(message ->
                message.getTimeCreated().isBefore(OffsetDateTime.now().minus(Duration.ofMillis(timeFrame)))
        );
    }

    private boolean isDiscordEmoji(@NotNull String character) {
        return character.matches("<(a)?:\\w+:(\\d+)>");
    }

    private boolean isEmoji(@NotNull String character) {
        return character.codePoints().anyMatch(codePoint ->
                Character.UnicodeBlock.of(codePoint) == Character.UnicodeBlock.EMOTICONS ||
                        Character.UnicodeBlock.of(codePoint) == Character.UnicodeBlock.MISCELLANEOUS_SYMBOLS ||
                        Character.UnicodeBlock.of(codePoint) == Character.UnicodeBlock.MISCELLANEOUS_SYMBOLS_AND_PICTOGRAPHS
        ) || isDiscordEmoji(character);
    }
}