/*
---------------------------------------------------------------------------------
File Name : VerificationManager

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 14/06/2024
Last Modified : 14/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.database.datamanager;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import eu.fluffici.bot.api.beans.verification.VerificationBuilder;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.jetbrains.annotations.NotNull;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("All")
public class VerificationManager {

    /**
     * Inserts a new verification record into the database.
     *
     * @param verification The verification object containing the necessary information for the record.
     * @param dataSource  The datasource to connect to the database.
     */
    public void createVerificationRecord(@NotNull VerificationBuilder verification, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "insert into verifications (user_id, verification_code, answers)";
            sql += " values (?, ?, ?)";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, verification.getUserId());
                statement.setString(2, verification.getVerificationCode());
                statement.setString(3, verification.getAnswers().toString());

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
    public VerificationBuilder getVerificationRecord(String challengeCode, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "select * from verifications where verification_code = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, challengeCode);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        ;
                        String answers = resultSet.getString("answers");
                        return new VerificationBuilder(
                                resultSet.getString("user_id"),
                                resultSet.getString("status"),
                                resultSet.getString("verified_by"),
                                challengeCode,
                                new Gson().fromJson(resultSet.getString("answers"), JsonObject.class)
                        );
                    }
                }
            }
        }  catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
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
            String sql = "select * from verifications where user_id = ? AND status = 'PENDING'";
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
    public void updateVerificationRecord(@NotNull VerificationBuilder verification, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "UPDATE verifications SET verified_by = ?, status = ?, updated_at = ? where verification_code = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, verification.getVerifiedBy());
                statement.setString(2, verification.getStatus());
                statement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
                statement.setString(4, verification.getVerificationCode());

                statement.executeUpdate();
            }

            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO verification_logs (verification_code, issuer_id) VALUES (?, ?)")) {
                statement.setString(1, verification.getVerificationCode());
                statement.setString(2, verification.getVerifiedBy());

                statement.executeUpdate();
            }
        }  catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Fetches all reminders from the verification_reminder table in the database.
     *
     * @param dataSource The DataSource object used to establish a connection to the database.
     * @return A HashMap<UserSnowflake, Long> containing the user IDs as keys and the number of hours until expiration as values.
     */
    public HashMap<UserSnowflake, Long> fetchAllReminders(DataSource dataSource) {
        HashMap<UserSnowflake, Long> expirationMap = new HashMap<>();

        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT user_id, expire_at FROM verification_reminder WHERE expire_at > NOW() AND is_notified = 0")) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    LocalDateTime now = LocalDateTime.now();

                    while (resultSet.next()) {
                        UserSnowflake user = UserSnowflake.fromId(resultSet.getString("user_id"));
                        Timestamp expireAt = resultSet.getTimestamp("expire_at");
                        LocalDateTime expireTime = expireAt.toLocalDateTime();

                        long hoursUntilExpire = Duration.between(now, expireTime).toHours();
                        expirationMap.put(user, hoursUntilExpire);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return expirationMap;
    }


    /**
     * Adds a new reminder to the verification_reminder table in the database.
     *
     * @param dataSource The DataSource object used to establish a connection to the database.
     * @param userId The ID of the user for whom the reminder is being added.
     * @param expireAt The expiration time for the reminder.
     * @return true if the reminder was added successfully, false otherwise.
     */
    public boolean addReminder(UserSnowflake user, DataSource dataSource) {
        String sql = "INSERT INTO verification_reminder (user_id, is_locked, is_notified, created_at, expire_at) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            LocalDateTime localDateTime = new Timestamp(System.currentTimeMillis()).toLocalDateTime().plusDays(4);

            preparedStatement.setString(1, user.getId());
            preparedStatement.setBoolean(2, false);
            preparedStatement.setBoolean(3, false);
            preparedStatement.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            preparedStatement.setTimestamp(5, Timestamp.valueOf(localDateTime));

            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Fetches all expired reminders from the verification_reminder table in the database.
     *
     * @param dataSource The DataSource object used to establish a connection to the database.
     * @return A HashMap<UserSnowflake, Timestamp> containing the user IDs as keys and the expiration timestamps as values.
     */
    public HashMap<UserSnowflake, Timestamp> fetchExpiredReminders(DataSource dataSource) {
        HashMap<UserSnowflake, Timestamp> expiredMap = new HashMap<>();

        try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("SELECT user_id, expire_at FROM verification_reminder WHERE expire_at <= NOW() AND is_locked = 0");
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                UserSnowflake user = UserSnowflake.fromId(resultSet.getString("user_id"));
                Timestamp expireAt = resultSet.getTimestamp("expire_at");
                expiredMap.put(user, expireAt);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return expiredMap;
    }

    public HashMap<UserSnowflake, Timestamp> fetchExpiredRemindersLocked(DataSource dataSource) {
        HashMap<UserSnowflake, Timestamp> expiredMap = new HashMap<>();

        try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("SELECT user_id, expire_at FROM verification_reminder WHERE expire_at <= NOW() AND is_locked = 1");
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                UserSnowflake user = UserSnowflake.fromId(resultSet.getString("user_id"));
                Timestamp expireAt = resultSet.getTimestamp("expire_at");
                expiredMap.put(user, expireAt);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return expiredMap;
    }

    /**
     * Checks if the given user ID has an expired reminder.
     *
     * @param dataSource The DataSource object used to establish a connection to the database.
     * @param userId The ID of the user to check.
     * @return true if the user ID has an expired reminder, false otherwise.
     */
    public boolean hasExpiredReminder(UserSnowflake user, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM verification_reminder WHERE user_id = ? AND expire_at <= NOW()")) {
            preparedStatement.setString(1, user.getId());

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Locks a reminder for a user by updating the 'is_locked' flag in the verification_reminder table in the database.
     *
     * @param user       The UserSnowflake object representing the user for whom the reminder is being locked.
     * @param dataSource The DataSource object used to establish a connection to the database.
     */
    public void lockReminder(UserSnowflake user, DataSource dataSource)  {
        String sql = "UPDATE verification_reminder SET is_locked = 1 WHERE user_id = ?";
        try (Connection connection = dataSource.getConnection();) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, user.getId());
                preparedStatement.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Notifies a user reminder by updating the 'is_notified' flag in the verification_reminder table.
     *
     * @param user The UserSnowflake object representing the user for whom the reminder is being notified.
     * @param dataSource The DataSource object used to establish a connection to the database.
     */
    public void notifyReminder(UserSnowflake user, DataSource dataSource)  {
        String sql = "UPDATE verification_reminder SET is_notified = 1 WHERE user_id = ?";
        try (Connection connection = dataSource.getConnection();) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, user.getId());
                preparedStatement.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes a reminder for a user from the verification_reminder table in the database.
     *
     * @param user       The UserSnowflake object representing the user for whom the reminder is being deleted.
     * @param dataSource The DataSource object used to establish a connection to the database.
     */
    public void deleteReminder(UserSnowflake user, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM verification_reminder WHERE user_id = ?")) {
            preparedStatement.setString(1, user.getId());
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}