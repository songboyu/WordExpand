package com.wiki.expand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import cn.com.cjf.CJFBeanFactory;
import cn.com.cjf.ChineseJF;

import com.entity.RelatedEntity;
import com.wiki.entity.CandidateCombination;
import com.wiki.entity.MCategory;
import com.wiki.entity.MPage;
import com.wiki.util.SetUtil;

import de.tudarmstadt.ukp.wikipedia.api.Category;
import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiTitleParsingException;

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
	public static Map<String, RelatedEntity> expandSeeds(String[] seeds){
		//创建wiki数据获取器
		WikiFetcher fetcher = new WikiFetcher();
		//中文简繁转换工具
		ChineseJF chineseJF = CJFBeanFactory.getChineseJF();
		//将所有种子词映射为种子词相关的页面集合
		//并利用页面集合计算获取最佳候选条目组合
		//再由该条目组合获取其中各个条目的分类集合
		//再根据分类集合获取他们的公共分类集合
		Map<String, Set<Page>> pageMap = fetcher.getPageMap(seeds);
		CandidateCombination suitableCombination = selectSuitableCandiDate(pageMap, fetcher);
		Set<Map<String,MCategory>> categoryMap = new HashSet<Map<String,MCategory>>();
		getCateGory(categoryMap, fetcher, suitableCombination);
		Set<MCategory> commonCategories = expand(
				categoryMap, 
				true,
				true,
				0);
		
		//由公共分类集合获取全部相关实体map
		//
		Map<String,Set<Page>> entityMap = 
				WikiFetcher.getEntityByCategory(commonCategories);
		Map<String, RelatedEntity> relatedEntityMap = 
				new HashMap<String, RelatedEntity>();
		List<String> removeEntityList = new ArrayList<String>();
		//遍历全部相关实体map
		//获取相关实体对象集合同时计算每个相关实体对象的相关度得分
		double totalScore = 0.0;
		for(Entry<String, Set<Page>> entry: entityMap.entrySet()){
			int pageCount = entry.getValue().size();
			//遍历每个分类的相关实体
			//创建相关实体对象
			//计算相关度得分
			for(Page p: entry.getValue()){
				RelatedEntity re = new RelatedEntity();
				try {
					re.setEntityTitle(chineseJF.chineseFan2Jan(p.getTitle().toString()));
				} catch (WikiTitleParsingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String title = re.getEntityTitle();
				//如果分类标题中包含该实体标题，则加入该删除的实体列表
				if(entry.getKey().contains(title) ||
						title.contains(entry.getKey())){
					removeEntityList.add(title);
				}
				//定义并计算wiki得分
				//wiki得分计算方法为候选条目与原始条目之间的相关度的和
				double wikiScore = 0.0;
				for(MPage mp: suitableCombination.getCandiDateSet()){
					try {
						//如果原始实体标题中包含该实体标题，
						//则将该实体加入该删除的实体列表
						if(chineseJF.chineseFan2Jan(mp.getPage().getTitle().toString()).contains(title)){
							removeEntityList.add(title);
						}
					} catch (WikiTitleParsingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					wikiScore += fetcher.calcRelevance(p, mp.getPage()) * 1.0/pageCount;
				}
				totalScore += wikiScore;
				//假如相关实体集合中已经包含该实体，则累加其相关度得分
				//否则，将该实体加入集合中
				if(relatedEntityMap.containsKey(title)){
					RelatedEntity tmp_re = relatedEntityMap.get(title);
					if(!tmp_re.getCategoryTitle().contains(entry.getKey())){
						String cat = "["+chineseJF.chineseFan2Jan(entry.getKey())+"]";
						if(!tmp_re.getCategoryTitle().contains(cat)){
							tmp_re.setCategoryTitle(tmp_re.getCategoryTitle() + 
									cat);
						}
					}
					tmp_re.setWikiScore(wikiScore + tmp_re.getWikiScore());
					relatedEntityMap.put(title, tmp_re);
				} else{
					re.setWikiScore(wikiScore);
					re.setCategoryTitle("["+chineseJF.chineseFan2Jan(entry.getKey())+"]");
					relatedEntityMap.put(title, re);
				}
			}
		}
		double avgScore = totalScore / relatedEntityMap.size();
		//遍历删除列表，从集合中删除该实体
		for(String key: removeEntityList){
			if(relatedEntityMap.containsKey(key))
				relatedEntityMap.remove(key);
		}
		if(avgScore != 0){
			//重置wiki得分
			for(String key: relatedEntityMap.keySet()){
				RelatedEntity re = relatedEntityMap.get(key);
				double score = re.getWikiScore() / avgScore;
				
				re.setWikiScore(score);
				re.setBaikeScore(score*2);
				re.setSealScore(score);

			}
		}
		return relatedEntityMap;
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

 	/**
 	 * 根据当前所有分类集合搜寻共同分类集合
 	 * @param categoryMap 分类集合
 	 * @param up 指代是否向上搜寻，其值为true则向上搜寻，否则不向上搜寻
	 * @param down 指代是否向下搜寻，其值为true则向下搜寻，否则不向下搜寻
	 * @param level 搜寻层数
	 * @return 返回最终获得的共同分类集合
 	 */
 	private static Set<MCategory> expand(
			Set<Map<String,MCategory>> categoryMap,
			boolean up,
			boolean down,
			int level){
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
		if(level > 3)
			return commonCategories;
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
				return expand(categoryMap, up, down, level+1);
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
