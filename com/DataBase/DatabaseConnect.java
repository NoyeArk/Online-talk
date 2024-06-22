/**
 * @BelongsProject: online_talk
 * @BelongsPackage: com.DataBase
 * @Author: 弘树
 * @CreateTime: 2024-06-19  19:13
 * @Description: 操作数据库的代码实现
 * @Version: 1.0
 */

package com.DataBase;

import java.sql.*;


public class DatabaseConnect {
    private String sql;
    private Statement stmt;
    private static Connection conn = null;

    public DatabaseConnect() {
        try {
            // 注册JDBC驱动程序
            Class.forName("com.mysql.jdbc.Driver");

            // 打开连接
            System.out.println("连接到数据库...");
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/db_stu?useSSL=false","root","root");
            stmt = conn.createStatement();
        } catch (SQLException se) {
            // 处理JDBC错误
            se.printStackTrace();
        } catch (Exception e) {
            // 处理Class.forName错误
            e.printStackTrace();
        }
    }

    public boolean query(String username, String password) throws SQLException {
        // 执行查询
        System.out.println("查询username为" + username);

        sql = "SELECT password FROM user where username='" + username + "'";
        ResultSet rs = stmt.executeQuery(sql);

        // 处理结果集
        if (rs.next()) {
            // 检索每列
            String pwd = rs.getString("password");

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

    public boolean insert(String username, String password) throws SQLException {
        sql = "INSERT INTO user values('" + username + "' ,'" + password + "');";
        int rowsAffected = stmt.executeUpdate(sql);
        return rowsAffected > 0;
    }
}
