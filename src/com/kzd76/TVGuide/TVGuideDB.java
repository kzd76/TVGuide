package com.kzd76.TVGuide;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class TVGuideDB {
	
	private SQLiteDatabase db;
	private final Context context;
	private final TVGuideDBhelper dbHelper;
	
	private static final String localLogTag = "_DBDriver";
	
	public TVGuideDB(Context c){
		this.context = c;
		this.dbHelper = new TVGuideDBhelper(context, Constants.DATABASE_NAME, null, Constants.DATABASE_VERSION);
	}
	
	public void close(){
		this.db.close();
	}
	
	public void open() throws SQLiteException{
		try {
			db = dbHelper.getWritableDatabase();
		} catch (SQLiteException e) {
			Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Unable to get writable database. " + e.getMessage());
			db = dbHelper.getReadableDatabase();
		}
	}
	
	public long insertChannel(String channelName, String channelId) {
		try {
			ContentValues newTaskValue = new ContentValues();
			newTaskValue.put(Constants.CHTABLE_CHANNEL_NAME, channelName);
			newTaskValue.put(Constants.CHTABLE_CHANNEL_ID, channelId);
			newTaskValue.put(Constants.CHTABLE_DATE_NAME, 0);
			return db.insert(Constants.CHTABLE_TABLE_NAME, null, newTaskValue);
		} catch (SQLiteException e) {
			Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Unable to insert into database. " + e.getMessage());
			return -1;
		}
	}
	
	public long insertChannel(Channel channel) {
		try {
			String temp = "";
			if (channel.offline) {
				temp = Constants.CHTABLE_OFFLINE_TRUE;
			} else {
				temp = Constants.CHTABLE_OFFLINE_FALSE;
			}
			ContentValues newTaskValue = new ContentValues();
			newTaskValue.put(Constants.CHTABLE_CHANNEL_NAME, channel.name);
			newTaskValue.put(Constants.CHTABLE_CHANNEL_ID, channel.id);
			newTaskValue.put(Constants.CHTABLE_DATE_NAME, 0);
			newTaskValue.put(Constants.CHTABLE_OFFLINE_MARKER, temp);
			return db.insert(Constants.CHTABLE_TABLE_NAME, null, newTaskValue);
		} catch (SQLiteException e) {
			Log.v(Constants.LOG_MAIN_TAG + localLogTag, "Unable to insert into database. " + e.getMessage());
			return -1;
		}
	}
	
	public long deleteAllChannels(){
		try {
			return db.delete(Constants.CHTABLE_TABLE_NAME, null, null);
		} catch (SQLiteException e) {
			Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Unable to insert into database. " + e.getMessage());
			return -1;
		}
	}
	
	public Cursor getChannels(){
		try {
			Cursor cursor = db.query(Constants.CHTABLE_TABLE_NAME, null, null, null, null, null, null);
			return cursor;
		} catch (SQLiteException e) {
			Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Unable to get channel list. " + e.getMessage());
			return null;
		}
	}
	
	public Cursor getTopChannels(int limit){
		try {
			String limitText = String.valueOf(limit);
			//Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Limit is: " + limitText);
			Cursor cursor = db.query(Constants.CHTABLE_TABLE_NAME, null, null, null, null, null, null, limitText);
			return cursor;
		}
		catch (SQLiteException e) {
			Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Unable to get TOP " + limit + " channels. " + e.getMessage());
			return null;
		}
	}
	
	public Cursor getTopOfflineChannels(int limit){
		try {
			String limitText = String.valueOf(limit);
			//Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Limit is: " + limitText);
			Cursor cursor = db.query(Constants.CHTABLE_TABLE_NAME, null, Constants.CHTABLE_OFFLINE_MARKER + "=\"" + Constants.CHTABLE_OFFLINE_TRUE + "\"", null, null, null, null, limitText);
			return cursor;
		}
		catch (SQLiteException e) {
			Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Unable to get TOP " + limit + " channels. " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}
	
	public Cursor getOfflineChannels(){
		try {
			Cursor cursor = db.query(Constants.CHTABLE_TABLE_NAME, null, Constants.CHTABLE_OFFLINE_MARKER + "=\"" + Constants.CHTABLE_OFFLINE_TRUE + "\"", null, null, null, null);
			return cursor;
		} catch (SQLiteException e) {
			Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Unable to get offline channels. " + e.getMessage());
			return null;
		}
	}
	
	public long deleteChannel(Channel channel){
		if (channel != null) {
			try {
				return db.delete(Constants.CHTABLE_TABLE_NAME, Constants.CHTABLE_CHANNEL_NAME + "=" + channel.name + " and " + Constants.CHTABLE_CHANNEL_ID + "=" + channel.id, null);
			} catch (SQLiteException e) {
				Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Unable to remove channel (" + channel.name + ") from database" + e.getMessage());
				return -1;
			}
		} else {
			Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Unable to remove a null object from database!");
			return -1;
		}
	}
	
	public long purgeEvents(String beforeDate){
		try {
			return db.delete(Constants.EVTABLE_TABLE_NAME, Constants.EVTABLE_EVENT_DAY + "<\"" + beforeDate + "\"", null);
		} catch (SQLiteException e) {
			Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Unable to purge events from database! " + e.getMessage());
			return -1;
		}
	}
	
	public long storeChannelData(ChannelData chdata) {
		try {
			if (chdata != null){
				String chId = chdata.getChannelId();
				String event_day = chdata.getEventDay();
				db.delete(Constants.EVTABLE_TABLE_NAME, Constants.EVTABLE_CHANNEL_ID + "=" + chId + " and " + Constants.EVTABLE_EVENT_DAY + "=\"" + event_day + "\"", null);
				Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Number of events to be stored in database: " + chdata.getEventCount());
				
				ArrayList<ChannelEvent> events = chdata.getEvents();
				
				for (ChannelEvent ev : events){
					//Log.d(Constants.LOG_MAIN_TAG + localLogTag, ev.toString());
					String event_name = ev.getEventName();
					String event_time = ev.getTime();
					String event_desc = ev.getEventDesc();
					String event_more = ev.getEventMore();
					String eventdata_info = "";
					String eventdata_desc = "";
					//String eventdata_imgalt = "";
					Bitmap eventdata_image = null;
					EventData ed = ev.getEventData();
					byte[] eventdata_imageBytes = null;
					
					if (ed != null) {
						eventdata_info = ed.getInfo();
						eventdata_desc = ed.getDesc();
						//eventdata_imgalt = ed.getImageAlt();
						eventdata_image = ed.getImage();
						if (eventdata_image != null) {
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							eventdata_image.compress(Bitmap.CompressFormat.PNG, 100, baos);    
							eventdata_imageBytes = baos.toByteArray();
						}
					}
					
					ContentValues newTaskValue = new ContentValues();
					newTaskValue.put(Constants.EVTABLE_CHANNEL_ID, chId);
					newTaskValue.put(Constants.EVTABLE_EVENT_DAY, event_day);
					newTaskValue.put(Constants.EVTABLE_EVENT_NAME, event_name);
					newTaskValue.put(Constants.EVTABLE_EVENT_START_TIME, event_time);
					newTaskValue.put(Constants.EVTABLE_EVENT_DESCRIPTION, event_desc);
					newTaskValue.put(Constants.EVTABLE_EVENT_DESC_URL, event_more);
					newTaskValue.put(Constants.EVTABLE_EVENT_INFO, eventdata_info);
					newTaskValue.put(Constants.EVTABLE_EVENT_DETAILS, eventdata_desc);
					newTaskValue.put(Constants.EVTABLE_EVENT_IMAGE, eventdata_imageBytes);
					
					db.insert(Constants.EVTABLE_TABLE_NAME, null, newTaskValue);
				}
				return 0;
			} else {
				Log.d(Constants.LOG_MAIN_TAG + localLogTag, "No channel data received to store");
				return -1;
			}
		} catch (SQLiteException e) {
			Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Unable to store channel data into database. " + e.getMessage());
			return -1;
		}
	}
	
	public ChannelData getChannelEvents(String channelId, String dayId){
		try {
			ChannelData cd = new ChannelData();
			Cursor cursor = db.query(Constants.EVTABLE_TABLE_NAME, null, Constants.EVTABLE_CHANNEL_ID + "=\"" + channelId + "\" and " + Constants.EVTABLE_EVENT_DAY + "=\"" + dayId + "\"", null, null, null, Constants.EVTABLE_EVENT_START_TIME);
			if (cursor.moveToFirst()) {
				Cursor nameCursor = db.query(Constants.CHTABLE_TABLE_NAME, null, Constants.CHTABLE_CHANNEL_ID + "=\"" + channelId + "\"", null, null, null, null);
				if (nameCursor.moveToFirst()){
					cd.setChannelName(nameCursor.getString(nameCursor.getColumnIndex(Constants.CHTABLE_CHANNEL_NAME)));
				} else {
					cd.setChannelName("Channel " + channelId);
				}
				nameCursor.close();
				cd.setChannelId(cursor.getString(cursor.getColumnIndex(Constants.EVTABLE_CHANNEL_ID)));
				cd.setEventDay(cursor.getString(cursor.getColumnIndex(Constants.EVTABLE_EVENT_DAY)));
				cd.setCaption(cd.buildCaption());
				ArrayList<ChannelEvent> events = new ArrayList<ChannelEvent>();
				do {
					ChannelEvent ce = new ChannelEvent();
					ce.setEventName(cursor.getString(cursor.getColumnIndex(Constants.EVTABLE_EVENT_NAME)));
					ce.setTime(cursor.getString(cursor.getColumnIndex(Constants.EVTABLE_EVENT_START_TIME)));
					ce.setEventDesc(cursor.getString(cursor.getColumnIndex(Constants.EVTABLE_EVENT_DESCRIPTION)));
					ce.setEventMore(cursor.getString(cursor.getColumnIndex(Constants.EVTABLE_EVENT_DESC_URL)));
					
					EventData ed = new EventData();
					ed.setInfo(cursor.getString(cursor.getColumnIndex(Constants.EVTABLE_EVENT_INFO)));
					ed.setDesc(cursor.getString(cursor.getColumnIndex(Constants.EVTABLE_EVENT_DETAILS)));
					byte[] bb = cursor.getBlob(cursor.getColumnIndex(Constants.EVTABLE_EVENT_IMAGE));
					if ((bb != null) && (bb.length > 0)) {
						Bitmap image = BitmapFactory.decodeByteArray(bb, 0, bb.length);
						ed.setImage(image);
					} else {
						ed.setImage(null);
					}
					
					if ((ed.getInfo() == null) && (ed.getDesc() == null) && (ed.getImage() == null)) {
						ed = null;
					} else {
						if (ed.getInfo() == null) ed.setInfo("");
						if (ed.getDesc() == null) ed.setDesc("");
						ed.setTitle(ce.getEventName());
					}
					ce.setEventData(ed);
					events.add(ce);
				} while (cursor.moveToNext());
				cd.setEvents(events);
			} else {
				cd = null;
			}
			cursor.close();
			return cd;
		} catch (SQLiteException e) {
			Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Unable to get channel events from database. " + e.getMessage());
			return null;
		}
	}
	
	public String[] getCurrentOfflineEvents(int topN, String eventDay, String currentTime){
		ArrayList<String> events = new ArrayList<String>();
		StringBuilder event;
		String[] result;
		try {
			Cursor chcursor;
			if (topN != 0) {
				Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Selecting top " + topN + " event on " + eventDay + " @ " + currentTime);
				chcursor = getTopOfflineChannels(topN);
			} else {
				Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Selecting all offline events on " + eventDay + " @ " + currentTime);
				chcursor = getOfflineChannels();
			}
			
			if (chcursor.moveToFirst()) {
				do {
					event = new StringBuilder();
					String chId = chcursor.getString(chcursor.getColumnIndex(Constants.CHTABLE_CHANNEL_ID));
					final String prevfilter = Constants.EVTABLE_CHANNEL_ID + "=\"" + chId + "\" and " + Constants.EVTABLE_EVENT_DAY + "=\"" + eventDay + "\" and " + Constants.EVTABLE_EVENT_START_TIME + "<\"" + currentTime + "\"";
					final String nextfilter = Constants.EVTABLE_CHANNEL_ID + "=\"" + chId + "\" and " + Constants.EVTABLE_EVENT_DAY + "=\"" + eventDay + "\" and " + Constants.EVTABLE_EVENT_START_TIME + ">\"" + currentTime + "\"";
					Cursor prevcursor = db.query(Constants.EVTABLE_TABLE_NAME, null, prevfilter, null, null, null, Constants.EVTABLE_EVENT_START_TIME + " desc", "1");
					Cursor nextcursor = db.query(Constants.EVTABLE_TABLE_NAME, null, nextfilter, null, null, null, Constants.EVTABLE_EVENT_START_TIME, "1");
					if (prevcursor.moveToFirst()){
						event.append(chcursor.getString(chcursor.getColumnIndex(Constants.CHTABLE_CHANNEL_NAME)));
						event.append("@@");
						event.append(prevcursor.getString(prevcursor.getColumnIndex(Constants.EVTABLE_EVENT_START_TIME)));
						event.append("@@");
						event.append(prevcursor.getString(prevcursor.getColumnIndex(Constants.EVTABLE_EVENT_NAME)));
						event.append("@@");
						if (nextcursor.moveToFirst()){
							event.append(nextcursor.getString(nextcursor.getColumnIndex(Constants.EVTABLE_EVENT_START_TIME)));
						} else {
							event.append("null");
						}
						events.add(event.toString());
					}
					prevcursor.close();
					nextcursor.close();
				} while (chcursor.moveToNext());
			}
			chcursor.close();
		} catch (SQLiteException e) {
			Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Unable to collect current events from favourite channels. " + e.getMessage());
		}
		if (topN != 0) {
			result = new String[topN];
		} else {
			result = new String[events.size()];
		}
		events.toArray(result);
		return result;
	}
}
