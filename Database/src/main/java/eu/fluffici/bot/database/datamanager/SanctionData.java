package eu.fluffici.bot.database.datamanager;

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


import eu.fluffici.bot.api.beans.players.SanctionBean;
import eu.fluffici.bot.api.hooks.PlayerBean;
import net.dv8tion.jda.api.entities.UserSnowflake;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("All")
public class SanctionData {
    public void applySanction(int sanctionType, SanctionBean sanction, DataSource dataSource) throws Exception {
        // Create the sanction
        try (Connection connection = dataSource.getConnection()) {
            Timestamp expirationTime = sanction.getExpirationTime();
            String expirationDate = new Timestamp(Instant.now().toEpochMilli()).toString();

            if(expirationTime != null)
                expirationDate = expirationTime.toString();

            // Query construction
            String sql = "insert into sanctions (user_id, type_id, reason, author_id, expiration_date, is_deleted, creation_date, update_date, attachment_url)";
            sql += " values (?, ?, ?, ?, ?, 0, now(), now(), ?)";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, sanction.getUserId());
                statement.setInt(2, sanctionType);
                statement.setString(3, sanction.getReason());
                statement.setString(4, sanction.getAuthorId());
                statement.setString(5, expirationDate);
                statement.setString(6, sanction.getAttachmentUrl());

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeSanction(int sanctionType, PlayerBean player, DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "update sanctions set is_deleted=1, update_date = now() where type_id = ? and user_id = ?";
            sql += " and is_deleted = 0 order by creation_date desc limit 1";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, sanctionType);
                statement.setString(2, player.getUserId());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public SanctionBean getPlayerBanned(String player, DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            // Set connection
            ;
            Timestamp expirationTime;

            // Query construction
            String sql = "";
            sql += "select sanction_id, user_id , type_id, reason, author_id, expiration_date, is_deleted, creation_date, update_date, attachment_url from sanctions";
            sql += " where user_id=? and type_id = ? and (expiration_date > now()) ";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, player);
                statement.setInt(2, SanctionBean.BAN);

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

                        return new SanctionBean(sanctionId, typeId, banPlayer, reason, punisher, expirationTime, creationDate, updateDate, isDeleted, attachmentUrl);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public SanctionBean getPlayerMuted(String player, DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            Timestamp expirationTime;

            // Query construction
            String sql = "";
            sql += "select sanction_id, user_id , type_id, reason, author_id, expiration_date, is_deleted, creation_date, update_date, attachment_url from sanctions";
            sql += " where user_id = ? and type_id = ? and expiration_date > now() and is_deleted = 0";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, player);
                statement.setInt(2, SanctionBean.MUTE);

                try (ResultSet resultset = statement.executeQuery()) {
                    if (resultset.next()) {
                        long sanctionId = resultset.getLong("sanction_id");
                        String mutePlayer = resultset.getString("user_id");
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

                        return new SanctionBean(sanctionId, typeId, mutePlayer, reason, punisher, expirationTime, creationDate, updateDate, isDeleted, attachmentUrl);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<SanctionBean> getAllSanctions(DataSource dataSource) throws Exception {
        List<SanctionBean> sanctionList = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {
            Timestamp expirationTime;

            // Query construction
            String sql = "select sanction_id, user_id, type_id, reason, author_id, expiration_date, is_deleted, creation_date, update_date, attachment_url from sanctions";
            sql += " order by creation_date desc";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
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
                        SanctionBean sanction =  new SanctionBean(sanctionId, typeId, playerUuid, reason, punisherUUID, expirationTime, creationDate, updateDate, isDeleted, attachmentUrl);

                        sanctionList.add(sanction);
                    }
                }
            }

        } catch(SQLException e) {
            e.printStackTrace();
        }

        return sanctionList;
    }

    public List<SanctionBean> getAllSanctionsBy(UserSnowflake moderator, DataSource dataSource) throws Exception {
        List<SanctionBean> sanctionList = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {
            Timestamp expirationTime;

            // Query construction
            String sql = "select sanction_id, user_id, type_id, reason, author_id, expiration_date, is_deleted, creation_date, update_date, attachment_url from sanctions WHERE author_id = ?";
            sql += " order by creation_date desc";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, moderator.getId());
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
                        SanctionBean sanction =  new SanctionBean(sanctionId, typeId, playerUuid, reason, punisherUUID, expirationTime, creationDate, updateDate, isDeleted, attachmentUrl);

                        sanctionList.add(sanction);
                    }
                }
            }

        } catch(SQLException e) {
            e.printStackTrace();
        }

        return sanctionList;
    }

    public List<SanctionBean> getAllWarns(String userId, DataSource dataSource) throws Exception {
        List<SanctionBean> sanctionList = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {
            Timestamp expirationTime;

            // Query construction
            String sql = "select sanction_id, user_id, type_id, reason, author_id, expiration_date, is_deleted, creation_date, update_date, attachment_url from sanctions";
            sql += " where user_id = ? and type_id = 1 and is_deleted = 0 order by creation_date desc";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, userId);

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
                        SanctionBean sanction =  new SanctionBean(sanctionId, typeId, playerUuid, reason, punisherUUID, expirationTime, creationDate, updateDate, isDeleted, attachmentUrl);

                        sanctionList.add(sanction);
                    }
                }
            }

        } catch(SQLException e) {
            e.printStackTrace();
        }

        return sanctionList;
    }

    public List<SanctionBean> getAllActiveWarns(String userId, DataSource dataSource) throws Exception {
        List<SanctionBean> sanctionList = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {
            Timestamp expirationTime;

            // Query construction
            String sql = "select sanction_id, user_id, type_id, reason, author_id, expiration_date, is_deleted, creation_date, update_date, attachment_url from sanctions";
            sql += " where user_id = ? and type_id = 1 order by creation_date desc";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, userId);

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
                        SanctionBean sanction =  new SanctionBean(sanctionId, typeId, playerUuid, reason, punisherUUID, expirationTime, creationDate, updateDate, isDeleted, attachmentUrl);

                        sanctionList.add(sanction);
                    }
                }
            }

        } catch(SQLException e) {
            e.printStackTrace();
        }

        return sanctionList;
    }

    public List<SanctionBean> getAllActiveSanctions(String userId, int sanctionType, DataSource dataSource) throws Exception {
        List<SanctionBean> sanctionList = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {
            Timestamp expirationTime;

            // Query construction
            String sql = "select sanction_id, user_id, type_id, reason, author_id, expiration_date, is_deleted, creation_date, update_date, attachment_url from sanctions";
            sql += " where user_id = ? and type_id = ? and is_deleted = 0 order by creation_date desc";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, userId);
                statement.setInt(2, sanctionType);

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
                        SanctionBean sanction =  new SanctionBean(sanctionId, typeId, playerUuid, reason, punisherUUID, expirationTime, creationDate, updateDate, isDeleted, attachmentUrl);

                        sanctionList.add(sanction);
                    }
                }
            }

        } catch(SQLException e) {
            e.printStackTrace();
        }

        return sanctionList;
    }

    public void updateSanctionStatus(long sanctionId, boolean status, DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "update sanctions set is_deleted = ?, update_date = now() where sanction_id = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setBoolean(1, status);
                statement.setLong(2, sanctionId);

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<SanctionBean> updateExpiredSanctions(DataSource dataSource) throws Exception {
        List<SanctionBean> updated = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("UPDATE `sanctions` SET is_deleted = 0, updated_at = CURRENT_TIMESTAMP() WHERE is_deleted = 1 AND (is_deleted != 0 AND expiration_date < NOW())")) {
                statement.executeUpdate();
            }

            try (PreparedStatement query = connection.prepareStatement("SELECT * FROM `sanctions` WHERE is_deleted = 1 AND updated_at > NOW() - INTERVAL 1 MINUTE")) {
                try (ResultSet resultset = query.executeQuery()) {
                    while (resultset.next()) {
                        long sanctionId = resultset.getLong("sanction_id");
                        String playerUuid = resultset.getString("user_id");
                        int typeId = resultset.getInt("type_id");
                        String reason = resultset.getString("reason");
                        String punisherUUID = resultset.getString("author_id");
                        Timestamp expirationTime = resultset.getTimestamp("expiration_date");
                        Timestamp creationDate = resultset.getTimestamp("creation_date");
                        Timestamp updateDate = resultset.getTimestamp("update_date");
                        boolean isDeleted = resultset.getBoolean("is_deleted");
                        String attachmentUrl = resultset.getString("attachment_url");

                        updated.add(new SanctionBean(sanctionId, typeId, playerUuid, reason, punisherUUID, expirationTime, creationDate, updateDate, isDeleted, attachmentUrl));
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return updated;
    }
}
