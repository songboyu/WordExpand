package com.wiki.expand;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.wiki.util.SetUtil;

import de.tudarmstadt.ukp.wikipedia.api.Category;

/**
 * 利用wiki数据进行实体集合扩展
 *
 */
public class WikiExpand {
	/**
	 * 根据多个种子词进行实体扩展
	 * @param seeds 种子词数组
	 * @return 返回所有类别的实体
	 */
	public static Map<String,Set<String>> expandSeeds(String[] seeds){
		WikiFetcher fetcher = new WikiFetcher();
		Map<String,Map<String,MCategory>> categoryMap = new HashMap<String, Map<String,MCategory>>();
		//根据种子词获取分类集合
		getCategoryMap(seeds, fetcher, categoryMap);
		//利用分类集合进行实体扩展
		Set<MCategory> commonCategories = expand(
				categoryMap, 
				true,
				true);
		
		return WikiFetcher.getEntityByCategory(commonCategories);
	}
	
	/**
	 * 根据种子词获取分类集合
	 * @param seeds 种子词数组
	 * @param fetcher wiki数据获取器
	 * @param categoryMap 分类集合
	 * @param cateStrMap 分类名称集合
	 */
	private static void getCategoryMap(
			String[] seeds,
			WikiFetcher fetcher,
			Map<String,Map<String,MCategory>> categoryMap){
		//遍历种子词数组，逐一获取分类集合
		for(String seed : seeds){
			Set<Category> cat = fetcher.getCategories(seed);
			categoryMap.put(seed,SetUtil.setToMap(cat, false,false));
		}
	}
	
	/**
	 * 根据当前所有分类集合搜寻共同分类集合
	 * @param categoryMap 分类集合
	 * @param up 指代是否向上搜寻，其值为true则向上搜寻，否则不向上搜寻
	 * @param down 指代是否向下搜寻，其值为true则向下搜寻，否则不向下搜寻
	 * @return 返回最终获得的共同分类集合
	 */
 	private static Set<MCategory> expand(
			Map<String,Map<String,MCategory>> categoryMap,
			boolean up,
			boolean down){
		if(categoryMap.size() == 0)
			return null;
		Set<MCategory> commonCategories = null;

		for(Map<String,MCategory> s:categoryMap.values()){
			if(commonCategories == null){
				commonCategories = new HashSet<MCategory>();
				commonCategories.addAll(s.values());
				
			}
			else
				commonCategories.retainAll(s.values());
		}
		if(commonCategories.size() == 0){
			for(String seed:categoryMap.keySet()){
//				System.out.println(seed);
				if(up){
					up = WikiFetcher.getParentCat(categoryMap.get(seed));
				}
				if(down){
					down = WikiFetcher.getChildrenCat(categoryMap.get(seed));
				}
			}
//			newCateStrList = fetcher.changeCatToStr(newCategoryList);
			if(up || down)
				return expand(categoryMap, up, down);
			else
				return null;
		}
		else{
			return commonCategories;
		}
	}

 	
}
