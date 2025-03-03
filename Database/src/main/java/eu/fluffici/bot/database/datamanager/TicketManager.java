/*
---------------------------------------------------------------------------------
File Name : TicketManager

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 14/06/2024
Last Modified : 14/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.database.datamanager;

import eu.fluffici.bot.api.beans.ticket.TicketBuilder;
import eu.fluffici.bot.api.beans.ticket.TicketMessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.jetbrains.annotations.NotNull;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TicketManager {
    /**
     * Creates a new ticket and inserts it into the database.
     *
     * @param ticket    The TicketBuilder object representing the ticket to be created.
     * @param dataSource The DataSource object for connecting to the database.
     */
    public void createTicket(@NotNull TicketBuilder ticket, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "insert into tickets (ticket_id, user_id, channel_id, status, is_staff) ";
            sql += " values (?, ?, ?, ?, ?)";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, ticket.getTicketId());
                statement.setString(2, ticket.getUserId());
                statement.setString(3, ticket.getChannelId());
                statement.setString(4, ticket.getStatus());
                statement.setBoolean(5, ticket.isStaff());

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Fetches a ticket from the database based on the given channel ID.
     *
     * @param channelId   The channel ID of the ticket to fetch.
     * @param dataSource  The DataSource object for connecting to the database.
     * @return A TicketBuilder object representing the fetched ticket, or null if the ticket was not found.
     */
    public TicketBuilder fetchTicket(String channelId, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT ticket_id, user_id, channel_id, status, created_at, is_staff FROM tickets WHERE channel_id = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, channelId);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return new TicketBuilder(
                                resultSet.getString("ticket_id"),
                                resultSet.getString("user_id"),
                                resultSet.getString("channel_id"),
                                resultSet.getString("status"),
                                resultSet.getTimestamp("created_at"),
                                resultSet.getBoolean("is_staff")
                        );
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Fetches a ticket from the database based on the given ticket ID.
     *
     * @param ticketId    The ID of the ticket to fetch.
     * @param dataSource  The DataSource object for connecting to the database.
     * @return A TicketBuilder object representing the fetched ticket, or null if the ticket was not found.
     */
    public TicketBuilder fetchTicketById(String ticketId, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT ticket_id, user_id, channel_id, status, created_at, is_staff FROM tickets WHERE ticket_id = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, ticketId);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return new TicketBuilder(
                                resultSet.getString("ticket_id"),
                                resultSet.getString("user_id"),
                                resultSet.getString("channel_id"),
                                resultSet.getString("status"),
                                resultSet.getTimestamp("created_at"),
                                resultSet.getBoolean("is_staff")
                        );
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Fetches a ticket from the database based on the given ticket ID.
     *
     * @param user    The user object of the ticket to fetch.
     * @param dataSource  The DataSource object for connecting to the database.
     * @return A TicketBuilder object representing the fetched ticket, or null if the ticket was not found.
     */
    public TicketBuilder fetchTicketByUser(UserSnowflake user, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT ticket_id, user_id, channel_id, status, created_at, is_staff FROM tickets WHERE user_id = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, user.getId());

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return new TicketBuilder(
                                resultSet.getString("ticket_id"),
                                resultSet.getString("user_id"),
                                resultSet.getString("channel_id"),
                                resultSet.getString("status"),
                                resultSet.getTimestamp("created_at"),
                                resultSet.getBoolean("is_staff")
                        );
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Updates a ticket in the database.
     *
     * @param ticket        The TicketBuilder object representing the updated ticket.
     * @param dataSource   The DataSource object for connecting to the database.
     */
    public void updateTicket(@NotNull TicketBuilder ticket, @NotNull DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("UPDATE tickets SET status = ? WHERE ticket_id = ?")) {
                statement.setString(1, ticket.getStatus());
                statement.setString(2, ticket.getTicketId());

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks if the given user has an open ticket in the database.
     *
     * @param user       The UserSnowflake object representing the user.
     * @param dataSource The DataSource object for connecting to the database.
     * @return {@code true} if the user has an open ticket, {@code false} otherwise.
     */
    public boolean hasTicket(@NotNull UserSnowflake user, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT channel_id FROM tickets WHERE user_id = ? AND status = 'OPENED'")) {
                statement.setString(1, user.getId());

                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Adds a message to the ticket with the specified ID.
     *
     * @param ticketId The ID of the ticket.
     * @param message The Message object representing the message to be added.
     * @param dataSource The DataSource object for connecting to the database.
     */
    public void addTicketMessage(String ticketId, @NotNull Message message, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "insert into tickets_messages (ticket_id, user_id, content, message_id, created_at)  ";
            sql += " values (?, ?, ?, ?, ?)";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, ticketId);
                statement.setString(2, message.getAuthor().getId());
                statement.setString(3, message.getContentRaw());
                statement.setString(4, message.getId());
                statement.setTimestamp(5, new Timestamp(System.currentTimeMillis()));

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the list of TicketMessageBuilder objects for the specified ticket ID.
     *
     * @param ticketId    The ID of the ticket.
     * @param dataSource  The DataSource object for connecting to the database.
     * @return The list of TicketMessageBuilder objects for the specified ticket ID.
     */
    public List<TicketMessageBuilder> fetchMessages(String ticketId, DataSource dataSource) {
        List<TicketMessageBuilder> ticketMessageBuilders = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT ticket_id, user_id, content, message_id, created_at FROM tickets_messages WHERE ticket_id = ? ORDER BY created_at DESC")) {
                statement.setString(1, ticketId);

                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        ticketMessageBuilders.add(new TicketMessageBuilder(
                                resultSet.getString("ticket_id"),
                                resultSet.getString("user_id"),
                                resultSet.getString("content"),
                                resultSet.getString("message_id"),
                                resultSet.getTimestamp("created_at")
                        ));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ticketMessageBuilders;
    }
}