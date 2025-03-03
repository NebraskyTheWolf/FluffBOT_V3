/*
---------------------------------------------------------------------------------
File Name : ConfirmOperation

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 06/06/2024
Last Modified : 06/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.components.button.confirm;

import lombok.NonNull;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Objects;

import static eu.fluffici.bot.api.MessageUtil.updateInteraction;

public class ConfirmOperation extends ListenerAdapter {
    private final ConfirmCallback callback;
    private boolean isDeferred;
    private boolean isAutoDelete;
    public ConfirmOperation(ConfirmCallback callback) {
        this.callback = callback;
        this.isDeferred = false;
        this.isAutoDelete = false;
    }

    public ConfirmOperation(ConfirmCallback callback, boolean isDeferred, boolean isAutoDelete) {
        this(callback);
        this.isDeferred = isDeferred;
        this.isAutoDelete = isAutoDelete;
    }

    /**
     * Handles button interactions.
     *
     * @param event The button interaction event.
     */
    @Override
    public void onButtonInteraction(@NonNull ButtonInteractionEvent event) {
        String id = event.getInteraction().getUser().getId();
        String buttonId = event.getButton().getId();
        String buttonUserId = Objects.requireNonNull(event.getButton().getId()).split("_")[1];

        if (!buttonUserId.equals(id)) {
            event.getInteraction().reply("This interaction is not for you.").setEphemeral(true).queue();
            return;
        }

        if (isDeferred) {
            event.deferEdit().queue();
        }

        try {
            if (buttonId.equals("button:confirm_".concat(id))) {
                callback.confirm(event.getInteraction());
            } else if (buttonId.equals("button:cancel_".concat(id))) {
                callback.cancel(event.getInteraction());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        updateInteraction(event.getMessage());

        event.getJDA().removeEventListener(this);
    }
}