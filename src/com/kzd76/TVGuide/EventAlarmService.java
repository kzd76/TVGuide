package com.kzd76.TVGuide;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

public class EventAlarmService extends Service{
	
	private static final String localLogTag = "_AlarmService";
	
	NotificationManager mNM;
	
	private static final int notificationID = 0;
	private static String eventTitle;
	private static String eventDescription;

	@Override
	public IBinder onBind(Intent intent) {
		Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Alarm service binded.");
		
		return mBinder;
	}
	
	@Override
	public void onCreate(){
		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		
		Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Alarm service created.");
	}
	
	private void showNotification() {
		
		Notification notification = new Notification(R.drawable.icon, "TV mûsor értesítés", System.currentTimeMillis());
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		notification.setLatestEventInfo(this, eventTitle, eventDescription, contentIntent);
		mNM.notify(notificationID, notification);
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		//mNM.cancel(notificationID);
		Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Alarm service destroyed.");
	}
	
	@Override
	public void onStart(Intent intent, int startId){
		super.onStart(intent, startId);
		
		Bundle params = intent.getExtras();
		eventTitle = params.getString("EventTitle");
		eventDescription = params.getString("EventDescription");
		
		showNotification();
		
		Thread thread = new Thread(null, mTask, "EventAlarmService");
		thread.start();
		
		
		Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Alarm service started.");
	}
	
	@Override
	public boolean onUnbind(Intent intent){
		Log.d(Constants.LOG_MAIN_TAG + localLogTag, "Alarm service unbinded.");
		return super.onUnbind(intent);
	}
	
	Runnable mTask = new Runnable() {
		
		@Override
		public void run() {
			long endTime = System.currentTimeMillis() + 15 * 1000;
			while (System.currentTimeMillis() < endTime) {
				synchronized (mBinder) {
					try {
						mBinder.wait(endTime - System.currentTimeMillis());
					} catch (Exception e) {
						
					}
				}
			}
			EventAlarmService.this.stopSelf();
		}
	};
	
	
	private final IBinder mBinder = new Binder(){
		@Override
		protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException{
			return super.onTransact(code, data, reply, flags);
		}
	};
}
