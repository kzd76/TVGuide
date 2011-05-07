package com.kzd76.TVGuide;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {
	
	final static String localLogTag = "_Alarm";
	
	 @Override
	 public void onReceive(Context context, Intent intent) {
		try {
			Bundle bundle = intent.getExtras();
			String message = bundle.getString("alarm_message");
			Log.d(Constants.LOG_MAIN_TAG + localLogTag,"Alarm request received: " + message);
			Toast.makeText(context, message, Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			Toast.makeText(context, "There was an error somewhere, but we still received an alarm", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
	    }
	 }
}