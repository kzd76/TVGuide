package com.kzd76.TVGuide;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ChannelDataRefreshTread extends Thread{
	
	public final static String PROGRESS_BAR_STATE_MSG = "ProgressBarState";
	public final static String PROGRESS_BAR_CHANNEL_MSG = "ProgressBarChannelDone";
	public final static String PROGRESS_BAR_DONE_MSG = "ProgressBarDone";
	
	private static final String localLogTag = "_DBRefresh";
	
	Handler handler;
	final static int STATE_DONE = 0;
	final static int STATE_RUNNING = 1;
	private TVGuidePreference tvprefs;
	private String[] channelIds;
	
	public ChannelDataRefreshTread(Handler handler, String[] channelIds, String threadName, TVGuidePreference tvprefs) {
		this.handler = handler;
		this.channelIds = channelIds;
		this.setName(threadName);
		this.tvprefs = tvprefs;
	}
	
	public void run(){
		final int days = tvprefs.getDaysToDownload();
		final boolean downloadDescriptions = tvprefs.isDownloadDescriptions();
		final boolean downloadImages = tvprefs.isDownloadImages();
		int channels = channelIds.length;
		ChannelData cd = null;
		URL url = null;
		HttpURLConnection urlConnection = null;
		int dayId;
		StringBuilder text = null;
		int channelCounter = 0;
		
		Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Thread started, channels: " + channelIds.length + ", days to download: " + days + ", images: " + downloadImages);
		
		for (String channelId : channelIds) {
			for (int dayCounter = 0; dayCounter < days; dayCounter++){
				dayId = dayCounter + 1;
				
				try {
					url = new URL(WebDataProcessor.getUrl(channelId, dayId));
					urlConnection = (HttpURLConnection) url.openConnection();
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
			    	text = new StringBuilder();
				    String NL = System.getProperty("line.separator");
				    Scanner scanner = new Scanner(in, charset);
			    	while (scanner.hasNextLine()){
			    		text.append(scanner.nextLine() + NL);
			    	}
			    	urlConnection.disconnect();
			    	
			    	int amount = text.length();  
			    	
			    	cd = WebDataProcessor.processChannelDataFromText(channelId, dayId, WebDataProcessor.host, text.toString());
					Log.d(Constants.LOG_MAIN_TAG + localLogTag, "From WebDataProcessor: " + cd.getCaption() + " / " + cd.getEventCount());
					
					ArrayList<ChannelEvent> events;
					
					String chInfo = cd.getChannelName() + " " + cd.getEventDay();
					
					if ((cd != null) && (downloadDescriptions)) {
						
						events = cd.getEvents();
						
						int evtot = cd.getEventCount();
						int evcur = 0;
						
						for (ChannelEvent ev : events) {
							
							if (ev.getEventMore().length() > 0) {
								
								url = new URL(ev.getEventMore());
								urlConnection = (HttpURLConnection) url.openConnection();
						        contentType = urlConnection.getContentType();
						        charset = "UTF-8";
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
						        in = new BufferedInputStream(urlConnection.getInputStream(), 8192);
						    	text = new StringBuilder();
							    scanner = new Scanner(in, charset);
						    	while (scanner.hasNextLine()){
						    		text.append(scanner.nextLine() + NL);
						    	}
						    	urlConnection.disconnect();
						    	
						    	if (evcur == 0) {
						    		amount = amount + text.length();
						    	} else {
						    		amount = text.length();
						    	}
						    	
								EventData ed = WebDataProcessor.processEventDataFromText(ev.getEventName(), text.toString(), downloadImages);
								ev.setEventData(ed);
								
							}
							
							double e = (100 / channels / days) * evcur / evtot;
							double f = 100 * channelCounter / channels;
							double g = (100 / channels) * dayCounter / days;
							double dPos = f + g;
							double dSecPos = (e + f + g);
							
							//Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Pos: " + e + " + " + f + " + " + g);
							//Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Calculated pos: " + dPos + " / " + dSecPos);
							
							Message msg = handler.obtainMessage();
							Bundle data = new Bundle();
							data.putString("Target", PROGRESS_BAR_STATE_MSG);
							data.putInt("Pos", (int)dPos);
							data.putInt("SecPos", (int)dSecPos);
							data.putDouble("Amount", amount / 1024);
							data.putString("Status", chInfo + "\n" + evcur + "/" + evtot);
							msg.setData(data);
							handler.sendMessage(msg);
							
							evcur++;
							
						}
					} else {
						double f = 100 * channelCounter / channels;
						double g = (100 / channels) * dayCounter / days;
						double dPos = f + g;
						
						//Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Pos: " + f + " + " + g);
						
						Message msg = handler.obtainMessage();
						Bundle data = new Bundle();
						data.putString("Target", PROGRESS_BAR_STATE_MSG);
						data.putInt("Pos", (int)dPos);
						data.putInt("SecPos", 0);
						data.putDouble("Amount", amount / 1024);
						data.putString("Status", chInfo);
						msg.setData(data);
						handler.sendMessage(msg);
					}
					
					Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Day finished");
					
					Message msg = handler.obtainMessage();
					Bundle data = new Bundle();
					data.putString("Target", PROGRESS_BAR_CHANNEL_MSG);
					msg.setData(data);
					msg.obj = cd;
					handler.sendMessage(msg);
					
				} catch (Exception e) {
					Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Error while downloading HTTP data." + e.getMessage()); 
					Log.d(Constants.LOG_MAIN_TAG + localLogTag, "URL: " + url.toString());
				}
				
			}
			
			if (days == 0) {
				Message msg = handler.obtainMessage();
				Bundle data = new Bundle();
				data.putString("Target", PROGRESS_BAR_CHANNEL_MSG);
				msg.setData(data);
				msg.obj = cd;
				handler.sendMessage(msg);
			}
			
			Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Channel finished");
			
			channelCounter++;	//Finally increment channel counter
			
		}
		
		Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Thread finished");
		Message msg = handler.obtainMessage();
		Bundle data = new Bundle();
		data.putString("Target", PROGRESS_BAR_DONE_MSG);
		msg.setData(data);
		handler.sendMessage(msg);
	}
	
}
