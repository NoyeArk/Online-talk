/**
 * @BelongsProject: online_talk
 * @BelongsPackage: other.Client.Client
 * @Author: 弘树
 * @CreateTime: 2024-06-18  13:52
 * @Description: TODO
 * @Version: 1.0
 */

package other.Client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.LineBorder;

public class Client extends JFrame implements Runnable {
    // Client_Name
    private String name;

    // other.Client.Client's socket对象
    private Socket socket;

    // 输出流
    private PrintWriter out;

    // 输入流
    private BufferedReader in;

    // 用于关闭线程
    Thread receivethread;

    // 窗口标签
    private JLabel label;

    // 面板用于容纳文本区和按钮
    private JPanel buttomPanel, inputPanel, labelPanel, centenPanel;

    // 显示区以及输入区
    private JTextArea centerTextArea, inputTextArea, clientTextArea;

    // 发送与清除按钮
    private JButton send, clear;

    //other.Client.Client
    public Client(String name, Socket socket) throws IOException {

        this.name = name;

        this.socket = socket;

        // 控制台输出端口号以及主机地址
        System.out.println(socket.getLocalPort());

        System.out.println(InetAddress.getLocalHost().getHostAddress());

        // 得到输入输出流
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        out = new PrintWriter(socket.getOutputStream());

        // 发送名字给服务器
        out.println(name);
        out.flush();

        setTitle(name);

        // Client端GUI
        create();

        // 创建并启动自己的接收线程
        receivethread = new Thread(this);

        receivethread.start();

        // 监听程序启动
        setActionLister();
    }

    //GUI
    private void create() {
        setSize(500, 500);

        // 窗口label
        label = new JLabel("10086 聊天室");

        label.setFont(new Font("宋体", 5, 15));

        labelPanel = new JPanel();

        labelPanel.setSize(150, 20);

        labelPanel.add(label, BorderLayout.CENTER);

        // 窗口center部分
        centerTextArea = new JTextArea(" ---聊天室已连接--- " + "\r\n");

        centerTextArea.setFont(new Font("微软雅黑", 5, 13));

        centerTextArea.setEditable(false); // 不可编辑

        centerTextArea.setBackground(Color.LIGHT_GRAY);

        // 窗口client部分
        clientTextArea = new JTextArea("--在线Client--" + "\r\n");

        clientTextArea.setEditable(false); // 不可编辑

        clientTextArea.setBorder(new LineBorder(null));

        // centerPanel 充当center和client的容器
        centenPanel = new JPanel(new BorderLayout());

        centenPanel.add(new JScrollPane(centerTextArea), BorderLayout.CENTER);

        centenPanel.add(clientTextArea, BorderLayout.EAST);

        // 窗口button按钮
        send = new JButton("发送");

        clear = new JButton("清空");

        buttomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));

        buttomPanel.add(clear);

        buttomPanel.add(send);

        // 窗口input部分
        inputPanel = new JPanel(new BorderLayout());

        inputTextArea = new JTextArea();

        inputTextArea = new JTextArea(7, 20);

        inputPanel.add(new JScrollPane(inputTextArea), BorderLayout.CENTER);

        inputPanel.add(buttomPanel, BorderLayout.SOUTH);

        add(labelPanel, BorderLayout.NORTH);

        add(centenPanel, BorderLayout.CENTER);

        add(inputPanel, BorderLayout.SOUTH);

        setVisible(true);

        setResizable(false); // 窗口大小不可调整

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    //inpuTextArea以及按钮send&clear的监听程序
    private void setActionLister() {
        // 关闭窗口时通知服务器本客户端已关闭
        addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
            try {
                if(out != null) {
                    out.println("Good Bye");
                    out.flush();
                }
            }
            catch (Exception e2) {
                System.out.println("客户端已关闭");
            }
            }

        });

        //点击send按钮将inputTextArea的消息全部发送
        send.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                String aLine = inputTextArea.getText();

                inputTextArea.setText("");	//输入框清空

                centerTextArea.append("你说： " + aLine + "\r\n");

                try {

                    out.println(aLine);

                    out.flush();

                    if (aLine.equals("Good Bye")) {

                        //接受线程中断
                        receivethread.interrupt();

                        socket.shutdownOutput();

                        socket.shutdownInput();

                        socket.close();
                    }
                } catch (Exception e1) {
                    System.out.println("已断开");
                }
            }
        });

        // 键盘监听事件
        inputTextArea.addKeyListener(new KeyAdapter() {

            public void keyReleased(KeyEvent e) {

                //CTRL+ENTER相当于点击send按钮
                if (e.getKeyCode() == KeyEvent.VK_ENTER && e.isControlDown()) {
                    send.doClick();
                }

            }

        });

        // 清除inputTextArea的内容
        clear.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                inputTextArea.setText("");
            }
        });

    }

    @Override
    public void run() {

        try {

            String aLine = "";

            while ((aLine = in.readLine()) != null) {

                //centerTextAre显示接受的消息
                if (!aLine.split(":")[0].equals("update"))
                    centerTextArea.append(aLine + "\r\n");

                    //在綫client更新
                else {
                    String[] strings = (aLine.split(":")[1]).split(" ");
                    clientTextArea.setText("--在线Client--\r\n");
                    for (String s : strings)
                        clientTextArea.append(s + "\r\n");
                }
            }

        } catch (Exception e) {

            centerTextArea.append("当前连接已断开\r\n");

            System.out.println("当前连接已断开");

        }
    }

    public static void main(String[] args) throws IOException {

        new Client("Client5", new Socket("localhost", 10086));

        // new clientGUI("Client2", new Socket("localhost", 10086));
    }

}

