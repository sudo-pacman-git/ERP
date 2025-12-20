package edu.univ.erp.ui;

import edu.univ.erp.domain.SectionRow;
import edu.univ.erp.domain.User;
import edu.univ.erp.service.StudentService;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

public class MyRegistrationsPanel extends JPanel {

    private final StudentService studentService;
    private final User currentUser;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private List<SectionRow> currentRows;

    public MyRegistrationsPanel(User user) {
        this.currentUser = user;
        this.studentService = new StudentService();

        setLayout(new MigLayout("fill, insets 20", "[grow]", "[][grow][]"));

        JLabel title = new JLabel("My Class Schedule & Grades");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        add(title, "wrap");

        String[] columns = {"Code", "Title", "Instructor", "Schedule", "Room", "Grade"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(25);

        add(new JScrollPane(table), "grow, wrap");

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadData());

        JButton dropBtn = new JButton("Drop Selected");
        dropBtn.setBackground(new Color(255, 200, 200));
        dropBtn.addActionListener(e -> handleDrop());


        JButton exportBtn = new JButton("Download Transcript (CSV)");
        exportBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        exportBtn.setIcon(UIManager.getIcon("FileView.floppyDriveIcon"));
        exportBtn.addActionListener(e -> handleExport());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(exportBtn);
        btnPanel.add(refreshBtn);
        btnPanel.add(dropBtn);
        add(btnPanel, "growx");

        loadData();
    }

    public void loadData() {
        tableModel.setRowCount(0);
        currentRows = studentService.getRegisteredSections(currentUser.getUserId());

        for (SectionRow row : currentRows) {
            tableModel.addRow(new Object[]{
                    row.getCourseCode(),
                    row.getTitle(),
                    row.getInstructorName(),
                    row.getSchedule(),
                    row.getRoom(),
                    row.getMyGrade() // Shows A, B, or -
            });
        }
    }

    private void handleDrop() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a course to drop.");
            return;
        }

        SectionRow data = currentRows.get(row);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to drop " + data.getCourseCode() + "?",
                "Confirm Drop", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                studentService.drop(currentUser.getUserId(), data.getSectionId());
                JOptionPane.showMessageDialog(this, "Success! Dropped " + data.getCourseCode());
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleExport() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Transcript");
        fileChooser.setSelectedFile(new File("transcript_" + currentUser.getUsername() + ".csv"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try {
                studentService.generateTranscript(currentUser.getUserId(), fileToSave);
                JOptionPane.showMessageDialog(this, "Transcript saved to: " + fileToSave.getAbsolutePath());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export Failed: " + ex.getMessage());
            }
        }
    }
}