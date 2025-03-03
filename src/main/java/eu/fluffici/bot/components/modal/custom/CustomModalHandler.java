/*
---------------------------------------------------------------------------------
File Name : CustomModalHandler

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 14/06/2024
Last Modified : 14/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.components.modal.custom;

import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectInteraction;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.interactions.modals.ModalInteraction;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings("All")
public class CustomModalHandler {
    /**
     * Handles a custom modal interaction.
     *
     * @param interaction    The command interaction triggering the modal.
     * @param title          The title of the modal.
     * @param actionRows     The list of action rows for the modal.
     * @param acceptanceId   The acceptance ID for the modal.
     * @param callback       The modal callback to execute on interaction with the modal.
     */
    public static void handleCustomModal(@NotNull CommandInteraction interaction, String title, List<ActionRow> actionRows, String acceptanceId, ModalCallback callback) {
        ModalOperation modalOperation = new ModalOperation(callback, acceptanceId);
        interaction.getJDA().addEventListener(modalOperation);

        Modal modal = Modal.create("custom:".concat(acceptanceId), title).addActionRows(actionRows).build();
        interaction.replyModal(modal).queue();
    }

    /**
     * Handles a custom modal interaction.
     *
     * @param interaction    The button interaction triggering the modal.
     * @param title          The title of the modal.
     * @param actionRows     The list of action rows for the modal.
     * @param acceptanceId   The acceptance ID for the modal.
     * @param callback       The modal callback to execute on interaction with the modal.
     */
    public static void handleCustomModal(@NotNull ButtonInteraction interaction, String title, List<ActionRow> actionRows, String acceptanceId, ModalCallback callback) {
        ModalOperation modalOperation = new ModalOperation(callback, acceptanceId);
        interaction.getJDA().addEventListener(modalOperation);

        Modal modal = Modal.create("custom:".concat(acceptanceId), title).addActionRows(actionRows).build();
        interaction.replyModal(modal).queue();
    }

    /**
     * Handles a custom modal interaction.
     *
     * @param interaction    The button interaction triggering the modal.
     * @param title          The title of the modal.
     * @param actionRows     The list of action rows for the modal.
     * @param acceptanceId   The acceptance ID for the modal.
     * @param callback       The modal callback to execute on interaction with the modal.
     */
    public static void handleCustomModal(@NotNull StringSelectInteraction interaction, String title, List<ActionRow> actionRows, String acceptanceId, ModalCallback callback) {
        ModalOperation modalOperation = new ModalOperation(callback, acceptanceId);
        interaction.getJDA().addEventListener(modalOperation);

        Modal modal = Modal.create("custom:".concat(acceptanceId), title).addActionRows(actionRows).build();
        interaction.replyModal(modal).queue();
    }
}