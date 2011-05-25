package com.kzd76.TVGuide;

public class Constants {
	
	/*
	 * DATABAE CONSTANTS
	 */
	
	public static final String DATABASE_NAME = "tvguidedatabase";
	public static final int DATABASE_VERSION = 1;
	
	public static final String CHTABLE_TABLE_NAME = "channels";
	public static final String CHTABLE_CHANNEL_NAME = "channel_name";
	public static final String CHTABLE_CHANNEL_ID = "channel_id";
	public static final String CHTABLE_DATE_NAME = "refresh_date";
	public static final String CHTABLE_OFFLINE_MARKER = "offline";
	public static final String CHTABLE_KEY_ID = "_id";
	
	public static final String CHTABLE_OFFLINE_TRUE = "true";
	public static final String CHTABLE_OFFLINE_FALSE = "";
	
	public static final String EVTABLE_TABLE_NAME = "events";
	public static final String EVTABLE_KEY_ID = "_id";
	public static final String EVTABLE_CHANNEL_ID = "channel_id";
	public static final String EVTABLE_EVENT_DAY = "event_day";
	public static final String EVTABLE_EVENT_NAME = "event_name";
	public static final String EVTABLE_EVENT_START_TIME = "start_time";
	public static final String EVTABLE_EVENT_DESCRIPTION = "event_desc";
	public static final String EVTABLE_EVENT_DESC_URL = "event_url";
	public static final String EVTABLE_EVENT_INFO = "event_info";
	public static final String EVTABLE_EVENT_DETAILS = "event_details";
	public static final String EVTABLE_EVENT_IMAGE = "event_image";
	
	/*
	 * Port.hu related parameters 
	 */
	
	public static final String TVGUIDE_HOST = "http://port.hu";
	public static final String TVGUIDE_CHANNEL_URL = "/pls/w/mobil.tv_channel?i_days={0}&i_ch_id={1}&i_box=1&i_serial=1&i_st_hour=23&i_st_min=59&i_topic_id=1";
	
	/*
	 * MISC CONSTANTS
	 */
	
	public static final String LOG_MAIN_TAG = "TVGuide";
}
