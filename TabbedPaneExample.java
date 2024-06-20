import javax.swing.*;
import java.awt.*;

public class TabbedPaneExample {
    private JFrame frame;
    private JTabbedPane tabbedPane;

    public TabbedPaneExample() {
        frame = new JFrame("Tabbed Pane Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLocationRelativeTo(null); // 居中显示

        tabbedPane = new JTabbedPane();

        // 创建几个面板作为选项卡的内容
        JPanel panel1 = createPanel("Tab 1 Content", Color.RED);
        JPanel panel2 = createPanel("Tab 2 Content", Color.BLUE);
        JPanel panel3 = createPanel("Tab 3 Content", Color.GREEN);

        // 添加选项卡到选项卡面板
        tabbedPane.addTab("Tab 1", panel1);
        tabbedPane.addTab("Tab 2", panel2);
        tabbedPane.addTab("Tab 3", panel3);

        // 将选项卡面板添加到主窗口
        frame.add(tabbedPane, BorderLayout.CENTER);

        frame.setVisible(true);
    }

    private JPanel createPanel(String content, Color color) {
        JPanel panel = new JPanel();
        panel.setBackground(color);
        JLabel label = new JLabel(content);
        label.setFont(new Font("Arial", Font.BOLD, 24));
        panel.add(label);
        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new TabbedPaneExample();
            }
        });
    }
}
