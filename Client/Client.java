/**
 * @BelongsProject: online_talk
 * @BelongsPackage: Client
 * @Author: horiki
 * @CreateTime: 2024-06-18  14:19
 * @Description: 客户端代码实现
 * @Version: 1.0
 */

package Client;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Client extends JFrame {
    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter out;
    private String chatUserName;

    // 界面绘制
    private JTabbedPane tabbedPane;
    private JTextField messageField;
    private DefaultListModel<String> userListModel;

    private final Map<String, JTextArea> mp = new HashMap<>();

    public Client() throws IOException {
        // 创建TCP连接
        socket = new Socket("localhost", 10086);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }

    public boolean loginCheck(String username, String password) throws IOException {
        // 发送登录命令
        out.println("login:" + username + " " + password);

        String message;
        if ((message = in.readLine()) != null) {
            System.out.println(message);
            return message.equals("true");
        }
        return false;
    }

    public void loginSuccess() {
        // 显示聊天界面
        createUi();
        // 启动接收消息的线程
        new Thread(new IncomingReader()).start();
    }

    private void createUi() {
        this.setTitle("在线聊天室");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(1000, 700);
        this.setLocationRelativeTo(null); // 居中显示

        // 布局设置为边界布局
        this.setLayout(new BorderLayout());

        tabbedPane = new JTabbedPane();

        // 聊天区域（中间）
        JTextArea chatArea = new JTextArea();
        chatArea.setEditable(false);  // 只读
        JScrollPane scrollPane = new JScrollPane(chatArea);
        mp.put("群聊", chatArea);

        tabbedPane.addTab("群聊", scrollPane);
        this.add(tabbedPane, BorderLayout.CENTER);

        // 底部面板，包含消息输入框和发送按钮
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());

        messageField = new JTextField();
        bottomPanel.add(messageField, BorderLayout.CENTER);

        JButton sendButton = new JButton("发送");
        JButton chatButton = new JButton("聊天");
        bottomPanel.add(sendButton, BorderLayout.EAST);

        // 添加底部面板到窗口底部
        this.add(bottomPanel, BorderLayout.SOUTH);

        // 左侧用户区域
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout());

        userListModel = new DefaultListModel<>();
        JList<String> userList = new JList<>(userListModel);
        userList.setFont(new Font("黑体", Font.PLAIN, 14));
        JScrollPane userScrollPane = new JScrollPane(userList);
        userScrollPane.setBorder(BorderFactory.createTitledBorder(new LineBorder(Color.lightGray, 1), "其他在线用户",
                TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                new Font("黑体", Font.BOLD, 16), Color.darkGray));
        userScrollPane.setPreferredSize(new Dimension(200, 0));

        leftPanel.add(userScrollPane);
        leftPanel.add(chatButton, BorderLayout.SOUTH);

        // 添加 ListSelectionListener 监听器
        userList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                JList<String> source = (JList<String>) e.getSource();
                int selectedIndex = source.getSelectedIndex();
                if (selectedIndex != -1) {
                    String selectedUser = userListModel.getElementAt(selectedIndex);
                    System.out.println("用户点击了：" + selectedUser);
                    // 在这里可以对选中的用户进行处理
                    chatUserName = selectedUser;
                }
            }
        });
        this.add(leftPanel, BorderLayout.WEST);
        // 按钮事件监听
        sendButton.addActionListener(e -> sendMessage());
        chatButton.addActionListener(e -> chatWithUser());
        // 显示窗口
        setVisible(true);
    }

    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            // 在聊天区域显示消息
            String username = tabbedPane.getTitleAt(tabbedPane.getSelectedIndex());
            mp.get(username).append("我： " + message + "\n");
            messageField.setText(""); // 清空消息输入框

            // 将消息发送给服务器
            out.println("messageOnly" + username + " " + message);
        }
    }

    private void chatWithUser() {
        // 展示一个新的界面
        JTextArea chatArea = new JTextArea();
        chatArea.setEditable(false);  // 只读
        JScrollPane scrollPane = new JScrollPane(chatArea);

        tabbedPane.addTab(chatUserName, scrollPane);
        mp.put(chatUserName, chatArea);
    }

    public class IncomingReader implements Runnable {
        public void run() {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    // 打印接收到的消息
                    System.out.println(message);
                    // 判断客户端收到的信息类型
                    // 1.新用户上线
                    if (message.contains("new:")) {
                        String newUserName = message.substring(4);
                        // 添加到在线用户列表中
                        userListModel.addElement(newUserName);
                    }
                    // 2.有用户下线
                    else if (message.contains("delete:")) {
                        String username = message.substring(7);
                        int id = userListModel.indexOf(username);
                        userListModel.remove(id);
                    }
                    else {
                        // 展示在界面的聊天区域中
                        String title = tabbedPane.getTitleAt(tabbedPane.getSelectedIndex());
                        mp.get(title).append(message + "\n");
                    }
                }
            } catch (IOException e) {
                System.out.println("Error reading from server: " + e.getMessage());
            }
        }
    }
}
