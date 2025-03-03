/*
---------------------------------------------------------------------------------
File Name : PaginationHandler

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 06/06/2024
Last Modified : 06/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.components.button.paginate;

import eu.fluffici.bot.FluffBOT;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectInteraction;
import net.dv8tion.jda.api.requests.restaction.MessageEditAction;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;

public class PaginationHandler {
    /**
     * Handles pagination for a command interaction.
     *
     * @param interaction        The CommandInteraction object representing the command interaction.
     * @param paginationBuilder  The PaginationBuilder object containing the pages for pagination.
     */
    public static void handlePagination(@NotNull CommandInteraction interaction, PaginationBuilder paginationBuilder) {
        PaginateOperation paginateOperation = new PaginateOperation(paginationBuilder);
        interaction.getJDA().addEventListener(paginateOperation);

        if (!paginationBuilder.hasPages()) {
            interaction.reply("No pages is available.").setEphemeral(true).queue();
            return;
        }

        PageBuilder firstPage = paginationBuilder.getFirstPage();

        if (firstPage.getTexture() != null) {
            interaction.replyEmbeds(firstPage.getMessage()).addActionRow(
                    Button.primary("button:next_".concat(paginationBuilder.getPaginationUniqueId()), "➡"),
                    Button.primary("button:previous_".concat(paginationBuilder.getPaginationUniqueId()), "⬅").asDisabled()
            ).addFiles(FileUpload.fromData(getTexture(firstPage.getTexture()), "item.png")).setEphemeral(paginationBuilder.isEphemeral()).queue();
        } else {
            interaction.replyEmbeds(firstPage.getMessage()).addActionRow(
                    Button.primary("button:next_".concat(paginationBuilder.getPaginationUniqueId()), "➡"),
                    Button.primary("button:previous_".concat(paginationBuilder.getPaginationUniqueId()), "⬅").asDisabled()
            ).setEphemeral(paginationBuilder.isEphemeral()).queue();
        }
    }

    /**
     * Handles pagination for a button interaction.
     *
     * @param interaction         The ButtonInteraction object representing the button interaction.
     * @param paginationBuilder   The PaginationBuilder object containing the pages for pagination.
     */
    public static void handlePagination(@NotNull ButtonInteraction interaction, PaginationBuilder paginationBuilder) {
        PaginateOperation paginateOperation = new PaginateOperation(paginationBuilder);
        interaction.getJDA().addEventListener(paginateOperation);

        PageBuilder firstPage = paginationBuilder.getFirstPage();

        if (firstPage.getTexture() != null) {
            interaction.replyEmbeds(firstPage.getMessage()).addActionRow(
                    Button.primary("button:next_".concat(paginationBuilder.getPaginationUniqueId()), "➡"),
                    Button.primary("button:previous_".concat(paginationBuilder.getPaginationUniqueId()), "⬅").asDisabled()
            ).addFiles(FileUpload.fromData(getTexture(firstPage.getTexture()), "item.png")).setEphemeral(paginationBuilder.isEphemeral()).queue();
        } else {
            interaction.replyEmbeds(firstPage.getMessage()).addActionRow(
                    Button.primary("button:next_".concat(paginationBuilder.getPaginationUniqueId()), "➡"),
                    Button.primary("button:previous_".concat(paginationBuilder.getPaginationUniqueId()), "⬅").asDisabled()
            ).setEphemeral(paginationBuilder.isEphemeral()).queue();
        }
    }

    /**
     * Handles pagination for a button interaction.
     *
     * @param interaction         The StringSelectInteraction object representing the button interaction.
     * @param paginationBuilder   The PaginationBuilder object containing the pages for pagination.
     */
    public static void handlePagination(@NotNull StringSelectInteraction interaction, PaginationBuilder paginationBuilder, boolean isEditOriginal) {
        PaginateOperation paginateOperation = new PaginateOperation(paginationBuilder, isEditOriginal, interaction.getMessage());
        interaction.getJDA().addEventListener(paginateOperation);

        PageBuilder firstPage = paginationBuilder.getFirstPage();
        String paginationId = paginationBuilder.getPaginationUniqueId();

        if (isEditOriginal) {
            interaction.deferEdit().queue();

            Message originalMessage = interaction.getMessage();
            MessageEditAction edit = originalMessage.editMessageEmbeds(firstPage.getMessage());

            if (firstPage.isTextured()) {
                edit.setFiles(FileUpload.fromData(getTexture(firstPage.getTexture()), "item.png"));
            }

            edit.setComponents(
                    ActionRow.of(
                            Button.primary("button:next_" + paginationId, "➡"),
                            Button.primary("button:previous_" + paginationId, "⬅").asDisabled()
                    )
            ).queue();
        } else {
            if (firstPage.getTexture() != null) {
                interaction.replyEmbeds(firstPage.getMessage()).addActionRow(
                        Button.primary("button:next_".concat(paginationBuilder.getPaginationUniqueId()), "➡"),
                        Button.primary("button:previous_".concat(paginationBuilder.getPaginationUniqueId()), "⬅").asDisabled()
                ).addFiles(FileUpload.fromData(getTexture(firstPage.getTexture()), "item.png")).setEphemeral(paginationBuilder.isEphemeral()).queue();
            } else {
                interaction.replyEmbeds(firstPage.getMessage()).addActionRow(
                        Button.primary("button:next_".concat(paginationBuilder.getPaginationUniqueId()), "➡"),
                        Button.primary("button:previous_".concat(paginationBuilder.getPaginationUniqueId()), "⬅").asDisabled()
                ).setEphemeral(paginationBuilder.isEphemeral()).queue();
            }
        }
    }

    private static InputStream getTexture(String itemName) {
        InputStream is = null;
        try {
            is = FluffBOT.getInstance().getClass().getResourceAsStream(String.format("/assets/items/%s.png", itemName));
            if (is == null) {
                is = FluffBOT.getInstance().getClass().getResourceAsStream("/assets/items/missingno.png");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return is;
    }
}