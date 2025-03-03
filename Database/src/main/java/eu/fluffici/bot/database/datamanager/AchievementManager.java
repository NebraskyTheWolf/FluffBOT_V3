package eu.fluffici.bot.database.datamanager;

/*
---------------------------------------------------------------------------------
File Name : AchievementManager.java

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


import eu.fluffici.bot.api.beans.achievements.AchievementBean;
import eu.fluffici.bot.api.beans.achievements.AchievementCategoryBean;
import eu.fluffici.bot.api.beans.achievements.AchievementProgressBean;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
@SuppressWarnings("All")
public class AchievementManager {
    public AchievementCategoryBean getAchievementCategory(int categoryId, DataSource dataSource) throws Exception {
        AchievementCategoryBean achievementCategory = null;

        try (Connection connection = dataSource.getConnection()) {

            String sql = "select category_name, category_description, icon, parent_id from achievement_categories where category_id = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, categoryId);

                try (ResultSet resultset = statement.executeQuery()) {
                    if(resultset.next()) {
                        // There's a result
                        String categoryName = resultset.getString("category_name");
                        String categoryDescription = resultset.getString("category_description");
                        String icon = resultset.getString("icon");
                        int parentId = resultset.getInt("parent_id");
                        achievementCategory = new AchievementCategoryBean(categoryId, categoryName, categoryDescription, icon, parentId);
                    }
                }
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }

        return achievementCategory;
    }

    // Get the categories
    public List<AchievementCategoryBean> getAchievementCategories(DataSource dataSource) throws Exception
    {
        List<AchievementCategoryBean> achievementCategories = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {
            String sql = "select category_id, category_name, category_description, icon, parent_id from achievement_categories";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                try (ResultSet resultset = statement.executeQuery()) {
                    while (resultset.next())
                    {
                        // There's a result
                        int categoryId = resultset.getInt("category_id");
                        String categoryName = resultset.getString("category_name");
                        String categoryDescription = resultset.getString("category_description");
                        String icon = resultset.getString("icon");
                        int parentId = resultset.getInt("parent_id");
                        achievementCategories.add(new AchievementCategoryBean(categoryId, categoryName, categoryDescription, icon, parentId));
                    }
                }
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }

        return achievementCategories;
    }

    // Get the achievement by ID
    public AchievementBean getAchievement(int achievementId, DataSource dataSource) throws Exception
    {
        try (Connection connection = dataSource.getConnection()) {
            AchievementBean achievement = null;

            ;

            // Query construction
            String sql = "select achievement_name, achievement_iconURL, achievement_description, progress_target, parent_id from achievements where achievement_id = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, achievementId);

                try (ResultSet resultset = statement.executeQuery()) {
                    if(resultset.next()) {
                        String achievementName = resultset.getString("achievement_name");
                        String achievementIconURL = resultset.getString("achievement_iconURL");
                        String achievementDescription = resultset.getString("achievement_description");
                        int progressTarget = resultset.getInt("progress_target");
                        int categoryId = resultset.getInt("parent_id");
                        achievement = new AchievementBean(achievementId, achievementName, achievementDescription, achievementIconURL, progressTarget, categoryId);
                    }
                }
            }

            return achievement;
        } catch(SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // Get the achievements
    public List<AchievementBean> getAchievements(DataSource dataSource) throws Exception {
        List<AchievementBean> achievements = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {
            String sql = "select achievement_id, achievement_iconURL, achievement_name, achievement_description, progress_target, parent_id from achievements";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                try (ResultSet resultset = statement.executeQuery()) {
                    while (resultset.next())
                    {
                        // There's a result
                        int achievementId = resultset.getInt("achievement_id");
                        String achievementName = resultset.getString("achievement_name");
                        String achievementIconURL = resultset.getString("achievement_iconURL");
                        String achievementDescription = resultset.getString("achievement_description");
                        int progressTarget = resultset.getInt("progress_target");
                        int categoryId = resultset.getInt("parent_id");
                        achievements.add(new AchievementBean(achievementId, achievementName, achievementDescription, achievementIconURL, progressTarget, categoryId));
                    }
                }
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }

        return achievements;
    }

    public AchievementProgressBean getAchievementProgress(String userId, int achievementId, DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "select user_id, progress_id, achievement_id, progress, start_date, unlock_date from achievement_progresses where user_id = ? and achievement_id = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, userId);
                statement.setInt(2, achievementId);

                try (ResultSet resultset = statement.executeQuery()) {
                    if (resultset.next()) {
                        // There's a result
                        String playerUuid = resultset.getString("user_id");
                        long progressId = resultset.getLong("progress_id");
                        int achievementId2 = resultset.getInt("achievement_id");
                        int achievementProgress = resultset.getInt("progress");
                        Timestamp startDate = resultset.getTimestamp("start_date");

                        Timestamp unlockDate;

                        try {
                            unlockDate = resultset.getTimestamp("unlock_date");
                        } catch (Exception dateException) {
                            unlockDate = null;
                        }

                        return new AchievementProgressBean(playerUuid, progressId, achievementId2, achievementProgress, startDate, unlockDate);
                    } else {
                        AchievementProgressBean achievementProgressBean = new AchievementProgressBean(userId, 0, 0, achievementId, null, null);
                        this.createAchievementProgress(userId, achievementProgressBean, dataSource);
                        achievementProgressBean = this.getAchievementProgress(userId, achievementId, dataSource);
                        return achievementProgressBean;
                    }
                }
            }
        }
        catch(SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // Get achievement progresses by UUID
    public List<AchievementProgressBean> getAchievementProgresses(String player, DataSource dataSource) throws Exception {
        List<AchievementProgressBean> achievementProgresses = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {
            String sql = "select user_id, progress_id, achievement_id, progress, start_date, unlock_date from achievement_progresses where user_id = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, player);

                try (ResultSet resultset = statement.executeQuery()) {
                    while (resultset.next()) {
                        // There's a result
                        String playerUuid = resultset.getString("user_id");
                        long progressId = resultset.getLong("progress_id");
                        int achievementId = resultset.getInt("achievement_id");
                        int achievementProgress = resultset.getInt("progress");
                        Timestamp startDate = resultset.getTimestamp("start_date");

                        Timestamp unlockDate;

                        try {
                            unlockDate = resultset.getTimestamp("unlock_date");
                        } catch (Exception dateException) {
                            unlockDate = null;
                        }

                        achievementProgresses.add(new AchievementProgressBean(playerUuid, progressId, achievementId, achievementProgress, startDate, unlockDate));
                    }
                }
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }

        return achievementProgresses;
    }

    // Update the achievement progress data
    public void updateAchievementProgress(AchievementProgressBean progress, DataSource dataSource) throws Exception
    {
        // Update the players data
        try (Connection connection = dataSource.getConnection()) {
            Timestamp unlockDate = progress.getUnlockDate();
            String unlockDateString = null;

            if(unlockDate != null)
                unlockDateString = unlockDate.toString();

            // Query construction
            String sql = "update achievement_progresses set progress = ?, start_date = ?, unlock_date = ? where progress_id = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, progress.getProgress());
                statement.setString(2, progress.getStartDate().toString());
                statement.setString(3, unlockDateString);
                statement.setLong(4, progress.getProgressId());

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int createAchievementProgress(String userId, AchievementProgressBean progress, DataSource dataSource) throws Exception {
        int newProgressId = -1;
        String checkSql = "SELECT progress_id FROM achievement_progresses WHERE achievement_id = ? AND user_id = ?";
        String insertSql = "INSERT INTO achievement_progresses (achievement_id, progress, start_date, unlock_date, user_id) VALUES (?, ?, NOW(), ?, ?)";

        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement checkStatement = connection.prepareStatement(checkSql)) {
                checkStatement.setInt(1, progress.getAchievementId());
                checkStatement.setString(2, userId);

                try (ResultSet checkResult = checkStatement.executeQuery()) {
                    if (checkResult.next()) {
                        this.updateAchievementProgress(progress, dataSource);
                        newProgressId = checkResult.getInt(1);
                    } else {
                        Timestamp unlockDate = progress.getUnlockDate();
                        String unlockDateString = null;

                        if(unlockDate != null) {
                            unlockDateString = unlockDate.toString();
                        }

                        try (PreparedStatement insertStatement = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                            insertStatement.setInt(1, progress.getAchievementId());
                            insertStatement.setInt(2, progress.getProgress());
                            insertStatement.setString(3, unlockDateString);
                            insertStatement.setString(4, userId);

                            // Execute the query
                            insertStatement.executeUpdate();

                            try (ResultSet generatedKeys = insertStatement.getGeneratedKeys()) {
                                if (generatedKeys.next()) {
                                    newProgressId = generatedKeys.getInt(1);
                                }
                            }
                        }
                    }
                }
            }
        }

        return newProgressId;
    }
}