package com.test;

import java.util.Map;

import com.baike.expand.BaikeExpand;
import com.entity.RelatedEntity;

public class Test_baike {
	public static void main(String[] args) throws Exception {
		String[] seedArray = {"哈工大","清华大学"};
		Map<String, RelatedEntity> entities = BaikeExpand.expandSeeds(seedArray);
		
		System.out.println(entities.size());
	}
}
