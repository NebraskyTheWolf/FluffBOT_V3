package eu.fluffici.bot.database.datamanager.furraiddb;

/*
---------------------------------------------------------------------------------
File Name : SanctionData.java

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


import eu.fluffici.bot.api.beans.furraid.Sanction;
import eu.fluffici.bot.api.beans.players.SanctionBean;
import eu.fluffici.bot.api.hooks.PlayerBean;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.UserSnowflake;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("All")
public class FurRaidSanctionData {
    public void applySanction(Sanction sanction, DataSource dataSource) throws Exception {
        // Create the sanction
        try (Connection connection = dataSource.getConnection()) {
            Timestamp expirationTime = sanction.getExpirationTime();
            String expirationDate = new Timestamp(Instant.now().toEpochMilli()).toString();

            if(expirationTime != null)
                expirationDate = expirationTime.toString();

            // Query construction
            String sql = "insert into furraid_sanctions (user_id, type_id, reason, author_id, expiration_date, is_deleted, creation_date, update_date, attachment_url, guild_id)";
            sql += " values (?, ?, ?, ?, ?, 0, now(), now(), ?, ?)";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, sanction.getUserId());
                statement.setInt(2, sanction.getTypeId());
                statement.setString(3, sanction.getReason());
                statement.setString(4, sanction.getAuthorId());
                statement.setString(5, expirationDate);
                statement.setString(6, sanction.getAttachmentUrl());
                statement.setString(7, sanction.getGuildId());

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeSanction(Guild guild, int sanctionType, UserSnowflake user, DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "update furraid_sanctions set is_deleted = 1, update_date = now() where type_id = ? and user_id = ?";
            sql += " and is_deleted = 0 and guild_id = ? order by creation_date desc limit 1";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, sanctionType);
                statement.setString(2, user.getId());
                statement.setString(3, guild.getId());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Sanction getPlayerBanned(String player, String guildId, DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            // Set connection
            ;
            Timestamp expirationTime;

            // Query construction
            String sql = "";
            sql += "select sanction_id, user_id , type_id, reason, author_id, expiration_date, is_deleted, creation_date, update_date, attachment_url from furraid_sanctions";
            sql += " where user_id=? and type_id = ? and (expiration_date > now()) and guild_id = ? ";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, player);
                statement.setInt(2, SanctionBean.BAN);
                statement.setString(3, guildId);

                try (ResultSet resultset = statement.executeQuery()) {
                    if (resultset.next()) {
                        // The player is banned
                        long sanctionId =  resultset.getLong("sanction_id");
                        String banPlayer = resultset.getString("user_id");
                        int typeId = resultset.getInt("type_id");
                        String reason = resultset.getString("reason");
                        String punisher = resultset.getString("author_id");

                        try {
                            expirationTime = resultset.getTimestamp("expiration_date");
                        } catch (Exception dateException) {
                            expirationTime = null;
                        }

                        boolean isDeleted = resultset.getBoolean("is_deleted");
                        Timestamp creationDate = resultset.getTimestamp("creation_date");
                        Timestamp updateDate = resultset.getTimestamp("update_date");

                        String attachmentUrl = resultset.getString("attachment_url");

                        return new Sanction(sanctionId, typeId, guildId, banPlayer, reason, punisher, expirationTime, creationDate, updateDate, isDeleted, attachmentUrl);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<Sanction> getAllSanctions(String guildId, DataSource dataSource) throws Exception {
        List<Sanction> sanctionList = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {
            Timestamp expirationTime;

            // Query construction
            String sql = "select sanction_id, user_id, type_id, reason, author_id, expiration_date, is_deleted, creation_date, update_date, attachment_url from furraid_sanctions";
            sql += " where guild_id = ? order by creation_date desc";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, guildId);
                try (ResultSet resultset = statement.executeQuery()) {
                    while (resultset.next()) {
                        long sanctionId = resultset.getLong("sanction_id");
                        String playerUuid = resultset.getString("user_id");
                        int typeId = resultset.getInt("type_id");
                        String reason = resultset.getString("reason");
                        String punisherUUID = resultset.getString("author_id");

                        try {
                            expirationTime = resultset.getTimestamp("expiration_date");
                        } catch (Exception dateException) {
                            expirationTime = null;
                        }

                        boolean isDeleted = resultset.getBoolean("is_deleted");
                        Timestamp creationDate = resultset.getTimestamp("creation_date");
                        Timestamp updateDate = resultset.getTimestamp("update_date");

                        String attachmentUrl = resultset.getString("attachment_url");
                        Sanction sanction =  new Sanction(sanctionId, typeId, guildId, playerUuid, reason, punisherUUID, expirationTime, creationDate, updateDate, isDeleted, attachmentUrl);

                        sanctionList.add(sanction);
                    }
                }
            }

        } catch(SQLException e) {
            e.printStackTrace();
        }

        return sanctionList;
    }

    public int getAllWarns(String userId, String guildId, DataSource dataSource) throws Exception {
        List<Sanction> sanctionList = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {
            Timestamp expirationTime;

            // Query construction
            String sql = "select sanction_id, user_id, type_id, reason, author_id, expiration_date, is_deleted, creation_date, update_date, attachment_url from furraid_sanctions";
            sql += " where user_id = ? and guild_id = ? and type_id = 1 and is_deleted = 0 order by creation_date desc";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, userId);
                statement.setString(2, guildId);

                try (ResultSet resultset = statement.executeQuery()) {
                    while (resultset.next()) {
                        long sanctionId = resultset.getLong("sanction_id");
                        String playerUuid = resultset.getString("user_id");
                        int typeId = resultset.getInt("type_id");
                        String reason = resultset.getString("reason");
                        String punisherUUID = resultset.getString("author_id");

                        try {
                            expirationTime = resultset.getTimestamp("expiration_date");
                        } catch (Exception dateException) {
                            expirationTime = null;
                        }

                        boolean isDeleted = resultset.getBoolean("is_deleted");
                        Timestamp creationDate = resultset.getTimestamp("creation_date");
                        Timestamp updateDate = resultset.getTimestamp("update_date");

                        String attachmentUrl = resultset.getString("attachment_url");
                        Sanction sanction =  new Sanction(sanctionId, typeId, guildId, playerUuid, reason, punisherUUID, expirationTime, creationDate, updateDate, isDeleted, attachmentUrl);

                        sanctionList.add(sanction);
                    }
                }
            }

        } catch(SQLException e) {
            e.printStackTrace();
        }

        return sanctionList.size();
    }

    public List<Sanction> getAllActiveSanctions(String userId, String guildId, int sanctionType, DataSource dataSource) throws Exception {
        List<Sanction> sanctionList = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {
            Timestamp expirationTime;

            // Query construction
            String sql = "select sanction_id, user_id, type_id, reason, author_id, expiration_date, is_deleted, creation_date, update_date, attachment_url from furraid_sanctions";
            sql += " where user_id = ? and type_id = ? and guild_id = ? and is_deleted = 0 order by creation_date desc";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, userId);
                statement.setInt(2, sanctionType);
                statement.setString(3, guildId);

                try (ResultSet resultset = statement.executeQuery()) {
                    while (resultset.next()) {
                        // There's a result
                        long sanctionId = resultset.getLong("sanction_id");
                        String playerUuid = resultset.getString("user_id");
                        int typeId = resultset.getInt("type_id");
                        String reason = resultset.getString("reason");
                        String punisherUUID = resultset.getString("author_id");

                        try {
                            expirationTime = resultset.getTimestamp("expiration_date");
                        }
                        catch (Exception dateException) {
                            expirationTime = null;
                        }

                        boolean isDeleted = resultset.getBoolean("is_deleted");
                        Timestamp creationDate = resultset.getTimestamp("creation_date");
                        Timestamp updateDate = resultset.getTimestamp("update_date");

                        String attachmentUrl = resultset.getString("attachment_url");
                        Sanction sanction =  new Sanction(sanctionId, typeId, guildId, playerUuid, reason, punisherUUID, expirationTime, creationDate, updateDate, isDeleted, attachmentUrl);

                        sanctionList.add(sanction);
                    }
                }
            }

        } catch(SQLException e) {
            e.printStackTrace();
        }

        return sanctionList;
    }

    public void updateSanctionStatus(long sanctionId, String guildId, boolean status, DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "update furraid_sanctions set is_deleted = ?, update_date = now() where sanction_id = ? and guild_id = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setBoolean(1, status);
                statement.setLong(2, sanctionId);
                statement.setString(3, guildId);

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Sanction> updateExpiredSanctions(DataSource dataSource) throws Exception {
        List<Sanction> updated = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {
            // First, update the sanctions
            try (PreparedStatement updateStatement = connection.prepareStatement(
                    "UPDATE `furraid_sanctions` SET is_deleted = 0, update_date = CURRENT_TIMESTAMP() WHERE is_deleted = 1 AND (is_deleted != 0 AND expiration_date < NOW())")) {
                updateStatement.executeUpdate();
            }

            // Then, retrieve the updated sanctions
            try (PreparedStatement selectStatement = connection.prepareStatement(
                    "SELECT * FROM `furraid_sanctions` WHERE is_deleted = 0 AND update_date > NOW() - INTERVAL 1 MINUTE")) {
                try (ResultSet resultset = selectStatement.executeQuery()) {
                    while (resultset.next()) {
                        FurRaidSanctionData sanction = new FurRaidSanctionData();
                        long sanctionId = resultset.getLong("sanction_id");
                        String guildId = resultset.getString("guild_id");
                        String playerUuid = resultset.getString("user_id");
                        int typeId = resultset.getInt("type_id");
                        String reason = resultset.getString("reason");
                        String punisherUUID = resultset.getString("author_id");

                        Timestamp expirationTime = resultset.getTimestamp("expiration_date");
                        Timestamp creationDate = resultset.getTimestamp("creation_date");
                        Timestamp updateDate = resultset.getTimestamp("update_date");

                        boolean isDeleted = resultset.getBoolean("is_deleted");

                        String attachmentUrl = resultset.getString("attachment_url");

                        updated.add(new Sanction(sanctionId, typeId, guildId, playerUuid, reason, punisherUUID, expirationTime, creationDate, updateDate, isDeleted, attachmentUrl));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return updated;
    }

}
