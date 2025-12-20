package edu.univ.erp.service;

import edu.univ.erp.auth.PasswordUtil;
import edu.univ.erp.domain.User;
import edu.univ.erp.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthService {

    public User login(String username, String rawPassword) {
        String sql = "SELECT user_id, username, role, password_hash FROM users_auth WHERE username = ?";

        try (Connection conn = DBConnection.getAuthConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("user_id");
                    String role = rs.getString("role");
                    String storedHash = rs.getString("password_hash");

                    if (PasswordUtil.checkPassword(rawPassword, storedHash)) {
                        return new User(id, username, role, storedHash);
                    } else {
                        throw new RuntimeException("Invalid Password");
                    }
                } else {
                    throw new RuntimeException("User not found");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Database Error");
        }
    }

    public void changePassword(int userId, String oldPass, String newPass) throws Exception {
        Connection conn = DBConnection.getAuthConnection();

        String checkSql = "SELECT password_hash FROM users_auth WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(checkSql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String currentHash = rs.getString("password_hash");
                if (!PasswordUtil.checkPassword(oldPass, currentHash)) {
                    throw new Exception("Old password is incorrect.");
                }
            } else {
                throw new Exception("User not found.");
            }
        }


        String updateSql = "UPDATE users_auth SET password_hash = ? WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
            stmt.setString(1, PasswordUtil.hashPassword(newPass));
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }

    public void forceResetPassword(int userId, String newPass) throws Exception {
        String updateSql = "UPDATE users_auth SET password_hash = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getAuthConnection();
             PreparedStatement stmt = conn.prepareStatement(updateSql)) {
            stmt.setString(1, PasswordUtil.hashPassword(newPass));
            stmt.setInt(2, userId);
            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new Exception("User ID not found in authentication database.");
            }
        }
    }

    public static void main(String[] args) {
        System.out.println(PasswordUtil.hashPassword("password123"));
    }
}