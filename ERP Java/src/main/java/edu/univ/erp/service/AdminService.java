package edu.univ.erp.service;

import edu.univ.erp.auth.PasswordUtil;
import edu.univ.erp.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class AdminService {

    public void createStudent(String username, String rawPassword, String rollNo, String program, int year, String name) throws Exception {
        int userId = createUserInAuth(username, rawPassword, "STUDENT");
        String sql = "INSERT INTO students (user_id, roll_no, program, current_year, first_name) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, rollNo);
            stmt.setString(3, program);
            stmt.setInt(4, year);
            stmt.setString(5, name);
            stmt.executeUpdate();
        }
    }

    public void createInstructor(String username, String rawPassword, String department, String fullName) throws Exception {
        int userId = createUserInAuth(username, rawPassword, "INSTRUCTOR");
        String sql = "INSERT INTO instructors (user_id, department, full_name) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, department);
            stmt.setString(3, fullName);
            stmt.executeUpdate();
        }
    }

    private int createUserInAuth(String username, String password, String role) throws Exception {
        String sql = "INSERT INTO users_auth (username, password_hash, role) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getAuthConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, username);
            stmt.setString(2, PasswordUtil.hashPassword(password));
            stmt.setString(3, role);

            int rows = stmt.executeUpdate();
            if (rows == 0) throw new Exception("Creating user failed, no rows affected.");

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new Exception("Creating user failed, no ID obtained.");
                }
            }
        }
    }

    public List<String[]> getAllUsers() {
        List<String[]> users = new ArrayList<>();

        String authSql = "SELECT user_id, username, role FROM users_auth WHERE role != 'ADMIN' ORDER BY user_id";

        String stuNameSql = "SELECT first_name FROM students WHERE user_id = ?";
        String instNameSql = "SELECT full_name FROM instructors WHERE user_id = ?";

        try (Connection authConn = DBConnection.getAuthConnection();
             Connection erpConn = DBConnection.getErpConnection();
             Statement authStmt = authConn.createStatement();
             ResultSet authRs = authStmt.executeQuery(authSql);
             PreparedStatement stuStmt = erpConn.prepareStatement(stuNameSql);
             PreparedStatement instStmt = erpConn.prepareStatement(instNameSql)) {

            while (authRs.next()) {
                String userId = authRs.getString("user_id");
                String username = authRs.getString("username");
                String role = authRs.getString("role");
                String name = "N/A";

                if ("STUDENT".equals(role)) {
                    stuStmt.setString(1, userId);
                    try (ResultSet rs = stuStmt.executeQuery()) {
                        if (rs.next()) name = rs.getString("first_name");
                    }
                } else if ("INSTRUCTOR".equals(role)) {
                    instStmt.setString(1, userId);
                    try (ResultSet rs = instStmt.executeQuery()) {
                        if (rs.next()) name = rs.getString("full_name");
                    }
                }

                users.add(new String[]{
                        userId,
                        username,
                        role,
                        name
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
        return users;
    }

    public void deleteUser(int userId, String role) throws Exception {
        Connection erpConn = DBConnection.getErpConnection();

        if ("STUDENT".equalsIgnoreCase(role)) {
            String checkSql = "SELECT COUNT(*) FROM enrollments WHERE student_id = ?";
            try (PreparedStatement checkStmt = erpConn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, userId);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new Exception("Cannot delete: Student has active enrollments.");
                }
            }
        }

        String profileTable = "STUDENT".equalsIgnoreCase(role) ? "students" : "instructors";
        String delProfileSql = "DELETE FROM " + profileTable + " WHERE user_id = ?";
        try (PreparedStatement stmt = erpConn.prepareStatement(delProfileSql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }

        String delAuthSql = "DELETE FROM users_auth WHERE user_id = ?";
        try (Connection authConn = DBConnection.getAuthConnection();
             PreparedStatement stmt = authConn.prepareStatement(delAuthSql)) {
            stmt.setInt(1, userId);
            int rows = stmt.executeUpdate();
            if (rows == 0) throw new Exception("User not found in Auth DB.");
        }
    }

    public void createCourse(String code, String title, int credits) throws Exception {
        String sql = "INSERT INTO courses (course_code, title, credits) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, code);
            stmt.setString(2, title);
            stmt.setInt(3, credits);
            stmt.executeUpdate();
        }
    }

    public void createSection(String courseCode, int instructorId, String dayTime, String room, int capacity, String semester, int year) throws Exception {
        String sql = "INSERT INTO sections (course_code, instructor_id, day_time, room, capacity, semester, year) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, courseCode);
            stmt.setInt(2, instructorId);
            stmt.setString(3, dayTime);
            stmt.setString(4, room);
            stmt.setInt(5, capacity);
            stmt.setString(6, semester);
            stmt.setInt(7, year);
            stmt.executeUpdate();
        }
    }

    public void updateSectionDetails(int sectionId, int instructorId, String room, String dayTime, int capacity) throws Exception {
        String sql = "UPDATE sections SET instructor_id = ?, room = ?, day_time = ?, capacity = ? WHERE section_id = ?";
        try (Connection conn = DBConnection.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, instructorId);
            stmt.setString(2, room);
            stmt.setString(3, dayTime);
            stmt.setInt(4, capacity);
            stmt.setInt(5, sectionId);
            stmt.executeUpdate();
        }
    }

    public List<String[]> getAllSectionsForAdmin() {
        List<String[]> list = new ArrayList<>();
        String sql = """
            SELECT s.section_id, c.course_code, c.title, i.full_name, s.day_time, s.room, s.capacity, s.instructor_id
            FROM sections s
            JOIN courses c ON s.course_code = c.course_code
            JOIN instructors i ON s.instructor_id = i.user_id
            ORDER BY c.course_code, s.section_id
        """;

        try (Connection conn = DBConnection.getErpConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new String[]{
                        rs.getString("section_id"),
                        rs.getString("course_code"),
                        rs.getString("title"),
                        rs.getString("full_name"),
                        rs.getString("day_time"),
                        rs.getString("room"),
                        rs.getString("capacity"),
                        rs.getString("instructor_id")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public List<String> getAllCourseCodes() {
        List<String> codes = new ArrayList<>();
        try (Connection conn = DBConnection.getErpConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT course_code, title FROM courses ORDER BY course_code")) {
            while (rs.next()) {
                codes.add(rs.getString("course_code") + " - " + rs.getString("title"));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return codes;
    }

    public List<String> getAllInstructors() {
        List<String> insts = new ArrayList<>();
        try (Connection conn = DBConnection.getErpConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT user_id, full_name FROM instructors ORDER BY full_name")) {
            while (rs.next()) {
                insts.add(rs.getInt("user_id") + " - " + rs.getString("full_name"));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return insts;
    }
}