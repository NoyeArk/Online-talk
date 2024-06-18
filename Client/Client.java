/**
 * @BelongsProject: online_talk
 * @BelongsPackage: Client
 * @Author: yanhongwei
 * @CreateTime: 2024-06-18  14:19
 * @Description: TODO
 * @Version: 1.0
 */

package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client extends JFrame {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    // 界面绘制
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;

    public Client(String serverAddress, int serverPort) throws IOException {
        socket = new Socket(serverAddress, serverPort);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        // 创建界面
        createUi();

        // 启动接收消息的线程
        new Thread(new IncomingReader()).start();
    }

    private void createUi() {
        setTitle("在线聊天室");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null); // 居中显示

        // 布局设置为边界布局
        setLayout(new BorderLayout());

        // 聊天区域（中间）
        chatArea = new JTextArea();
        chatArea.setEditable(false); // 只读
        JScrollPane scrollPane = new JScrollPane(chatArea);
        add(scrollPane, BorderLayout.CENTER);

        // 底部面板，包含消息输入框和发送按钮
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());

        messageField = new JTextField();
        bottomPanel.add(messageField, BorderLayout.CENTER);

        sendButton = new JButton("发送");
        bottomPanel.add(sendButton, BorderLayout.EAST);

        // 添加底部面板到窗口底部
        add(bottomPanel, BorderLayout.SOUTH);

        // 发送按钮事件监听
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        // 显示窗口
        setVisible(true);
    }

    // 发送消息方法
    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            // 在聊天区域显示消息
            chatArea.append("我： " + message + "\n");
            messageField.setText(""); // 清空消息输入框

            // 将消息发送给服务器
            out.println(message);
        }
    }

    public class IncomingReader implements Runnable {
        public void run() {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    // 打印接收到的消息
                    System.out.println(message);
                    // 展示在界面的聊天区域中
                    chatArea.append(message + "\n");
                }
            } catch (IOException e) {
                System.out.println("Error reading from server: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        String serverAddress = "localhost"; // 服务器地址
        int serverPort = 10086; // 服务器端口

        try {
            Client client = new Client(serverAddress, serverPort);

            // 从命令行读取消息并发送给服务器
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            String message;
            while ((message = reader.readLine()) != null) {
                client.sendMessage();
            }
        } catch (IOException e) {
            System.out.println("Error connecting to server: " + e.getMessage());
        }
    }
}
