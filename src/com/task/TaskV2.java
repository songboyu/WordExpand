package com.task;

import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.baike.expand.BaikeExpand;
import com.entity.RelatedEntity;
import com.merge.MergeFetcher;
import com.task.util.TaskDBManager;
import com.wiki.expand.WikiExpand;

public class TaskV2 {
	private String[] seedArray = null;
	private String taskId = "";
	List<RelatedEntity> sealEntityList = new ArrayList<RelatedEntity>();
	List<RelatedEntity> wikiEntityList = new ArrayList<RelatedEntity>();
	List<RelatedEntity> baikeEntityList = new ArrayList<RelatedEntity>();
	private boolean sealFinished = false;
	private boolean wikiFinished = false;
	private boolean baikeFinished = false;
	
	public TaskV2(){
		
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
			sealEntityList = MergeFetcher.sealExpand(seedArray);
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
			Map<String, RelatedEntity> relatedEntities = WikiExpand.expandSeeds(seedArray);
			wikiEntityList = new ArrayList<RelatedEntity>(relatedEntities.values());
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
			Map<String, RelatedEntity> relatedEntities = BaikeExpand.expandSeeds(seedArray);
			baikeEntityList = new ArrayList<RelatedEntity>(relatedEntities.values());
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
				if(sealFinished && wikiFinished && baikeFinished){
					break;
				}
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			List<RelatedEntity> mergeList = MergeFetcher.mergeEntity(sealEntityList, wikiEntityList, baikeEntityList);
			for(RelatedEntity re: mergeList){
				if(writeResultToDb(new String[]{
						re.getEntityTitle(), 
						re.getCategoryTitle(),
						re.getRealScore()+""
					}))
					increaseRsCount();
			}
		}
		
		private boolean writeResultToDb(String[] result){
			boolean flag = false;
//			System.out.println(Arrays.toString(result));
			if(result != null){
				
				String sql = "insert into result(task_id,rs,tag,score) values(?,?,?,?)";
				List<Object>params = new ArrayList<Object>();
				params.add(taskId);
				params.add(result[0]);
				params.add(result[1]);
				params.add(result[2]);
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
