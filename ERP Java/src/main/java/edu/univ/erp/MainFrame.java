package edu.univ.erp;

import com.formdev.flatlaf.FlatLightLaf;
import edu.univ.erp.domain.User;
import edu.univ.erp.ui.AdminDashboardPanel;
import edu.univ.erp.ui.InstructorDashboardPanel;
import edu.univ.erp.ui.LoginPanel;
import edu.univ.erp.ui.StudentDashboardPanel;

import javax.swing.*;

public class MainFrame extends JFrame {

    public MainFrame() {
        setTitle("University ERP System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        showLogin();
    }

    public void showLogin() {
        setContentPane(new LoginPanel(this));
        revalidate();
        repaint();
    }

    public void showDashboard(User user) {
        if ("ADMIN".equals(user.getRole())) {

            setContentPane(new AdminDashboardPanel(this, user));

        } else if ("STUDENT".equals(user.getRole())) {

            setContentPane(new StudentDashboardPanel(this, user));

        } else if ("INSTRUCTOR".equals(user.getRole())) {

            setContentPane(new InstructorDashboardPanel(this, user));

        } else {

            JPanel errorPanel = new JPanel();
            errorPanel.add(new JLabel("Error: Role not recognized."));
            errorPanel.add(new JButton("Logout") {{
                addActionListener(e -> showLogin());
            }});
            setContentPane(errorPanel);
        }

        revalidate();
        repaint();
    }

    public static void main(String[] args) {
        // 1. Initialize Seed Data
        edu.univ.erp.util.DatabaseSeeder.initializeSeedData();

        // 2. Initialize Look and Feel
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize LaF");
        }

        // 3. Launch UI
        SwingUtilities.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });
    }
}