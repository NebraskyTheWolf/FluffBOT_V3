/*
---------------------------------------------------------------------------------
File Name : CommandGlobalBlacklist

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 18/06/2024
Last Modified : 18/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.furraid.components.commands.staff;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import eu.fluffici.bot.api.beans.furraid.Blacklist;
import eu.fluffici.bot.api.beans.furraid.GuildSettings;
import eu.fluffici.bot.api.game.GameId;
import eu.fluffici.bot.api.hooks.FIleUploadCallback;
import eu.fluffici.bot.api.hooks.FileBuilder;
import eu.fluffici.bot.api.hooks.furraid.BlacklistBuilder;
import eu.fluffici.bot.api.interactions.CommandCategory;
import eu.fluffici.bot.api.interactions.FCommand;
import eu.fluffici.bot.components.button.confirm.ConfirmCallback;
import eu.fluffici.furraid.FurRaidDB;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static eu.fluffici.bot.api.IconRegistry.ICON_CIRCLE_SLASHED;
import static eu.fluffici.bot.components.button.confirm.ConfirmHandler.handleConfirmation;
import static eu.fluffici.bot.components.button.confirm.ConfirmHandler.handleConfirmationFurRaid;
import static eu.fluffici.bot.components.modal.custom.CustomModalHandler.handleCustomModal;
import static net.dv8tion.jda.internal.utils.Helpers.listOf;

public class CommandGlobalBlacklist extends FCommand {
    public CommandGlobalBlacklist() {
        super("global-blacklist", "Managing the global blacklist of FurRaidDB", CommandCategory.STAFF);

        this.setPermissions(DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS));
        this.getOptions().put("isStaff", true);
        this.getOptions().put("noSelfUser", true);

        this.getSubcommandData().add(new SubcommandData("add", "Add a member to the global blacklist")
                .addOption(OptionType.USER, "user", "Select a member", true)
                .addOption(OptionType.ATTACHMENT, "attachment", "The proof for the blacklist", true)
        );

        this.getSubcommandData().add(new SubcommandData("bulk-add", "Add multiples users at once to the global blacklist")
                .addOption(OptionType.STRING, "users-id", "Enter all ID separated with a ','", true)
                .addOption(OptionType.ATTACHMENT, "attachment", "The proof for the blacklist", true)
        );

        this.getSubcommandData().add(new SubcommandData("remove", "Remove a member from the global blacklist")
                .addOption(OptionType.STRING, "user-id", "Select a member", true)
        );

        this.getSubcommandData().add(new SubcommandData("show", "Checking if a user is globally blacklisted")
                .addOption(OptionType.STRING, "user-id", "Select a member", true)
        );
    }

    /**
     * Executes the command based on the given CommandInteraction.
     *
     * @param interaction The CommandInteraction representing the event triggered by the user.
     */
    @Override
    @SuppressWarnings("All")
    public void execute(CommandInteraction interaction, GuildSettings settings) {
        String command = interaction.getSubcommandName();

        switch (command) {
            case "add" -> this.handleAddBlacklist(interaction);
            case "bulk-add" -> this.handleBulkAddBlacklist(interaction);
            case "remove" -> this.handleRemoveBlacklist(interaction);
            case "show" -> this.handleShowBlacklist(interaction);
        }
    }

    /**
     * Handles the bulk addition of users to the blacklist.
     *
     * @param interaction The CommandInteraction representing the event triggered by the user.
     */
    @SuppressWarnings("All")
    private void handleBulkAddBlacklist(@NotNull CommandInteraction interaction) {
        String optionUsersId = interaction.getOption("users-id").getAsString();
        if (optionUsersId == null || optionUsersId.isEmpty()) {
            interaction.replyEmbeds(buildError(getLanguageManager().get("command.global_blacklist.add.bulk.no_users"))).setEphemeral(true).queue();
            return;
        }

        Message.Attachment attachment = interaction.getOption("attachment").getAsAttachment();

        List<UserSnowflake> usersSelected = new ArrayList<>();

        String[] users = optionUsersId.split(",");
        for (String userId : users) {
            UserSnowflake userSnowflake = UserSnowflake.fromId(userId);
            Blacklist blacklist = FurRaidDB.getInstance().getBlacklistManager().fetchGlobalBlacklist(userSnowflake);
            if (blacklist != null)
                continue;

            usersSelected.add(userSnowflake);
        }

        handleCustomModal(interaction, this.getLanguageManager().get("command.global_blacklist.modal.bulk.title", usersSelected.size()), listOf(ActionRow.of(
                TextInput.create("row:reason", this.getLanguageManager().get("common.reason"), TextInputStyle.PARAGRAPH)
                        .setMaxLength(1000)
                        .setMinLength(2)
                        .build()
        )), GameId.generateId(), (modalInteraction, acceptanceId) -> {
            String reason = modalInteraction.getValue("row:reason").getAsString();
            String selected = String.join("\n", usersSelected.stream().map(IMentionable::getAsMention).toList());

            handleConfirmationFurRaid(modalInteraction,
                    this.getLanguageManager().get("command.global_blacklist.bulk.confirm", selected, reason),
                    this.getLanguageManager().get("command.global_blacklist.add.confirm.button"),
                    new ConfirmCallback() {
                        @Override
                        public void confirm(ButtonInteraction buttonInteraction) throws Exception {
                            FurRaidDB.getInstance().getFileUploadManager().uploadFile(FileBuilder
                                    .builder()
                                    .fileName(GameId.generateId().concat(".png"))
                                    .data(attachment.retrieveInputStream().get())
                                    .bucketName("attachments")
                                    .build(), new FIleUploadCallback() {
                                @Override
                                @SneakyThrows
                                public void done(Response response) {
                                    JsonObject data = new Gson().fromJson(response.body().string(), JsonObject.class);

                                    if (data.has("id")) {
                                        for (UserSnowflake user : usersSelected) {
                                            BlacklistBuilder blacklist = BlacklistBuilder
                                                    .builder()
                                                    .user(user)
                                                    .reason(reason)
                                                    .attachmentUrl("https://autumn.fluffici.eu/attachments/".concat(data.get("id").getAsString()))
                                                    .author(interaction.getUser())
                                                    .build();

                                            FurRaidDB.getInstance().getBlacklistManager().addGlobalBlacklist(blacklist);
                                            handleLogging(interaction, blacklist);
                                        }

                                        buttonInteraction.replyEmbeds(buildSuccess(getLanguageManager().get("command.global_blacklist.add.bulk.success",selected, reason))).queue();
                                    }
                                }

                                @Override
                                @SneakyThrows
                                public void error(Response response) {
                                    JsonObject data = new Gson().fromJson(response.body().string(), JsonObject.class);
                                    if (data.has("error")) {
                                        buttonInteraction.replyEmbeds(buildError(getLanguageManager().get("command.global_blacklist.add.attachment_upload.failed", data.get("error").getAsString()))).queue();
                                    } else {
                                        buttonInteraction.replyEmbeds(buildError(getLanguageManager().get("command.global_blacklist.add.attachment_upload.failed.unknown"))).queue();
                                    }
                                }
                            });
                        }

                        @Override
                        public void cancel(ButtonInteraction buttonInteraction) throws Exception {
                            buttonInteraction.replyEmbeds(buildError(getLanguageManager().get("command.global_blacklist.add.bulk.cancelled"))).queue();
                        }
                    }
            );
        });
    }

    /**
     * Handles the addition of a member to the global blacklist.
     *
     * @param interaction The CommandInteraction representing the event triggered by the user.
     */
    @SuppressWarnings("All")
    private void handleAddBlacklist(CommandInteraction interaction) {
        User user = interaction.getOption("user").getAsUser();
        Message.Attachment attachment = interaction.getOption("attachment").getAsAttachment();

        Blacklist blacklist = FurRaidDB.getInstance().getBlacklistManager().fetchGlobalBlacklist(user);

        if (blacklist != null) {
            interaction.replyEmbeds(buildError(getLanguageManager().get("command.global_blacklist.remove.already_blacklisted", user.getId()))).setEphemeral(true).queue();
            return;
        }

        handleCustomModal(interaction, this.getLanguageManager().get("command.global_blacklist.modal.title"), listOf(ActionRow.of(
                TextInput.create("row:reason", this.getLanguageManager().get("common.reason"), TextInputStyle.PARAGRAPH)
                        .setMaxLength(1000)
                        .setMinLength(2)
                        .build()
        )), GameId.generateId(), (modalInteraction, acceptanceId) -> {
            String reason = modalInteraction.getValue("row:reason").getAsString();

            handleConfirmationFurRaid(modalInteraction,
                    this.getLanguageManager().get("command.global_blacklist.add.confirm", user.getAsMention(), reason),
                    this.getLanguageManager().get("command.global_blacklist.add.confirm.button"),
                    new ConfirmCallback() {
                        @Override
                        public void confirm(ButtonInteraction buttonInteraction) throws Exception {
                            FurRaidDB.getInstance().getFileUploadManager().uploadFile(FileBuilder
                                    .builder()
                                            .fileName(GameId.generateId().concat(".png"))
                                            .data(attachment.retrieveInputStream().get())
                                            .bucketName("attachments")
                                    .build(), new FIleUploadCallback() {
                                @Override
                                @SneakyThrows
                                public void done(Response response) {
                                    JsonObject data = new Gson().fromJson(response.body().string(), JsonObject.class);

                                    BlacklistBuilder blacklist = BlacklistBuilder
                                            .builder()
                                            .user(user)
                                            .reason(reason)
                                            .attachmentUrl("https://autumn.fluffici.eu/attachments/".concat(data.get("id").getAsString()))
                                            .author(interaction.getUser())
                                            .build();

                                    FurRaidDB.getInstance().getBlacklistManager().addGlobalBlacklist(blacklist);
                                    handleLogging(interaction, blacklist);
                                    buttonInteraction.replyEmbeds(buildSuccess(getLanguageManager().get("command.global_blacklist.add.success", user.getAsMention(), reason))).queue();
                                }

                                @Override
                                @SneakyThrows
                                public void error(Response response) {
                                    JsonObject data = new Gson().fromJson(response.body().string(), JsonObject.class);
                                    if (data.has("error")) {
                                        buttonInteraction.replyEmbeds(buildError(getLanguageManager().get("command.global_blacklist.add.attachment_upload.failed", data.get("error").getAsString()))).queue();
                                    } else {
                                        buttonInteraction.replyEmbeds(buildError(getLanguageManager().get("command.global_blacklist.add.attachment_upload.failed.unknown"))).queue();
                                    }
                                }
                            });
                        }

                        @Override
                        public void cancel(ButtonInteraction buttonInteraction) throws Exception {
                            buttonInteraction.replyEmbeds(buildError(getLanguageManager().get("command.global_blacklist.add.cancelled"))).queue();
                        }
                    }
            );
        });
    }

    /**
     * Handles the removal of a member from the global blacklist.
     *
     * @param interaction The CommandInteraction representing the event triggered by the user.
     */
    @SuppressWarnings("All")
    private void handleRemoveBlacklist(@NotNull CommandInteraction interaction) {
        UserSnowflake user = UserSnowflake.fromId(interaction.getOption("user-id").getAsString());

        Blacklist blacklist = FurRaidDB.getInstance().getBlacklistManager().fetchGlobalBlacklist(user);

        if (blacklist == null) {
            interaction.replyEmbeds(buildError(getLanguageManager().get("command.global_blacklist.remove.not_found", user.getId()))).queue();
            return;
        }

        handleConfirmationFurRaid(interaction,
                this.getLanguageManager().get("command.global_blacklist.remove.confirm", user.getAsMention()),
                this.getLanguageManager().get("command.global_blacklist.remove.confirm.button"),
                new ConfirmCallback() {
                    @Override
                    public void confirm(ButtonInteraction buttonInteraction) throws Exception {
                        FurRaidDB.getInstance().getBlacklistManager().removeGlobalBlacklist(user);

                        buttonInteraction.replyEmbeds(buildSuccess(getLanguageManager().get("command.global_blacklist.remove.success", user.getId()))).queue();
                    }

                    @Override
                    public void cancel(ButtonInteraction buttonInteraction) throws Exception {
                        buttonInteraction.replyEmbeds(buildError(getLanguageManager().get("command.global_blacklist.remove.cancelled"))).queue();
                    }
                }
        );
    }

    /**
     * Handles the display of the global blacklist.
     *
     * @param interaction The CommandInteraction representing the event triggered by the user.
     */
    @SuppressWarnings("All")
    private void handleShowBlacklist(@NotNull CommandInteraction interaction) {
        UserSnowflake user = UserSnowflake.fromId(interaction.getOption("user-id").getAsString());

        Blacklist blacklist = FurRaidDB.getInstance().getBlacklistManager().fetchGlobalBlacklist(user);

        if (blacklist != null) {
            EmbedBuilder blacklistedMessage = this.getEmbed().simpleAuthoredEmbed();
            blacklistedMessage.setColor(Color.decode("#9412d5"));
            blacklistedMessage.setAuthor(this.getLanguageManager().get("common.globally_blacklisted.title"), "https://frdb.fluffici.eu", ICON_CIRCLE_SLASHED);
            blacklistedMessage.setDescription(this.getLanguageManager().get("common.globally_blacklisted.description"));

            if (blacklist.getAttachmentUrl() != null)
                blacklistedMessage.setImage(blacklist.getAttachmentUrl());

            blacklistedMessage.setTimestamp(Instant.now());

            blacklistedMessage.addField(this.getLanguageManager().get("common.user.id"), blacklist.getUser().getId(), true);
            blacklistedMessage.addField(this.getLanguageManager().get("common.issued_by"), blacklist.getAuthor().getAsMention(), true);
            blacklistedMessage.addField(this.getLanguageManager().get("common.reason"), blacklist.getReason(), false);
            blacklistedMessage.addField(this.getLanguageManager().get("common.issued_at"), blacklist.getCreatedAt().toLocalDateTime().format(FurRaidDB.getInstance().getDateTimeFormatter()), true);

            interaction.replyEmbeds(blacklistedMessage.build()).queue();
        } else {
            interaction.replyEmbeds(buildError(getLanguageManager().get("command.global_blacklist.show.not_found"))).queue();
        }
    }

    /**
     * Handles logging of a blacklisted user.
     *
     * @param interaction The CommandInteraction representing the event triggered by the user.
     * @param blacklist   The BlacklistBuilder object containing information about the blacklisted user.
     */
    @SuppressWarnings("All")
    private void handleLogging(@NotNull CommandInteraction interaction, @NotNull BlacklistBuilder blacklist) {
        GuildSettings guildSettings = FurRaidDB.getInstance()
                .getBlacklistManager()
                .fetchGuildSettings(interaction.getGuild());

        User user = interaction.getJDA().getUserById(blacklist.getUser().getId());

        if (guildSettings != null && guildSettings.getLoggingChannel() != null) {
            TextChannel loggingChannel = interaction.getGuild().getTextChannelById(guildSettings.getLoggingChannel());
            TextChannel loggingInternalChannel = interaction.getJDA().getTextChannelById("863407026360025108");

            EmbedBuilder blacklistedMessage = this.getEmbed().simpleAuthoredEmbed();
            blacklistedMessage.setColor(Color.decode("#9412d5"));
            blacklistedMessage.setAuthor(this.getLanguageManager().get("common.globally_blacklisted.title.added", user.getGlobalName()), "https://frdb.fluffici.eu", ICON_CIRCLE_SLASHED);
            blacklistedMessage.setDescription(this.getLanguageManager().get("common.globally_blacklisted.description.added"));

            if (blacklist.getAttachmentUrl() != null)
                blacklistedMessage.setImage(blacklist.getAttachmentUrl());

            blacklistedMessage.setThumbnail(user.getAvatarUrl());
            blacklistedMessage.setTimestamp(Instant.now());

            blacklistedMessage.addField(this.getLanguageManager().get("common.user.id"), blacklist.getUser().getId(), false);
            blacklistedMessage.addField(this.getLanguageManager().get("common.user.name"), user.getEffectiveName(), true);
            blacklistedMessage.addField(this.getLanguageManager().get("common.issued_by"), interaction.getUser().getAsMention(), true);
            blacklistedMessage.addField(this.getLanguageManager().get("common.reason"), blacklist.getReason(), false);
            blacklistedMessage.addField(this.getLanguageManager().get("common.issued_at"), new Timestamp(System.currentTimeMillis()).toLocalDateTime().format(FurRaidDB.getInstance().getDateTimeFormatter()), true);

            loggingChannel.sendMessageEmbeds(blacklistedMessage.build()).queue();

            blacklistedMessage.setTitle("New blacklist from " + interaction.getGuild().getName());
            loggingInternalChannel.sendMessageEmbeds(blacklistedMessage.build()).queue();

            Member member = interaction.getGuild().getMember(user);

            if (member.canInteract(interaction.getGuild().getSelfMember())) {
                interaction.getGuild().getMember(user).ban(7, TimeUnit.DAYS).reason(blacklist.getReason()).queue();
            }
        }
    }
}