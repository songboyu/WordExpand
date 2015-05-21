package com.baike.util;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 获取数据库连接，释放资源
 * 
 * @author Xiang
 * 
 */
public class DBManager {
	/**
	 * 获取数据库联接对象
	 * 
	 * @return 获取的的数据库联接对象
	 */
	// 连接池方式
	
	private static String driver="com.mysql.jdbc.Driver";
//	private static String url			= "jdbc:mysql://125.211.198.185:3306/baidubaike2015";
//	private static String user			= "bluetech";
//	private static String password		= "No.9332";
	private static String url="jdbc:mysql://127.0.0.1:3306/baidubaike";
	private static String user="root";
	private static String password="123123";//改成自己的mysql密码
	private static Connection conn 	= null;
	private static Statement st		= null;
	private static ResultSet rs		= null;
	
	public static void getConnection() {
		try {
			Class.forName(driver);
			conn=DriverManager.getConnection(url, user, password);
			
			if(!conn.isClosed())
				;
			
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
	public static void closeAll(Connection conn, Statement st, ResultSet rs) 	{
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
	
	public static void closeAll() 	{
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
	public static ResultSet query(String sql, List<Object>params) 
			throws SQLException,SQLTimeoutException{
		PreparedStatement ps = conn.prepareStatement(sql);
		if(params != null){
			int i = 1;
			for(Object param: params){
				
				if(param instanceof Integer){
					ps.setInt(i, (Integer) param);
				}
				else if(param instanceof String){
					ps.setString(i, (String) param);
				}
				i++;
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
	public static boolean insert(String sql, List<Object>params) 
			throws SQLException,SQLTimeoutException{
		PreparedStatement ps = conn.prepareStatement(sql);
		int i = 1;
		for(Object param: params){
			
			if(param instanceof Integer){
				ps.setInt(i, (Integer) param);
			}
			else if(param instanceof String){
				ps.setString(i, (String) param);
			}
			i++;
		}
		int count = ps.executeUpdate();
		closeAll(conn,ps,null);
		return count > 0;
	}

	/**
	 * 执行更新语句
	 * @param sql 更新sql语句
	 * @param params 参数列表
	 * @return 返回是否更新成功
	 * @throws SQLException sql执行异常
	 * @throws SQLTimeoutException sql超时异常
	 */
	public static boolean update(String sql, List<Object>params) 
			throws SQLException,SQLTimeoutException{
		PreparedStatement ps = conn.prepareStatement(sql);
		int i = 1;
		for(Object param: params){
			
			if(param instanceof Integer){
				ps.setInt(i, (Integer) param);
			}
			else if(param instanceof String){
				ps.setString(i, (String) param);
			}
			i++;
		}
		int count = ps.executeUpdate();
		closeAll(conn,ps,null);
		return count > 0;
	}

	/**
	 * 批量执行语句
	 * @param sql 批量sql语句
	 * @param params 参数列表
	 * @return 返回是否执行成功
	 * @throws SQLException 抛出SQL异常
	 */
	public static boolean executeBatch(String sql, List<Object[]>params) throws SQLException{
		boolean flag = false;

		for(int i = 1;i < params.size();i++){
			if(params.get(i).length != params.get(i-1).length){
				return flag;
			}
		}
		conn.setAutoCommit(false);
		PreparedStatement ps = conn.prepareStatement(sql);
		for(Object[] param: params){
			int i = 1;
			for(Object p: param){
				if(p instanceof Integer){
					ps.setInt(i, (Integer) p);
				}
				else if(p instanceof String){
					ps.setString(i, (String) p);
				}
				i++;
			}
			ps.addBatch();
		}
		ps.executeBatch();
		conn.setAutoCommit(true);
		flag = true;
		return flag;
	}

	/**
	 * 调用MySQL存储过程进行查询
	 * @param sql 调用MySQL存储过程的sql语句
	 * @param params 调用参数
	 * @return CallableStatement cs
	 * @throws SQLException 抛出sql执行异常
	 */
	public static CallableStatement executeCallable(String sql, Map<Object, Boolean>params) 
			throws SQLException{
		CallableStatement cs = conn.prepareCall(sql);
		
		if(params != null){
			int i = 1;
			Iterator<Entry<Object, Boolean>> it = params.entrySet().iterator();
			while(it.hasNext()){
				Entry<Object, Boolean> param = (Entry<Object, Boolean>) it.next();
				if(param.getKey() instanceof Integer){

					cs.setInt(i, (Integer) param.getKey());
					if(param.getValue())
						cs.registerOutParameter(i, Types.INTEGER);
				}
				else if(param.getKey() instanceof String){
					cs.setString(i, (String) param.getKey());
					if(param.getValue())
						cs.registerOutParameter(i, Types.VARCHAR);
				}
				i++;
			}
		}
		cs.execute();
		
		return cs;
		
	}
	
}
