package edu.univ.erp.ui;

import edu.univ.erp.MainFrame;
import edu.univ.erp.domain.SectionRow;
import edu.univ.erp.domain.User;
import edu.univ.erp.service.InstructorService;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class InstructorDashboardPanel extends JPanel {

    private final InstructorService instructorService;
    private final User currentUser;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private List<SectionRow> currentRows;

    public InstructorDashboardPanel(MainFrame frame, User user) {
        this.currentUser = user;
        this.instructorService = new InstructorService();

        setLayout(new BorderLayout());

        // 1. Header (Purple)
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.setBackground(new Color(100, 50, 150));

        JLabel title = new JLabel("Instructor Portal - " + user.getUsername());
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        header.add(title);

        // CHANGE PASSWORD BUTTON
        JButton pwdBtn = new JButton("Change Password");
        pwdBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pwdBtn.addActionListener(e -> {
            JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
            new ChangePasswordDialog(parent, currentUser).setVisible(true);
        });
        header.add(pwdBtn);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> frame.showLogin());
        header.add(logoutBtn);

        add(header, BorderLayout.NORTH);

        // 2. Content
        JPanel content = new JPanel(new MigLayout("fill, insets 20", "[grow]", "[][grow][]"));
        content.add(new JLabel("My Assigned Sections"), "wrap");

        String[] columns = {"Code", "Title", "Schedule", "Room", "Capacity"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        content.add(new JScrollPane(table), "grow, wrap");

        JButton openGradebookBtn = new JButton("Open Gradebook");
        openGradebookBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        openGradebookBtn.addActionListener(e -> openGradebook());

        content.add(openGradebookBtn, "right");

        add(content, BorderLayout.CENTER);

        loadData();
    }

    private void loadData() {
        tableModel.setRowCount(0);
        currentRows = instructorService.getInstructorSections(currentUser.getUserId());
        for (SectionRow row : currentRows) {
            tableModel.addRow(new Object[]{
                    row.getCourseCode(), row.getTitle(), row.getSchedule(), row.getRoom(), row.getCapacity()
            });
        }
    }

    private void openGradebook() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a section to grade.");
            return;
        }
        SectionRow section = currentRows.get(row);
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        GradebookDialog dialog = new GradebookDialog(parentFrame, section.getSectionId(), section.getTitle());
        dialog.setVisible(true);
    }
}