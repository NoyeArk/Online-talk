/**
 * @BelongsProject: online_talk
 * @BelongsPackage: Client
 * @Author: 弘树
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
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String chatUserName;

    // 界面绘制
    JButton sendButton, chatButton;

    private JTabbedPane tabbedPane;
    private JTextField messageField;
    private DefaultListModel<String> userListModel;

    private final Map<String, JTextArea> mp = new HashMap<>();

    public Client(int num) {
        createUi();
    }

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
        new Thread(new ServerAgent()).start();
    }

    private void createUi() {
        UIManager.put("TabbedPane.font", new Font("黑体", Font.PLAIN, 17));

        // 字体设置
        Font btnFont = new Font("黑体", Font.BOLD, 17);
        Font labelFont = new Font("黑体", Font.PLAIN, 17);
        Font msgFeildFont = new Font("黑体", Font.PLAIN, 15);

        ImageIcon icon = new ImageIcon("img\\client.png");
        this.setIconImage(icon.getImage());

        this.setTitle("在线聊天室");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(1000, 700);
        this.setLocationRelativeTo(null); // 居中显示

        // 布局设置为边界布局
        this.setLayout(new BorderLayout());

        tabbedPane = new JTabbedPane();
        this.add(tabbedPane, BorderLayout.CENTER);

        // 聊天区域（中间）
        newChatArea("群聊");

        // 底部面板，包含消息输入框和发送按钮
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());

        JLabel label = new JLabel("      请输入文字     →");
        label.setFont(labelFont);
        bottomPanel.add(label, BorderLayout.WEST);

        messageField = new JTextField();
        messageField.setFont(msgFeildFont);
        bottomPanel.add(messageField, BorderLayout.CENTER);

        // 按钮
        sendButton = new JButton("发送消息");
        chatButton = new JButton("选定用户私聊");
        sendButton.setForeground(new Color(6, 174, 86));
        sendButton.setBackground(new Color(233, 233, 233));
        chatButton.setForeground(new Color(236, 73, 73));
        chatButton.setBackground(new Color(157, 204, 243));
        sendButton.setFont(btnFont);
        chatButton.setFont(btnFont);
        bottomPanel.add(sendButton, BorderLayout.EAST);
        this.add(bottomPanel, BorderLayout.SOUTH);

        // 左侧用户区域
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout());

        userListModel = new DefaultListModel<>();
        JList<String> userList = new JList<>(userListModel);
        userList.setFont(new Font("黑体", Font.PLAIN, 20));
        JScrollPane userScrollPane = new JScrollPane(userList);
        userScrollPane.setBorder(BorderFactory.createTitledBorder(new LineBorder(Color.lightGray, 1), "其他在线用户",
                TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                new Font("黑体", Font.BOLD, 25), Color.blue));
        userScrollPane.setPreferredSize(new Dimension(200, 0));

        leftPanel.add(userScrollPane);
        leftPanel.add(chatButton, BorderLayout.SOUTH);

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

    private void newChatArea(String title) {
        JTextArea chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("宋体", Font.PLAIN, 20));
        JScrollPane scrollPane = new JScrollPane(chatArea);
        mp.put(title, chatArea);
        tabbedPane.addTab(title, scrollPane);
    }

    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            // 在聊天区域显示消息
            String username = tabbedPane.getTitleAt(tabbedPane.getSelectedIndex());
            mp.get(username).append("我：" + message + "\n");
            messageField.setText(""); // 清空消息输入框

            // 将消息发送给服务器
            if (username.equals("群聊"))
                out.println("message:" + message);
            else
                out.println("messageOnly:" + username + " " + message);
        }
    }

    private void chatWithUser() {
        newChatArea(chatUserName);
        tabbedPane.setSelectedIndex(tabbedPane.indexOfTab(chatUserName));
    }

    private void newUserLogin(String username) {
        userListModel.addElement(username);
    }

    private void removeUser(String username) {
        int id = userListModel.indexOf(username);
        userListModel.remove(id);
    }

    private void newMessage(String message) {
        JTextArea chatArea;
        String title, content;
        // 判断是私聊还是群聊
        if (message.contains("private:")) {
            int st = message.indexOf(":");
            int ed = message.indexOf(" ");
            String username = message.substring(st + 1, ed);
            String msg = message.substring(ed + 1);

            if (!mp.containsKey(username))
                newChatArea(username);
            title = username;
            content = username + "：" + msg;
        }
        else {
            title = "群聊";
            content = message;
        }
        chatArea = mp.get(title);
        // 跳转到指定的聊天窗口
        tabbedPane.setSelectedIndex(tabbedPane.indexOfTab(title));
        chatArea.append(content + "\n");
    }

    public class ServerAgent extends Thread {
        public void run() {
            String message;
            while (true) {
                try {
                    if ((message = in.readLine()) == null) break;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                // 打印接收到的消息
                System.out.println(message);
                // 判断客户端收到的信息类型
                // 1.新用户上线
                if (message.contains("new:")) {
                    String newUserName = message.substring(4);
                    newUserLogin(newUserName);
                }
                // 2.有用户下线
                else if (message.contains("delete:")) {
                    String username = message.substring(7);
                    removeUser(username);
                }
                // 3.强制下线
                else if (message.contains("exit")) {
                    dispose();
                    return ;
                }
                // 4.解除禁言
                else if (message.contains("dismute")) {
                    System.out.println("23432243");
                    sendButton.setEnabled(true);
                    chatButton.setEnabled(true);
                    sendButton.setText("发送消息");
                }
                // 5.禁言
                else if (message.contains("mute")) {
                    sendButton.setEnabled(false);
                    chatButton.setEnabled(false);
                    sendButton.setText("禁言中");
                }
                // 6.聊天消息
                else newMessage(message);
            }
        }
    }

    public static void main(String[] args) {
        new Client(1);
    }
}
