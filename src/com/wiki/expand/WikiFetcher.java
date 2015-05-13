package com.wiki.expand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.com.cjf.CJFBeanFactory;
import cn.com.cjf.ChineseJF;

import com.wiki.entity.CandidateCombination;
import com.wiki.entity.MCategory;
import com.wiki.entity.MPage;
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
	
	private static final String DSIAMBIG_SUFFIX = "_(消歧义)";
	
	private static final long WIKI_PAGE_COUNT = 1023909;
	/**
	 * 中文简繁转换工具
	 */
	private static ChineseJF chinesdJF = CJFBeanFactory.getChineseJF();
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
	 * 将所有种子词映射为种子词相关的页面集合
	 * @param seeds 种子词数组
	 * @return 返回映射结果
	 */
	public Map<String, Set<Page>> getPageMap(String[] seeds){
		Map<String, Set<Page>> rMap = new HashMap<String, Set<Page>>();
		//遍历种子词并获取其相关页面集合
		for(String seed : seeds){
			rMap.put(seed, getPageSet(seed));
		}
		return rMap;
	}
	
	/**
	 * 根据种子词获取种子词相关的页面集合
	 * @param seed 种子词
	 * @return 返回种子词相关的页面集合
	 */
	private Set<Page> getPageSet(String seed){
		Set<Page> rSet = new HashSet<Page>();
		
		Page oriPage = null;
		try {
			//获取种子词直接相关的页面，，如果存在则加入集合中
			oriPage = wiki.getPage(seed);
			
		} catch (WikiApiException e1) {
		}
		//消歧义页面标志位
		boolean flag = false;
		try {
			//遍历页面所属类别，如果属于消歧义页面，则设置标志位为true
			for(Category c:oriPage.getCategories()){
				String fanTitle = chinesdJF.chineseFan2Jan(c.getTitle().toString());
				if(fanTitle.contains("消歧义")){
					flag = true;
					break;
				}
				
			}
		} catch (WikiApiException e1) {

		}
		//消歧义页面title
		String catSeed = seed;
		//如果该页面不为消歧义页面，则将该页面加入集合中
		//同时设置消岐页面title
		if(!flag && oriPage != null){
			rSet.add(oriPage);
			catSeed += DSIAMBIG_SUFFIX;
		}
		try {
			//获取种子词相关的消岐页面
			//从消岐页面获取相关条目页面并加入集合中
			Page disPage = wiki.getPage(catSeed);
			rSet.addAll(getOutlinks(disPage));
		} catch (WikiApiException e) {
		}
//		for(Page p: rSet){
//			try {
//				System.out.println(p.getTitle());
//			} catch (WikiTitleParsingException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
		return rSet;
	}
	
	/**
	 * 获取指向该页面的所有页面
	 * @param page 页面对象
	 * @return 返回指向该页面的所有页面集合
	 */
	@SuppressWarnings("unused")
	private Set<Page> getInlinks(Page page){
		Set<Page> rSet = new HashSet<Page>();
		rSet = page.getInlinks();
		return rSet;
	} 
	
	/**
	 * 获取该页面指向的所有页面
	 * @param page 页面对象
	 * @return 返回该页面指向的所有页面集合
	 */
	private Set<Page> getOutlinks(Page page){
		Set<Page> rSet = new HashSet<Page>();
		rSet = page.getOutlinks();
		return rSet;
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

	public Set<Category> getCategories(Page p){
		Set<Category> categorySet = null;
		Set<Category> resultSet = new HashSet<Category>();
		//获取分类集合
		categorySet = p.getCategories();
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
	 * 计算候选组合概率得分
	 * @param c 组合对象
	 */
	public void calcProbScore(CandidateCombination c){
		double score = 0;
		for(MPage p: c.getCandiDateSet()){
			score += Math.log(p.getProb());
		}
		c.setProbScore(-score);
	}
	
	/**
	 * 计算候选组合相关度分数
	 * @param c 组合对象
	 */
	public void calcRelScore(CandidateCombination c){
		Set<MPage> candidateSet = c.getCandiDateSet();
		List<MPage> candidateList = new ArrayList<MPage>(candidateSet);
		double relevance = 0.0;
		//根据公式R(ai,bj)=ln[rel(ai,bj)]
		//计算组合相关度得分
		//首先，计算两两条目的相关度并相加
		//然后对相关度取log值
		for(int i=0;i < candidateList.size() - 1;i++){
			for(int j=i+1;j < candidateList.size();j++){
				relevance += calcRelevance(candidateList.get(i), candidateList.get(j));
			}
		}
		relevance = Math.log(relevance);
		c.setRelScore(-relevance);
	}
	
	/**
	 * 计算两个条目的相关度
	 * @param mp1 条目1
	 * @param mp2 条目2
	 * @return 返回两个条目的相关度
	 */
	private double calcRelevance(MPage mp1, MPage mp2){
		double relevance = 1.0;
		//首先，获取两个扩展条目的条目对象
		//接着，获取链接到这两个条目的条目id集合
		//然后计算链接到这两个条目的条目id集合的交集
		Page p1 = mp1.getPage();
		Page p2 = mp2.getPage();
		Set<Integer> X = p1.getInlinkIDs();
		Set<Integer> Y = p2.getInlinkIDs();
		Set<Integer> retain = new HashSet<Integer>(X);
		retain.retainAll(Y);
		
		//根据公式rel(x,y)=1-[log(max(|X|,|Y|))-log(|X∩Y|)]/[log(|W|)-log(min(|X|,|Y|))]
		//计算两个条目的相关度
		relevance -= (Math.log(Math.max(X.size()+1, Y.size()+1)) - 
				Math.log(retain.size()+1)) / (Math.log(WIKI_PAGE_COUNT) - 
				Math.log(Math.min(X.size()+1, Y.size()+1)));
		return relevance;
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
