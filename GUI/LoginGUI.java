/**
 * @BelongsProject: online_talk
 * @BelongsPackage: GUI
 * @Author: 弘树
 * @CreateTime: 2024-06-19  18:56
 * @Description: 登录界面绘制
 * @Version: 1.0
 */

package GUI;

import Client.Client;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;


public class LoginGUI extends JFrame {
    private final Client client;
    private final JTextField usernameField;
    private final JPasswordField passwordField;

    public LoginGUI() throws IOException {
        client = new Client();

        int width = 600;
        int height = 400;

        ImageIcon icon = new ImageIcon("img\\logo.png");
        ImageIcon img = new ImageIcon("img\\background.jpg");
        Font font = new Font("宋体", Font.PLAIN, 20);
        Color color = new Color(255, 255, 255);

        // 设置背景图片
        ((JPanel)this.getContentPane()).setOpaque(false);
        JLabel background = new  JLabel(img);
        this.getLayeredPane().add(background, new Integer(Integer.MIN_VALUE));
        background.setBounds(0, 0, width, height);

        // 主窗体界面设置
        this.setTitle("聊天室登录");
        this.setLayout(null);
        this.setBounds(0, 0, width, height);
        this.setIconImage(icon.getImage());
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setResizable(false);  // 设置窗体不可缩放

        // 用户名输入框
        JPanel usernamePanel = new JPanel(new FlowLayout());
        JLabel usernameLabel = new JLabel("用户名：");
        usernameField = new JTextField(15);

        usernameLabel.setFont(font);
        usernameLabel.setForeground(color);

        usernamePanel.add(usernameLabel);
        usernamePanel.add(usernameField);
        usernamePanel.setOpaque(false);
        usernamePanel.setBounds(65, 100, 400, 50);

        // 密码输入框
        JPanel passwordPanel = new JPanel(new FlowLayout());
        JLabel passwordLabel = new JLabel("密码：");
        passwordLabel.setFont(font);
        passwordLabel.setForeground(color);
        passwordField = new JPasswordField(15);

        passwordPanel.add(passwordLabel);
        passwordPanel.add(passwordField);
        passwordPanel.setOpaque(false);
        passwordPanel.setBounds(75, 150, 400, 50);

        // 登录按钮
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton loginButton = new JButton("登录");
        loginButton.setFont(font);

        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            char[] passwordChars = passwordField.getPassword();
            String password = new String(passwordChars); // Convert char[] to String

            // 从数据库中查询是否存在对应的sno和password
            try {
                if (client.loginCheck(username, password)) {
                    JOptionPane.showMessageDialog(LoginGUI.this, "登录成功!");
                    // 隐藏界面
                    dispose();
                    client.loginSuccess();
                }
                else {
                    JOptionPane.showMessageDialog(LoginGUI.this,
                            "登录失败，请检查用户名或密码是否正确",
                            "Login Failed", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        buttonPanel.add(loginButton);
        buttonPanel.setOpaque(false);
        buttonPanel.setBounds(230, 250, 100, 100);

        // 添加组件到主窗体中
        add(usernamePanel);
        add(passwordPanel);
        add(buttonPanel);

        setVisible(true);
    }

    public static void main(String[] args) throws IOException {
        new LoginGUI();
    }
}
