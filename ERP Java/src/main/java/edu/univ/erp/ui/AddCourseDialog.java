package edu.univ.erp.ui;

import edu.univ.erp.service.AdminService;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AddCourseDialog extends JDialog {

    private final AdminService adminService;
    private JComboBox<String> courseCombo;
    private JComboBox<String> instructorCombo;
    private final DefaultTableModel allSectionsTableModel;
    private final JTable allSectionsTable;
    private final Frame ownerFrame;

    public AddCourseDialog(Frame owner) {
        super(owner, "Manage Courses & Sections", true);
        this.adminService = new AdminService();
        this.ownerFrame = owner; 

        setLayout(new BorderLayout());
        setSize(850, 600);
        setLocationRelativeTo(owner);

        String[] adminCols = {"ID", "Code", "Title", "Instructor", "Day/Time", "Room", "Capacity", "Inst ID"};
        allSectionsTableModel = new DefaultTableModel(adminCols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        allSectionsTable = new JTable(allSectionsTableModel);
        allSectionsTable.setRowHeight(25);

        // Setup Tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("1. Create Course", createCoursePanel());
        tabs.addTab("2. Create Section", createSectionPanel());
        tabs.addTab("3. Edit Existing Sections", createEditSectionPanel());

        add(tabs, BorderLayout.CENTER);

        refreshAllSectionsTable();
    }


    private JPanel createCoursePanel() {
        JPanel p = new JPanel(new MigLayout("fillx, wrap 2, insets 20", "[right][grow]"));

        JTextField codeField = new JTextField();
        JTextField titleField = new JTextField();
        JSpinner creditSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 10, 1));

        p.add(new JLabel("Course Code (e.g. CS102):")); p.add(codeField, "growx");
        p.add(new JLabel("Course Title:")); p.add(titleField, "growx");
        p.add(new JLabel("Credits:")); p.add(creditSpinner, "width 60!");

        JButton saveBtn = new JButton("Create Course");
        saveBtn.addActionListener(e -> {
            try {
                adminService.createCourse(
                        codeField.getText().trim(),
                        titleField.getText().trim(),
                        (Integer) creditSpinner.getValue()
                );
                JOptionPane.showMessageDialog(this, "Course Created Successfully!");
                refreshDropdowns();
                refreshAllSectionsTable();
                codeField.setText(""); titleField.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        p.add(saveBtn, "span, center, gaptop 20");
        return p;
    }


    private JPanel createSectionPanel() {
        JPanel p = new JPanel(new MigLayout("fillx, wrap 2, insets 20", "[right][grow]"));

        courseCombo = new JComboBox<>();
        instructorCombo = new JComboBox<>();
        refreshDropdowns();

        JTextField scheduleField = new JTextField("Mon/Wed 10:00 AM");
        JTextField roomField = new JTextField();
        JSpinner capSpinner = new JSpinner(new SpinnerNumberModel(30, 1, 500, 1));
        JTextField semField = new JTextField("Spring");
        JTextField yearField = new JTextField("2026");

        p.add(new JLabel("Select Course:")); p.add(courseCombo, "growx");
        p.add(new JLabel("Select Instructor:")); p.add(instructorCombo, "growx");
        p.add(new JLabel("Day/Time:")); p.add(scheduleField, "growx");
        p.add(new JLabel("Room:")); p.add(roomField, "growx");
        p.add(new JLabel("Capacity:")); p.add(capSpinner, "width 60!");
        p.add(new JLabel("Semester:")); p.add(semField, "growx");
        p.add(new JLabel("Year:")); p.add(yearField, "growx");

        JButton saveBtn = new JButton("Create Section");
        saveBtn.addActionListener(e -> {
            try {
                String rawCourse = (String) courseCombo.getSelectedItem();
                String rawInst = (String) instructorCombo.getSelectedItem();

                if (rawCourse == null || rawInst == null) throw new Exception("Select course/instructor first.");

                String courseCode = rawCourse.split(" - ")[0];
                int instructorId = Integer.parseInt(rawInst.split(" - ")[0]);

                adminService.createSection(
                        courseCode, instructorId,
                        scheduleField.getText(), roomField.getText(),
                        (Integer) capSpinner.getValue(), semField.getText(),
                        Integer.parseInt(yearField.getText())
                );
                JOptionPane.showMessageDialog(this, "Section Created Successfully!");
                refreshAllSectionsTable();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        p.add(saveBtn, "span, center, gaptop 20");
        return p;
    }


    private JPanel createEditSectionPanel() {
        JPanel p = new JPanel(new MigLayout("fill, wrap 1", "[grow]", "[grow][]"));

        p.add(new JLabel("Select a section to edit the instructor, room, or schedule:"), "wrap");

        JScrollPane scrollPane = new JScrollPane(allSectionsTable);
        p.add(scrollPane, "grow, wrap 10");

        JButton editBtn = new JButton("Edit Selected Section");
        editBtn.addActionListener(e -> editSelectedSection());

        p.add(editBtn, "right");

        return p;
    }


    private void editSelectedSection() {
        int row = allSectionsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a section to edit.");
            return;
        }

        String sectionIdStr = (String) allSectionsTableModel.getValueAt(row, 0);
        String courseTitle = (String) allSectionsTableModel.getValueAt(row, 2);
        int sectionId = Integer.parseInt(sectionIdStr);

        EditSectionDialog dialog = new EditSectionDialog(ownerFrame, this.adminService, sectionId, courseTitle);
        dialog.setVisible(true);

        refreshAllSectionsTable();
    }


    private void refreshDropdowns() {
        courseCombo.removeAllItems();
        instructorCombo.removeAllItems();

        List<String> courses = adminService.getAllCourseCodes();
        for (String c : courses) courseCombo.addItem(c);

        List<String> insts = adminService.getAllInstructors();
        for (String i : insts) instructorCombo.addItem(i);
    }

    private void refreshAllSectionsTable() {
        allSectionsTableModel.setRowCount(0);
        List<String[]> sections = adminService.getAllSectionsForAdmin();
        for (String[] section : sections) {
            allSectionsTableModel.addRow(new String[]{
                    section[0], // ID
                    section[1], // Code
                    section[2], // Title
                    section[3], // Full Name
                    section[4], // Day/Time
                    section[5], // Room
                    section[6]  // Capacity
            });
        }
    }
}