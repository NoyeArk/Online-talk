package GUI;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GUI extends JFrame {
    private JTextArea messageArea;
    private JList<String> userList;
    private DefaultListModel<String> userListModel;

    public GUI() {
        setTitle("服务器控制台");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null); // 居中显示

        // 创建主面板，使用边界布局
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        getContentPane().add(mainPanel);

        // 创建消息区域
        messageArea = new JTextArea();
        messageArea.setEditable(false);
        messageArea.setFont(new Font("Arial", Font.PLAIN, 14));
        JScrollPane messageScrollPane = new JScrollPane(messageArea);
        messageScrollPane.setBorder(BorderFactory.createTitledBorder(new LineBorder(Color.lightGray, 1), "消息日志",
                TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                new Font("Arial", Font.BOLD, 16), Color.darkGray));
        mainPanel.add(messageScrollPane, BorderLayout.CENTER);

        // 创建在线用户列表
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setFont(new Font("Arial", Font.PLAIN, 14));
        JScrollPane userScrollPane = new JScrollPane(userList);
        userScrollPane.setBorder(BorderFactory.createTitledBorder(new LineBorder(Color.lightGray, 1), "在线用户",
                TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                new Font("Arial", Font.BOLD, 16), Color.darkGray));
        userScrollPane.setPreferredSize(new Dimension(200, 0));
        mainPanel.add(userScrollPane, BorderLayout.EAST);

        // 用户操作面板
        JPanel controlPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        controlPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        controlPanel.setBackground(new Color(240, 240, 240));
        mainPanel.add(controlPanel, BorderLayout.SOUTH);

        // 强制下线按钮
        JButton kickButton = new JButton("强制下线");
        kickButton.setFont(new Font("Arial", Font.PLAIN, 14));
        kickButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                kickUser();
            }
        });
        controlPanel.add(kickButton);

        // 禁言按钮
        JButton muteButton = new JButton("禁言");
        muteButton.setFont(new Font("Arial", Font.PLAIN, 14));
        muteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                muteUser();
            }
        });
        controlPanel.add(muteButton);

        // 设置背景色
        mainPanel.setBackground(Color.white);
        messageArea.setBackground(Color.white);
        userList.setBackground(Color.white);

        // 设置窗口可见性
        setVisible(true);
    }

    // 添加用户到在线用户列表
    public void addUser(String username) {
        userListModel.addElement(username);
    }

    // 从在线用户列表移除用户
    public void removeUser(String username) {
        userListModel.removeElement(username);
    }

    // 显示收到的消息
    public void displayMessage(String message) {
        messageArea.append(message + "\n");
        messageArea.setCaretPosition(messageArea.getDocument().getLength()); // 滚动到最后一行
    }

    // 强制下线用户
    private void kickUser() {
        String selectedUser = userList.getSelectedValue();
        if (selectedUser != null) {
            JOptionPane.showMessageDialog(this, "已强制下线用户: " + selectedUser, "强制下线", JOptionPane.INFORMATION_MESSAGE);
            removeUser(selectedUser); // 从列表移除用户
        } else {
            JOptionPane.showMessageDialog(this, "请选择要强制下线的用户", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 禁言用户
    private void muteUser() {
        String selectedUser = userList.getSelectedValue();
        if (selectedUser != null) {
            JOptionPane.showMessageDialog(this, "已禁言用户: " + selectedUser, "禁言", JOptionPane.INFORMATION_MESSAGE);
            // 可以在此处实现禁言逻辑
        } else {
            JOptionPane.showMessageDialog(this, "请选择要禁言的用户", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        // 设置界面风格
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // 在主线程中创建界面
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new GUI();
            }
        });
    }
}
