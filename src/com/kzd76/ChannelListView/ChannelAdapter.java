package com.kzd76.ChannelListView;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.kzd76.TVGuide.Channel;

public final class ChannelAdapter extends BaseAdapter implements RemoveListener, DropListener {

	private int[] textids;
	private int[] layouts;
	private int[] offlineids;
	private LayoutInflater inflater;
    private ArrayList<Channel> items;
    private OnClickListener checkBoxListener;
    
    ChangeListener changeListener;

    public ChannelAdapter(Context context, ArrayList<Channel> items) {
    	init(context, new int[]{android.R.layout.simple_list_item_1}, new int[]{android.R.id.text1}, new int[]{android.R.id.checkbox}, items, null);
    }
    
    /**
     * ChannelAdapter is an extension of a BaseAdapter also implementing RemoveListener and DropListener functions. 
     * 
     * Parameters:
     * @param context Context for the list (usually "this")
     * @param paramLayouts Array of layouts defined for list rows
     * @param paramTextIds Array of TextViews for channel names
     * @param paramOfflineIds Array of CheckBoxes for channels' offline states
     * @param items ArrayList of Channels - the data input for the list, each list row will contain a Channel object
     * @param checkBoxClickListener Listener object to handle clicking event for checkboxes
     */
    public ChannelAdapter(Context context, int[] paramLayouts, int[] paramTextIds, int[] paramOfflineIds, ArrayList<Channel> items, OnClickListener checkBoxClickListener){
    	init(context, paramLayouts, paramTextIds, paramOfflineIds, items, checkBoxClickListener);
    }
    
    private void init(Context context, int[] paramLayouts, int[] paramTextIds, int[] paramOfflineIds, ArrayList<Channel> listitems, OnClickListener checkBoxClickListener) {
		textids = paramTextIds;
		layouts = paramLayouts;
		offlineids = paramOfflineIds;
		inflater = LayoutInflater.from(context);
		items = listitems;
		checkBoxListener = checkBoxClickListener; 
	}
    
    /**
     * Public method to pass the ChangeListener object to handle changes in the list
     * @param changeListener Listener for list change events  
     */
    
    public void setChangeListener(ChangeListener changeListener) {
		this.changeListener = changeListener;
	}
    
    public int getCount(){
    	return items.size();
    }
    
    public Channel getItem(int position){
    	return items.get(position);
    }
    
    public long getItemId(int position){
    	return position;
    }
    
    static class ViewHolder {
    	TextView text;
    	CheckBox offlinecheck;
    }
    
	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
    	
		ViewHolder holder;
		final Channel ch = items.get(position);
		
		View v = convertView;
    	if (v == null) {
    		
    		v = inflater.inflate(layouts[0], null);
    		holder = new ViewHolder();
    		holder.text = (TextView) v.findViewById(textids[0]);
    		holder.offlinecheck = (CheckBox) v.findViewById(offlineids[0]);
    		holder.offlinecheck.setOnClickListener(checkBoxListener);
    		
    		v.setTag(holder);
    	} else {
    		holder = (ViewHolder) v.getTag();
    	}
    	
    	if (ch != null) {
    		holder.text.setText(ch.name);
    		holder.offlinecheck.setChecked(ch.offline);    		
    	}
    	return v;
    }

	@Override
	public void onDrop(int from, int to) {
		if (from < items.size()) {
			Channel temp = items.get(from);
			items.remove(from);
			items.add(to, temp);
			if (changeListener != null){
				changeListener.onChange();
			}
		}
	}

	@Override
	public void onRemove(int which) {
		if ((which < 0) || (which > items.size())) {
			return;
		}
		items.remove(which);
		if (changeListener != null){
			changeListener.onChange();
		}
	}
	
	public void updateItem(int index, boolean isChecked){
		items.get(index).offline = isChecked;
	}
}