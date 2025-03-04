/*
---------------------------------------------------------------------------------
File Name : PurchaseHandler

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 05/06/2024
Last Modified : 05/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.components.button.shop;

import eu.fluffici.bot.FluffBOT;
import eu.fluffici.bot.api.beans.shop.ItemDescriptionBean;
import eu.fluffici.bot.api.inventory.InventoryItem;
import eu.fluffici.bot.components.button.shop.impl.OperationType;
import eu.fluffici.bot.components.button.shop.impl.Purchase;
import eu.fluffici.bot.components.button.shop.impl.PurchaseCallback;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.text.NumberFormat;
import java.time.Instant;

import static eu.fluffici.bot.api.IconRegistry.ICON_QUESTION_MARK;

public class PurchaseHandler {

    public static void handlePurchase(CommandInteraction interaction, OperationType type, ItemDescriptionBean item, int quantity, PurchaseCallback callback) {
        ConfirmOperation confirmOperation = new ConfirmOperation(interaction, new Purchase(item, quantity, (type == OperationType.SELL ? item.getSalePrice() * quantity : item.getPriceTokens() * quantity)), callback);
        interaction.getJDA().addEventListener(confirmOperation);


        String title;
        String description;
        String confirmText;
        String cancelText = FluffBOT.getInstance().getLanguageManager().get("common.cancel");

        double totalPrice = item.getPriceTokens() * quantity;
        double totalSellPrice = item.getSalePrice() * quantity;

        if (type == OperationType.SELL) {
            InventoryItem playerItem = FluffBOT.getInstance().getGameServiceManager().getPlayerItem(interaction.getUser(), item);
            if (playerItem != null) {
                int playerItemDurability = playerItem.getDurability();
                int maxDurability = item.getDurability();

                if (playerItemDurability != maxDurability) {
                    double durabilityRatio = (double) playerItemDurability / maxDurability;

                    if (durabilityRatio < 0.5) {
                        interaction.deferReply(true).queue();
                        callback.error("Položka '" + item.getItemName() + "' je příliš poškozená, než aby mohla být prodána (Trvanlivost: " + (int) (durabilityRatio * 100) + "%).");
                        return;
                    }

                    totalSellPrice *= durabilityRatio;

                    if (totalSellPrice <= 0) {
                        interaction.deferReply(true).queue();
                        callback.error("Upravená prodejní cena položky '" + item.getItemName() + "' je příliš nízká na to, aby mohla být prodána.");
                        return;
                    }
                }
            }
        }

        if (type == OperationType.PURCHASE) {
            title = FluffBOT.getInstance().getLanguageManager().get("purchase.confirm.title");
            description = String.format(
                    FluffBOT.getInstance().getLanguageManager().get("purchase.confirm.description"),
                    item.getItemName(), item.getItemDesc(), item.getItemRarity().name().toLowerCase(), quantity, NumberFormat.getCurrencyInstance().format(totalPrice)
            );
            confirmText = FluffBOT.getInstance().getLanguageManager().get("common.pay", NumberFormat.getCurrencyInstance().format(totalPrice));
        } else if (type == OperationType.SELL) {
            title = FluffBOT.getInstance().getLanguageManager().get("sell.confirm.title");
            description = String.format(
                    FluffBOT.getInstance().getLanguageManager().get("sell.confirm.description"),
                    item.getItemName(), FluffBOT.getInstance().getLanguageManager().get(item.getItemDesc()), FluffBOT.getInstance().getLanguageManager().get("rarity.".concat(item.getItemRarity().name().toLowerCase())), quantity, NumberFormat.getCurrencyInstance().format(totalSellPrice)
            );
            confirmText = FluffBOT.getInstance().getLanguageManager().get("common.sell", NumberFormat.getCurrencyInstance().format(totalSellPrice));
        } else {
            callback.error("Invalid operation type");
            throw new IllegalArgumentException("Invalid operation type");
        }

        interaction.replyEmbeds(FluffBOT.getInstance().getEmbed()
                .simpleAuthoredEmbed()
                .setAuthor(title, "https://fluffici.eu", ICON_QUESTION_MARK.getUrl())
                .setDescription(description)
                .setFooter(FluffBOT.getInstance().getLanguageManager().get("confirm.choice.footer"))
                .setTimestamp(Instant.now())
                .build()
        ).addActionRow(
                Button.success("button:confirm_".concat(interaction.getUser().getId()), confirmText),
                Button.danger("button:cancel_".concat(interaction.getUser().getId()), cancelText)
        ).setEphemeral(true).queue();
    }
}