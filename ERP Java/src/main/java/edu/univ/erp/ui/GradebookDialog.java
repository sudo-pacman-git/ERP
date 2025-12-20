package edu.univ.erp.ui;

import edu.univ.erp.domain.GradeRow;
import edu.univ.erp.service.InstructorService;
import edu.univ.erp.service.SettingsService;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class GradebookDialog extends JDialog {

    private final InstructorService service;
    private final SettingsService settingsService;
    private final int sectionId;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JLabel statsLabel;
    private List<GradeRow> rows;

    public GradebookDialog(Frame owner, int sectionId, String courseTitle) {
        super(owner, "Gradebook - " + courseTitle, true);
        this.sectionId = sectionId;
        this.service = new InstructorService();
        this.settingsService = new SettingsService();

        setLayout(new MigLayout("fill, insets 20", "[grow]", "[][grow][]"));
        setSize(700, 500);
        setLocationRelativeTo(owner);

        // Header
        JLabel title = new JLabel("Editing Grades: " + courseTitle);
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        add(title, "wrap");

        // Table
        String[] cols = {"Roll No", "Name", "Quiz (20)", "Midterm (30)", "Exam (50)", "Grade"};

        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 2 || col == 3 || col == 4;
            }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex >= 2 && columnIndex <= 4) return Double.class;
                return String.class;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        add(new JScrollPane(table), "grow, wrap");

        // Save Button
        JButton saveBtn = new JButton("Save All Grades");
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveBtn.setBackground(new Color(100, 200, 100));
        saveBtn.setForeground(Color.WHITE);

        saveBtn.addActionListener(e -> save());

        // Stats Label
        statsLabel = new JLabel("Class Average: Calculating...");
        statsLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statsLabel.setForeground(new Color(50, 50, 150));

        JPanel bottomPanel = new JPanel(new MigLayout("fillx, insets 0"));
        bottomPanel.add(statsLabel, "growx");
        bottomPanel.add(saveBtn, "right");

        add(bottomPanel, "growx");

        load();
    }

    private void load() {
        tableModel.setRowCount(0);
        rows = service.getGradebookData(sectionId);

        if (rows.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No students enrolled in this section yet.");
        }

        for (GradeRow r : rows) {
            tableModel.addRow(new Object[]{
                    r.getRollNo(),
                    r.getStudentName(),
                    r.getQuizScore(),
                    r.getMidtermScore(),
                    r.getExamScore(),
                    r.getLetter()
            });
        }

        updateStats();
    }

    private void save() {
        if (table.isEditing()) table.getCellEditor().stopCellEditing();

        try {
            // CRITICAL FIX: CHECK MAINTENANCE MODE FIRST
            if (settingsService.isMaintenanceModeOn()) {
                JOptionPane.showMessageDialog(this, "Operation blocked: System is in Maintenance Mode.",
                        "Access Denied", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // END CRITICAL FIX

            // 1. VALIDATION LOOP
            for (int i = 0; i < rows.size(); i++) {
                GradeRow rowObj = rows.get(i);

                double quiz = parseScore(table.getValueAt(i, 2));
                double mid = parseScore(table.getValueAt(i, 3));
                double exam = parseScore(table.getValueAt(i, 4));

                // Validation Rules (Max 20, 30, 50)
                if (quiz < 0 || quiz > 20) {
                    showError(rowObj.getStudentName(), "Quiz score must be between 0 and 20.");
                    return;
                }
                if (mid < 0 || mid > 30) {
                    showError(rowObj.getStudentName(), "Midterm score must be between 0 and 30.");
                    return;
                }
                if (exam < 0 || exam > 50) {
                    showError(rowObj.getStudentName(), "Exam score must be between 0 and 50.");
                    return;
                }
            }

            // 2. SAVING LOOP (Only runs if Maintenance is OFF and Validation Passes)
            for (int i = 0; i < rows.size(); i++) {
                GradeRow rowObj = rows.get(i);

                double quiz = parseScore(table.getValueAt(i, 2));
                double mid = parseScore(table.getValueAt(i, 3));
                double exam = parseScore(table.getValueAt(i, 4));

                service.saveGrade(rowObj.getEnrollmentId(), "Quiz", quiz);
                service.saveGrade(rowObj.getEnrollmentId(), "Midterm", mid);
                service.saveGrade(rowObj.getEnrollmentId(), "Exam", exam);
            }

            JOptionPane.showMessageDialog(this, "Grades Saved Successfully!");
            load(); // Reload to update letter grades and stats

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving: " + ex.getMessage());
        }
    }

    private void updateStats() {
        if (rows.isEmpty()) {
            statsLabel.setText("Class Average: N/A");
            return;
        }

        double totalSum = 0;
        for (GradeRow r : rows) {
            totalSum += (r.getQuizScore() + r.getMidtermScore() + r.getExamScore());
        }

        double average = totalSum / rows.size();
        statsLabel.setText(String.format("Class Average: %.2f%%", average));
    }

    private void showError(String student, String msg) {
        JOptionPane.showMessageDialog(this, "Error for " + student + ":\n" + msg, "Invalid Score", JOptionPane.ERROR_MESSAGE);
    }

    private double parseScore(Object obj) {
        if (obj == null) return 0.0;
        if (obj instanceof Number) return ((Number) obj).doubleValue();
        try { return Double.parseDouble(obj.toString()); } catch (Exception e) { return 0.0; }
    }
}