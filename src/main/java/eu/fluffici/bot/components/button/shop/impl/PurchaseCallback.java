/*
---------------------------------------------------------------------------------
File Name : PurchaseCallback

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 05/06/2024
Last Modified : 05/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.components.button.shop.impl;

import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;

public interface PurchaseCallback {
    void cancelled(ButtonInteraction interaction);
    void execute(ButtonInteraction interaction, Purchase purchase);

    void error(String message);
}
