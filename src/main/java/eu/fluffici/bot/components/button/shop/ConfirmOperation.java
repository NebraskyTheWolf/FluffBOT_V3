/*
---------------------------------------------------------------------------------
File Name : ConfirmPurchase

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 05/06/2024
Last Modified : 05/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.components.button.shop;

import eu.fluffici.bot.FluffBOT;
import eu.fluffici.bot.components.button.shop.impl.Purchase;
import eu.fluffici.bot.components.button.shop.impl.PurchaseCallback;
import lombok.NonNull;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import java.time.Instant;

import static eu.fluffici.bot.api.IconRegistry.ICON_ALERT_CIRCLE;
import static eu.fluffici.bot.api.IconRegistry.ICON_QUESTION_MARK;
import static eu.fluffici.bot.api.MessageUtil.updateInteraction;

public class ConfirmOperation extends ListenerAdapter {
    private final CommandInteraction interaction;

    private final PurchaseCallback callback;
    private final Purchase purchase;
    public ConfirmOperation(CommandInteraction interaction, Purchase purchase, PurchaseCallback callback) {
        this.interaction = interaction;
        this.purchase = purchase;
        this.callback = callback;
    }

    /**
     * Handles button interactions.
     *
     * @param event The button interaction event.
     */
    @Override
    public void onButtonInteraction(@NonNull ButtonInteractionEvent event) {
        if (!event.getUser().getId().equals(interaction.getUser().getId())) {
            event.getInteraction().replyEmbeds(FluffBOT.getInstance().getEmbed()
                    .simpleAuthoredEmbed()
                            .setAuthor(FluffBOT.getInstance().getLanguageManager().get("common.error"), "https://fluffici.eu", ICON_ALERT_CIRCLE.getUrl())
                            .setDescription(FluffBOT.getInstance().getLanguageManager().get("common.interaction.not_owned"))
                            .setTimestamp(Instant.now())
                            .setFooter(FluffBOT.getInstance().getLanguageManager().get("common.error.footer"), ICON_QUESTION_MARK.getUrl())
                    .build()
            ).setEphemeral(true).queue();
            return;
        }

        String buttonId = event.getButton().getId();

        if (buttonId.equals("button:confirm_".concat(interaction.getUser().getId()))) {
            callback.execute(event.getInteraction(), this.purchase);
        } else if (buttonId.equals("button:cancel_".concat(interaction.getUser().getId()))) {
            callback.cancelled(event.getInteraction());
        }

        updateInteraction(event.getMessage());
        event.getJDA().removeEventListener(this);
    }
}