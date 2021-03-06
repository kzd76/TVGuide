package com.kzd76.TVGuide;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class WebDataProcessor {
	
	private static final String localLogTag = "_WDB";
	
	public static final String host = Constants.TVGUIDE_HOST;
	public static final String urlTemplate = host + Constants.TVGUIDE_CHANNEL_URL;
	public static final String urlEarlyTemplate = host + Constants.TVGUIDE_CHANNEL_EARLY_URL;
	
	public final static String getUrl(String channelId, int dayId){
		
		String dayIdText = Integer.toString(dayId);
    	Object[] data = {dayIdText, channelId};
    	MessageFormat urlformatter = new MessageFormat(urlTemplate);
    	String urltext = urlformatter.format(data);
    	
    	return urltext; 
	}
	
	private static String getEarlyUrl(String channelId, int dayId) {
		
		String dayIdText = Integer.toString(dayId);
    	Object[] data = {dayIdText, channelId};
    	MessageFormat urlformatter = new MessageFormat(urlEarlyTemplate);
    	String urltext = urlformatter.format(data);
    	
    	return urltext; 
	}
	
	public final static String getHTTPData(String target){
		try {
			URL url = new URL(target);
	        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
	        String contentType = urlConnection.getContentType();
	        String charset = "UTF-8";
	        if ((contentType.length() > 0) && (contentType.toUpperCase().indexOf("CHARSET")) > 0) {
	        	charset = contentType.substring(contentType.toUpperCase().indexOf("CHARSET") + 7);
	        	charset = charset.substring(charset.indexOf("=") + 1);
	        	int endpos = charset.indexOf(";");
	        	if (endpos < 0) {
	        		endpos = charset.length();
	        	}
	        	charset = charset.substring(0, endpos);
	        	charset = charset.trim();
	        }
	        InputStream in = new BufferedInputStream(urlConnection.getInputStream(), 8192);
	    	StringBuilder text = new StringBuilder();
		    String NL = System.getProperty("line.separator");
		    Scanner scanner = new Scanner(in, charset);
	    	while (scanner.hasNextLine()){
	    		text.append(scanner.nextLine() + NL);
	    	}
	    	urlConnection.disconnect();
	    	
	    	return text.toString();
			
		} catch (Exception e) {
			Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Error while downloading HTTP data. " + e.getMessage()); 
			Log.d(Constants.LOG_MAIN_TAG + localLogTag, "URL: " + target);
			return null;
		}
	}
	
	public final static ChannelData processChannelData(String channelId, int dayId) {
		
		try {
			
			ChannelData cd = new ChannelData();
			String text;
			
			if (Constants.TVGUIDE_USE_EARLY_URL) {
				if (dayId > 0) {
					text = getHTTPData(getEarlyUrl(channelId, dayId - 1));
					cd = processChannelDataFromText(null, channelId, dayId - 1, host, text);
				}
			} else {
				cd = null;
			}
			
			text = getHTTPData(getUrl(channelId, dayId));
	    	return processChannelDataFromText(cd, channelId, dayId, host, text);
			
		} catch (Exception e) {
			Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Error while processing channel data: " + e.getMessage());
			return null;
		}
	}
	
	public final static ChannelData processChannelDataFromText(ChannelData channelData, String channelId, int dayId, String host, String receivedText) {
		final String text = receivedText;
		
		String chname = text.substring(text.indexOf("span class=\"txt\"")+17);
    	String prevlist = chname;
    	int prevData = 0;
    	
    	ChannelData cd;
    	ArrayList<ChannelEvent> events;
    	
    	if (channelData != null){
    		cd = channelData;
    		events = cd.getEvents();
    		prevData = cd.getDataLength();
    	} else {
    		cd = new ChannelData();
    		events = new ArrayList<ChannelEvent>();
    	}
    	
    	chname = chname.substring(0, chname.indexOf("</span>"));
    	
    	//String chdate = chname.substring(chname.indexOf(":<br") + 6);
    	chname = chname.substring(0, chname.indexOf(":<br"));
    	
    	prevlist = prevlist.substring(0, prevlist.indexOf("<div align=\"center\">"));
    	
    	String[] content = prevlist.split("<span class=\"btxt\" ");
    	
    	for (String event : content) {
    		if (event.indexOf("<span class=\"rtxt\"") < 0){
    			String starttime = event.substring(event.indexOf(">") + 1, event.indexOf("</span>"));
    			String eventmore = "";
    			String eventname = "";
    			String eventdesc = "";
    			if (event.indexOf("a  href") > -1){
    				eventmore = event.substring(event.indexOf("<a  href=\"") + 10, event.indexOf("</a>"));
		    		eventname = eventmore.substring(eventmore.indexOf(">") + 1);
		    		eventmore = eventmore.substring(0, eventmore.indexOf("\""));
		    		if (event.indexOf("<span class=\"txt\">") > -1) {
		    			eventdesc = event.substring(event.indexOf("<span class=\"txt\">") + 19);
		    			eventdesc = eventdesc.substring(0, eventdesc.indexOf("</span>") - 1);
		    		}
    			} else {
    				eventname = event.substring(event.indexOf("<span class=\"btxt\">") + 19);
    				eventname = eventname.substring(0, eventname.indexOf("</span>"));
    				if (event.indexOf("<span class=\"txt\">") > -1) {
    					eventdesc = event.substring(event.indexOf("<span class=\"txt\">") + 19);
    					eventdesc = eventdesc.substring(0, eventdesc.indexOf("</span>") - 1);
    				}
    			}
	    		
    			if ((eventmore.indexOf("http://") < 0) && (eventmore.length() > 0)) {
    				eventmore = host + eventmore;
    			}
    			
    			//Log.d(Constants.LOG_MAIN_TAG + localLogTag,"---\n" + event + "---\n");
    			
    			ChannelEvent e = new ChannelEvent();
    			//eventname = Html.fromHtml("<b><big>" + eventname + "</big></b>").toString();
    			//eventdesc = Html.fromHtml("<small>" + eventdesc + "</small>").toString();
    			e.setEventName(eventname);
    			e.setEventDesc(eventdesc);
    			e.setEventMore(eventmore);
    			e.setTime(starttime); 
    			events.add(e);
    			
    			//Log.d(Constants.LOG_MAIN_TAG + localLogTag, starttime + " - " + eventname + " / " + eventdesc + "   (" + eventmore + ")");
    			
    		}
    	}
    	
    	Calendar cal = Calendar.getInstance();
    	int diffDays = dayId - 1;
    	cal.add(Calendar.DATE, diffDays);
    	DateFormat df = new SimpleDateFormat("yyyy.MM.dd");
    	Date now = cal.getTime();
    	String eventDay = df.format(now);
    	
    	
    	cd.setChannelName(chname);
    	cd.setChannelId(channelId);
    	cd.setEventDay(eventDay);
    	cd.setCaption(cd.buildCaption());
    	cd.setEvents(events);
    	cd.setDataLength(prevData + text.length());
    	
    	return cd;
	}
	
	public final static EventData processEventData(String header, String target, boolean downloadImages){
		//System.out.println("Target URL is: " + target);
		
		EventData ed = new EventData();
		try {
			String text = getHTTPData(target);
			
			ed = processEventDataFromText(header, text, downloadImages);
			
			return ed;
		} catch (Exception e){
			Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Error while processing event description: " + e.getMessage());
			return null;
		}
	}
	
	public final static EventData processEventDataFromText(String header, String receivedText, boolean downloadImages) {
		
		EventData ed = new EventData();
		final String text = receivedText;
		
		try {
			String subtext = text.substring(text.indexOf("\"stitle2\"") + 10);
			String info = subtext.substring(subtext.indexOf("<span class=\"btxt\">") + 19);
			subtext = subtext.substring(0, subtext.indexOf("<script>"));
			info = info.substring(0, info.indexOf("<"));
			
			String eventdesc = "";
			
			String imageurl = "";
			String imgalt = "";
			
			if (subtext.indexOf("object_picture") >= 0) {
				imageurl = subtext.substring(subtext.indexOf("object_picture") + 14);
				imageurl = imageurl.substring(imageurl.indexOf("src=\"") + 5);
				imageurl = imageurl.substring(0, imageurl.indexOf("\""));
				imgalt = subtext.substring(subtext.indexOf("alt=\"") + 5);
				imgalt = imgalt.substring(0, imgalt.indexOf("\""));
				//Log.d(Constants.LOG_MAIN_TAG + localLogTag,imgalt);
			}
			
			eventdesc = subtext.substring(subtext.indexOf("+++++++"));
			eventdesc = eventdesc.substring(eventdesc.indexOf("<span class=\"txt\">") + 18);
			eventdesc = eventdesc.substring(0, eventdesc.indexOf("</span>") - 1);
			if (eventdesc.indexOf("<b>") >= 0){
				if (imgalt.length() > 0) {
					eventdesc = imgalt;
				} else {
					eventdesc = "";
				}
			}
			
			ed.setTitle(header);
			ed.setImageAlt(imgalt);
			ed.setDesc(eventdesc);
			
			int bmpSize = 0;
			
			if ((imageurl.length() > 0) && (imageurl.indexOf("http://") >= 0) && (downloadImages)) {
				URL imgurl = new URL(imageurl);
				HttpURLConnection imgconn = (HttpURLConnection) imgurl.openConnection();
				imgconn.setDoInput(true);
				imgconn.connect();
				bmpSize = imgconn.getContentLength();
				Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Image length: " + bmpSize);
				InputStream inst = imgconn.getInputStream();
				/*
				ByteArrayOutputStream bytes = new ByteArrayOutputStream();
				byte[] buffer = new byte[128];
				int read;
				while ((read = inst.read(buffer)) > 0) {
					bmpSize = bmpSize + read;
					bytes.write(buffer, 0, read);
				}
				
				bmpSize = inst.available();
				*/
				Bitmap bmp = BitmapFactory.decodeStream(inst);
				ed.setImage(bmp);
			} else {
				ed.setImage(null);
			}
			
			ed.setDataLength(text.length() + bmpSize);
			
			return ed;
		} catch (Exception e) {
			Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Error while processing event from received text. " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}
	
	public final static ArrayList<ChannelGroup> processChannelGroupsFromWeb(){
		
		ArrayList<ChannelGroup> groups = new ArrayList<ChannelGroup>();
		
		final String text = getHTTPData(host + Constants.TVGUIDE_CHANNEL_GROUP_SOURCE_URL);
		
		if (text.length() > 0) {
			String[] rawGroups = text.split("a class=\"txt\"");
			
			for (String rawItem : rawGroups){
				if (rawItem.contains("i_grp_id=")){
					ChannelGroup group = new ChannelGroup();
					String temp = rawItem.substring(rawItem.indexOf("i_grp_id=") + 9);
					temp = temp.substring(0, temp.indexOf("&"));
					group.groupId = temp;
					temp = rawItem.substring(rawItem.indexOf("\">") + 2);
					temp = temp.substring(0, temp.indexOf("<"));
					group.groupName = temp;
					
					groups.add(group);
				}
			}
		}
		Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Groups from web: " + groups.size());
		return groups;
	}
	
	public final static ArrayList<Channel> processChannelsFromWeb(ArrayList<ChannelGroup> channelGroups){
		ArrayList<Channel> channels = new ArrayList<Channel>();
		ArrayList<ChannelGroup> groups;
		
		if ((channelGroups == null) && (Constants.TVGUIDE_CHANNELGROUPS)){
			groups = processChannelGroupsFromWeb();
		} else {
			groups = channelGroups;
		}
		
		Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Channel groups to process: " + groups.toString());
		
		for (ChannelGroup group : groups) {
			if (group.groupId != null) {
				
				Object[] data = {group.groupId};
		    	MessageFormat urlformatter = new MessageFormat(host + Constants.TVGUIDE_CHANNEL_SOURCE_URL);
		    	String urltext = urlformatter.format(data);
		    	
		    	Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Downloading channel data from: " + urltext);
				
				String text = getHTTPData(urltext);
				
				if (text.length() > 0) {
					String[] rawChannels = text.split("a class=\"txt\"");
					
					for (String rawChannel : rawChannels){
						if (rawChannel.contains("i_ch_id")){
							Channel channel = new Channel();
							String temp = rawChannel.substring(rawChannel.indexOf("i_ch_id=") + 8);
							temp = temp.substring(0, temp.indexOf("&"));
							channel.id = temp;
							temp = rawChannel.substring(rawChannel.indexOf("\">") + 2);
							temp = temp.substring(0, temp.indexOf("<"));
							channel.name = temp;
							
							channels.add(channel);
						}
					}
				} else {
					Log.d(Constants.LOG_MAIN_TAG + localLogTag, "No data is received from " + urltext);
				}
			}
		}

		Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Channels from web: " + channels.size());
		
		return channels;
	}
	
}
