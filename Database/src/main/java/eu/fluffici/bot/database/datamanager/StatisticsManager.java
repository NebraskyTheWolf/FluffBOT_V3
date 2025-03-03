package eu.fluffici.bot.database.datamanager;

/*
---------------------------------------------------------------------------------
File Name : StatisticsManager.java

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

import eu.fluffici.bot.api.beans.players.LeaderboardBuilder;
import eu.fluffici.bot.api.beans.players.PlayerVoiceActivity;
import eu.fluffici.bot.api.beans.statistics.GuildEngagement;
import eu.fluffici.bot.api.beans.statistics.ReportNotification;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.internal.utils.tuple.Pair;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StatisticsManager {

    public void insertVoiceActivity(UserSnowflake user, int amount, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO players_voice_statistics (user_id, amount, created_at) VALUES (?, ?, ?)")) {
                statement.setString(1, user.getId());
                statement.setInt(2, amount);
                statement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<PlayerVoiceActivity> getVoiceActivity(DataSource dataSource) {
        List<PlayerVoiceActivity> voiceActivities = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT user_id, amount, created_at FROM players_voice_statistics")) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        voiceActivities.add(new PlayerVoiceActivity(
                                resultSet.getString("user_id"),
                                Math.abs(resultSet.getInt("amount") / 3600),
                                resultSet.getTimestamp("created_at")
                        ));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return voiceActivities;
    }


    public void insertGuildEngagement(UserSnowflake user, GuildEngagement.Action action, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO guild_engagement (user_id, guild_action, created_at) VALUES (?, ?, ?)")) {
                statement.setString(1, user.getId());
                statement.setString(2, action.name());
                statement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<GuildEngagement> getGuildEngagements(DataSource dataSource) {
        List<GuildEngagement> voiceActivities = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT user_id, guild_action, created_at FROM guild_engagement")) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        voiceActivities.add(new GuildEngagement(
                                resultSet.getString("user_id"),
                                GuildEngagement.Action.valueOf(resultSet.getString("guild_action")),
                                resultSet.getTimestamp("created_at")
                        ));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return voiceActivities;
    }

    public void insertGuildEngagementReport(ReportNotification.ReportType reportType, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO reports_notification (report_type, created_at) VALUES (?, ?)")) {
                statement.setString(1, reportType.name());
                statement.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Pair<Boolean, ReportNotification> hasActiveReport(ReportNotification.ReportType reportType, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = switch (reportType) {
                case WEEKLY -> "SELECT id, report_type, created_at FROM reports_notification WHERE created_at >= DATE_SUB(NOW(), INTERVAL 1 WEEK) AND report_type = ?";
                case MONTHLY, MONTHLY_MESSAGES, PAYCHECK -> "SELECT id, report_type, created_at FROM reports_notification WHERE created_at >= DATE_SUB(NOW(), INTERVAL 1 MONTH) AND report_type = ?";
            };

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, reportType.name());

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return Pair.of(true, new ReportNotification(
                                ReportNotification.ReportType.valueOf(resultSet.getString("report_type")),
                                resultSet.getTimestamp("created_at")
                        ));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Pair.of(false, null);
    }

    public List<LeaderboardBuilder> getTopUsersByLevel(int limit, DataSource dataSource) throws Exception {
        List<LeaderboardBuilder> users = new ArrayList<>();

        String query = "SELECT user_id, level FROM players ORDER BY level DESC LIMIT " + limit;

        try (Statement stmt = dataSource.getConnection().createStatement(); ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String userId = rs.getString("user_id");
                int level = rs.getInt("level");

                users.add(LeaderboardBuilder
                        .builder()
                        .userId(userId)
                        .name("level")
                        .score(level)
                        .build()
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }

    public List<LeaderboardBuilder> getTopUsersByStats(int limit, String statistics, DataSource dataSource) throws Exception {
        List<LeaderboardBuilder> users = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {

            try (PreparedStatement statement = connection.prepareStatement("SELECT user_id, name, score FROM players_statistics WHERE name = ? ORDER BY score DESC LIMIT " + limit)) {
                statement.setString(1, statistics);

                try (ResultSet resultset = statement.executeQuery()) {
                    while (resultset.next()) {
                        String userId = resultset.getString("user_id");
                        String name = resultset.getString("name");
                        int score = resultset.getInt("score");

                        users.add(LeaderboardBuilder
                                .builder()
                                .userId(userId)
                                .name(name)
                                .score(score)
                                .build()
                        );
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }

    public void createUserStatistics(UserSnowflake user, String statistic, DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO players_statistics (user_id, name, score) VALUES (?, ?, ?)")) {
                statement.setString(1, user.getId());
                statement.setString(2, statistic);
                statement.setInt(3, 0);

                statement.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void incrementUserStatistics(UserSnowflake user, String statistic, int value, DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection()) {

            try (PreparedStatement statement = connection.prepareStatement("UPDATE players_statistics SET score = score + ? WHERE user_id = ? AND name = ?")) {
                statement.setInt(1, value);
                statement.setString(2, user.getId());
                statement.setString(3, statistic);

                statement.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setUserStatistics(UserSnowflake user, String statistic, long value, DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("UPDATE players_statistics SET score = ? WHERE user_id = ? AND name = ?")) {
                statement.setLong(1, value);
                statement.setString(2, user.getId());
                statement.setString(3, statistic);

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public LeaderboardBuilder getPlayerStatistics(UserSnowflake user, String statistic, DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT name, score FROM players_statistics WHERE user_id = ? AND name = ?")) {
                statement.setString(1, user.getId());
                statement.setString(2, statistic);

                try (ResultSet resultset = statement.executeQuery()) {
                    if (resultset.next()) {
                        String name = resultset.getString("name");
                        int score = resultset.getInt("score");

                        return LeaderboardBuilder
                                .builder()
                                .name(name)
                                .score(score)
                                .build();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return LeaderboardBuilder
                .builder()
                .name(statistic)
                .score(0)
                .build();
    }

    public boolean hasStatistics(UserSnowflake user, String statistic, DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT name, score FROM players_statistics WHERE user_id = ? AND name = ?")) {
                statement.setString(1, user.getId());
                statement.setString(2, statistic);

                try (ResultSet resultset = statement.executeQuery()) {
                    return resultset.next();

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}
