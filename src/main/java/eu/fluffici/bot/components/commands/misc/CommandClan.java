package eu.fluffici.bot.components.commands.misc;

/*
---------------------------------------------------------------------------------
File Name : CommandClan.java

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
import eu.fluffici.bot.api.bucket.CommandHandle;
import eu.fluffici.bot.api.bucket.Deprecated;
import eu.fluffici.bot.api.game.GameId;
import eu.fluffici.bot.api.hooks.FIleUploadCallback;
import eu.fluffici.bot.api.hooks.FileBuilder;
import eu.fluffici.bot.components.commands.Command;
import eu.fluffici.bot.api.beans.clans.ClanBean;
import eu.fluffici.bot.api.beans.clans.ClanMembersBean;
import eu.fluffici.bot.api.beans.clans.ClanRequestBean;
import eu.fluffici.bot.api.hooks.PlayerBean;
import eu.fluffici.bot.api.interactions.CommandCategory;
import eu.fluffici.bot.api.interactions.Interactions;
import eu.fluffici.bot.components.button.confirm.ConfirmCallback;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import okhttp3.Response;

import java.awt.*;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static eu.fluffici.bot.api.IconRegistry.ICON_TRUCK;
import static eu.fluffici.bot.components.button.confirm.ConfirmHandler.handleConfirmation;

@CommandHandle
@Deprecated
@SuppressWarnings("All")
public class CommandClan extends Command {

    public final FluffBOT instance;

    public CommandClan(FluffBOT instance) {
        super("clans", "These commands is related to the management of your clan", CommandCategory.MISC);
        this.instance = instance;

        this.getSubcommandData().add(new SubcommandData("create", "Create a new clan."));

        this.getSubcommandData().add(new SubcommandData("upload-icon", "Change the icon of your clan")
                .addOption(OptionType.ATTACHMENT, "icon", "Upload your avatar icon", true)
        );

        List<SubcommandData> management = new ArrayList<>();
        management.add(new SubcommandData("disband", "Dissolves your clan."));
        management.add(new SubcommandData("invite", "Sends an invitation to a user to join the clan.")
                .addOptions(
                        new OptionData(OptionType.USER, "user", "The user you want to invite.")
                                .setRequired(true)
                )
        );
        management.add(new SubcommandData("info", "Provides information about your clan."));
        management.add(new SubcommandData("edit", "Edit your clan."));
        management.add(new SubcommandData("kick", "Removes a member from your clan.")
                .addOptions(
                        new OptionData(OptionType.USER, "user", "The user you want to kick.")
                                .setRequired(true)
                )
        );



        this.getSubcommandData().addAll(management);

        this.getOptions().put("channelRestricted", true);
        this.getOptions().put("rate-limit", true);
        this.getOptions().put("noSelfUser", true);
    }

    @Override
    public void execute(CommandInteraction interaction) {
        String command = interaction.getSubcommandName();
        this.handleManagement(interaction, command);
    }

    public void handleNormal(CommandInteraction interaction) {
        PlayerBean executor = this.getUserManager().fetchUser(interaction.getUser());
        ClanBean currentClan = this.instance.getClanManager().fetchClan(executor);

        if (!this.getUserManager().hasEnoughTokens(executor, 50)) {
            interaction.reply(this.getLanguageManager().get("common.token.not_enough")).setEphemeral(true).queue();
            return;
        }

        if (currentClan != null) {
            interaction.reply(this.getLanguageManager().get("command.clan.already_in_clan"))
                    .setEphemeral(true).queue();
        } else {
            TextInput title = TextInput.create("row_clan_title", this.getLanguageManager().get("common.title"), TextInputStyle.SHORT)
                    .setMaxLength(16)
                    .setMinLength(4)
                    .setRequired(true)
                    .build();
            TextInput prefix = TextInput.create("row_clan_prefix", this.getLanguageManager().get("common.tag"), TextInputStyle.SHORT)
                    .setMaxLength(4)
                    .setMinLength(3)
                    .setRequired(true)
                    .build();
            TextInput description = TextInput.create("row_clan_description", this.getLanguageManager().get("common.description"), TextInputStyle.PARAGRAPH)
                    .setMaxLength(200)
                    .setMinLength(10)
                    .setRequired(false)
                    .build();
            Modal modal = Modal.create("row_modal_create_clan", this.getLanguageManager().get("common.create_clan"))
                    .addActionRow(title)
                    .addActionRow(prefix)
                    .addActionRow(description)
                    .build();
            interaction.replyModal(modal).queue();
        }
    }

    public void handleManagement(CommandInteraction interaction, String command) {
        PlayerBean executor = this.getUserManager().fetchUser(interaction.getUser());
        if (executor != null) {
            ClanMembersBean currentMember = this.instance.getClanManager().fetchClanMember(executor);
            ClanBean currentClan = this.instance.getClanManager().fetchClan(executor);
            if (currentClan != null) {
                if (currentMember != null) {
                    switch (command) {
                        case "disband" -> this.handleDisband(interaction, currentMember, currentClan);
                        case "invite" -> this.handleInvite(interaction, currentMember, currentClan);
                        case "info" -> this.handleInfo(interaction, currentMember, currentClan);
                        case "kick" -> this.handleKick(interaction, currentMember, currentClan);
                        case "edit" -> this.handleDescription(interaction, currentMember, currentClan);
                        case "upload-icon" -> this.handleUploadIcon(interaction, currentMember, currentClan);

                        // Default
                        default -> interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("common.clan.not_found"))).setEphemeral(true).queue();
                    }
                } else {
                    interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("common.clan.not_found"))).setEphemeral(true).queue();
                }
            } else if (command.equals("create")) {
                this.handleNormal(interaction);
            } else {
                interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("common.clan.not_found"))).setEphemeral(true).queue();
            }
        } else {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("common.profile.unable_to_load"))).setEphemeral(true).queue();
        }
    }

    /**
     * Handles the upload of an icon for a clan.
     *
     * @param interaction    The command interaction object representing the interaction with the command.
     * @param currentMember  The current member of the clan.
     * @param currentClan    The current clan.
     */
    private void handleUploadIcon(CommandInteraction interaction, ClanMembersBean currentMember, ClanBean currentClan) {
        Message.Attachment icon = interaction.getOption("icon").getAsAttachment();
        PlayerBean owner = this.getUserManager().fetchUser(interaction.getUser());

        if (this.isOwner(currentMember, currentClan)) {
            if (!this.getUserManager().hasEnoughTokens(owner, 50)) {
                interaction.replyEmbeds(buildError(getLanguageManager().get("command.clan.upload.not_enough_tokens"))).setEphemeral(true).queue();
                return;
            }

            handleConfirmation(interaction,
                    this.getLanguageManager().get("command.clan.upload.confirm"),
                    this.getLanguageManager().get("command.clan.upload.confirm.button"),
                    new ConfirmCallback() {
                        @Override
                        public void confirm(ButtonInteraction interaction) throws Exception {
                            if (!icon.isImage()) {
                                interaction.replyEmbeds(buildError(getLanguageManager().get("command.clan.upload.invalid_file"))).setEphemeral(true).queue();
                                return;
                            }

                            if (!icon.getFileName().endsWith(".png")) {
                                interaction.replyEmbeds(buildError(getLanguageManager().get("command.clan.upload.invalid_file_type"))).setEphemeral(true).queue();
                                return;
                            }

                            int width = icon.getWidth();
                            int height = icon.getHeight();

                            if (width > 256 || height > 256 || width != height)  {
                                interaction.replyEmbeds(buildError(getLanguageManager().get("command.clan.upload.invalid_dimensions"))).setEphemeral(true).queue();
                                return;
                            }

                            long maxSizeBytes = 16 * 1024 * 1024;
                            long fileSize = icon.getSize();
                            double fileSizeMB = (double) fileSize / (1024 * 1024);

                            if (fileSize > maxSizeBytes) {
                                interaction.replyEmbeds(buildError(getLanguageManager().get("command.clan.upload.file_too_large", fileSizeMB))).setEphemeral(true).queue();
                                return;
                            }

                            String fileId = GameId.generateId();

                            try {
                                instance.getFileUploadManager().uploadFile(FileBuilder
                                        .builder()
                                        .bucketName("attachments")
                                        .data(icon.retrieveInputStream().get())
                                        .fileName(fileId + ".png")
                                        .build(), new FIleUploadCallback() {
                                    @Override
                                    @SneakyThrows
                                    public void done(Response response) {
                                        JsonObject data = new Gson().fromJson(response.body().string(), JsonObject.class);

                                        if (data.has("id")) {
                                            currentClan.setIconURL("https://autumn.fluffici.eu/attachments/".concat(data.get("id").getAsString()));

                                            instance.getClanManager().updateClan(currentClan);

                                            // Removing the token only in case of success.
                                            getUserManager().removeTokens(owner, 50);

                                            interaction.replyEmbeds(buildSuccess(getLanguageManager().get("command.clan.upload.success"))).setEphemeral(true).queue();
                                        } else if (data.has("error")) {
                                            interaction.replyEmbeds(buildSuccess(getLanguageManager().get("command.clan.upload.error", data.get("error").getAsString()))).setEphemeral(true).queue();
                                        }
                                    }

                                    @Override
                                    @SneakyThrows
                                    public void error(Response response) {
                                        JsonObject data = new Gson().fromJson(response.body().string(), JsonObject.class);

                                        if (data.has("error")) {
                                            interaction.replyEmbeds(buildSuccess(getLanguageManager().get("command.clan.upload.error", data.get("error").getAsString()))).setEphemeral(true).queue();
                                            return;
                                        }

                                        System.out.println(data.toString());

                                        interaction.replyEmbeds(buildError(getLanguageManager().get("command.clan.upload.failed"))).setEphemeral(true).queue();
                                    }
                                });
                            } catch (IOException|ExecutionException|InterruptedException e) {
                                e.printStackTrace();
                                interaction.replyEmbeds(buildError(getLanguageManager().get("command.clan.upload.failed"))).setEphemeral(true).queue();
                            }
                        }

                        @Override
                        public void cancel(ButtonInteraction interaction) throws Exception {
                            interaction.replyEmbeds(buildError(getLanguageManager().get("command.clan.upload.cancelled"))).setEphemeral(true).queue();
                        }
                    }
            );
        } else {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("common.permission_denied"))).queue();
        }
    }

    private void handleDisband(CommandInteraction interaction, ClanMembersBean executor, ClanBean currentClan) {
        if (this.isOwner(executor, currentClan)) {
            List<ClanMembersBean> members = this.instance.getClanManager().fetchClanMembers(currentClan);
            members.forEach(clanMembersBean -> {
                try {
                    this.instance.getGameServiceManager().deleteUserClan(clanMembersBean);
                    this.instance.getGameServiceManager().deleteClanMember(clanMembersBean);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

           try {
               this.instance.getGameServiceManager().deleteClan(currentClan);
           } catch (Exception e) {
               e.printStackTrace();
           }

            interaction.replyEmbeds(this.buildSuccess(this.getLanguageManager().get("common.clan.deleted"))).queue();
        } else {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("common.permission_denied"))).queue();
        }
    }
    private void handleInvite(CommandInteraction interaction, ClanMembersBean executor, ClanBean currentClan) {
        if (this.isOwner(executor, currentClan)) {
            User target = interaction.getOption("user").getAsUser();

            this.instance.getClanManager().sendClanInvite(new ClanRequestBean(
                    UUID.randomUUID().toString(),
                    currentClan.getClanId(),
                    target.getId(),
                    interaction.getUser().getId(),
                    "ACTIVE",
                    new Timestamp(Instant.now().plusSeconds(600).toEpochMilli()),
                    new Timestamp(Instant.now().toEpochMilli())
            ));

            Pair<Button, String> acceptInvite = this.instance.getButtonManager().toInteraction("row_accept_invite", target, null, true);
            Message message = target.openPrivateChannel().complete().sendMessageEmbeds(
                    this.getEmbed()
                            .simpleAuthoredEmbed()
                            .setAuthor(currentClan.getTitle(), "https://fluffici.eu", ( currentClan.getIconURL() != null && !currentClan.getIconURL().isEmpty() ?  currentClan.getIconURL() : ICON_TRUCK.getUrl()))
                            .setTitle(this.getLanguageManager().get("common.clan.invited", interaction.getUser().getEffectiveName()))
                            .setDescription("**Description**\n\n" + currentClan.getDescription())
                            .build()
            ).mention(target)
                    .addActionRow(acceptInvite.getLeft())
                    .complete();

            Interactions interactions = this.instance.getInteractionManager().fetchInteraction(acceptInvite.getRight());
            interactions.setAttached(true);
            interactions.setMessageId(message.getId());
            this.instance.getInteractionManager().updateInteraction(interactions);

            interaction.replyEmbeds(this.buildSuccess(this.getLanguageManager().get("command.clan.invite_sent", target.getEffectiveName()))).queue();
        } else {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("common.permission_denied"))).queue();
        }
    }
    private void handleInfo(CommandInteraction interaction, ClanMembersBean executor, ClanBean currentClan) {
        List<String> members = this.instance.getClanManager().fetchClanMembers(currentClan)
                .stream()
                .map(clanMembersBean -> {
                    User user = this.instance.getJda().getUserById(clanMembersBean.getUserId());
                    if (user != null)
                        if (this.isOwner(clanMembersBean, currentClan))
                            return user.getAsMention() + " :crown: ";
                        else return user.getAsMention();
                    else return "Deleted User";
                }).toList();

        User clanOwner = this.instance.getJda().getUserById(currentClan.getOwnerId());

        interaction.replyEmbeds(this.getEmbed()
                .simpleAuthoredEmbed()
                .setAuthor(this.getLanguageManager().get("common.clan.information", currentClan.getTitle()), "https://fluffici.eu", (currentClan.getIconURL() != null && !currentClan.getIconURL().isEmpty() ? currentClan.getIconURL() : ICON_TRUCK.getUrl()))
                .setDescription(this.getLanguageManager().get("common.clan.description", currentClan.getDescription()))
                .addField(this.getLanguageManager().get("common.label.members"), String.join("\n", members), true)
                .addField(this.getLanguageManager().get("common.label.prefix"), currentClan.getPrefix(), true)
                .setColor(Color.decode(currentClan.getColor()))
                .setFooter(clanOwner.getEffectiveName(), clanOwner.getAvatarUrl())
                .setTimestamp(Instant.now())
                .build()
        ).queue();
    }
    private void handleKick(CommandInteraction interaction, ClanMembersBean executor, ClanBean currentClan) {
        if (this.isOwner(executor, currentClan)) {
            User target = interaction.getOption("user").getAsUser();
            PlayerBean handler = this.getUserManager().fetchUser(target);

            try {
                ClanMembersBean membersBean = this.instance.getClanManager().fetchClanMember(handler);
                this.instance.getGameServiceManager().deleteUserClan(membersBean);
                this.instance.getGameServiceManager().deleteClanMember(membersBean);

                target.openPrivateChannel().complete().sendMessage(this.getLanguageManager().get("common.you.got.kicked.smile", currentClan.getTitle())).queue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            interaction.replyEmbeds(this.buildSuccess(this.getLanguageManager().get("common.clan.kicked", target.getEffectiveName()))).queue();
        } else {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("common.permission_denied"))).queue();
        }
    }

    private void handleDescription(CommandInteraction interaction, ClanMembersBean executor, ClanBean currentClan) {
        if (this.isOwner(executor, currentClan)) {
            TextInput title = TextInput.create("row_clan_title", this.getLanguageManager().get("common.title"), TextInputStyle.SHORT)
                    .setMaxLength(16)
                    .setMinLength(4)
                    .setValue(currentClan.getTitle())
                    .setRequired(true)
                    .build();
            TextInput prefix = TextInput.create("row_clan_prefix", this.getLanguageManager().get("common.tag"), TextInputStyle.SHORT)
                    .setMaxLength(4)
                    .setMinLength(3)
                    .setValue(currentClan.getPrefix())
                    .setRequired(true)
                    .build();
            TextInput description = TextInput.create("row_clan_description", this.getLanguageManager().get("common.description"), TextInputStyle.PARAGRAPH)
                    .setMaxLength(200)
                    .setMinLength(10)
                    .setRequired(false)
                    .setValue(currentClan.getDescription())
                    .build();
            Modal modal = Modal.create("row_modal_update_clan", this.getLanguageManager().get("common.update_clan", currentClan.getTitle()))
                    .addActionRow(title)
                    .addActionRow(prefix)
                    .addActionRow(description)
                    .build();
            interaction.replyModal(modal).queue();
        } else {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("common.permission_denied"))).queue();
        }
    }

    private boolean isOwner(ClanMembersBean executor, ClanBean currentClan) {
       return currentClan.getOwnerId().equals(executor.getUserId());
    }
}
