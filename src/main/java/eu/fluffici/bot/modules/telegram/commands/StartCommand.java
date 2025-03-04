/*
---------------------------------------------------------------------------------
File Name : StartCommand

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 20/06/2024
Last Modified : 20/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.modules.telegram.commands;

import eu.fluffici.bot.FluffBOT;
import eu.fluffici.bot.api.beans.telegram.TelegramVerification;
import eu.fluffici.bot.api.beans.telegram.VerificationStatus;
import eu.fluffici.bot.api.game.GameId;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class StartCommand extends BotCommand {

    private final FluffBOT instance;

    public StartCommand(FluffBOT instance) {
        super("start", "This command will help you linking your Fluffici account");

        this.instance = instance;
    }

    /**
     * Executes the start command in the Telegram bot.
     * This command helps to link a user's Telegram account to their Fluffici account.
     *
     * @param telegramClient The TelegramClient object used to interact with the Telegram API.
     * @param user The User object representing the user who sent the command.
     * @param chat The Chat object representing the chat in which the command was sent.
     * @param strings An array of strings representing any additional arguments passed with the command.
     *                Not used in this method.
     */
    @Override
    @SneakyThrows
    public void execute(TelegramClient telegramClient, User user, @NotNull Chat chat, String[] strings) {
        if (chat.getType().equals("private")) {
            boolean isVerified = this.instance.getGameServiceManager().isVerified(user.getId());
            boolean hasVerificationCode = this.instance.getGameServiceManager().hasVerificationCode(user.getId());

            if (isVerified) {
                telegramClient.execute(new SendMessage(chat.getId().toString(), this.instance.getLanguageManager().get("telegram.verification.already_verified")));
            } else {
                if (hasVerificationCode) {
                    TelegramVerification verification = this.instance.getGameServiceManager().getTelegramVerification(user.getId());

                    telegramClient.execute(new SendMessage(chat.getId().toString(), this.instance.getLanguageManager().get("telegram.verification.verification_code", verification.getVerificationCode())));
                } else {
                    String verificationCode = GameId.generateId();

                    this.instance.getGameServiceManager().createTelegramVerification(new TelegramVerification(
                            user.getId(),
                            user.getUserName(),
                            verificationCode,
                            VerificationStatus.PENDING,
                            null,
                            null
                    ));

                    telegramClient.execute(new SendMessage(chat.getId().toString(), this.instance.getLanguageManager().get("telegram.verification.verification_code", verificationCode)));
                }
            }
        } else {
            telegramClient.execute(new SendMessage(chat.getId().toString(), this.instance.getLanguageManager().get("command.unlink.not_private")));
        }
    }
}