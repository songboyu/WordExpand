package com.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import cn.com.cjf.CJFBeanFactory;
import cn.com.cjf.ChineseJF;
import de.tudarmstadt.ukp.wikipedia.api.Category;
import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;


public class WikiTest {
	public static void main(String args[]) throws Exception {
		// 数据库连接参数配置
		DatabaseConfiguration dbConfig = new DatabaseConfiguration();
		dbConfig.setHost("localhost");
		dbConfig.setDatabase("wiki");
		dbConfig.setUser("root");
		dbConfig.setPassword("13936755635");
		dbConfig.setLanguage(Language.chinese);

		Wikipedia wiki = new Wikipedia(dbConfig);
		
		String seed1 = "王老吉";
		String seed2 = "加多宝";
		
		Page page1 = wiki.getPage(seed1); 
		Page page2 = wiki.getPage(seed2);      
		
		List<String> categorys1 = new ArrayList<String>();
		List<String> categorys2 = new ArrayList<String>();
		
		for (Category category : page1.getCategories()) {
			categorys1.add(category.getTitle().toString());
		}
		for (Category category : page2.getCategories()) {
			categorys2.add(category.getTitle().toString());
		}
		
		categorys1.retainAll(categorys2);
		
		for(String category : categorys1){
			System.out.println(category);
		}
		
	}
}
