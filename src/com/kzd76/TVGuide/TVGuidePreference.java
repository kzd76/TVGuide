package com.kzd76.TVGuide;

import java.io.Serializable;

import android.content.SharedPreferences;
import android.util.Log;

public class TVGuidePreference implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static String localLogTag = "_Preference";
	
	private boolean offlineMode;
	private int daysToDownload;
	private boolean downloadDescriptions;
	private boolean downloadImages;
	private boolean downloadOnlineImages;
	private boolean onlineEnabled;
	private boolean onlyWifi;
	private boolean eventsRestrictedWhenOffline;
	
	public boolean isDownloadOnlineImages() {
		return downloadOnlineImages;
	}

	public boolean isOfflineMode() {
		return offlineMode;
	}

	public int getDaysToDownload() {
		return daysToDownload;
	}

	public boolean isDownloadDescriptions() {
		return downloadDescriptions;
	}

	public boolean isDownloadImages() {
		return downloadImages;
	}

	public boolean isOnlineEnabled() {
		return onlineEnabled;
	}

	public boolean isOnlyWifi() {
		return onlyWifi;
	}

	public boolean isEventsRestrictedWhenOffline() {
		return eventsRestrictedWhenOffline;
	}

	public TVGuidePreference(SharedPreferences prefs){
		this.daysToDownload = 1;
		this.offlineMode=false;
		this.downloadDescriptions=true;
		this.downloadImages=false;
		this.onlineEnabled=false;
		this.onlyWifi=false;
		this.eventsRestrictedWhenOffline=true;
		
		try {
			if (prefs != null){
				this.daysToDownload = Integer.parseInt(prefs.getString("daysToDownload", "1"));
				this.offlineMode = prefs.getBoolean("offlineMode", false);
				this.downloadDescriptions = prefs.getBoolean("downloadDescriptions", true);
				this.downloadImages = prefs.getBoolean("downloadEventPictures", false);
				this.downloadOnlineImages = prefs.getBoolean("downloadOnlineImages", false);
				this.onlineEnabled = prefs.getBoolean("onlineTrafficEnabled", false);
				this.onlyWifi = prefs.getBoolean("onlyWifi", false);
				this.eventsRestrictedWhenOffline = prefs.getBoolean("eventsWhenOffline", true);
			} else {
				Log.d(Constants.LOG_MAIN_TAG + localLogTag, "No default preference was found");
			}
		} catch (Exception e){
			Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Invalid preference value for daysToDownload");
		}
	}
	
}
