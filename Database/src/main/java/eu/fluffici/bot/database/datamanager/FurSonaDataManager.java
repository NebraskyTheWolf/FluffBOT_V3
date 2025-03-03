/*
---------------------------------------------------------------------------------
File Name : FurSonaDataManager

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 08/07/2024
Last Modified : 08/07/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.database.datamanager;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import eu.fluffici.bot.api.beans.players.FurSonaBuilder;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.jetbrains.annotations.NotNull;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FurSonaDataManager {
    /**
     * Creates a new character in the database using the provided builder and data source.
     *
     * @param builder The FurSonaBuilder object used to set the character's attributes.
     * @param dataSource The data source used to establish a connection to the database.
     */
    public void createCharacter(@NotNull FurSonaBuilder builder, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO players_fursona " +
                    "(owner_id, character_age, character_color, character_name, character_quote, character_specie, character_picture, character_gender, character_refsheet, character_pronouns, character_descriptions, character_extra, character_extra_pictures) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"))
            {
                statement.setString(1, builder.getOwnerId().getId());
                statement.setInt(2, builder.getCharacterAge());
                statement.setInt(3, builder.getCharacterColor());
                statement.setString(4, builder.getCharacterName());
                statement.setString(5, builder.getCharacterQuote());
                statement.setString(6, builder.getCharacterSpecie());
                statement.setString(7, builder.getCharacterPictureURL());
                statement.setString(8, builder.getCharacterGender());
                statement.setString(9, builder.getCharacterRefsheetURL());
                statement.setString(10, builder.getCharacterPronouns());
                statement.setString(11, builder.getCharacterDescriptions());
                statement.setString(12, new JsonObject().toString());
                statement.setString(13, new JsonObject().toString());

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates the character attributes using the provided builder and data source.
     *
     * @param builder The FurSonaBuilder object containing the updated character attributes.
     * @param dataSource The data source used to establish a connection to the database.
     */
    public void updateCharacter(@NotNull FurSonaBuilder builder, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("UPDATE players_fursona SET character_age = ?, character_color = ?, character_name = ?, character_quote = ?, character_specie = ?, character_picture = ?, character_gender = ?, character_refsheet = ?, character_pronouns = ?, character_descriptions = ?, character_extra = ?, character_extra_pictures = ? WHERE owner_id = ?")) {
                statement.setInt(1, builder.getCharacterAge());
                statement.setInt(2, builder.getCharacterColor());
                statement.setString(3, builder.getCharacterName());
                statement.setString(4, builder.getCharacterQuote());
                statement.setString(5, builder.getCharacterSpecie());
                statement.setString(6, builder.getCharacterPictureURL());
                statement.setString(7, builder.getCharacterGender());
                statement.setString(8, builder.getCharacterRefsheetURL());
                statement.setString(9, builder.getCharacterPronouns());
                statement.setString(10, builder.getCharacterDescriptions());
                statement.setString(11, builder.getCharacterExtra().toString());
                statement.setString(12, builder.getCharacterExtraPictures().toString());
                statement.setString(13, builder.getOwnerId().getId());

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks if a user has a character in the players_fursona table in the database.
     *
     * @param user       The UserSnowflake object representing the user.
     * @param dataSource The data source used to establish a connection to the database.
     * @return true if the user has a character, false otherwise.
     */
    public boolean hasCharacter(@NotNull UserSnowflake user, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM players_fursona WHERE owner_id = ?")) {
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
     * Deletes a character from the database based on the provided builder and data source.
     *
     * @param builder     The FurSonaBuilder object containing the character's attributes.
     * @param dataSource  The data source used to establish a connection to the database.
     * @since version 1.0
     */
    public void deleteCharacter(@NotNull FurSonaBuilder builder, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("DELETE FROM players_fursona WHERE owner_id = ?")) {
                statement.setString(1, builder.getOwnerId().getId());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * Retrieves a FurSona character from the database based on the owner's user ID.
     *
     * @param owner The UserSnowflake object representing the owner of the character
     * @param dataSource The data source used to establish a connection to the database
     * @return The FurSonaBuilder object representing the retrieved character, or null if the character does not exist
     */
    public FurSonaBuilder getCharacterByOwner(@NotNull UserSnowflake owner, DataSource  dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM players_fursona WHERE owner_id = ?")) {
                statement.setString(1, owner.getId());
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        FurSonaBuilder builder = new FurSonaBuilder();
                        builder.setOwnerId(owner);
                        builder.setCharacterAge(resultSet.getInt("character_age"));
                        builder.setCharacterColor(resultSet.getInt("character_color"));
                        builder.setCharacterName(resultSet.getString("character_name"));
                        builder.setCharacterQuote(resultSet.getString("character_quote"));
                        builder.setCharacterSpecie(resultSet.getString("character_specie"));
                        builder.setCharacterPictureURL(resultSet.getString("character_picture"));
                        builder.setCharacterGender(resultSet.getString("character_gender"));
                        builder.setCharacterRefsheetURL(resultSet.getString("character_refsheet"));
                        builder.setCharacterPronouns(resultSet.getString("character_pronouns"));
                        builder.setCharacterDescriptions(resultSet.getString("character_descriptions"));
                        builder.setCharacterExtra(new JsonParser().parse(resultSet.getString("character_extra")).getAsJsonObject());
                        builder.setCharacterExtraPictures(new JsonParser().parse(resultSet.getString("character_extra_pictures")).getAsJsonObject());
                        return builder;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }

        return null;
    }
}