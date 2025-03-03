package eu.fluffici.bot.database.datamanager;

/*
---------------------------------------------------------------------------------
File Name : ChannelDataManager.java

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


import eu.fluffici.bot.api.beans.players.ChannelBean;
import eu.fluffici.bot.api.beans.players.ChannelOption;
import eu.fluffici.bot.api.beans.players.ChannelRent;
import eu.fluffici.bot.api.beans.players.DummyChannel;
import eu.fluffici.bot.api.game.GameId;
import net.dv8tion.jda.api.entities.UserSnowflake;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class ChannelDataManager {
    /**
     * Creates a new channel in the database.
     *
     * @param channel    the ChannelBean object representing the channel to be created
     * @param dataSource the DataSource object representing the connection pool to the database
     */
    public void createChannel(ChannelBean channel, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO players_channel (user_id, channel_id, created_at, migration_id) VALUES (?, ?, ?, ?)")) {
                statement.setString(1, channel.getUserId());
                statement.setString(2, channel.getChannelId());
                statement.setTimestamp(3, channel.getCreatedAt());
                statement.setString(4, channel.getMigrationId());

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO players_channel_rent (payer_id, channel_id, transaction_id, paid_at) VALUES (?, ?, ?, ?)")) {
                statement.setString(1, channel.getUserId());
                statement.setString(2, channel.getChannelId());
                statement.setString(3, GameId.generateId());
                statement.setTimestamp(4, channel.getCreatedAt());

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO players_channel_options (channel_id, is_auto_renew) VALUES (?, ?)")) {
                statement.setString(1, channel.getChannelId());
                statement.setBoolean(2, channel.getChannelOption().isAutoRenew());

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ChannelOption getChannelOption(String channelId, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT is_auto_renew FROM players_channel_options WHERE channel_id = ?")) {
                statement.setString(1, channelId);

                try (ResultSet resultSet = statement.executeQuery()) {
                    return new ChannelOption(
                            resultSet.getBoolean("is_auto_renew")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new ChannelOption(false);
    }

    public void updateChannelOption(boolean isAutoRenewal, String channelId, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("UPDATE players_channel_options SET is_auto_renew = ? WHERE channel_id = ?")) {
                statement.setBoolean(1, isAutoRenewal);
                statement.setString(2, channelId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the ChannelBean object representing a channel for the given user from the database.
     *
     * @param currentOwner the UserSnowflake object representing the current owner of the channel
     * @param dataSource   the DataSource object representing the connection pool to the database
     * @return the ChannelBean object representing the channel, or null if the channel is not found
     * @throws Exception if there is an error retrieving the channel
     */
    public ChannelBean getChannel(UserSnowflake currentOwner, DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT channel_id, created_at, migration_id FROM players_channel WHERE user_id = ?")) {
                statement.setString(1, currentOwner.getId());

                try (ResultSet resultset = statement.executeQuery()) {
                    if (resultset.next()) {
                        String channelId = resultset.getString("channel_id");
                        Timestamp createdAt = resultset.getTimestamp("created_at");
                        String migrationId = resultset.getString("migration_id");

                        List<ChannelRent> channelRents = new ArrayList<>();
                        Timestamp latestPaidAt = null;

                        try (Connection connectionTwo = dataSource.getConnection()) {
                            try (PreparedStatement statementTwo = connectionTwo.prepareStatement("SELECT payer_id, channel_id, transaction_id, paid_at FROM players_channel_rent WHERE channel_id = ? ORDER BY paid_at DESC")) {
                                statementTwo.setString(1, channelId);

                                try (ResultSet resultSet = statementTwo.executeQuery()) {
                                    while (resultSet.next()) {
                                        Timestamp paidAt = resultSet.getTimestamp("paid_at");
                                        ChannelRent channelRent = new ChannelRent(
                                                resultSet.getString("payer_id"),
                                                resultSet.getString("channel_id"),
                                                resultSet.getString("transaction_id"),
                                                paidAt
                                        );
                                        channelRents.add(channelRent);
                                        if (latestPaidAt == null || paidAt.after(latestPaidAt)) {
                                            latestPaidAt = paidAt;
                                        }
                                    }
                                }
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }

                        boolean isAutoRenew = false;
                        try (Connection connectionTwo = dataSource.getConnection()) {
                            try (PreparedStatement statementTwo = connectionTwo.prepareStatement("SELECT is_auto_renew FROM players_channel_options WHERE channel_id = ?")) {
                                statementTwo.setString(1, channelId);

                                try (ResultSet resultSet = statementTwo.executeQuery()) {
                                    if (resultSet.next()) {
                                        isAutoRenew = resultSet.getBoolean("is_auto_renew");
                                    }
                                }
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }

                        boolean isInGracePeriod = false;
                        if (latestPaidAt != null) {
                            LocalDateTime paidAtDateTime = latestPaidAt.toLocalDateTime();
                            LocalDateTime now = LocalDateTime.now();
                            LocalDateTime nextPaymentDueDate = paidAtDateTime.plusMonths(1).truncatedTo(ChronoUnit.DAYS);
                            LocalDateTime gracePeriodEnd = nextPaymentDueDate.plusDays(4);

                            if (now.isBefore(gracePeriodEnd)) {
                                isInGracePeriod = true;
                            }
                        }

                        return new ChannelBean(currentOwner.getId(), channelId, migrationId, createdAt, channelRents, new ChannelOption(isAutoRenew), isInGracePeriod);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean hasChannel(UserSnowflake user, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT channel_id FROM players_channel WHERE user_id = ?")) {
                statement.setString(1, user.getId());

                try (ResultSet resultset = statement.executeQuery()) {
                    return resultset.next();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public void transferOwner(UserSnowflake currentOwner, UserSnowflake newOwner, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("UPDATE players_channel SET user_id = ? WHERE user_id = ?")) {
                statement.setString(1, newOwner.getId());
                statement.setString(2, currentOwner.getId());

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteChannel(UserSnowflake currentOwner, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("DELETE FROM players_channel WHERE user_id = ?")) {
                statement.setString(1, currentOwner.getId());
                statement.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes the channels that have not paid the rent within the grace period.
     *
     * @param dataSource the DataSource object representing the connection pool to the database
     * @return a List of strings containing the channel IDs of the deleted channels
     */
    public List<DummyChannel> deleteNotPaidChannels(DataSource dataSource) {
        List<DummyChannel> removedChannels = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {
            // Retrieve all channels with their latest rent payment date
            String selectQuery = "SELECT pc.channel_id, pc.user_id, MAX(pcr.paid_at) AS latest_paid_at " +
                    "FROM players_channel pc " +
                    "LEFT JOIN players_channel_rent pcr ON pc.channel_id = pcr.channel_id " +
                    "GROUP BY pc.channel_id, pc.user_id";
            try (PreparedStatement selectStatement = connection.prepareStatement(selectQuery);
                 ResultSet resultSet = selectStatement.executeQuery()) {

                while (resultSet.next()) {
                    String channelId = resultSet.getString("channel_id");
                    String userId = resultSet.getString("user_id");
                    Timestamp latestPaidAt = resultSet.getTimestamp("latest_paid_at");

                    boolean shouldDelete = false;

                    if (latestPaidAt != null) {
                        LocalDateTime paidAtDateTime = latestPaidAt.toLocalDateTime();
                        LocalDateTime now = LocalDateTime.now();
                        LocalDateTime nextPaymentDueDate = paidAtDateTime.plusMonths(1).truncatedTo(ChronoUnit.DAYS);
                        LocalDateTime gracePeriodEnd = nextPaymentDueDate.plusDays(4);

                        if (now.isAfter(gracePeriodEnd)) {
                            shouldDelete = true;
                        }
                    } else {
                        shouldDelete = true;
                    }

                    if (shouldDelete) {
                        try (PreparedStatement deleteStatement = connection.prepareStatement("DELETE FROM players_channel WHERE channel_id = ?")) {
                            deleteStatement.setString(1, channelId);
                            deleteStatement.executeUpdate();
                        }
                        removedChannels.add(new DummyChannel(
                                channelId,
                                userId,
                                latestPaidAt
                        ));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return removedChannels;
    }

    /**
     * Retrieves the list of channels that are expiring soon.
     *
     * @param dataSource the DataSource object representing the connection pool to the database
     * @return a List of DummyChannel objects representing the channels that are expiring soon
     */
    public List<DummyChannel> getChannelsExpiringSoon(DataSource dataSource) {
        List<DummyChannel> expiringChannels = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {
            // Retrieve all channels with their latest rent payment date
            String selectQuery = "SELECT pc.channel_id AS channel_id, pc.user_id AS user_id, MAX(pcr.paid_at) AS latest_paid_at " +
                    "FROM players_channel pc " +
                    "LEFT JOIN players_channel_rent pcr ON pc.channel_id = pcr.channel_id " +
                    "GROUP BY pc.channel_id, pc.user_id";
            try (PreparedStatement selectStatement = connection.prepareStatement(selectQuery);
                 ResultSet resultSet = selectStatement.executeQuery()) {

                while (resultSet.next()) {
                    String channelId = resultSet.getString("channel_id");
                    String userId = resultSet.getString("user_id");
                    Timestamp latestPaidAt = resultSet.getTimestamp("latest_paid_at");

                    if (latestPaidAt != null) {
                        LocalDateTime paidAtDateTime = latestPaidAt.toLocalDateTime();
                        LocalDateTime now = LocalDateTime.now();
                        LocalDateTime expirationNoticeDate = paidAtDateTime.plusMonths(1).minusDays(5).truncatedTo(ChronoUnit.DAYS);

                        // Check if the current date is within the 9-day notice period
                        if (now.isAfter(expirationNoticeDate) && now.isBefore(paidAtDateTime.plusMonths(1).plusDays(4))) {
                            expiringChannels.add(new DummyChannel(
                                    channelId,
                                    userId,
                                    latestPaidAt
                            ));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return expiringChannels;
    }

    public DummyChannel getChannelsExpiringSoon(String id, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            String selectQuery = "SELECT pc.channel_id AS channel_id, " +
                    "pc.user_id AS user_id, " +
                    "MAX(pcr.paid_at) AS latest_paid_at " +
                    "FROM players_channel pc " +
                    "LEFT JOIN players_channel_rent pcr " +
                    "ON pc.channel_id = pcr.channel_id " +
                    "AND pc.channel_id = ? " +
                    "GROUP BY pc.channel_id, pc.user_id";
            try (PreparedStatement selectStatement = connection.prepareStatement(selectQuery)) {
                selectStatement.setString(1, id);

                try (ResultSet resultSet = selectStatement.executeQuery()) {
                    while (resultSet.next()) {
                        String channelId = resultSet.getString("channel_id");
                        String userId = resultSet.getString("user_id");
                        Timestamp latestPaidAt = resultSet.getTimestamp("latest_paid_at");

                        if (latestPaidAt != null) {
                            LocalDateTime paidAtDateTime = latestPaidAt.toLocalDateTime();
                            LocalDateTime now = LocalDateTime.now();
                            LocalDateTime expirationNoticeDate = paidAtDateTime.plusMonths(1).minusDays(5).truncatedTo(ChronoUnit.DAYS);

                            // Check if the current date is within the 9-day notice period
                            if (now.isAfter(expirationNoticeDate) && now.isBefore(paidAtDateTime.plusMonths(1).plusDays(4))) {
                                return new DummyChannel(
                                        channelId,
                                        userId,
                                        latestPaidAt
                                );
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void addChannelRent(ChannelRent rent, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO players_channel_rent (payer_id, channel_id, transaction_id, paid_at) VALUES (?, ?, ?, ?)")) {
                statement.setString(1, rent.getPayerId());
                statement.setString(2, rent.getChannelId());
                statement.setString(3, rent.getTransactionId());
                statement.setTimestamp(4, new Timestamp(System.currentTimeMillis()));

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
