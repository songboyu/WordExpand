package com.test;

import java.util.Map;

import com.baike.expand.BaikeExpand;
import com.entity.RelatedEntity;

public class Test_baike {
	public static void main(String[] args) throws Exception {
		String[] seedArray = {"武松","鲁智深"};
		Map<String, RelatedEntity> entities = BaikeExpand.expandSeeds(seedArray);
		
		System.out.println(entities.size());
	}
}
