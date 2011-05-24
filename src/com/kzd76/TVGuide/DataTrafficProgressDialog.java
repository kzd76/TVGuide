package com.kzd76.TVGuide;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DataTrafficProgressDialog extends Dialog{

	public enum TrafficUnit{
		BYTE,
		KILOBYTE,
		MEGABYTE,
		GIGABYTE,
		TERRABYTE;
	}
	
	private Context context;
	
	private ProgressBar progressBar;
	private TextView progressText;
	private TextView statusText;
	private TrafficUnit downloadedUnit = TrafficUnit.KILOBYTE;
	private String unitText;
	
	public DataTrafficProgressDialog(Context context, String title) {
		super(context);
		this.context = context;
		
		this.setTitle(title);

		LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        View layout = inflater.inflate(R.layout.data_traffic_progress_dialog, (ViewGroup) findViewById(R.id.dialog_layout));
        
		this.progressBar = (ProgressBar) layout.findViewById(R.id.dialog_progressbar);
		this.progressText = (TextView) layout.findViewById(R.id.dialog_progresstext);
		this.statusText = (TextView) layout.findViewById(R.id.dialog_statustext);
		
		setContentView(layout);
		
	}

	public void setDownloadedAmount(double downloadedAmount) {
		this.progressText.setText(downloadedAmount + " " + unitText);
	}
	
	public void setStatusText(String statusText){
		this.statusText.setText(statusText);
	}

	public void setDownloadedUnit(TrafficUnit downloadedUnit) {
		this.downloadedUnit = downloadedUnit;
		switch (this.downloadedUnit) {
		case BYTE: 
			unitText = "B";
			break;
		case KILOBYTE: 
			unitText = "kB";
			break;
		case MEGABYTE: 
			unitText = "MB";
			break;
		case GIGABYTE: 
			unitText = "GB";
			break;
		case TERRABYTE: 
			unitText = "TB";
			break;
		default: unitText = "B";
		}
	}

	public void setProgress(int progress){
		this.progressBar.setProgress(progress);
	}
	
	public void setSecondaryProgress(int secondaryProgress){
		this.progressBar.setSecondaryProgress(secondaryProgress);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
	}
}
