package edu.univ.erp.ui;

import edu.univ.erp.service.AdminService;
import edu.univ.erp.service.AuthService;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;

public class ManageUsersPanel extends JPanel {

    private final AdminService adminService;
    private final AuthService authService;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private static final String TEMP_PASS = "temp123"; // Temporary password for reset

    public ManageUsersPanel() {
        this.adminService = new AdminService();
        this.authService = new AuthService();

        setLayout(new MigLayout("fill, insets 20", "[grow]", "[][grow][]"));


        JLabel title = new JLabel("All Students & Instructors");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        add(title, "wrap");


        String[] columns = {"ID", "Username", "Role", "Name"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Integer.class;
                return String.class;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(25);


        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        table.getColumnModel().getColumn(0).setCellRenderer(leftRenderer);


        add(new JScrollPane(table), "grow, wrap");

        JButton refreshBtn = new JButton("Refresh List");
        refreshBtn.addActionListener(e -> loadData());

        JButton deleteBtn = new JButton("Remove User");
        deleteBtn.setBackground(new Color(200, 100, 100));
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.addActionListener(e -> deleteUser());

        JButton resetBtn = new JButton("Force Reset Password");
        resetBtn.setBackground(new Color(255, 180, 50));
        resetBtn.addActionListener(e -> resetPassword());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(resetBtn);
        btnPanel.add(refreshBtn);
        btnPanel.add(deleteBtn);
        add(btnPanel, "growx");

        loadData();
    }

    public void loadData() {
        tableModel.setRowCount(0);
        List<String[]> users = adminService.getAllUsers();

        for (String[] user : users) {

            tableModel.addRow(new Object[]{
                    Integer.parseInt(user[0]),
                    user[1], // Username
                    user[2], // Role
                    user[3]  // Name
            });
        }
    }

    private void deleteUser() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a user to remove.");
            return;
        }

        int modelRow = table.convertRowIndexToModel(row);

        String userIdStr = tableModel.getValueAt(modelRow, 0).toString();
        String username = (String) tableModel.getValueAt(modelRow, 1);
        String role = (String) tableModel.getValueAt(modelRow, 2);
        int userId = Integer.parseInt(userIdStr);

        int confirm = JOptionPane.showConfirmDialog(this,
                "WARNING: Permanently remove " + username + " (" + role + ")?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                adminService.deleteUser(userId, role);
                JOptionPane.showMessageDialog(this, "User " + username + " removed.");
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Deletion Failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void resetPassword() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a user to reset.");
            return;
        }

        int modelRow = table.convertRowIndexToModel(row);

        String userIdStr = tableModel.getValueAt(modelRow, 0).toString();
        String username = (String) tableModel.getValueAt(modelRow, 1);
        int userId = Integer.parseInt(userIdStr);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Set temporary password ('" + TEMP_PASS + "') for " + username + "?",
                "Confirm Reset",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                authService.forceResetPassword(userId, TEMP_PASS);
                JOptionPane.showMessageDialog(this,
                        "Password reset successful! Temporary password is '" + TEMP_PASS + "'.\nUser must change it immediately.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Reset Failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}