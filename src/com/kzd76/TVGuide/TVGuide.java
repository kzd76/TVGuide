package com.kzd76.TVGuide;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;


public class TVGuide extends Activity {
	
	private TVGuideDB dba;
	
	private static final int GROUP_DEFAULT = 0;
	private static final int MENU_PREFERENCES = 1;
	private static final int MENU_CHANNELS = 2;
	private static final int MENU_ABOUT = 3;
	private static final int MENU_DOWNLOAD = 4;
	
	private static final int PROGRESS_DIALOG = 0;
	
	private ArrayList<Channel> favChList;
	
	private ProgressDialog progress = null;
	private ChannelDataRefreshTread chRefreshThread;
	private TVGuidePreference tvprefs;
	
	private static final String localLogTag = "_Main";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.main);
		
        favChList = new ArrayList<Channel>();
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		tvprefs = new TVGuidePreference(prefs);
		
        
        if (checkDB() > 0) {
        	Button btn1 = (Button) findViewById(R.id.button1);
    		Button btn2 = (Button) findViewById(R.id.button2);
    		
    		btn1.setOnClickListener(new View.OnClickListener() {
    			
    			@Override
    			public void onClick(View v) {
    				// TODO Auto-generated method stub
    				Intent i = new Intent(TVGuide.this, EventListScreen.class);
    				i.putExtra("SpinnerVisibility", true);
    				startActivity(i);
    			}
    		});
    		if (favChList.size() > 0) {
    			btn2.setText(favChList.get(0).name);    		
        		btn2.setOnClickListener(new View.OnClickListener() {
        			
        			@Override
        			public void onClick(View v) {
        				// TODO Auto-generated method stub
        				Intent i = new Intent(TVGuide.this, EventListScreen.class);
        				i.putExtra("SpinnerVisibility", false);
        				i.putExtra("ChannelName", favChList.get(0).name);
        				i.putExtra("ChannelID", favChList.get(0).id);
        				i.putExtra("downloadOnlineImages", tvprefs.isDownloadOnlineImages());
        				startActivity(i);
        			}
        		});
    		} else {
    			btn2.setVisibility(View.INVISIBLE);
    		}
    		
        } else {
        	// No channels were found in database starting with channel manager window
        	// also, buttons on main screen should go invisible
        	/*
        	Intent i = new Intent(TVGuide.this, EventListScreen.class);
			i.putExtra("SpinnerVisibility", true);
			startActivity(i);
			*/
        	Button btn1 = (Button) findViewById(R.id.button1);
    		Button btn2 = (Button) findViewById(R.id.button2);
    		
        	btn1.setVisibility(View.INVISIBLE);
        	btn2.setVisibility(View.INVISIBLE);
        }
	}
	
	private int checkDB() {
		int result = 0;
		
		dba = new TVGuideDB(this);
		dba.open();
		Cursor cursor = dba.getTopChannels(9);
		
		if (cursor.moveToFirst()) {
			if (favChList != null) {
				favChList.clear();
			} else {
				favChList = new ArrayList<Channel>();
			}
			do {
        		Channel ch = new Channel();
        		ch.name = cursor.getString(cursor.getColumnIndex(Constants.CHTABLE_CHANNEL_NAME));
        		ch.id = cursor.getString(cursor.getColumnIndex(Constants.CHTABLE_CHANNEL_ID));
        		ch.refreshdate = cursor.getLong(cursor.getColumnIndex(Constants.CHTABLE_DATE_NAME));
        		favChList.add(ch);
        		Log.d(Constants.LOG_MAIN_TAG + localLogTag, ch.toString());
        	} while (cursor.moveToNext());
			result = favChList.size();
		}
		cursor.close();
		dba.close();
		
		return result;
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	
		menu.add(GROUP_DEFAULT, MENU_CHANNELS, 1, "Csatornák");
		menu.add(GROUP_DEFAULT, MENU_DOWNLOAD, 2, "Mûsorújság frissítés");
    	menu.add(GROUP_DEFAULT, MENU_PREFERENCES, 3, "Beállítások");
    	menu.add(GROUP_DEFAULT, MENU_ABOUT, 4, "Névjegy");
    	
    	return super.onCreateOptionsMenu(menu);
    }
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item){
    	
    	switch(item.getItemId()) {
    	case MENU_PREFERENCES:
    		getPreferencesWindow();
    		return true;
    	case MENU_DOWNLOAD:
    		performDBRefresh();
    		return true;
    	case MENU_CHANNELS:
    		getChannelsWindow();
    		return true;
    	case MENU_ABOUT:
    		getAboutWindow();
    		return true;
    	}
    	
    	return super.onOptionsItemSelected(item);
    }

	private void performDBRefresh() {
		showDialog(PROGRESS_DIALOG);
		dba.open();
		Calendar cal = Calendar.getInstance();
		DateFormat df = new SimpleDateFormat("yyyy.MM.dd");
		String beforeDate = df.format(cal.getTime());
		dba.purgeEvents(beforeDate);
		Cursor cursor = dba.getOfflineChannels();
		String[] chIds = new String[cursor.getCount()];
		int i = 0;
		if (cursor.moveToFirst()){
			do {
				chIds[i] = cursor.getString(cursor.getColumnIndex(Constants.CHTABLE_CHANNEL_ID));
				//Log.d(Constants.LOG_MAIN_TAG + localLogTag, "ChID: " + chIds[i]);
				i++;
			} while (cursor.moveToNext());
		}
		//Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Cursor size: " + cursor.getCount() + " / chIds: " + chIds.length);
		cursor.close();
		dba.close();
		
		chRefreshThread = new ChannelDataRefreshTread(handler, chIds, "ChannelRefreshThread", tvprefs);
		Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Thread ID: " + chRefreshThread.getId());
		chRefreshThread.start();
	}

	private void getAboutWindow() {
		// TODO Auto-generated method stub
		
	}

	private void getChannelsWindow() {
		// TODO Auto-generated method stub
		Intent i = new Intent(TVGuide.this, ChannelManager.class);
		startActivity(i);
	}

	private void getPreferencesWindow() {
		// TODO Auto-generated method stub
		Intent i = new Intent(TVGuide.this, PreferencesScreen.class);
		startActivity(i);
	}
	
	@Override
	protected Dialog onCreateDialog(int id){
		switch (id){
		case PROGRESS_DIALOG:
			progress = new ProgressDialog(TVGuide.this);
			progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progress.setTitle("Adatok letöltése...");
			progress.setCancelable(false);
			return progress;
		default:
			return null;
		}		
	}
	
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
		case PROGRESS_DIALOG:
			progress.setProgress(0);
			progress.setSecondaryProgress(0);
		}
	}
	
	final Handler handler = new Handler(){
		public void handleMessage(Message msg){
			Bundle data = msg.getData();
			String target = data.getString("Target");
			if (ChannelDataRefreshTread.PROGRESS_BAR_STATE_MSG.equals(target)) {
				int pos = msg.arg1;
				int secpos = msg.arg2;
				//Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Progress: " + pos);
				progress.setProgress(pos);
				progress.setSecondaryProgress(secpos);
			}
			if (ChannelDataRefreshTread.PROGRESS_BAR_DONE_MSG.equals(target)){
				Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Thread finished");
				dismissDialog(PROGRESS_DIALOG);
				Toast.makeText(TVGuide.this, "A mûsorújság frissítése megtörtént", Toast.LENGTH_SHORT).show();
			}
			if (ChannelDataRefreshTread.PROGRESS_BAR_CHANNEL_MSG.equals(target)){
				Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Channel finished");
				ChannelData cd = (ChannelData)msg.obj;
				if (cd != null) {
					Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Channel data received, passing data to database");
					storeChannelData(cd);
				}
			}
		}
	};
	
	private void storeChannelData(ChannelData cd){
		Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Storing data...");
		dba.open();
		dba.storeChannelData(cd);
		dba.close();
	}
}