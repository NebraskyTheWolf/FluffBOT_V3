/*
---------------------------------------------------------------------------------
File Name : ApplicationManager

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 14/06/2024
Last Modified : 14/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.database.datamanager;

import eu.fluffici.bot.api.beans.app.ApplicationModuleBuilder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
public class ApplicationManager {
    public ApplicationModuleBuilder fetchModule(String slug, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT * FROM application_module WHERE slug = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, slug);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return new ApplicationModuleBuilder(
                                resultSet.getString("slug"),
                                resultSet.getBoolean("is_enabled")
                        );
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new ApplicationModuleBuilder(
                slug,
                false
        );
    }

    public boolean hasMaintenanceRecord(int id, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {

            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM maintenance_record WHERE maintenance_id = ?")) {
                statement.setInt(1, id);

                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public void addMaintenanceRecord(int id, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO maintenance_record (maintenance_id) VALUES (?)")) {
                statement.setInt(1, id);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}