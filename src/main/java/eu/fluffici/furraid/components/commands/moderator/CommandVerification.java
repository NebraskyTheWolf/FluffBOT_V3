/*
---------------------------------------------------------------------------------
File Name : CommandVerification

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 15/06/2024
Last Modified : 15/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.furraid.components.commands.moderator;

import eu.fluffici.bot.api.beans.furraid.FurRaidConfig;
import eu.fluffici.bot.api.beans.furraid.GuildSettings;
import eu.fluffici.bot.api.beans.furraid.OfferQuota;
import eu.fluffici.bot.api.game.GameId;
import eu.fluffici.bot.api.interactions.CommandCategory;
import eu.fluffici.bot.api.interactions.FCommand;
import eu.fluffici.bot.components.button.confirm.ConfirmCallback;
import eu.fluffici.furraid.FurRaidDB;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import static eu.fluffici.bot.api.IconRegistry.ICON_CIRCLE_SLASHED;
import static eu.fluffici.bot.components.button.confirm.ConfirmHandler.handleConfirmationFurRaid;
import static eu.fluffici.bot.components.modal.custom.CustomModalHandler.handleCustomModal;
import static net.dv8tion.jda.internal.utils.Helpers.listOf;

@SuppressWarnings("All")
public class CommandVerification extends FCommand {
    public CommandVerification() {
        super("verification", "Management of the user verification", CommandCategory.MODERATOR);


        this.getSubcommandGroupData().add(new SubcommandGroupData("verification", "Managing the verification of members")
                .addSubcommands(new SubcommandData("manual-verification", "Manually verifying a member on the server")
                        .addOption(OptionType.USER, "user", "Select the member to verify")
                )
                .addSubcommands(new SubcommandData("kick-all-unverified", "Kicking all unverified members"))
        );
    }

    /**
     * Executes the specified command based on the subcommand name.
     *
     * @param interaction The CommandInteraction representing the event triggered by the user.
     */
    @Override
    @SuppressWarnings("All")
    public void execute(@NotNull CommandInteraction interaction, GuildSettings settings) {
        String command = interaction.getSubcommandName();

        if (!settings.getConfig().getFeatures().getVerification().isEnabled()) {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.verification.feature_disabled"))).setEphemeral(true).queue();
            return;
        }

        OfferQuota offerQuota = FurRaidDB.getInstance().getOfferManager().getByGuild(interaction.getGuild().getIdLong());

        if (!offerQuota.isHasVerificationFeature()) {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("button.verification.premium"))).setEphemeral(true).queue();
            return;
        }

        if (!settings.getConfig().getFeatures().getVerification().isEnabled()) {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.verification.feature_disabled"))).setEphemeral(true).queue();
            return;
        }

        FurRaidConfig.VerificationFeature feature = settings.getConfig().getFeatures().getVerification();

        switch (command) {
            case "manual-verification" -> this.handleManualVerification(interaction, feature);
            case "kick-all-unverified" -> this.handleKickAllUnverified(interaction, feature);
        }
    }

    /**
     * Handles the manual verification of a member on the server.
     *
     * @param interaction The CommandInteraction representing the event triggered by the user.
     */
    private void handleManualVerification(@NotNull CommandInteraction interaction, FurRaidConfig.VerificationFeature verification) {
        User user = interaction.getOption("user").getAsUser();
        Member targetMember = interaction.getGuild().getMember(user);

        if (targetMember == null) {
            interaction.replyEmbeds(this.buildError("Invalid user specified.")).queue();
            return;
        }

        Role unverified = interaction.getGuild().getRoleById(
                verification.getSettings().getUnverifiedRole()
        );

        Role verified = interaction.getGuild().getRoleById(
                verification.getSettings().getVerifiedRole()
        );

        if (verified == null || unverified == null) {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.verification.roles_not_found"))).setEphemeral(true).queue();
            return;
        } else if (targetMember.getRoles().contains(verified)) {
            interaction.replyEmbeds(buildError(this.getLanguageManager().get("command.verification.manual.already_verified"))).queue();
            return;
        }

        interaction.getGuild().addRoleToMember(targetMember, verified).queue();
        interaction.getGuild().removeRoleFromMember(targetMember, unverified).queue();
        interaction.replyEmbeds(buildSuccess(this.getLanguageManager().get("command.verification.manual.success", targetMember.getAsMention()))).setEphemeral(true).queue();
    }

    /**
     * Handles kicking all unverified members.
     *
     * @param interaction The CommandInteraction representing the event triggered by the user.
     */
    private void handleKickAllUnverified(@NotNull CommandInteraction interaction, FurRaidConfig.VerificationFeature verification) {
        Role unverified = interaction.getGuild().getRoleById(
                verification.getSettings().getUnverifiedRole()
        );
        List<Member> unverifiedMembers = interaction.getGuild().getMembersWithRoles(unverified)
                .stream()
                .distinct()
                .toList();
        List<String> memberGlobalNames = unverifiedMembers
                .stream()
                .map(member -> member.getUser().getGlobalName()).toList();

        handleCustomModal(interaction, "Kicking all unverified members", listOf(ActionRow.of(
                TextInput.create("row:reason", this.getLanguageManager().get("common.reason"),  TextInputStyle.PARAGRAPH)
                        .build()
        )), GameId.generateId(), (modalInteraction, acceptanceId) -> {
            String reason = modalInteraction.getValue("row:reason").getAsString();

            if (unverifiedMembers.isEmpty()) {
                modalInteraction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.verification.no_unverified_members")))
                        .setEphemeral(true)
                        .queue();
                return;
            }

            String unverifiedCount = NumberFormat.getNumberInstance().format(unverifiedMembers.size());

            if (unverifiedMembers.size() >= 10) {
                handleConfirmationFurRaid(modalInteraction,
                        this.getLanguageManager().get("command.verification.kick.confirm"),
                        this.getLanguageManager().get("command.verification.kick.confirm.button", unverifiedCount),
                        new ConfirmCallback() {
                            @Override
                            public void confirm(ButtonInteraction buttonInteraction) throws Exception {
                                for (Member member : unverifiedMembers) {
                                    if (!member.getUser().isBot()) {
                                        member.kick().reason(reason).queue();
                                    }
                                }

                                buttonInteraction.replyEmbeds(getEmbed()
                                        .simpleAuthoredEmbed()
                                        .setAuthor(getLanguageManager().get("common.verification.kick.success"), "https://fluffici.eu", ICON_CIRCLE_SLASHED)
                                        .setDescription(getLanguageManager().get("common.verification.kick.success.description", String.join("\n * ", memberGlobalNames), reason))
                                        .build()
                                ).queue();
                            }

                            @Override
                            public void cancel(ButtonInteraction buttonInteraction) throws Exception {
                                buttonInteraction.replyEmbeds(buildError(getLanguageManager().get("command.verification.kick.cancelled"))).queue();
                            }
                        }
                );
            } else {
                for (Member member : unverifiedMembers) {
                    if (!member.getUser().isBot()) {
                        member.kick().reason(reason).queue();
                    }
                }

                modalInteraction.replyEmbeds(this.getEmbed()
                        .simpleAuthoredEmbed()
                        .setAuthor(this.getLanguageManager().get("common.verification.kick.success"), "https://fluffici.eu", ICON_CIRCLE_SLASHED)
                        .setDescription(this.getLanguageManager().get("common.verification.kick.success.description", String.join("\n * ", memberGlobalNames), reason))
                        .build()
                ).queue();
            }
        });
    }
}