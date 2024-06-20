/**
 * @BelongsProject: online_talk
 * @BelongsPackage: Server
 * @Author: horiki
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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Server extends JFrame {
    private final DatabaseConnect db;
    private final ServerSocket serverSocket;
    private final Map<Socket, String> mp = new HashMap<>();
    private final List<ClientEvent> clients = new ArrayList<>();

    // 界面相关
    private JTextArea messageArea;
    private JList<String> userList;
    private DefaultListModel<String> userListModel;

    public Server(int port) throws IOException {
        // 实例化数据库连接对象
        db = new DatabaseConnect();
        // 显示界面
        createUi();
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
                ClientEvent client = new ClientEvent(socket);

                // 添加到当前所有用户请求的列表中
                clients.add(client);
                client.start();
            } catch (IOException e) {
                System.out.println("Error in server: " + e.getMessage());
                break;
            }
        }
    }

    private void createUi() {
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
        messageArea.setFont(new Font("黑体", Font.PLAIN, 14));
        JScrollPane messageScrollPane = new JScrollPane(messageArea);
        messageScrollPane.setBorder(BorderFactory.createTitledBorder(new LineBorder(Color.lightGray, 1), "消息日志",
                TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                new Font("黑体", Font.BOLD, 16), Color.darkGray));
        mainPanel.add(messageScrollPane, BorderLayout.CENTER);

        // 创建在线用户列表
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setFont(new Font("黑体", Font.PLAIN, 14));
        JScrollPane userScrollPane = new JScrollPane(userList);
        userScrollPane.setBorder(BorderFactory.createTitledBorder(new LineBorder(Color.lightGray, 1), "在线用户",
                TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                new Font("黑体", Font.BOLD, 16), Color.darkGray));
        userScrollPane.setPreferredSize(new Dimension(200, 0));
        mainPanel.add(userScrollPane, BorderLayout.EAST);

        // 用户操作面板
        JPanel controlPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        controlPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        controlPanel.setBackground(new Color(240, 240, 240));
        mainPanel.add(controlPanel, BorderLayout.SOUTH);

        // 强制下线按钮
        JButton kickButton = new JButton("强制下线");
        kickButton.setFont(new Font("黑体", Font.PLAIN, 14));
        kickButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                kickUser();
            }
        });
        controlPanel.add(kickButton);

        // 禁言按钮
        JButton muteButton = new JButton("禁言");
        muteButton.setFont(new Font("黑体", Font.PLAIN, 14));
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

    // 从在线用户列表移除用户
    public void removeUser(String username) {
        userListModel.removeElement(username);
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

    // 当一个用户发送消息时将该消息给所有连接的客户端
    public synchronized void broadcastMessage(String message, ClientEvent excludeClient) {
        for (ClientEvent client : clients) {
            if (client != excludeClient) {
                client.sendMessage(message);
            }
        }
    }

    // 处理客户端的线程
    private class ClientEvent extends Thread {
        private final Socket socket;
        private PrintWriter send;
        private BufferedReader receive;
        private String myName;

        public ClientEvent(Socket socket) {
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

        public void sendMessage(String message) {
            send.println(message);
        }

        public void run() {
            // 用于接受当前socket客户端发送的消息
            try {
                String message;
                while ((message = receive.readLine()) != null) {
                    System.out.println("收到客户端消息：" + message);

                    // 解析客户端发送的消息类型
                    // 1.login
                    if (message.contains("login:")) {
                        int st = message.indexOf(":");
                        int ed = message.indexOf(" ");
                        String username = message.substring(st + 1, ed);
                        String password = message.substring(ed + 1);
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
                            userListModel.addElement(username);
                            // 添加socket-username映射
                            mp.put(socket, username);
                            this.myName = username;
                        }
                        else {
                            // 登录失败
                            send.println("false");
                        }
                    }
                    // 2.message
                    else if (message.contains("messageOnly")) {
                        int st = message.indexOf(":");
                        int ed = message.indexOf(" ");
                        String username = message.substring(st + 1, ed);
                        String msg = message.substring(ed + 1);
                        System.out.println(username + " " + msg);
                        for (ClientEvent client : clients) {
                            if (client.myName.equals(username)) {
                                client.sendMessage("private:" + myName + " " + msg);
                                break;
                            }
                        }

                    }
                    else if (message.contains("message:")) {
                        String content = mp.get(socket) + "：" + message.substring(8);
                        // 收到客户端消息，广播给所有客户端
                        broadcastMessage(content, this);
                        // 展示在消息日志中
                        messageArea.append(content + "\n");
                    }
                }
            } catch (IOException e) {
                System.out.println("Error handling client: " + e.getMessage());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } finally {
                try {
                    String username = mp.get(socket);
                    // 通知每个用户删除这个用户
                    broadcastMessage("delete:" + username, this);
                    // 从服务器端界面中删除指定用户
                    int id = userListModel.indexOf(username);
                    userListModel.remove(id);
                    // 删除这个映射
                    mp.remove(socket);

                    // 关闭流和socket
                    send.close();
                    receive.close();
                    socket.close();
                    clients.remove(this);
                    System.out.println("Client disconnected: " + socket);
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
}
