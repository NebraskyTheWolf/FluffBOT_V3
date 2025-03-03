/*
---------------------------------------------------------------------------------
File Name : ITicketManager

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 14/06/2024
Last Modified : 14/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api.hooks;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import java.util.List;

/**
 * The ITicketManager interface represents a ticket management system that allows creating, closing, and
 * interacting with tickets in a guild.
 *
 * @param <T> the type of ticket
 * @param <V> the type of ticket message
 */
public interface ITicketManager<T, V, B> {

    /**
     * Creates a ticket for the specified user.
     *
     * @param user the user for whom the ticket is created
     * @return true if the ticket is successfully created, false otherwise
     * @throws Exception if an error occurs while creating the ticket
     */
    TextChannel createTicket(Guild guild, UserSnowflake user, boolean isStaff) throws Exception;

    /**
     * Closes a ticket with the specified ticket ID.
     *
     * @param ticket the object of the ticket to be closed
     * @throws Exception if an error occurs while closing the ticket
     */
    void closeTicket(T ticket, String closedBy) throws Exception;

    /**
     * Fetches the messages of a ticket with the specified ticket ID.
     *
     * @param ticketId the ID of the ticket for which the messages are retrieved
     * @return a list of messages associated with the ticket
     * @throws Exception if an error occurs while fetching the messages
     */
    List<V> fetchTicketMessages(String ticketId, String guildId) throws Exception;

    /**
     * Adds a message to a ticket with the specified ticket ID.
     *
     * @param message the message to be added to the ticket
     * @param ticketId the ID of the ticket to which the message is added
     * @throws Exception if an error occurs while adding the message
     */
    void addTicketMessage(Message message, String ticketId) throws Exception;

    /**
     * Retrieves the transcript of a ticket.
     *
     * @param ticketId the ID of the ticket for which the transcript is requested
     * @return true if the transcript is successfully retrieved, false otherwise
     * @throws Exception if an error occurs while retrieving the transcript
     */
    B transcript(String ticketId, String guildId) throws Exception;
}
