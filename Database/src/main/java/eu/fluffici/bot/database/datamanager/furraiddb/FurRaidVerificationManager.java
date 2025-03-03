/*
---------------------------------------------------------------------------------
File Name : FurRaidVerificationManager

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 27/06/2024
Last Modified : 27/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.database.datamanager.furraiddb;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import eu.fluffici.bot.api.beans.furraid.verification.Verification;
import eu.fluffici.bot.api.beans.furraid.verification.VerificationParser;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.jetbrains.annotations.NotNull;

import javax.sql.DataSource;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FurRaidVerificationManager {

    /**
     * Inserts a new verification record into the database.
     *
     * @param verification The verification object containing the necessary information for the record.
     * @param dataSource  The datasource to connect to the database.
     */
    public void createVerificationRecord(@NotNull Verification verification, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "insert into furraid_verifications (guild_id, user_id, answers, verification_code, message_id) ";
            sql += " values (?, ?, ?, ?, ?)";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, verification.getGuildId());
                statement.setString(2, verification.getUserId());
                statement.setString(3, new Gson().toJson(verification.getAnswers()));
                statement.setString(4, verification.getVerificationCode());
                statement.setString(5, verification.getMessageId());

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves a verification record from the database based on the provided challenge code.
     *
     * @param challengeCode The challenge code used to retrieve the verification record.
     * @param dataSource    The datasource used to establish a database connection.
     * @return The VerificationBuilder object representing the retrieved verification record. Returns null if no record is found.
     */
    public Verification getVerificationRecord(String guildId, String challengeCode, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "select * from furraid_verifications where verification_code = ? and guild_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, challengeCode);
                statement.setString(2, guildId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        Type listType = new TypeToken<ArrayList<VerificationParser.Question>>(){}.getType();
                        ArrayList<VerificationParser.Question> result = new Gson().fromJson(resultSet.getString("answers"), listType);

                        return new Verification(
                                resultSet.getInt("id"),
                                resultSet.getString("guild_id"),
                                resultSet.getString("user_id"),
                                resultSet.getString("status"),
                                resultSet.getString("verified_by"),
                                challengeCode,
                                resultSet.getString("message_id"),
                                result,
                                resultSet.getTimestamp("created_at"),
                                resultSet.getTimestamp("updated_at")
                        );
                    }
                }
            }
        }  catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Verification getVerificationRecord(String guildId, UserSnowflake user, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "select * from furraid_verifications where user_id = ? and guild_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, user.getId());
                statement.setString(2, guildId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        Type listType = new TypeToken<ArrayList<VerificationParser.Question>>(){}.getType();
                        ArrayList<VerificationParser.Question> result = new Gson().fromJson(resultSet.getString("answers"), listType);

                        return new Verification(
                                resultSet.getInt("id"),
                                resultSet.getString("guild_id"),
                                resultSet.getString("user_id"),
                                resultSet.getString("status"),
                                resultSet.getString("verified_by"),
                                resultSet.getString("verification_code"),
                                resultSet.getString("message_id"),
                                result,
                                resultSet.getTimestamp("created_at"),
                                resultSet.getTimestamp("updated_at")
                        );
                    }
                }
            }
        }  catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }


    public Verification getVerificationRecord(String guildId, int id, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "select * from furraid_verifications where id = ? and guild_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, id);
                statement.setString(2, guildId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        Type listType = new TypeToken<ArrayList<VerificationParser.Question>>(){}.getType();
                        ArrayList<VerificationParser.Question> result = new Gson().fromJson(resultSet.getString("answers"), listType);

                        return new Verification(
                                resultSet.getInt("id"),
                                resultSet.getString("guild_id"),
                                resultSet.getString("user_id"),
                                resultSet.getString("status"),
                                resultSet.getString("verified_by"),
                                resultSet.getString("verification_code"),
                                resultSet.getString("message_id"),
                                result,
                                resultSet.getTimestamp("created_at"),
                                resultSet.getTimestamp("updated_at")
                        );
                    }
                }
            }
        }  catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<Verification> getVerificationRecords(String guildId, DataSource dataSource) {
        List<Verification> verifications = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {
            String sql = "select * from furraid_verifications where guild_id = ? ORDER BY created_at DESC, status = 'PENDING' DESC LIMIT 10";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, guildId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        Type listType = new TypeToken<ArrayList<VerificationParser.Question>>(){}.getType();
                        ArrayList<VerificationParser.Question> result = new Gson().fromJson(resultSet.getString("answers"), listType);

                        verifications.add(new Verification(
                                resultSet.getInt("id"),
                                resultSet.getString("guild_id"),
                                resultSet.getString("user_id"),
                                resultSet.getString("status"),
                                resultSet.getString("verified_by"),
                                resultSet.getString("verification_code"),
                                resultSet.getString("message_id"),
                                result,
                                resultSet.getTimestamp("created_at"),
                                resultSet.getTimestamp("updated_at")
                        ));
                    }
                }
            }
        }  catch (SQLException e) {
            e.printStackTrace();
        }

        return verifications;
    }

    public List<Verification> getAllVerificationRecords(String guildId, DataSource dataSource) {
        List<Verification> verifications = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {
            String sql = "select * from furraid_verifications where guild_id = ? ORDER BY created_at";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, guildId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        Type listType = new TypeToken<ArrayList<VerificationParser.Question>>(){}.getType();
                        ArrayList<VerificationParser.Question> result = new Gson().fromJson(resultSet.getString("answers"), listType);

                        verifications.add(new Verification(
                                resultSet.getInt("id"),
                                resultSet.getString("guild_id"),
                                resultSet.getString("user_id"),
                                resultSet.getString("status"),
                                resultSet.getString("verified_by"),
                                resultSet.getString("verification_code"),
                                resultSet.getString("message_id"),
                                result,
                                resultSet.getTimestamp("created_at"),
                                resultSet.getTimestamp("updated_at")
                        ));
                    }
                }
            }
        }  catch (SQLException e) {
            e.printStackTrace();
        }

        return verifications;
    }

    /**
     * Checks if the given user has a verification record in the database.
     *
     * @param user the UserSnowflake object representing the user to check for verification.
     * @param dataSource the DataSource object used to establish a connection to the database.
     * @return true if the user has a verification record in the database, false otherwise.
     */
    public boolean hasVerification(@NotNull UserSnowflake user, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "select * from furraid_verifications where user_id = ? AND status = 'PENDING'";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, user.getId());
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next();
                }
            }
        }  catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Updates a verification record in the database.
     *
     * @param verification The VerificationBuilder object representing the record to be updated.
     * @param dataSource   The DataSource used to establish a database connection.
     * @throws SQLException If an error occurs while executing the SQL query.
     */
    public void updateVerificationRecord(@NotNull Verification verification, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "UPDATE furraid_verifications SET verified_by = ?, status = ?, updated_at = ? where verification_code = ? and guild_id = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, verification.getVerifiedBy());
                statement.setString(2, verification.getStatus());
                statement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
                statement.setString(4, verification.getVerificationCode());
                statement.setString(5, verification.getGuildId());

                statement.executeUpdate();
            }
        }  catch (SQLException e) {
            e.printStackTrace();
        }
    }
}