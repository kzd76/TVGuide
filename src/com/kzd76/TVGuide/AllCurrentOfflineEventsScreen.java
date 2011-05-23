package com.kzd76.TVGuide;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

public class AllCurrentOfflineEventsScreen extends ListActivity{
	
	private final static String localLogTag = "_OfflineEvents";

	TVGuideDB dba;
	ArrayList<String> items;
	Runnable viewOfflineEvents;
	
	private OfflineEventAdapter adapter;
	private String currentTime;
	private String eventDay;
	
	DateFormat df = new SimpleDateFormat("yyyy.MM.dd");
	DateFormat tf = new SimpleDateFormat("HH:mm");
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		Log.d(Constants.LOG_MAIN_TAG + localLogTag, "All current offline events screen started.");
		
		Calendar cal = Calendar.getInstance();
		Date now = cal.getTime();
		
		eventDay = df.format(now);
		currentTime = tf.format(now);
		
		
		dba = new TVGuideDB(this);
		items = new ArrayList<String>();
		this.adapter = new OfflineEventAdapter(this, R.layout.offlineevent, items);
		
		setListAdapter(adapter);
		
		viewOfflineEvents = new Runnable() {
			
			@Override
			public void run() {
				getItems();
			}
		};
		
		Thread thread = new Thread(null, viewOfflineEvents, "ViewCurrentOfflineEvents");
		thread.start();
	}
	
	private void getItems(){
		
		dba.open();
		String[] result = dba.getCurrentOfflineEvents(0, eventDay, currentTime);
		for (String item : result) {
			items.add(item);
		}
		dba.close();
		
		Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Response from database: " + items.size());
		
		runOnUiThread(returnRes);
	}
	
	private Runnable returnRes = new Runnable() {
		
		@Override
		public void run() {
			
			if ((items != null) && (items.size() > 0)){
				adapter.notifyDataSetChanged();
				for (int i = 0; i < items.size(); i++){
					//adapter.add(items.get(i));
				}
			}
			if (items.size() == 0) {
				adapter.notifyDataSetChanged();
				adapter.clear();
			}
			adapter.notifyDataSetChanged();
		}
	};
	
	private class OfflineEventAdapter extends ArrayAdapter<String> {

		ArrayList<String> adapterItems;
		
		public OfflineEventAdapter(Context context, int textViewResourceId,	ArrayList<String> items) {
			super(context, textViewResourceId, items);
			this.adapterItems = items;
		}
		
		public View getView(int position, View convertView, ViewGroup parent) {
        	
			View v = convertView;
        	
			if (v == null) {
        		LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        		v = vi.inflate(R.layout.offlineevent, null);
        	}
        	
        	String item = adapterItems.get(position);
        	try {
	        	if (item != null) {
	        		//Log.d(Constants.LOG_MAIN_TAG + localLogTag, ce.getTime() + " - " + ce.getEventName() + " / " + ce.getEventDesc() + "   (" + ce.getEventMore() + ")");
	    			String[] temp = item.split("@@");
	    			final String chName = temp[0];
	    			final String startTime = temp[1];
	        		final String text = temp[2];
	    			final String endTime = temp[3];
	    			final String nextEvent = temp[3] + ": " + temp[4]; 
	    			
	    			//Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Input: " + chName + " | " + startTime + " | " + text + " | " + endTime);
	    			
	    			TextView offlineEventChannelTextView = (TextView) v.findViewById(R.id.offlineevent_channel);
	        		TextView offlineEventTextView = (TextView) v.findViewById(R.id.offlineevent_text);
	        		TextView offlineNextEventTextView = (TextView) v.findViewById(R.id.offlinenextevent_text);
	        		ProgressBar pb = (ProgressBar) v.findViewById(R.id.progressbar);
	
	        		if (offlineEventChannelTextView != null) {
	        			offlineEventChannelTextView.setText(chName);        			
	        		}
	        		if (offlineEventTextView != null) {
	        			offlineEventTextView.setText(startTime + " " + text);        			
	        		}
	        		if ((offlineNextEventTextView != null) && (!nextEvent.equals("null: null"))) {
	        			offlineNextEventTextView.setText("Következik: " + nextEvent);
	        		}
	        		if (pb != null){
	        			if (endTime.equals("null")){
	        				pb.setVisibility(View.INVISIBLE);
	        			} else {
	        				Date startDate = tf.parse(startTime);
	        				Date endDate = tf.parse(endTime);
	        				Date currentDate = tf.parse(currentTime);
	        				
	        				Calendar cal = Calendar.getInstance();
	        				
	        				cal.setTime(startDate);
	        				long start = cal.getTimeInMillis();
	        				cal.setTime(endDate);
	        				long end = cal.getTimeInMillis();
	        				cal.setTime(currentDate);
	        				long current = cal.getTimeInMillis() - start;
	        				long length = end - start;
	        				int progress = (int) Math.round(100 * current / length);
	        				
	        				Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Progress is: " + progress + " (" + length + " = " + " " + end + " - " + start + ")");
	        				pb.setSecondaryProgress(progress);
	        			}
	        			
	        		}
	        	}
        	} catch (Exception e) {
        		Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Cannot create view for list item: " + item);
        		e.printStackTrace();
        	}
        	
        	return v;
        }
		
	}
	
}
