package com.test;

import java.io.File;

import org.apache.log4j.Logger;

import com.seal.expand.Entity;
import com.seal.expand.EntityList;
import com.seal.expand.Seal;
import com.seal.util.Helper;

public class Test {
	public static Logger log = Logger.getLogger(Seal.class);
	public static void main(String args[]) {
		String[] querySeeds = {"奔驰","奥迪"};

		File seedFile = new File(querySeeds[0]);
		String[] seedArr;
		String hint = null;
		if (seedFile.exists()) {
			seedArr = Helper.readFile(seedFile).split("\n");
			if (querySeeds.length >= 2) {
				File hintFile = Helper.toFileOrDie(querySeeds[1]);
				hint = Helper.readFile(hintFile).replaceAll("[\r\n]+", " ");
			}
		} else {
			for (int i = 0; i < querySeeds.length; i++)
				querySeeds[i] = querySeeds[i].replace('_', ' ');
			seedArr = querySeeds;
		}
		EntityList seeds = new EntityList();
		for (String s : seedArr) 
			seeds.add(Entity.parseEntity(s));
		Seal seal = new Seal();
		seal.expand(seeds, seeds, hint);
//		seal.save();

		log.info(seal.getEntityList().toDetails(100, seal.getFeature()));

//		Helper.printMemoryUsed();
//		Helper.printElapsedTime(startTime);
	}
}
