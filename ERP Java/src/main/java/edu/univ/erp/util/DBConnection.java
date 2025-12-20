package edu.univ.erp.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public final class DBConnection {

    private static final HikariDataSource AUTH_DS;
    private static final HikariDataSource ERP_DS;

    static {
        HikariConfig authCfg = new HikariConfig();
        authCfg.setJdbcUrl("jdbc:mysql://localhost:3306/university_auth");
        authCfg.setUsername("root");
        authCfg.setPassword("password123");
        authCfg.setDriverClassName("com.mysql.cj.jdbc.Driver");
        authCfg.setMaximumPoolSize(10);
        AUTH_DS = new HikariDataSource(authCfg);

        HikariConfig erpCfg = new HikariConfig();
        erpCfg.setJdbcUrl("jdbc:mysql://localhost:3306/university_erp");
        erpCfg.setUsername("root");
        erpCfg.setPassword("password123");
        erpCfg.setDriverClassName("com.mysql.cj.jdbc.Driver");
        erpCfg.setMaximumPoolSize(10);
        ERP_DS = new HikariDataSource(erpCfg);
    }

    private DBConnection() {}

    public static Connection getAuthConnection() throws SQLException {
        return AUTH_DS.getConnection();
    }

    public static Connection getErpConnection() throws SQLException {
        return ERP_DS.getConnection();
    }

    public static void main(String[] args) {
        test("Auth DB");
        test("ERP DB");
    }

    private static void test(String label) {
        try (Connection conn = "Auth DB".equals(label) ? getAuthConnection() : getErpConnection()) {
            boolean ok = conn != null && conn.isValid(2);
            System.out.println(ok ? (label + ": Success") : (label + ": Failure"));
        } catch (Exception e) {
            System.out.println(label + ": Failure");
            e.printStackTrace();
        }
    }
}