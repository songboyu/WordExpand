package com.baike;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.tags.Div;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.ParagraphTag;
import org.htmlparser.tags.Span;
import org.htmlparser.util.NodeList;

import com.baike.util.DBManager;
import com.baike.util.WikiHttpClient;
import com.seal.util.Helper;

public class BaikeFetch {
	public static Logger log = Logger.getLogger(BaikeFetch.class);

	private final static String ID_REX 	= "subview/(.*?).htm|view/(.*?).htm";
	private	 static Pattern idPattern = Pattern.compile(ID_REX);

	/**
	 * @param seed
	 * 输入种子词
	 * 输出百度百科中该词页面链接
	 * @return String word_page_url
	 */
	@SuppressWarnings("serial")
	public static Map<String, String> getWordPageURL(String seed){
		String searchURL = "http://baike.baidu.com/search/word?word="+seed;
		Map<String,String> word_page_urls = new HashMap<String, String>();
		try {
			Parser parser = new Parser(searchURL);
			parser.setEncoding("UTF-8"); 


			NodeFilter filter = new NodeFilter() {
				public boolean accept(Node node) {
					if (node instanceof LinkTag && 
							node.getParent() instanceof ParagraphTag &&
							node.getParent().getParent()!=null &&
							node.getParent().getParent().getText().startsWith("li class=\"list-dot list-dot-paddingleft\"") &&
							node.getText().startsWith("a target=_blank")) {
						return true;
					} else {
						return false;
					}
				}
			};
			NodeList nodelist = parser.extractAllNodesThatMatch(filter); 
			int size = nodelist.size()==0?1:nodelist.size();

			if(size == 1){
				parser = new Parser(searchURL);
				parser.setEncoding("UTF-8"); 

				filter = new NodeFilter() {
					public boolean accept(Node node) {
						if (node.getPreviousSibling() instanceof Span &&
								node.getParent()!=null &&
								node.getParent().getParent()!=null &&
								node.getParent().getParent().getParent()!=null &&
								node.getParent().getParent().getParent() instanceof Div &&
								((Div)(node.getParent().getParent().getParent())).getAttribute("class")!=null &&
								((Div)(node.getParent().getParent().getParent())).getAttribute("class").equals("polysemeBodyCon")){
							return true;
						} else {
							return false;
						}
					}
				};
				nodelist = parser.extractAllNodesThatMatch(filter);

				size = nodelist.size()==0?1:nodelist.size();
				if(size == 1){
					word_page_urls.put(parser.getURL(), seed);
				}else{
					for (Node node : nodelist.toNodeArray()) { 
						try{
							LinkTag link = (LinkTag) node; 
							word_page_urls.put(link.getLink(), seed+"："+link.getLinkText());
						}catch(Exception e){
							Span span = (Span) node;
							word_page_urls.put(parser.getURL(), seed+"："+span.getStringText());
						}
					}
				}
			}
			else{
				for (Node node : nodelist.toNodeArray()) {  
					LinkTag link = (LinkTag) node; 
					word_page_urls.put(link.getLink(),link.getLinkText());
				}
			}
			log.info("【"+seed+"】有"+size+"个语义");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return word_page_urls;
	}

	/**
	 * @param seed
	 * 输入种子词
	 * 获取百度百科中该词类别标签集合
	 * @return 返回标签集合
	 */
	public static Set<String> getCategorysFromWeb(String seed){
		Set<String> categorys = new HashSet<String>();
		try {
			Map<String,String> word_page_urls = getWordPageURL(seed);
			for(String url : word_page_urls.keySet()){
				Parser parser = new Parser(url);
				parser.setEncoding("UTF-8"); 

				NodeFilter filter = new NodeFilter() {
					public boolean accept(Node node) {
						if (node instanceof LinkTag && 
								(node.getText().startsWith("a href=\"/fenlei/") || 
										node.getText().startsWith("a class=\"taglist\""))) {
							return true;
						} else {
							return false;
						}
					}
				};
				NodeList nodelist = parser.extractAllNodesThatMatch(filter); 
				String word = word_page_urls.get(url);

				Set<String> sub_categorys = new HashSet<String>();
				for (Node node : nodelist.toNodeArray()) {  
					LinkTag link = (LinkTag) node; 
					sub_categorys.add(link.getLinkText());
				}
				log.info(Helper.repeat('·', 20-word.length())+sub_categorys);
				categorys.addAll(sub_categorys);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return categorys;
	}

	/**
	 * @param seed
	 * 输入种子词
	 * 获取百度百科中该词类别标签集合
	 * @return 返回标签集合
	 */
	public static Map<String,String> getCategorysFromDb(String seed){
		Map<String,String> categorys = new HashMap<String,String>();
		Map<String,String> word_page_urls = getWordPageURL(seed);
		for(String url : word_page_urls.keySet()){
			Matcher idMatcher = idPattern.matcher(url);
			String semantics = word_page_urls.get(url);
			log.info(url.substring(22)+ Helper.repeat('-', 70-url.length()+15)+semantics);
			
			if(idMatcher.find()){
				String id = idMatcher.group(1);
				if(id == null){
					id = idMatcher.group(2);
				}else{
					id = "s_"+id;
				}
				categorys.putAll(getCategoryByEntId(id));
			}
		}
		return categorys;
	}

	public static Map<String,String> getCategoryByEntId(String id){
		Map<String,String> categorys = new HashMap<String,String>();
		String sql = "select a.tid,b.t_name from relation a join tag b on a.tid=b.tid where ent_id = ?";
		List<Object> params = new ArrayList<Object>();
		params.add(id);
		try {
			ResultSet rs = DBManager.query(sql, params);
			while(rs.next()){
				String cid = rs.getString(1);
				String c_name = rs.getString(2);
				categorys.put(cid, c_name);
			}
			DBManager.closeAll(null, null, rs);
		} catch (SQLTimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.info(categorys.values());
		return categorys;
	}

	public static String getCategoryNameByCId(String id){
		String sql = "select t_name from tag where tid = ?";
		List<Object> params = new ArrayList<Object>();
		params.add(id);
		try {
			ResultSet rs = DBManager.query(sql, params);
			if(rs.next())
				return rs.getString(1);
			DBManager.closeAll(null, null, rs);
		} catch (SQLTimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * 根据分类集合，获取每个分类的所有实体
	 * @param categorys 分类集合
	 * @return 返回所有实体
	 */
	public static Map<String,Set<String>> getWordForAllCategory(Map<String,String> categorys){
		Map<String,Set<String>> wordsMap = new HashMap<String, Set<String>>();
		for(String c : categorys.keySet()){
//			String cat = getCategoryNameByCId(e);
			String cat = categorys.get(c);
			if(cat.length() == 0)
				continue;
			Set<String> words = getRelatedWord(c, 500, 1);
			if(words != null)
				wordsMap.put(cat, words);
		}
		return wordsMap;
	}

	/**
	 * 输入百度百科分类链接，获取该分类的所有实体
	 * @param tagId  百度百科分类标签id
	 * @return 返回所有实体集合
	 */
	public static Set<String> getRelatedWord(
			String tagId,
			int limit,
			int index){
		Set<String> wordSet = new HashSet<String>();
		String sql = "select ent_name from entries where ent_id in (select ent_id from relation where tid=?)";
		List<Object> params = new ArrayList<Object>();
		params.add(tagId);
		ResultSet rs;
		try {
			rs = DBManager.query(sql, params);
			while(rs.next())
				wordSet.add(rs.getString(1));
			DBManager.closeAll(null, null, rs);
		} catch (SQLTimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return wordSet;
	}

	/**
	 * 获取百度百科分类页面中表格内的实体
	 * @param uri 页面地址
	 * @return 返回实体集合
	 */
	public static Set<String> getRelatedWord(String uri)
	{
		String content = WikiHttpClient.get(uri);
		String tableId = getTableId(content);
		if(tableId == null)
			return null;
		return getTableData(tableId);
	}

	/**
	 * 获取列表id
	 * @param content 页面内容
	 * @return 返回列表id
	 */
	public static String getTableId(String content){
		String id = null;
		Pattern p = Pattern.compile("div id=\"rs-container-(.*?)\"{1,1}");
		Matcher m = p.matcher(content);
		if(m.find())
		{
			id = m.group(1);
			int start = id.indexOf('-');
			id = id.substring(start+1);
		}

		return id;
	}

	/**
	 * 通过tableid获取表格数据
	 * @param id
	 * @return 返回表格数据
	 */
	public static Set<String> getTableData(String id){
		Set<String> wordSet = new HashSet<String>();
		String uri = "http://baike.baidu.com/guanxi/jsondata?" +
				"action=getViewLemmaData&args=%5B0%2C8%2C%7B%22" +
				"fentryTableId%22%3A"+id+"%2C%22" +
				"lemmaId%22%3Anull%2C%22" +
				"subLemmaId%22%3A0%7D%2Cfalse%5D";
		String content = WikiHttpClient.get(uri);
		Pattern p = Pattern.compile("<a class=\\\\\"link-inner\\\\\".*?>(.*?)<");
		Matcher m = p.matcher(content);
		while(m.find())
		{
			String w = encodingtoStr(m.group(1));
			wordSet.add(w);
		}
		return wordSet;
	}

	/**
	 * Unicode转汉字
	 * @param str
	 * @return
	 */
	public static String encodingtoStr(String str){
		Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");
		Matcher matcher = pattern.matcher(str);
		char ch;
		while (matcher.find()) {
			ch = (char) Integer.parseInt(matcher.group(2), 16);
			str = str.replace(matcher.group(1), ch + "");
		}
		return str;
	}

	public static Map<String,Set<String>> baikeExpand(String[] seeds){
		DBManager.getConnection();
		Map<String,String> categorys = null;
		Map<String,String> rCategorys = new HashMap<String,String>();
		boolean flag = true;
		for (String seed : seeds){
			if(flag){
				categorys = getCategorysFromDb(seed);
				flag = false;
			}
			else
				if(categorys != null){
					Map<String,String> categorysFromDb = getCategorysFromDb(seed);
					for(String c : categorysFromDb.keySet()){
						if(categorys.containsKey(c)){
							rCategorys.put(c, categorys.get(c));
						}
					}
				}
		}
		return getWordForAllCategory(rCategorys);
	}

}
