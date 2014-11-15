package com.test;

import cn.com.cjf.CJFBeanFactory;
import cn.com.cjf.ChineseJF;
import de.tudarmstadt.ukp.wikipedia.api.Category;
import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;

public class WikiTest02 {
	public static void main(String args[]) throws Exception {
		// 数据库连接参数配置
		DatabaseConfiguration dbConfig = new DatabaseConfiguration();
		dbConfig.setHost("localhost");
		dbConfig.setDatabase("wiki");
		dbConfig.setUser("root");
		dbConfig.setPassword("13936755635");
		dbConfig.setLanguage(Language.chinese);
		// 创建Wikipedia处理对象
		Wikipedia wiki = new Wikipedia(dbConfig);
		String title = "";
		// 创建类对象
		Category cat = wiki.getCategory(title);
		StringBuilder sb = new StringBuilder();
		String LF = WikiConstants.LF;
		// 类别名
		sb.append("Title : " + cat.getTitle() + LF );
		sb.append(LF);
		// 类别的父类信息
		sb.append("# super categories : " + cat.getParents().size() + LF);
		for (Category parent : cat.getParents()) {
		    sb.append("  " + parent.getTitle() + LF);
		}
		sb.append(LF);       
		// 类别的子类信息
		sb.append("# sub categories : " + cat.getChildren().size() + LF);
		for (Category child : cat.getChildren()) {
		    sb.append("  " + child.getTitle() + LF); 
		}
		sb.append(LF);
		// 类别下的所有页面
		sb.append("# pages : " + cat.getArticles().size() + LF);
		for (Page page : cat.getArticles()) {
		    sb.append("  " + page.getTitle() + LF);
		}
		ChineseJF chinesdJF = CJFBeanFactory.getChineseJF();
		String janText = chinesdJF.chineseFan2Jan(sb.toString());
		System.out.println(janText); 
	}
}
