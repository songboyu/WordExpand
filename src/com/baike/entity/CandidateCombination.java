package com.baike.entity;

import java.util.HashSet;
import java.util.Set;

/**
 * 候选组合实体类
 * @author Xiang
 *
 */
public class CandidateCombination {
	/**
	 * 不同种子词相应的候选集合
	 */
	private Set<BaikeEntry> candiDateSet;
	/**
	 * 该候选集合的概率得分
	 */
	private double probScore;
	/**
	 * 该候选结合的相关度得分
	 */
	private double relScore;
	/**
	 * 该候选集合的最终得分
	 */
	private double realScore;
	
	public CandidateCombination(){
		candiDateSet = new HashSet<BaikeEntry>();
	}
	
	/**
	 * 向候选集合添加候选
	 * @param page 要添加的候选条目
	 */
	public void addCandidate(BaikeEntry page){
		this.candiDateSet.add(page);
	}
	
	/**
	 * 计算最终得分
	 * @param prop 两部分的比重
	 */
	public void calcScore(double prop){
		//根据公式argmax(ai,bj)=λP(ai,bj)+(1-λ)R(ai，bj)计算最终得分
		realScore = prop*probScore+(1-prop)*relScore;
		
	}
	
	public Set<BaikeEntry> getCandiDateSet() {
		return candiDateSet;
	}
	public void setCandiDateSet(Set<BaikeEntry> candiDateSet) {
		this.candiDateSet = candiDateSet;
	}
	public double getProbScore() {
		return probScore;
	}
	public void setProbScore(double probScore) {
		this.probScore = probScore;
	}
	public double getRelScore() {
		return relScore;
	}
	public void setRelScore(double relScore) {
		this.relScore = relScore;
	}
	public double getRealScore() {
		return realScore;
	}
	public void setRealScore(double realScore) {
		this.realScore = realScore;
	}
}
