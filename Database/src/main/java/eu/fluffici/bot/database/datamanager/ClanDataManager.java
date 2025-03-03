package eu.fluffici.bot.database.datamanager;

/*
---------------------------------------------------------------------------------
File Name : ClanDataManager.java

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
import eu.fluffici.bot.api.beans.clans.ClanRequestBean;
import eu.fluffici.bot.api.hooks.PlayerBean;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("All")
public class ClanDataManager {
    // Create a sanction
    public void createClan(ClanBean clan, DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO `clans` (owner_id, clan_id, title, prefix, description, icon_url, color) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                statement.setString(1, clan.getOwnerId());
                statement.setString(2, clan.getClanId());
                statement.setString(3, clan.getTitle());
                statement.setString(4, clan.getPrefix());
                statement.setString(5, clan.getDescription());
                statement.setString(6, clan.getIconURL());
                statement.setString(7, clan.getColor());

                statement.executeUpdate();
            }
        }
        catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public ClanBean getClan(String clanId, DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT owner_id, clan_id , title, prefix, description, icon_url, color FROM clans WHERE clan_id = ?")) {
                statement.setString(1, clanId);

                try (ResultSet resultset = statement.executeQuery()) {
                    if (resultset.next()) {
                        String owner_id =  resultset.getString("owner_id");
                        String clan_id = resultset.getString("clan_id");
                        String title = resultset.getString("title");
                        String prefix = resultset.getString("prefix");
                        String description = resultset.getString("description");
                        String icon_url = resultset.getString("icon_url");
                        String color = resultset.getString("color");

                        return new ClanBean(owner_id, clan_id, title, prefix, description, icon_url, color);
                    }
                    else {
                        return null;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean hasPrefix(String prefix, DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT clan_id FROM clans WHERE prefix = ?")) {
                statement.setString(1, prefix);

                try (ResultSet resultset = statement.executeQuery()) {
                    if (resultset.next())
                    {
                        return true;
                    }
                    else
                    {
                        return false;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean hasTitle(String title, DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT clan_id FROM clans WHERE title = ?")) {
                statement.setString(1, title);

                try (ResultSet resultset = statement.executeQuery()) {
                    if (resultset.next()) {
                        return true;
                    }
                    else {
                        return false;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // Update a sanction status
    public void updateClan(ClanBean clan, DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("UPDATE clans SET owner_id = ?, title = ?, prefix = ?, description = ?, icon_url = ?, color = ? WHERE clan_id = ?")) {
                statement.setString(1, clan.getOwnerId());
                statement.setString(2, clan.getTitle());
                statement.setString(3, clan.getPrefix());
                statement.setString(4, clan.getDescription());
                statement.setString(5, clan.getIconURL());
                statement.setString(6, clan.getColor());
                statement.setString(7, clan.getClanId());

                // Execute the query
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteClan(ClanBean clan, DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement( "DELETE FROM clans WHERE clan_id = ?")) {
                statement.setString(1, clan.getClanId());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<ClanMembersBean> getClanMembers(String clanId, DataSource dataSource) throws Exception {
        List<ClanMembersBean> clanMembers = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM clan_members WHERE clan_id = ?")) {
                statement.setString(1, clanId);
                try (ResultSet resultset = statement.executeQuery()) {
                    while (resultset.next()) {
                        String clanIds = resultset.getString("clan_id");
                        String userId = resultset.getString("user_id");
                        String rank = resultset.getString("rank");

                        clanMembers.add(new ClanMembersBean(clanIds, userId, rank));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return clanMembers;
    }

    public ClanMembersBean getClanMember(PlayerBean player, DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM clan_members WHERE user_id = ?")) {
                statement.setString(1, player.getUserId());

                try (ResultSet resultset = statement.executeQuery()) {
                    if (resultset.next()) {
                        String clanIds = resultset.getString("clan_id");
                        String userId = resultset.getString("user_id");
                        String rank = resultset.getString("rank");

                        return new ClanMembersBean(clanIds, userId, rank);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void addClanMember( ClanMembersBean member, DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO clan_members (`clan_id`, `user_id`, `rank`) VALUES (?, ?, ?)")) {
                statement.setString(1, member.getClanId());
                statement.setString(2, member.getUserId());
                statement.setString(3, member.getRank());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteClanMember(ClanMembersBean member, DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("DELETE FROM clan_members WHERE clan_id = ? AND user_id = ?")) {
                statement.setString(1, member.getClanId());
                statement.setString(2, member.getUserId());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createClanInvite(ClanRequestBean request, DataSource dataSource) throws Exception {
        try (Connection  connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO clan_requests (invite_id, clan_id, user_id, sent_by, status, expiration, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                statement.setString(1, request.getInviteId());
                statement.setString(2, request.getClanId());
                statement.setString(3, request.getUserId());
                statement.setString(4, request.getSentBy());
                statement.setString(5, request.getStatus());
                statement.setTimestamp(6, request.getExpiration());
                statement.setTimestamp(7, request.getCreatedAt());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ClanRequestBean getActiveInvite(String userId, DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM clan_requests WHERE user_id = ? AND status = 'ACTIVE'")) {
                statement.setString(1, userId);

                try (ResultSet  resultset = statement.executeQuery()) {
                    if (resultset.next()) {
                        String inviteId = resultset.getString("invite_id");
                        String clanId = resultset.getString("clan_id");
                        String sentBy = resultset.getString("sent_by");
                        String status = resultset.getString("status");
                        Timestamp expiration = resultset.getTimestamp("expiration");
                        Timestamp createdAt = resultset.getTimestamp("created_at");

                        return new ClanRequestBean(inviteId, clanId, userId, sentBy, status, expiration, createdAt);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void setAcknowledged(String inviteId, DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("UPDATE `clan_requests` SET status = 'ACKNOWLEDGED' WHERE invite_id = ?")) {
                statement.setString(1, inviteId);
                statement.executeUpdate();
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setDenied(String inviteId, DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("UPDATE `clan_requests` SET status = 'DENIED' WHERE invite_id = ?")) {
                statement.setString(1, inviteId);
                statement.executeUpdate();
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateExpiredInvites(DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("UPDATE `clan_requests` SET status = 'EXPIRED' WHERE status = 'ACTIVE' AND (status != 'EXPIRED' AND expiration < NOW())")) {
                statement.executeUpdate();
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
