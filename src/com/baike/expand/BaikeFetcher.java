package com.baike.expand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.tags.Div;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.ParagraphTag;
import org.htmlparser.tags.Span;
import org.htmlparser.util.NodeList;

import com.baike.BaikeFetch;
import com.baike.entity.BaikeCategory;
import com.baike.entity.BaikeEntry;
import com.baike.entity.CandidateCombination;
import com.baike.util.DBManager;
import com.seal.util.Helper;

import de.tudarmstadt.ukp.wikipedia.api.Category;
import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.Title;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiTitleParsingException;

public class BaikeFetcher {
	private static Logger log = Logger.getLogger(BaikeFetch.class);
	private static final long BAIKE_ENTRY_COUNT = 1023909;
	private final static String ID_REX 	= "subview/(.*?).htm|view/(.*?).htm";
	private	 static Pattern idPattern = Pattern.compile(ID_REX);
	
	public BaikeFetcher(){
	}
	
	/**
	 * 将所有种子词映射为种子词相关的页面集合
	 * @param seeds 种子词数组
	 * @return 返回映射结果
	 */
	public Map<String, Set<BaikeEntry>> getBaikeEntryMap(String[] seeds){
		DBManager.getConnection();
		Map<String, Set<BaikeEntry>> rMap = new HashMap<String, Set<BaikeEntry>>();
		//遍历种子词并获取其相关页面集合
		for(String seed : seeds){
			rMap.put(seed, getEntrySet(seed));
		}
		return rMap;
	}
	
	/**
	 * 根据种子词获取种子词相关的页面集合
	 * @param seed 种子词
	 * @return 返回种子词相关的页面集合
	 */
	private Set<BaikeEntry> getEntrySet(String seed){
		Set<BaikeEntry> rSet = new HashSet<BaikeEntry>();
		//将种子词送入百度百科搜索引擎搜索，获取词条链接以解析词条id
		Map<String,String> word_page_urls = getWordPageURL(seed);
		//遍历链接集合，解析词条id，并创建词条对象
		for(String url : word_page_urls.keySet()){
			//利用正则表达式解析url并获取词条id
			Matcher idMatcher = idPattern.matcher(url);
			String semantics = word_page_urls.get(url);
			log.info(url.substring(22)+ Helper.repeat('-', 70-url.length()+15)+semantics);
			if(idMatcher.find()){
				String id = idMatcher.group(1);
				if(id == null){
					id = idMatcher.group(2);
				}else{
					id = "s_"+id;
				}
				
				//从数据中获取词条对象并加入集合中
				BaikeEntry entry = getEntryById(id);
				if(entry != null){
					rSet.add(entry);
				}
			}
		}
		return rSet;
	}
	
	private BaikeEntry getEntryById(String entryId){
		
		BaikeEntry entry = Baike.getBaikeEntryById(entryId);
		return entry;
	}
	
	/**
	 * 输入种子词
	 * 输出百度百科中该词页面链接
	 * @param seed 种子词
	 * @return String word_page_url
	 */
	private Map<String, String> getWordPageURL(String seed){
		//定义百度百科搜索url即百科条目url集合
		String searchURL = "http://baike.baidu.com/search/word?word="+seed;
		Map<String,String> word_page_urls = new HashMap<String, String>();
		try {
			//创建html解析器
			Parser parser = new Parser(searchURL);
			parser.setEncoding("UTF-8"); 

			//html内容过滤器，取出条目url
			NodeFilter filter = new NodeFilter() {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1894619904528404753L;

				/**
				 * 解析节点
				 */
				public boolean accept(Node node) {
					if (node instanceof LinkTag && 
							node.getParent() instanceof ParagraphTag &&
							node.getParent().getParent()!=null &&
							node.getParent().getParent().getText().startsWith("li class=\"list-dot list-dot-paddingleft\"") &&
							node.getText().startsWith("a target=_blank")) {
						return true;
					} else {
						return false;
					}
				}
			};
			//利用过滤器获取条目url节点列表
			NodeList nodelist = parser.extractAllNodesThatMatch(filter); 
			int size = nodelist.size()==0?1:nodelist.size();

			if(size == 1){
				parser = new Parser(searchURL);
				parser.setEncoding("UTF-8"); 

				filter = new NodeFilter() {
					/**
					 * 
					 */
					private static final long serialVersionUID = 2869026100914248888L;

					public boolean accept(Node node) {
						if (node.getPreviousSibling() instanceof Span &&
								node.getParent()!=null &&
								node.getParent().getParent()!=null &&
								node.getParent().getParent().getParent()!=null &&
								node.getParent().getParent().getParent() instanceof Div &&
								((Div)(node.getParent().getParent().getParent())).getAttribute("class")!=null &&
								((Div)(node.getParent().getParent().getParent())).getAttribute("class").equals("polysemeBodyCon")){
							return true;
						} else {
							return false;
						}
					}
				};
				nodelist = parser.extractAllNodesThatMatch(filter);

				size = nodelist.size()==0?1:nodelist.size();
				if(size == 1){
					word_page_urls.put(parser.getURL(), seed);
				}else{
					for (Node node : nodelist.toNodeArray()) { 
						try{
							LinkTag link = (LinkTag) node; 
							word_page_urls.put(link.getLink(), seed+"："+link.getLinkText());
						}catch(Exception e){
							Span span = (Span) node;
							word_page_urls.put(parser.getURL(), seed+"："+span.getStringText());
						}
					}
				}
			}
			else{
				for (Node node : nodelist.toNodeArray()) {  
					LinkTag link = (LinkTag) node; 
					word_page_urls.put(link.getLink(),link.getLinkText());
				}
			}
			log.info("【"+seed+"】有"+size+"个语义");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return word_page_urls;
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
	@SuppressWarnings("unused")
	private Set<Page> getOutlinks(Page page){
		Set<Page> rSet = new HashSet<Page>();
		rSet = page.getOutlinks();
		return rSet;
	}
	

	/**
	 * 根据条目获取其分类集合
	 * @param p 条目页面
	 * @return 分类集合
	 */
	public Set<BaikeCategory> getCategories(BaikeEntry be){
		Set<BaikeCategory> categorySet = null;
		//获取分类集合
		categorySet = Baike.getCategories(be);
		return categorySet;
	}
	
	/**
	 * 计算候选组合概率得分
	 * @param c 组合对象
	 */
	public void calcProbScore(CandidateCombination c){
		double score = 0;
		for(BaikeEntry p: c.getCandiDateSet()){
			score += Math.log(1-p.getProb()/2);
		}
		c.setProbScore(-score);
	}
	
	/**
	 * 计算候选组合相关度分数
	 * @param c 组合对象
	 */
	public void calcRelScore(CandidateCombination c){
		Set<BaikeEntry> candidateSet = c.getCandiDateSet();
		List<BaikeEntry> candidateList = new ArrayList<BaikeEntry>(candidateSet);
		double relevance = 0.0;
		//根据公式R(ai,bj)=ln[rel(ai,bj)]
		//计算组合相关度得分
		//首先，计算两两条目的相关度并相加
		//然后对相关度取log值
		for(int i=0;i < candidateList.size() - 1;i++){
			for(int j=i+1;j < candidateList.size();j++){
				relevance += calcRelevance(candidateList.get(i), 
						candidateList.get(j), false);
			}
		}
		relevance = Math.log(relevance+2);
		c.setRelScore(relevance);
	}
	
	/**
	 * 计算两个条目的相关度
	 * @param mp1 条目1
	 * @param mp2 条目2
	 * @param isRelate 是否是计算相关实体相关度
	 * @return 返回两个条目的相关度
	 */
	public double calcRelevance(BaikeEntry e1, BaikeEntry e2, boolean isRelate){
		double relevance = 1.0;
		//首先，获取两个扩展条目的条目对象
		//接着，获取链接到这两个条目的条目id集合
		//然后计算链接到这两个条目的条目id集合的交集
//		Page p1 = mp1.getPage();
//		Page p2 = mp2.getPage();
		int revise = 1;
		
		int X=0,Y=0,Z=0;
		if(!isRelate){
			X = e1.getInlinkCount();
			Y = e2.getInlinkCount();
			Z = Baike.getIntersectCount(e1.getEntryId(), e2.getEntryId());
			if(Baike.isDirectRel(e1.getEntryId(), e2.getEntryId())){
				revise = 3;
			}
		} 
		else{
			Set<String> XLink = e1.getInlinks();
			Set<String> YLink = e2.getInlinks();
			if(XLink.contains(e2.getEntryId()) || YLink.contains(e1.getEntryId()))
			{
				revise = 3;
			}
			X = XLink.size();
			Y = YLink.size();
			Set<String> retain = new HashSet<String>(XLink);
			retain.retainAll(YLink);
			Z = retain.size();
		}
		if( X < 1 || Y < 1 || Z < 1){
			return 0;
		}
		//对各个计算参数进行加1平滑
		//根据公式rel(x,y)=1-[log(max(|X|,|Y|))-log(|X∩Y|)]/[log(|W|)-log(min(|X|,|Y|))]
		//计算两个条目的相关度
		relevance -= (Math.log(Math.max(X+1, Y+1)) - 
				Math.log(Z+1)) / (Math.log(BAIKE_ENTRY_COUNT) - 
						Math.log(Math.min(X+1, Y+1)));
//		System.out.println(e1.getEntryId() + "\t" + e2.getEntryId() + "\t"+relevance);
		return relevance *revise;
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
	 * @return 返回相似实体集合
	 */
	public static Map<String,Set<BaikeEntry>> getEntityByCategory(
			Set<BaikeCategory> commonCategories){
		Map<String,Set<BaikeEntry>> relatedEntity = new HashMap<String, Set<BaikeEntry>>();
		//若公有分类集合为null，则直接返回
		if(commonCategories == null){
			return relatedEntity;
		}
		
		for(BaikeCategory c:commonCategories){
			//如果类别下的条目数过多，则说明该类别颗粒度过大，因此舍弃该条目
			int entityCount = Baike.getEntityCountByCategory(c);
			if(entityCount > 5000){
				continue;
			}
			log.info(c.getId()+" "+c.getTitle()+"  Size:"+entityCount);
			//相似实体集合
			List<BaikeEntry> relatedEntry = new ArrayList<BaikeEntry>();
			//遍历该分类下的每个页面，并将页面实体放入相似实体集合中
			for(BaikeEntry be : Baike.getEntityByCategory(c)){
				relatedEntry.add(be);
			}
//			Baike.getInlinks(relatedEntry);
			//将相似实体集合放入结果中
			relatedEntity.put(c.getTitle(), new HashSet<BaikeEntry>(relatedEntry));
		}
		return relatedEntity;
	}
	
}
