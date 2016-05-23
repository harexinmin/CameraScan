package com.android.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SharedPrefsUtil {
	private final static String TAG = "SharedPrefsUtil";
	public final static String SETTING = "DATABASE";
	private static Context mContext;
	public SharedPrefsUtil(Context context){
		mContext = context;
	}
	public static void putValue(String key, int value) {
		 Editor sp =  mContext.getSharedPreferences(SETTING, Context.MODE_PRIVATE).edit();
		 sp.putInt(key, value);
		 sp.commit();
	}
	public static void putValue(String key, boolean value) {
		 Editor sp =  mContext.getSharedPreferences(SETTING, Context.MODE_PRIVATE).edit();
		 sp.putBoolean(key, value);
		 sp.commit();
	}
	public static void putValue(String key, String value) {
		 Editor sp =  mContext.getSharedPreferences(SETTING, Context.MODE_PRIVATE).edit();
		 sp.putString(key, value);
		 sp.commit();
	}
	public static int getValue(String key, int defValue) {
		SharedPreferences sp =  mContext.getSharedPreferences(SETTING, Context.MODE_PRIVATE);
		int value = sp.getInt(key, defValue);
		return value;
	}
	public static boolean getValue(String key, boolean defValue) {
		SharedPreferences sp =  mContext.getSharedPreferences(SETTING, Context.MODE_PRIVATE);
		boolean value = sp.getBoolean(key, defValue);
		return value;
	}
	public static String getValue(String key, String defValue) {
		SharedPreferences sp =  mContext.getSharedPreferences(SETTING, Context.MODE_PRIVATE);
		String value = sp.getString(key, defValue);
		return value;
	}
}

