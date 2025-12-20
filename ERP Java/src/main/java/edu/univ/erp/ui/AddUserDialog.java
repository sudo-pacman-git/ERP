package edu.univ.erp.ui;

import edu.univ.erp.service.AdminService;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public class AddUserDialog extends JDialog {

    private final AdminService adminService;

    public AddUserDialog(Frame owner) {
        super(owner, "Manage Users", true);
        this.adminService = new AdminService();

        setLayout(new BorderLayout());
        setSize(600, 500);
        setLocationRelativeTo(owner);

        JTabbedPane tabs = new JTabbedPane();


        tabs.addTab("1. Add New User", createAddUserTabs());


        tabs.addTab("2. View & Delete", new ManageUsersPanel());

        add(tabs, BorderLayout.CENTER);
    }

    private JTabbedPane createAddUserTabs() {
        JTabbedPane userTabs = new JTabbedPane();
        userTabs.addTab("Student", createStudentPanel());
        userTabs.addTab("Instructor", createInstructorPanel());
        return userTabs;
    }

    private JPanel createStudentPanel() {
        JPanel p = new JPanel(new MigLayout("fillx, wrap 2, insets 20", "[right][grow]"));

        JTextField userField = new JTextField();
        JPasswordField passField = new JPasswordField();
        JTextField nameField = new JTextField();
        JTextField rollField = new JTextField();
        JTextField programField = new JTextField();
        JSpinner yearSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 4, 1)); // Year 1-4

        p.add(new JLabel("Username:")); p.add(userField, "growx");
        p.add(new JLabel("Password:")); p.add(passField, "growx");
        p.add(new JSeparator(), "span, growx, gaptop 10, gapbottom 10");
        p.add(new JLabel("Full Name:")); p.add(nameField, "growx");
        p.add(new JLabel("Roll No:")); p.add(rollField, "growx");
        p.add(new JLabel("Program:")); p.add(programField, "growx");
        p.add(new JLabel("Year:")); p.add(yearSpinner, "width 60!");

        JButton saveBtn = new JButton("Create Student");
        saveBtn.addActionListener(e -> {
            try {
                adminService.createStudent(
                        userField.getText().trim(),
                        new String(passField.getPassword()),
                        rollField.getText().trim(),
                        programField.getText().trim(),
                        (Integer) yearSpinner.getValue(),
                        nameField.getText().trim()
                );
                JOptionPane.showMessageDialog(this, "Student Created Successfully!");

                // Refresh the table in the other tab (optional but good UX)
                if (getOwner() instanceof JFrame) {
                    JTabbedPane mainTabs = (JTabbedPane) SwingUtilities.getAncestorOfClass(JTabbedPane.class, this);
                    if (mainTabs != null && mainTabs.getTabCount() > 1 && mainTabs.getComponentAt(1) instanceof ManageUsersPanel) {
                        ((ManageUsersPanel) mainTabs.getComponentAt(1)).loadData();
                    }
                }

                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Creation Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        p.add(saveBtn, "span, center, gaptop 20");
        return p;
    }

    private JPanel createInstructorPanel() {
        JPanel p = new JPanel(new MigLayout("fillx, wrap 2, insets 20", "[right][grow]"));

        JTextField userField = new JTextField();
        JPasswordField passField = new JPasswordField();
        JTextField nameField = new JTextField();
        JTextField deptField = new JTextField();

        p.add(new JLabel("Username:")); p.add(userField, "growx");
        p.add(new JLabel("Password:")); p.add(passField, "growx");
        p.add(new JSeparator(), "span, growx, gaptop 10, gapbottom 10");
        p.add(new JLabel("Full Name:")); p.add(nameField, "growx");
        p.add(new JLabel("Department:")); p.add(deptField, "growx");

        JButton saveBtn = new JButton("Create Instructor");
        saveBtn.addActionListener(e -> {
            try {
                adminService.createInstructor(
                        userField.getText().trim(),
                        new String(passField.getPassword()),
                        deptField.getText().trim(),
                        nameField.getText().trim()
                );
                JOptionPane.showMessageDialog(this, "Instructor Created Successfully!");

                if (getOwner() instanceof JFrame) {
                    JTabbedPane mainTabs = (JTabbedPane) SwingUtilities.getAncestorOfClass(JTabbedPane.class, this);
                    if (mainTabs != null && mainTabs.getTabCount() > 1 && mainTabs.getComponentAt(1) instanceof ManageUsersPanel) {
                        ((ManageUsersPanel) mainTabs.getComponentAt(1)).loadData();
                    }
                }

                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Creation Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        p.add(saveBtn, "span, center, gaptop 20");
        return p;
    }
}