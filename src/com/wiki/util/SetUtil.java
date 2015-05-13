package com.wiki.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.wiki.entity.MCategory;

import de.tudarmstadt.ukp.wikipedia.api.Category;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiTitleParsingException;

public class SetUtil {
	/**
	 * 将set类型集合转换为map
	 * @param s 即将转换的set集合
	 * @param up 
	 * @param down
	 * @return 返回转换后的map
	 */
	public static Map<String,MCategory> setToMap(Set<Category> s,boolean up,boolean down){
		Map<String,MCategory> m = new HashMap<String,MCategory>();
		for(Category c:s)
			try {
				m.put(c.getTitle().toString(),new MCategory(c, up, down));
			} catch (WikiTitleParsingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return m;
	}
}
