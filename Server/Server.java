/**
 * @BelongsProject: online_talk
 * @BelongsPackage: Server
 * @Author: 弘树
 * @CreateTime: 2024-06-18  14:12
 * @Description: 服务器端代码实现
 * @Version: 1.0
 */

package Server;

import DataBase.DatabaseConnect;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Server extends JFrame {
    private final ServerUI ui;
    private final DatabaseConnect db;
    private final ServerSocket serverSocket;
    private final Map<Socket, String> mp = new HashMap<>();
    private final List<ClientAgent> clients = new ArrayList<>();

    public Server(int port) throws IOException {
        // 实例化数据库连接对象
        db = new DatabaseConnect();
        // 显示界面
        ui = new ServerUI();
        // 创建socket
        serverSocket = new ServerSocket(port);
        System.out.println("服务器正在端口" + port + "上运行....");
    }

    public void start() {
        while (true) {
            try {
                // 等待客户端进行连接
                Socket socket = serverSocket.accept();
                System.out.println("新的客户端发送请求：" + socket);

                // 创建一个新的线程用于处理客户的请求
                ClientAgent client = new ClientAgent(socket);

                // 添加到当前所有用户请求的列表中
                clients.add(client);
                client.start();
            } catch (IOException e) {
                System.out.println("Error in server: " + e.getMessage());
                break;
            }
        }
    }

    // 当一个用户发送消息时将该消息给所有连接的客户端
    public synchronized void broadcastMessage(String message, ClientAgent me) {
        for (ClientAgent client : clients) {
            if (client != me) {
                client.send.println(message);
            }
        }
    }

    // 处理客户端的线程
    private class ClientAgent extends Thread {
        private String myName;
        private final Socket socket;
        private PrintWriter send;
        private BufferedReader receive;

        public ClientAgent(Socket socket) {
            this.socket = socket;
            try {
                // 用于向客户端发送消息
                send = new PrintWriter(socket.getOutputStream(), true);
                // 用于从客户端接受消息
                receive = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                System.out.println("服务器启动用户线程时错误：" + e.getMessage());
            }
        }

        public void login(String username, String password) throws SQLException {
            System.out.println("username:" + username + " password:" + password);
            boolean res = db.querySno(username, password);
            if (res) {
                // 向客户端回送登录成功true
                send.println("true");
                // 向其他在线用户发送这个用户的姓名
                broadcastMessage("new:" + username, this);
                // 向该用户发送当前所有在线的其他用户
                for (String val : mp.values()) {
                    send.println("new:" + val);
                }
                // 添加到在线用户列表中
                ui.addUser(username);
                // 添加socket-username映射
                mp.put(socket, username);
                this.myName = username;
                // 展示在消息日志中
                ui.showMessage("用户" + username + "登录");
            }
            else {
                // 登录失败
                send.println("false");
            }
        }

        public void exit(String username) {
            // 通知每个用户删除这个用户
            broadcastMessage("delete:" + username, this);
            // 从服务器端界面中删除指定用户
            ui.remove(username);
            // 删除这个映射
            mp.remove(socket);
            // 消息日志更新
            ui.showMessage("用户" + username + "退出");
        }

        public void privateChat(String username, String message) {
            for (ClientAgent client : clients)
                if (client.myName.equals(username)) {
                    client.send.println("private:" + myName + " " + message);
                    break;
                }
        }

        public void run() {
            // 用于接受当前socket客户端发送的消息
            try {
                String message;
                while ((message = receive.readLine()) != null) {
                    System.out.println("收到客户端消息：" + message);

                    // 解析客户端发送的消息类型
                    if (message.contains("message:")) {
                        String content = mp.get(socket) + "：" + message.substring(8);
                        // 收到客户端消息，广播给所有客户端
                        broadcastMessage(content, this);
                    }
                    else {
                        int st = message.indexOf(":");
                        int ed = message.indexOf(" ");
                        String username = message.substring(st + 1, ed);
                        String pwdOrMsg = message.substring(ed + 1);
                        if (message.contains("login:"))
                            login(username, pwdOrMsg);
                        else
                            privateChat(username, pwdOrMsg);
                    }
                }
            } catch (IOException e) {
                System.out.println("Error handling client: " + e.getMessage());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } finally {
                try {
                    String username = mp.get(socket);
                    exit(username);

                    // 关闭流和socket
                    send.close();
                    receive.close();
                    socket.close();
                    clients.remove(this);
                    System.out.println("用户断开连接: " + socket);
                } catch (IOException e) {
                    System.out.println("Error closing client: " + e.getMessage());
                }
            }
        }
    }

    public static void main(String[] args) {
        int port = 10086;
        try {
            Server server = new Server(port);
            server.start();
        } catch (IOException e) {
            System.out.println("Error starting the server: " + e.getMessage());
        }
    }

    private class ServerUI extends JFrame {
        private String selectUserName;
        private final JTextArea messageArea;
        private final JList<String> userList;
        private final DefaultListModel<String> userListModel;

        public ServerUI() {
            ImageIcon icon = new ImageIcon("img\\server.png");
            this.setIconImage(icon.getImage());

            setTitle("服务器控制台");
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setSize(800, 600);
            setLocationRelativeTo(null); // 居中显示
            this.setResizable(false);  // 设置窗体不可缩放

            // 创建主面板，使用边界布局
            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
            getContentPane().add(mainPanel);

            // 创建消息区域
            messageArea = new JTextArea();
            messageArea.setEditable(false);
            messageArea.setFont(new Font("黑体", Font.PLAIN, 17));
            JScrollPane messageScrollPane = new JScrollPane(messageArea);
            messageScrollPane.setBorder(BorderFactory.createTitledBorder(new LineBorder(Color.lightGray, 1), "消息日志",
                    TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                    new Font("黑体", Font.BOLD, 25), Color.red));
            mainPanel.add(messageScrollPane, BorderLayout.CENTER);

            // 创建在线用户列表
            userListModel = new DefaultListModel<>();
            userList = new JList<>(userListModel);
            userList.setFont(new Font("黑体", Font.PLAIN, 20));
            JScrollPane userScrollPane = new JScrollPane(userList);
            userScrollPane.setBorder(BorderFactory.createTitledBorder(new LineBorder(Color.lightGray, 1), "在线用户",
                    TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                    new Font("黑体", Font.BOLD, 25), Color.blue));
            userScrollPane.setPreferredSize(new Dimension(200, 0));

            userList.addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    JList<String> source = (JList<String>) e.getSource();
                    int selectedIndex = source.getSelectedIndex();
                    if (selectedIndex != -1) {
                        String selectedUser = userListModel.getElementAt(selectedIndex);
                        System.out.println("用户点击了：" + selectedUser);
                        // 在这里可以对选中的用户进行处理
                        selectUserName = selectedUser;
                    }
                }
            });
            mainPanel.add(userScrollPane, BorderLayout.EAST);

            // 用户操作面板
            JPanel controlPanel = new JPanel(new GridLayout(1, 3, 10, 10));
            controlPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
            controlPanel.setBackground(new Color(240, 240, 240));
            mainPanel.add(controlPanel, BorderLayout.SOUTH);

            // 强制下线按钮
            JButton kickButton = new JButton("强制下线");
            kickButton.setFont(new Font("黑体", Font.BOLD, 17));
            kickButton.addActionListener(e -> kickUser());
            controlPanel.add(kickButton);

            // 禁言按钮
            JButton muteButton = new JButton("禁言");
            muteButton.setFont(new Font("黑体", Font.BOLD, 17));
            muteButton.addActionListener(e -> muteUser());
            controlPanel.add(muteButton);

            // 解除禁言按钮
            JButton dismuteButton = new JButton("解除禁言");
            dismuteButton.setFont(new Font("黑体", Font.BOLD, 17));
            dismuteButton.addActionListener(e -> dismuteUser());
            controlPanel.add(dismuteButton);

            kickButton.setForeground(new Color(236, 73, 73));
            kickButton.setBackground(new Color(157, 204, 243));
            muteButton.setForeground(new Color(236, 73, 73));
            muteButton.setBackground(new Color(157, 204, 243));
            dismuteButton.setForeground(new Color(236, 73, 73));
            dismuteButton.setBackground(new Color(157, 204, 243));

            // 设置背景色
            mainPanel.setBackground(Color.white);
            messageArea.setBackground(Color.white);
            userList.setBackground(Color.white);

            // 设置窗口可见性
            setVisible(true);
        }


        public void removeUser(String username) {
            userListModel.removeElement(username);
        }

        // 强制下线用户
        private void kickUser() {
            if (selectUserName != null) {
                JOptionPane.showMessageDialog(this, "已强制下线用户: " + selectUserName, "强制下线", JOptionPane.INFORMATION_MESSAGE);
                removeUser(selectUserName);  // 从列表移除用户
                for (ClientAgent client : clients)
                    if (client.myName.equals(selectUserName)) {
                        client.send.println("exit");
                        break;
                    }
                // 更新日志
                ui.showMessage("用户" + selectUserName + "强制下线");
            } else
                JOptionPane.showMessageDialog(this, "请选择要强制下线的用户", "错误", JOptionPane.ERROR_MESSAGE);
        }

        // 禁言用户
        private void muteUser() {
            if (selectUserName != null) {
                JOptionPane.showMessageDialog(this, "已禁言用户: " + selectUserName, "禁言", JOptionPane.INFORMATION_MESSAGE);
                for (ClientAgent client : clients)
                    if (client.myName.equals(selectUserName)) {
                        client.send.println("mute");
                        break;
                    }
                // 更新日志
                ui.showMessage("用户" + selectUserName + "被禁言");
            } else {
                JOptionPane.showMessageDialog(this, "请选择要禁言的用户", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void dismuteUser() {
            if (selectUserName != null) {
                JOptionPane.showMessageDialog(this, "解除禁言用户: " + selectUserName, "解除禁言", JOptionPane.INFORMATION_MESSAGE);
                for (ClientAgent client : clients)
                    if (client.myName.equals(selectUserName)) {
                        client.send.println("dismute");
                        break;
                    }
                // 更新日志
                ui.showMessage("用户" + selectUserName + "解除禁言");
            } else {
                JOptionPane.showMessageDialog(this, "请选择要禁言的用户", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }

        public void addUser(String username) {
            userListModel.addElement(username);
        }

        public void remove(String username) {
            int id = userListModel.indexOf(username);
            userListModel.remove(id);
        }

        public void showMessage(String message) {
            LocalDateTime currentTime = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedTime = currentTime.format(formatter);
            messageArea.append(formattedTime + "：" + message + "\n");
        }
    }
}