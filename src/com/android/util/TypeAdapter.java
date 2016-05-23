package com.android.util;

import java.util.ArrayList;

import com.android.CameraScan.R;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by sky063486 on 2016/5/16.
 */
public class TypeAdapter extends BaseAdapter {
	private String TAG = "TypeAdapter";
	private Context mContext;
	private ArrayList<String> mlist;
	private int mPos = 0;

	public TypeAdapter(Context context) {
		//Log.v(TAG, "lhy TypeAdapter");
		mContext = context;
		mlist = new ArrayList<String>();
	}

	public ArrayList<String> getList() {
		return mlist;
	}

	public void setList(ArrayList<String> mlist) {
		this.mlist = mlist;
	}

	@Override
	public int getCount() {
		return mlist.size();
	}

	public int getpos() {
		return mPos;
	}

	public void setpos(int pos) {
		Log.v(TAG,"setPos = " + pos);
		this.mPos = pos;
	}

	@Override
	public Object getItem(int i) {
		return null;
	}

	@Override
	public long getItemId(int i) {
		return 0;
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		LinearLayout layout = null;
		Hold mhold = null;
		if (view == null) {
			layout = (LinearLayout) LayoutInflater.from(mContext).inflate(
					R.layout.groupitem, null);
			mhold = new Hold();
			mhold.Type_Name = (TextView) layout.findViewById(R.id.type_name);
			mhold.Type_icon = (ImageView) layout.findViewById(R.id.type_icon);
			layout.setTag(mhold);

		} else {
			layout = (LinearLayout) view;
			mhold = (Hold) layout.getTag();
		}

		Log.v(TAG, "lhy getView name=" + mlist.get(i) + " i = " + i + " pos = " + mPos);
		if (mlist.get(i) != null)
			mhold.Type_Name.setText(mlist.get(i));

		/**
		 * 
		 * 0,名片 1,照片 2,文档 3,白板
		 * 
		 * */
		switch (i) {
		case 0:
			mhold.Type_icon.setImageResource(R.drawable.ic_document_mode);
			mhold.Type_Name.setTextColor(Color.WHITE);
			break;
		case 1:
			mhold.Type_icon.setImageResource(R.drawable.ic_whiteboard_mode);
			mhold.Type_Name.setTextColor(Color.WHITE);
			break;
		case 2:
			mhold.Type_icon.setImageResource(R.drawable.ic_photo_mode);
			mhold.Type_Name.setTextColor(Color.WHITE);
			break;
		case 3:
			mhold.Type_icon.setImageResource(R.drawable.ic_businesscard_mode);
			mhold.Type_Name.setTextColor(Color.WHITE);
			break;
		}
		if(i == mPos) {
			switch (mPos) {
				case 0:
					mhold.Type_icon
							.setImageResource(R.drawable.ic_document_mode_selected);
					mhold.Type_Name.setTextColor(Color.rgb(255, 92, 38)); //Color.RED
					break;
				case 1:
					mhold.Type_icon
							.setImageResource(R.drawable.ic_whiteboard_mode_selected);
					mhold.Type_Name.setTextColor(Color.rgb(255, 92, 38)); //Color.RED
					break;
				case 2:
					mhold.Type_icon.setImageResource(R.drawable.ic_photo_mode_selected);
					mhold.Type_Name.setTextColor(Color.rgb(255, 92, 38)); //Color.RED
					break;
				case 3:
					mhold.Type_icon
							.setImageResource(R.drawable.ic_businesscard_mode_selected);
					mhold.Type_Name.setTextColor(Color.rgb(255, 92, 38)); //Color.RED
					break;
			}
		}
		return layout;
	}

	class Hold {
		ImageView Type_icon;
		TextView Type_Name;

	}

}
