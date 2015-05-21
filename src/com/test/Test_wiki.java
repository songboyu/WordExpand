package com.test;

import java.util.Map;
import java.util.Map.Entry;

import com.entity.RelatedEntity;
import com.wiki.expand.WikiExpand;

public class Test_wiki {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String[] seeds = {"哈工大","北京大学"};
		Map<String,RelatedEntity> relatedEntities = WikiExpand.expandSeeds(seeds);
		System.out.println(relatedEntities.size());
		for(Entry<String, RelatedEntity> e:relatedEntities.entrySet()){
			System.out.println(e.getKey() + "\t" + 
					e.getValue().getWikiScore() + "\t" +
					e.getValue().getCategoryTitle());
//			for(String title:e.getValue())
//				System.out.println(title);
		}
	}
}
