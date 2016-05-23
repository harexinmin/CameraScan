package com.android.util;

import android.graphics.Bitmap;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import android.util.Log;

public class ImageContainer {
	private Map<String, SoftReference<Bitmap>> localImageCache = new HashMap<String, SoftReference<Bitmap>>();
	private static ImageContainer mImageContainer = null;

	static public ImageContainer instance() {
		if (null == mImageContainer) {
			mImageContainer = new ImageContainer();
		}

		return mImageContainer;
	}

	public void clear(){
		localImageCache.clear();
	}

	//debug related API: to avoid localImageCache.size() increasing
	public void printSize(){
		Log.v("ImageContainer", "Container size = " + localImageCache.size());
	}
	
	public void putBitmap2Container(String key,Bitmap bmp) {
		if (key == null || bmp == null) {
			return;
		}

		localImageCache.put(key,new SoftReference<Bitmap>(bmp));
	}
	
	public Bitmap getBitmapFromContainer(String key) {
		if (key == null) {
			return null;
		}
		
		SoftReference<Bitmap> softReference = localImageCache.get(key);
		return softReference.get();
	} 

	public void removeBitmapFromContainer(String key) {
		localImageCache.remove(key);
		return;
	}
}
