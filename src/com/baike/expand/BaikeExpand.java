package com.baike.expand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.baike.entity.BaikeCategory;
import com.baike.entity.BaikeEntry;
import com.baike.entity.CandidateCombination;
import com.entity.RelatedEntity;

/**
 * 利用wiki数据进行实体集合扩展
 *
 */
public class BaikeExpand {
	
	private static double PROB_PROP = 0.5;
	
	/**
	 * 根据多个种子词进行实体扩展
	 * @param seeds 种子词数组
	 * @return 返回所有类别的实体
	 */
	public static Map<String, RelatedEntity> expandSeeds(String[] seeds){
		//创建百度百科数据获取器
		BaikeFetcher fetcher = new BaikeFetcher();
		//将所有种子词映射为种子词相关的词条集合
		//并利用页面集合计算获取最佳候选条目组合
		//再由该条目组合获取其中各个条目的分类集合
		//再根据分类集合获取他们的公共分类集合
		Map<String, Set<BaikeEntry>> pageMap = fetcher.getBaikeEntryMap(seeds);
		CandidateCombination suitableCombination = selectSuitableCandiDate(pageMap, fetcher);
		Set<Set<BaikeCategory>> categoryMap = new HashSet<Set<BaikeCategory>>();
		getCateGory(categoryMap, fetcher, suitableCombination);
		Set<BaikeCategory> commonCategories = expand(
				categoryMap);
		//由公共分类集合获取全部相关实体map
		Map<String,Set<BaikeEntry>> entityMap = 
				BaikeFetcher.getEntityByCategory(commonCategories);
		Map<String, RelatedEntity> relatedEntityMap = 
				new HashMap<String, RelatedEntity>();
		List<String> removeEntityList = new ArrayList<String>();
		double totalScore = 0.0;
		//遍历全部相关实体map
		//获取相关实体对象集合同时计算每个相关实体对象的相关度得分
		for(Entry<String, Set<BaikeEntry>> entry: entityMap.entrySet()){
			int pageCount = entry.getValue().size();
			//遍历每个分类的相关实体
			//创建相关实体对象
			//计算相关度得分
			for(BaikeEntry p: entry.getValue()){
				RelatedEntity re = new RelatedEntity();
				re.setEntityTitle(p.getEntryName());
				String title = re.getEntityTitle();
				//如果分类标题中包含该实体标题，则加入该删除的实体列表
				if(entry.getKey().contains(title) ||
						title.contains(entry.getKey())){
					removeEntityList.add(title);
				}
				//定义并计算百科得分
				//百科得分计算方法为候选条目与原始条目之间的相关度的和
				double baikeScore = 0.0;
				for(BaikeEntry mp: suitableCombination.getCandiDateSet()){
					//如果原始实体标题中包含该实体标题，
					//则将该实体加入该删除的实体列表
					if(mp.getEntryName().contains(title)){
						removeEntityList.add(title);
					}
					baikeScore += fetcher.calcRelevance(p, mp, true) * 1.0/pageCount;
				}
				totalScore += baikeScore;
				//假如相关实体集合中已经包含该实体，则累加其相关度得分
				//否则，将该实体加入集合中
				if(relatedEntityMap.containsKey(title)){
					RelatedEntity tmp_re = relatedEntityMap.get(title);
					String cat = "["+entry.getKey()+"]";
					if(!tmp_re.getCategoryTitle().contains(cat)){
						tmp_re.setCategoryTitle(tmp_re.getCategoryTitle() + 
								cat);
					}
					tmp_re.setBaikeScore(baikeScore + tmp_re.getWikiScore());
					relatedEntityMap.put(title, tmp_re);
				} else{
					re.setBaikeScore(baikeScore);
					re.setCategoryTitle("["+entry.getKey()+"]");
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
			//重置百科得分
			for(String key: relatedEntityMap.keySet()){
				RelatedEntity re = relatedEntityMap.get(key);
				double score = re.getBaikeScore() / avgScore;
				re.setBaikeScore(score);
				re.setWikiScore(score/2);
				re.setSealScore(score);
				

			}
		}
		return relatedEntityMap;
//		return null;
	}
	
	/**
	 * 根据候选条目组合获取分类集合
	 * @param categoryMap 分类集合
	 * @param fetcher wiki数据获取器
	 * @param c 候选条目组合
	 */
	private static void getCateGory(
			Set<Set<BaikeCategory>> categoryMap,
			BaikeFetcher fetcher,
			CandidateCombination c){
		if(c == null)
			return;
		//遍历候选组合中的候选集合
		//获取每个候选的分类集合
		//将分类集合加入分类列表中
		Set<BaikeEntry> s = c.getCandiDateSet();
		if(s == null)
			return;
		for(BaikeEntry mp: s){
			Set<BaikeCategory> cat = fetcher.getCategories(mp);
			categoryMap.add(cat);
		}
	}
	
 	/**
 	 * 根据当前所有分类集合搜寻共同分类集合
 	 * @param categorySet 分类集合
	 * @return 返回最终获得的共同分类集合
 	 */
 	private static Set<BaikeCategory> expand(
			Set<Set<BaikeCategory>> categorySet){
		if(categorySet.size() == 0)
			return null;
		
		Set<BaikeCategory> commonCategories = null;
		//获取全部分类集合的公共分类集合并作为结果返回
		for(Set<BaikeCategory> s:categorySet){
			if(commonCategories == null){
				commonCategories = new HashSet<BaikeCategory>();
				commonCategories.addAll(s);
				
			}
			else
				commonCategories.retainAll(s);
		}
		return commonCategories;
	}
 	
 	/**
 	 * 获取最佳条目候选组合
 	 * @param oriMap 原始条目map
 	 * @param fetcher wiki
 	 * @return 返回最佳条目候选组合
 	 */
 	private static CandidateCombination selectSuitableCandiDate(
 			Map<String, Set<BaikeEntry>> oriMap,
 			BaikeFetcher fetcher){
 		//利用原始条目map创建候选组合列表以便选取最佳候选组合
 		List<CandidateCombination> conbinationList = buildCandidateCombination(oriMap);
 		//遍历候选组合列表，并计算组合的概率得分、相关度得分及最终得分
 		for(CandidateCombination c: conbinationList){
 			fetcher.calcProbScore(c);
 			fetcher.calcRelScore(c);
 			c.calcScore(PROB_PROP);
// 			for(BaikeEntry b:c.getCandiDateSet()){
// 				System.out.print(b.getEntryId() + "\t");
// 			}
// 			System.out.println(c.getRealScore());
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
 			List<BaikeEntry> oldPageList,
 			List<String> seedSet,
 			Map<String, Set<BaikeEntry>> oriMap,
 			int level, int levelCount){
 		//获取该层号表示的种子词的候选集合
 		Set<BaikeEntry> candidateSet = oriMap.get(seedSet.get(level));
 		
 		//遍历候选集合，获取候选集合的全排列
 		for(BaikeEntry entry: candidateSet){
 			//创建新的候选列表
 			List<BaikeEntry> newPageList = null;
 			//如果之前的候选列表为null（即该递归层为第一层），则创建新的候选列表
 			//否则，利用原来的候选列表创建新的候选列表
 			//创建后将循环中的条目加入列表中
 	 		if(oldPageList == null)
 	 			newPageList = new ArrayList<BaikeEntry>();
 	 		else
 	 			newPageList = new ArrayList<BaikeEntry>(oldPageList);
 	 		BaikeEntry mEntry = entry;
 	 		mEntry.setProb(1 / (double)candidateSet.size());
 	 		newPageList.add(mEntry);
 	 		//如果不是最后一层（即所有种子词的一个条目都在候选列表中），则向下递归一层
 	 		//否则，创建新的CandidateCombination对象并遍历候选列表
 	 		//将候选条目加入新的CandidateCombination中，
 	 		//并将该CandidateCombination对象加入候选组合列表中
 			if(level < levelCount - 1){
 				buildCandidateCombination(conbinationList,
 						newPageList, seedSet, oriMap, level+1, levelCount);
 			} else{
 				CandidateCombination c = new CandidateCombination();
 				for(BaikeEntry be: newPageList){
 					c.addCandidate(be);
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
 			Map<String, Set<BaikeEntry>> oriMap){
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
