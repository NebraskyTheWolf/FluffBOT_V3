/*
---------------------------------------------------------------------------------
File Name : SelectQuarantineActions

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 17/06/2024
Last Modified : 17/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.components.button.quarantine;

import eu.fluffici.bot.FluffBOT;
import eu.fluffici.bot.api.beans.QuarantineAction;
import eu.fluffici.bot.api.game.GameId;
import eu.fluffici.bot.api.interactions.SelectMenu;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectInteraction;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static eu.fluffici.bot.api.IconRegistry.ICON_CIRCLE_SLASHED;
import static eu.fluffici.bot.api.MessageUtil.updateInteraction;
import static eu.fluffici.bot.components.modal.custom.CustomModalHandler.handleCustomModal;
import static net.dv8tion.jda.internal.utils.Helpers.listOf;

@SuppressWarnings("All")
public class SelectQuarantineActions extends SelectMenu<StringSelectInteraction> {
    public SelectQuarantineActions() {
        super("select:restore-access-action");
    }

    @Override
    public void execute(StringSelectInteraction interaction) {
        QuarantineAction action = QuarantineAction.valueOf(interaction.getValues().getFirst().split(":")[0]);
        Member member = interaction.getGuild().getMemberById(interaction.getValues().getFirst().split(":")[1]);

        Role verified = interaction.getGuild().getRoleById(FluffBOT.getInstance().getDefaultConfig().getProperty("roles.verified"));
        Role unverified = interaction.getGuild().getRoleById(FluffBOT.getInstance().getDefaultConfig().getProperty("roles.unverified"));

        if (member == null || verified == null || unverified == null) {
            interaction.reply(this.getLanguageManager().get("select.quarantine.failed")).setEphemeral(true).queue();
            updateInteraction(interaction.getMessage());
            return;
        }

        switch (action) {
            case RESTORE -> {
                interaction.getGuild().removeRoleFromMember(member, unverified);
                interaction.getGuild().addRoleToMember(member, verified);

                FluffBOT.getInstance().getGameServiceManager().unquarantineUser(member);

                interaction.reply(this.getLanguageManager().get("select.quarantine.restored", member.getAsMention())).setEphemeral(true).queue();
            }
            case KICK -> {
                handleCustomModal(interaction, this.getLanguageManager().get("common.quarantine.kick.title", member.getUser().getGlobalName()), listOf(ActionRow.of(
                        TextInput.create("row:reason", this.getLanguageManager().get("common.reason"),  TextInputStyle.PARAGRAPH)
                                .build()
                )), GameId.generateId(), (modalInteraction, acceptanceId) -> {
                    String reason = modalInteraction.getValue("row:reason").getAsString();

                    FluffBOT.getInstance().getSanctionManager().kick(
                            member,
                            interaction.getUser(),
                            reason,
                            null
                    );

                    modalInteraction.replyEmbeds(this.getEmbed()
                            .simpleAuthoredEmbed()
                            .setAuthor(this.getLanguageManager().get("common.quarantine.kick.success.title"), "https://fluffici.eu", ICON_CIRCLE_SLASHED.getUrl())
                            .setDescription(this.getLanguageManager().get("common.quarantine.kick.description", member.getUser().getGlobalName(), reason))
                            .build()
                    ).setEphemeral(true).queue();
                });
            }
            case BAN -> {
                handleCustomModal(interaction, this.getLanguageManager().get("common.quarantine.ban.title", member.getUser().getGlobalName()), listOf(ActionRow.of(
                        TextInput.create("row:reason", this.getLanguageManager().get("common.reason"),  TextInputStyle.PARAGRAPH)
                                .build(),
                        TextInput.create("row:days", this.getLanguageManager().get("common.days"),  TextInputStyle.SHORT)
                                .build()
                )), GameId.generateId(), (modalInteraction, acceptanceId) -> {
                    String reason = modalInteraction.getValue("row:reason").getAsString();
                    String days = modalInteraction.getValue("row:days").getAsString();

                    Instant future = Instant.now().plus(Integer.parseInt(days), ChronoUnit.DAYS);

                    FluffBOT.getInstance().getSanctionManager().ban(
                            interaction.getGuild(),
                            member.getUser(),
                            interaction.getUser(),
                            reason,
                            (days.isEmpty() ? -1L : future.toEpochMilli()),
                            null
                    );

                    modalInteraction.replyEmbeds(this.getEmbed()
                            .simpleAuthoredEmbed()
                            .setAuthor(this.getLanguageManager().get("common.quarantine.ban.success.title"), "https://fluffici.eu", ICON_CIRCLE_SLASHED.getUrl())
                            .setDescription(this.getLanguageManager().get("common.quarantine.ban.description", member.getUser().getGlobalName(), reason))
                            .build()
                    ).setEphemeral(true).queue();
                });
            }
            default -> interaction.reply("This interaction is not valid.").setEphemeral(true).queue();
        }

        updateInteraction(interaction.getMessage());
    }
}