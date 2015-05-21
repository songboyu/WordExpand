package com.baike.entity;

import java.util.Set;


/**
 * 百度百科条目类
 * @author Xiang
 *
 */
public class BaikeEntry {
	private String entryName;
	private String entryId;
	private Set<String> inlinks;
	private int inlinkCount;
	/**
	 * 该条目被选中的概率
	 */
	private double prob;
	public BaikeEntry(){}
	public BaikeEntry(String entryId, String entryName){
		this.entryId = entryId;
		this.entryName = entryName;
	}
	
	
	public String getEntryName() {
		return entryName;
	}
	public void setEntryName(String entryName) {
		this.entryName = entryName;
	}
	public String getEntryId() {
		return entryId;
	}
	public void setEntryId(String entryId) {
		this.entryId = entryId;
	}
	public Set<String> getInlinks() {
		return inlinks;
	}
	public void setInlinks(Set<String> inlinks) {
		this.inlinks = inlinks;
	}
	
	public int getInlinkCount() {
		return inlinkCount;
	}
	public void setInlinkCount(int inlinkCount) {
		this.inlinkCount = inlinkCount;
	}
	public double getProb() {
		return prob;
	}
	public void setProb(double prob) {
		this.prob = prob;
	}
	
	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return this.entryId.hashCode();
	}
	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		if(obj instanceof BaikeEntry)
			return this.entryId.equals(((BaikeEntry)obj).getEntryId());
		return false;
	}
	
	
}
