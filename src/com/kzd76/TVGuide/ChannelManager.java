package com.kzd76.TVGuide;

import java.util.ArrayList;
import java.util.Scanner;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.kzd76.ChannelListView.*;

public class ChannelManager extends ListActivity{
	
	private static final int GROUP_DEFAULT = 0;
	private static final int MENU_GETWEB = 1;
	private static final int MENU_SAVE = 2;
	private static final int MENU_RELOAD = 3;
	
	private static final String localLogTag = "_CHManager";
	
	private ArrayList<Channel> chList;
	private ChannelAdapter adapter;
	private Runnable viewChannels;
	
	private TVGuideDB dba;
	
	private final int DOWNLOAD_WEB = 0;
	private final int CANCEL = 1;
	
	private final int WEBDOWNLOAD_DIALOG = 0;
	private final int CHANNELSTORE_DIALOG = 1;
	
	private boolean changed = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		setContentView(R.layout.channelmanager_screen);
		
		chList = new ArrayList<Channel>();
		
		viewChannels = new Runnable() {
			
			@Override
			public void run() {
				Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Calling getChList");
				getChList();
			}
		};
		
		Thread thread = new Thread(null, viewChannels, "BackgroundChannelProcessing");
        thread.start();
		
	}
	
	@Override
    public void onDestroy(){
    	if (changed) {
    		//TODO Ask user about changed list and save list if needed.
    		Log.d(Constants.LOG_MAIN_TAG + localLogTag, "List changed, dialog goes here");
    	}
    	super.onDestroy();
    }
	
	private ChangeListener changeListener = 
    	new ChangeListener() {
			
			@Override
			public void onChange() {				
				changed = true;
			}
		};
	
	private DropListener dropListener = new DropListener() {
		
		@Override
		public void onDrop(int from, int to) {
			ListAdapter adapter = getListAdapter();
			if (adapter instanceof ChannelAdapter) {
				((ChannelAdapter) adapter).onDrop(from, to);
				getListView().invalidateViews();
			}
		}
	};
	
	private RemoveListener removeListener = new RemoveListener() {
		
		@Override
		public void onRemove(int which) {
			ListAdapter adapter = getListAdapter();
			if (adapter instanceof ChannelAdapter) {
				((ChannelAdapter) adapter).onRemove(which);
				getListView().invalidateViews();
			}
		}
	};
	
	private DragListener dragListener = new DragListener() {
		
		int bgColor = 0xe0444444;
		int defaultBgColor = 0xe0444444;
		int deleteBgColor = 0xe0441010;
		
		boolean inDeleteRange = false;
		
		int itemViewWidth;
		int itemViewHeight;
		View dragView;
		
		//float origTextSize;
		int origTextWidth;
		int origX;
		int origY;
		
		ViewGroup.LayoutParams origLayoutParams;
		
		public void onDrag(int x, int y, ListView listView) {
			boolean prevInDeleteRange = inDeleteRange;
			
			int deltaX = (int) Math.abs(origX - x);
			int deltaY = (int) Math.abs(origY - y);

			if ((deltaX > 5*itemViewWidth/8) && (deltaY < itemViewHeight)) {
				inDeleteRange = true;
				if (!prevInDeleteRange) {
					dragView.setBackgroundColor(deleteBgColor);
					TextView tv = (TextView)dragView.findViewById(R.id.channelnametext);
					tv.setText("Engedd el itt a törléshez");
				}
			} else {
				inDeleteRange = false;
				if (prevInDeleteRange) {
					dragView.setBackgroundColor(bgColor);
					TextView tv = (TextView)dragView.findViewById(R.id.channelnametext);
					tv.setText("Húzd az új helyére vagy jobbra a törléshez");
				}
			}			
		}
		
		public void onStartDrag(View itemView, int x, int y) {
			//itemView.setVisibility(View.INVISIBLE);
			//defaultBgColor = itemView.getDrawingCacheBackgroundColor();
			itemView.setBackgroundColor(bgColor);
			itemViewHeight = itemView.getHeight();
			itemViewWidth = itemView.getWidth();
			origX = x;
			origY = y;
			dragView = itemView;
			ImageView iv = (ImageView)itemView.findViewById(R.id.dragimage);
			if (iv != null) {
				iv.setVisibility(View.INVISIBLE);
			}
			CheckBox cb = (CheckBox)itemView.findViewById(R.id.offlinecheck);
			if (cb != null) {
				cb.setVisibility(View.INVISIBLE);
			}
		}
		
		public void onStopDrag(View itemView) {
			//itemView.setVisibility(View.VISIBLE);
			itemView.setBackgroundColor(defaultBgColor);
			ImageView iv = (ImageView)itemView.findViewById(R.id.dragimage);
			if (iv != null) {
				iv.setVisibility(View.VISIBLE);
			}
			TextView tv = (TextView)itemView.findViewById(R.id.channelnametext);
			tv.setSingleLine(true);
			tv.setWidth(origTextWidth);
			tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);
			//tv.setTextSize(origTextSize);
			tv.setLayoutParams(origLayoutParams);
			CheckBox cb = (CheckBox)itemView.findViewById(R.id.offlinecheck);
			if (cb != null) {
				cb.setVisibility(View.VISIBLE);
			}
		}

		public void afterStartDrag(View itemView) {
			TextView tv = (TextView)itemView.findViewById(R.id.channelnametext);
			origTextWidth = tv.getWidth();
			//origTextSize = tv.getTextSize();
			origLayoutParams = tv.getLayoutParams();
			tv.setWidth(itemViewWidth / 2);
			tv.setSingleLine(false);
			tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
			tv.setText("Húzd az új helyére vagy jobbra a törléshez");
		}
		
	};
	
	private OnClickListener checkboxListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			View view = (View)v.getParent();
			int index = getListView().indexOfChild(view);
			ListAdapter la = getListAdapter();
			CheckBox cb = (CheckBox)view.findViewById(R.id.offlinecheck);
			if (cb.isChecked()){
				Toast.makeText(ChannelManager.this, "A csatornaadatok elérhetõek lesznek adatkapcsolat nélkül is!", Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(ChannelManager.this, "A csatornaadatok nem lesznek elérhetõek adatkapcsolat nélkül!", Toast.LENGTH_LONG).show();
			}
			((ChannelAdapter)la).updateItem(index, cb.isChecked());
		}
	};
    
	
	private void getChList(){
    	int result = getChListDB();
    	Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Results from DB: " + result);
    	if (result == 0){
	    	Message msg = handler.obtainMessage();
	    	Bundle b = new Bundle();
	    	b.putBoolean("EmptyList", true);
	    	msg.setData(b);
	    	handler.sendMessage(msg);
    	} else {
    		Message msg = handler.obtainMessage();
	    	Bundle b = new Bundle();
	    	b.putBoolean("chListReady", true);
	    	msg.setData(b);
	    	handler.sendMessage(msg);
    	}
    }

	private int getChListDB() {
		dba = new TVGuideDB(this);
		dba.open();
		Cursor cursor = dba.getChannels();
		int result = 0;
		
		if (cursor.moveToFirst()) {
			chList.clear();
			do {
        		Channel ch = new Channel();
        		ch.name = cursor.getString(cursor.getColumnIndex(Constants.CHTABLE_CHANNEL_NAME));
        		ch.id = cursor.getString(cursor.getColumnIndex(Constants.CHTABLE_CHANNEL_ID));
        		ch.refreshdate = cursor.getLong(cursor.getColumnIndex(Constants.CHTABLE_DATE_NAME));
        		String temp = cursor.getString(cursor.getColumnIndex(Constants.CHTABLE_OFFLINE_MARKER));
        		if (temp.equals(Constants.CHTABLE_OFFLINE_TRUE)) {
        			ch.offline = true;
        		} else {
        			ch.offline = false;
        		}
        		chList.add(ch);
        		//Log.v("CHLIST", ch.toString());
        	} while (cursor.moveToNext());
			result = chList.size();
		}
		cursor.close();
		dba.close();
		Log.d(Constants.LOG_MAIN_TAG + localLogTag, "List size from database: " + result);
		return result;
	}
	
	final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            Bundle b = msg.getData();
            boolean emptyList = b.getBoolean("EmptyList");
            boolean chListReady = b.getBoolean("chListReady");
            if (emptyList) {
    			showDialog(WEBDOWNLOAD_DIALOG);
            }
            if (chListReady) {
            	adapter = new ChannelAdapter(ChannelManager.this, new int[]{R.layout.channel}, new int[]{R.id.channelnametext}, new int[]{R.id.offlinecheck}, chList, checkboxListener);
        		setListAdapter(adapter);
        		
        		ListAdapter listAdapter = getListAdapter();
        		
        		if (listAdapter instanceof ChannelAdapter) {
        			((ChannelAdapter) listAdapter).setChangeListener(changeListener);
        		}
        		
            	Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Generating listview");
            	ListView lv = getListView();
        		
        		if (lv instanceof ChannelListView) {
        			((ChannelListView) lv).setDropListener(dropListener);
        			((ChannelListView) lv).setRemoveListener(removeListener);
        			((ChannelListView) lv).setDragListener(dragListener);
        		}
            }
        }
    };
    
    private void finishChannelManager(){
    	this.finish();
    }
	
	private void downloadChListFromWeb() {
		chList.clear();
		
		//Log.d("CHMANAG","Opening resource: channels");
		
		Scanner scanner = new Scanner(this.getResources().openRawResource(R.raw.channels));
    	int i = 0;
    	while (scanner.hasNextLine()){
    		String channeltext = scanner.nextLine();
    		if (!channeltext.startsWith("#")){
    			Channel channel = new Channel();
        		String[] channelText = channeltext.split("\\|\\|");
        		//Log.i("Channel","Name: " + channelText[0] + " ID: " + channelText[1] + " / Original: " + channeltext);
        		if (channelText.length == 2) {
        			channel.name = channelText[0];
        			channel.id = channelText[1];
        			channel.offline = false;
        			chList.add(channel);
        		}
        		i++;
    		}
    	}
    	
    	if (i > 0) {
    		showDialog(CHANNELSTORE_DIALOG);
    	} else {
    		Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Channel list from WEB is empty!");
    	}
		
	}
	
	protected Dialog onCreateDialog(int id){
		
		Dialog dialog;
		
		switch (id) {
		case WEBDOWNLOAD_DIALOG:
			final CharSequence[] items = {"Frissítés WEB-rõl", "Mégsem"};
	    	
			AlertDialog.Builder dialogTemplate = new AlertDialog.Builder(ChannelManager.this);
			dialogTemplate.setTitle("Nincsenek tárolt csatornák");
			dialogTemplate.setItems(items, new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int item) {
			        if (item == DOWNLOAD_WEB) {
			        	downloadChListFromWeb();
			        }
			        if (item == CANCEL) {
			        	finishChannelManager();
			        }
			    }

			});
			dialog = dialogTemplate.create();
			break;
		case CHANNELSTORE_DIALOG:
			AlertDialog.Builder yesNoDialog = new AlertDialog.Builder(ChannelManager.this);
			yesNoDialog.setMessage("Új csatornalistát találtam. Frissítsem az adatbázist?")
			       .setCancelable(false)
			       .setPositiveButton("Igen", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			                storeChannelList(true);
			           }
			       })
			       .setNegativeButton("Nem", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			                dialog.cancel();
			           }
			       });
			dialog = yesNoDialog.create();
			break;
		default:
			dialog = null;
		}
		
		return dialog;
	}

	private void storeChannelList(boolean refreshList) {
		
		dba = new TVGuideDB(this);
		dba.open();
		
		dba.deleteAllChannels();
		
		final int N = chList.size();
		if ( N > 0) {
			for (int i = 0; i < N; i++){
				dba.insertChannel(chList.get(i));
			}
		}
		
		dba.close();
		
		Toast.makeText(ChannelManager.this, "A csatornák tárolása az adatbázisban megtörtént", Toast.LENGTH_SHORT).show();
		
		Log.d(Constants.LOG_MAIN_TAG + localLogTag, N + " new channels were inserted to database");
		
		if (refreshList) {
			Message msg = handler.obtainMessage();
	    	Bundle b = new Bundle();
	    	b.putBoolean("chListReady", true);
	    	msg.setData(b);
	    	handler.sendMessage(msg);
		}
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	
    	menu.add(GROUP_DEFAULT, MENU_GETWEB, 0, "Frissítés WEB-rõl");
    	menu.add(GROUP_DEFAULT, MENU_SAVE, 0, "Mentés");
    	menu.add(GROUP_DEFAULT, MENU_RELOAD, 0, "Újratöltés adatbázisból");
    	//menu.add(GROUP_DEL, MENU_DEL, 0, "Del");
    	
    	return super.onCreateOptionsMenu(menu);
    }
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item){
    	
    	switch(item.getItemId()) {
    	case MENU_GETWEB:
    		downloadChListFromWeb();
    		return true;
    	case MENU_SAVE:
    		storeChannelList(false);
    		return true;
    	case MENU_RELOAD:
    		getChListDB();
    		Message msg = handler.obtainMessage();
	    	Bundle b = new Bundle();
	    	b.putBoolean("chListReady", true);
	    	msg.setData(b);
	    	handler.sendMessage(msg);
    		return true;
    	}
    	
    	return super.onOptionsItemSelected(item);
    }
}
