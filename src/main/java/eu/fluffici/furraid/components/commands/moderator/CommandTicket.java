/*
---------------------------------------------------------------------------------
File Name : CommandTicket

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 03/07/2024
Last Modified : 03/07/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.furraid.components.commands.moderator;

import eu.fluffici.bot.api.beans.furraid.GuildSettings;
import eu.fluffici.bot.api.beans.furraid.ticket.TicketBuilder;
import eu.fluffici.bot.api.interactions.CommandCategory;
import eu.fluffici.bot.api.interactions.FCommand;
import eu.fluffici.furraid.FurRaidDB;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a command associated with a ticket.
 */
@SuppressWarnings("All")
public class CommandTicket extends FCommand {

    private FurRaidDB instance;

    public CommandTicket(FurRaidDB instance) {
        super("ticket", "Management of the server tickets", CommandCategory.MODERATOR);

        this.instance = instance;

        this.getSubcommandData().add(new SubcommandData("open-ticket", "Open a ticket for a specific user")
                .addOption(OptionType.USER, "user", "Select a user")
        );

        this.getSubcommandData().add(new SubcommandData("close-ticket", "Close the ticket channel"));

        this.getSubcommandData().add(new SubcommandData("add-user", "Close the ticket channel")
                .addOption(OptionType.USER, "user", "Select a user")
        );

        this.setPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS));
    }

    @Override
    public void execute(@NotNull CommandInteraction interaction, GuildSettings guildSettings) {
        String command = interaction.getSubcommandName();

        if (!guildSettings.getConfig().getFeatures().getTicket().isEnabled()) {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.ticket.feature_disabled"))).setEphemeral(true).queue();
            return;
        }

        this.getLanguageManager().loadProperties(guildSettings.getConfig().getSettings().getLanguage());

        switch (command) {
            case "open-ticket" -> this.handleOpenTicket(interaction);
            case "close-ticket" -> this.handleCloseTicket(interaction);
            case "add-user" -> this.handleTicketAddUser(interaction);
        }
    }

    /**
     * Handles the opening of a ticket. This method is called when a user requests to open a ticket.
     *
     * @param interaction The CommandInteraction representing the event triggered by the user.
     */
    @SneakyThrows
    private void handleOpenTicket(@NotNull CommandInteraction interaction) {
        User target = interaction.getOption("user").getAsUser();

        boolean hasTicket = this.instance.getGameServiceManager().hasFTicket(target, interaction.getGuild().getId());

        if (hasTicket) {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.ticket.user.already_has_ticket"))).setEphemeral(true).queue();
            return;
        }

        TextChannel ticketChannel = this.instance.getTicketManager().createTicket(interaction.getGuild() ,target, true);
        if (ticketChannel == null) {
            interaction.reply("Failed to create ticket").setEphemeral(true).queue();
            return;
        }

        interaction.replyEmbeds(this.buildSuccess(this.getLanguageManager().get("command.ticket.user.ticket_created", target.getAsMention())))
                .setEphemeral(true)
                .addActionRow(Button.link(ticketChannel.getJumpUrl(), "Ticket channel"))
                .queue();
    }

    /**
     * Closes a ticket.
     *
     * @param interaction The CommandInteraction representing the event triggered by the user.
     */
    @SneakyThrows
    private void handleCloseTicket(@NotNull CommandInteraction interaction) {
        TicketBuilder ticket = this.instance.getGameServiceManager().fetchFTicket(interaction.getChannelId(), interaction.getGuild().getId());

        if (ticket == null) {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.ticket.not_found"))).setEphemeral(true).queue();
            return;
        }

        this.instance.getTicketManager().closeTicket(ticket, interaction.getMember().getEffectiveName());

        interaction.replyEmbeds(this.buildSuccess(this.getLanguageManager().get("command.ticket.closed", "<@" + ticket.getUserId() + ">")))
                .setEphemeral(true)
                .queue();
    }

    /**
     * Handles the addition of a user to a ticket.
     *
     * @param interaction The CommandInteraction representing the event triggered by the user.
     */
    private void handleTicketAddUser(@NotNull CommandInteraction interaction) {
        User target = interaction.getOption("user").getAsUser();

        TicketBuilder ticket = this.instance.getGameServiceManager().fetchFTicket(interaction.getChannelId(), interaction.getGuild().getId());

        if (ticket == null) {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.ticket.not_found"))).setEphemeral(true).queue();
            return;
        }

        if (interaction.getChannel() instanceof TextChannel channel) {
            channel.upsertPermissionOverride(interaction.getGuild().getMemberById(target.getId()))
                    .grant(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_ATTACH_FILES, Permission.MESSAGE_HISTORY)
                    .queue();

            channel.sendMessage(this.getLanguageManager().get("command.ticket.user_added_channel", target.getAsMention(), interaction.getUser().getAsMention())).queue();
        }


        interaction.replyEmbeds(this.buildSuccess(this.getLanguageManager().get("command.ticket.user_added", target.getAsMention(), interaction.getChannel().getAsMention())))
                .setEphemeral(true)
                .queue();
    }
}