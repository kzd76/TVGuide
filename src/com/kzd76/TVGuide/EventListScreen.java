package com.kzd76.TVGuide;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class EventListScreen extends ListActivity {
	
	private static final String localLogTag = "_EventListScreen";
	
	private ArrayList<ChannelEvent> events = null;
	private ChannelEventAdapter adapter;
	private Runnable viewOnlineEvents;
	private Runnable viewOfflineEvents;
	private ProgressDialog progress = null;
	
	private int dayId = 1;
	private int chId = 1;
	//private String chName = "";
	
	private String[] channels = null;
	private String[] channelIds = null;
	
	private boolean online = true;
	private boolean isSpinnerVisible = true;
	
	private static String channelName;
	
	private TVGuideDB dba;
	
	private TVGuidePreference tvprefs;
	
	private double data;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        dba = new TVGuideDB(this);
        
        Bundle params = getIntent().getExtras();
        if (params != null) {
        	this.isSpinnerVisible = params.getBoolean("SpinnerVisibility", true);
        	
        	if (params.containsKey("ChannelName")) {
        		channelName = params.getString("ChannelName");
        	} else {
        		channelName = "";
        	}

        	if (params.containsKey("ChannelID")) {
        		this.chId = Integer.decode(params.getString("ChannelID"));
        	} else {
        		this.chId = 0;
        	}
        	
        	if (params.containsKey("Data")) {
        		this.data = Integer.decode(params.getString("Data"));
        	} else {
        		this.data = 0;
        	}
        }
        
        TextView dtv = (TextView) findViewById(R.id.eventlist_header);
        if (dtv != null) {
        	dtv.setText("Online forgalom: " + data + " kB");
        }
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		tvprefs = new TVGuidePreference(prefs);
		
        //Log.d(Constants.LOG_MAIN_TAG + localLogTag,"Spinner: " + this.isSpinnerVisible);
        //Log.d(Constants.LOG_MAIN_TAG + localLogTag,"Channel: " + this.chId);
        
        setContentView(R.layout.eventlist_screen);
        
        TextView rightSelector = (TextView) findViewById(R.id.rightselector);
        if (dayId == 7) {
        	rightSelector.setVisibility(View.INVISIBLE);
        }
        rightSelector.setClickable(true);
        rightSelector.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				incrementDays();
			}
		});
        
        TextView leftSelector = (TextView) findViewById(R.id.leftselector);
        if (dayId == 1) {
        	leftSelector.setVisibility(View.INVISIBLE);
        }
        leftSelector.setClickable(true);
        leftSelector.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				decrementDays();
			}
		});
        
        events = new ArrayList<ChannelEvent>();
        this.adapter = new ChannelEventAdapter(this, R.layout.row, events);
        
        setListAdapter(adapter);
        
        if(isSpinnerVisible) {
	        ArrayList<String> channelsArray = new ArrayList<String>();
	        ArrayList<String> channelIdsArray = new ArrayList<String>();
	        
	        try {
	        	
	        	dba.open();
	        	Cursor cursor = dba.getChannels();
	        	
	        	if (cursor.moveToFirst()) {
	        		do {
	        			channelsArray.add(cursor.getString(cursor.getColumnIndex(Constants.CHTABLE_CHANNEL_NAME)));
	        			channelIdsArray.add(cursor.getString(cursor.getColumnIndex(Constants.CHTABLE_CHANNEL_ID)));
	        		} while (cursor.moveToNext());
	        	} else {
	        		Toast.makeText(this, "Nincs letöltött csatornalista az adatbázisban! Frissítsd a listát a webrõl!", Toast.LENGTH_SHORT).show();
	        	}
	        	
	        	cursor.close();
	        	dba.close();
	        	
	        	/*
	        	//Scanner scanner = new Scanner(urlConn.getInputStream());
	        	Scanner scanner = new Scanner(this.getResources().openRawResource(R.raw.channels));
	        	int i = 0;
	        	while (scanner.hasNextLine()){
	        		String channeltext = scanner.nextLine();
	        		String[] channel = channeltext.split("\\|\\|");
	        		//Log.i(Constants.LOG_MAIN_TAG + localLogTag,"Name: " + channel[0] + " ID: " + channel[1] + " / Original: " + channeltext);
	        		if (channel.length == 2) {
	        			channelsArray.add(channel[0]);
	        			channelIdsArray.add(channel[1]);
	        		}
	        		i++;
	        	}
	        	Log.d(Constants.LOG_MAIN_TAG + localLogTag,"Total rows from file: " + i);
	        	
		        //Log.i(Constants.LOG_MAIN_TAG + localLogTag, "channel names: " + channelsArray.size());
		        //Log.i(Constants.LOG_MAIN_TAG + localLogTag, "channel ids: " + channelIdsArray.size());
		        */
	        	
		        channels = new String[channelsArray.size()];
		        channelIds = new String[channelIdsArray.size()];
		        
		        channelsArray.toArray(channels);
		        channelIdsArray.toArray(channelIds);
		        
		        //Log.i(Constants.LOG_MAIN_TAG + localLogTag, "channel names: " + channels.length);
		        //Log.i(Constants.LOG_MAIN_TAG + localLogTag, "channel ids: " + channelIds.length);
		        
		        chId = Integer.decode(channelIdsArray.get(0));
		        Spinner spin = (Spinner) findViewById(R.id.spinner);
		        
		        ArrayAdapter<String> spinadapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, channels);
		        spinadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		        spin.setAdapter(spinadapter);
		        
		        spin.setOnItemSelectedListener(new OnItemSelectedListener() {
		            @Override
		            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
		            	if ((id <= channels.length) && (id <= channelIds.length)) {
		            		int pos = (int) id;
		            		events.clear();
		            		runOnUiThread(returnRes);
		            		channelName = channels[pos];
		            		getChannelEvents(channelIds[pos], dayId);
		            	} else {
		            		// Invalid channel selection
		            	}
		            }
		
		            @Override
		            public void onNothingSelected(AdapterView<?> parentView) {
		                // your code here
		            }
		
		        });
	        } catch (Exception e) {
		        Log.e(Constants.LOG_MAIN_TAG + localLogTag,"Error while composing the spinner");
		        e.printStackTrace();
	        }
        } else {
        	//Just call the getEventList function with fixed parameters, no spinner is available
        	Spinner spin = (Spinner) findViewById(R.id.spinner);
        	spin.setVisibility(View.GONE);
        	events.clear();
    		runOnUiThread(returnRes);
        	getChannelEvents(Integer.toString(chId), dayId);
        }
        
    }
    
	final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            Bundle b = msg.getData();
            String id = b.getString("Id");
            if ("ChannelData".equals(id)) {
            	String caption = b.getString("Caption");
            	int chdata = b.getInt("Data");
            	TextView txt = (TextView) findViewById(R.id.channelinfo);
            	txt.setText(Html.fromHtml(caption));
            	
            	data = data + (chdata / 1000);
        		TextView dtv = (TextView) findViewById(R.id.eventlist_header);
        		if (dtv != null) {
                	dtv.setText("Online forgalom: " + data + " kB");
                }
            }
            if ("EventData".equals(id)) {
            	int chdata = b.getInt("Data");
            	
            	data = data + (chdata / 1000);
        		TextView dtv = (TextView) findViewById(R.id.eventlist_header);
        		if (dtv != null) {
                	dtv.setText("Online forgalom: " + data + " kB");
                }
            }
        }
    };
    
    private void getChannelEvents(String channelId, int dayId ){
    	final String channel = channelId;
    	final int day = dayId;
    	
    	/*
    	 * Calculating date from dayId for database query
    	 */ 
    	
    	int dayDiff = day - 1;
    	DateFormat df = new SimpleDateFormat("yyyy.MM.dd");
    	Calendar cal = Calendar.getInstance();
    	cal.add(Calendar.DATE, dayDiff);
    	String requestedDate = df.format(cal.getTime());
    	
    	/*
    	 * Getting channel data from database and deciding about online/offline mode
    	 */ 
    	
    	dba.open();
    	final ChannelData cd = dba.getChannelEvents(channelId, requestedDate);
    	if (cd != null) {
    		online = false;
    		Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Data found in database, going offline...");
    	} else {
    		online = true;
    		Log.d(Constants.LOG_MAIN_TAG + localLogTag, "No data found in database, staying online...");
    	}
    	dba.close();
    	
    	/*
    	 * If online is true (channel data was not found in database) events will collected from WEB
    	 * through WebDataProcessor
    	 * 
    	 * If channel data was found in database events will be presented directly 
    	 */ 
    	
    	if (online) {
	    	try {
	            
		    	viewOnlineEvents = new Runnable(){
		            @Override
		            public void run() {
		                getOnlineEvents(channel, day);
		            }
		            
		        };
		        
		        progress = ProgressDialog.show(EventListScreen.this, "Türelem...", "Az adatok letöltése folyamatban van.", true);
		        Thread thread = new Thread(null, viewOnlineEvents, "BackgroundEventProcessing");
		        thread.start();
		        
	        } catch (Exception e) {
	        	//txt.append(e.getMessage());
	        }
    	} else {
    		if (cd != null) {
    			
    			viewOfflineEvents = new Runnable(){
		            @Override
		            public void run() {
		                getOfflineEvents(cd);
		            }
		            
		        };
		        
		        progress = ProgressDialog.show(EventListScreen.this, "Türelem...", "Az adatok betöltése folyamatban van.", true);
		        Thread thread = new Thread(null, viewOfflineEvents, "BackgroundEventProcessing");
		        thread.start();
    		} else {
    			// TODO No data from database
    		}
    	}
    	
    }
    
    private void getOfflineEvents(ChannelData cd){
    	
    	Message msg = handler.obtainMessage();
    	Bundle b = new Bundle();
    	String caption = cd.getCaption();
    	
    	if ((caption == null) || (caption.length() == 0)) {
    		caption = "<b>Nincs adat</b>";
    	}
    	
    	b.putString("Id", "ChannelData");
    	b.putString("Caption", caption);
    	b.putInt("Data", 0);
    	msg.setData(b);
    	handler.sendMessage(msg);
    	
    	events = cd.getEvents();
    	
    	Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Events collected from database: " + events.size());
    	
    	runOnUiThread(returnRes);
    }
    
    private void getOnlineEvents(String channelId, int dayId) {
    	ChannelData cd = WebDataProcessor.processChannelData(channelId, dayId);
    	
    	Message msg = handler.obtainMessage();
    	Bundle b = new Bundle();
    	String caption = cd.getCaption();
    	
    	b.putString("Id", "ChannelData");
    	b.putString("Caption", caption);
    	b.putInt("Data", cd.getDataLength());
    	msg.setData(b);
    	handler.sendMessage(msg);
    	
    	events = cd.getEvents();
    	
    	runOnUiThread(returnRes);
    }
    
    private Runnable returnRes = new Runnable() {

        @Override
        public void run() {
            if(events != null && events.size() > 0){
                adapter.notifyDataSetChanged();
                for(int i=0;i<events.size();i++)
                adapter.add(events.get(i));
            }
            if (events.size() == 0) {
            	adapter.notifyDataSetChanged();
            	adapter.clear();
            }
            if (progress != null) {
            	progress.dismiss();
            }
            adapter.notifyDataSetChanged();
        }
    };
    
    private void followTargetOnline(String header, String target, String startTime) {
    	try {
    		
    		EventData ed = WebDataProcessor.processEventData(header, target, tvprefs.isDownloadOnlineImages());
    		
    		Message msg = handler.obtainMessage();
        	Bundle b = new Bundle();
        	
        	b.putString("Id", "EventData");
        	b.putInt("Data", ed.getDataLength());
        	msg.setData(b);
        	handler.sendMessage(msg);
    		
    		followTarget(ed, startTime);
    	} catch (Exception e) {
    		Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Error while collecting event data from the WEB");
    		e.printStackTrace();
    	}
    }
    
    private void followTarget(EventData ed, String startTime) {
    	try{
    		String eventdesc = ed.getDesc();
    		String imgalt = ed.getImageAlt();
    		Bitmap bmp = ed.getImage();
    		String eventtitle = ed.getTitle();
    		String info = ed.getInfo();
    		
    		if (eventdesc == null) eventdesc = "";
    		if (info == null) info = "";
    		
    		if ((eventdesc.length() == 0) && (imgalt.length() == 0) && (bmp == null)) {
    			
    		} else {
    			Dialog dialog = new Dialog(this);

    			dialog.setContentView(R.layout.description_dialog);
    			dialog.setTitle(eventtitle);
    			
    			TextView itxt = (TextView) dialog.findViewById(R.id.infotext);
    			if ((info != null) && (info.length() > 0)) {
    				itxt.setText(info);
    			} else {
    				itxt.setVisibility(View.GONE);
    			}
    			
    			TextView txt = (TextView) dialog.findViewById(R.id.text);
    			txt.setText(eventdesc);
    			
    			ImageView image = (ImageView) dialog.findViewById(R.id.image);
				
    			if (bmp != null) {
    				image.setImageBitmap(bmp);
    			} else {
    				image.setVisibility(View.GONE);
    			}
    			
    			Button alertButton = (Button) dialog.findViewById(R.id.setalert);
    			if (alertButton != null) {
    				
    				final String eventStartTime = startTime;
    				final String eventNameText = eventtitle;
    				final String eventDescription = startTime + " - " + channelName;
    				
    				//alertButton.setVisibility(View.INVISIBLE);

    				alertButton.setOnClickListener(new View.OnClickListener() {
        				
        				@Override
        				public void onClick(View v) {
        					setAlarm(eventStartTime, eventNameText, eventDescription);
        				}
        			});
        			
    			} else {
    				Log.d(Constants.LOG_MAIN_TAG + localLogTag,"Button initialization failed");
    			}
    			
    			
    			dialog.show();
    		}
    	} catch (Exception e) {
    		Log.e(Constants.LOG_MAIN_TAG + localLogTag, "Error in event description dialog");
    		e.printStackTrace();
    	}
    }
    
    @SuppressWarnings("unused")
	private void setAlarm(String startTime, String eventName, String eventDescription){
    	try {
    		Calendar cal = Calendar.getInstance();
    		
    		DateFormat df = new SimpleDateFormat("hh:mm");
        	Date start = df.parse(startTime);
        	Date now = cal.getTime();
        	
        	int newDay = cal.get(Calendar.DATE);
        	if (dayId > 1) {
        		newDay = newDay + (dayId - 1);
        	}
        	int newHour = start.getHours();
    		int newMin = start.getMinutes();
    		
    		int newYear = cal.get(Calendar.YEAR);
    		int newMonth = cal.get(Calendar.MONTH);
    		
    		cal.set(newYear, newMonth, newDay, newHour, newMin, 0);
    		
    		start = cal.getTime();
    		
    		Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Now: " + now + ", start: " + start);
        	
        	if (start.after(now)) {
        		
        		//cal.add(Calendar.SECOND, 30);
        		
        		Intent intent = new Intent(EventListScreen.this, EventAlarmService.class);
        		intent.putExtra("EventTitle", eventName);
        		intent.putExtra("EventDescription", eventDescription);
        		PendingIntent pendingIntent = PendingIntent.getService(EventListScreen.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        		
        		int reqCode = 760630; //can be anything
            	String alarmText = "Értesítés mûsorkezdésrõl";
            	Log.d(Constants.LOG_MAIN_TAG + localLogTag,"Alarm set for " + eventName + " / " + eventDescription);
            	
            	AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        		alarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
        		String rest = "...";
        		Toast.makeText(this, "Értesítés " + rest + " múlva", Toast.LENGTH_LONG).show();
        		
        	} else {
        		Toast.makeText(this, "A mûsor korábban kezdõdött, nem lehet értesítõt beállítani", Toast.LENGTH_SHORT).show();
        	}
        	
		} catch (Exception e) {
			e.printStackTrace();
		} 	
    }
    
    private class ChannelEventAdapter extends ArrayAdapter<ChannelEvent> {

        private ArrayList<ChannelEvent> items;

        public ChannelEventAdapter(Context context, int textViewResourceId, ArrayList<ChannelEvent> items) {
                super(context, textViewResourceId, items);
                this.items = items;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
        	View v = convertView;
        	if (v == null) {
        		LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        		v = vi.inflate(R.layout.row, null);
        	}
        	ChannelEvent ce = items.get(position);
        	if (ce != null) {
        		//Log.d(Constants.LOG_MAIN_TAG + localLogTag, ce.getTime() + " - " + ce.getEventName() + " / " + ce.getEventDesc() + "   (" + ce.getEventMore() + ")");
    			final String eventStartTime = ce.getTime();
    			final String en = ce.getEventName();
    			final String ed = ce.getEventDesc();
    			final String em = ce.getEventMore();
    			TextView it = (TextView) v.findViewById(R.id.timetext);
        		TextView tt = (TextView) v.findViewById(R.id.toptext);
        		TextView bt = (TextView) v.findViewById(R.id.bottomtext);
        		ImageView iv = (ImageView) v.findViewById(R.id.infoimage);
        		if (it != null) {
        			it.setText(eventStartTime);        			
        		}
        		if (tt != null) {
        			tt.setText(en);        			
        		}
        		if(bt != null){
        			if (ed.length() > 0) {
        				bt.setText(ed);        				
        			} else {
        				bt.setVisibility(View.GONE);        				
        			}
        			
        		}
        		if (iv != null) {
        			if (online) {
	        			if (em.length() > 0) {
	        				final String header = en;
	        				final String target = em;
	        				iv.setImageResource(R.drawable.information_bw);
	        				iv.setClickable(true);
	        				iv.setOnClickListener(new View.OnClickListener() {
	        					
	        					public void onClick(View v) {
	        						followTargetOnline(header, target, eventStartTime);
	        					}
	        				});
	        				iv.setVisibility(View.VISIBLE);
	        			} else {
	        				iv.setVisibility(View.INVISIBLE);
	        			}
        			} else {
        				
        				// Offline case
        				
        				final EventData eData = ce.getEventData();
        				if (eData != null) {
        					
        					// Event description found in database
        					
        					String temp = eData.getDesc() + eData.getInfo();
        					if (temp.length() > 0) {
        						iv.setImageResource(R.drawable.information_bw);
    	        				iv.setClickable(true);
    	        				iv.setOnClickListener(new View.OnClickListener() {
    	        					
    	        					public void onClick(View v) {
    	        						followTarget(eData, eventStartTime);
    	        					}
    	        				});
        					} else {
        						
        						// Event data from database is null, event may has link for details to follow
        						
        						if ((em.length() > 0) && (!tvprefs.isEventsRestrictedWhenOffline()) && (tvprefs.isOnlineEnabled())){
    								final String header = en;
        	        				final String target = em;
        	        				iv.setImageResource(R.drawable.information_bw);
        	        				iv.setClickable(true);
        	        				iv.setOnClickListener(new View.OnClickListener() {
        	        					
        	        					public void onClick(View v) {
        	        						followTargetOnline(header, target, eventStartTime);
        	        					}
        	        				});
        	        				iv.setVisibility(View.VISIBLE);
    							} else {
    								iv.setVisibility(View.INVISIBLE);
    							}
        					}
        				} else {
        					
        					// Event data from database is null, event may has link for details to follow
        					
        					if ((em.length() > 0) && (!tvprefs.isEventsRestrictedWhenOffline()) && (tvprefs.isOnlineEnabled())) {
        						final String header = en;
    	        				final String target = em;
    	        				iv.setImageResource(R.drawable.information_bw);
    	        				iv.setClickable(true);
    	        				iv.setOnClickListener(new View.OnClickListener() {
    	        					
    	        					public void onClick(View v) {
    	        						followTargetOnline(header, target, eventStartTime);
    	        					}
    	        				});
    	        				iv.setVisibility(View.VISIBLE);
        					} else {
        						iv.setVisibility(View.INVISIBLE);
        					}
        				}
        			}
        		}
        	}
        	return v;
        }
	}
    
    private void incrementDays() {
		if (dayId<7) {
			dayId++;
		}
    	if (dayId == 7) {
    		TextView rightSelector = (TextView) findViewById(R.id.rightselector);
    		rightSelector.setVisibility(View.INVISIBLE);
    	}
    	if (dayId == 2) {
    		TextView leftSelector = (TextView) findViewById(R.id.leftselector);
    		leftSelector.setVisibility(View.VISIBLE);
    	}
    	
    	if (isSpinnerVisible) {
	    	Spinner spin = (Spinner) findViewById(R.id.spinner);
	    	
	    	int pos = spin.getSelectedItemPosition();
	    	
	    	if ((pos <= channels.length) && (pos <= channelIds.length)) {
	    		events.clear();
	    		runOnUiThread(returnRes);
	    		getChannelEvents(channelIds[pos], dayId);
	    	} else {
	    		// Invalid channel selection
	    	}
    	} else {
    		events.clear();
    		runOnUiThread(returnRes);
    		getChannelEvents(Integer.toString(chId), dayId);
    	}
	}
    
    private void decrementDays() {
		if (dayId>1) {
			dayId--;
		}
    	if (dayId == 1) {
    		TextView leftSelector = (TextView) findViewById(R.id.leftselector);
    		leftSelector.setVisibility(View.INVISIBLE);
    	}
    	if (dayId == 6) {
    		TextView rightSelector = (TextView) findViewById(R.id.rightselector);
    		rightSelector.setVisibility(View.VISIBLE);
    	}
    	
    	if (isSpinnerVisible) {
    	    Spinner spin = (Spinner) findViewById(R.id.spinner);
	    	
	    	int pos = spin.getSelectedItemPosition();
	    	
	    	if ((pos <= channels.length) && (pos <= channelIds.length)) {
	    		events.clear();
	    		runOnUiThread(returnRes);
	    		getChannelEvents(channelIds[pos], dayId);
	    	} else {
	    		// Invalid channel selection
	    	}
    	} else {
    		events.clear();
    		runOnUiThread(returnRes);
    		getChannelEvents(Integer.toString(chId), dayId);
    	}
	}
    
}
