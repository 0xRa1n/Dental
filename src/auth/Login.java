package auth;

import javax.swing.*;
import application.Doctor;
import application.Main;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import passanduser.Dao;
import model.User;

import application.Main;
import admin.AdminUI;

public class Login extends JDialog {
	private static boolean isJavaFxLaunched = false;
    private static final long serialVersionUID = 1L;
    private JTextField txtUsername;
    private JPasswordField txtPassword;

    public Login(JFrame parent) {
        super(parent, "Login", true);
        initUI(parent);
    }

    private void initUI(JFrame parent) {
        setSize(500, 350); 
        setLayout(new BorderLayout(10, 10));
        setLocationRelativeTo(parent);

        // Title
        JLabel titleLabel = new JLabel("Welcome Back!", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.blue);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(25, 0, 20, 0));

        // Main panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 20, 25, 20));
        mainPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 10, 15, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Input fields
        txtUsername = new JTextField();
        txtPassword = new JPasswordField();

        txtUsername.setPreferredSize(new Dimension(350, 35));
        txtPassword.setPreferredSize(new Dimension(350, 35));
        txtUsername.setFont(new Font("Arial", Font.PLAIN, 14));
        txtPassword.setFont(new Font("Arial", Font.PLAIN, 14));

        // Username
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        JLabel lblUsername = new JLabel("Username or Email:");
        lblUsername.setFont(new Font("Arial", Font.BOLD, 13));
        mainPanel.add(lblUsername, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(txtUsername, gbc);

        // Password
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        JLabel lblPassword = new JLabel("Password:");
        lblPassword.setFont(new Font("Arial", Font.BOLD, 13));
        mainPanel.add(lblPassword, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(txtPassword, gbc);

        // Login button
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JButton btnLogin = new JButton("Login");
        btnLogin.setBackground(new Color(41, 128, 185));
        btnLogin.setForeground(Color.blue);
        btnLogin.setFont(new Font("Arial", Font.BOLD, 16));
        btnLogin.setPreferredSize(new Dimension(280, 45));
        mainPanel.add(btnLogin, gbc);

        // Register panel
        JPanel registerPanel = createRegisterPanel(parent);

        add(titleLabel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        add(registerPanel, BorderLayout.SOUTH);

        // Actions
        btnLogin.addActionListener(e -> performLogin());
        txtPassword.addActionListener(e -> performLogin());

        // Styling
        getRootPane().setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2));
        getContentPane().setBackground(Color.WHITE);
        txtUsername.requestFocus();
    }

    private JPanel createRegisterPanel(JFrame parent) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 0, 15, 0));

        JLabel lblRegister = new JLabel("Don't have an account?");
        lblRegister.setForeground(Color.GRAY);
        lblRegister.setFont(new Font("Arial", Font.PLAIN, 13));
        lblRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));

        lblRegister.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showRegisterDialog(parent);
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                lblRegister.setForeground(new Color(41, 128, 185));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                lblRegister.setForeground(Color.GRAY);
            }
        });

        JButton btnRegister = new JButton("Register Here");
        btnRegister.setBackground(Color.WHITE);
        btnRegister.setForeground(new Color(41, 128, 185));
        btnRegister.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        btnRegister.setFocusPainted(false);
        btnRegister.setContentAreaFilled(false);
        btnRegister.setFont(new Font("Arial", Font.BOLD, 13));
        btnRegister.addActionListener(e -> showRegisterDialog(parent));

        panel.add(lblRegister);
        panel.add(btnRegister);
        return panel;
    }

    private void showRegisterDialog(JFrame parent) {
        JDialog registerDialog = new JDialog(parent, "Register Patient Account", true);
        registerDialog.setSize(380, 320);
        registerDialog.setLayout(new BorderLayout());
        registerDialog.setLocationRelativeTo(this);

        JPanel regPanel = new JPanel(new GridLayout(5, 2, 12, 12));
        regPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JTextField txtUsernameReg = new JTextField(18);
        JTextField txtEmailReg = new JTextField(18);
        JTextField txtNameReg = new JTextField(18);
        JPasswordField txtPasswordReg = new JPasswordField(18);

        regPanel.add(new JLabel("Username"));
        regPanel.add(txtUsernameReg);
        regPanel.add(new JLabel("Email"));
        regPanel.add(txtEmailReg);
        regPanel.add(new JLabel("Full Name"));
        regPanel.add(txtNameReg);
        regPanel.add(new JLabel("Password"));
        regPanel.add(txtPasswordReg);
        regPanel.add(new JLabel());
        JButton btnReg = new JButton("Register");
        regPanel.add(btnReg);

        btnReg.addActionListener(e -> {
            User u = new User(
                txtUsernameReg.getText().trim(),
                txtEmailReg.getText().trim(),
                new String(txtPasswordReg.getPassword()).trim(),
                txtNameReg.getText().trim(),
                "patient"
            );

            try {
                if (Dao.registerUser(u)) {
                    JOptionPane.showMessageDialog(registerDialog, "✅ Patient account registered!");
                    registerDialog.dispose();
                    txtUsername.setText(txtUsernameReg.getText().trim());
                    txtPassword.requestFocus();
                } else {
                    JOptionPane.showMessageDialog(registerDialog, "❌ Username/Email already exists!");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(registerDialog, "Error: " + ex.getMessage());
            }
        });

        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(e -> registerDialog.dispose());

        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.add(btnCancel);

        registerDialog.add(regPanel, BorderLayout.CENTER);
        registerDialog.add(bottomPanel, BorderLayout.SOUTH);
        registerDialog.setVisible(true);
    }

    private void performLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter username and password!",
                "Missing Info", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            User user = Dao.login(username, password);
            if (user != null) {
                System.out.println(user.getRole().toUpperCase());
                
                // Safely initialize JavaFX toolkit only once
                if (!isJavaFxLaunched) {
                    isJavaFxLaunched = true;
                    try {
                        javafx.application.Platform.startup(() -> {});
                    } catch (IllegalStateException e) {
                        // Suppress if toolkit is already initialized
                    }
                }

                // Prevent JavaFX from shutting down when the last window is closed
                javafx.application.Platform.setImplicitExit(false); 

                String role = user.getRole().toUpperCase();

                // Run UI creation on the JavaFX Application Thread
                javafx.application.Platform.runLater(() -> {
                     try {
                         if (role.equals("PATIENT")) {
                             new application.Main.App().start(new javafx.stage.Stage());
                         } else if (role.equals("ADMIN")) {
                             new admin.AdminUI().start(new javafx.stage.Stage());
                         } else if (role.equals("DOCTOR")) {
                        	 application.Doctor doc = new application.Doctor();
                             doc.start(new javafx.stage.Stage(), user.getUsername());
                         } else {
                             // Must run Swing dialog logically back on EDT, or just sysout
                             SwingUtilities.invokeLater(() -> 
                                 JOptionPane.showMessageDialog(null, "❌ Unrecognized user role: " + role)
                             );
                         }
                     } catch (Exception ex) {
                         ex.printStackTrace();
                     }
                });

                // Close the login window
                dispose();
                Window parentWindow = SwingUtilities.getWindowAncestor(this);
                if (parentWindow != null) {
                    parentWindow.dispose();
                }

            } else {
                JOptionPane.showMessageDialog(this,
                    "❌ Invalid username or password!",
                    "Login Failed", JOptionPane.ERROR_MESSAGE);
                txtPassword.setText("");
                txtUsername.requestFocus();
                txtUsername.selectAll();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Login Error: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        JFrame parent = new JFrame();
        parent.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        SwingUtilities.invokeLater(() -> {
            Login login = new Login(parent);
            login.setVisible(true);
        });
    }
}