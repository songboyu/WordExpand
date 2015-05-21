package com.merge;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.baike.expand.BaikeExpand;
import com.entity.RelatedEntity;
import com.merge.kohonen.KohonenBuilder;
import com.merge.kohonen.KohonenElements;
import com.merge.kohonen.Matrix;
import com.seal.expand.Entity;
import com.seal.expand.EntityList;
import com.seal.expand.Seal;
import com.seal.util.Helper;
import com.wiki.expand.WikiExpand;

/**
 * 融合算法实现
 * @author Xiang
 *
 */
public class MergeFetcher {
	
	/**
	 * 利用kohonen神经网络算法对数据进行聚类
	 * 
	 * @param Koh 原始数据矩阵
	 * @param entriesNum 神经元维度（应大于等于原始数据维度）,维度越大，计算越复杂，同时得到聚类结果越真实
	 * @param neuronsNum 神经元个数，即类别个数（应大于1）
	 * @return 返回聚类后得分高的一类的下标列表
	 */
	private static List<Integer> cluteringData(Matrix Koh, int entriesNum, int neuronsNum){
		if(neuronsNum < 2 || entriesNum < Koh.getHeigth()){
			return null;
		}
		//对数据进行归一化
		float sumRoot = Koh.normalizeKohonen() ;
        
		//创建kohonen网络，并获取其神经元
		//对原始数据进行学习
        KohonenBuilder kohNet = new KohonenBuilder() ;
        kohNet.makeNetwork(entriesNum, neuronsNum);
        Matrix neurons = kohNet.neurons;
        kohNet.start_learning(Koh);
        
        //创建聚类矩阵
        Matrix clusters = Matrix.create(Koh.getWidth(), 1, -1);
        //遍历聚类矩阵的每个位置（即每一行数据row），
        //遍历每个神经元，计算row与神经元的欧氏距离
        //记录与row距离最短的神经元下标并设置聚类矩阵当前位置的值
        for(int i=0;i<clusters.getWidth();i++){
            float minDist = Float.MAX_VALUE ;
            for(int a=0;a<neurons.getHeigth();a++){
            	//计算行数据与神经元的欧氏距离
            	double dist = KohonenElements.distance(neurons.getRow(a),Koh.getColumn(i));
                if(dist<minDist){     
                	//设置聚类矩阵当前位置的值
                    clusters.setValue(i, 0, a) ;
                    //重置最短距离
                    minDist = (float) dist ;
                }
            }
        }
        //创建分类矩阵，用于记录分类后的数据
        Matrix X = Matrix.create(clusters.getValueCount(0), Koh.getHeigth(), 0);
        Matrix Y = Matrix.create(clusters.getValueCount(1),Koh.getHeigth(), 0);
      //创建下标列表，用于记录分类后的数据在原始数据矩阵中的下标
        List<Integer> indexX = new ArrayList<Integer>();
        List<Integer> indexY = new ArrayList<Integer>();
        int x = 0;
        int y = 0;
        //解除原始数据的归一化
        Koh.denormalizeKohonen(sumRoot);
        //遍历聚类矩阵，将聚类后的数据从原始数据中取出来
        for(int i = 0;i < clusters.getWidth(); i++){
        	//如果聚类结果为0，则将该行数据放入X矩阵中,
        	//否则，将改行数据放入Y矩阵中
        	if(clusters.getValue(i, 0) == 0){
        		for(int j = 0;j < X.getHeigth();j++)
        		{
        			X.setValue(x, j, Koh.getValue(i, j));
        		}
        		x++;
        		indexX.add(i);
        		
        	} else if(clusters.getValue(i, 0) == 1){
        		for(int j = 0;j < Y.getHeigth();j++)
        		{
        			Y.setValue(y, j, Koh.getValue(i, j));
        		}
        		y++;
        		indexY.add(i);
        	}
        }
        //比较X矩阵和Y矩阵数据的均值，
        //返回均值较大的矩阵对应的数据在原始矩阵中的下标列表
        return X.avg() > Y.avg()?indexX:indexY;
	}

	/**
	 * 对相关实体进行融合
	 * @param relatedEntityList 相关实体列表
	 * @return 返回
	 */
	private static List<RelatedEntity> merge(List<RelatedEntity> relatedEntityList){
		//根据相关实体信息创建原始数据数组
		//分别将seal得分、wiki得分、百度百科得分作为原始数据的一个维度
		//在利用原始数据数组创建原始数据矩阵
		double[][] data = new double[relatedEntityList.size()][3];
		int i =0;
		for(RelatedEntity re:relatedEntityList){
			data[i][1] = re.getSealScore();
			data[i][2] = re.getWikiScore();
			data[i][0] = re.getBaikeScore();
			i++;
		}
		Matrix Koh = new Matrix(data);
		//调用聚类方法，获取聚类后相关度较高的一类的下标列表
		//在根据这个下标列表获取相对应的相关度较高的相关实体
		//并计算这些实体的最终得分
		List<Integer> indexList = cluteringData(Koh, 9, 2);
		List<RelatedEntity> rList = new ArrayList<RelatedEntity>();
		for(int index: indexList){
			RelatedEntity re = relatedEntityList.get(index);
			re.calcRealScore(3, 4, 3);
			rList.add(re);
		}
		relatedEntityList = null;
		return rList;
	}
	
	/**
	 * 对三个单一识别的结果列表进行融合处理
	 * @param sealEntityList 网页模板匹配识别结果列表
	 * @param wikiEntityList wiki识别结果列表
	 * @param baikeEntityList 百度百科识别结果列表
	 * @return 返回融合后的结果列表
	 */
	public static List<RelatedEntity> mergeEntity(
			List<RelatedEntity> sealEntityList,
			List<RelatedEntity> wikiEntityList,
			List<RelatedEntity> baikeEntityList
			){
		List<RelatedEntity> mergeEntityList = new ArrayList<RelatedEntity>();
		//向融合列表添加网页模板匹配结果
		for(RelatedEntity re: sealEntityList){
			mergeEntityList.add(re);
		}
		//向融合列表添加wiki识别结果
		//如果融合列表中已经存在该实体，则累加实体的wiki得分
		//如果不存在，则直接添加该结果
		for(RelatedEntity re: wikiEntityList){
			if(mergeEntityList.contains(re)){
				int index = mergeEntityList.indexOf(re);
				RelatedEntity oriRe = mergeEntityList.get(index);
				oriRe.setWikiScore(re.getWikiScore()+oriRe.getWikiScore());
				String catsStr = re.getCategoryTitle();
				String[] cats = catsStr.split("\\[");
				String oriCat = oriRe.getCategoryTitle();
				for(String cat: cats){
					if(cat.length() < 1)
						continue;
					cat = cat.substring(0, cat.length()-1);
					if(!oriCat.contains(cat)){
						oriRe.setCategoryTitle(oriCat +"[" +cat+"]");
					}
				}
				
			} else{
				mergeEntityList.add(re);
			}
		}
		//向融合列表添加百度百科识别结果
		//如果融合列表中已经存在该实体，则累加实体的百科得分
		//如果不存在，则直接添加该结果
		for(RelatedEntity re: baikeEntityList){
			if(mergeEntityList.contains(re)){
				int index = mergeEntityList.indexOf(re);
				RelatedEntity oriRe = mergeEntityList.get(index);
				oriRe.setBaikeScore(re.getBaikeScore()+oriRe.getBaikeScore());
				
				oriRe.setWikiScore(re.getWikiScore()+oriRe.getWikiScore());
				String catsStr = re.getCategoryTitle();
				String[] cats = catsStr.split("\\[");
				String oriCat = oriRe.getCategoryTitle();
				for(String cat: cats){
					if(cat.length() < 1)
						continue;
					cat = cat.substring(0, cat.length()-1);
					
					if(!oriCat.contains(cat)){
						oriRe.setCategoryTitle(oriCat +"[" +cat+"]");
					}
				}
			} else{
				mergeEntityList.add(re);
			}
		}
		//调用融合算法对融合列表进行聚类并返回结果
		return merge(mergeEntityList);
	}
	
	public static List<RelatedEntity> expandSeeds(String[] seedArray){

		List<RelatedEntity> sealEntityList = sealExpand(seedArray);
		List<RelatedEntity> wikiEntityList = wikiExpand(seedArray);
		List<RelatedEntity> baikeEntityList = baikeExpand(seedArray);
		return mergeEntity(sealEntityList, wikiEntityList, baikeEntityList);
		
	}
	public static List<RelatedEntity> sealExpand(String[] seedArray){
		List<RelatedEntity> sealEntityList = new ArrayList<RelatedEntity>();
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

		try {
			Thread.sleep(1);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
//		double totalScore = 0.0;
		for(Entity e : seal.getEntities()){
			RelatedEntity re = new RelatedEntity();
			re.setEntityTitle(e.getName().toString());
			double score = e.getScore();
			re.setSealScore(score);
			re.setBaikeScore(score*2);
			re.setWikiScore(score/2);
//			totalScore += e.getScore()*2;
			sealEntityList.add(re);
		}
//		double avgScore = totalScore / sealEntityList.size();
//		if(avgScore != 0){
//			//重置百科得分
//			for(RelatedEntity re: sealEntityList){
//				re.setSealScore(re.getSealScore() / avgScore);
//
//			}
//		}
		return sealEntityList;
	}
	
	private static List<RelatedEntity> baikeExpand(String[] seedArray){
		Map<String, RelatedEntity> relatedEntities = BaikeExpand.expandSeeds(seedArray);
		return new ArrayList<RelatedEntity>(relatedEntities.values());
	}
	
	private static List<RelatedEntity> wikiExpand(String[] seedArray){
		Map<String, RelatedEntity> relatedEntities = WikiExpand.expandSeeds(seedArray);
		return new ArrayList<RelatedEntity>(relatedEntities.values());
	}
}
