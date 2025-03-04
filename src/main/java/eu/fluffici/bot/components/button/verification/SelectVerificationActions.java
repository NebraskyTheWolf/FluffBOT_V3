/*
---------------------------------------------------------------------------------
File Name : SelectVerificationActions

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 14/06/2024
Last Modified : 14/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.components.button.verification;

import eu.fluffici.bot.FluffBOT;
import eu.fluffici.bot.api.beans.verification.VerificationAction;
import eu.fluffici.bot.api.beans.verification.VerificationBuilder;
import eu.fluffici.bot.api.game.GameId;
import eu.fluffici.bot.api.interactions.SelectMenu;
import eu.fluffici.bot.components.button.confirm.ConfirmCallback;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectInteraction;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import static eu.fluffici.bot.api.IconRegistry.*;
import static eu.fluffici.bot.components.button.confirm.ConfirmHandler.handleConfirmation;
import static eu.fluffici.bot.components.modal.custom.CustomModalHandler.handleCustomModal;
import static net.dv8tion.jda.internal.utils.Helpers.listOf;

public class SelectVerificationActions extends SelectMenu<StringSelectInteraction> {
    public final int SPAMMER_MASK = 1 << 20;

    public SelectVerificationActions() {
        super("select:verification-action");
    }

    @Override
    @SuppressWarnings("All")
    public void execute(@NotNull StringSelectInteraction interaction) {
        VerificationAction action = VerificationAction.valueOf(interaction.getValues().getFirst().split(":")[0]);
        String challengeId = interaction.getValues().getFirst().split(":")[1];

        try {
            VerificationBuilder form = FluffBOT.getInstance()
                    .getGameServiceManager()
                    .getVerificationRecord(challengeId);

            if (form == null) {
                interaction.reply("Verification record not found").setEphemeral(true).queue();
                return;
            }

            Member challenger = interaction.getGuild().getMember(UserSnowflake.fromId(form.getUserId()));
            Message originalMessage = interaction.getMessage();

            boolean isSpammer = (challenger.getUser().getFlagsRaw() & SPAMMER_MASK) == SPAMMER_MASK;

            // Reformatting the form to be public
            MessageEmbed copiedForm = originalMessage.getEmbeds().getFirst();
            EmbedBuilder publicForm = new EmbedBuilder();
            publicForm.setAuthor("Nový člen byl právě ověřen!", "https://fluffici.eu", interaction.getJDA().getSelfUser().getAvatarUrl());
            publicForm.setDescription(copiedForm.getDescription());
            publicForm.addField(copiedForm.getFields().getFirst());
            publicForm.setTimestamp(Instant.now());
            publicForm.setColor(copiedForm.getColor());
            publicForm.setThumbnail(copiedForm.getThumbnail().getUrl());

            // New commer card in admin
            EmbedBuilder newCommer = new EmbedBuilder();
            newCommer.setThumbnail(copiedForm.getThumbnail().getUrl());
            newCommer.setAuthor(challenger.getUser().getGlobalName() + " byl právě ověřen!", "https://fluffici.eu", ICON_CHECKS.getUrl());
            newCommer.setDescription("Přivítejte ho mezi Fluffíky! :blue_heart:");
            newCommer.setColor(Color.GREEN);
            newCommer.setTimestamp(Instant.now());

            switch (action) {
                case ACCEPT -> {
                    if (isSpammer) {
                        handleConfirmation(interaction,
                                this.getLanguageManager().get("select.verification.confirm", challenger.getAsMention()),
                                this.getLanguageManager().get("select.verification.confirm.button"),
                                new ConfirmCallback() {
                                    @Override
                                    public void confirm(ButtonInteraction interaction) throws Exception {
                                        interaction.getGuild().removeRoleFromMember(challenger, interaction.getGuild().getRoleById(FluffBOT.getInstance().getDefaultConfig().getProperty("roles.unverified"))).queue();
                                        interaction.getGuild().addRoleToMember(challenger, interaction.getGuild().getRoleById(FluffBOT.getInstance().getDefaultConfig().getProperty("roles.verified"))).queue();

                                        interaction.getGuild().getTextChannelById(FluffBOT.getInstance().getDefaultConfig().getProperty("channel.verified"))
                                                .sendMessageEmbeds(publicForm.build())
                                                .queue();
                                        interaction.getGuild().getTextChannelById(FluffBOT.getInstance().getDefaultConfig().getProperty("channel.main"))
                                                .sendMessageEmbeds(newCommer.build())
                                                .addActionRow(Button.link("https://discord.com/channels/606534136806637589/customize-community", "Channels & Roles"))
                                                .queue();

                                        form.setStatus("VERIFIED");

                                        form.setVerifiedBy(interaction.getUser().getId());
                                        FluffBOT.getInstance().getGameServiceManager().updateVerificationRecord(form);

                                        FluffBOT.getInstance().getGameServiceManager().removeReminder(challenger);

                                        originalMessage.editMessageComponents(ActionRow.of(Button.success("button:none", "Žádost schválena od " + interaction.getUser().getGlobalName()).asDisabled())).queue();

                                        interaction.replyEmbeds(getEmbed()
                                                .simpleAuthoredEmbed()
                                                .setAuthor(getLanguageManager().get("common.verification.granted.success"), "https://fluffici.eu", ICON_CLIPBOARD_CHECKED.getUrl())
                                                .setDescription(getLanguageManager().get("common.verification.granted.description", challenger.getAsMention()))
                                                .build()
                                        ).setEphemeral(true).queue();
                                    }

                                    @Override
                                    public void cancel(ButtonInteraction interaction) throws Exception {
                                        handleConfirmation(interaction,
                                                getLanguageManager().get("select.verification.sanction.confirm", challenger.getAsMention()),
                                                getLanguageManager().get("select.verification.sanction.confirm.button"),
                                                new ConfirmCallback() {
                                                    @Override
                                                    public void confirm(ButtonInteraction interaction) throws Exception {
                                                       handleCustomModal(interaction, getLanguageManager().get("select.verification.sanction.modal.title"), listOf(ActionRow.of(
                                                               TextInput.create("row:reason", getLanguageManager().get("common.reason"), TextInputStyle.PARAGRAPH)
                                                                       .setMinLength(2)
                                                                       .setMaxLength(4000)
                                                                       .build()
                                                       )), GameId.generateId(), (modalInteraction, acceptanceId) -> {
                                                           String reason = modalInteraction.getValue("row:reason").getAsString();

                                                           FluffBOT.getInstance().getSanctionManager().kick(
                                                                   challenger,
                                                                   modalInteraction.getUser(),
                                                                   reason,
                                                                   null
                                                           );

                                                           interaction.replyEmbeds(getEmbed()
                                                                   .simpleAuthoredEmbed()
                                                                   .setAuthor(getLanguageManager().get("common.verification.sanction.success"), "https://fluffici.eu", ICON_CLIPBOARD_CHECKED.getUrl())
                                                                   .setDescription(getLanguageManager().get("common.verification.sanction.description", challenger.getAsMention(), reason))
                                                                   .build()
                                                           ).setEphemeral(true).queue();
                                                       });
                                                    }

                                                    @Override
                                                    public void cancel(ButtonInteraction interaction) throws Exception {
                                                        interaction.reply(getLanguageManager().get("select.verification.sanction.cancelled", challenger.getAsMention())).setEphemeral(true).queue();
                                                    }
                                                }
                                        );
                                    }
                                }
                        );
                    } else {
                        interaction.getGuild().removeRoleFromMember(challenger, interaction.getGuild().getRoleById(FluffBOT.getInstance().getDefaultConfig().getProperty("roles.unverified"))).queue();
                        interaction.getGuild().addRoleToMember(challenger, interaction.getGuild().getRoleById(FluffBOT.getInstance().getDefaultConfig().getProperty("roles.verified"))).queue();

                        interaction.getGuild().getTextChannelById(FluffBOT.getInstance().getDefaultConfig().getProperty("channel.verified"))
                                .sendMessageEmbeds(publicForm.build())
                                .queue();
                        interaction.getGuild().getTextChannelById(FluffBOT.getInstance().getDefaultConfig().getProperty("channel.main"))
                                .sendMessageEmbeds(newCommer.build())
                                .addActionRow(Button.link("https://discord.com/channels/606534136806637589/customize-community", "Channels & Roles"))
                                .queue();

                        form.setStatus("VERIFIED");

                        form.setVerifiedBy(interaction.getUser().getId());
                        FluffBOT.getInstance().getGameServiceManager().updateVerificationRecord(form);

                        FluffBOT.getInstance().getGameServiceManager().removeReminder(challenger);

                        originalMessage.editMessageComponents(ActionRow.of(Button.success("button:none", "Žádost schválena od " + interaction.getUser().getGlobalName()).asDisabled())).queue();

                        interaction.replyEmbeds(this.getEmbed()
                                .simpleAuthoredEmbed()
                                .setAuthor(this.getLanguageManager().get("common.verification.granted.success"), "https://fluffici.eu", ICON_CLIPBOARD_CHECKED.getUrl())
                                .setDescription(this.getLanguageManager().get("common.verification.granted.description", challenger.getAsMention()))
                                .build()
                        ).setEphemeral(true).queue();
                    }
                }
                case DENY -> {
                    handleCustomModal(interaction, this.getLanguageManager().get("common.verification.deny.title", challenger.getUser().getGlobalName()), listOf(ActionRow.of(
                            TextInput.create("row:reason", this.getLanguageManager().get("common.reason"),  TextInputStyle.PARAGRAPH)
                                    .build()
                    )), form.getVerificationCode(), (modalInteraction, acceptanceId) -> {
                        String reason = modalInteraction.getValue("row:reason").getAsString();

                        PrivateChannel privateChannel = challenger.getUser().openPrivateChannel().complete();
                        if (privateChannel.canTalk()) {
                            privateChannel.sendMessageEmbeds(this.getEmbed()
                                    .simpleAuthoredEmbed()
                                            .setAuthor("Výsledek ověření od " + interaction.getGuild().getName(), "https://fluffici.eu", ICON_CIRCLE_MINUS.getUrl())
                                            .setTitle("Ověření bylo zamítnuto")
                                            .setDescription(String.format(
                                                    """
                                                    Tvoje žádost o ověření byla zamítnuta z následujícího důvodu
                                                    
                                                    **Důvod**: %s
                                                    """, reason)
                                            )
                                            .setTimestamp(Instant.now())
                                            .setColor(Color.RED)
                                    .build()
                            ).addActionRow(Button.link("https://discord.com/channels/606534136806637589/1220790179749560320", "Ověření")).queue();
                        }

                        form.setStatus("DENIED");
                        form.setVerifiedBy(interaction.getUser().getId());
                        FluffBOT.getInstance().getGameServiceManager().updateVerificationRecord(form);

                        originalMessage.editMessageComponents(ActionRow.of(Button.secondary("button:none", "Žádost zamítnuta (" + interaction.getUser().getGlobalName().concat(")")).asDisabled())).queue();

                        modalInteraction.replyEmbeds(this.getEmbed()
                                .simpleAuthoredEmbed()
                                .setAuthor(this.getLanguageManager().get("common.verification.denied"), "https://fluffici.eu", ICON_CIRCLE_SLASHED.getUrl())
                                .setDescription(this.getLanguageManager().get("common.verification.denied.description", challenger.getAsMention(), reason))
                                .build()
                        ).setEphemeral(true).queue();
                    });
                }
                case BAN -> {
                    handleCustomModal(interaction, this.getLanguageManager().get("common.verification.ban.title", challenger.getUser().getGlobalName()), listOf(ActionRow.of(
                            TextInput.create("row:reason", this.getLanguageManager().get("common.reason"),  TextInputStyle.PARAGRAPH)
                                    .build()
                    )), form.getVerificationCode(), (modalInteraction, acceptanceId) -> {
                        String reason = modalInteraction.getValue("row:reason").getAsString();

                        challenger.ban(7, TimeUnit.DAYS).reason(reason).queue();

                        form.setStatus("BANNED");
                        form.setVerifiedBy(interaction.getUser().getId());
                        FluffBOT.getInstance().getGameServiceManager().updateVerificationRecord(form);

                        originalMessage.editMessageComponents(ActionRow.of(Button.secondary("button:none", "Uživatel byl zabanován od (" + interaction.getUser().getGlobalName().concat(")")).asDisabled())).queue();

                        modalInteraction.replyEmbeds(this.getEmbed()
                                .simpleAuthoredEmbed()
                                .setAuthor(this.getLanguageManager().get("common.verification.ban.success"), "https://fluffici.eu", ICON_CIRCLE_SLASHED.getUrl())
                                .setDescription(this.getLanguageManager().get("common.verification.ban.success.description", challenger.getAsMention(), reason))
                                .build()
                        ).setEphemeral(true).queue();
                    });
                }
            }
        } catch (Exception e) {
            interaction.reply("An error occurred while processing the verification form").setEphemeral(true).queue();
            e.printStackTrace();
        }

        this.getUserManager().addPointToStaff(interaction.getUser(), 10);
    }
}