package auth;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import passanduser.Dao;
import model.User;

public class RegisterFrame extends JDialog {

    private static final long serialVersionUID = 1L;

    JTextField txtUsername, txtEmail, txtName;
    JPasswordField txtPassword;

    public RegisterFrame(JFrame parent) {
        super(parent, "Register Patient Account", true);
        setSize(380, 320); 
        setLayout(new BorderLayout(10, 10)); 
        setLocationRelativeTo(parent);

        // ✅ YOUR ORIGINAL MAIN PANEL 
        JPanel mainPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Input fields 
        txtUsername = new JTextField();
        txtEmail = new JTextField();
        txtName = new JTextField();
        txtPassword = new JPasswordField();

        // Add labels and fields 
        mainPanel.add(new JLabel("Username")); mainPanel.add(txtUsername);
        mainPanel.add(new JLabel("Email")); mainPanel.add(txtEmail);
        mainPanel.add(new JLabel("Full Name")); mainPanel.add(txtName);
        mainPanel.add(new JLabel("Password")); mainPanel.add(txtPassword);

        // Register button (
        JButton btn = new JButton("Register");
        mainPanel.add(new JLabel()); 
        mainPanel.add(btn);

        // REGISTER LOGIC 
        btn.addActionListener(e -> {
            User u = new User(
                txtUsername.getText(),
                txtEmail.getText(),
                new String(txtPassword.getPassword()),
                txtName.getText(),
                "patient"
            );

            try {
                if (Dao.registerUser(u)) {
                    JOptionPane.showMessageDialog(this, "Patient account registered!");
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Error registering patient!");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Exception: " + ex.getMessage());
            }
        });

        // ✅ LOGIN LINK PANEL
        JPanel loginPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        loginPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        JLabel lblLogin = new JLabel("Already have an account?");
        lblLogin.setForeground(Color.GRAY);
        lblLogin.setFont(new Font("Arial", Font.PLAIN, 12));
        lblLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Login link styling
        JButton btnLogin = new JButton("Login Here");
        btnLogin.setBackground(Color.WHITE);
        btnLogin.setForeground(new Color(52, 152, 219));
        btnLogin.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        btnLogin.setFocusPainted(false);
        btnLogin.setContentAreaFilled(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.setFont(new Font("Arial", Font.BOLD, 12));
        btnLogin.setRolloverEnabled(false);

        // Login link hover effect
        lblLogin.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openLogin(parent);
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                lblLogin.setForeground(new Color(52, 152, 219));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                lblLogin.setForeground(Color.GRAY);
            }
        });

        // Login button action
        btnLogin.addActionListener(e -> openLogin(parent));

        loginPanel.add(lblLogin);
        loginPanel.add(btnLogin);

        // Layout assembly
        add(mainPanel, BorderLayout.CENTER);
        add(loginPanel, BorderLayout.SOUTH);

        // Modern border
        getRootPane().setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
    }

    //  Opens Login window
    private void openLogin(JFrame parent) {
        dispose(); // Close register
        new Login(parent).setVisible(true); // Open login
    }

    // Main method 
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        JFrame parent = new JFrame();
        parent.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        SwingUtilities.invokeLater(() -> {
            RegisterFrame rf = new RegisterFrame(parent);
            rf.setVisible(true);
        });
    }
}