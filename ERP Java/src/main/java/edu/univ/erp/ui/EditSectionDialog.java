package edu.univ.erp.ui;

import edu.univ.erp.service.AdminService;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class EditSectionDialog extends JDialog {

    private final AdminService adminService;
    private final int sectionId;

    public EditSectionDialog(Frame owner, AdminService service, int sectionId, String courseTitle) {
        super(owner, "Edit Section: " + courseTitle, true);
        this.adminService = service;
        this.sectionId = sectionId;

        setLayout(new MigLayout("fillx, wrap 2, insets 20", "[right][grow]"));
        setSize(450, 350);
        setLocationRelativeTo(owner);

        add(new JLabel("Editing Section ID: " + sectionId + " (" + courseTitle + ")"), "span 2, center, wrap 20");

        JComboBox<String> instructorCombo = new JComboBox<>();
        JTextField roomField = new JTextField();
        JTextField scheduleField = new JTextField();
        JSpinner capSpinner = new JSpinner(new SpinnerNumberModel(30, 1, 500, 1));


        loadSectionData(instructorCombo, roomField, scheduleField, capSpinner);

        add(new JLabel("Reassign Instructor:")); add(instructorCombo, "growx");
        add(new JLabel("New Room:")); add(roomField, "growx");
        add(new JLabel("New Schedule:")); add(scheduleField, "growx");
        add(new JLabel("New Capacity:")); add(capSpinner, "width 60!");

        JButton saveBtn = new JButton("Save Changes");
        saveBtn.addActionListener(e -> saveChanges(instructorCombo, roomField, scheduleField, capSpinner));

        add(saveBtn, "span 2, center, gaptop 20");
    }


    private void loadSectionData(JComboBox<String> instructorCombo, JTextField roomField, JTextField scheduleField, JSpinner capSpinner) {
        List<String[]> allSections = adminService.getAllSectionsForAdmin();


        List<String> insts = adminService.getAllInstructors();
        for (String i : insts) instructorCombo.addItem(i);

        String initialInstructorId = null;
        for (String[] section : allSections) {
            if (section[0].equals(String.valueOf(sectionId))) {
                roomField.setText(section[5]);
                scheduleField.setText(section[4]);
                capSpinner.setValue(Integer.parseInt(section[6]));
                initialInstructorId = section[7];
                break;
            }
        }

        // Select the current instructor in the dropdown
        if (initialInstructorId != null) {
            for (int i = 0; i < instructorCombo.getItemCount(); i++) {
                if (instructorCombo.getItemAt(i).startsWith(initialInstructorId + " -")) {
                    instructorCombo.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void saveChanges(JComboBox<String> instructorCombo, JTextField roomField, JTextField scheduleField, JSpinner capSpinner) {
        try {
            String rawInst = (String) instructorCombo.getSelectedItem();
            if (rawInst == null) throw new Exception("Instructor selection missing.");
            int newInstructorId = Integer.parseInt(rawInst.split(" - ")[0]);

            adminService.updateSectionDetails(
                    sectionId,
                    newInstructorId,
                    roomField.getText().trim(),
                    scheduleField.getText().trim(),
                    (Integer) capSpinner.getValue()
            );

            JOptionPane.showMessageDialog(this, "Section details updated successfully!");
            dispose(); // Close dialog

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Update Failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}