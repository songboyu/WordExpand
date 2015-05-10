package com.test;

import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.Title;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.Category;
import de.tudarmstadt.ukp.wikipedia.api.Page;


public class WikiTest01 {
	public static void main(String args[]) throws Exception {
		// 数据库连接参数配置
		DatabaseConfiguration dbConfig = new DatabaseConfiguration();
		//设置主机
		dbConfig.setHost("125.211.198.185");
		//设置数据库
		dbConfig.setDatabase("wiki");
		//设置用户名
		dbConfig.setUser("bluetech");
		//设置密码
		dbConfig.setPassword("No.9332");
		//设置语言
		dbConfig.setLanguage(Language.chinese);
		// 创建Wikipedia处理对象
		Wikipedia wiki = new Wikipedia(dbConfig);
		String title = "血小板";
		Page page = wiki.getPage(title);       
		// wikipedia页面的title
		System.out.println("Queried string       : " + title);
		System.out.println("Title                : " + page.getTitle());
		// 是否是消歧页面
		System.out.println("IsDisambiguationPage : " + page.isDisambiguation());       
		// 是否是重定向页面
		System.out.println("redirect page query  : " + page.isRedirect());       
		// 有多少个页面指向该页面
		System.out.println("# of ingoing links   : " + page.getNumberOfInlinks());       
		// 该页面指向了多少个页面
		System.out.println("# of outgoing links  : " + page.getNumberOfOutlinks());
		// 该页面属于多少个类别
		System.out.println("# of categories      : " + page.getNumberOfCategories());
		StringBuilder sb = new StringBuilder();
		String LF = WikiConstants.LF;
		// 页面的所有重定向页面
		sb.append("Redirects" + LF);
		for (String redirect : page.getRedirects()) {
		    sb.append("  " + new Title(redirect).getPlainTitle() + LF);
		}
		sb.append(LF);       
		// 页面的所述的所有类别
		sb.append("Categories" + LF);
		for (Category category : page.getCategories()) {
		    sb.append("  " + category.getTitle() + LF);
		}
		sb.append(LF);
		// 指向该页面的所有页面
		sb.append("In-Links" + LF);
		for (Page inLinkPage : page.getInlinks()) {
		    sb.append("  " + inLinkPage.getTitle() + LF);
		}
		sb.append(LF);
		// 该页面指向的所有页面
		sb.append("Out-Links" + LF);
		for (Page outLinkPage : page.getOutlinks()) {
		    sb.append("  " + outLinkPage.getTitle() + LF);
		}       
		System.out.println(sb);
	}
}
