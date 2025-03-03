/*
---------------------------------------------------------------------------------
File Name : CommandHandler

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 20/06/2024
Last Modified : 20/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.modules.telegram.instance;

import eu.fluffici.bot.FluffBOT;
import eu.fluffici.bot.api.events.telegram.*;
import eu.fluffici.bot.modules.telegram.TelegramBOT;
import eu.fluffici.bot.modules.telegram.commands.StartCommand;
import eu.fluffici.bot.modules.telegram.commands.UnlinkCommand;
import org.telegram.telegrambots.extensions.bots.commandbot.CommandLongPollingTelegramBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class CommandHandler extends CommandLongPollingTelegramBot {
    public CommandHandler(FluffBOT instance, String botToken, String botUsername) {
        super(new OkHttpTelegramClient(botToken), true, () -> botUsername);

        register(new StartCommand(instance));
        register(new UnlinkCommand(instance));

        registerDefaultAction((telegramClient, message) -> {
            SendMessage commandUnknownMessage = new SendMessage(String.valueOf(message.getChatId()),
                    "The command '" + message.getText() + "' is not known by this bot. Here comes some help ");
            try {
                telegramClient.execute(commandUnknownMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * This method processes non-command updates received by the Telegram bot.
     * It handles different types of update such as messages, callback queries, shipping queries, pre-checkout queries,
     * chat join requests, edited messages, and chat member updates.
     *
     * @param update The update object containing the non-command update.
     */
    @Override
    public void processNonCommandUpdate(Update update) {
        if (update.hasMessage()) {
            TelegramBOT.getInstance().getEventBus().post(new TelegramMessageSendEvent(update.getMessage().getFrom(), update.getMessage()));
        } else if (update.hasCallbackQuery()) {
            TelegramBOT.getInstance().getEventBus().post(new TelegramCallbackQuery(update.getCallbackQuery()));
        } else if (update.hasShippingQuery()) {
            TelegramBOT.getInstance().getEventBus().post(new TelegramShippingQuery(update.getShippingQuery()));
        } else if (update.hasPreCheckoutQuery()) {
            TelegramBOT.getInstance().getEventBus().post(new TelegramPreCheckoutQuery(update.getPreCheckoutQuery()));
        } else if (update.hasChatJoinRequest()) {
            TelegramBOT.getInstance().getEventBus().post(new TelegramChatJoinRequest(update.getChatJoinRequest()));
        } else if (update.hasEditedMessage()) {
            TelegramBOT.getInstance().getEventBus().post(new TelegramMessageEditEvent(update.getEditedMessage().getFrom(), update.getEditedMessage()));
        } else if (update.hasChatMember()) {
            TelegramBOT.getInstance().getEventBus().post(new TelegramChatMemberEvent(update.getChatMember()));
        }
    }
}