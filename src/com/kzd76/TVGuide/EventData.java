package com.kzd76.TVGuide;

import android.graphics.Bitmap;

public class EventData {
	
	private Bitmap image;
	private String title;
	private String info;
	private String desc;
	private String imageAlt;
	
	public Bitmap getImage() {
		return image;
	}
	public void setImage(Bitmap image) {
		this.image = image;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public String getImageAlt() {
		return imageAlt;
	}
	public void setImageAlt(String imageAlt) {
		this.imageAlt = imageAlt;
	}
	public String getInfo() {
		return info;
	}
	public void setInfo(String info) {
		this.info = info;
	}
	
}
