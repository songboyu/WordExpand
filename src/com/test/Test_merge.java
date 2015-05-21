package com.test;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.entity.RelatedEntity;
import com.merge.MergeFetcher;

public class Test_merge {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String[] seedArray = {"哈工大","上海交通大学"};
		List<RelatedEntity> relatedEntityList = MergeFetcher.expandSeeds(seedArray);
		
		Collections.sort(relatedEntityList, new Comparator<RelatedEntity>(){

			@Override
			public int compare(RelatedEntity o1, RelatedEntity o2) {
				// TODO Auto-generated method stub
				double d = o1.getRealScore() - o2.getRealScore();
				if( d > 0)
					return -1;
				else if(d == 0)
					return 0;
				else 
					return 1;
			}
		});
		for(RelatedEntity re: relatedEntityList){
			System.out.println(re.getEntityTitle() +"\t"+ re.getCategoryTitle() + "\t" + re.getRealScore());
		}
		System.out.println(relatedEntityList.size());
	}

}
