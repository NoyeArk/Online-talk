/**
 * @BelongsProject: online_talk
 * @BelongsPackage: Server
 * @Author: yanhongwei
 * @CreateTime: 2024-06-18  14:12
 * @Description: TODO
 * @Version: 1.0
 */

package Server;

import Client.Client;
import com.mysql.cj.util.StringUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Server extends JFrame {
    private ServerSocket serverSocket;
    private Map<Socket, String> mp = new HashMap<>();
    private List<ClientEvent> clients = new ArrayList<>();

    // 界面相关
    private JTextArea messageArea;
    private JList<String> userList;
    private DefaultListModel<String> userListModel;

    public Server(int port) throws IOException {
        createUi();
        serverSocket = new ServerSocket(port);
        System.out.println("服务器正在端口" + port + "上运行....");
    }

    public void start() {
        while (true) {
            try {
                // 等待客户端进行连接
                Socket socket = serverSocket.accept();
                System.out.println("新的客户端发送请求：" + socket);

                // 保存这个socket
                mp.put(socket, "null");

                // 创建一个新的线程用于处理客户的请求
                ClientEvent client = new ClientEvent(socket);

                // 添加到当前所有用户请求的列表中
                clients.add(client);
                client.start();

                // 在界面中显示该用户
                userListModel.addElement("用户111");

                // 广播给其他用户有一个用户上线
//                broadcastMessage();

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
        private Socket socket;
        private PrintWriter receive;
        private BufferedReader send;

        public ClientEvent(Socket socket) {
            this.socket = socket;
            try {
                // 用于向客户端发送消息
                receive = new PrintWriter(socket.getOutputStream(), true);
                // 用于从客户端接受消息
                send = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                System.out.println("服务器启动用户线程时错误：" + e.getMessage());
            }
        }

        public void sendMessage(String message) {
            receive.println(message);
        }

        public void run() {
            try {
                String inputLine;
                while ((inputLine = send.readLine()) != null) {
                    System.out.println("收到客户端消息：" + inputLine);
                    if (inputLine.contains("###")) {
                        String username = inputLine.substring(3);
                        mp.put(socket, username);
                    }
//                    System.out.println("我的sokcet是：" + this.socket);
                    // 收到客户端消息，广播给所有客户端
                    broadcastMessage(inputLine, this);
                    // 展示在消息日志中
                    messageArea.append(inputLine);
                }
            } catch (IOException e) {
                System.out.println("Error handling client: " + e.getMessage());
            } finally {
                try {
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
