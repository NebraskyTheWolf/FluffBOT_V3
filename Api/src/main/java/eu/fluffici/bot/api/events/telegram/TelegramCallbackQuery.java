/*
---------------------------------------------------------------------------------
File Name : TelegramMessageSendEvent

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 19/06/2024
Last Modified : 19/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api.events.telegram;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;

@Getter
@Setter
@AllArgsConstructor
public class TelegramCallbackQuery {
    private CallbackQuery callbackQuery;
}