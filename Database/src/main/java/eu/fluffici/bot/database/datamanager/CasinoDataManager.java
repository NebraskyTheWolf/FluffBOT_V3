/*
---------------------------------------------------------------------------------
File Name : CasinoDataManager

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 03/06/2024
Last Modified : 03/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.database.datamanager;

import eu.fluffici.bot.api.beans.game.CasinoGameBuilder;
import net.dv8tion.jda.api.entities.UserSnowflake;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CasinoDataManager {
    public void createGameSession(CasinoGameBuilder game, DataSource dataSource)  {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO casino_game (game_id, game_status, owner_id, player_score, neon_score) VALUES (?, ?, ?, ?, ?)")) {
                statement.setString(1, game.getGameId());
                statement.setString(2, game.getGameStatus().name());
                statement.setString(3, game.getUser().getId());
                statement.setInt(4, game.getPlayerScore());
                statement.setInt(5, game.getNeonScore());

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public CasinoGameBuilder getGameSession(String gameId, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM casino_game WHERE game_id = ?")) {
                statement.setString(1, gameId);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        String gameStatus = resultSet.getString("game_status");
                        String ownerId = resultSet.getString("owner_id");
                        int playerScore = resultSet.getInt("player_score");
                        int neonScore = resultSet.getInt("neon_score");

                        CasinoGameBuilder.GameStatus status = CasinoGameBuilder.GameStatus.valueOf(gameStatus);

                        return new CasinoGameBuilder(gameId, status, UserSnowflake.fromId(ownerId), playerScore, neonScore);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateGameSession(CasinoGameBuilder game, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("UPDATE casino_game SET game_status = ?, owner_id = ?, player_score = ?, neon_score = ? WHERE game_id = ?")) {
                statement.setString(1, game.getGameStatus().name());
                statement.setString(2, game.getUser().getId());
                statement.setInt(3, game.getPlayerScore());
                statement.setInt(4, game.getNeonScore());
                statement.setString(5, game.getGameId());

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteGameSession(String gameId, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("DELETE FROM casino_game WHERE game_id = ?")) {
                statement.setString(1, gameId);

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<CasinoGameBuilder> getAllGameSessions(DataSource dataSource) {
        List<CasinoGameBuilder> gameSessions = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM casino_game")) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        String gameId = resultSet.getString("game_id");
                        String gameStatus = resultSet.getString("game_status");
                        String ownerId = resultSet.getString("owner_id");
                        int playerScore = resultSet.getInt("player_score");
                        int neonScore = resultSet.getInt("neon_score");

                        CasinoGameBuilder.GameStatus status = CasinoGameBuilder.GameStatus.valueOf(gameStatus);

                        CasinoGameBuilder game = new CasinoGameBuilder(gameId, status, UserSnowflake.fromId(ownerId), playerScore, neonScore);
                        gameSessions.add(game);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return gameSessions;
    }

    public void updateGameStatus(String gameId, CasinoGameBuilder.GameStatus gameStatus, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("UPDATE casino_game SET game_status = ? WHERE game_id = ?")) {
                statement.setString(1, gameStatus.name());
                statement.setString(2, gameId);

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateScores(String gameId, int playerScore, int neonScore, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("UPDATE casino_game SET player_score = ?, neon_score = ? WHERE game_id = ?")) {
                statement.setInt(1, playerScore);
                statement.setInt(2, neonScore);
                statement.setString(3, gameId);

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean hasGame(UserSnowflake user, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT id FROM casino_game WHERE owner_id = ?")) {
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

    public CasinoGameBuilder fetchGame(UserSnowflake user, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM casino_game WHERE owner_id = ? AND game_status = ?")) {
                statement.setString(1, user.getId());
                statement.setString(2, CasinoGameBuilder.GameStatus.ONGOING.name());

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        String gameId = resultSet.getString("game_id");
                        String gameStatus = resultSet.getString("game_status");
                        String ownerId = resultSet.getString("owner_id");
                        int playerScore = resultSet.getInt("player_score");
                        int neonScore = resultSet.getInt("neon_score");

                        CasinoGameBuilder.GameStatus status = CasinoGameBuilder.GameStatus.valueOf(gameStatus);

                        return new CasinoGameBuilder(gameId, status, UserSnowflake.fromId(ownerId), playerScore, neonScore);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}