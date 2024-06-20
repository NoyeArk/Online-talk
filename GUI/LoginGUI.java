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
    private int width = 600;
    private int height = 400;

    private JTextField usernameField;
    private JPasswordField passwordField;
    // 数据库连接代理
    DatabaseConnect db;

    public LoginGUI() {
        // 实例化数据库代理
        db = new DatabaseConnect();

        ImageIcon icon = new ImageIcon("img\\logo.png");
        ImageIcon bg = new ImageIcon("img\\background.jpg");
        Font font = new Font("黑体", Font.PLAIN, 15);

        // 主窗体界面设置
        this.setTitle("聊天室登录");
        this.setLayout(null);
        this.setBounds(0, 0, width, height);
        this.setIconImage(icon.getImage());
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);

        // 背景图片设置
        ((JPanel)this.getContentPane()).setOpaque(false);
        JLabel background = new JLabel();
        background.setIcon(bg);
        this.getLayeredPane().add(background, new Integer(Integer.MIN_VALUE));

        JPanel panel = new JPanel();
        panel.setOpaque(false);

        // 用户名输入框
        JPanel usernamePanel = new JPanel(new FlowLayout());
        JLabel usernameLabel = new JLabel("用户名：");
        usernameField = new JTextField(15);

        usernameLabel.setFont(font);
        usernameLabel.setForeground(new Color(255, 44, 222));

        usernamePanel.add(usernameLabel);
        usernamePanel.add(usernameField);
        usernamePanel.setBounds(30, 30, 300, 30);

        // 密码输入框
        JPanel passwordPanel = new JPanel(new FlowLayout());
        JLabel passwordLabel = new JLabel("密码：");
        passwordLabel.setFont(font);
        passwordLabel.setForeground(new Color(255, 44, 222));
        passwordField = new JPasswordField(15);

        passwordPanel.add(passwordLabel);
        passwordPanel.add(passwordField);
        passwordPanel.setBounds(30, 100, 300, 30);

        // 登录按钮
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBounds(200, 200, 50, 50);

        JButton loginButton = new JButton("登录");

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

        // 添加组件到主窗体中
        add(usernamePanel);
        add(passwordPanel);
        add(buttonPanel);

        setVisible(true);
    }

//    public void paint(Graphics g) {
//        ImageIcon bg = new ImageIcon("img\\background.jpg");
//        super.paint(g);
//        g.drawImage(bg.getImage(), 0, 0, null);
//    }

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
        new LoginGUI();
    }
}
