/*
 /*
 * This code is inspired by DragNDropListView object composed by Eric Harlow
 * 
 * http://ericharlow.blogspot.com/2010/10/experience-android-drag-and-drop-list.html
 * 
 */

package com.kzd76.ChannelListView;

/**
 * Implement to handle removing items.
 * An adapter handling the underlying data 
 * will most likely handle this interface.
 */
public interface RemoveListener {
	
	/**
	 * Called when an item is to be removed
	 * @param which - indicates which item to remove.
	 */
	void onRemove(int which);
}
