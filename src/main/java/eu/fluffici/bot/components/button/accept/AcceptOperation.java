/*
---------------------------------------------------------------------------------
File Name : AcceptOperation

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 06/06/2024
Last Modified : 06/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.components.button.accept;

import lombok.NonNull;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import static eu.fluffici.bot.api.MessageUtil.updateInteraction;

public class AcceptOperation extends ListenerAdapter {
    private final String acceptanceId;
    private final AcceptCallback callback;
    public AcceptOperation(String acceptanceId, AcceptCallback callback) {
        this.acceptanceId = acceptanceId;
        this.callback = callback;
    }

    /**
     * Handles button interactions.
     *
     * @param event The button interaction event.
     */
    @Override
    public void onButtonInteraction(@NonNull ButtonInteractionEvent event) {
        String buttonId = event.getButton().getId();

        if (buttonId.equals("button:accept_".concat(this.acceptanceId))) {
            try {
                callback.execute(event.getInteraction(), this.acceptanceId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        updateInteraction(event.getMessage());

        event.getJDA().removeEventListener(this);
    }
}