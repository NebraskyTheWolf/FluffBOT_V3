/*
---------------------------------------------------------------------------------
File Name : ButtonVerification

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 14/06/2024
Last Modified : 14/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.furraid.components.button.support;

import eu.fluffici.bot.api.beans.furraid.FurRaidConfig;
import eu.fluffici.bot.api.beans.furraid.GuildSettings;
import eu.fluffici.bot.api.beans.furraid.ticket.TicketBuilder;
import eu.fluffici.bot.api.interactions.ButtonBuilder;
import eu.fluffici.bot.components.button.confirm.ConfirmCallback;
import eu.fluffici.furraid.FurRaidDB;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import org.jetbrains.annotations.NotNull;

import static eu.fluffici.bot.components.button.confirm.ConfirmHandler.handleConfirmationFurRaid;

public class ButtonCloseTicket extends ButtonBuilder {
    public ButtonCloseTicket() {
        super("row:close-ticket", "Close ticket", ButtonStyle.PRIMARY);
    }

    /**
     * Executes the close ticket button interaction.
     *
     * @param interaction The button interaction for closing the ticket.
     */
    @Override
    @SuppressWarnings("All")
    public void execute(@NotNull ButtonInteraction interaction) {
        TicketBuilder ticket = FurRaidDB.getInstance()
                .getGameServiceManager()
                .fetchFTicket(interaction.getChannelId(), interaction.getGuild().getId());

        GuildSettings guildSettings = FurRaidDB.getInstance().getBlacklistManager().fetchGuildSettings(interaction.getGuild());
        this.getLanguageManager().loadProperties(guildSettings.getConfig().getSettings().getLanguage());
        FurRaidConfig.TicketFeature ticketFeature = guildSettings.getConfig().getFeatures().getTicket();

        if (!ticketFeature.isEnabled()) {
            return;
        }

        if (ticket == null) {
            interaction.replyEmbeds(buildError(getLanguageManager().get("button.close.ticket.not_found"))).setEphemeral(true).queue();
            return;
        } else if (ticket.isStaff() && !interaction.getMember().hasPermission(Permission.MODERATE_MEMBERS, Permission.ADMINISTRATOR)) {
            interaction.replyEmbeds(buildError(getLanguageManager().get("button.close.ticket.only_staff"))).setEphemeral(true).queue();
            return;
        }

        handleConfirmationFurRaid(interaction,
                this.getLanguageManager().get("button.ticket.close.confirm"),
                this.getLanguageManager().get("button.ticket.close.confirm.button"),
                new ConfirmCallback() {
                    @Override
                    public void confirm(ButtonInteraction buttonInteraction) throws Exception {
                        FurRaidDB.getInstance()
                                .getTicketManager()
                                .closeTicket(ticket, buttonInteraction.getMember().getEffectiveName());

                        interaction.getMessage().editMessageComponents(ActionRow.of(Button.primary("button:none", "Ticket closed").asDisabled())).queue();
                        buttonInteraction.replyEmbeds(buildSuccess(getLanguageManager().get("button.close.ticket.closed"))).setEphemeral(true).queue();
                    }

                    @Override
                    public void cancel(ButtonInteraction interaction) throws Exception {
                        interaction.replyEmbeds(buildError(getLanguageManager().get("button.close.ticket.cancelled"))).setEphemeral(true).queue();
                    }
                }
        );
    }
}