/*
---------------------------------------------------------------------------------
File Name : RolesDataManager

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 08/06/2024
Last Modified : 08/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.database.datamanager;

import eu.fluffici.bot.api.beans.roles.PurchasableRoles;
import eu.fluffici.bot.api.beans.roles.PurchasedRoles;
import net.dv8tion.jda.api.entities.UserSnowflake;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ALL")
public class RolesDataManager {

    public List<PurchasableRoles> fetchAllPurchasableRoles(DataSource dataSource) {
        List<PurchasableRoles> purchasableRoles = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT role_id, price FROM purchasable_roles")) {
                try (ResultSet resultSet = statement.executeQuery()) {
                   while (resultSet.next()) {
                       purchasableRoles.add(new PurchasableRoles(
                               resultSet.getString("role_id"),
                               resultSet.getInt("price")
                       ));
                   }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return purchasableRoles;
    }

    public PurchasableRoles fetchRoleById(String roleId, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT role_id, price FROM purchasable_roles WHERE role_id = ?")) {
                statement.setString(1, roleId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return new PurchasableRoles(
                                resultSet.getString("role_id"),
                                resultSet.getInt("price")
                        );
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public PurchasedRoles fetchRoleById(UserSnowflake user, String roleId, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT user_id, role_id, is_selected FROM players_purchased_roles WHERE role_id = ? AND user_id = ?")) {
                statement.setString(1, roleId);
                statement.setString(2, user.getId());
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return new PurchasedRoles(
                                UserSnowflake.fromId(resultSet.getString("user_id")),
                                resultSet.getString("role_id"),
                                resultSet.getBoolean("is_selected")
                        );
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public PurchasedRoles fetchSelectedRole(UserSnowflake user, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT user_id, role_id, is_selected FROM players_purchased_roles WHERE user_id = ? AND is_selected = 1")) {
                statement.setString(1, user.getId());
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return new PurchasedRoles(
                                UserSnowflake.fromId(resultSet.getString("user_id")),
                                resultSet.getString("role_id"),
                                resultSet.getBoolean("is_selected")
                        );
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean addPurchasedRole(PurchasedRoles role, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO players_purchased_roles (user_id, role_id, is_selected) VALUES (?, ?, ?)")) {
                statement.setString(1, role.getUser().getId());
                statement.setString(2, role.getRoleId());
                statement.setBoolean(3, role.isSelected());
                int rowsAffected = statement.executeUpdate();
                return rowsAffected > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<PurchasedRoles> fetchAllPurchasedRoles(UserSnowflake user, DataSource dataSource) {
        List<PurchasedRoles> purchasedRoles = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT user_id, role_id, is_selected FROM players_purchased_roles WHERE user_id = ?")) {
                statement.setString(1, user.getId());
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        purchasedRoles.add(new PurchasedRoles(
                                UserSnowflake.fromId( resultSet.getString("user_id")),
                                resultSet.getString("role_id"),
                                resultSet.getBoolean("is_selected")
                        ));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return purchasedRoles;
    }

    public void updatePurchasedRole(PurchasedRoles role, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("UPDATE players_purchased_roles SET is_selected = ? WHERE user_id = ? AND role_id = ?")) {
                statement.setBoolean(1, role.isSelected());
                statement.setString(2, role.getUser().getId());
                statement.setString(3, role.getRoleId());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean hasPurchasedRole(UserSnowflake user, String roleId, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM players_purchased_roles WHERE user_id = ? AND role_id = ?")) {
                statement.setString(1, user.getId());
                statement.setString(2, roleId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public void deletePurchasedRole(PurchasedRoles role, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("DELETE FROM players_purchased_roles WHERE user_id = ? AND role_id = ?")) {
                statement.setString(1, role.getUser().getId());
                statement.setString(2, role.getRoleId());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}