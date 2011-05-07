package com.kzd76.TVGuide;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

public class TVGuideDBhelper extends SQLiteOpenHelper{
	
	private static final String CREATE_CHANNEL_TABLE = "create table " +
	Constants.CHTABLE_TABLE_NAME + " (" + 
	Constants.CHTABLE_KEY_ID + " integer primary key autoincrement, " + 
	Constants.CHTABLE_CHANNEL_NAME + " text not null, " +
	Constants.CHTABLE_CHANNEL_ID + " text not null, " + 
	Constants.CHTABLE_DATE_NAME + " long, " +
	Constants.CHTABLE_OFFLINE_MARKER + " text not null );";
	
	private static final String CREATE_EVENT_TABLE = "create table " +
	Constants.EVTABLE_TABLE_NAME + " (" + 
	Constants.EVTABLE_KEY_ID + " integer primary key autoincrement, " + 
	Constants.EVTABLE_CHANNEL_ID + " text not null, " +
	Constants.EVTABLE_EVENT_DAY + " text not null, " +
	Constants.EVTABLE_EVENT_NAME + " text not null, " + 
	Constants.EVTABLE_EVENT_START_TIME + " text not null, " +
	Constants.EVTABLE_EVENT_DESCRIPTION + " text not null, " +
	Constants.EVTABLE_EVENT_DESC_URL + " text, " +
	Constants.EVTABLE_EVENT_INFO + " text, " + 
	Constants.EVTABLE_EVENT_DETAILS + " text, " +
	Constants.EVTABLE_EVENT_IMAGE + " blob );";

	public TVGuideDBhelper(Context context, String name, CursorFactory factory, int version){
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		try {
			db.execSQL(CREATE_CHANNEL_TABLE);
			db.execSQL(CREATE_EVENT_TABLE);
		} catch (SQLiteException e) {
			Log.v("DBHELPER","Unable to create database. " + e.getMessage());
		}
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.v("DBHELPER", "Database upgrade from " + oldVersion + " to " + newVersion);
		db.execSQL("drop table if exists " + Constants.CHTABLE_TABLE_NAME);
		db.execSQL("drop table if exists " + Constants.EVTABLE_TABLE_NAME);
		onCreate(db);
	}
	
}
