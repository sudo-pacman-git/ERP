package edu.univ.erp.domain;

public class GradeRow {
    private int enrollmentId;
    private String rollNo;
    private String studentName;

    private double quizScore;    // Max 20
    private double midtermScore; // Max 30
    private double examScore;    // Max 50

    public GradeRow(int enrollmentId, String rollNo, String studentName) {
        this.enrollmentId = enrollmentId;
        this.rollNo = rollNo;
        this.studentName = studentName;
    }

    public void setQuizScore(double s) { this.quizScore = s; }
    public void setMidtermScore(double s) { this.midtermScore = s; }
    public void setExamScore(double s) { this.examScore = s; }

    public int getEnrollmentId() { return enrollmentId; }
    public String getRollNo() { return rollNo; }
    public String getStudentName() { return studentName; }
    public double getQuizScore() { return quizScore; }
    public double getMidtermScore() { return midtermScore; }
    public double getExamScore() { return examScore; }

    public String getLetter() {
        double total = quizScore + midtermScore + examScore; // Max 100
        if (total >= 90) return "A";
        if (total >= 80) return "B";
        if (total >= 70) return "C";
        if (total >= 60) return "D";
        return "F";
    }
}