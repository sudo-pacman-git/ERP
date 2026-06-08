package edu.univ.erp.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import edu.univ.erp.MainFrame;
import edu.univ.erp.domain.User;
import edu.univ.erp.service.AuthService;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.net.URL;

public class LoginPanel extends JPanel {

    private JTextField userField;
    private JPasswordField passField;
    private AuthService authService;
    private MainFrame mainFrame;

    private JLabel titleLabel;
    private JButton loginButton;
    private Image backgroundImage;

    private final Color COLOR_TITLE_LIGHT = new Color(50, 50, 150);
    private final Color COLOR_TITLE_DARK  = new Color(180, 200, 255);

    private final Color COLOR_BTN_LIGHT   = new Color(50, 100, 200);
    private final Color COLOR_BTN_DARK    = new Color(70, 130, 240);

    public LoginPanel(MainFrame frame) {
        this.mainFrame = frame;
        this.authService = new AuthService();


        try {
            URL imageUrl = getClass().getResource("/background.png");
            if (imageUrl != null) {
                this.backgroundImage = ImageIO.read(imageUrl);
            } else {
                System.err.println("Image not found at path: /background.png");
            }
        } catch (IOException e) {
            System.err.println("Error loading background image: " + e.getMessage());
        }

        setLayout(new BorderLayout());
        setOpaque(false);

        
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topBar.setOpaque(false);

        JToggleButton themeToggle = new JToggleButton("Dark Mode");
        themeToggle.setFocusPainted(false);
        themeToggle.putClientProperty("JButton.buttonType", "roundRect");

        themeToggle.addActionListener(e -> {
            try {
                if (themeToggle.isSelected()) {
                    UIManager.setLookAndFeel(new FlatDarkLaf());
                    themeToggle.setText("Light Mode");
                    titleLabel.setForeground(COLOR_TITLE_DARK);
                    loginButton.setBackground(COLOR_BTN_DARK);
                } else {
                    UIManager.setLookAndFeel(new FlatLightLaf());
                    themeToggle.setText("Dark Mode");
                    titleLabel.setForeground(COLOR_TITLE_LIGHT);
                    loginButton.setBackground(COLOR_BTN_LIGHT);
                }
                FlatLaf.updateUI();
                revalidate();
                repaint();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        topBar.add(themeToggle);
        add(topBar, BorderLayout.NORTH);


        JPanel centerPanel = new JPanel(new MigLayout("fill, wrap 1, align center center"));
        centerPanel.setOpaque(false);

        titleLabel = new JLabel("IIITD ERP SYSTEM");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(COLOR_TITLE_LIGHT);
        centerPanel.add(titleLabel, "center, gapbottom 30");

        JPanel fieldsPanel = new JPanel(new MigLayout("wrap 2", "[right]10[fill, 250!]"));
        fieldsPanel.setOpaque(false);

        JLabel userLbl = new JLabel("Username:");
        userLbl.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        userField = new JTextField();
        userField.putClientProperty("JTextField.placeholderText", "Enter username");
        userField.putClientProperty("Component.arc", 20);

        fieldsPanel.add(userLbl);
        fieldsPanel.add(userField);

        JLabel passLbl = new JLabel("Password:");
        passLbl.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        passField = new JPasswordField();
        passField.putClientProperty("JTextField.placeholderText", "Enter password");
        passField.putClientProperty("Component.arc", 20);

        passField.putClientProperty(FlatClientProperties.STYLE, "showRevealButton:true");

        fieldsPanel.add(passLbl);
        fieldsPanel.add(passField);

        centerPanel.add(fieldsPanel, "center, gapbottom 20");

        loginButton = new JButton("Login");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        loginButton.setBackground(COLOR_BTN_LIGHT);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.putClientProperty("JButton.buttonType", "roundRect");

        loginButton.addActionListener((ActionEvent e) -> handleLogin());

        centerPanel.add(loginButton, "center, wmin 150, hmin 40");

        add(centerPanel, BorderLayout.CENTER);
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (backgroundImage != null) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            g2d.dispose();
            if (UIManager.getLookAndFeel() instanceof FlatDarkLaf) {
                g.setColor(new Color(0, 0, 0, 150));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        }
    }


    private void handleLogin() {
        String username = userField.getText();
        String password = new String(passField.getPassword());

        try {
            User user = authService.login(username, password);
            mainFrame.showDashboard(user);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Login Failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}