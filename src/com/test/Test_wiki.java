package com.test;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.wiki.expand.WikiExpand;

public class Test_wiki {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String[] seeds = {"哈尔滨工业大学","上海交通大学"};
		Map<String,Set<String>> relatedEntities = WikiExpand.expandSeeds(seeds);
		for(Entry<String, Set<String>> e:relatedEntities.entrySet()){
			System.out.println("---------------------"+e.getKey());
			for(String title:e.getValue())
				System.out.println(title);
		}
	}
}
