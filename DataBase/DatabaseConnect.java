package DataBase;

import java.sql.*;
import java.util.Objects;

/**
 * @BelongsProject: online_talk
 * @BelongsPackage: DataBase
 * @Author: yanhongwei
 * @CreateTime: 2024-06-19  19:13
 * @Description: TODO
 * @Version: 1.0
 */

public class DatabaseConnect {
    private static Connection conn = null;

    public DatabaseConnect() {
        try {
            // 注册JDBC驱动程序
            Class.forName("com.mysql.jdbc.Driver");

            // 打开连接
            System.out.println("连接到数据库...");
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/db_stu?useSSL=false","root","root");
        } catch (SQLException se) {
            // 处理JDBC错误
            se.printStackTrace();
        } catch (Exception e) {
            // 处理Class.forName错误
            e.printStackTrace();
        }
        System.out.println("Goodbye!");
    }

    public boolean querySno(String sno, String pwd) throws SQLException {
        // 执行查询
        System.out.println("查询sno为" + sno);
        Statement stmt = conn.createStatement();
        System.out.println("34324");

        String sql;
        sql = "SELECT password FROM user where sno=" + sno;
        ResultSet rs = stmt.executeQuery(sql);

        // 处理结果集
        if (rs.next()) {
            // 检索每列
            String password = rs.getString("password");

            // 清理环境
            rs.close();
            stmt.close();
            return password.equals(pwd);
        }
        // 清理环境
        rs.close();
        stmt.close();
        return false;
    }
}
