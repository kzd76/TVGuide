/*
 * This code is inspired by DragNDropListView object composed by Eric Harlow
 * 
 * http://ericharlow.blogspot.com/2010/10/experience-android-drag-and-drop-list.html
 * 
 */

package com.kzd76.ChannelListView;

import android.view.View;
import android.widget.ListView;

/**
 * Implement to handle an item being dragged.
 */
public interface DragListener {
	/**
	 * Called when a drag starts.
	 * @param itemView - the view of the item to be dragged i.e. the drag view
	 */
	void onStartDrag(View itemView, int x, int y);
	
	/**
	 * Called when a drag is to be performed.
	 * @param x - horizontal coordinate of MotionEvent.
	 * @param y - verital coordinate of MotionEvent.
	 * @param listView - the listView
	 */
	void onDrag(int x, int y, ListView listView);
	
	/**
	 * Called when a drag stops.
	 * Any changes in onStartDrag need to be undone here 
	 * so that the view can be used in the list again.
	 * @param itemView - the view of the item to be dragged i.e. the drag view
	 */
	void onStopDrag(View itemView);
	
	/**
	 * Called after a drag starts and drag view is initialized.
	 * @param itemView - the view of the item to be dragged i.e. the drag view
	 */
	void afterStartDrag(View itemView);
}
