/*
---------------------------------------------------------------------------------
File Name : TelegramManager

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 20/06/2024
Last Modified : 20/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.database.datamanager;

import eu.fluffici.bot.api.beans.telegram.TelegramVerification;
import eu.fluffici.bot.api.beans.telegram.VerificationStatus;
import org.jetbrains.annotations.NotNull;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TelegramManager {

    public void createTelegramVerification(@NotNull TelegramVerification verification, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO telegram_verification (user_id, verification_code, username) VALUES (?, ?, ?)")) {
                statement.setLong(1, verification.getUserId());
                statement.setString(2, verification.getVerificationCode());
                statement.setString(3, verification.getUsername());

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public TelegramVerification getTelegramVerification(long userId, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM telegram_verification WHERE user_id = ?")) {
                statement.setLong(1, userId);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        String username = resultSet.getString("username");
                        String verificationCode = resultSet.getString("verification_code");

                        return new TelegramVerification(userId, username, verificationCode, VerificationStatus.valueOf(
                                resultSet.getString("status")
                        ), resultSet.getTimestamp("created_at"), resultSet.getTimestamp("updated_at"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean hasVerificationCode(long userId, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM telegram_verification WHERE user_id = ? AND status = 'PENDING'")) {
                statement.setLong(1, userId);

                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean isVerified(long userId, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM telegram_verified_users WHERE user_id = ?")) {
                statement.setLong(1, userId);

                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next();
                }
            }
        }  catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public void unlinkAccount(long userId, DataSource dataSource) {
       try (Connection connection = dataSource.getConnection()) {
           try (PreparedStatement statement = connection.prepareStatement("DELETE FROM telegram_verified_users WHERE user_id = ?")) {
               statement.setLong(1, userId);
               statement.executeUpdate();
           }
       } catch (SQLException e) {
           e.printStackTrace();
       }
    }

    public void addMessage(long messageId, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO telegram_messages (message_id) VALUES (?)")) {
                statement.setLong(1, messageId);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean hasMessage(long messageId, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM telegram_messages WHERE message_id = ?")) {
                statement.setLong(1, messageId);

                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
}