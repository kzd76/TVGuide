package com.kzd76.TVGuide;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.widget.LinearLayout;
import android.widget.TextView;
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
	
	private DataTrafficProgressDialog progress = null;
	private ChannelDataRefreshTread chRefreshThread;
	private TVGuidePreference tvprefs;
	
	private static final String localLogTag = "_Main";
	
	private double downloadedAmount = 0;
	private double totalData = 0;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.main);
        
        favChList = new ArrayList<Channel>();
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		tvprefs = new TVGuidePreference(prefs);
		
		ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo mobileInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		
		Log.d(Constants.LOG_MAIN_TAG + localLogTag, "WiFi info: " + wifiInfo.toString());
		Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Mobile info: " + mobileInfo.toString());
		Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Active info: " + activeNetworkInfo.toString());
		
		if ((tvprefs.isOnlyWifi()) && (!wifiInfo.isConnectedOrConnecting())){
			
			//TODO Alert the user here that only wifi is preferred for connections but no wifi is available, then open the WiFi preferences screen
			//startActivityForResult(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS), 0);
		}
		
		TextView ht = (TextView) findViewById(R.id.main_header);
		if (ht != null) {
			ht.setText("Online forgalom: " + totalData + " kB");
		}
		
		LinearLayout ll = (LinearLayout) findViewById(R.id.main_layout);
		ll.setOrientation(LinearLayout.VERTICAL);
		
		int channelsInDatabase = checkDB();
		
		if (channelsInDatabase > 0) {
        	
			// Top channels found in database, creating buttons for short access and ALL button to a spinner featured list screen 
			
			Button btnAll = new Button(this);
			btnAll.setText("Minden adó");
    		btnAll.setOnClickListener(new View.OnClickListener() {
    			@Override
    			public void onClick(View v) {
    				Intent i = new Intent(TVGuide.this, EventListScreen.class);
    				i.putExtra("SpinnerVisibility", true);
    				i.putExtra("TotalData", totalData);
    				startActivity(i);
    			}
    		});
    		
    		ll.addView(btnAll);
    		
    		
    		int j = 0;
    		
    		LinearLayout row = new LinearLayout(this);
    		
    		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
    		lp.weight = 1.0f;
    		
    		row.setOrientation(LinearLayout.HORIZONTAL);
    		
    		for (int i = 0; i < channelsInDatabase; i++) {
				j++;
				final Channel channel = favChList.get(i);
				Button btn = new Button(this);
				btn.setText(channel.name);
				btn.setLines(2);
				btn.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						Intent i = new Intent(TVGuide.this, EventListScreen.class);
						i.putExtra("SpinnerVisibility", false);
						i.putExtra("ChannelName", channel.name);
						i.putExtra("ChannelID", channel.id);
						i.putExtra("TotalData", totalData);
						startActivityForResult(i, 0);
					}
				});
				
				row.addView(btn, lp);
				
				if (j == 3) {
					j = 0;
					ll.addView(row, lp);
					row = new LinearLayout(this);
					row.setOrientation(LinearLayout.HORIZONTAL);
				}
			}
			
    		if (j != 0) {
    			ll.addView(row, lp);
    		}
			
        } else {
        	// No channels were found in database
        	// TODO Create a dialog here to redirect the user to channel manager window
        	// TODO if user selects no for download channel list redirect him/her to a new screen where "browsing" is implemented - channel groups - channels - events
        	
        	/*
        	Intent i = new Intent(TVGuide.this, EventListScreen.class);
			i.putExtra("SpinnerVisibility", true);
			startActivity(i);
			*/
        	
        }
		
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Called activity finished: " + data.toString());
		if (resultCode == RESULT_OK){
			Bundle bundle = data.getExtras();
			double received = bundle.getDouble("TotalData");
			Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Received totalData=" + received);
			totalData = totalData + received;
			
			TextView ht = (TextView) findViewById(R.id.main_header);
			if (ht != null) {
				ht.setText("Online forgalom: " + totalData + " kB");
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
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
        		String temp = cursor.getString(cursor.getColumnIndex(Constants.CHTABLE_OFFLINE_MARKER));
        		if (Constants.CHTABLE_OFFLINE_TRUE.equals(temp)) {
        			ch.offline = true;
        		} else {
        			ch.offline = false;
        		}
        		
        		byte[] bb = cursor.getBlob(cursor.getColumnIndex(Constants.CHTABLE_CHANNEL_IMAGE));
				if ((bb != null) && (bb.length > 0)) {
					Bitmap image = BitmapFactory.decodeByteArray(bb, 0, bb.length);
					ch.image = image;
				} else {
					ch.image = null;
				}
        		
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
		//sendBroadcast(new Intent(TVGuideWidget.ACTION_UPDATE));
		Intent i = new Intent(TVGuide.this, AllCurrentOfflineEventsScreen.class);
		startActivity(i);
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
			progress = new DataTrafficProgressDialog(TVGuide.this, "Adatok letöltése...");
			progress.setDownloadedUnit(DataTrafficProgressDialog.TrafficUnit.KILOBYTE);
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
			progress.setDownloadedAmount(0);
			downloadedAmount = 0;
		}
	}
	
	final Handler handler = new Handler(){
		public void handleMessage(Message msg){
			Bundle data = msg.getData();
			String target = data.getString("Target");
			if (ChannelDataRefreshTread.PROGRESS_BAR_STATE_MSG.equals(target)) {
				int pos = data.getInt("Pos", 0);
				int secpos = data.getInt("SecPos", 0);
				double amount = data.getDouble("Amount", 0);
				String statusMsg = data.getString("Status");
				downloadedAmount = downloadedAmount + amount;
				//Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Progress: " + pos);
				progress.setProgress(pos);
				progress.setSecondaryProgress(secpos);
				progress.setDownloadedAmount(downloadedAmount);
				progress.setStatusText(statusMsg);
			}
			if (ChannelDataRefreshTread.PROGRESS_BAR_DONE_MSG.equals(target)){
				Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Thread finished");
				dismissDialog(PROGRESS_DIALOG);
				Toast.makeText(TVGuide.this, "A mûsorújság frissítése megtörtént", Toast.LENGTH_SHORT).show();
				totalData = totalData + downloadedAmount;
				downloadedAmount = 0;
				sendBroadcast(new Intent(TVGuideWidget.ACTION_UPDATE));
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