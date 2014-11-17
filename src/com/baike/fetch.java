package com.baike;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.tags.Div;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.ParagraphTag;
import org.htmlparser.tags.Span;
import org.htmlparser.util.NodeList;

import com.seal.util.Helper;

public class fetch {

	public static void main(String[] args) throws Exception {
		Map<String,String> categorys_01 = getCategorys("哈工大");
		Map<String,String> categorys_02 = getCategorys("哈尔滨工程大学");
		categorys_01.keySet().retainAll(categorys_02.keySet());
//		for(String c : categorys_01.keySet()){
//			System.out.println(c+"\t"+categorys_01.get(c));
//		}
		Map<String,Set<String>> entities = getWordForAllCategory(categorys_01);
		for(Entry<String,Set<String>> e:entities.entrySet()){
			System.out.println("----------------------"+e.getKey());
			for(String word:e.getValue()){
				System.out.println(word);
			}
		}
	}

	/**
	 * @param seed
	 * 输入种子词
	 * 输出百度百科中该词页面链接
	 * @return String word_page_url
	 */
	public static Map<String, String> getWordPageURL(String seed){
		String searchURL = "http://baike.baidu.com/search/word?pic=1&sug=1&enc=utf8&word="+seed;
		Map<String,String> word_page_urls = new HashMap<String, String>();
		try {
			Parser parser = new Parser(searchURL);
			parser.setEncoding("UTF-8"); 
			NodeFilter filter = new NodeFilter() {
				public boolean accept(Node node) {
					if (node instanceof LinkTag && 
							node.getParent() instanceof ParagraphTag &&
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
//						boolean m = ((Div)(node.getParent().getParent().getParent())).getAttribute("class").equals("polysemeBodyCon");
						if (node instanceof LinkTag &&
								node.getPreviousSibling() instanceof Span &&
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
				size = nodelist.size()==0?1:nodelist.size()+1;
				word_page_urls.put(searchURL,seed);
				for (Node node : nodelist.toNodeArray()) {  
					LinkTag link = (LinkTag) node; 
					word_page_urls.put(link.getLink(),link.getLinkText());
				}
			}
			else{
				for (Node node : nodelist.toNodeArray()) {  
					LinkTag link = (LinkTag) node; 
					word_page_urls.put(link.getLink(),link.getLinkText());
				}
			}
			System.out.println("【"+seed+"】有"+size+"个语义");

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
	 * @return Map<text,link> categorys
	 */
	public static Map<String,String> getCategorys(String seed){
		Map<String,String> categorys = new HashMap<String, String>();
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
				System.out.print(word);
				Map<String,String> sub_categorys = new HashMap<String, String>();
				for (Node node : nodelist.toNodeArray()) {  
					LinkTag link = (LinkTag) node; 
					sub_categorys.put(link.getLinkText(), link.getLink());
				}
				System.out.println(Helper.repeat('·', 20-word.length())+sub_categorys.keySet());
				categorys.putAll(sub_categorys);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return categorys;
	}

	/**
	 * 根据分类集合，获取每个分类的所有实体
	 * @param categorys 分类集合
	 * @return 返回所有实体
	 */
	public static Map<String,Set<String>> getWordForAllCategory(Map<String,String> categorys){
		Map<String,Set<String>> wordsMap = new HashMap<String, Set<String>>();
		for(Entry<String, String> e : categorys.entrySet()){
			Set<String> words = getRelatedWord(e.getValue(), 500, 1);
			if(words != null)
				wordsMap.put(e.getKey(), words);
		}
		return wordsMap;
	}
	
	/**
	 * 输入百度百科分类链接，获取该分类的所有实体
	 * @param url  百度百科分类链接
	 * @param limit 每页获取实体条数
	 * @param index 当前页面
	 * @return 返回所有实体集合
	 */
	public static Set<String> getRelatedWord(
			String url,
			int limit,
			int index){
		Set<String> wordSet = new HashSet<String>();
		String uri = url +"?limit="+limit +"&index="+index;
		try {
//			System.out.println(uri);
			Parser parser = new Parser(uri);
			parser.setEncoding("UTF-8"); 

			NodeFilter filter = new NodeFilter() {
				public boolean accept(Node node) {
					if (node instanceof LinkTag && 
							(node.getText().startsWith("a class=\"title"))) {
						return true;
					} else {
						return false;
					}
				}
			};
			NodeList nodelist = parser.extractAllNodesThatMatch(filter); 
			if(nodelist.size() > 0){
				for (Node node : nodelist.toNodeArray()) {  
					LinkTag link = (LinkTag) node; 
					wordSet.add(link.getLinkText());
				}
				Set<String> tmp = getRelatedWord(url, limit, index+1);
				if(tmp != null)
					wordSet.addAll(tmp);
				return wordSet;
			}
			else
				return null;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}


}
