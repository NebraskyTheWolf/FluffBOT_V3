package eu.fluffici.bot.components.commands.misc;

/*
---------------------------------------------------------------------------------
File Name : CommandFursona.java

Developer : vakea
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 02/06/2024
Last Modified : 02/06/2024

---------------------------------------------------------------------------------
*/



/*
                            LICENCE PRO PROPRIETÁRNÍ SOFTWARE
            Verze 1, Organizace: Fluffici, z.s. IČO: 19786077, Rok: 2024
                            PODMÍNKY PRO POUŽÍVÁNÍ

    a. Použití: Software lze používat pouze podle přiložené dokumentace.
    b. Omezení reprodukce: Kopírování softwaru bez povolení je zakázáno.
    c. Omezení distribuce: Distribuce je povolena jen přes autorizované kanály.
    d. Oprávněné kanály: Distribuci určuje výhradně držitel autorských práv.
    e. Nepovolené šíření: Šíření mimo povolené podmínky je zakázáno.
    f. Právní důsledky: Porušení podmínek může vést k právním krokům.
    g. Omezení úprav: Úpravy softwaru jsou zakázány bez povolení.
    h. Rozsah oprávněných úprav: Rozsah úprav určuje držitel autorských práv.
    i. Distribuce upravených verzí: Distribuce upravených verzí je povolena jen s povolením.
    j. Zachování autorských atribucí: Kopie musí obsahovat všechny autorské atribuce.
    k. Zodpovědnost za úpravy: Držitel autorských práv nenese odpovědnost za úpravy.

    Celý text licence je dostupný na adrese:
    https://autumn.fluffici.eu/attachments/xUiAJbvhZaXW3QIiLMFFbVL7g7nPC2nfX7v393UjEn/fluffici_software_license_cz.pdf
*/


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import eu.fluffici.bot.FluffBOT;
import eu.fluffici.bot.api.beans.players.FurSonaBuilder;
import eu.fluffici.bot.api.game.GameId;
import eu.fluffici.bot.api.hooks.FIleUploadCallback;
import eu.fluffici.bot.api.hooks.FileBuilder;
import eu.fluffici.bot.components.commands.Command;
import eu.fluffici.bot.api.interactions.CommandCategory;
import eu.fluffici.bot.components.button.confirm.ConfirmCallback;
import eu.fluffici.bot.components.modal.custom.ModalCallback;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.ModalInteraction;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static eu.fluffici.bot.api.IconRegistry.ICON_BOOK;
import static eu.fluffici.bot.components.button.confirm.ConfirmHandler.handleConfirmation;
import static eu.fluffici.bot.components.modal.custom.CustomModalHandler.handleCustomModal;
import static net.dv8tion.jda.internal.utils.Helpers.listOf;

@SuppressWarnings("All")
public class CommandFursona extends Command {
    public CommandFursona() {
        super("fursona", "Create or see the fursona of someone!", CommandCategory.MISC);

        this.getSubcommandData().add(new SubcommandData("create", "Create a new fursona for yourself or someone else")
                .addOption(OptionType.STRING, "color-code", "Please enter a RGB color code", true)
                .addOption(OptionType.ATTACHMENT, "picture", "Upload your profile picture", true)
                .addOption(OptionType.ATTACHMENT, "reference", "Upload your refsheet", true)
        );

        this.getSubcommandData().add(new SubcommandData("update", "Edit your existing fursona")
                .addOption(OptionType.STRING, "color-code", "Please enter a RGB color code", true)
                .addOption(OptionType.ATTACHMENT, "picture", "Upload your profile picture", true)
                .addOption(OptionType.ATTACHMENT, "reference", "Upload your refsheet", true)
        );

        this.getSubcommandData().add(new SubcommandData("show", "Render your fursona to a embedded message"));
        this.getSubcommandData().add(new SubcommandData("delete", "Delete your existing fursona from FluffBOT"));

        this.getOptions().put("channelRestricted", true);
        this.getOptions().put("rate-limit", true);
    }

    @Override
    public void execute(@NotNull CommandInteraction interaction) {
        String command = interaction.getSubcommandName();

        try {
            switch (command) {
                case "create" -> this.handleCreateOrUpdate(interaction, false, null);
                case "update" -> this.handleUpdate(interaction);
                case "delete" -> this.handleRemove(interaction);
                case "show" -> this.handleLookup(interaction);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the creation of a fursona based on the given CommandInteraction.
     *
     * @param interaction The CommandInteraction object representing the interaction with the command.
     */
    private void handleCreateOrUpdate(@NotNull CommandInteraction interaction, boolean isAlreadyCreated, FurSonaBuilder furSonaBuilder) {
        int color = this.hexToARGB(interaction.getOption("color-code").getAsString());

        if (!isValidColor(color)) {
            interaction.replyEmbeds(this.buildError(getLanguageManager().get("command.fursona.create.color.invalid"))).queue();
            return;
        }

        Message.Attachment picture = interaction.getOption("picture").getAsAttachment();
        Message.Attachment reference = interaction.getOption("reference").getAsAttachment();

        if (!picture.isImage()) {
            interaction.replyEmbeds(buildError(getLanguageManager().get("command.fursona.upload.invalid_file"))).setEphemeral(true).queue();
            return;
        }

        if (!picture.getFileName().endsWith(".png")) {
            interaction.replyEmbeds(buildError(getLanguageManager().get("command.fursona.upload.invalid_file_type"))).setEphemeral(true).queue();
            return;
        }

        if (!reference.isImage()) {
            interaction.replyEmbeds(buildError(getLanguageManager().get("command.fursona.upload.invalid_file"))).setEphemeral(true).queue();
            return;
        }

        if (!reference.getFileName().endsWith(".png")) {
            interaction.replyEmbeds(buildError(getLanguageManager().get("command.fursona.upload.invalid_file_type"))).setEphemeral(true).queue();
            return;
        }

        if (this.isReachingMaxSize(picture,60 * 1024 * 1024)) {
            interaction.replyEmbeds(buildError(getLanguageManager().get("command.fursona.picture.upload.too_big"))).setEphemeral(true).queue();
            return;
        }

        if (this.isReachingMaxSize(picture,200 * 1024 * 1024)) {
            interaction.replyEmbeds(buildError(getLanguageManager().get("command.fursona.reference.upload.too_big"))).setEphemeral(true).queue();
            return;
        }

        List<ActionRow> firstStep = new ArrayList<>();
        if (isAlreadyCreated) {
            firstStep.add(ActionRow.of(
                    TextInput.create("row:age", this.getLanguageManager().get("command.fursona.create.age"), TextInputStyle.SHORT)
                            .setMinLength(2)
                            .setMaxLength(6)
                            .setRequired(true)
                            .setValue(String.valueOf(furSonaBuilder.getCharacterAge()))
                            .build()
            ));
            firstStep.add(ActionRow.of(
                    TextInput.create("row:name", this.getLanguageManager().get("command.fursona.create.name"), TextInputStyle.SHORT)
                            .setMinLength(2)
                            .setMaxLength(64)
                            .setRequired(true)
                            .setValue(furSonaBuilder.getCharacterName())
                            .build()
            ));
            firstStep.add(ActionRow.of(
                    TextInput.create("row:specie", this.getLanguageManager().get("command.fursona.create.specie"), TextInputStyle.SHORT)
                            .setMinLength(2)
                            .setMaxLength(32)
                            .setValue(furSonaBuilder.getCharacterSpecie())
                            .build()
            ));
            firstStep.add(ActionRow.of(
                    TextInput.create("row:gender", this.getLanguageManager().get("command.fursona.create.gender"), TextInputStyle.SHORT)
                            .setMinLength(2)
                            .setMaxLength(32)
                            .setRequired(true)
                            .setValue(furSonaBuilder.getCharacterGender())
                            .build()
            ));
            firstStep.add(ActionRow.of(
                    TextInput.create("row:pronouns", this.getLanguageManager().get("command.fursona.create.pronouns"), TextInputStyle.SHORT)
                            .setMinLength(2)
                            .setMaxLength(32)
                            .setRequired(true)
                            .setValue(furSonaBuilder.getCharacterPronouns())
                            .build()
            ));
        } else {
            firstStep.add(ActionRow.of(
                    TextInput.create("row:age", this.getLanguageManager().get("command.fursona.create.age"), TextInputStyle.SHORT)
                            .setMinLength(2)
                            .setMaxLength(6)
                            .setRequired(true)
                            .build()
            ));
            firstStep.add(ActionRow.of(
                    TextInput.create("row:name", this.getLanguageManager().get("command.fursona.create.name"), TextInputStyle.SHORT)
                            .setMinLength(2)
                            .setMaxLength(64)
                            .setRequired(true)
                            .build()
            ));
            firstStep.add(ActionRow.of(
                    TextInput.create("row:specie", this.getLanguageManager().get("command.fursona.create.specie"), TextInputStyle.SHORT)
                            .setMinLength(2)
                            .setMaxLength(32)
                            .build()
            ));
            firstStep.add(ActionRow.of(
                    TextInput.create("row:gender", this.getLanguageManager().get("command.fursona.create.gender"), TextInputStyle.SHORT)
                            .setMinLength(2)
                            .setMaxLength(32)
                            .setRequired(true)
                            .build()
            ));
            firstStep.add(ActionRow.of(
                    TextInput.create("row:pronouns", this.getLanguageManager().get("command.fursona.create.pronouns"), TextInputStyle.SHORT)
                            .setMinLength(2)
                            .setMaxLength(32)
                            .setRequired(true)
                            .build()
            ));
        }

        handleCustomModal(interaction, (isAlreadyCreated ? "Upravit " + furSonaBuilder.getCharacterName() : "Vytvořit vaši postavu"), firstStep, GameId.generateId(), new ModalCallback() {
            @Override
            public void execute(ModalInteraction modalInteraction, String acceptanceId) throws Exception {
                int age = Integer.parseInt(modalInteraction.getValue("row:age").getAsString());
                String name = modalInteraction.getValue("row:name").getAsString();
                String specie = modalInteraction.getValue("row:specie").getAsString();
                String gender = modalInteraction.getValue("row:gender").getAsString();
                String pronouns = modalInteraction.getValue("row:pronouns").getAsString();

                handleConfirmation(modalInteraction,
                        getLanguageManager().get("command.fursona.create.next_step"),
                        getLanguageManager().get("command.fursona.create.button.continue"),
                        new ConfirmCallback() {
                            @Override
                            public void confirm(ButtonInteraction buttonInteraction) throws Exception {
                                handleCustomModal(buttonInteraction, "Step 2 : Enter the about-me", (isAlreadyCreated ? listOf(ActionRow.of(
                                        TextInput.create("row:description", getLanguageManager().get("command.fursona.create.description"), TextInputStyle.PARAGRAPH)
                                                .setMinLength(2)
                                                .setMaxLength(4000)
                                                .setRequired(true)
                                                .setValue(furSonaBuilder.getCharacterDescriptions())
                                                .build()
                                ), ActionRow.of(
                                        TextInput.create("row:quote", getLanguageManager().get("command.fursona.create.quote"), TextInputStyle.PARAGRAPH)
                                                .setMinLength(2)
                                                .setMaxLength(800)
                                                .setValue(furSonaBuilder.getCharacterQuote())
                                                .build()
                                )) : listOf(ActionRow.of(
                                        TextInput.create("row:description", getLanguageManager().get("command.fursona.create.description"), TextInputStyle.PARAGRAPH)
                                                .setMinLength(2)
                                                .setMaxLength(4000)
                                                .setRequired(true)
                                                .build()
                                ), ActionRow.of(
                                        TextInput.create("row:quote", getLanguageManager().get("command.fursona.create.quote"), TextInputStyle.PARAGRAPH)
                                                .setMinLength(2)
                                                .setMaxLength(800)
                                                .build()
                                ))), acceptanceId, new ModalCallback() {
                                    @Override
                                    public void execute(ModalInteraction modalInteraction1, String acceptanceId) throws Exception {
                                        String description = modalInteraction1.getValue("row:description").getAsString();
                                        String quote = modalInteraction1.getValue("row:quote").getAsString();

                                        FurSonaBuilder character = new FurSonaBuilder();
                                        character.setOwnerId(interaction.getUser());
                                        character.setCharacterAge(age);
                                        character.setCharacterColor(color);
                                        character.setCharacterName(name);
                                        character.setCharacterQuote(quote);
                                        character.setCharacterSpecie(specie);
                                        character.setCharacterGender(gender);
                                        character.setCharacterPronouns(pronouns);
                                        character.setCharacterDescriptions(description);

                                        try {
                                            FluffBOT.getInstance().getFileUploadManager().uploadFile(FileBuilder
                                                    .builder()
                                                    .bucketName("attachments")
                                                    .data(picture.retrieveInputStream().get())
                                                    .fileName(acceptanceId + "_picture.png")
                                                    .build(), new FIleUploadCallback() {
                                                @Override
                                                @SneakyThrows
                                                public void done(Response response) {
                                                    JsonObject data = new Gson().fromJson(response.body().string(), JsonObject.class);

                                                    if (data.has("id")) {
                                                        character.setCharacterPictureURL("https://autumn.fluffici.eu/attachments/".concat(data.get("id").getAsString()));

                                                        FluffBOT.getInstance().getFileUploadManager().uploadFile(FileBuilder
                                                                .builder()
                                                                .bucketName("attachments")
                                                                .data(reference.retrieveInputStream().get())
                                                                .fileName(acceptanceId + "_reference.png")
                                                                .build(), new FIleUploadCallback() {
                                                            @Override
                                                            @SneakyThrows
                                                            public void done(Response response) {
                                                                JsonObject data = new Gson().fromJson(response.body().string(), JsonObject.class);

                                                                if (data.has("id")) {
                                                                    character.setCharacterRefsheetURL("https://autumn.fluffici.eu/attachments/".concat(data.get("id").getAsString()));
                                                                } else if (data.has("error")) {
                                                                    interaction.replyEmbeds(buildSuccess(getLanguageManager().get("command.fursona.reference.upload.error", data.get("error").getAsString()))).setEphemeral(true).queue();
                                                                }
                                                            }

                                                            @Override
                                                            @SneakyThrows
                                                            public void error(Response response) {
                                                                JsonObject data = new Gson().fromJson(response.body().string(), JsonObject.class);

                                                                if (data.has("error")) {
                                                                    interaction.replyEmbeds(buildSuccess(getLanguageManager().get("command.fursona.reference.upload.error", data.get("error").getAsString()))).setEphemeral(true).queue();
                                                                    return;
                                                                }

                                                                interaction.replyEmbeds(buildError(getLanguageManager().get("command.fursona.reference.upload.failed"))).setEphemeral(true).queue();
                                                            }
                                                        });
                                                    } else if (data.has("error")) {
                                                        interaction.replyEmbeds(buildSuccess(getLanguageManager().get("command.fursona.picture.upload.error", data.get("error").getAsString()))).setEphemeral(true).queue();
                                                    }
                                                }

                                                @Override
                                                @SneakyThrows
                                                public void error(Response response) {
                                                    JsonObject data = new Gson().fromJson(response.body().string(), JsonObject.class);

                                                    if (data.has("error")) {
                                                        interaction.replyEmbeds(buildSuccess(getLanguageManager().get("command.fursona.picture.upload.error", data.get("error").getAsString()))).setEphemeral(true).queue();
                                                        return;
                                                    }

                                                    interaction.replyEmbeds(buildError(getLanguageManager().get("command.fursona.picture.upload.failed"))).setEphemeral(true).queue();
                                                }
                                            });
                                        } catch (IOException | ExecutionException | InterruptedException e) {
                                            e.printStackTrace();
                                            interaction.replyEmbeds(buildError(getLanguageManager().get("command.upload.upload.failed"))).setEphemeral(true).queue();
                                        }

                                        if (isAlreadyCreated) {
                                            FluffBOT.getInstance()
                                                    .getGameServiceManager()
                                                    .updateCharacter(furSonaBuilder);
                                            modalInteraction1.replyEmbeds(buildSuccess(getLanguageManager().get("command.fursona.updated", name))).queue();
                                        } else {
                                            FluffBOT.getInstance()
                                                    .getGameServiceManager()
                                                    .createCharacter(character);

                                            FluffBOT.getInstance().getAchievementManager()
                                                    .incrementAchievement(interaction.getUser().getId(), 44, 1);

                                            modalInteraction1.replyEmbeds(buildSuccess(getLanguageManager().get("command.fursona.created", name))).queue();
                                        }
                                    }
                                });
                            }

                            @Override
                            public void cancel(ButtonInteraction interaction) throws Exception {
                                interaction.replyEmbeds(buildError(getLanguageManager().get("command.fursona.create.cancelled"))).setEphemeral(true).queue();
                            }
                        });
            }
        });
    }

    /**
     * Handles the update of a fursona.
     *
     * @param interaction The CommandInteraction object representing the interaction with the command.
     */
    private void handleUpdate(@NotNull CommandInteraction interaction) throws SQLException {
        if (FluffBOT.getInstance().getGameServiceManager().hasCharacter(interaction.getUser())) {
            this.handleCreateOrUpdate(interaction, true, FluffBOT.getInstance()
                    .getGameServiceManager()
                    .getCharacterByOwner(interaction.getUser())
            );
        } else {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.fursona.no_fursona"))).queue();
        }
    }

    /**
     * Handles the removal of a fursona.
     *
     * @param interaction The CommandInteraction object representing the interaction with the command.
     */
    private void handleRemove(@NotNull CommandInteraction interaction) throws SQLException {
        if (FluffBOT.getInstance().getGameServiceManager().hasCharacter(interaction.getUser())) {
            handleConfirmation(interaction,
                    "command.fursona.delete.confirm",
                    this.getLanguageManager().get("command.fursona.delete.button"),
                    new ConfirmCallback() {
                        @Override
                        public void confirm(ButtonInteraction interaction) throws Exception {
                            FurSonaBuilder character = FluffBOT.getInstance()
                                    .getGameServiceManager()
                                    .getCharacterByOwner(interaction.getUser());

                            FluffBOT.getInstance()
                                    .getGameServiceManager()
                                    .deleteCharacter(character);

                            interaction.replyEmbeds(buildSuccess(getLanguageManager().get("command.fursona.remove.removed"))).queue();
                        }

                        @Override
                        public void cancel(ButtonInteraction interaction) throws Exception {
                            interaction.replyEmbeds(buildError(getLanguageManager().get("command.fursona.remove.cancel"))).queue();
                        }
                    });
        } else {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.fursona.no_fursona"))).queue();
        }
    }

    /**
     * Handles the lookup of a player's information.
     *
     * @param interaction The CommandInteraction object representing the interaction with the command.
     * @param player The PlayerBean object representing the player.
     */
    private void handleLookup(@NotNull CommandInteraction interaction) throws SQLException {
        if (FluffBOT.getInstance().getGameServiceManager().hasCharacter(interaction.getUser())) {
            FurSonaBuilder character = FluffBOT.getInstance()
                    .getGameServiceManager()
                    .getCharacterByOwner(interaction.getUser());

            EmbedBuilder characterEmbed = this.getEmbed().simpleAuthoredEmbed();
            characterEmbed.setColor(character.getCharacterColor());
            characterEmbed.setTitle(character.getCharacterName());
            characterEmbed.setDescription(character.getCharacterDescriptions());
            characterEmbed.setThumbnail(character.getCharacterPictureURL());

            if (character.getCharacterRefsheetURL() != null)
                characterEmbed.setImage(character.getCharacterRefsheetURL());

            characterEmbed.addField(this.getLanguageManager().get("command.fursona.create.age"), NumberFormat.getNumberInstance().format(character.getCharacterAge()), true);
            if (character.getCharacterSpecie() != null)
                characterEmbed.addField(this.getLanguageManager().get("command.fursona.create.specie"), character.getCharacterSpecie(), true);
            if (character.getCharacterGender() != null)
                characterEmbed.addField(this.getLanguageManager().get("command.fursona.create.gender"), character.getCharacterGender(), true);
            if (character.getCharacterPronouns() != null)
                characterEmbed.addField(this.getLanguageManager().get("command.fursona.create.pronouns"), character.getCharacterPronouns(), true);

            if (character.getCharacterQuote() != null)
                characterEmbed.setFooter(character.getCharacterQuote(), ICON_BOOK);
            characterEmbed.setTimestamp(Instant.now());

            interaction.replyEmbeds(characterEmbed.build()).queue();
        } else {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.fursona.no_fursona"))).queue();
        }
    }

    /**
     * Checks if the given color is a valid color value.
     *
     * @param color The color value to be checked.
     * @return {@code true} if the color is valid, {@code false} otherwise.
     */
    private boolean isValidColor(int color) {
        return color >= -16777216 && color <= -1;
    }

    /**
     * Converts a hexadecimal color value to an ARGB integer representation.
     *
     * @param hexColor The hexadecimal color value to convert. The value may start with or without a leading '#' character.
     * @return The ARGB integer representation of the given
     */
    private int hexToARGB(@NotNull String hexColor) {
        if (hexColor.startsWith("#")) {
            hexColor = hexColor.substring(1);
        }
        long longVal = Long.parseLong(hexColor, 16);
        return (int) ((255L << 24) | longVal);
    }

    /**
     * Checks if the size of the attachment is reaching the maximum size specified.
     *
     * @param attachment The attachment to check.
     * @param maxSize    The maximum size in bytes.
     * @return {@code true} if the attachment is reaching the maximum size, {@code false} otherwise.
     */
    private boolean isReachingMaxSize(@NotNull Message.Attachment attachment, int maxSize) {
        return (double) attachment.getSize() / (1024 * 1024) > maxSize;
    }
}
