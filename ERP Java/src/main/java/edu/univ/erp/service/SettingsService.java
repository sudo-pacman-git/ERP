package edu.univ.erp.service;

import edu.univ.erp.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SettingsService {

    public boolean isMaintenanceModeOn() {
        String sql = "SELECT setting_value FROM settings WHERE setting_key = 'maintenance_on'";

        try (Connection conn = DBConnection.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return "true".equalsIgnoreCase(rs.getString("setting_value"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void setMaintenanceMode(boolean isOn) {
        String sql = "UPDATE settings SET setting_value = ? WHERE setting_key = 'maintenance_on'";

        try (Connection conn = DBConnection.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, isOn ? "true" : "false");
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not save settings");
        }
    }
}