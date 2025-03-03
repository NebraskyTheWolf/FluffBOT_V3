/*
---------------------------------------------------------------------------------
File Name : FurRaidTicketManager

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 03/07/2024
Last Modified : 03/07/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.database.datamanager.furraiddb;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import eu.fluffici.bot.api.beans.furraid.ticket.TicketBuilder;
import eu.fluffici.bot.api.beans.furraid.ticket.TicketMessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.jetbrains.annotations.NotNull;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FurRaidTicketManager {
    /**
     * Creates a new ticket and inserts it into the database.
     *
     * @param ticket    The TicketBuilder object representing the ticket to be created.
     * @param dataSource The DataSource object for connecting to the database.
     */
    public void createTicket(@NotNull TicketBuilder ticket, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "insert into furraid_tickets (guild_id, ticket_id, user_id, username, channel_id, status, is_staff, webhook_url) ";
            sql += " values (?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, ticket.getGuildId());
                statement.setString(2, ticket.getTicketId());
                statement.setString(3, ticket.getUserId());
                statement.setString(4, ticket.getUsername());
                statement.setString(5, ticket.getChannelId());
                statement.setString(6, ticket.getStatus());
                statement.setBoolean(7, ticket.isStaff());
                statement.setString(8, ticket.getWebhookUrl());

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
    public TicketBuilder fetchTicket(String channelId, String guildId, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT guild_id, ticket_id, user_id, username, channel_id, status, created_at, is_staff, webhook_url FROM furraid_tickets WHERE channel_id = ? AND guild_id = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, channelId);
                statement.setString(2, guildId);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return new TicketBuilder(
                                resultSet.getString("guild_id"),
                                resultSet.getString("ticket_id"),
                                resultSet.getString("user_id"),
                                resultSet.getString("username"),
                                resultSet.getString("channel_id"),
                                resultSet.getString("status"),
                                resultSet.getTimestamp("created_at"),
                                resultSet.getBoolean("is_staff"),
                                resultSet.getString("webhook_url")
                        );
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<TicketBuilder> fetchAllTickets(String guildId, DataSource dataSource) {
        List<TicketBuilder> ticketBuilders = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT guild_id, ticket_id, user_id, username, channel_id, status, created_at, is_staff FROM furraid_tickets WHERE guild_id = ? ORDER BY created_at DESC, IF(status = 'OPENED', 0, 1)";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, guildId);

                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        ticketBuilders.add(new TicketBuilder(
                                resultSet.getString("guild_id"),
                                resultSet.getString("ticket_id"),
                                resultSet.getString("user_id"),
                                resultSet.getString("username"),
                                resultSet.getString("channel_id"),
                                resultSet.getString("status"),
                                resultSet.getTimestamp("created_at"),
                                resultSet.getBoolean("is_staff"),
                                "disclosed"
                        ));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ticketBuilders;
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
            String sql = "SELECT guild_id, ticket_id, user_id, username, channel_id, status, created_at, is_staff, webhook_url FROM furraid_tickets WHERE ticket_id = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, ticketId);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return new TicketBuilder(
                                resultSet.getString("guild_id"),
                                resultSet.getString("ticket_id"),
                                resultSet.getString("user_id"),
                                resultSet.getString("username"),
                                resultSet.getString("channel_id"),
                                resultSet.getString("status"),
                                resultSet.getTimestamp("created_at"),
                                resultSet.getBoolean("is_staff"),
                                resultSet.getString("webhook_url")
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
    public TicketBuilder fetchTicketByUser(UserSnowflake user, String guildId, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT guild_id, ticket_id, user_id, username, channel_id, status, created_at, is_staff, webhook_url FROM furraid_tickets WHERE user_id = ? AND guild_id = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, user.getId());
                statement.setString(2, guildId);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return new TicketBuilder(
                                resultSet.getString("guild_id"),
                                resultSet.getString("ticket_id"),
                                resultSet.getString("user_id"),
                                resultSet.getString("username"),
                                resultSet.getString("channel_id"),
                                resultSet.getString("status"),
                                resultSet.getTimestamp("created_at"),
                                resultSet.getBoolean("is_staff"),
                                resultSet.getString("webhook_url")
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
            try (PreparedStatement statement = connection.prepareStatement("UPDATE furraid_tickets SET status = ? WHERE ticket_id = ? AND guild_id = ?")) {
                statement.setString(1, ticket.getStatus());
                statement.setString(2, ticket.getTicketId());
                statement.setString(3, ticket.getGuildId());

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
    public boolean hasTicket(@NotNull UserSnowflake user, String guildId, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT channel_id FROM furraid_tickets WHERE user_id = ? AND status = 'OPENED' AND guild_id = ?")) {
                statement.setString(1, user.getId());
                statement.setString(2, guildId);

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
            String sql = "insert into furraid_ticket_messages (guild_id, ticket_id, user_id, content, message_id, created_at, sender)  ";
            sql += " values (?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, message.getGuildId());
                statement.setString(2, ticketId);
                statement.setString(3, message.getAuthor().getId());
                statement.setString(4, message.getContentRaw());
                statement.setString(5, message.getId());
                statement.setTimestamp(6, new Timestamp(System.currentTimeMillis()));

                JsonObject sender = new JsonObject();
                sender.addProperty("username", message.getAuthor().getEffectiveName());
                sender.addProperty("avatarUrl", message.getAuthor().getAvatarUrl());

                statement.setString(7, sender.toString());

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
            try (PreparedStatement statement = connection.prepareStatement("SELECT guild_id, ticket_id, user_id, content, message_id, created_at, sender FROM furraid_ticket_messages WHERE ticket_id = ? ORDER BY created_at DESC")) {
                statement.setString(1, ticketId);

                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        ticketMessageBuilders.add(new TicketMessageBuilder(
                                resultSet.getString("guild_id"),
                                resultSet.getString("ticket_id"),
                                resultSet.getString("user_id"),
                                resultSet.getString("content"),
                                resultSet.getString("message_id"),
                                resultSet.getTimestamp("created_at"),
                                new Gson().fromJson(resultSet.getString("sender"), JsonObject.class)
                        ));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ticketMessageBuilders;
    }

    /**
     * Retrieves the list of TicketMessageBuilder objects for the specified ticket ID.
     *
     * @param ticketId    The ID of the ticket.
     * @param dataSource  The DataSource object for connecting to the database.
     * @return The list of TicketMessageBuilder objects for the specified ticket ID.
     */
    public TicketMessageBuilder fetchMessageById(String ticketId, String messageId, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT guild_id, ticket_id, user_id, content, message_id, created_at, sender FROM furraid_ticket_messages WHERE ticket_id = ? AND message_id = ? ORDER BY created_at DESC")) {
                statement.setString(1, ticketId);
                statement.setString(2, messageId);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return new TicketMessageBuilder(
                                resultSet.getString("guild_id"),
                                resultSet.getString("ticket_id"),
                                resultSet.getString("user_id"),
                                resultSet.getString("content"),
                                resultSet.getString("message_id"),
                                resultSet.getTimestamp("created_at"),
                                new Gson().fromJson(resultSet.getString("sender"), JsonObject.class)
                        );
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}