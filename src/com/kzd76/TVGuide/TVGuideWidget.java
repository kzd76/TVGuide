package com.kzd76.TVGuide;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

public class TVGuideWidget extends AppWidgetProvider {
	
	public static final String ACTION_UPDATE = "com.kzd76.UPDATE_TVGUIDE_WIDGET";
	private static final String localLogTag = "_Widget";
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds){
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		
		//Loop through all TVGuide widgets to display an update
		
		for (int appWidgetId : appWidgetIds){
			updateAppWidget(context, appWidgetManager, appWidgetId);
		}
	}
	
	@Override
	public void onReceive(Context context, Intent intent){
		super.onReceive(context, intent);
		if (intent.getAction().equals(ACTION_UPDATE)){
			Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Custom update action has been received.");
			
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
			
			RemoteViews view = updateRemoteView(context);
			
			ComponentName thisAppWidget = new ComponentName(context, getClass());
			try {
				appWidgetManager.updateAppWidget(thisAppWidget, view);
			} catch (Exception e) {
				Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Error while calling widget updater. " + e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	private void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
		
		Intent intent = new Intent(context, AllCurrentOfflineEventsScreen.class);
		PendingIntent pi = PendingIntent.getActivity(context, 0, intent, 0);
		
		RemoteViews view = updateRemoteView(context);
		
		view.setOnClickPendingIntent(R.id.widgetinfoimage, pi);
		
		appWidgetManager.updateAppWidget(appWidgetId, view);
	}
	
	private RemoteViews updateRemoteView(Context context){
		TVGuideDB dba = new TVGuideDB(context.getApplicationContext());
		dba.open();
		
		Calendar cal = Calendar.getInstance();
		DateFormat df = new SimpleDateFormat("yyyy.MM.dd");
		DateFormat tf = new SimpleDateFormat("HH:mm");
		Date now = cal.getTime();
		
		String eventDay = df.format(now);
		String currentTime = tf.format(now);
		
		String[] result = dba.getCurrentOfflineEvents(3, eventDay, currentTime);
		dba.close();
		
		Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Widget is being updated. Results from database: " + result.length);
		
		RemoteViews view = new RemoteViews(context.getPackageName(), R.layout.tvguide_widget);
		
		if (result.length == 3) {
			if (result[0] != null) {
				Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Result[0]: " + result[0]);
				String[] temp = result[0].split("@@");
				view.setTextViewText(R.id.widgettopchannel, temp[0]);
				view.setViewVisibility(R.id.widgettopchannel, View.VISIBLE);
				view.setTextViewText(R.id.widgettoptext, temp[1] + " " + temp[2]);
				view.setViewVisibility(R.id.widgettoptext, View.VISIBLE);
			}  else {
				view.setViewVisibility(R.id.widgettopchannel, View.INVISIBLE);
				view.setViewVisibility(R.id.widgettoptext, View.INVISIBLE);
			}
			if (result[1] != null) {
				Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Result[1]: " + result[1]);
				String[] temp = result[1].split("@@");
				view.setTextViewText(R.id.widgetmiddlechannel, temp[0]);
				view.setViewVisibility(R.id.widgetmiddlechannel, View.VISIBLE);
				view.setTextViewText(R.id.widgetmiddletext, temp[1] + " " + temp[2]);
				view.setViewVisibility(R.id.widgetmiddletext, View.VISIBLE);
			}  else {
				view.setViewVisibility(R.id.widgetmiddlechannel, View.INVISIBLE);
				view.setViewVisibility(R.id.widgetmiddletext, View.INVISIBLE);
			}
			if (result[2] != null) {
				Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Result[2]: " + result[2]);
				String[] temp = result[2].split("@@");
				view.setTextViewText(R.id.widgetbottomchannel, temp[0]);
				view.setViewVisibility(R.id.widgetbottomchannel, View.VISIBLE);
				view.setTextViewText(R.id.widgetbottomtext, temp[1] + " " + temp[2]);
				view.setViewVisibility(R.id.widgetbottomtext, View.VISIBLE);
			} else {
				view.setViewVisibility(R.id.widgetbottomchannel, View.INVISIBLE);
				view.setViewVisibility(R.id.widgetbottomtext, View.INVISIBLE);
			}
			
			if ((result[0] == null) && (result[1] == null) && (result[2] == null)) {
				Log.d(Constants.LOG_MAIN_TAG + localLogTag, "No valid result set from database. Length is: " + result.length);
				view.setViewVisibility(R.id.widgettoptext, View.INVISIBLE);
				view.setViewVisibility(R.id.widgetbottomtext, View.INVISIBLE);
				view.setViewVisibility(R.id.widgetmiddletext, View.VISIBLE);
				view.setTextViewText(R.id.widgetmiddletext, "Nincs elérhetõ mûsorújság!");
			}
			
		} else {
			Log.d(Constants.LOG_MAIN_TAG + localLogTag, "No valid result set from database. Length is: " + result.length);
			view.setViewVisibility(R.id.widgettoptext, View.INVISIBLE);
			view.setViewVisibility(R.id.widgetbottomtext, View.INVISIBLE);
			view.setViewVisibility(R.id.widgetmiddletext, View.VISIBLE);
			view.setTextViewText(R.id.widgetmiddletext, "Nincs elérhetõ mûsorújság!");
		}
		
		return view;
	}
	
}
