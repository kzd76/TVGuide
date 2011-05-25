package com.kzd76.TVGuide;

import java.util.ArrayList;

public class ChannelData {
	
	private String channelName;
	private String channelId;
	private String eventDay;
	private String caption;
	private ArrayList<ChannelEvent> events;
	private int dataLength;
	
	public String getChannelName() {
		return channelName;
	}
	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}
	public String getEventDay() {
		return eventDay;
	}
	public void setEventDay(String eventDay) {
		this.eventDay = eventDay;
	}
	public String getChannelId() {
		return channelId;
	}
	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}
	public String getCaption() {
		return caption;
	}
	public void setCaption(String caption) {
		this.caption = caption;
	}
	public ArrayList<ChannelEvent> getEvents() {
		return events;
	}
	public void setEvents(ArrayList<ChannelEvent> events) {
		this.events = events;
	}
	public int getDataLength() {
		return dataLength;
	}
	public void setDataLength(int dataLength) {
		this.dataLength = dataLength;
	}
	
	public String buildCaption(){
		String result = "<b>" + this.channelName + "</b><br/>" + this.eventDay + "";
		return result;
	}
	
	public int getEventCount(){
		return this.events.size();
	}
	
}
