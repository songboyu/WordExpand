package com.baike.expand;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.baike.entity.BaikeCategory;
import com.baike.entity.BaikeEntry;
import com.baike.util.DBManager;

/**
 * 与百度百科数据进行交互
 * @author Xiang
 *
 */
public class Baike {
	/**
	 * 根据词条id获取百科词条
	 * @param entryId 百度百科词条id
	 * @return 返回百科条目对象
	 */
	public static BaikeEntry getBaikeEntryById(String entryId){
		String entryName = null;
		//创建sql语句，并设置查询参数以获取结果
		String sql = "select COUNT(*),e.ent_name from "+
				"(select * from inlinks where inlink = ?) as i "+
				"INNER JOIN "+
				"(select ent_name,ent_id from entries where ent_id=?) as e "+
				"on e.ent_id = i.inlink "+
				"GROUP BY e.ent_id,e.ent_name";
		List<Object> params = new ArrayList<Object>();
		params.add(entryId);
		params.add(entryId);
		ResultSet rs = null;
		int count = 0;
		try {
			rs = DBManager.query(sql, params);
			if(rs.next()){
				count = rs.getInt(1);
				entryName = rs.getString(2);
			}
		} catch (SQLTimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			//关闭结果集
			DBManager.closeAll(null, null, rs);
		}
		//如果entryName不为null，则新创建词条对象并返回
		if(entryName != null){
			BaikeEntry be = new BaikeEntry(entryId, entryName);
			be.setInlinkCount(count);
			return be;
		}
		return null;
	}

	/**
	 * 根据词条信息获取其所有分类
	 * @param be
	 * @return
	 */
	public static Set<BaikeCategory> getCategories(BaikeEntry be){
		Set<BaikeCategory> rSet = new HashSet<BaikeCategory>();
		String sql = "SELECT t.tid,t.t_name from tags as t "+
				"INNER JOIN relations as r "+
				"on t.tid = r.tid "+
				"where r.ent_id = ?";
		List<Object> params = new ArrayList<Object>();
		params.add(be.getEntryId());
		ResultSet rs = null;
		try {
			rs = DBManager.query(sql, params);
			while(rs.next()){
				BaikeCategory c = new BaikeCategory(rs.getString(1), rs.getString(2));
				rSet.add(c);
			}
		} catch (SQLTimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			//关闭结果集
			DBManager.closeAll(null, null, rs);
		}
		return rSet;
	}

	/**
	 * 根据词条信息获取指向该词条的所有页面id
	 * @param entry 词条信息
	 */
	public static void getInlinks(BaikeEntry entry){
		String sql = "SELECT count(ent_id) from inlinks where inlink = ?";
		List<Object> params = new ArrayList<Object>();
		params.add(entry.getEntryId());
		ResultSet rs = null;
		try {
			rs = DBManager.query(sql, params);
			if(rs.next()){
				entry.setInlinkCount(rs.getInt(1));
			}
		} catch (SQLTimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			//关闭结果集
			DBManager.closeAll(null, null, rs);
		}
	}

	/**
	 * 根据词条信息获取指向该词条的所有页面id
	 * @param entry 词条信息
	 */
	public static void getInlinks(List<BaikeEntry> entryList){
		
		StringBuffer sql = new StringBuffer("select inlink, count(ent_id)  "+
				"from inlinks where inlink in (");
		int i = 0;
		Map<String, Integer> indexMap = new HashMap<String, Integer>();
		for(BaikeEntry b: entryList){
			indexMap.put(b.getEntryId(), i);
			if(i==0){
				sql.append("'"+b.getEntryId()+"'");
				
			}
			else{
				sql.append(",'"+b.getEntryId()+"'");
			}
			i++;
		}
		sql.append(") GROUP BY inlink");
		List<Object> params = new ArrayList<Object>();
		
		
		ResultSet rs = null;
		try {
			rs = DBManager.query(sql.toString(), params);
			if(rs.next()){
				entryList.get(indexMap.get(rs.getString(1))).setInlinkCount(rs.getInt(2));
			}
		} catch (SQLTimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			//关闭结果集
			DBManager.closeAll(null, null, rs);
		}
	}
	
	/**
	 * 获取某一类别下的所有词条
	 * @param c 类别信息
	 * @return 返回词条集合
	 */
	public static Set<BaikeEntry> getEntityByCategory(BaikeCategory c){
		Set<BaikeEntry> rSet = new HashSet<BaikeEntry>();
		String sql = "call getEntriesByTag(?)";
		Map<Object, Boolean> params = new LinkedHashMap<Object, Boolean>();
		params.put(c.getId(), false);
		ResultSet rs = null;
		CallableStatement cs = null;
		try {
			cs = DBManager.executeCallable(sql, params);
			rs = cs.getResultSet();
			BaikeEntry be0 = null;
			if(rs.next()){
				be0 = new BaikeEntry(rs.getString(1), rs.getString(2));
			}
			Set<String> links0 = new HashSet<String>();
			if(cs.getMoreResults()){
				rs = cs.getResultSet();
				while(rs.next())
					links0.add(rs.getString(1));
			}
			if(be0 != null){
				be0.setInlinks(links0);
				rSet.add(be0);
			}
			while(cs.getMoreResults()){
				rs = cs.getResultSet();
				BaikeEntry be = null;
				if(rs.next()){
					
					be = new BaikeEntry(rs.getString(1), rs.getString(2));
				}
				Set<String> links = new HashSet<String>();
				if(cs.getMoreResults()){
					rs = cs.getResultSet();
					while(rs.next())
						links.add(rs.getString(1));
				}
				if(be != null){
					be.setInlinks(links);
					rSet.add(be);
				}
			}
		} catch (SQLTimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println(c.getId());
			e.printStackTrace();
		} finally{
			//关闭结果集
			DBManager.closeAll(null, cs, rs);
		}
		return rSet;
	}
	
	/**
	 * 获取某一类别下的百科条目数
	 * @param c 类别信息
	 * @return 返回该类别下的百科条目数
	 */
	public static int getEntityCountByCategory(BaikeCategory c){
		String sql = "select count(*) from (select ent_id from relations where tid=?) as r";
		List<Object> params = new ArrayList<Object>();
		params.add(c.getId());
		ResultSet rs = null;
		int count = 0;
		try {
			rs = DBManager.query(sql, params);
			if(rs.next()){
				count = rs.getInt(1);
			}
		} catch (SQLTimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println(c.getId());
			e.printStackTrace();
		} finally{
			//关闭结果集
			DBManager.closeAll(null, null, rs);
		}
		return count;
	}
	
	/**
	 * 查询两个词条是否直接相关
	 * @param id1 词条1id
	 * @param id2 词条2id
	 * @return 返回是否直接相关
	 */
	public static boolean isDirectRel(String id1, String id2){
		String sql = "SELECT count(*) FROM "+
				"inlinks "+
				"where (ent_id=? and inlink = ?) or "+
				"(ent_id = ? and inlink = ?)";
		List<Object> params = new ArrayList<Object>();
		params.add(id1);
		params.add(id2);
		params.add(id2);
		params.add(id1);
		ResultSet rs = null;
		boolean flag = false;
		try {
			rs = DBManager.query(sql, params);
			if(rs.next()){
				int count = rs.getInt(1);
				if(count > 0)
					flag = true;
			}
		} catch (SQLTimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			//关闭结果集
			DBManager.closeAll(null, null, rs);
		}
		return flag;
	}

	/**
	 * 获取两个词条相同的inlink个数
	 * @param id1 词条1id
	 * @param id2 词条2id
	 * @return 返回相同的inlink个数
	 */
	public static int getIntersectCount(String id1, String id2){
		int count = 0;
		String sql = "select count(*) from inlinks "+
				"where inlink = ? AND ent_id "+
				"in( select i.ent_id from inlinks "+
				"i where i.inlink = ?)";
		List<Object> params = new ArrayList<Object>();
		params.add(id1);
		params.add(id2);
		ResultSet rs = null;
		try {
			rs = DBManager.query(sql, params);
			if(rs.next()){
				count = rs.getInt(1);
			}
		} catch (SQLTimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			//关闭结果集
			DBManager.closeAll(null, null, rs);
		}
		return count;
	}
	
}
