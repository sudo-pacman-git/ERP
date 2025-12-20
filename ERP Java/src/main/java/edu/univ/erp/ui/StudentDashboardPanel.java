package edu.univ.erp.ui;

import com.formdev.flatlaf.FlatClientProperties;
import edu.univ.erp.MainFrame;
import edu.univ.erp.domain.SectionRow;
import edu.univ.erp.domain.User;
import edu.univ.erp.service.StudentService;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

public class StudentDashboardPanel extends JPanel {

    public StudentDashboardPanel(MainFrame frame, User user) {
        setLayout(new BorderLayout());


        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.setBackground(new Color(50, 150, 50));

        JLabel title = new JLabel("Student Portal - " + user.getUsername());
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        header.add(title);

        JButton pwdBtn = new JButton("Change Password");
        pwdBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pwdBtn.addActionListener(e -> {
            JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
            new ChangePasswordDialog(parent, user).setVisible(true);
        });
        header.add(pwdBtn);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> frame.showLogin());
        header.add(logoutBtn);

        add(header, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();

        tabs.addTab("Course Catalog", new CatalogPanel(user));

        tabs.addTab("My Registrations", new MyRegistrationsPanel(user));

        add(tabs, BorderLayout.CENTER);
    }

    static class CatalogPanel extends JPanel {
        private final StudentService studentService = new StudentService();
        private final DefaultTableModel tableModel;
        private final JTable table;
        private final TableRowSorter<DefaultTableModel> sorter; // For Live Search
        private List<SectionRow> currentRows;
        private final User user;

        public CatalogPanel(User user) {
            this.user = user;
            setLayout(new MigLayout("fill, insets 10", "[grow]", "[][][grow][]"));

            JLabel searchLabel = new JLabel("Search Catalog:");
            searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));

            JTextField searchField = new JTextField();
            searchField.putClientProperty("JTextField.placeholderText", "Type course name, code, or instructor...");
            searchField.putClientProperty("Component.arc", 15);
            searchField.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);

            add(searchLabel, "split 2, gapright 10");
            add(searchField, "growx, wrap");

            String[] cols = {"Code", "Title", "Instructor", "Schedule", "Room", "Capacity"};
            tableModel = new DefaultTableModel(cols, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            table = new JTable(tableModel);
            table.setRowHeight(25);
            table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));


            sorter = new TableRowSorter<>(tableModel);
            table.setRowSorter(sorter);

            add(new JScrollPane(table), "grow, wrap");


            searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
                public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
                public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }

                private void filter() {
                    String text = searchField.getText();
                    if (text.trim().length() == 0) {
                        sorter.setRowFilter(null);
                    } else {

                        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                    }
                }
            });

            // 5. Buttons
            JButton refresh = new JButton("Refresh");
            refresh.addActionListener(e -> load());
            JButton register = new JButton("Register Selected");
            register.addActionListener(e -> register());

            JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            btns.add(refresh);
            btns.add(register);
            add(btns, "growx");

            load();
        }

        private void load() {
            tableModel.setRowCount(0);
            currentRows = studentService.getAvailableSections();
            for (SectionRow r : currentRows) tableModel.addRow(r.toArray());
        }

        private void register() {
            int viewRow = table.getSelectedRow();
            if (viewRow < 0) {
                JOptionPane.showMessageDialog(this, "Select a course first.");
                return;
            }

            int modelRow = table.convertRowIndexToModel(viewRow);

            try {
                studentService.register(user.getUserId(), currentRows.get(modelRow).getSectionId());
                JOptionPane.showMessageDialog(this, "Success! Registered.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}