package edu.univ.erp.util;

import org.mindrot.jbcrypt.BCrypt;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

public final class DatabaseSeeder {

    private DatabaseSeeder() {}

    public static void initializeSeedData() {
        Properties props = new Properties();
        try (InputStream input = DatabaseSeeder.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input != null) {
                props.load(input);
                seedUser(props.getProperty("seed.admin.username"), props.getProperty("seed.admin.password"), "ADMIN");
                seedUser(props.getProperty("seed.instructor.username"), props.getProperty("seed.instructor.password"), "INSTRUCTOR");
                seedUser(props.getProperty("seed.student.username"), props.getProperty("seed.student.password"), "STUDENT");
                System.out.println("System initialization complete: Seed users verified.");
            }
        } catch (Exception e) {
            System.err.println("Failed to run database seeder: " + e.getMessage());
        }
    }

    private static void seedUser(String username, String rawPassword, String role) {
        if (username == null || rawPassword == null) return;

        String checkSql = "SELECT COUNT(*) FROM users_auth WHERE username = ?";
        String insertSql = "INSERT INTO users_auth (username, password_hash, role) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getAuthConnection()) {
            
            // 1. Check if user already exists
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, username);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        return; // User exists, skip insertion
                    }
                }
            }

            // 2. Hash and insert if user does not exist
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                String hash = BCrypt.hashpw(rawPassword, BCrypt.gensalt(10));
                insertStmt.setString(1, username);
                insertStmt.setString(2, hash);
                insertStmt.setString(3, role);
                insertStmt.executeUpdate();
                System.out.println("Seeded new default user: " + username + " (" + role + ")");
            }

        } catch (Exception e) {
            System.err.println("Error seeding user " + username + ": " + e.getMessage());
        }
    }
}