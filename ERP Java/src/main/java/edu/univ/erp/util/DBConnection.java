package edu.univ.erp.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public final class DBConnection {

    private static final HikariDataSource AUTH_DS;
    private static final HikariDataSource ERP_DS;

    static {
        Properties props = new Properties();
        try (InputStream input = DBConnection.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new RuntimeException("Fatal Error: application.properties not found in classpath.");
            }
            props.load(input);
        } catch (Exception ex) {
            throw new ExceptionInInitializerError("Failed to load database configuration: " + ex.getMessage());
        }

        HikariConfig authCfg = new HikariConfig();
        authCfg.setJdbcUrl(props.getProperty("db.auth.url"));
        authCfg.setUsername(props.getProperty("db.username"));
        authCfg.setPassword(props.getProperty("db.password"));
        authCfg.setDriverClassName("com.mysql.cj.jdbc.Driver");
        authCfg.setMaximumPoolSize(10);
        AUTH_DS = new HikariDataSource(authCfg);

        HikariConfig erpCfg = new HikariConfig();
        erpCfg.setJdbcUrl(props.getProperty("db.erp.url"));
        erpCfg.setUsername(props.getProperty("db.username"));
        erpCfg.setPassword(props.getProperty("db.password"));
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
            System.err.println(label + ": Failure - " + e.getMessage());
        }
    }
}