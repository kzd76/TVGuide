package com.kzd76.TVGuide;

import android.graphics.Bitmap;

public class Channel {
	public String name;
	public String id;
	public long refreshdate;
	public boolean offline;
	public Bitmap image;
	
	public String toString(){
		String temp = "";
		if (this.offline) {
			temp = "offline is checked";
		} else {
			temp = "offline is not checked";
		}
		if (this.image != null) {
			temp = temp + " and has image";
		} else {
			temp = temp + " and has no image";
		}
		return this.name + " - " + this.id + " @ " + this.refreshdate + " & " + temp;
	}
}
