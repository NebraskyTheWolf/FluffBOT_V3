/*
---------------------------------------------------------------------------------
File Name : EconomyDataManager

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 06/06/2024
Last Modified : 06/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.database.datamanager;

import eu.fluffici.bot.api.beans.players.EconomyHistory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EconomyDataManager {
    /**
     * Inserts an {@link EconomyHistory} object into the "economy_history" table of the database.
     *
     * @param economyHistory The {@link EconomyHistory} object to be inserted into the database.
     * @param dataSource     The {@link DataSource} used to establish a connection to the database.
     *
     */
    public void createEconomyHistory(EconomyHistory economyHistory, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "insert into economy_history (user_id, currency, operation, amount, created_at)";
            sql += " values (?, ?, ?, ?, ?)";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, economyHistory.getUserId());
                statement.setString(2, economyHistory.getCurrency().name());
                statement.setString(3, economyHistory.getOperation().name());
                statement.setInt(4, economyHistory.getAmount());
                statement.setTimestamp(5, economyHistory.getCreatedAt());

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves all records from the "economy_history" table in the database.
     *
     * @param dataSource The DataSource used to establish a connection to the database.
     * @return A list of EconomyHistory objects representing the records retrieved from the table.
     */
    public List<EconomyHistory> getAllEconomy(DataSource dataSource) {
        List<EconomyHistory> birthdays = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {

            try (PreparedStatement statement = connection.prepareStatement("SELECT user_id, operation, amount, currency, created_at FROM economy_history")) {
                try (ResultSet resultset = statement.executeQuery()) {
                    while (resultset.next()) {
                        String userId = resultset.getString("user_id");
                        String operation = resultset.getString("operation");
                        String currency = resultset.getString("currency");
                        int amount = resultset.getInt("amount");
                        Timestamp createdAt = resultset.getTimestamp("created_at");

                        birthdays.add(new EconomyHistory(userId, amount, EconomyHistory.Currency.valueOf(currency), EconomyHistory.Operation.valueOf(operation), createdAt));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return birthdays;
    }
}