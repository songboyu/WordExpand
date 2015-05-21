package com.task.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.sql.Statement;
import java.util.List;

/**
 * 获取数据库连接，释放资源
 * 
 * @author new
 * 
 */
public class TaskDBManager {
	/**
	 * 获取数据库联接对象
	 * 
	 * @return 获取的的数据库联接对象
	 */
	// 连接池方式
	
	private String driver		= "com.mysql.jdbc.Driver";
	private String url			= "jdbc:mysql://125.211.198.185:3306/word_expand_task";
	private String user			= "bluetech";
	private String password		= "No.9332";//改成自己的mysql密码
//	private String url			= "jdbc:mysql://127.0.0.1/word_expand_task";
//	private String user			= "root";
//	private String password		= "123123";//改成自己的mysql密码
	private Connection conn 	= null;
	private Statement st		= null;
	private ResultSet rs		= null;
	
	public void getConnection() {
		try {
			if(conn != null && !conn.isClosed())
				return;
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			Class.forName(driver);
			conn=DriverManager.getConnection(url, user, password);
			
			if(!conn.isClosed())
				System.out.println("连接成功！");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 释放资源
	 * 
	 * @param conn
	 *            数据库连接对象
	 * @param st
	 *            语句对象
	 * @param rs
	 *            结果集对象
	 */
	public void closeAll(Connection conn, Statement st, ResultSet rs) 	{
		try {
			if (rs != null && !rs.isClosed()) {
				Statement t = rs.getStatement();
				rs.close();
				if(t != null && !t.isClosed())
					t.close();
			}
			if (st != null && !st.isClosed()) {
				st.close();
			}
			if (conn != null && !conn.isClosed()) {
				conn.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void closeAll() 	{
		try {
			if (rs != null && !rs.isClosed()) {
				Statement t = rs.getStatement();
				rs.close();
				if(t != null && !t.isClosed())
					t.close();
			}
			if (st != null && !st.isClosed()) {
				st.close();
			}
			if (conn != null && !conn.isClosed()) {
				conn.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 执行查询语句
	 * @param sql 查询条件，sql语句
	 * @param params 参数列表
	 * @return 返回查询结果
	 * @throws SQLException  sql执行异常
	 * @throws SQLTimeoutException 超时异常
	 */
	public ResultSet query(String sql, List<Object>params) 
			throws SQLException,SQLTimeoutException{
		PreparedStatement ps = conn.prepareStatement(sql);
		if(params != null){
			for(Object param: params){
				if(param instanceof Integer){
					ps.setInt(params.indexOf(param) + 1, (Integer) param);
				}
				else if(param instanceof String){
					ps.setString(params.indexOf(param) + 1, (String) param);
				}
			}
		}
		rs = ps.executeQuery();
		return rs;
	}

	/**
	 * 执行插入语句
	 * @param sql 插入sql语句
	 * @param params 参数列表
	 * @return 返回是否插入成功
	 * @throws SQLException sql执行异常
	 * @throws SQLTimeoutException sql超时异常
	 */
	public boolean insert(String sql, List<Object>params) 
			throws SQLException,SQLTimeoutException{
		PreparedStatement ps = conn.prepareStatement(sql);
		for(Object param: params){
			if(param instanceof Integer){
				ps.setInt(params.indexOf(param) + 1, (Integer) param);
			}
			else if(param instanceof String){
				ps.setString(params.indexOf(param) + 1, (String) param);
			}
		}
		int count = ps.executeUpdate();
		closeAll(null,ps,null);
		return count > 0;
	}
	
	/**
	 * 执行更新语句
	 * @param updateSql 更新语句
	 * @param params 参数列表
	 * @return 返回是否更新成功
	 * @throws SQLException
	 * @throws SQLTimeoutException
	 */
	public boolean update(String updateSql, List<Object>params) 
			throws SQLException,SQLTimeoutException{
		PreparedStatement ps = conn.prepareStatement(updateSql);
		if(params != null){
			for(Object param: params){
				if(param instanceof Integer){
					ps.setInt(params.indexOf(param) + 1, (Integer) param);
				}
				else if(param instanceof String){
					ps.setString(params.indexOf(param) + 1, (String) param);
				}
			}
		}
		int count = ps.executeUpdate();
		closeAll(null,ps,null);
		return count > 0;
	}
	
}
