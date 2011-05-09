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
import android.widget.TextView;

public class AllCurrentOfflineEventsScreen extends ListActivity{
	
	private final static String localLogTag = "_OfflineEvents";

	TVGuideDB dba;
	ArrayList<String> items;
	Runnable viewOfflineEvents;
	
	private OfflineEventAdapter adapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		Log.d(Constants.LOG_MAIN_TAG + localLogTag, "All current offline events screen started.");
		
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
		Calendar cal = Calendar.getInstance();
		DateFormat df = new SimpleDateFormat("yyyy.MM.dd");
		DateFormat tf = new SimpleDateFormat("HH:mm");
		Date now = cal.getTime();
		
		String eventDay = df.format(now);
		String currentTime = tf.format(now);
		
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
        	
        	if (item != null) {
        		//Log.d(Constants.LOG_MAIN_TAG + localLogTag, ce.getTime() + " - " + ce.getEventName() + " / " + ce.getEventDesc() + "   (" + ce.getEventMore() + ")");
    			String[] temp = item.split("@@");
        		final String chName = temp[0];
    			final String text = temp[1];
    			
    			TextView offlineEventChannelTextView = (TextView) v.findViewById(R.id.offlineevent_channel);
        		TextView offlineEventTextView = (TextView) v.findViewById(R.id.offlineevent_text);

        		if (offlineEventChannelTextView != null) {
        			offlineEventChannelTextView.setText(chName);        			
        		}
        		if (offlineEventTextView != null) {
        			offlineEventTextView.setText(text);        			
        		}
        	}
        	return v;
        }
		
	}
	
}
