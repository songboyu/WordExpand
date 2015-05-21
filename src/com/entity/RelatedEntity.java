package com.entity;

/**
 * 相关实体类
 * @author Xiang
 *
 */
public class RelatedEntity {
	String entityTitle = "";
	String categoryTitle = "";
	double wikiScore = 0.0;
	double baikeScore = 0.0;
	double sealScore = 0.0;
	double realScore = 0.0;
	public RelatedEntity(){}
	public String getEntityTitle() {
		return entityTitle;
	}
	public void setEntityTitle(String entityTitle) {
		this.entityTitle = entityTitle;
	}
	
	public String getCategoryTitle() {
		return categoryTitle;
	}
	public void setCategoryTitle(String categoryTitle) {
		this.categoryTitle = categoryTitle;
	}
	public double getWikiScore() {
		return wikiScore;
	}
	public void setWikiScore(double wikiScore) {
		this.wikiScore = wikiScore;
	}
	public double getBaikeScore() {
		return baikeScore;
	}
	public void setBaikeScore(double baikeScore) {
		this.baikeScore = baikeScore;
	}
	public double getSealScore() {
		return sealScore;
	}
	public void setSealScore(double sealScore) {
		this.sealScore = sealScore;
	}
	public double getRealScore() {
		return realScore;
	}
	
	/**
	 * 计算相关实体最终分数
	 * @param x seal得分权重
	 * @param y wiki得分权重
	 * @param z 百度百科得分权重
	 */
	public void calcRealScore(double x, double y, double z){
		realScore = x*sealScore + y*wikiScore + z*baikeScore;
	}
	
	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return this.entityTitle.hashCode();
	}
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof RelatedEntity)
			return this.entityTitle.equals(((RelatedEntity)obj).getEntityTitle());
		return false;
	}
	
	
}
