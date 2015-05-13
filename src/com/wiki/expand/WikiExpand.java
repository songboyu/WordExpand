package com.wiki.expand;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.com.cjf.CJFBeanFactory;
import cn.com.cjf.ChineseJF;

import com.wiki.entity.CandidateCombination;
import com.wiki.entity.MCategory;
import com.wiki.entity.MPage;
import com.wiki.util.SetUtil;

import de.tudarmstadt.ukp.wikipedia.api.Category;
import de.tudarmstadt.ukp.wikipedia.api.Page;

/**
 * 利用wiki数据进行实体集合扩展
 *
 */
public class WikiExpand {
	
	private static double PROB_PROP = 0.5;
	
	/**
	 * 根据多个种子词进行实体扩展
	 * @param seeds 种子词数组
	 * @return 返回所有类别的实体
	 */
	public static Map<String,Set<String>> expandSeeds(String[] seeds){
		WikiFetcher fetcher = new WikiFetcher();
		//中文简繁转换工具
		ChineseJF chinesdJF = CJFBeanFactory.getChineseJF();
		Map<String, Set<Page>> pageMap = fetcher.getPageMap(seeds);
		CandidateCombination suitableCombination = selectSuitableCandiDate(pageMap, fetcher);
		Set<Map<String,MCategory>> categoryMap = new HashSet<Map<String,MCategory>>();
		getCateGory(categoryMap, fetcher, suitableCombination);
		Set<MCategory> commonCategories = expand(
				categoryMap, 
				true,
				true);
//		System.out.println(suitableCombination.getRealScore());
		
//		for(MPage mp: suitableCombination.getCandiDateSet()){
//			try {
//				System.out.println(chinesdJF.chineseFan2Jan(mp.getPage().getTitle().toString()));
//			} catch (WikiTitleParsingException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		for(Entry<String, Set<Page>> entry: pageMap.entrySet()){
//			System.out.println(entry.getKey()+"--------------");
//			for(Page p: entry.getValue()){
//				try {
//					System.out.println(chinesdJF.chineseFan2Jan(p.getTitle().toString()));
//				} catch (WikiTitleParsingException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		}
//		Map<String,Map<String,MCategory>> categoryMap = new HashMap<String, Map<String,MCategory>>();
//		//根据种子词获取分类集合
//		getCategoryMap(seeds, fetcher, categoryMap);
//		//利用分类集合进行实体扩展
//		Set<MCategory> commonCategories = expand(
//				categoryMap, 
//				true,
//				true);
//		
		return WikiFetcher.getEntityByCategory(commonCategories);
//		return null;
	}
	
	/**
	 * 根据种子词获取分类集合
	 * @param seeds 种子词数组
	 * @param fetcher wiki数据获取器
	 * @param categoryMap 分类集合
	 * @param cateStrMap 分类名称集合
	 */
	@SuppressWarnings("unused")
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
	 * 根据候选条目组合获取分类集合
	 * @param categoryMap 分类集合
	 * @param fetcher wiki数据获取器
	 * @param c 候选条目组合
	 */
	private static void getCateGory(
			Set<Map<String,MCategory>> categoryMap,
			WikiFetcher fetcher,
			CandidateCombination c){
		//遍历候选组合中的候选集合
		//获取每个候选的分类集合
		//将分类集合加入分类列表中
		for(MPage mp: c.getCandiDateSet()){
			Set<Category> cat = fetcher.getCategories(mp.getPage());
			categoryMap.add(SetUtil.setToMap(cat, false, false));
		}
	}
	
	/**
	 * 根据当前所有分类集合搜寻共同分类集合
	 * @param categoryMap 分类集合
	 * @param up 指代是否向上搜寻，其值为true则向上搜寻，否则不向上搜寻
	 * @param down 指代是否向下搜寻，其值为true则向下搜寻，否则不向下搜寻
	 * @return 返回最终获得的共同分类集合
	 */
 	@SuppressWarnings("unused")
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

 	private static Set<MCategory> expand(
			Set<Map<String,MCategory>> categoryMap,
			boolean up,
			boolean down){
		if(categoryMap.size() == 0)
			return null;
		Set<MCategory> commonCategories = null;

		for(Map<String,MCategory> s:categoryMap){
			if(commonCategories == null){
				commonCategories = new HashSet<MCategory>();
				commonCategories.addAll(s.values());
				
			}
			else
				commonCategories.retainAll(s.values());
		}
		if(commonCategories.size() == 0){
			for(Map<String,MCategory> s:categoryMap){
//				System.out.println(seed);
				if(up){
					up = WikiFetcher.getParentCat(s);
				}
				if(down){
					down = WikiFetcher.getChildrenCat(s);
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
 	
 	/**
 	 * 获取最佳条目候选组合
 	 * @param oriMap 原始条目map
 	 * @param fetcher wiki
 	 * @return 返回最佳条目候选组合
 	 */
 	private static CandidateCombination selectSuitableCandiDate(
 			Map<String, Set<Page>> oriMap,
 			WikiFetcher fetcher){
 		//利用原始条目map创建候选组合列表以便选取最佳候选组合
 		List<CandidateCombination> conbinationList = buildCandidateCombination(oriMap);
 		//遍历候选组合列表，并计算组合的概率得分、相关度得分及最终得分
 		for(CandidateCombination c: conbinationList){
 			fetcher.calcProbScore(c);
 			fetcher.calcRelScore(c);
 			c.calcScore(PROB_PROP);
 		}
 		//根据最终得分选取最佳候选组合
 		//遍历全部组合列表，比较最终得分与目前最大得分
 		//若最终得分大于目前最大得分，则重置最佳组合与最大得分
 		double maxScore = 0.0;
 		CandidateCombination suitableCombination = null;
 		for(CandidateCombination c: conbinationList){
 			if(c.getRealScore() > maxScore){
 				maxScore = c.getRealScore();
 				suitableCombination = c;
 			}
 		}
 		return suitableCombination;
 	}
 	
 	/**
 	 * 递归方法获取候选组合列表
 	 * @param conbinationList 候选条目组合列表
 	 * @param oldPageList 已经选择的条目列表
 	 * @param seedSet 种子词列表
 	 * @param oriMap 原始种子词与条目集合map
 	 * @param level 种子词index，即递归层号
 	 * @param levelCount 种子词个数，即需要递归的层数
 	 */
 	private static void buildCandidateCombination(
 			List<CandidateCombination> conbinationList,
 			List<MPage> oldPageList,
 			List<String> seedSet,
 			Map<String, Set<Page>> oriMap,
 			int level, int levelCount){
 		//获取该层号表示的种子词的候选集合
 		Set<Page> candidateSet = oriMap.get(seedSet.get(level));
 		
 		//遍历候选集合，获取候选集合的全排列
 		for(Page page: candidateSet){
 			//创建新的候选列表
 			List<MPage> newPageList = null;
 			//如果之前的候选列表为null（即该递归层为第一层），则创建新的候选列表
 			//否则，利用原来的候选列表创建新的候选列表
 			//创建后将循环中的条目加入列表中
 	 		if(oldPageList == null)
 	 			newPageList = new ArrayList<MPage>();
 	 		else
 	 			newPageList = new ArrayList<MPage>(oldPageList);
 	 		MPage mPage = new MPage(page);
 	 		mPage.setProb(1 / (double)candidateSet.size());
 	 		newPageList.add(mPage);
 	 		//如果不是最后一层（即所有种子词的一个条目都在候选列表中），则向下递归一层
 	 		//否则，创建新的CandidateCombination对象并遍历候选列表
 	 		//将候选条目加入新的CandidateCombination中，
 	 		//并将该CandidateCombination对象加入候选组合列表中
 			if(level < levelCount - 1){
 				buildCandidateCombination(conbinationList,
 						newPageList, seedSet, oriMap, level+1, levelCount);
 			} else{
 				CandidateCombination c = new CandidateCombination();
 				for(MPage p: newPageList){
 					c.addCandidate(p);
 				}
 				conbinationList.add(c);
 			}
 		}
 	}
 	
 	/**
 	 * 创建候选组合列表
 	 * @param oriMap 原始种子词与条目集合map
 	 * @return 返回候选条目组合列表
 	 */
 	private static List<CandidateCombination> buildCandidateCombination(
 			Map<String, Set<Page>> oriMap){
 		//获取种子词个数并将种子词集合转换为list类型
 		int seedCount = oriMap.keySet().size();
 		List<String> seedSet = new ArrayList<String>(oriMap.keySet());
 		//创建候选条目组合列表
 		List<CandidateCombination> conbinationList = new ArrayList<CandidateCombination>();
 		//利用递归方法获取全部候选条目组合列表
 		buildCandidateCombination(conbinationList, null, seedSet, oriMap, 0, seedCount);
 		return conbinationList;
 	}
}
