package com.kzd76.TVGuide;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Scanner;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

public class EventListScreen extends ListActivity {
	
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
	
	private static final String localLogTag = "_EventListScreen";
	
	private TVGuideDB dba;
	
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
        	/*
        	if (params.containsKey("ChannelName")) {
        		this.chName = params.getString("ChannelName");
        	} else {
        		this.chName = "";
        	}
        	*/
        	if (params.containsKey("ChannelID")) {
        		this.chId = Integer.decode(params.getString("ChannelID"));
        	} else {
        		this.chId = 0;
        	}
        }
        
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
		            		//chName = channels[pos];
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
            String caption = b.getString(null);
            TextView txt = (TextView) findViewById(R.id.channelinfo);
            txt.setText(Html.fromHtml(caption));
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
    	Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Events collected from database: " + events.size());
    	
    	Message msg = handler.obtainMessage();
    	Bundle b = new Bundle();
    	String caption = cd.getCaption();
    	
    	if ((caption == null) || (caption.length() == 0)) {
    		caption = "<b>Nincs adat</b>";
    	}
    	
    	b.putString(null, caption);
    	msg.setData(b);
    	handler.sendMessage(msg);
    	
    	events = cd.getEvents();
    	
    	runOnUiThread(returnRes);
    }
    
    private void getOnlineEvents(String channelId, int dayId) {
    	ChannelData cd = WebDataProcessor.processChannelData(channelId, dayId);
    	
    	Message msg = handler.obtainMessage();
    	Bundle b = new Bundle();
    	String caption = cd.getCaption();
    	b.putString(null, caption);
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
    
    private void followTargetOnline(String header, String target) {
    	try {
    		
    		EventData ed = WebDataProcessor.processEventData(header, target);
    		
    		String eventdesc = ed.getDesc();
    		String imgalt = ed.getImageAlt();
    		Bitmap bmp = ed.getImage();
    		String eventtitle = ed.getTitle();
    		String info = ed.getInfo();
    		
    		if ((eventdesc.length() == 0) && (imgalt.length() == 0) && (bmp == null)) {
    			
    		} else {
    			Dialog dialog = new Dialog(this);

    			dialog.setContentView(R.layout.description_dialog);
    			dialog.setTitle(eventtitle);
    			
    			TextView itxt = (TextView) dialog.findViewById(R.id.infotext);
    			if (info.length() > 0) {
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
    				alertButton.setVisibility(View.INVISIBLE);
    				/*
    				alertButton.setOnClickListener(new View.OnClickListener() {
        				
        				@Override
        				public void onClick(View v) {
        					setAlarm(eventStartTime, eventNameText);
        				}
        			});
        			*/
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
	private void setAlarm(String startTime, String eventName){
    	try {
    		Calendar cal = Calendar.getInstance();
    		// TODO Fix Alarm
    		
    		/*
    		DateFormat df = new SimpleDateFormat("hh:mm");
        	Date start = df.parse(startTime);
        	Date now = cal.getTime();
        	int newDay = now.getDay();
        	if (dayId > 1) {
        		newDay = newDay + (dayId - 1);
        	}
        	int newHour = start.getHours();
    		int newMin = start.getMinutes();
    		
    		cal.set(now.getYear(), now.getMonth(), newDay, newHour, newMin);
        	
        	if ((dayId > 1) || (cal.getTime().after(now))) {
        		
        	} else {
        		Toast.makeText(this, "A mûsor korábban kezdõdött, nem lehet értesítõt beállítani", Toast.LENGTH_SHORT).show();
        	}
        	*/
    		/*
    		cal.add(Calendar.SECOND, 30);
    		
        	Intent intent = new Intent(this, AlarmReceiver.class);
        	int reqCode = 760630; //can be anything
        	String alarmText = "Beállított értesítés\n" + chName + "\n" + startTime + " - " + eventName;
        	Log.d(Constants.LOG_MAIN_TAG + localLogTag,alarmText);
        	intent.putExtra("alarm_message", alarmText);
        	PendingIntent alarmSender = PendingIntent.getBroadcast(this, reqCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        	AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
    		alarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), alarmSender);
    		String rest = "";
    		Toast.makeText(this, "Értesítés " + rest + " múlva", Toast.LENGTH_LONG).show();
    		*/
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
    			final String st = ce.getTime();
    			final String en = ce.getEventName();
    			final String ed = ce.getEventDesc();
    			final String em = ce.getEventMore();
    			TextView it = (TextView) v.findViewById(R.id.timetext);
        		TextView tt = (TextView) v.findViewById(R.id.toptext);
        		TextView bt = (TextView) v.findViewById(R.id.bottomtext);
        		ImageView iv = (ImageView) v.findViewById(R.id.infoimage);
        		if (it != null) {
        			it.setText(st);        			
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
	        						followTargetOnline(header, target);
	        					}
	        				});
	        			} else {
	        				iv.setVisibility(View.INVISIBLE);
	        			}
        			} else {
        				// TODO Info "button" action when data is coming from database. Temporarily it is now invisible
        				iv.setVisibility(View.INVISIBLE);
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
