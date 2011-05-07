package com.kzd76.TVGuide;

public class Channel {
	public String name;
	public String id;
	public long refreshdate;
	public boolean offline;
	
	public String toString(){
		String temp = "";
		if (this.offline) {
			temp = "offline is checked";
		} else {
			temp = "offline is not checked";
		}
		return this.name + " - " + this.id + " @ " + this.refreshdate + " & " + temp;
	}
}
