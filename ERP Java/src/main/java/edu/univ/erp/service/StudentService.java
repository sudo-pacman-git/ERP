package edu.univ.erp.service;

import edu.univ.erp.domain.SectionRow;
import edu.univ.erp.util.DBConnection;

import java.io.File;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class StudentService {

    public List<SectionRow> getAvailableSections() {
        List<SectionRow> list = new ArrayList<>();
        String sql = """
            SELECT s.section_id, c.course_code, c.title, i.full_name, s.day_time, s.room, s.capacity
            FROM sections s
            JOIN courses c ON s.course_code = c.course_code
            LEFT JOIN instructors i ON s.instructor_id = i.user_id
            ORDER BY c.course_code
        """;

        try (Connection conn = DBConnection.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(new SectionRow(
                        rs.getInt("section_id"),
                        rs.getString("course_code"),
                        rs.getString("title"),
                        rs.getString("full_name"),
                        rs.getString("day_time"),
                        rs.getString("room"),
                        rs.getInt("capacity")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public void register(int studentId, int sectionId) throws Exception {
        Connection conn = DBConnection.getErpConnection();

        String maintSql = "SELECT setting_value FROM settings WHERE setting_key='maintenance_on'";
        try (PreparedStatement stmt = conn.prepareStatement(maintSql)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && "true".equalsIgnoreCase(rs.getString("setting_value"))) {
                throw new Exception("Registration is blocked: Maintenance Mode is ON.");
            }
        }

        String checkSql = "SELECT enrollment_id FROM enrollments WHERE student_id = ? AND section_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(checkSql)) {
            stmt.setInt(1, studentId);
            stmt.setInt(2, sectionId);
            if (stmt.executeQuery().next()) throw new Exception("You are already registered!");
        }

        int capacity = 0;
        int enrolled = 0;
        String capSql = "SELECT capacity FROM sections WHERE section_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(capSql)) {
            stmt.setInt(1, sectionId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) capacity = rs.getInt("capacity");
        }
        String countSql = "SELECT COUNT(*) FROM enrollments WHERE section_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(countSql)) {
            stmt.setInt(1, sectionId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) enrolled = rs.getInt(1);
        }

        if (enrolled >= capacity) throw new Exception("Registration failed: Section is FULL.");

        String insertSql = "INSERT INTO enrollments (student_id, section_id) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
            stmt.setInt(1, studentId);
            stmt.setInt(2, sectionId);
            stmt.executeUpdate();
        }
    }

    public List<SectionRow> getRegisteredSections(int studentId) {
        List<SectionRow> list = new ArrayList<>();

        String sql = """
            SELECT s.section_id, c.course_code, c.title, i.full_name, s.day_time, s.room, s.capacity, e.enrollment_id,
                   (SELECT SUM(score) FROM grades g WHERE g.enrollment_id = e.enrollment_id) as total_score
            FROM enrollments e
            JOIN sections s ON e.section_id = s.section_id
            JOIN courses c ON s.course_code = c.course_code
            LEFT JOIN instructors i ON s.instructor_id = i.user_id
            WHERE e.student_id = ?
            ORDER BY s.day_time
        """;

        try (Connection conn = DBConnection.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    SectionRow row = new SectionRow(
                            rs.getInt("section_id"),
                            rs.getString("course_code"),
                            rs.getString("title"),
                            rs.getString("full_name"),
                            rs.getString("day_time"),
                            rs.getString("room"),
                            rs.getInt("capacity")
                    );

                    double score = rs.getDouble("total_score");
                    if (rs.wasNull()) {
                        row.setMyGrade("-");
                    } else {
                        row.setMyGrade(calculateLetter(score));
                    }

                    list.add(row);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public void drop(int studentId, int sectionId) throws Exception {
        Connection conn = DBConnection.getErpConnection();

        String maintSql = "SELECT setting_value FROM settings WHERE setting_key='maintenance_on'";
        try (PreparedStatement stmt = conn.prepareStatement(maintSql)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && "true".equalsIgnoreCase(rs.getString("setting_value"))) {
                throw new Exception("Dropping is blocked: Maintenance Mode is ON.");
            }
        }

        Integer enrollmentId = null;
        String findSql = "SELECT enrollment_id FROM enrollments WHERE student_id = ? AND section_id = ?";
        try (PreparedStatement findStmt = conn.prepareStatement(findSql)) {
            findStmt.setInt(1, studentId);
            findStmt.setInt(2, sectionId);
            ResultSet rs = findStmt.executeQuery();
            if (rs.next()) {
                enrollmentId = rs.getInt("enrollment_id");
            } else {
                throw new Exception("You are not enrolled in this course.");
            }
        }

        if (enrollmentId != null) {
            String delGradesSql = "DELETE FROM grades WHERE enrollment_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(delGradesSql)) {
                stmt.setInt(1, enrollmentId);
                stmt.executeUpdate();
            }
        }

        String delEnrollmentSql = "DELETE FROM enrollments WHERE student_id = ? AND section_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(delEnrollmentSql)) {
            stmt.setInt(1, studentId);
            stmt.setInt(2, sectionId);
            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new Exception("Error: Enrollment record was not found for deletion.");
            }
        }
    }

    public void generateTranscript(int studentId, File file) throws Exception {
        List<SectionRow> sections = getRegisteredSections(studentId);

        try (PrintWriter writer = new PrintWriter(file)) {
            writer.println("Course Code,Course Title,Instructor,Schedule,Grade");

            for (SectionRow row : sections) {
                writer.printf("%s,%s,%s,%s,%s%n",
                        row.getCourseCode(),
                        row.getTitle(),
                        row.getInstructorName(),
                        row.getSchedule(),
                        row.getMyGrade()
                );
            }
        }
    }

    private String calculateLetter(double total) {
        if (total >= 90) return "A";
        if (total >= 80) return "B";
        if (total >= 70) return "C";
        if (total >= 60) return "D";
        return "F";
    }
}