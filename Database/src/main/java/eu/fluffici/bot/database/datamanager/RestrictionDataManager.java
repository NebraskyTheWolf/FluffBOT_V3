/*
---------------------------------------------------------------------------------
File Name : RestrictionDataManager

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 11/06/2024
Last Modified : 11/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.database.datamanager;

import eu.fluffici.bot.api.beans.players.RestrictedAccess;
import net.dv8tion.jda.api.entities.UserSnowflake;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RestrictionDataManager {

    /**
     * Fetches the player restriction for a specific user from the database.
     *
     * @param user The UserSnowflake object representing the user.
     * @param dataSource The DataSource object used to establish a database connection.
     * @return The RestrictedAccess object containing the user's restriction information.
     */
    public RestrictedAccess fetchPlayerRestriction(UserSnowflake user, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT user_id, author_id, reason, is_active, created_at FROM players_restricted WHERE user_id = ?")) {
                statement.setString(1, user.getId());
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return new RestrictedAccess(
                                UserSnowflake.fromId(resultSet.getString("user_id")),
                                UserSnowflake.fromId(resultSet.getString("author_id")),
                                resultSet.getString("reason"),
                                resultSet.getBoolean("is_active"),
                                resultSet.getTimestamp("created_at")
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
     * Saves the player restriction to the database.
     *
     * @param restrictedAccess The RestrictedAccess object containing the user, author, reason, status, and creation timestamp of the restriction.
     * @param dataSource The DataSource object used to establish a connection with the database.
     */
    public void savePlayerRestriction(RestrictedAccess restrictedAccess, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO players_restricted (user_id, author_id, reason, is_active, created_at) VALUES (?, ?, ?, ?, ?)")) {
                statement.setString(1, restrictedAccess.getUser().getId());
                statement.setString(2, restrictedAccess.getAuthor().getId());
                statement.setString(3, restrictedAccess.getReason());
                statement.setBoolean(4, restrictedAccess.isActive());
                statement.setTimestamp(5, restrictedAccess.getCreatedAt());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}