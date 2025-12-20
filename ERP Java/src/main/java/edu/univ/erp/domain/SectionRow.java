package edu.univ.erp.domain;

public class SectionRow {
    private int sectionId;
    private String courseCode;
    private String title;
    private String instructorName;
    private String schedule;
    private String room;
    private int capacity;

    // NEW FIELD: Stores "A", "B", "95.0", or "-" if no grade yet
    private String myGrade = "-";

    public SectionRow(int sectionId, String courseCode, String title, String instructorName, String schedule, String room, int capacity) {
        this.sectionId = sectionId;
        this.courseCode = courseCode;
        this.title = title;
        this.instructorName = instructorName;
        this.schedule = schedule;
        this.room = room;
        this.capacity = capacity;
    }

    public void setMyGrade(String grade) {
        this.myGrade = grade;
    }

    public String getMyGrade() {
        return myGrade;
    }

    // Getters
    public int getSectionId() { return sectionId; }
    public String getCourseCode() { return courseCode; }
    public String getTitle() { return title; }
    public String getInstructorName() { return instructorName; }
    public String getSchedule() { return schedule; }
    public String getRoom() { return room; }
    public int getCapacity() { return capacity; }

    public Object[] toArray() {
        return new Object[]{courseCode, title, instructorName, schedule, room, capacity};
    }
}