package com.kzd76.TVGuide;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;

public class PreferencesScreen extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	
	//private static final String localLogTag = "_Preferences";
	
	private ListPreference listPref;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.preferences);
		
		listPref = (ListPreference)getPreferenceScreen().findPreference("daysToDownload");
		
	}
	
	@Override
	protected void onPause(){
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		listPref.setSummary("Mûsorújság letöltésekor " + listPref.getValue() + " napot tárol az adatbázis.");
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}
	
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		
		//Log.d(Constants.LOG_MAIN_TAG + localLogTag,"Key: " + key);
		
		if (key.equals("daysToDownload")){
			listPref.setSummary("Mûsorújság letöltésekor " + listPref.getValue() + " napot tárol az adatbázis.");
		}
    }
	
}
