package eu.fluffici.bot.components.commands.moderator;

/*
---------------------------------------------------------------------------------
File Name : CommandTicket.java

Developer : vakea
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 14/06/2024
Last Modified : 14/06/2024

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


import eu.fluffici.bot.FluffBOT;
import eu.fluffici.bot.api.beans.ticket.TicketBuilder;
import eu.fluffici.bot.api.bucket.CommandHandle;
import eu.fluffici.bot.components.commands.Command;
import eu.fluffici.bot.api.interactions.CommandCategory;
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

import java.util.concurrent.CompletableFuture;

@CommandHandle
@SuppressWarnings("All")
public class CommandTicket extends Command {

    private final FluffBOT instance;

    public CommandTicket(FluffBOT instance) {
        super("ticket", "Management of the server tickets", CommandCategory.MODERATOR);

        this.instance = instance;

        this.getSubcommandData().add(new SubcommandData("open-ticket", "Open a ticket for a specific user")
                .addOption(OptionType.USER, "user", "Select a user")
        );

        this.getSubcommandData().add(new SubcommandData("close-ticket", "Close the ticket channel"));

        this.getSubcommandData().add(new SubcommandData("add-user", "Close the ticket channel")
                .addOption(OptionType.USER, "user", "Select a user")
        );

        this.getSubcommandData().add(new SubcommandData("transcript", "Regenerate the transcript of a specific ticket")
                .addOption(OptionType.STRING, "ticket-id", "Enter the ticketId")
        );

        this.setPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS));
    }

    @Override
    public void execute(@NotNull CommandInteraction interaction) {
        String command = interaction.getSubcommandName();

        switch (command) {
            case "open-ticket" -> this.handleOpenTicket(interaction);
            case "close-ticket" -> this.handleCloseTicket(interaction);
            case "add-user" -> this.handleTicketAddUser(interaction);
            case "transcript" -> this.handleTicketTranscript(interaction);
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

        boolean hasTicket = FluffBOT.getInstance().getGameServiceManager().hasTicket(target);

        if (hasTicket) {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.ticket.user.already_has_ticket"))).setEphemeral(true).queue();
            return;
        }

        TextChannel ticketChannel = FluffBOT.getInstance().getTicketManager().createTicket(interaction.getGuild(), target, true);
        if (ticketChannel == null) {
            interaction.reply("Failed to create ticket").setEphemeral(true).queue();
            return;
        }

        interaction.replyEmbeds(this.buildSuccess(this.getLanguageManager().get("command.ticket.user.ticket_created")))
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
        TicketBuilder ticket = FluffBOT.getInstance().getGameServiceManager().fetchTicket(interaction.getChannelId());

        if (ticket == null) {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.ticket.not_found"))).setEphemeral(true).queue();
            return;
        }

        FluffBOT.getInstance()
                .getTicketManager()
                .closeTicket(ticket, interaction.getMember().getEffectiveName());

        this.getUserManager().addPointToStaff(interaction.getUser(), 5);

        interaction.replyEmbeds(this.buildSuccess(this.getLanguageManager().get("command.ticket.closed")))
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

        TicketBuilder ticket = FluffBOT.getInstance().getGameServiceManager().fetchTicket(interaction.getChannelId());

        if (ticket == null) {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.ticket.not_found"))).setEphemeral(true).queue();
            return;
        }

        if (interaction.getChannel() instanceof TextChannel channel) {
            channel.upsertPermissionOverride(interaction.getGuild().getMemberById(target.getId()))
                    .grant(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_ATTACH_FILES, Permission.MESSAGE_HISTORY)
                    .queue();

            channel.sendMessage(this.getLanguageManager().get("command.ticket.user_added_channel", target.getAsMention())).queue();
        }


        interaction.replyEmbeds(this.buildSuccess(this.getLanguageManager().get("command.ticket.user_added", target.getAsMention(), interaction.getChannel().getAsMention())))
                .setEphemeral(true)
                .queue();
    }

    /**
     * Handles the transcript for a ticket. This method is called when a ticket is closed to generate a transcript of the ticket conversation.
     *
     * @param interaction The CommandInteraction representing the event triggered by the user.
     */
    private void handleTicketTranscript(@NotNull CommandInteraction interaction) {
        String ticketId = interaction.getOption("ticket-id").getAsString();

        interaction.deferReply(true);

        TicketBuilder ticket = FluffBOT.getInstance().getGameServiceManager().fetchTicket(ticketId);

        if (ticket == null) {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.ticket.not_found"))).setEphemeral(true).queue();
            return;
        }

        CompletableFuture.runAsync(() -> this.instance.getTicketManager().transcript(ticketId, ""))
                .whenComplete(((unused, throwable) -> {

                }))
                .exceptionally((e) -> {
                    interaction.getHook().sendMessageEmbeds(this.buildError(this.getLanguageManager().get("command.ticket.transcript_error"))).setEphemeral(true).queue();
                    return null;
                });
    }
}
