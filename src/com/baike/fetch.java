package com.baike;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.ParagraphTag;
import org.htmlparser.util.NodeList;

import com.seal.util.Helper;

public class fetch {

	public static void main(String[] args) throws Exception {
		Map<String,String> categorys_01 = getCategorys("胡锦涛");
		Map<String,String> categorys_02 = getCategorys("温家宝");
		categorys_01.keySet().retainAll(categorys_02.keySet());
		System.out.println("-------------------------------");
		System.out.println("共属类别"+Helper.repeat('·', 16)+categorys_01.keySet());
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
			System.out.println("【"+seed+"】有"+size+"个语义");
			if(size == 1){
				word_page_urls.put(searchURL,seed);
			}
			else{
				for (Node node : nodelist.toNodeArray()) {  
					LinkTag link = (LinkTag) node; 
					word_page_urls.put(link.getLink(),link.getLinkText());
				}
			}

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
}
