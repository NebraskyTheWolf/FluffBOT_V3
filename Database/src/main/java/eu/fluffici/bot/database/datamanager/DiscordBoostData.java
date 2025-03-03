package eu.fluffici.bot.database.datamanager;

/*
---------------------------------------------------------------------------------
File Name : DiscordBoostData.java

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


import eu.fluffici.bot.api.beans.players.PlayerBoost;
import net.dv8tion.jda.api.entities.UserSnowflake;

import javax.sql.DataSource;
import java.sql.*;

public class DiscordBoostData {
    public void applyBoost(PlayerBoost boost, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO players_boosts (user_id, last_reward_claim, is_cancelled) VALUES (?, ?, ?)")) {
                statement.setString(1, boost.getUserId());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isBoosting(UserSnowflake user, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT id FROM players_boosts WHERE user_id = ? AND is_cancelled = 0")) {
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

    public PlayerBoost fetchBoost(UserSnowflake user, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT user_id, last_reward_claim as last_claim, is_cancelled FROM players_boosts WHERE user_id = ? AND is_cancelled = 0")) {
                statement.setString(1, user.getId());

                try (ResultSet resultset = statement.executeQuery()) {
                    if (resultset.next()) {
                        String userId = resultset.getString("user_id");
                        Timestamp lastClaimedReward = resultset.getTimestamp("last_claim");
                        boolean isCancelled = resultset.getBoolean("is_cancelled");

                        return new PlayerBoost(userId, lastClaimedReward, isCancelled);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void updateBoost(PlayerBoost playerBoost, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("UPDATE players_boosts SET last_reward_claim = ?, is_cancelled = ? WHERE user_id = ?")) {
                statement.setTimestamp(1, playerBoost.getLastRewardClaimed());
                statement.setBoolean(2, playerBoost.isCancelled());
                statement.setString(3, playerBoost.getUserId());

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
