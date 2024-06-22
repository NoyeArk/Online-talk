/**
 * @BelongsProject: online_talk
 * @BelongsPackage: com.GUI
 * @Author: 弘树
 * @CreateTime: 2024-06-19  18:56
 * @Description: 登录界面绘制
 * @Version: 1.0
 */

package com.GUI;

import com.Client.Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;


public class LoginUI extends JFrame {
    public final Client client;
    private final JTextField usernameField;
    private final JPasswordField passwordField;

    public LoginUI() throws IOException {
        client = new Client();

        int width = 600;
        int height = 400;

        ImageIcon icon = new ImageIcon("img\\logo.png");
        ImageIcon img = new ImageIcon("img\\background.jpg");
        Font font = new Font("宋体", Font.BOLD, 20);
        Color color = new Color(255, 255, 255);

        // 设置背景图片
        ((JPanel)this.getContentPane()).setOpaque(false);
        JLabel background = new JLabel(img);
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

        // 标题展示
        JLabel titleLabel = new JLabel("NEUQ在线聊天室");
        titleLabel.setBounds(105, 50, 450, 60);
        titleLabel.setFont(new Font("楷体", Font.BOLD, 55));
        titleLabel.setForeground(Color.orange);
        titleLabel.setOpaque(false);

        // 用户名输入框
        JPanel usernamePanel = new JPanel(new FlowLayout());
        JLabel usernameLabel = new JLabel("用户名：");
        usernameField = new JTextField(15);

        usernameLabel.setFont(font);
        usernameLabel.setForeground(color);

        usernamePanel.add(usernameLabel);
        usernamePanel.add(usernameField);
        usernamePanel.setOpaque(false);
        usernamePanel.setBounds(65, 150, 400, 50);

        // 密码输入框
        JPanel passwordPanel = new JPanel(new FlowLayout());
        JLabel passwordLabel = new JLabel("密码：");
        passwordLabel.setFont(font);
        passwordLabel.setForeground(color);
        passwordField = new JPasswordField(15);

        passwordPanel.add(passwordLabel);
        passwordPanel.add(passwordField);
        passwordPanel.setOpaque(false);
        passwordPanel.setBounds(75, 200, 400, 50);

        // 登录按钮
        JButton loginButton = new JButton("登录");
        JButton registerButton = new JButton("注册");

        loginButton.setFont(font);
        registerButton.setFont(font);

        loginButton.setForeground(new Color(0, 0, 0));
        loginButton.setBackground(new Color(157, 204, 243));
        registerButton.setForeground(new Color(0, 0, 0));
        registerButton.setBackground(new Color(157, 204, 243));

        // 添加事件监听
        loginButton.addActionListener(e -> clickedLoginBtn());
        registerButton.addActionListener(e -> clickedRegisterBtn());

        loginButton.setOpaque(true);
        registerButton.setOpaque(true);
        loginButton.setBounds(180, 270, 80, 40);
        registerButton.setBounds(320, 270, 80, 40);

        // 添加组件到主窗体中
        add(titleLabel);
        add(usernamePanel);
        add(passwordPanel);
        add(loginButton);
        add(registerButton);

        setVisible(true);
    }

    private void clickedLoginBtn() {
        String username = usernameField.getText();
        char[] passwordChars = passwordField.getPassword();
        String password = new String(passwordChars); // Convert char[] to String

        // 从数据库中查询是否存在对应的sno和password
        try {
            if (client.loginCheck(username, password)) {
                JOptionPane.showMessageDialog(LoginUI.this, "登录成功!");
                // 隐藏界面
                dispose();
                client.loginSuccess(username);
            }
            else {
                JOptionPane.showMessageDialog(LoginUI.this,
                        "登录失败，请检查用户名或密码是否正确",
                        "登录失败", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void clickedRegisterBtn() {
        RegisterUI ui = new RegisterUI();
    }

    public static void main(String[] args) throws IOException {
        new LoginUI();
    }

    private class RegisterUI extends JFrame {
        private JTextField usernameField;
        private JPasswordField passwordField;
        private JPasswordField passwordConfirmField;

        public RegisterUI() {
            setTitle("注册页面");
            setSize(250, 200);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);

            JPanel panel = new JPanel();
            panel.setLayout(new GridLayout(4, 2));

            JLabel usernameLabel = new JLabel("   用户名:");
            panel.add(usernameLabel);

            usernameField = new JTextField();
            panel.add(usernameField);

            JLabel passwordLabel = new JLabel("    密码:");
            panel.add(passwordLabel);

            passwordField = new JPasswordField();
            panel.add(passwordField);

            JLabel passwordConfirmLabel = new JLabel("确认密码:");
            panel.add(passwordConfirmLabel);

            passwordConfirmField = new JPasswordField();
            panel.add(passwordConfirmField);

            JButton registerButton = new JButton("注册");
            panel.add(registerButton);

            JButton cancelButton = new JButton("取消");
            panel.add(cancelButton);

            add(panel);

            // 注册按钮点击事件监听
            registerButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String username = usernameField.getText().trim();
                    char[] passwordChars = passwordField.getPassword();
                    String password = new String(passwordChars);

                    char[] passwordConfirmChars = passwordConfirmField.getPassword();
                    String passwordConfirm = new String(passwordConfirmChars);

                    System.out.println(password + " " + passwordConfirm);

                    if (!password.equals(passwordConfirm)) {
                        JOptionPane.showMessageDialog(RegisterUI.this,
                                "两次密码输入不一致",
                                "注册失败", JOptionPane.ERROR_MESSAGE);
                    }
                    else {
                        try {
                            register(username, password);
                            dispose();
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }
            });

            cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dispose(); // 取消按钮直接关闭窗口
                }
            });

            setVisible(true);
        }

        private void register(String username, String password) throws IOException {
            if (client.registerCheck(username, password))
                JOptionPane.showMessageDialog(this, "注册成功！");
            else
                JOptionPane.showMessageDialog(RegisterUI.this,
                        "注册失败",
                        "注册失败", JOptionPane.ERROR_MESSAGE);
        }
    }
}
