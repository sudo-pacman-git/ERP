package edu.univ.erp.ui;

import edu.univ.erp.domain.User;
import edu.univ.erp.service.AuthService;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public class ChangePasswordDialog extends JDialog {

    private final AuthService authService;
    private final User user;

    public ChangePasswordDialog(Frame owner, User user) {
        super(owner, "Change Password", true);
        this.user = user;
        this.authService = new AuthService();

        setLayout(new MigLayout("fill, insets 20, wrap 2", "[right][grow]"));
        setSize(400, 250);
        setLocationRelativeTo(owner);

        JPasswordField oldPass = new JPasswordField();
        JPasswordField newPass = new JPasswordField();
        JPasswordField confirmPass = new JPasswordField();

        add(new JLabel("Old Password:")); add(oldPass, "growx");
        add(new JLabel("New Password:")); add(newPass, "growx");
        add(new JLabel("Confirm New:"));  add(confirmPass, "growx");

        JButton saveBtn = new JButton("Update Password");
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));

        saveBtn.addActionListener(e -> {
            String p1 = new String(newPass.getPassword());
            String p2 = new String(confirmPass.getPassword());
            String old = new String(oldPass.getPassword());

            if (p1.isEmpty() || old.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.");
                return;
            }
            if (!p1.equals(p2)) {
                JOptionPane.showMessageDialog(this, "New passwords do not match.");
                return;
            }

            try {
                authService.changePassword(user.getUserId(), old, p1);
                JOptionPane.showMessageDialog(this, "Success! Please log in again.");
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        add(saveBtn, "span, center, gaptop 20");
    }
}