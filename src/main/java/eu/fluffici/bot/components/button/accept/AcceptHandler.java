/*
---------------------------------------------------------------------------------
File Name : AcceptHandler

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 06/06/2024
Last Modified : 06/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.components.button.accept;

import eu.fluffici.bot.FluffBOT;
import eu.fluffici.bot.api.beans.shop.ItemDescriptionBean;
import eu.fluffici.bot.api.inventory.InventoryItem;
import eu.fluffici.bot.components.button.shop.ConfirmOperation;
import eu.fluffici.bot.components.button.shop.impl.OperationType;
import eu.fluffici.bot.components.button.shop.impl.Purchase;
import eu.fluffici.bot.components.button.shop.impl.PurchaseCallback;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;

public class AcceptHandler {
    public static void handleAcceptance(@NotNull CommandInteraction interaction, String title, String description, String confirmText, String acceptanceId, AcceptCallback callback) {
        AcceptOperation confirmOperation = new AcceptOperation(acceptanceId, callback);
        interaction.getJDA().addEventListener(confirmOperation);

        interaction.replyEmbeds(FluffBOT.getInstance().getEmbed()
                .simpleAuthoredEmbed()
                .setTitle(title)
                .setDescription(description)
                .setFooter(FluffBOT.getInstance().getLanguageManager().get("confirm.choice.footer"))
                .build()
        ).addActionRow(
                Button.success("button:accept_".concat(acceptanceId), confirmText)
        ).queue();
    }

    public static void handleAcceptance(@NotNull ButtonInteraction interaction, String title, String description, String confirmText, String acceptanceId, AcceptCallback callback) {
        AcceptOperation confirmOperation = new AcceptOperation(acceptanceId, callback);
        interaction.getJDA().addEventListener(confirmOperation);

        interaction.replyEmbeds(FluffBOT.getInstance().getEmbed()
                .simpleAuthoredEmbed()
                .setTitle(title)
                .setDescription(description)
                .setFooter(FluffBOT.getInstance().getLanguageManager().get("confirm.choice.footer"))
                .build()
        ).addActionRow(
                Button.success("button:accept_".concat(acceptanceId), confirmText)
        ).queue();
    }
}