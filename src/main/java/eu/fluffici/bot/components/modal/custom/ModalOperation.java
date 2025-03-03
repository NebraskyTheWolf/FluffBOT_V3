/*
---------------------------------------------------------------------------------
File Name : ModalOperation

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 14/06/2024
Last Modified : 14/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.components.modal.custom;

import eu.fluffici.bot.FluffBOT;
import lombok.NonNull;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.concurrent.TimeUnit;

/**
 * A class that represents a modal operation and extends the ListenerAdapter class.
 */
public class ModalOperation extends ListenerAdapter {
    private final ModalCallback callback;
    private final String acceptanceId;

    public ModalOperation(ModalCallback callback, String acceptanceId) {
        this.callback = callback;
        this.acceptanceId = acceptanceId;
    }

    @Override
    public void onModalInteraction(@NonNull ModalInteractionEvent event) {
        if (event.getModalId().equals("custom:".concat(this.acceptanceId))) {
            try {
                callback.execute(event.getInteraction(), acceptanceId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        event.getInteraction().getJDA().removeEventListener(this);
    }
}