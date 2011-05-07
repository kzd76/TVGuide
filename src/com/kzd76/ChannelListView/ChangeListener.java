package com.kzd76.ChannelListView;

/**
 * Implement to handle list changes.
 * An adapter handling the underlying data 
 * will most likely handle this interface.
 *  
 * @author Barna Szternák
 */
public interface ChangeListener {
	
	/**
	 * Called when the list changed.
	 */
	void onChange();
}