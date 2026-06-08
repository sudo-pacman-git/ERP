package edu.univ.erp.ui;

import edu.univ.erp.MainFrame;
import edu.univ.erp.domain.User;
import edu.univ.erp.service.SettingsService;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.net.URL;

public class AdminDashboardPanel extends JPanel {

    private final SettingsService settingsService;
    private final JToggleButton maintenanceToggle;
    private final User currentUser;
    private Image backgroundImage;

    public AdminDashboardPanel(MainFrame frame, User user) {
        this.currentUser = user;
        this.settingsService = new SettingsService();

        try {
            URL imageUrl = getClass().getResource("/background.png");
            if (imageUrl != null) {
                this.backgroundImage = ImageIO.read(imageUrl);
            }
        } catch (IOException e) {
            System.err.println("Error loading background image: " + e.getMessage());
        }

        setOpaque(false);

        setLayout(new MigLayout("fill, wrap 1, insets 20", "[grow]", "[]10[]10[]"));


        JLabel title = new JLabel("Admin Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setForeground(new Color(50, 50, 150));
        add(title, "center, gapbottom 10");

        add(new JSeparator(), "growx, gaptop 5, gapbottom 10");

        JPanel managePanel = new JPanel(new MigLayout("insets 15", "[fill, 220]10[fill, 250]"));
        managePanel.setBorder(BorderFactory.createTitledBorder("Administration Actions"));
        managePanel.setOpaque(false); 


        JButton addUserBtn = new JButton("Add New User");
        addUserBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        addUserBtn.putClientProperty("Component.arc", 15);
        addUserBtn.addActionListener(e -> {
            JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
            new AddUserDialog(parent).setVisible(true);
        });

        JButton courseBtn = new JButton("Manage Courses & Sections");
        courseBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        courseBtn.putClientProperty("Component.arc", 15);

        courseBtn.addActionListener(e -> {
            JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
            new AddCourseDialog(parent).setVisible(true);
        });

        managePanel.add(addUserBtn, "growx, hmin 45, wmin 180");
        managePanel.add(courseBtn, "growx, hmin 45, wmin 220");

        add(managePanel, "center, growx, gapbottom 15");

        JPanel controlPanel = new JPanel(new MigLayout("insets 15", "[]15[]"));
        controlPanel.setBorder(BorderFactory.createTitledBorder("System Controls"));
        controlPanel.setOpaque(false); 

        JLabel maintenanceLabel = new JLabel("Maintenance Mode:");
        maintenanceLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        maintenanceToggle = new JToggleButton("Loading...");
        maintenanceToggle.putClientProperty("Component.arc", 15);

        updateButtonState(settingsService.isMaintenanceModeOn());

        maintenanceToggle.addActionListener(e -> {
            boolean newState = maintenanceToggle.isSelected();
            try {
                settingsService.setMaintenanceMode(newState);
                updateButtonState(newState);

                String status = newState ? "ON. System Locked." : "OFF. Access Restored.";
                JOptionPane.showMessageDialog(this, "Maintenance Mode is now " + status);
            } catch (Exception ex) {
                maintenanceToggle.setSelected(!newState);
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        controlPanel.add(maintenanceLabel);
        controlPanel.add(maintenanceToggle);
        add(controlPanel, "center, growx, gapbottom 10");

        // 4. Footer (Logout + Change Password)
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false); // Make transparent

        JButton pwdBtn = new JButton("Change Password");
        pwdBtn.putClientProperty("Component.arc", 15);
        pwdBtn.addActionListener(e -> {
            JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
            new ChangePasswordDialog(parent, currentUser).setVisible(true);
        });

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.putClientProperty("Component.arc", 15);
        logoutBtn.addActionListener(e -> frame.showLogin());

        footer.add(pwdBtn);
        footer.add(logoutBtn);
        add(footer, "south, growx");
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            g2d.dispose();

            g.setColor(new Color(255, 255, 255, 100));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }


    private void updateButtonState(boolean isOn) {
        maintenanceToggle.setSelected(isOn);
        maintenanceToggle.setText(isOn ? "ON (System Locked)" : "OFF");
        maintenanceToggle.setBackground(isOn ? new Color(255, 100, 100) : null);
        maintenanceToggle.setForeground(isOn ? Color.WHITE : Color.BLACK);
    }
}