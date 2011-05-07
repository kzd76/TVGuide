package com.kzd76.TVGuide;

public class ChannelEvent {
	
	private String time;
	private String eventName;
	private String eventDesc;
	private String eventMore;
	private EventData eventData;
	
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getEventName() {
		return eventName;
	}
	public void setEventName(String eventName) {
		this.eventName = eventName;
	}
	public String getEventDesc() {
		return eventDesc;
	}
	public void setEventDesc(String eventDesc) {
		this.eventDesc = eventDesc;
	}
	public String getEventMore() {
		return eventMore;
	}
	public void setEventMore(String eventMore) {
		this.eventMore = eventMore;
	}
	public EventData getEventData() {
		return eventData;
	}
	public void setEventData(EventData eventData) {
		this.eventData = eventData;
	}
	
	public String toString(){
		String eventDataFlag = "";
		if (eventData != null) {
			eventDataFlag = " event has description";
		}
		String result = eventName + " (" + eventDesc + ") @ " + time + " / " + eventMore + " & " + eventDataFlag; 
		return result;
	}
	
}
