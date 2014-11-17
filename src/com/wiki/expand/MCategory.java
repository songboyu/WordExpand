package com.wiki.expand;

import de.tudarmstadt.ukp.wikipedia.api.Category;


public class MCategory{

	private String title;
	private Category category;
	private boolean up;
	private boolean down;
	public MCategory(){}
	public MCategory(Category category,boolean up,boolean down){
		this.category = category;
		this.title = WikiFetcher.getTitleStrForCat(category);
		this.up = up;
		this.down = down;
	}
	public Category getCategory() {
		return category;
	}
	public void setCategory(Category category) {
		this.category = category;
	}
	public boolean isUp() {
		return up;
	}
	public void setUp(boolean up) {
		this.up = up;
	}
	public boolean isDown() {
		return down;
	}
	public void setDown(boolean down) {
		this.down = down;
	}
	public String getTitle() {
		return title;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MCategory other = (MCategory) obj;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		return true;
	}
	
}
