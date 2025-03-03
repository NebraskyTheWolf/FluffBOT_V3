package eu.fluffici.bot.database.datamanager;

/*
---------------------------------------------------------------------------------
File Name : InteractionData.java

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


import eu.fluffici.bot.api.interactions.Interactions;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InteractionData {

    // Create a sanction
    public void newInteraction(Interactions interactions, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "insert into interactions (interaction_id, user_id, custom_id, message_id, channel_id, is_attached, is_acknowledged, is_expired, is_updated, is_dm, expiration, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, interactions.getInteractionId());
                statement.setString(2, interactions.getUserId());
                statement.setString(3, interactions.getCustomId());
                statement.setString(4, interactions.getMessageId());
                statement.setString(5, interactions.getChannelId());
                statement.setBoolean(6, interactions.isAttached());
                statement.setBoolean(7, interactions.isAcknowledged());
                statement.setBoolean(8, interactions.isExpired());
                statement.setBoolean(9, interactions.isUpdated());
                statement.setBoolean(10, interactions.isDm());
                statement.setTimestamp(11, interactions.getExpiration());
                statement.setTimestamp(12, interactions.getCreatedAt());
                statement.setTimestamp(13, interactions.getUpdatedAt());

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateInteraction(Interactions interactions, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("UPDATE interactions SET  message_id = ?, is_attached = ?, is_acknowledged = ?, is_expired = ?, is_updated = ?, is_dm = ?, channel_id = ? WHERE interaction_id = ?")) {
                statement.setString(1, interactions.getMessageId());
                statement.setBoolean(2, interactions.isAttached());
                statement.setBoolean(3, interactions.isAcknowledged());
                statement.setBoolean(4, interactions.isExpired());
                statement.setBoolean(5, interactions.isUpdated());
                statement.setBoolean(6, interactions.isDm());
                statement.setString(7, interactions.getChannelId());
                statement.setString(8, interactions.getInteractionId());

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Interactions fetchInteraction(String id, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT interaction_id, user_id, custom_id, message_id, channel_id, is_attached, is_acknowledged, is_expired, is_updated, is_dm, expiration, created_at, updated_at FROM interactions WHERE interaction_id = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, id);

                try (ResultSet resultset = statement.executeQuery()) {
                    if (resultset.next()) {
                        String interaction_id = resultset.getString("interaction_id");
                        String user_id = resultset.getString("user_id");
                        String custom_id = resultset.getString("custom_id");
                        String message_id = resultset.getString("message_id");
                        String channel_id = resultset.getString("channel_id");

                        boolean is_attached = resultset.getBoolean("is_attached");
                        boolean is_acknowledged = resultset.getBoolean("is_acknowledged");
                        boolean is_expired = resultset.getBoolean("is_expired");
                        boolean is_updated = resultset.getBoolean("is_updated");
                        boolean is_dm = resultset.getBoolean("is_dm");

                        Timestamp expiration = resultset.getTimestamp("expiration");
                        Timestamp created_at = resultset.getTimestamp("created_at");
                        Timestamp updated_at = resultset.getTimestamp("updated_at");

                        return new Interactions(interaction_id, user_id, custom_id, message_id, channel_id, is_attached, is_acknowledged, is_expired, is_updated, is_dm, expiration, created_at, updated_at);
                    }
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void setAcknowledged(String userId, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("UPDATE `interactions` SET is_acknowledged = 1 WHERE is_acknowledged = 0 AND user_id = ?")) {
                statement.setString(1, userId);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setAttached(Interactions interactions, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("UPDATE `interactions` SET is_attached = 1, message_id = ? WHERE interaction_id = ?")) {
                statement.setString(1, interactions.getMessageId());
                statement.setString(2, interactions.getInteractionId());

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setUpdated(Interactions interactions, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("UPDATE `interactions` SET is_updated = 1 WHERE interaction_id = ?")) {
                statement.setString(1, interactions.getInteractionId());

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateExpiredInteractions(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("UPDATE `interactions` SET is_expired = 1 WHERE is_expired = 0 AND (is_expired != 1 AND expiration < NOW())")) {
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Interactions> getAllInteractions(DataSource dataSource) throws Exception {
        List<Interactions> interactions = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT interaction_id, user_id, custom_id, message_id, channel_id, is_attached, is_acknowledged, is_expired, is_updated, is_dm, expiration, created_at, updated_at FROM interactions WHERE is_updated != 1";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                try (ResultSet resultset = statement.executeQuery()) {
                    while (resultset.next()) {
                        String interaction_id = resultset.getString("interaction_id");
                        String user_id = resultset.getString("user_id");
                        String custom_id = resultset.getString("custom_id");
                        String message_id = resultset.getString("message_id");
                        String channel_id = resultset.getString("channel_id");

                        boolean is_attached = resultset.getBoolean("is_attached");
                        boolean is_acknowledged = resultset.getBoolean("is_acknowledged");
                        boolean is_expired = resultset.getBoolean("is_expired");
                        boolean is_updated = resultset.getBoolean("is_updated");
                        boolean is_dm = resultset.getBoolean("is_dm");

                        Timestamp expiration = resultset.getTimestamp("expiration");
                        Timestamp created_at = resultset.getTimestamp("created_at");
                        Timestamp updated_at = resultset.getTimestamp("updated_at");

                        interactions.add(new Interactions(
                                interaction_id,
                                user_id,
                                custom_id,
                                message_id,
                                channel_id,
                                is_attached,
                                is_acknowledged,
                                is_expired,
                                is_updated,
                                is_dm,
                                expiration,
                                created_at,
                                updated_at
                        ));
                    }
                }
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }

        return interactions;
    }
}
