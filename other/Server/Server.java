/**
*@BelongsProject: online_talk
*@BelongsPackage: other.Client.Client.Server
*@Author: 弘树
*@CreateTime: 2024-06-18  11:48
*@Description: TODO
*@Version: 1.0
*/

package other.Server;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.LineBorder;

public class Server extends JFrame {

    // server 对象
    private ServerSocket server;

    // other.Client.Client's socket对象
    private Socket socket;

    // 存放客户端Client
    List<Client> list;

    // 窗口标签
    private JLabel label;

    // 面板用于容纳文本区和按钮
    private JPanel labelPanel, clientPanel;

    // 显示区
    private JTextArea centerTextArea, clientTextArea;

    public Server(int port) throws IOException {
        server = new ServerSocket(port);

        // 控制台输出服务器端口号和ip地址
        System.out.println(server.getLocalPort());

        System.out.println(InetAddress.getLocalHost().getHostAddress());

        // 初始化存放Client的链表
        list = new ArrayList<Client>();

        setTitle("服务器");

        // 图形界面GUI
        create();

        // 添加客户端
        addClient();
    }

    private void create() {
        setSize(500, 500);

        // 窗口label，显示服务器名字
        label = new JLabel("10086 服务器");

        label.setFont(new Font("宋体", 5, 15));

        labelPanel = new JPanel();

        labelPanel.setSize(150, 20);

        labelPanel.add(label, BorderLayout.CENTER);

        // 窗口center部分，显示接收到的Client的消息
        centerTextArea = new JTextArea(" ---聊天室已连接--- " + "\r\n");

        centerTextArea.setFont(new Font("微软雅黑", 5, 13));

        centerTextArea.setEditable(false); // 不可编辑

        centerTextArea.setBackground(Color.LIGHT_GRAY);

        // 窗口client部分，显示当前在线的Client

        clientTextArea = new JTextArea("--在线Client--" + "\r\n");

        clientTextArea.setEditable(false); // 不可编辑

        //装载clientTextArea的容器
        clientPanel = new JPanel(new FlowLayout());

        clientPanel.setBorder(new LineBorder(null));

        clientPanel.add(clientTextArea);

        //JFrame框架用于添加各种容器
        add(clientPanel, BorderLayout.EAST);

        add(labelPanel, BorderLayout.NORTH);

        add(new JScrollPane(centerTextArea), BorderLayout.CENTER);

        setVisible(true);

        setResizable(false); // 窗口大小不可调整

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }

    // 添加客户端
    private void addClient() throws IOException {
        // 循环监听客户端的连接请求并逐个分配线程
        while (true) {

            // 接受客户端连接请求并保存
            socket = server.accept();

            // 图形界面输出当前客户端ip地址以及在线人数
            String ip = socket.getInetAddress().getHostAddress();

            centerTextArea.append(ip + "连接成功，当前客户端数为：" + (list.size() + 1) + "\r\n");

            Client client = new Client(socket);

            // 添加用户到列表
            list.add(client);

            // 为用户创建并启动一个接受线程
            new Thread(client).start();

            //【在线客户端】更新通知
            client.update();
        }
    }

    // 客户端处理类（【接受线程】用于不断接受消息 同时不影响自己发送消息）
    class Client implements Runnable {
        String name;

        Socket socket;

        // 输出流
        private PrintWriter out;

        // 输入流
        private BufferedReader in;

        public Client(Socket socket) throws IOException {

            this.socket = socket;

            // 获得相应Client's Socket 输入流
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // 获得相应Client's Socket 输出流
            out = new PrintWriter(socket.getOutputStream());

            //获取当前Client的名字并更新clientTextArea
            name = in.readLine();

            clientTextArea.append(name+"\r\n");
        }

        // 向对应流发送消息
        public void send(String str) {
            out.println(str);
            out.flush();
        }

        // 给【每一个客户端】发送更新clientTextArea的消息
        public void update() {

            StringBuffer sBuffer = new StringBuffer("update:");

            clientTextArea.setText("--在线Client--\r\n");

            for(int i = 0; i < list.size(); i ++) {
                clientTextArea.append(list.get(i).name+"\r\n");
                sBuffer.append(list.get(i).name+" ");
            }

            for(int i = 0; i < list.size(); i ++) {
                list.get(i).out.println(sBuffer);
                list.get(i).out.flush();
            }

        }

        @Override
        public void run() {
            try {
                String aLine;
                boolean flag = true;

                while (flag && (aLine = in.readLine()) != null) {
                    // 处理并保存当前客户端发来的消息（消息前加上当前客户端名字）
                    String str = this.name + "说：" + aLine;

                    // 内容显示
                    centerTextArea.append(str + "\r\n");

                    // 给每一个保存在List中的客户端（除了当前客户端）转发当前客户端发来的消息
                    for (int i = 0; i < list.size(); i++) {
                        Client client = list.get(i);
                        if (client != this) {
                            client.send(str);
                            /* * 如果当前客户端发来的消息是“Good Bye”表示其要下线 * 服务端则转告给【其他的客户端】该客户端下线的消息并进行对 * 当前客户端Socket关闭的处理 */
                            if (aLine.equals("Good Bye")) {
                                client.send(this.name + "已离线");
                                flag = false;
                            }
                        }
                    }
                }
            } catch (Exception e1) {

            } finally {
                try {
                    // 当前客户端Socket关闭处理

                    // 1.链表中移除该Client
                    if (list.contains(this))
                        list.remove(this);

                    /* * 2.更新服务器界面中的【在线客户端窗口】以及 * 为其他的客户端发送【更新在线客户端窗口】的消息 */
                    update();

                    System.out.println(this.name+"已离开");

                    // 3.输出输入流关闭处理
                    socket.shutdownOutput();
                    socket.shutdownInput();
                    socket.close();
                } catch (Exception e) {
                    System.out.println(this.name + "出现异常强行关闭");
                }
            }
        }

    }

    public static void main(String[] args) throws IOException {
        Server Test = new Server(10086);
    }

}


