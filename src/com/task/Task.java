package com.task;

import java.io.File;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.baike.BaikeFetch;
import com.seal.expand.Entity;
import com.seal.expand.EntityList;
import com.seal.expand.Seal;
import com.seal.util.Helper;
import com.task.util.TaskDBManager;
import com.wiki.expand.WikiExpand;

public class Task {
	private ConcurrentLinkedQueue<String[]> resultQueue = new ConcurrentLinkedQueue<String[]>();
	private String[] seedArray = null;
	private String taskId = "";
	private boolean sealFinished = false;
	private boolean wikiFinished = false;
	private boolean baikeFinished = false;
	private boolean crawingFinished = false;
	
	public Task(){
		
	}
	
	public boolean createTask(String taskId, String seeds){
		try{
			seedArray = seeds.split(" ");
		} catch(Exception e0){
			return false;
		}
		this.taskId = taskId;
		SealExpandThread set = new SealExpandThread();
		set.start();
		WikiExpandThread wet = new WikiExpandThread();
		wet.start();
		BaikeExpandThread bet = new BaikeExpandThread();
		bet.start();
		WriteResultThread wrt = new WriteResultThread();
		wrt.start();
		return true;
	}
	
	private class SealExpandThread extends Thread{

		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			sealExpand();
			sealFinished = true;
		}
		
		private void sealExpand(){
			File seedFile = new File(seedArray[0]);
			String[] seedArr;
			String hint = null;
			if (seedFile.exists()) {
				seedArr = Helper.readFile(seedFile).split("\n");
				if (seedArray.length >= 2) {
					File hintFile = Helper.toFileOrDie(seedArray[1]);
					hint = Helper.readFile(hintFile).replaceAll("[\r\n]+", " ");
				}
			} else {
				for (int i = 0; i < seedArray.length; i++)
					seedArray[i] = seedArray[i].replace('_', ' ');
				seedArr = seedArray;
			}
			EntityList seeds = new EntityList();
			for (String s : seedArr) 
				seeds.add(Entity.parseEntity(s));
			Seal seal = new Seal();
			seal.expand(seeds, seeds, hint);
//			seal.save();
			crawingFinished = true;

			try {
				Thread.sleep(1);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
//			seal.getEntityList();
			for(Entity e : seal.getEntities()){
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				resultQueue.add(new String[]{e.getName().toString(), ""});
			}
		}
		
	}
	
	private class WikiExpandThread extends Thread{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			wikiExpand();
			wikiFinished = true;
		}
		
		private void wikiExpand(){
			Map<String,Set<String>> relatedEntities = WikiExpand.expandSeeds(seedArray);
			while(!crawingFinished){
				try {
					Thread.sleep(1);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			for(Entry<String, Set<String>> e:relatedEntities.entrySet()){
//				System.out.println("---------------------"+e.getKey());
				
				for(String title:e.getValue())
					resultQueue.add(new String[]{title, e.getKey()});
					try {
						Thread.sleep(100);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
			}
		}
		
	}
	
	private class BaikeExpandThread extends Thread{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			baikeExpand();
			baikeFinished = true;
		}
		
		private void baikeExpand(){
			Map<String,Set<String>> entities = BaikeFetch.baikeExpand(seedArray);

			while(!crawingFinished){
				try {
					Thread.sleep(1);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			for(Entry<String,Set<String>> e:entities.entrySet()){
//				System.out.println("----------------------"+e.getKey());
				for(String word:e.getValue()){
					try {
						Thread.sleep(100);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					resultQueue.add(new String[]{word, e.getKey()});
				}
			}
		}
		
	}
	
	private class WriteResultThread extends Thread{
		TaskDBManager tdb = new TaskDBManager();
		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			tdb.getConnection();
			Random r = new Random(System.currentTimeMillis());
			try {
				Thread.sleep(Math.abs(r.nextLong() % 5000));
				changeState(1);
				Thread.sleep(Math.abs(r.nextLong() % 5000));
				changeState(2);
				Thread.sleep(Math.abs(r.nextLong() % 5000));
				changeState(3);
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch(IllegalArgumentException e0){
			}
			writeResult();
			changeState(4);
			tdb.closeAll();
		}
		
		private void changeState(int state){
			String sql = "update task set state=? where task_id=?";
			List<Object> params = new ArrayList<Object>();
			params.add(state);
			params.add(taskId);
			try {
				tdb.update(sql, params);
			} catch (SQLTimeoutException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		private void writeResult(){
			while(true){
				while(!resultQueue.isEmpty()){
					if(writeResultToDb(resultQueue.poll()))
						increaseRsCount();
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(sealFinished && wikiFinished && baikeFinished && resultQueue.isEmpty()){
					break;
				}
				else{

				}
			}
		}
		
		private boolean writeResultToDb(String[] result){
			boolean flag = false;
//			System.out.println(Arrays.toString(result));
			if(result != null){
				
				String sql = "insert into result(task_id,rs,tag) values(?,?,?)";
				List<Object>params = new ArrayList<Object>();
				params.add(taskId);
				params.add(result[0]);
				params.add(result[1]);
				try {
					flag = tdb.insert(sql, params);
				} catch (SQLTimeoutException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return flag;
		}
		
		private void increaseRsCount(){
			String sql = "update task set rs_count=rs_count+1 where task_id=?";
			List<Object> params = new ArrayList<Object>();
			params.add(taskId);
			try {
				tdb.update(sql, params);
			} catch (SQLTimeoutException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
}
