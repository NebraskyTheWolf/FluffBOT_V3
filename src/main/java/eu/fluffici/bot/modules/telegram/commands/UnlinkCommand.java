/*
---------------------------------------------------------------------------------
File Name : UnlinkCommand

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 20/06/2024
Last Modified : 20/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.modules.telegram.commands;

import eu.fluffici.bot.FluffBOT;
import lombok.SneakyThrows;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class UnlinkCommand extends BotCommand {

    private final FluffBOT instance;

    public UnlinkCommand(FluffBOT instance) {
        super("unlink", "Unlink your Telegram account from your Fluffici's account");

        this.instance = instance;
    }

    /**
     * Executes the unlink command to unlink a user's Telegram account from their Fluffici account.
     * This command can only be executed in private chat.
     * If the user is verified, their Telegram account will be unlinked. Otherwise, a message indicating
     * that the account is unverified will be sent.
     *
     * @param telegramClient the Telegram client used to execute API requests
     * @param user the Telegram user executing the command
     * @param chat the chat in which the command is executed
     * @param strings additional arguments passed to the command (not used in this method)
     */
    @Override
    @SneakyThrows
    public void execute(TelegramClient telegramClient, User user, Chat chat, String[] strings) {
        if (chat.getType().equals("private")) {
            boolean isVerified = this.instance.getGameServiceManager().isVerified(user.getId());

            if (isVerified) {
                this.instance.getGameServiceManager().unlinkAccount(user.getId());
                telegramClient.execute(new SendMessage(chat.getId().toString(), this.instance.getLanguageManager().get("command.unlink.unlinked")));
            } else {
                telegramClient.execute(new SendMessage(chat.getId().toString(), this.instance.getLanguageManager().get("command.unlink.unverified")));
            }
        } else {
            telegramClient.execute(new SendMessage(chat.getId().toString(), this.instance.getLanguageManager().get("command.unlink.not_private")));
        }
    }
}