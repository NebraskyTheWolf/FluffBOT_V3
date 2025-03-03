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

package eu.fluffici.bot.components.button.confirm;

import eu.fluffici.bot.FluffBOT;
import eu.fluffici.furraid.FurRaidDB;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.context.MessageContextInteraction;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectInteraction;
import net.dv8tion.jda.api.interactions.modals.ModalInteraction;
import org.jetbrains.annotations.NotNull;

import static eu.fluffici.bot.api.IconRegistry.ICON_QUESTION_MARK;

public class ConfirmHandler {
    /**
     * Handles a confirmation operation.
     *
     * @param interaction The command interaction.
     * @param description The description of the confirmation operation.
     * @param callback    The callback for handling confirm and cancel actions.
     */
    public static void handleConfirmation(@NotNull CommandInteraction interaction, String description, ConfirmCallback callback) {
        ConfirmOperation confirmOperation = new ConfirmOperation(callback);
        interaction.getJDA().addEventListener(confirmOperation);

        interaction.replyEmbeds(FluffBOT.getInstance().getEmbed()
                .simpleAuthoredEmbed()
                .setAuthor(FluffBOT.getInstance().getLanguageManager().get("common.confirm.operation"), "https://fluffici.eu", ICON_QUESTION_MARK)
                .setDescription(FluffBOT.getInstance().getLanguageManager().get(description))
                .setFooter(FluffBOT.getInstance().getLanguageManager().get("confirm.choice.footer"))
                .build()
        ).addActionRow(
                Button.success("button:confirm_".concat(interaction.getUser().getId()), FluffBOT.getInstance().getLanguageManager().get("common.confirm")),
                Button.danger("button:cancel_".concat(interaction.getUser().getId()), FluffBOT.getInstance().getLanguageManager().get("common.cancel"))
        ).setEphemeral(true).queue();
    }

    /**
     * Handles a confirmation operation.
     *
     * @param interaction The command interaction.
     * @param description The description of the confirmation operation.
     * @param callback    The callback for handling confirm and cancel actions.
     */
    public static void handleConfirmation(@NotNull StringSelectInteraction interaction, String description, ConfirmCallback callback) {
        ConfirmOperation confirmOperation = new ConfirmOperation(callback);
        interaction.getJDA().addEventListener(confirmOperation);

        interaction.replyEmbeds(FluffBOT.getInstance().getEmbed()
                .simpleAuthoredEmbed()
                .setAuthor(FluffBOT.getInstance().getLanguageManager().get("common.confirm.operation"), "https://fluffici.eu", ICON_QUESTION_MARK)
                .setDescription(FluffBOT.getInstance().getLanguageManager().get(description))
                .setFooter(FluffBOT.getInstance().getLanguageManager().get("confirm.choice.footer"))
                .build()
        ).addActionRow(
                Button.success("button:confirm_".concat(interaction.getUser().getId()), FluffBOT.getInstance().getLanguageManager().get("common.confirm")),
                Button.danger("button:cancel_".concat(interaction.getUser().getId()), FluffBOT.getInstance().getLanguageManager().get("common.cancel"))
        ).setEphemeral(true).queue();
    }

    /**
     * Handles a confirmation operation.
     *
     * @param interaction    The command interaction.
     * @param description    The description of the confirmation operation.
     * @param confirmButton  The label for the confirm button.
     * @param callback       The callback for handling confirm and cancel actions.
     */
    public static void handleConfirmation(@NotNull CommandInteraction interaction, String description, String confirmButton, ConfirmCallback callback) {
        ConfirmOperation confirmOperation = new ConfirmOperation(callback);
        interaction.getJDA().addEventListener(confirmOperation);

        interaction.replyEmbeds(FluffBOT.getInstance().getEmbed()
                .simpleAuthoredEmbed()
                .setAuthor(FluffBOT.getInstance().getLanguageManager().get("common.confirm.operation"), "https://fluffici.eu", ICON_QUESTION_MARK)
                .setDescription(FluffBOT.getInstance().getLanguageManager().get(description))
                .setFooter(FluffBOT.getInstance().getLanguageManager().get("confirm.choice.footer"))
                .build()
        ).addActionRow(
                Button.success("button:confirm_".concat(interaction.getUser().getId()), confirmButton),
                Button.danger("button:cancel_".concat(interaction.getUser().getId()), FluffBOT.getInstance().getLanguageManager().get("common.cancel"))
        ).setEphemeral(true).queue();
    }

    /**
     * Handles a confirmation operation.
     *
     * @param interaction    The command interaction.
     * @param description    The description of the confirmation operation.
     * @param confirmButton  The label for the confirm button.
     * @param callback       The callback for handling confirm and cancel actions.
     */
    public static void handleConfirmation(@NotNull StringSelectInteraction interaction, String description, String confirmButton, ConfirmCallback callback) {
        ConfirmOperation confirmOperation = new ConfirmOperation(callback);
        interaction.getJDA().addEventListener(confirmOperation);

        interaction.replyEmbeds(FluffBOT.getInstance().getEmbed()
                .simpleAuthoredEmbed()
                .setAuthor(FluffBOT.getInstance().getLanguageManager().get("common.confirm.operation"), "https://fluffici.eu", ICON_QUESTION_MARK)
                .setDescription(FluffBOT.getInstance().getLanguageManager().get(description))
                .setFooter(FluffBOT.getInstance().getLanguageManager().get("confirm.choice.footer"))
                .build()
        ).addActionRow(
                Button.success("button:confirm_".concat(interaction.getUser().getId()), confirmButton),
                Button.danger("button:cancel_".concat(interaction.getUser().getId()), FluffBOT.getInstance().getLanguageManager().get("common.cancel"))
        ).setEphemeral(true).queue();
    }

    /**
     * Handles a confirmation operation.
     *
     * @param interaction   The command interaction.
     * @param description   The description of the confirmation operation.
     * @param confirmButton The label for the confirm button.
     * @param callback      The callback for handling confirm and cancel actions.
     */
    public static void handleConfirmation(@NotNull MessageContextInteraction interaction, String description, String confirmButton, ConfirmCallback callback) {
        ConfirmOperation confirmOperation = new ConfirmOperation(callback);
        interaction.getJDA().addEventListener(confirmOperation);

        interaction.replyEmbeds(FluffBOT.getInstance().getEmbed()
                .simpleAuthoredEmbed()
                .setAuthor(FluffBOT.getInstance().getLanguageManager().get("common.confirm.operation"), "https://fluffici.eu", ICON_QUESTION_MARK)
                .setDescription(FluffBOT.getInstance().getLanguageManager().get(description))
                .setFooter(FluffBOT.getInstance().getLanguageManager().get("confirm.choice.footer"))
                .build()
        ).addActionRow(
                Button.success("button:confirm_".concat(interaction.getUser().getId()), confirmButton),
                Button.danger("button:cancel_".concat(interaction.getUser().getId()), FluffBOT.getInstance().getLanguageManager().get("common.cancel"))
        ).setEphemeral(true).queue();
    }

    /**
     * Handles a confirmation operation.
     *
     * @param interaction   The command interaction.
     * @param description   The description of the confirmation operation.
     * @param confirmButton The label for the confirm button.
     * @param callback      The callback for handling confirm and cancel actions.
     */
    public static void handleConfirmation(@NotNull ButtonInteraction interaction, String description, String confirmButton, ConfirmCallback callback) {
        ConfirmOperation confirmOperation = new ConfirmOperation(callback);
        interaction.getJDA().addEventListener(confirmOperation);

        interaction.replyEmbeds(FluffBOT.getInstance().getEmbed()
                .simpleAuthoredEmbed()
                .setAuthor(FluffBOT.getInstance().getLanguageManager().get("common.confirm.operation"), "https://fluffici.eu", ICON_QUESTION_MARK)
                .setDescription(FluffBOT.getInstance().getLanguageManager().get(description))
                .setFooter(FluffBOT.getInstance().getLanguageManager().get("confirm.choice.footer"))
                .build()
        ).addActionRow(
                Button.success("button:confirm_".concat(interaction.getUser().getId()), confirmButton),
                Button.danger("button:cancel_".concat(interaction.getUser().getId()), FluffBOT.getInstance().getLanguageManager().get("common.cancel"))
        ).setEphemeral(true).queue();
    }

    /**
     * Handles a confirmation operation.
     *
     * @param interaction The ModalInteraction object representing the interaction.
     * @param description The description of the confirmation operation.
     * @param confirmButton The label for the confirm button.
     * @param callback The callback for handling confirm and cancel actions.
     */
    public static void handleConfirmation(@NotNull ModalInteraction interaction, String description, String confirmButton, ConfirmCallback callback) {
        ConfirmOperation confirmOperation = new ConfirmOperation(callback);
        interaction.getJDA().addEventListener(confirmOperation);

        interaction.replyEmbeds(FluffBOT.getInstance().getEmbed()
                .simpleAuthoredEmbed()
                .setAuthor(FluffBOT.getInstance().getLanguageManager().get("common.confirm.operation"), "https://fluffici.eu", ICON_QUESTION_MARK)
                .setDescription(FluffBOT.getInstance().getLanguageManager().get(description))
                .setFooter(FluffBOT.getInstance().getLanguageManager().get("confirm.choice.footer"))
                .build()
        ).addActionRow(
                Button.success("button:confirm_".concat(interaction.getUser().getId()), confirmButton),
                Button.danger("button:cancel_".concat(interaction.getUser().getId()), FluffBOT.getInstance().getLanguageManager().get("common.cancel"))
        ).setEphemeral(true).queue();
    }

    public static void handleConfirmationFurRaid(@NotNull StringSelectInteraction interaction, String description, String confirmButton, ConfirmCallback callback) {
        ConfirmOperation confirmOperation = new ConfirmOperation(callback);
        interaction.getJDA().addEventListener(confirmOperation);

        interaction.replyEmbeds(FurRaidDB.getInstance().getEmbed()
                .simpleAuthoredEmbed()
                .setAuthor(FurRaidDB.getInstance().getLanguageManager().get("common.confirm.operation"), "https://fluffici.eu", ICON_QUESTION_MARK)
                .setDescription(FurRaidDB.getInstance().getLanguageManager().get(description))
                .setFooter(FurRaidDB.getInstance().getLanguageManager().get("confirm.choice.footer"))
                .build()
        ).addActionRow(
                Button.success("button:confirm_".concat(interaction.getUser().getId()), confirmButton),
                Button.danger("button:cancel_".concat(interaction.getUser().getId()), FurRaidDB.getInstance().getLanguageManager().get("common.cancel"))
        ).setEphemeral(true).queue();
    }

    public static void handleConfirmationFurRaid(@NotNull ButtonInteraction interaction, String description, String confirmButton, ConfirmCallback callback) {
        ConfirmOperation confirmOperation = new ConfirmOperation(callback);
        interaction.getJDA().addEventListener(confirmOperation);

        interaction.replyEmbeds(FurRaidDB.getInstance().getEmbed()
                .simpleAuthoredEmbed()
                .setAuthor(FurRaidDB.getInstance().getLanguageManager().get("common.confirm.operation"), "https://fluffici.eu", ICON_QUESTION_MARK)
                .setDescription(FurRaidDB.getInstance().getLanguageManager().get(description))
                .setFooter(FurRaidDB.getInstance().getLanguageManager().get("confirm.choice.footer"))
                .build()
        ).addActionRow(
                Button.success("button:confirm_".concat(interaction.getUser().getId()), confirmButton),
                Button.danger("button:cancel_".concat(interaction.getUser().getId()), FurRaidDB.getInstance().getLanguageManager().get("common.cancel"))
        ).setEphemeral(true).queue();
    }

    public static void handleConfirmationFurRaid(@NotNull ModalInteraction interaction, String description, String confirmButton, ConfirmCallback callback) {
        ConfirmOperation confirmOperation = new ConfirmOperation(callback);
        interaction.getJDA().addEventListener(confirmOperation);

        interaction.replyEmbeds(FurRaidDB.getInstance().getEmbed()
                .simpleAuthoredEmbed()
                .setAuthor(FurRaidDB.getInstance().getLanguageManager().get("common.confirm.operation"), "https://fluffici.eu", ICON_QUESTION_MARK)
                .setDescription(FurRaidDB.getInstance().getLanguageManager().get(description))
                .setFooter(FurRaidDB.getInstance().getLanguageManager().get("confirm.choice.footer"))
                .build()
        ).addActionRow(
                Button.success("button:confirm_".concat(interaction.getUser().getId()), confirmButton),
                Button.danger("button:cancel_".concat(interaction.getUser().getId()), FurRaidDB.getInstance().getLanguageManager().get("common.cancel"))
        ).setEphemeral(true).queue();
    }

    public static void handleConfirmationFurRaid(@NotNull CommandInteraction interaction, String description, String confirmButton, ConfirmCallback callback) {
        ConfirmOperation confirmOperation = new ConfirmOperation(callback);
        interaction.getJDA().addEventListener(confirmOperation);

        interaction.replyEmbeds(FurRaidDB.getInstance().getEmbed()
                .simpleAuthoredEmbed()
                .setAuthor(FurRaidDB.getInstance().getLanguageManager().get("common.confirm.operation"), "https://fluffici.eu", ICON_QUESTION_MARK)
                .setDescription(FurRaidDB.getInstance().getLanguageManager().get(description))
                .setFooter(FurRaidDB.getInstance().getLanguageManager().get("confirm.choice.footer"))
                .build()
        ).addActionRow(
                Button.success("button:confirm_".concat(interaction.getUser().getId()), confirmButton),
                Button.danger("button:cancel_".concat(interaction.getUser().getId()), FurRaidDB.getInstance().getLanguageManager().get("common.cancel"))
        ).setEphemeral(true).queue();
    }


    /**
     * Handles a confirmation operation.
     *
     * @param interaction     The command interaction.
     * @param description     The description of the confirmation operation.
     * @param confirmButton   The label for the confirm button.
     * @param callback        The callback for handling confirm and cancel actions.
     * @param isDeferred      Indicates whether the reply should be deferred.
     */
    public static void handleConfirmation(@NotNull CommandInteraction interaction, String description, String confirmButton, ConfirmCallback callback, boolean isDeferred, boolean isAutoDelete) {
        ConfirmOperation confirmOperation = new ConfirmOperation(callback, isDeferred, isAutoDelete);
        interaction.getJDA().addEventListener(confirmOperation);

        interaction.replyEmbeds(FluffBOT.getInstance().getEmbed()
                .simpleAuthoredEmbed()
                .setAuthor(FluffBOT.getInstance().getLanguageManager().get("common.confirm.operation"), "https://fluffici.eu", ICON_QUESTION_MARK)
                .setDescription(FluffBOT.getInstance().getLanguageManager().get(description))
                .setFooter(FluffBOT.getInstance().getLanguageManager().get("confirm.choice.footer"))
                .build()
        ).addActionRow(
                Button.success("button:confirm_".concat(interaction.getUser().getId()), confirmButton),
                Button.danger("button:cancel_".concat(interaction.getUser().getId()), FluffBOT.getInstance().getLanguageManager().get("common.cancel"))
        ).setEphemeral(true).queue();
    }

    /**
     * Handles a confirmation operation.
     *
     * @param interaction  The command interaction.
     * @param description  The description of the confirmation operation.
     * @param confirmButton  The label for the confirm button.
     * @param callback  The callback for handling confirm and cancel actions.
     * @param isDeferred  Indicates whether the reply should be deferred.
     * @param isEphemeral  Indicates whether the reply should be ephemeral.
     * @param isMention  Indicates whether to mention the user in the reply.
     */
    public static void handleConfirmation(@NotNull CommandInteraction interaction, String description, String confirmButton, ConfirmCallback callback, boolean isDeferred, boolean isEphemeral, boolean isMention) {
        ConfirmOperation confirmOperation = new ConfirmOperation(callback, isDeferred, false);
        interaction.getJDA().addEventListener(confirmOperation);

        if (isMention) {
            interaction.replyEmbeds(FluffBOT.getInstance().getEmbed()
                    .simpleAuthoredEmbed()
                    .setAuthor(FluffBOT.getInstance().getLanguageManager().get("common.confirm.operation"), "https://fluffici.eu", ICON_QUESTION_MARK)
                    .setDescription(FluffBOT.getInstance().getLanguageManager().get(description))
                    .setFooter(FluffBOT.getInstance().getLanguageManager().get("confirm.choice.footer"))
                    .build()
            ).addActionRow(
                    Button.success("button:confirm_".concat(interaction.getUser().getId()), confirmButton),
                    Button.danger("button:cancel_".concat(interaction.getUser().getId()), FluffBOT.getInstance().getLanguageManager().get("common.cancel"))
            ).setEphemeral(isEphemeral).setContent(interaction.getUser().getAsMention()).queue();
        } else {
            interaction.replyEmbeds(FluffBOT.getInstance().getEmbed()
                    .simpleAuthoredEmbed()
                    .setAuthor(FluffBOT.getInstance().getLanguageManager().get("common.confirm.operation"), "https://fluffici.eu", ICON_QUESTION_MARK)
                    .setDescription(FluffBOT.getInstance().getLanguageManager().get(description))
                    .setFooter(FluffBOT.getInstance().getLanguageManager().get("confirm.choice.footer"))
                    .build()
            ).addActionRow(
                    Button.success("button:confirm_".concat(interaction.getUser().getId()), confirmButton),
                    Button.danger("button:cancel_".concat(interaction.getUser().getId()), FluffBOT.getInstance().getLanguageManager().get("common.cancel"))
            ).setEphemeral(isEphemeral).queue();
        }
    }
}