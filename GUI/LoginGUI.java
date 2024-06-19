package GUI;

import Client.Client;
import DataBase.DatabaseConnect;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.SQLException;

/**
 * @BelongsProject: online_talk
 * @BelongsPackage: GUI
 * @Author: yanhongwei
 * @CreateTime: 2024-06-19  18:56
 * @Description: TODO
 * @Version: 1.0
 */

public class LoginGUI extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    // 数据库连接代理
    DatabaseConnect db;

    public LoginGUI() {
        // 实例化数据库代理
        db = new DatabaseConnect();

        setTitle("Chat Room Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 300);
        setLocationRelativeTo(null); // Center the frame on the screen

        // Panel to hold components
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 1));

        // Username input
        JPanel usernamePanel = new JPanel(new FlowLayout());
        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField(15);
        usernamePanel.add(usernameLabel);
        usernamePanel.add(usernameField);

        // Password input
        JPanel passwordPanel = new JPanel(new FlowLayout());
        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField(15);
        passwordPanel.add(passwordLabel);
        passwordPanel.add(passwordField);

        // Login button
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                char[] passwordChars = passwordField.getPassword();
                String password = new String(passwordChars); // Convert char[] to String

                // 从数据库中查询是否存在对应的sno和password
                try {
                    if (db.querySno(username, password)) {
                        JOptionPane.showMessageDialog(LoginGUI.this,
                                "Login successful!");
                        // 隐藏界面
                        dispose();
                        loginSuccess();
                    } else {
                        JOptionPane.showMessageDialog(LoginGUI.this,
                                "Login failed. Please check your username and password.",
                                "Login Failed", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        buttonPanel.add(loginButton);

        // Add components to the main panel
        panel.add(usernamePanel);
        panel.add(passwordPanel);
        panel.add(buttonPanel);

        // Add panel to the frame
        add(panel);

        // Make the frame visible
        setVisible(true);
    }

    private void loginSuccess() {
        String serverAddr = "localhost";
        int port = 10086;
        try {
            Client client = new Client(serverAddr, port);
        } catch (IOException e) {
            System.out.println("Error connecting to server: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LoginGUI();
            }
        });
    }
}
