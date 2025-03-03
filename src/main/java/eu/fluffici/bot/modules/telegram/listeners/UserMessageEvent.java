/*
---------------------------------------------------------------------------------
File Name : UserMessageEvent

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 20/06/2024
Last Modified : 20/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.modules.telegram.listeners;

import com.google.common.eventbus.Subscribe;
import com.google.gson.Gson;
import eu.fluffici.bot.FluffBOT;
import eu.fluffici.bot.api.UrlExtractor;
import eu.fluffici.bot.api.events.telegram.TelegramMessageSendEvent;
import eu.fluffici.bot.api.offence.OffenceType;
import eu.fluffici.bot.database.request.RequestModerationCheck;
import eu.fluffici.bot.modules.moderation.ModerationModule;
import eu.fluffici.bot.modules.telegram.TelegramBOT;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.groupadministration.RestrictChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.ChatPermissions;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import java.awt.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static eu.fluffici.bot.api.IconRegistry.ICON_ALERT;
import static net.dv8tion.jda.internal.utils.Helpers.listOf;

@SuppressWarnings("All")
public class UserMessageEvent {
    private long GROUP_ID = -1001736854039L;
    private final Pattern LINK_PATTERN = Pattern.compile("(https?|ftp|http)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
    private final UrlExtractor urlExtractor = new UrlExtractor();
    private final OkHttpClient client = new OkHttpClient();
    private final OkHttpTelegramClient telegramClient;

    private final ConcurrentHashMap<Long, ModerationModule.UserMessageInfo> userMessageInfos = new ConcurrentHashMap<>();
    private final LinkedHashMap<Long, AtomicInteger> userStrikes = new LinkedHashMap<>();

    public UserMessageEvent(String botToken) {
        this.telegramClient = new OkHttpTelegramClient(botToken);
    }

    /**
     * Subscriber method that is invoked when a Telegram message is sent.
     * Only messages from a specific group identified by GROUP_ID will be processed.
     *
     * @param event The TelegramMessageSendEvent object containing details about the message.
     */
    @Subscribe
    public void onTelegramMessage(@NotNull TelegramMessageSendEvent event) {
        if (event.getMessage().getChatId() != this.GROUP_ID)
            return;
        if (event.getMessage().getFrom().getIsBot())
            return;

        Long userId = event.getMessage().getFrom().getId();
        long currentTime = System.currentTimeMillis();

        ModerationModule.UserMessageInfo messageInfo = userMessageInfos.getOrDefault(userId, new ModerationModule.UserMessageInfo(0L, 0));

        if (FluffBOT.getInstance().getGameServiceManager().hasTelegramMessage(event.getMessage().getMessageId())) {
            return;
        }

        if (event.getMessage().hasText()) {
            String messageContent = event.getMessage().getText();

            Pair<Boolean, List<String>> parsedContent = this.containsLink(messageContent);
            if (parsedContent.getLeft()) {
                this.handleAntiScam(event.getMessage(), parsedContent.getRight());
            }

            checkOffence(event.getMessage(), messageInfo, OffenceType.SPAM, currentTime);
            userMessageInfos.put(userId, messageInfo);

            FluffBOT.getInstance().getGameServiceManager().addTelegramMessage(event.getMessage().getMessageId());
        }
    }

    /**
     * Handles anti-spam functionality for a given message.
     *
     * @param message The message to handle.
     */
    @SneakyThrows
    private void handleAntiSpam(@NotNull Message message) {
        this.muteUser(message, 600);

        this.telegramClient.execute(new DeleteMessage(message.getChatId().toString(), message.getMessageId()));
        this.telegramClient.execute(new SendMessage(message.getChatId().toString(), FluffBOT.getInstance().getLanguageManager().get("common.no_spam")));
    }

    /**
     * Handles anti-scam functionality for a given message.
     *
     * @param message The message to handle.
     * @param urls The list of extracted URLs from the message.
     */
    @SneakyThrows
    private void handleAntiScam(Message message, @NotNull List<String> urls) {
        for (String url : urls) {
            if (this.isScamURL(url)) {
                this.telegramClient.execute(new DeleteMessage(message.getChatId().toString(), message.getMessageId()));
                this.telegramClient.execute(new SendMessage(message.getChatId().toString(), FluffBOT.getInstance().getLanguageManager().get("common.not_scamming_bonk", message.getFrom().getUserName())));
                this.handleLogging(message, OffenceType.MALICIOUS_LINK);
                break;
            }
        }
    }

    @SneakyThrows
    private void checkOffence(Message message, ModerationModule.UserMessageInfo messageInfo, @NotNull OffenceType offenceType, long currentTime) {
        switch (offenceType) {
            case SPAM -> {
                if (currentTime - messageInfo.getLastMessageTime() <= offenceType.getTimeThreshold()) {
                    messageInfo.setMessageCount(messageInfo.getMessageCount() + 1);

                    if (messageInfo.getMessageCount() > offenceType.getThreshold()) {
                        this.handleAntiSpam(message);
                        messageInfo.setMessageCount(0);
                    }
                } else {
                    messageInfo.setMessageCount(1);
                }
            }
        }

        System.out.println(messageInfo.getMessageCount());
        System.out.println(messageInfo.getLastMessageTime());

        messageInfo.setLastMessageTime(currentTime);
    }

    private final Map<Long, Map<Long, Long>> mutedUsers = new HashMap<>();

    @SneakyThrows
    private void muteUser(Message message, int durationInSeconds) {
        if (this.checkIfMuted(message.getChatId(), message.getFrom().getId()))
            return;

        ChatPermissions permissions = new ChatPermissions();
        permissions.setCanSendMessages(false);
        permissions.setCanSendPolls(false);
        permissions.setCanSendOtherMessages(false);
        permissions.setCanAddWebPagePreviews(false);
        permissions.setCanChangeInfo(false);
        permissions.setCanInviteUsers(false);
        permissions.setCanPinMessages(false);

        long currentTime = System.currentTimeMillis() / 1000;

        this.handleLogging(message, OffenceType.SPAM);

        this.telegramClient.execute(new RestrictChatMember(
                message.getChatId().toString(),
                message.getFrom().getId(),
                permissions,
                (int) (currentTime + durationInSeconds),
                true
        ));

        mutedUsers.computeIfAbsent(message.getChatId(), k -> new HashMap<>()).put(message.getFrom().getId(), currentTime + durationInSeconds);
    }

    private boolean checkIfMuted(long chatId, long userId) {
        long currentTime = System.currentTimeMillis() / 1000;
        Map<Long, Long> chatMutedUsers = mutedUsers.get(chatId);

        if (chatMutedUsers != null && chatMutedUsers.containsKey(userId)) {
            long muteEndTime = chatMutedUsers.get(userId);
            if (currentTime < muteEndTime) {
               return true;
            } else {
                chatMutedUsers.remove(userId);
            }
        }
        return false;
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
                        TelegramBOT.getInstance().getLogger().warn("URL: %s is cleared. this URL is not flagged in the registry.", url);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            TelegramBOT.getInstance().getLogger().warn("A error occurred while checking potential malicious-url: %s (%s)", response.message(), response.code());
            TelegramBOT.getInstance().getLogger().warn("Potential malicious-url: %s", url);
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

        return Pair.of(false, listOf());
    }

    /**
     * Handles the logging of a message.
     *
     * @param message The message to handle.
     */
    private void handleLogging(@NotNull Message message, @NotNull OffenceType offenceType) {
        FluffBOT instance = FluffBOT.getInstance();
        Guild mainGuild = instance.getJda().getGuildById(instance.getDefaultConfig().getProperty("main.guild"));
        TextChannel staffChannel = mainGuild.getTextChannelById(instance.getChannelConfig().getProperty("channel.staff"));

        EmbedBuilder telegramLog = instance.getEmbed().simpleAuthoredEmbed();
        telegramLog.setTimestamp(Instant.now());
        telegramLog.setAuthor(instance.getLanguageManager().get("common.telegram.auto_mod.title", offenceType.name().replaceAll("_", " ")), "https://fluffici.eu", ICON_ALERT);
        telegramLog.setColor(Color.RED);

        telegramLog.setDescription(
                """
                **Message content**:
                ```
                %s
                ```
                """.formatted(message.getText())
        );

        telegramLog.addField(instance.getLanguageManager().get("common.user.id"), message.getFrom().getId().toString(), false);
        telegramLog.addField(instance.getLanguageManager().get("common.user.name"), message.getFrom().getUserName(), false);

        staffChannel.sendMessageEmbeds(telegramLog.build()).queue();
    }
}