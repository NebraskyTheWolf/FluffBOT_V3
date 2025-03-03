/*
---------------------------------------------------------------------------------
File Name : GuildDataManager

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 11/06/2024
Last Modified : 11/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.database.datamanager;

import eu.fluffici.bot.api.beans.channel.AutoReactionBuilder;
import eu.fluffici.bot.api.beans.channel.AutoReactions;
import org.jetbrains.annotations.NotNull;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GuildDataManager {
    /**
     * Creates a channel auto reaction entry in the database.
     *
     * @param channelId The AutoReactions object containing the channelId and reactionType.
     * @param type The AutoReactions object containing the channelId and reactionType.
     * @param dataSource The DataSource object to establish a connection with the database.
     */
    public void createChannelAutoReact(String channelId, String type, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "INSERT INTO channel_auto_reactions (channel_id, reaction_type) VALUES (?, ?)";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, channelId);
                statement.setString(2, type);

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Fetches the AutoReactions for a given channel from the database.
     *
     * @param channelId The ID of the channel to fetch reactions for.
     * @param dataSource The DataSource object to establish a connection with the database.
     * @return The AutoReactions object for the specified channel or null if not found.
     */
    public AutoReactionBuilder fetchChannelReactions(String channelId, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT * FROM channel_auto_reactions WHERE channel_id = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, channelId);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        String reactionType = resultSet.getString("reaction_type");
                        List<String> reactions = getReactions(dataSource, reactionType);

                        return AutoReactionBuilder
                                .builder()
                                .channel(new AutoReactions(channelId, reactionType))
                                .reactions(reactions)
                                .build();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @NotNull
    private static List<String> getReactions(DataSource dataSource, String reactionType) throws SQLException {
        List<String> reactions = new ArrayList<>();
        try (Connection connectionType = dataSource.getConnection()) {
            try (PreparedStatement statementType = connectionType.prepareStatement("SELECT emoji_id, reaction_type FROM channel_reactions_type WHERE reaction_type = ?")) {
                statementType.setString(1, reactionType);

                try (ResultSet resultSetType = statementType.executeQuery()) {
                    while (resultSetType.next()) {
                        reactions.add(resultSetType.getString("emoji_id"));
                    }
                }
            }
        }
        return reactions;
    }

    /**
     * Checks if a channel has reactions in the database.
     *
     * @param channelId The ID of the channel to check.
     * @param dataSource The DataSource object to establish a connection with the database.
     * @return true if the channel has reactions, false otherwise.
     */
    public boolean hasChannelReactions(String channelId, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT COUNT(*) FROM channel_auto_reactions WHERE channel_id = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, channelId);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        int count = resultSet.getInt(1);
                        return count > 0;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Deletes the auto reaction entries for a specific channel from the database.
     *
     * @param channelId The ID of the channel to delete auto reactions for.
     * @param dataSource The DataSource object to establish a connection with the database.
     */
    public void deleteAutoReaction(String channelId, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "DELETE FROM channel_auto_reactions WHERE channel_id = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, channelId);

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}