/*
---------------------------------------------------------------------------------
File Name : AcceptCallback

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 06/06/2024
Last Modified : 06/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.components.button.confirm;

import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;

public interface ConfirmCallback {
    void confirm(ButtonInteraction interaction) throws Exception;
    void cancel(ButtonInteraction interaction) throws Exception;
}
