package com.kzd76.TVGuide;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class YesNoDialog extends Activity {
	
	private boolean cancellable = false;
	private String[] extra;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		Bundle bundle = getIntent().getExtras();
		
		String title = bundle.getString("Title");
		String text = bundle.getString("Text");
		this.cancellable = bundle.getBoolean("Cancellable");
		this.extra = bundle.getStringArray("Extra");
		
		this.setTitle(title);
		
		setContentView(R.layout.yesnodialog);
		
		Button yesButton = (Button) findViewById(R.id.yesnodialog_buttonYes);
		Button noButton = (Button) findViewById(R.id.yesnodialog_buttonNo);
		
		yesButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.putExtra("Extra", extra);
				setResult(RESULT_OK, intent);
				finish();
			}
		});
		
		noButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});
		
		TextView textView = (TextView) findViewById(R.id.yesnodialog_text);
		textView.setText(text);
	}
	
	@Override
	public void onBackPressed(){
		if (cancellable){
			super.onBackPressed();
		}
	}
	
}
