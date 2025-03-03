/*
---------------------------------------------------------------------------------
File Name : RelationshipDataManager

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 06/06/2024
Last Modified : 06/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.database.datamanager;

import eu.fluffici.bot.api.beans.players.RelationshipInviteBuilder;
import eu.fluffici.bot.api.beans.players.RelationshipMember;
import net.dv8tion.jda.api.entities.UserSnowflake;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RelationshipDataManager {
    public void removeRelationship(UserSnowflake userIdToRemove, UserSnowflake target, DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("DELETE FROM relationships_members WHERE user_id = ?")) {
                statement.setString(1, userIdToRemove.getId());
                statement.executeUpdate();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void createRelationshipInvite(RelationshipInviteBuilder relationshipInvite, DataSource dataSource) throws Exception {
        if (relationshipInvite.getRelationshipOwner() == null || relationshipInvite.getUserId() == null) {
            throw new IllegalArgumentException("Both owner and user must have valid IDs");
        } else {
            relationshipInvite.getUserId().getId();
        }

        this.createRelationship(relationshipInvite.getRelationshipOwner(), dataSource);

        String sql = "INSERT INTO players_relationship_invites (relationship_owner, user_id) VALUES (?, ?)";

        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, relationshipInvite.getRelationshipOwner().getId());
                statement.setString(2, relationshipInvite.getUserId().getId());
                statement.executeUpdate();
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public void acceptRelationshipInvite(UserSnowflake user, DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "UPDATE players_relationship_invites SET is_acknowledged = 1 WHERE user_id = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, user.getId());
                statement.executeUpdate();
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public RelationshipInviteBuilder fetchRelationshipInvite(UserSnowflake user, DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT relationship_owner, user_id, is_acknowledged, is_denied FROM players_relationship_invites WHERE user_id = ? AND is_acknowledged = 0 AND is_denied = 0";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, user.getId());

                try (ResultSet resultset = statement.executeQuery()) {
                    if (resultset.next()) {
                        return RelationshipInviteBuilder
                                .builder()
                                .relationshipOwner(UserSnowflake.fromId(resultset.getString("relationship_owner")))
                                .userId(UserSnowflake.fromId(resultset.getString("user_id")))
                                .isAcknowledged(false)
                                .isDenied(false)
                                .build();
                    }
                }
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean fetchRelationship(UserSnowflake ownerId, UserSnowflake userId, DataSource dataSource, boolean noNeedClosing) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "select owner_id, user_id from relationships_members WHERE owner_id = ? OR user_id = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, ownerId.getId());
                statement.setString(2, userId.getId());

                try (ResultSet resultset = statement.executeQuery()) {
                    return resultset.next();
                }
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public void createRelationship(UserSnowflake userId, DataSource dataSource) throws Exception {
        if (this.hasRelationship(userId, userId, dataSource, true)) {
            return;
        }

        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO `relationships` (user_id) VALUES (?)")) {
                statement.setString(1, userId.getId());
                statement.executeUpdate();
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean hasRelationship(UserSnowflake ownerId, UserSnowflake userId, DataSource dataSource, boolean noNeedClosing) throws Exception {
        return this.fetchRelationship(ownerId, userId, dataSource, noNeedClosing);
    }

    public void addMemberToRelationship(UserSnowflake ownerId, UserSnowflake userId, DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "INSERT INTO relationships_members (owner_id, user_id) VALUES (?, ?)";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, ownerId.getId());
                statement.setString(2, userId.getId());

                statement.executeUpdate();
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public void remMemberToRelationship(UserSnowflake userId, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "DELETE FROM relationships_members WHERE user_id = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, userId.getId());
                statement.executeUpdate();
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public List<RelationshipMember> fetchAllRelationshipMembers(UserSnowflake ownerId, DataSource dataSource) throws Exception {
        List<RelationshipMember> members = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT owner_id, user_id FROM relationships_members WHERE owner_id = ? OR user_id = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, ownerId.getId());
                statement.setString(2, ownerId.getId());

                try (ResultSet resultset = statement.executeQuery()) {
                    while (resultset.next()) {
                        members.add(RelationshipMember
                                .builder()
                                .relationshipOwner(UserSnowflake.fromId(resultset.getString("owner_id")))
                                .userId(UserSnowflake.fromId(resultset.getString("user_id")))
                                .build()
                        );
                    }
                }
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }

        return members;
    }

    public boolean isRelationshipOwner(UserSnowflake user, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT * FROM relationships_members WHERE owner_id = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, user.getId());

                try (ResultSet resultset = statement.executeQuery()) {
                    return resultset.next();
                }
            }
        }  catch(SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
}