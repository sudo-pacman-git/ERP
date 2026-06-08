package edu.univ.erp.service;

import edu.univ.erp.domain.GradeRow;
import edu.univ.erp.domain.SectionRow;
import edu.univ.erp.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class InstructorService {

    public List<SectionRow> getInstructorSections(int instructorId) {
        List<SectionRow> list = new ArrayList<>();

        String sql = """
            SELECT s.section_id, c.course_code, c.title, s.day_time, s.room, s.capacity
            FROM sections s
            JOIN courses c ON s.course_code = c.course_code
            WHERE s.instructor_id = ?
            ORDER BY s.day_time
        """;

        try (Connection conn = DBConnection.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, instructorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new SectionRow(
                            rs.getInt("section_id"),
                            rs.getString("course_code"),
                            rs.getString("title"),
                            "Me",
                            rs.getString("day_time"),
                            rs.getString("room"),
                            rs.getInt("capacity")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<GradeRow> getGradebookData(int sectionId) {
        List<GradeRow> rows = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DBConnection.getErpConnection();

            
            String stuSql = """
                SELECT e.enrollment_id, s.roll_no, s.first_name 
                FROM enrollments e
                JOIN students s ON e.student_id = s.user_id
                WHERE e.section_id = ?
                ORDER BY s.roll_no
            """;

            try (PreparedStatement stmt = conn.prepareStatement(stuSql)) {
                stmt.setInt(1, sectionId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    
                    String realName = rs.getString("first_name");
                    if (realName == null || realName.isEmpty()) realName = "Student";

                    rows.add(new GradeRow(
                            rs.getInt("enrollment_id"),
                            rs.getString("roll_no"),
                            realName
                    ));
                }
            }

            String gradeSql = "SELECT component, score FROM grades WHERE enrollment_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(gradeSql)) {
                for (GradeRow row : rows) {
                    stmt.setInt(1, row.getEnrollmentId());
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        String comp = rs.getString("component");
                        double score = rs.getDouble("score");

                        if ("Quiz".equalsIgnoreCase(comp)) row.setQuizScore(score);
                        if ("Midterm".equalsIgnoreCase(comp)) row.setMidtermScore(score);
                        if ("Exam".equalsIgnoreCase(comp)) row.setExamScore(score);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return rows;
    }

    public void saveGrade(int enrollmentId, String component, double score) {
        String updateSql = "UPDATE grades SET score = ? WHERE enrollment_id = ? AND component = ?";
        String insertSql = "INSERT INTO grades (enrollment_id, component, score) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getErpConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                stmt.setDouble(1, score);
                stmt.setInt(2, enrollmentId);
                stmt.setString(3, component);
                int rows = stmt.executeUpdate();

                if (rows == 0) {
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                        insertStmt.setInt(1, enrollmentId);
                        insertStmt.setString(2, component);
                        insertStmt.setDouble(3, score);
                        insertStmt.executeUpdate();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}