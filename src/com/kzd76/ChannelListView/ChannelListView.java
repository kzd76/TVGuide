/*
 * This code is inspired by DragNDropListView object composed by Eric Harlow
 * 
 * http://ericharlow.blogspot.com/2010/10/experience-android-drag-and-drop-list.html
 * 
 */

package com.kzd76.ChannelListView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;

public class ChannelListView extends ListView {

	boolean dragMode;
	
	int startPos;
	int endPos;
	int offset;
	
	int hx;
	int hy;
	
	ImageView dragView;
	
	DropListener dropListener;
	RemoveListener removeListener;
	DragListener dragListener;
	
	public ChannelListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setDropListener(DropListener dropListener) {
		this.dropListener = dropListener;
	}

	public void setRemoveListener(RemoveListener removeListener) {
		this.removeListener = removeListener;
	}

	public void setDragListener(DragListener dragListener) {
		this.dragListener = dragListener;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event){
		
		final int action = event.getAction();
		final int x = (int) event.getX();
		final int y = (int) event.getY();
		
		if ((action == MotionEvent.ACTION_DOWN) && (x < this.getWidth() / 4)){
			dragMode = true;
		}
		
		if (!dragMode) {
			return super.onTouchEvent(event);
		}
		
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			startPos = pointToPosition(x, y);
			if (startPos != INVALID_POSITION) {
				int itemPos = startPos - getFirstVisiblePosition();
				offset = y - getChildAt(itemPos).getTop();
				offset = offset - ((int)event.getRawY()) - y;
				startDrag(itemPos, x, y);
				drag(x, y);
			}
			break;
		case MotionEvent.ACTION_MOVE:
			drag(x,y);
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			if (dragView != null) {              	
	        	int deltaX = (int)Math.abs(hx-x);
	        	int deltaY = (int)Math.abs(hy - y);
	        	if ((deltaX > 5*dragView.getWidth()/8) && (deltaY < dragView.getHeight())) {
	        		removeListener.onRemove(startPos);
	            	stopDrag(startPos - getFirstVisiblePosition());
	        	}
	        }
		default:
			dragMode = false;
			endPos = pointToPosition(x, y);
			stopDrag(startPos - getFirstVisiblePosition());
			if ((dropListener != null) && (startPos != INVALID_POSITION) && (endPos != INVALID_POSITION)) {
				dropListener.onDrop(startPos, endPos);
			}
			break;
		}
		return true;
	}

	private void stopDrag(int itemIndex) {
		if (dragView != null) {
			if (dragListener != null) {
				dragListener.onStopDrag(getChildAt(itemIndex));
			}
			dragView.setVisibility(GONE);
			WindowManager wm = (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
			wm.removeView(dragView);
			dragView.setImageDrawable(null);
			dragView = null;
		}
	}

	private void drag(int x, int y) {
		if (dragView != null) {
			WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) dragView.getLayoutParams();
			layoutParams.x = x;
			layoutParams.y = y;
			WindowManager wm = (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
			wm.updateViewLayout(dragView, layoutParams);
			
			if (dragListener != null) {
				dragListener.onDrag(x, y, this);
			}
		}
	}

	private void startDrag(int itemIndex, int x, int y) {
		stopDrag(itemIndex);
		
		hx = x;
		hy = y;
		
		View item = getChildAt(itemIndex);
		if (item == null) {
			return;
		}
		item.setDrawingCacheEnabled(true);
		if (dragListener != null) {
			dragListener.onStartDrag(item, hx, hy);
		}
		
		Bitmap bm = Bitmap.createBitmap(item.getDrawingCache());
		WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
		layoutParams.gravity = Gravity.TOP;
		layoutParams.x = 0;
		layoutParams.y = y - offset;
		layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
		layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE 
			| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE 
			| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON 
			| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
			| WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
		layoutParams.format = PixelFormat.TRANSLUCENT;
		layoutParams.windowAnimations = 0;
		
		layoutParams.alpha = 0.85f;
		layoutParams.dimAmount = 0.2f;
		
		Context context = getContext();
		ImageView iv = new ImageView(context);
		iv.setImageBitmap(bm);
		
		WindowManager wm = (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
		wm.addView(iv, layoutParams);
		dragView = iv;
		
		if (dragListener != null) {
			dragListener.afterStartDrag(item);
		}
	}
	
}
