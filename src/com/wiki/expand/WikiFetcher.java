package com.wiki.expand;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import cn.com.cjf.CJFBeanFactory;
import cn.com.cjf.ChineseJF;

import com.wiki.util.DBConfig;
import com.wiki.util.SetUtil;

import de.tudarmstadt.ukp.wikipedia.api.Category;
import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.Title;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiInitializationException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiTitleParsingException;

public class WikiFetcher {
	/**
	 * 数据库连接参数配置
	 */
	private DatabaseConfiguration dbConfig;
	/**
	 * Wikipedia处理对象
	 */
	private Wikipedia wiki;
	private static Set<String> badCategoryTitle;
	static{
		badCategoryTitle = new HashSet<String>();
		badCategoryTitle.add("本地相关图片与维基数据不同");
		badCategoryTitle.add("本地链接的维基共享资源分类与Wikidata不同");
		badCategoryTitle.add("含有英語的條目");
		badCategoryTitle.add("含有明確引用中文的條目");
		badCategoryTitle.add("含有漢語拼音的條目");
		badCategoryTitle.add("含有电话号码的条目");
		badCategoryTitle.add("含有非中文內容的條目");
		badCategoryTitle.add("带有失效链接的条目");
		badCategoryTitle.add("含有拉丁語的條目");
		badCategoryTitle.add("使用Catnav的页面");
		badCategoryTitle.add("隐藏分类");
		badCategoryTitle.add("维基百科维护");
		badCategoryTitle.add("維基百科特殊頁面");
		badCategoryTitle.add("追蹤分類");
		badCategoryTitle.add("含有滿語的條目");
		badCategoryTitle.add("可能含有拼写错误的蒙文");
		badCategoryTitle.add("含有蒙古語的條目");
		badCategoryTitle.add("特色条目");
		badCategoryTitle.add("含有朝鮮語的條目");
		badCategoryTitle.add("自2014年5月需补充来源的条目");
		badCategoryTitle.add("自2011年5月有未列明来源语句的条目");
		badCategoryTitle.add("有未列明来源语句的条目");
		badCategoryTitle.add("依國家來作的分類");
		badCategoryTitle.add("頁面分類");
		badCategoryTitle.add("可能清空的分類");
		badCategoryTitle.add("各種主題的頁面分類");
		badCategoryTitle.add("母分类");
		badCategoryTitle.add("各类产业");
		badCategoryTitle.add("缺少Wikidata链接的维基共享资源分类");
		badCategoryTitle.add("含有访问日期但无网址的引用的页面");
		badCategoryTitle.add("TaxoboxLatinName");
	}
	public WikiFetcher(){
		dbConfig = DBConfig.getDBConfig();
		try {
			wiki = new Wikipedia(dbConfig);
		} catch (WikiInitializationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 根据关键词获取其分类集合
	 * @param title 关键词
	 * @return 分类集合
	 */
	public Set<Category> getCategories(String title){
		Set<Category> categorySet = null;
		Set<Category> resultSet = new HashSet<Category>();
		try {
			//获取分类集合
			categorySet = wiki.getPage(title).getCategories();
		} catch (WikiApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(categorySet != null){
			for(Category c: categorySet){
				try {
					if(c != null){
						Title t = c.getTitle();
						//判断是否是坏分类（wiki数据中存在一些坏分类，比如"含有拉丁語的條目"等）
						if(t != null &&!badCategoryTitle.contains(t.toString())){
							resultSet.add(c);
						}
					}
				} catch (WikiTitleParsingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		categorySet = null;
		return resultSet;
	}

	/**
	 * 获取父分类集合
	 * @param cat1 子分类集合
	 * @return 返回是否有父分类添加入集合中
	 */
	public static boolean getParentCat(Map<String,MCategory> cat1){
		boolean hasParent = false;
		Iterator<String> it= cat1.keySet().iterator(); 
		Map<String,MCategory> parentMap = new HashMap<String, MCategory>();
		while(it.hasNext()){
			String key = it.next();
			MCategory value = cat1.get(key);
			if(!value.isUp()){
				//获取父分类集合
				Set<Category> parents = getParent(value.getCategory());
				if(parents != null && parents.size() > 0)
				{
					hasParent = true;
					//将父分类集合加入集合
					parentMap.putAll(SetUtil.setToMap(parents,false,true));
				}
				//设置标记，下次不再获取该分类的父类
				value.setUp(true);
			}
		}
		if(parentMap.size() != 0)
			cat1.putAll(parentMap);
		return hasParent;
	}

	/**
	 * 获取某一分类的父分类集合
	 * @param c 一个分类
	 * @return 返回父分类集合
	 */
	private static Set<Category> getParent(Category c){
		Set<Category> parents = c.getParents();
		Set<Category> result = null;
		if(parents != null && parents.size() != 0){
			result = new HashSet<Category>();
			for(Category p :parents){
				try {
					if(c != null){
						Title t = p.getTitle();
						//判断是否是坏分类（wiki数据中存在一些坏分类，比如"含有拉丁語的條目"等）
						if(t != null &&!badCategoryTitle.contains(t.toString())){
							result.add(p);
						}
					}
				} catch (WikiTitleParsingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return result;
	}
	
	/**
	 * 获取子分类集合
	 * @param cat1 父分类集合
	 * @return 返回是否有子分类添加入集合中
	 */
	public static boolean getChildrenCat(Map<String,MCategory> cat1){
		boolean hasChildren = false;
		Iterator<String> it= cat1.keySet().iterator(); 
		Map<String,MCategory> childrenMap = new HashMap<String, MCategory>();
		while(it.hasNext()){
			String key = it.next();
			MCategory value = cat1.get(key);
			if(!value.isDown()){
				//获取子分类集合
				Set<Category> children = getChildren(value.getCategory());
				if(children != null && children.size() > 0)
				{
					hasChildren = true;
					//将子分类集合加入集合
					childrenMap.putAll(SetUtil.setToMap(children,true,false));
				}
				//设置标记，下次不再获取该分类的子类
				value.setDown(true);
			}
		}
		cat1.putAll(childrenMap);
		return hasChildren;
	}

	/**
	 * 获取某一分类的子分类集合
	 * @param c 分类
	 * @return 返回子分类集合
	 */
	private static Set<Category> getChildren(Category c){
		Set<Category> children = c.getChildren();
		Set<Category> result = null;
		if(children != null && children.size() != 0){
			result = new HashSet<Category>();
			for(Category p :children){
				try {
					if(c != null){
						Title t = p.getTitle();
						//判断是否是坏分类（wiki数据中存在一些坏分类，比如"含有拉丁語的條目"等）
						if(t != null &&!badCategoryTitle.contains(t.toString())){
							result.add(p);
						}
					}
				} catch (WikiTitleParsingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	/**
	 * 获取分类名称
	 * @param c 分类
	 * @return 返回分类名称
	 */
	public static String getTitleStrForCat(Category c){
		try {
			Title t = c.getTitle();
			if(t != null)
				return t.toString();
		} catch (WikiTitleParsingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 根据公有分类集合获取每个分类的相似实体集合
	 * @param commonCategories 公有分类集合
	 * @return 返货相似实体集合
	 */
	public static Map<String,Set<String>> getEntityByCategory(Set<MCategory> commonCategories){
		Map<String,Set<String>> relatedEntity = new HashMap<String, Set<String>>();
		//若公有分类集合为null，则直接返回
		if(commonCategories == null){
			return relatedEntity;
		}
		//中文简繁转换工具
		ChineseJF chinesdJF = CJFBeanFactory.getChineseJF();
		for(MCategory c:commonCategories){
			//相似实体集合
			Set<String> relatedStr = new HashSet<String>();
			try {
				//遍历该分类下的每个页面，并将页面实体放入相似实体集合中
				for(Page p : c.getCategory().getArticles()){
					relatedStr.add(chinesdJF.chineseFan2Jan(p.getTitle().toString()));
				}
				
			} catch (WikiApiException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//将相似实体集合放入结果中
			relatedEntity.put(c.getTitle(), relatedStr);
		}
		return relatedEntity;
	}
}
