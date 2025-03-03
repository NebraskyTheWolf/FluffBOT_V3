package eu.fluffici.bot.database.datamanager;

/*
---------------------------------------------------------------------------------
File Name : UserData.java

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

import eu.fluffici.bot.api.beans.clans.ClanBean;
import eu.fluffici.bot.api.beans.clans.ClanMembersBean;
import eu.fluffici.bot.api.beans.level.LevelReward;
import eu.fluffici.bot.api.beans.players.*;
import eu.fluffici.bot.api.beans.shop.ItemDescriptionBean;
import eu.fluffici.bot.api.beans.staff.StaffPaycheck;
import eu.fluffici.bot.api.hooks.PlayerBean;
import eu.fluffici.bot.database.GameServiceManager;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@SuppressWarnings("All")
public class UserData {

    private GameServiceManager instance;

    public UserData(GameServiceManager instance) {
        this.instance = instance;
    }

    public PlayerBean getPlayer(String userId, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "select user_id, bound_to, has_nickname, nickname, experience, level, coins, tokens, events, upvote, karma, booster, seed, health_percentage, hunger_percentage, mana_percentage, inventory_size, migration_id, legacy_message_count from players where user_id = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, userId);

                try (ResultSet resultset = statement.executeQuery()) {
                    if (resultset.next()) {
                        String boundTo = resultset.getString("bound_to");
                        boolean hasNickname = resultset.getBoolean("has_nickname");
                        String nickname = resultset.getString("nickname");
                        long experience = resultset.getLong("experience");
                        long level = resultset.getLong("level");
                        long coins = resultset.getLong("coins");
                        long tokens = resultset.getLong("tokens");
                        long events = resultset.getLong("events");
                        long upvote = resultset.getLong("upvote");
                        long karma = resultset.getLong("karma");
                        long booster = resultset.getLong("booster");
                        int seed = resultset.getInt("seed");
                        double healthPercentage = resultset.getDouble("health_percentage");
                        double hungerPercentage = resultset.getInt("hunger_percentage");
                        double manaPercentage = resultset.getInt("mana_percentage");
                        int inventorySize = resultset.getInt("inventory_size");
                        long legacyMessageCount = resultset.getInt("legacy_message_count");
                        String migrationId = resultset.getString("migration_id");

                        PlayerProfile profile = this.getPlayerProfile(userId, dataSource);

                        return new PlayerBean(userId, boundTo, nickname, hasNickname, experience, level, coins, tokens, events, upvote, karma, booster, seed, profile, healthPercentage, 0, hungerPercentage, manaPercentage, inventorySize, migrationId, legacyMessageCount);
                    }
                }
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Retrieves the player profile for the given user ID from the database.
     *
     * @param userId The ID of the user for whom to retrieve the profile.
     * @param dataSource The DataSource used to establish a connection to the database.
     * @return The PlayerProfile object representing the player's profile. If no profile is found, a default profile is returned.
     * @throws Exception if there was an error during the database operation.
     */
    private PlayerProfile getPlayerProfile(String userId, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT background_id as id, background_color as color, is_selected as selected FROM player_profile WHERE user_id = ? AND is_selected = true")) {
                statement.setString(1, userId);

                try (ResultSet resultset = statement.executeQuery()) {
                    if (resultset.next()) {
                        int backgroundId = resultset.getInt("id");
                        String backgroundColor = resultset.getString("color");
                        boolean isSelected = resultset.getBoolean("selected");

                        ItemDescriptionBean itemDescriptionBean = this.instance.fetchItem(backgroundId);

                        return new PlayerProfile(backgroundId, itemDescriptionBean, backgroundColor, isSelected);
                    }
                }
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }

        return new PlayerProfile(0, null, "#FFF", true);
    }

    public boolean isDeveloper(String userId, DataSource dataSource) throws Exception {
        try (Connection  connection = dataSource.getConnection()) {
            String sql = "select user_id from developers where user_id = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, userId);

                try (ResultSet resultset = statement.executeQuery()) {
                    if (resultset.next())
                    {
                        return true;
                    }
                }
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // Update the player data
    public void updatePlayer(PlayerBean player, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "UPDATE players SET experience = ?, level = ?, coins = ?, tokens = ?, events = ?, upvote = ?, karma = ?, nickname = ?, booster = ?, has_nickname = ?, booster = ?, seed = ?, bound_to = ?, health_percentage = ?, hunger_percentage = ?, mana_percentage = ?, inventory_size = ?, legacy_message_count = ?, migration_id = ?";
            sql += " WHERE user_id = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, player.getExperience());
                statement.setLong(2, player.getLevel());
                statement.setLong(3, player.getCoins());
                statement.setLong(4, player.getTokens());
                statement.setLong(5, player.getEvents());
                statement.setLong(6, player.getUpvote());
                statement.setLong(7, player.getKarma());
                statement.setString(8, player.getNickname());
                statement.setLong(9, player.getBooster());
                statement.setBoolean(10, player.isHasNickname());
                statement.setLong(11, player.getBooster());
                statement.setLong(12, player.getSeed());
                statement.setString(13, player.getBoundTo());

                // Preventing the factor to go higher than 1.0
                statement.setDouble(14, player.getHealthPercentage());
                statement.setDouble(15, player.getHungerPercentage());
                statement.setDouble(16, player.getManaPercentage());
                // Inventory size purchasable
                statement.setDouble(17, player.getInventorySize());
                statement.setLong(18, player.getLegacyMessageCount());
                statement.setString(19, player.getMigrationId());

                statement.setString(20, player.getUserId());

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Create the player
    public void createPlayer(PlayerBean player, DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "insert into players (user_id, experience, level, coins, tokens, events, upvote, karma)";
            sql += " values (?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, player.getUserId());
                statement.setLong(2, player.getExperience());
                statement.setLong(3, player.getLevel());
                statement.setLong(4, player.getCoins());
                statement.setLong(5, player.getTokens());
                statement.setLong(6, player.getEvents());
                statement.setLong(7, player.getUpvote());
                statement.setLong(8, player.getKarma());

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void incrementMessage(Message player, DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "insert into `messages` (user_id, message_id) values (?, ?)";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, player.getUserId());
                statement.setString(2, player.getMessageId());

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Message> sumById(String userId, DataSource dataSource) throws Exception {
        List<Message> messages = new CopyOnWriteArrayList<>();

        try (Connection connection = dataSource.getConnection()) {
           String sql = "select user_id, message_id, created_at from messages where user_id = ?";

           try (PreparedStatement statement = connection.prepareStatement(sql)) {
               statement.setString(1, userId);

               try (ResultSet resultset = statement.executeQuery()) {
                   while (resultset.next()) {
                       String messageId = resultset.getString("message_id");
                       Timestamp createdAt = resultset.getTimestamp("created_at");

                       messages.add(new Message(userId, messageId, createdAt));
                   }
               }
           }
       } catch (SQLException e) {
           e.printStackTrace();
       }

        return messages;
    }

    public List<Message> sumAll(DataSource dataSource) {
        Set<String> dup = new HashSet<>();
        List<Message> messages = new CopyOnWriteArrayList<>();

        try (Connection connection = dataSource.getConnection()) {
            String sql = "select user_id, message_id, created_at from messages";

            try (PreparedStatement  statement = connection.prepareStatement(sql)) {
                try (ResultSet resultset = statement.executeQuery()) {
                    while (resultset.next()) {
                        String userId = resultset.getString("user_id");
                        String messageId = resultset.getString("message_id");
                        Timestamp createdAt = resultset.getTimestamp("created_at");

                        if (dup.add(messageId)) {
                            messages.add(new Message(userId, messageId, createdAt));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Remove all duplicates.
        return messages;
    }

    public void deletePlayer(String player, DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "delete from players where user_id = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, player);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createBirthdate(BirthdayBean birthday, DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "insert into players_birthday (user_id, month, day, year)";
            sql += " values (?, ?, ?, ?)";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, birthday.getUserId());
                statement.setInt(2, birthday.getMonth());
                statement.setInt(3, birthday.getDay());
                statement.setInt(4, birthday.getYear());

                statement.executeUpdate();
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }


    public void updateBirthdate(BirthdayBean player, DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "update players_birthday set month = ?, day = ?, year = ?, rewarded_date = ?, last_notification = ? where user_id = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, player.getMonth());
                statement.setInt(2, player.getDay());
                statement.setInt(3, player.getYear());
                statement.setTimestamp(4, player.getLastRewardAt());
                statement.setTimestamp(5, player.getLastNotification());
                statement.setString(6, player.getUserId());

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public BirthdayBean getUserBirthdate(String userId, DataSource dataSource) throws Exception {
        Timestamp rewardedAt;
        Timestamp lastNotification;

        try (Connection connection = dataSource.getConnection()) {
            String sql = "select user_id, month, day, year, rewarded_date, last_notification from players_birthday where user_id = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, userId);

                try (ResultSet resultset = statement.executeQuery()) {
                    if (resultset.next()) {
                        String userIds = resultset.getString("user_id");
                        int month = resultset.getInt("month");
                        int day = resultset.getInt("day");
                        int year = resultset.getInt("year");

                        try {
                            rewardedAt = resultset.getTimestamp("rewarded_date");
                        } catch (Exception dateException) {
                            rewardedAt = null;
                        }

                        try {
                            lastNotification = resultset.getTimestamp("last_notification");
                        } catch (Exception dateException) {
                            lastNotification = null;
                        }

                        return new BirthdayBean(userIds, month, day, year, rewardedAt, lastNotification);
                    }
                }
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void saveUserClan(UserClanBean clan, DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "insert into players_clan (user_id, clanId)";
            sql += " values (?, ?)";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, clan.getUserId());
                statement.setString(2, clan.getClanId());

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteUserClan(ClanMembersBean clan, DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "delete from players_clan where user_id = ?";

            try (PreparedStatement  statement = connection.prepareStatement(sql)) {
                statement.setString(1, clan.getUserId());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteClan(ClanBean clan, DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "delete from clans where clan_id = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, clan.getClanId());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public UserClanBean getUserClan(String userId, DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "select user_id, clanId from players_clan where user_id = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, userId);

                try (ResultSet resultset = statement.executeQuery()) {
                    if (resultset.next()) {
                        String userIds = resultset.getString("user_id");
                        String clanId = resultset.getString("clanId");

                        return new UserClanBean(userIds, clanId);
                    }
                }
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean hasClan(String userId, DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "select user_id, clanId from players_clan where user_id = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, userId);
                try (ResultSet resultset = statement.executeQuery()) {
                    if (resultset.next())
                    {
                        return true;
                    }
                }
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public List<BirthdayBean> getAllBirthdates(DataSource dataSource) throws Exception {
        List<BirthdayBean> birthdays = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {
            String sql = "select user_id, month, day, year, rewarded_date, last_notification from players_birthday";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                try (ResultSet resultset = statement.executeQuery()) {
                    while (resultset.next()) {
                        String userId = resultset.getString("user_id");
                        int month = resultset.getInt("month");
                        int day = resultset.getInt("day");
                        int year = resultset.getInt("year");

                        Timestamp rewardedAt;

                        try {
                            rewardedAt = resultset.getTimestamp("rewarded_date");
                        } catch (Exception dateException) {
                            rewardedAt = null;
                        }

                        Timestamp lastNotification;

                        try {
                            lastNotification = resultset.getTimestamp("last_notification");
                        } catch (Exception dateException) {
                            lastNotification = null;
                        }

                        birthdays.add(new BirthdayBean(userId, month, day, year, rewardedAt, lastNotification));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return birthdays;
    }

    public Pair<Boolean, PlayerJob> getPlayerLastShift(UserSnowflake user, DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM players_pracuj WHERE user_id = ? ORDER BY created_at DESC LIMIT 1")) {
                statement.setString(1, user.getId());

                try (ResultSet resultset = statement.executeQuery()) {
                    if (resultset.next()) {
                        Timestamp lastShift = resultset.getTimestamp("created_at");

                        return Pair.of(true, new PlayerJob(lastShift));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Pair.of(false, null);
    }

    public int getSumAllPlayersShiftsToday(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT count(*) FROM players_pracuj WHERE DATE(created_at) = curdate()")) {
                try (ResultSet resultset = statement.executeQuery()) {
                    if (resultset.next()) {
                        return resultset.getInt(1);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public List<OutingSubscriber> fetchAllSubscribers(DataSource dataSource) throws Exception {
        List<OutingSubscriber> outingSubscribers = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT user_id FROM akce_subscription")) {
                try (ResultSet resultset = statement.executeQuery()) {
                    while (resultset.next()) {
                        outingSubscribers.add(new OutingSubscriber(
                                UserSnowflake.fromId(resultset.getString("user_id"))
                        ));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return outingSubscribers;
    }

    public boolean isSubscribed(UserSnowflake user, DataSource dataSource) throws Exception {
        try (Connection  connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT user_id FROM akce_subscription WHERE user_id = ?")) {
                statement.setString(1, user.getId());

                try (ResultSet resultset = statement.executeQuery()) {
                    return resultset.next();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public void createOutingSubscriber(UserSnowflake user, DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO akce_subscription (user_id) VALUES (?)")) {
                statement.setString(1, user.getId());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeOutingSubscriber(UserSnowflake user, DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("DELETE FROM akce_subscription WHERE user_id = ?")) {
                statement.setString(1, user.getId());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createPlayerLastShift(UserSnowflake user, DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO players_pracuj (user_id, created_at) VALUES (?, ?)")) {
                statement.setString(1, user.getId());
                statement.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
                statement.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean hasSubscriptionStatus(UUID eventId, String status, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT event_id FROM updated_subscription WHERE event_id = ? AND status = ?")) {
                statement.setString(1, eventId.toString());
                statement.setString(2, status);
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public void createOrUpdateStatus(UUID eventId, String status, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "INSERT INTO updated_subscription (event_id, status) VALUES (?, ?) ON DUPLICATE KEY UPDATE status = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, eventId.toString());
                statement.setString(2, status);
                statement.setString(3, status);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addToBlocklist(UserSnowflake user, UserSnowflake target, DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO players_blocklist (user_id, target_id) VALUES (?, ?)")) {
                statement.setString(1, user.getId());
                statement.setString(2, target.getId());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void remToBlocklist(UserSnowflake user, UserSnowflake target, DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("DELETE FROM players_blocklist WHERE user_id = ? AND target_id = ?")) {
                statement.setString(1, user.getId());
                statement.setString(2, target.getId());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public BlockedUser isBlocked(UserSnowflake user, UserSnowflake target, DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT user_id, target_id FROM players_blocklist WHERE user_id = ? AND target_id = ?")) {
                statement.setString(1, user.getId());
                statement.setString(2, target.getId());

                try (ResultSet resultset = statement.executeQuery()) {
                    if (resultset.next()) {
                        String userId = resultset.getString("user_id");
                        String targetId = resultset.getString("target_id");

                        return new BlockedUser(userId, targetId);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<BlockedUser> getBlockedUsers(UserSnowflake user, DataSource dataSource) throws Exception {
        List<BlockedUser> blockedUsers = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT user_id, target_id FROM players_blocklist WHERE user_id = ?")) {
                statement.setString(1, user.getId());

                try (ResultSet resultset = statement.executeQuery()) {
                    while (resultset.next()) {
                        String userId = resultset.getString("user_id");
                        String targetId = resultset.getString("target_id");

                        blockedUsers.add(new BlockedUser(userId, targetId));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return blockedUsers;
    }

    // Reward

    public boolean hasUnlockedRole(UserSnowflake user, String roleId, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT count(*) FROM players_level_rewards WHERE user_id = ? AND role_id = ?")) {
                statement.setString(1, user.getId());
                statement.setString(2, roleId);
                try (ResultSet resultset = statement.executeQuery()) {
                    if (resultset.next()) {
                        return resultset.getInt(1) > 0;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public void unlockRole(@NotNull UserSnowflake user, @NotNull LevelReward reward, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "INSERT INTO players_level_rewards (user_id, role_id) VALUES (?, ?)";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, user.getId());
                statement.setString(2, reward.getRoleId());

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createRewardRole(String roleId, int requiredLevel, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "INSERT INTO levelup_rewards (role_id, required_level) VALUES (?, ?)";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, roleId);
                statement.setInt(2, requiredLevel);

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<LevelReward> getAllRewards(DataSource dataSource) {
        List<LevelReward> rewards = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT role_id, required_level FROM levelup_rewards")) {
                try (ResultSet resultset = statement.executeQuery()) {
                    while (resultset.next()) {
                        rewards.add(new LevelReward(
                                resultset.getString("role_id"),
                                resultset.getInt("required_level")
                        ));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rewards;
    }

    public boolean hasRewardAtLevel(long level, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT count(*) FROM levelup_rewards WHERE required_level = ?")) {
                statement.setLong(1, level);
                try (ResultSet resultset = statement.executeQuery()) {
                    if (resultset.next()) {
                        return resultset.getInt(1) > 0;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public LevelReward getRewardAtLevel(long level, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT role_id, required_level FROM levelup_rewards WHERE required_level = ?")) {
                statement.setLong(1, level);
                try (ResultSet resultset = statement.executeQuery()) {
                    if (resultset.next()) {
                        return new LevelReward(
                                resultset.getString("role_id"),
                                resultset.getInt("required_level")
                        );
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<LevelReward> getRewardsUpToLevel(long level, DataSource dataSource) {
        List<LevelReward> rewards = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT role_id, required_level FROM levelup_rewards WHERE required_level <= ?")) {

            statement.setLong(1, level);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    rewards.add(new LevelReward(
                            resultSet.getString("role_id"),
                            resultSet.getInt("required_level")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rewards;
    }

    /**
     * Adds a permanent role to the database for a given user.
     *
     * @param role       The PermanentRole object representing the role to be added.
     * @param dataSource The DataSource used to establish a connection to the database.
     */
    public void addPermanentRole(PermanentRole role, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "INSERT INTO players_permanent_roles (user_id, role_id) VALUES (?, ?)";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, role.getUser().getId());
                statement.setString(2, role.getRoleId());

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removePermanentRole(PermanentRole role, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "DELETE FROM players_permanent_roles WHERE user_id = ? AND role_id = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, role.getUser().getId());
                statement.setString(2, role.getRoleId());

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the permanent roles associated with a specific user from the database.
     *
     * @param user       The UserSnowflake object representing the user.
     * @param dataSource The DataSource used to establish a connection to the database.
     * @return A List of PermanentRole objects representing the permanent roles of the user. If no roles are found, an empty list is returned.
     */
    public List<PermanentRole> fetchPermanentRoles(UserSnowflake user, DataSource dataSource) {
        List<PermanentRole> permanentRoles = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM players_permanent_roles WHERE user_id = ?")) {
                statement.setString(1, user.getId());

                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        permanentRoles.add(new PermanentRole(
                                UserSnowflake.fromId(resultSet.getString("user_id")),
                                resultSet.getString("role_id")
                        ));
                    }
                }
            }
        }  catch (SQLException e) {
            e.printStackTrace();
        }

        return permanentRoles;
    }

    /**
     * Checks if a user is quarantined.
     *
     * @param user       The UserSnowflake object representing the user to check.
     * @param dataSource The DataSource used to establish a connection to the database.
     * @return true if the user is quarantined, false otherwise.
     */
    public boolean isQuarantined(UserSnowflake user, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM quarantined_users WHERE user_id = ?")) {
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
     * Inserts a user into the quarantined_users table in the database.
     *
     * @param user       The UserSnowflake object representing the user to be quarantined.
     * @param dataSource The DataSource used to establish a connection to the database.
     */
    public void quarantineUser(UserSnowflake user, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO quarantined_users (user_id) VALUES (?)")) {
                statement.setString(1, user.getId());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Removes a user from the quarantined_users table in the database.
     *
     * @param user       The UserSnowflake object representing the user to be unquarantined.
     * @param dataSource The DataSource used to establish a connection to the database.
     */
    public void unquarantineUser(UserSnowflake user, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("DELETE FROM quarantined_users WHERE user_id = ?")) {
                statement.setString(1, user.getId());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addPlayerDeck(@NotNull UserSnowflake user, String slug, boolean isSelected, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO players_decks (slug, is_selected, user_id) VALUES (?, ?, ?)")) {
                statement.setString(1, slug);
                statement.setBoolean(2, isSelected);
                statement.setString(3, user.getId());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getSelectedDeck(@NotNull UserSnowflake user, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT slug FROM players_decks WHERE user_id = ? AND is_selected = true LIMIT 1";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, user.getId());

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getString("slug");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void switchDeck(@NotNull UserSnowflake user, String slug, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("UPDATE players_decks SET is_selected = ? WHERE user_id = ?")) {
                statement.setBoolean(1, false);
                statement.setString(2, user.getId());
                statement.executeUpdate();
            }

            try (PreparedStatement statement = connection.prepareStatement("UPDATE players_decks SET is_selected = ? WHERE user_id = ? AND slug = ?")) {
                statement.setBoolean(1, true);
                statement.setString(2, user.getId());
                statement.setString(3, slug);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean hasStaffPaycheck(UserSnowflake user, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT user_id, points, created_at, updated_at FROM staff_paycheck WHERE user_id = ?")) {
                statement.setString(1, user.getId());
                try (ResultSet resultset = statement.executeQuery()) {
                    return resultset.next();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void updateStaffPaycheck(UserSnowflake user, int points, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("UPDATE staff_paycheck SET points = ? WHERE user_id = ?")) {
                statement.setInt(1, points);
                statement.setString(2, user.getId());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void staffPaycheckWithdraw(UserSnowflake user, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("UPDATE staff_paycheck SET points = ?, updated_at = ? WHERE user_id = ?")) {
                statement.setInt(1, 0);
                statement.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
                statement.setString(3, user.getId());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<StaffPaycheck> getAllStaff(DataSource dataSource) {
        List<StaffPaycheck> staffPaychecks = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM staff_paycheck")) {
               try (ResultSet resultset = statement.executeQuery()) {
                   while (resultset.next()) {
                       StaffPaycheck staffPaycheck = new StaffPaycheck();
                       staffPaycheck.setUser(UserSnowflake.fromId(resultset.getString("user_id")));
                       staffPaycheck.setPoints(resultset.getInt("points"));
                       staffPaycheck.setCreatedAt(resultset.getTimestamp("created_at"));
                       staffPaycheck.setUpdatedAt(resultset.getTimestamp("updated_at"));
                       staffPaychecks.add(staffPaycheck);
                   }
               }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return staffPaychecks;
    }
}
