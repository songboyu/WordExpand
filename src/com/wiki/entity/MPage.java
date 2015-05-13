package com.wiki.entity;

import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiTitleParsingException;

/**
 * Page类型扩展
 * @author Xiang
 *
 */
public class MPage {
	private Page page;
	/**
	 * 该条目被选中的概率
	 */
	private double prob;
	public MPage(){}
	public MPage(Page page){
		this.page = page;
	}
	public Page getPage() {
		return page;
	}
	public void setPage(Page page) {
		this.page = page;
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
		try {
			return this.page.getTitle().toString().hashCode();
		} catch (WikiTitleParsingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return super.hashCode();
	}
	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		if(obj instanceof MPage)
			try {
				return this.page.getTitle().toString().equals(((MPage)obj).getPage().getTitle().toString());
			} catch (WikiTitleParsingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return false;
	}
	
	
}
