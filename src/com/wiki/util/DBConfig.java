package com.wiki.util;

import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;

/**
 * 数据库配置类，配置数据库
 *
 */
public class DBConfig {
	
	/**
	 * 获取一个数据库配置对象
	 * @return
	 */
	public static DatabaseConfiguration getDBConfig(){
		DatabaseConfiguration dbConfig = new DatabaseConfiguration();
		//设置主机
		dbConfig.setHost("127.0.0.1");
		//设置数据库
		dbConfig.setDatabase("wiki");
		//设置用户名
		dbConfig.setUser("root");
		//设置密码
		dbConfig.setPassword("123123");
		//设置语言
		dbConfig.setLanguage(Language.chinese);
		return dbConfig;
	}
}
