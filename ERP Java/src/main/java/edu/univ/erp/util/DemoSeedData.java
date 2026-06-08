package edu.univ.erp.util;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class DemoSeedData {

    public static void main(String[] args) {
        try {
            //  Insert Instructors
            int instSambuddho = seedInstructor("sambuddho", "password123", "Computer Science", "Dr. Sambuddho");
            int instSubhajit = seedInstructor("subhajit", "password123", "Mathematics", "Dr. Subhajit");
            int instOjaswa = seedInstructor("ojaswa", "password123", "Computer Science", "Dr. Ojaswa Sharma");
            int instPravesh = seedInstructor("pravesh", "password123", "Electronics", "Dr. Pravesh Biyani");
            int instAnubha = seedInstructor("anubha", "password123", "Computer Science", "Dr. Anubha Gupta");

            // Insert Courses
            seedCourse("CSE201", "Advanced Programming", 4);
            seedCourse("MTH100", "Linear Algebra", 4);
            seedCourse("CSE102", "Data Structures and Algorithms", 4);
            seedCourse("ECE111", "Digital Circuits", 4);
            seedCourse("CSE343", "Machine Learning", 4);

        
            int secAP = seedSection("CSE201", instSambuddho, "Mon/Wed 08:30 AM", "C01", 150, "Monsoon", 2024);
            int secLA = seedSection("MTH100", instSubhajit, "Tue/Thu 10:00 AM", "C02", 150, "Monsoon", 2024);
            int secDSA = seedSection("CSE102", instOjaswa, "Mon/Wed 11:30 AM", "C01", 150, "Monsoon", 2024);
            int secDC = seedSection("ECE111", instPravesh, "Tue/Thu 02:30 PM", "L31", 100, "Monsoon", 2024);
            int secML = seedSection("CSE343", instAnubha, "Fri 09:00 AM", "C11", 60, "Monsoon", 2024);

        
            String[] studentNames = {
                "Akash", "Aniket", "Harsh", "Aditya Gautam", 
                "Rohan Sharma", "Priya Singh", "Rahul Kumar", "Sneha Gupta",
                "Aman Verma", "Neha Patel", "Karan Singh", "Pooja Das",
                "Vikas Yadav", "Anjali Tiwari", "Rohit Malhotra", "Shreya Iyer",
                "Kunal Desai", "Megha Reddy", "Aryan Jain", "Riya Kapoor"
            };

            int[] studentIds = new int[20];
            for (int i = 0; i < studentNames.length; i++) {
                // Generate username from first name + index (e.g., akash1, aniket2)
                String username = studentNames[i].split(" ")[0].toLowerCase() + (i + 1);
                String rollNo = "MT2024" + String.format("%03d", i + 1);
                studentIds[i] = seedStudent(username, "password123", rollNo, "B.Tech CS", 1, studentNames[i]);
            }

        
        
            for (int i = 0; i < studentIds.length; i++) {
                int sId = studentIds[i];

        
                int enrAP = seedEnrollment(sId, secAP);
                seedGrade(enrAP, "Quiz", 12.0 + (i % 8));       // Max 20
                seedGrade(enrAP, "Midterm", 18.0 + (i % 12));   // Max 30
                seedGrade(enrAP, "Exam", 35.0 + (i % 15));      // Max 50

            
                int enrLA = seedEnrollment(sId, secLA);
                seedGrade(enrLA, "Quiz", 10.0 + (i % 10));
                seedGrade(enrLA, "Midterm", 15.0 + (i % 15));
                seedGrade(enrLA, "Exam", 30.0 + (i % 20));

            
                if (i % 2 == 0) {
                    int enrDSA = seedEnrollment(sId, secDSA);
                    seedGrade(enrDSA, "Quiz", 15.0 + (i % 5));
                    seedGrade(enrDSA, "Midterm", 20.0 + (i % 10));
                    seedGrade(enrDSA, "Exam", 40.0 + (i % 10));
                } else {
                    int enrDC = seedEnrollment(sId, secDC);
                    seedGrade(enrDC, "Quiz", 14.0 + (i % 6));
                    seedGrade(enrDC, "Midterm", 22.0 + (i % 8));
                    seedGrade(enrDC, "Exam", 38.0 + (i % 12));
                }

        
                if (i < 5) {
                    int enrML = seedEnrollment(sId, secML);
                    seedGrade(enrML, "Quiz", 18.0);
                    seedGrade(enrML, "Midterm", 28.0);

                }
            }

            System.out.println("Demo Data Seeding Completed Successfully.");

        } catch (Exception e) {
            System.err.println("Seeding failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static int seedAuthUser(String username, String password, String role) throws Exception {
        String checkSql = "SELECT user_id FROM users_auth WHERE username = ?";
        try (Connection conn = DBConnection.getAuthConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, username);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) return rs.getInt("user_id");
            }

            String insertSql = "INSERT INTO users_auth (username, password_hash, role) VALUES (?, ?, ?)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                insertStmt.setString(1, username);
                insertStmt.setString(2, BCrypt.hashpw(password, BCrypt.gensalt(10)));
                insertStmt.setString(3, role);
                insertStmt.executeUpdate();
                try (ResultSet keys = insertStmt.getGeneratedKeys()) {
                    if (keys.next()) return keys.getInt(1);
                }
            }
        }
        throw new Exception("Failed to retrieve generated ID for user: " + username);
    }

    private static int seedInstructor(String username, String password, String dept, String name) throws Exception {
        int userId = seedAuthUser(username, password, "INSTRUCTOR");
        String sql = "INSERT IGNORE INTO instructors (user_id, department, full_name) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, dept);
            stmt.setString(3, name);
            stmt.executeUpdate();
        }
        return userId;
    }

    private static int seedStudent(String username, String password, String rollNo, String program, int year, String name) throws Exception {
        int userId = seedAuthUser(username, password, "STUDENT");
        String sql = "INSERT IGNORE INTO students (user_id, roll_no, program, current_year, first_name) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, rollNo);
            stmt.setString(3, program);
            stmt.setInt(4, year);
            stmt.setString(5, name);
            stmt.executeUpdate();
        }
        return userId;
    }

    private static void seedCourse(String code, String title, int credits) throws Exception {
        String sql = "INSERT IGNORE INTO courses (course_code, title, credits) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, code);
            stmt.setString(2, title);
            stmt.setInt(3, credits);
            stmt.executeUpdate();
        }
    }

    private static int seedSection(String courseCode, int instId, String dayTime, String room, int capacity, String semester, int year) throws Exception {
        String checkSql = "SELECT section_id FROM sections WHERE course_code = ? AND instructor_id = ? AND day_time = ?";
        try (Connection conn = DBConnection.getErpConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, courseCode);
            checkStmt.setInt(2, instId);
            checkStmt.setString(3, dayTime);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) return rs.getInt("section_id");
            }

            String insertSql = "INSERT INTO sections (course_code, instructor_id, day_time, room, capacity, semester, year) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                insertStmt.setString(1, courseCode);
                insertStmt.setInt(2, instId);
                insertStmt.setString(3, dayTime);
                insertStmt.setString(4, room);
                insertStmt.setInt(5, capacity);
                insertStmt.setString(6, semester);
                insertStmt.setInt(7, year);
                insertStmt.executeUpdate();
                try (ResultSet keys = insertStmt.getGeneratedKeys()) {
                    if (keys.next()) return keys.getInt(1);
                }
            }
        }
        throw new Exception("Failed to seed section for course: " + courseCode);
    }

    private static int seedEnrollment(int studentId, int sectionId) throws Exception {
        String checkSql = "SELECT enrollment_id FROM enrollments WHERE student_id = ? AND section_id = ?";
        try (Connection conn = DBConnection.getErpConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setInt(1, studentId);
            checkStmt.setInt(2, sectionId);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) return rs.getInt("enrollment_id");
            }

            String insertSql = "INSERT INTO enrollments (student_id, section_id) VALUES (?, ?)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                insertStmt.setInt(1, studentId);
                insertStmt.setInt(2, sectionId);
                insertStmt.executeUpdate();
                try (ResultSet keys = insertStmt.getGeneratedKeys()) {
                    if (keys.next()) return keys.getInt(1);
                }
            }
        }
        throw new Exception("Failed to seed enrollment.");
    }

    private static void seedGrade(int enrollmentId, String component, double score) throws Exception {
        String sql = "INSERT IGNORE INTO grades (enrollment_id, component, score) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, enrollmentId);
            stmt.setString(2, component);
            stmt.setDouble(3, score);
            stmt.executeUpdate();
        }
    }
}