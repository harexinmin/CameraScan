/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.CameraScan;

import android.content.Context;
import android.content.Intent;
import android.graphics.*;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import com.android.util.ImageContainer;
import com.android.util.Util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
/**
 * The activity can crop specific region of interest from an image.
 */
public class CropImageActivity extends MonitoredActivity {
	boolean mSaving = false; // Whether the "ok" button is already clicked.
	private static String TAG_ACTIVITY = "CropImageActivity";
	private static String TAG_VIEW = "CropImageView";
	HighlightView mCrop;
	private CropImageView mImageView;
	private Bitmap mBitmap;
	private final Handler mHandler = new Handler();
	private boolean mCircleCrop = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cropimage);
		mImageView = (CropImageView) findViewById(R.id.image);
		//get the image to be cropped from ImageContainer
		String ori_image_name = getIntent().getStringExtra("image_name");
		mBitmap = ImageContainer.instance().getBitmapFromContainer(ori_image_name);
		// Make UI fullscreen.
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				onSaveClicked();
			}
		});
		startDetection();
	}

	private void startDetection() {
		// set the image to be cropped
		mImageView.setImageBitmapResetBase(mBitmap, true);
		Util.startBackgroundJob(this, null, "请稍候...", new Runnable() {
			public void run() {
				final CountDownLatch latch = new CountDownLatch(1);
				final Bitmap b = mBitmap;
				mHandler.post(new Runnable() {
					public void run() {
						if (b != mBitmap && b != null) {
							mImageView.setImageBitmapResetBase(b, true);
							mBitmap.recycle();
							mBitmap = b;
						}
						if (mImageView.getScale() == 1.0f) {
							mImageView.center(true, true);
						}
						latch.countDown();
					}
				});
				try {
					latch.await();
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
				mRunFaceDetection.run();
			}
		}, mHandler);
	}

	/**
	 * 保存按钮点击后，剪裁图片
	 */
	private void onSaveClicked() {
		// CR: TODO!
		// TODO this code needs to change to use the decode/crop/encode single
		// step api so that we don't require that the whole (possibly large)
		// bitmap doesn't have to be read into memory
		if (mSaving)
			return;

		if (mCrop == null) {
			return;
		}
		mSaving = true;

		Rect r = mCrop.getCropRect();

		int width = r.width(); // CR: final == happy panda!
		int height = r.height();

		// If we are circle cropping, we want alpha channel, which is the
		// third param here.
		Bitmap croppedImage = Bitmap.createBitmap(width, height,
				mCircleCrop ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
		{
			Canvas canvas = new Canvas(croppedImage);
			Rect dstRect = new Rect(0, 0, width, height);
			canvas.drawBitmap(mBitmap, r, dstRect, null);
		}

		if (mCircleCrop) {
			// OK, so what's all this about?
			// Bitmaps are inherently rectangular but we want to return
			// something that's basically a circle. So we fill in the
			// area around the circle with alpha. Note the all important
			// PortDuff.Mode.CLEARes.
			Canvas c = new Canvas(croppedImage);
			Path p = new Path();
			p.addCircle(width / 2F, height / 2F, width / 2F, Path.Direction.CW);
			c.clipPath(p, Region.Op.DIFFERENCE);
			c.drawColor(0x00000000, PorterDuff.Mode.CLEAR);
		}

		String result_image_name = "IMG_" + //new SimpleDateFormat("yyyymmddhhMMssSSS").format(new Date());
		DateFormat.format("yyyyMMdd_hhmmss",
				Calendar.getInstance(Locale.CHINA)).toString() + ".jpg";
        ImageContainer.instance().putBitmap2Container(result_image_name, croppedImage);
        Intent intent = new Intent();
        intent.putExtra("result_image_name", result_image_name);
		Log.v(TAG_ACTIVITY,"onSaveClicked result_image_name = " + result_image_name);
        this.setResult(RESULT_OK,intent);
		this.finish();
	}

	Runnable mRunFaceDetection = new Runnable() {

		Matrix mImageMatrix;

		// Create a default HightlightView if we found no face in the picture.
		private void makeDefault() {
			HighlightView hv = new HighlightView(mImageView);

			int width = mBitmap.getWidth();
			int height = mBitmap.getHeight();

			Rect imageRect = new Rect(0, 0, width, height);

			// CR: sentences!
			// make the default size about 4/5 of the width or height
			int cropWidth = Math.min(width, height) * 4 / 5;
			int cropHeight = cropWidth;

			int x = (width - cropWidth) / 2;
			int y = (height - cropHeight) / 2;

			RectF cropRect = new RectF(x, y, x + cropWidth, y + cropHeight);
			hv.setup(mImageMatrix, imageRect, cropRect, mCircleCrop, false);
			mImageView.add(hv);
		}

		public void run() {
			mImageMatrix = mImageView.getImageMatrix();
			mHandler.post(new Runnable() {
				public void run() {
					makeDefault();
					mImageView.invalidate();
					if (mImageView.mHighlightViews.size() == 1) {
						mCrop = mImageView.mHighlightViews.get(0);
						mCrop.setFocus(true);
					}
				}
			});
		}
	};
}

class CropImageView extends ImageViewTouchBase {
	ArrayList<HighlightView> mHighlightViews = new ArrayList<HighlightView>();
	HighlightView mMotionHighlightView = null;
	float mLastX, mLastY;
	int mMotionEdge;

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (mBitmapDisplayed.getBitmap() != null) {
			for (HighlightView hv : mHighlightViews) {
				hv.mMatrix.set(getImageMatrix());
				hv.invalidate();
				if (hv.mIsFocused) {
					centerBasedOnHighlightView(hv);
				}
			}
		}
	}

	public CropImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void zoomTo(float scale, float centerX, float centerY) {
		super.zoomTo(scale, centerX, centerY);
		for (HighlightView hv : mHighlightViews) {
			hv.mMatrix.set(getImageMatrix());
			hv.invalidate();
		}
	}

	@Override
	protected void zoomIn() {
		super.zoomIn();
		for (HighlightView hv : mHighlightViews) {
			hv.mMatrix.set(getImageMatrix());
			hv.invalidate();
		}
	}

	@Override
	protected void zoomOut() {
		super.zoomOut();
		for (HighlightView hv : mHighlightViews) {
			hv.mMatrix.set(getImageMatrix());
			hv.invalidate();
		}
	}

	@Override
	protected void postTranslate(float deltaX, float deltaY) {
		super.postTranslate(deltaX, deltaY);
		for (int i = 0; i < mHighlightViews.size(); i++) {
			HighlightView hv = mHighlightViews.get(i);
			hv.mMatrix.postTranslate(deltaX, deltaY);
			hv.invalidate();
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		CropImageActivity cropImage = (CropImageActivity) getContext();
		if (cropImage.mSaving) {
			return false;
		}

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN: // CR: inline case blocks.

			for (int i = 0; i < mHighlightViews.size(); i++) { // CR:
																// iterator
																// for; if
																// not, then
																// i++ =>
																// ++i.
				HighlightView hv = mHighlightViews.get(i);
				int edge = hv.getHit(event.getX(), event.getY());
				if (edge != HighlightView.GROW_NONE) {
					mMotionEdge = edge;
					mMotionHighlightView = hv;
					mLastX = event.getX();
					mLastY = event.getY();
					// CR: get rid of the extraneous parens below.
					mMotionHighlightView
							.setMode((edge == HighlightView.MOVE) ? HighlightView.ModifyMode.Move
									: HighlightView.ModifyMode.Grow);
					break;
				}
			}
			break;
		// CR: vertical space before case blocks.
		case MotionEvent.ACTION_UP:
			if (mMotionHighlightView != null) {
				centerBasedOnHighlightView(mMotionHighlightView);
				mMotionHighlightView.setMode(HighlightView.ModifyMode.None);
			}
			mMotionHighlightView = null;
			break;
		case MotionEvent.ACTION_MOVE:
			if (mMotionHighlightView != null) {
				mMotionHighlightView.handleMotion(mMotionEdge, event.getX()
						- mLastX, event.getY() - mLastY);
				mLastX = event.getX();
				mLastY = event.getY();

				if (true) {
					// This section of code is optional. It has some user
					// benefit in that moving the crop rectangle against
					// the edge of the screen causes scrolling but it means
					// that the crop rectangle is no longer fixed under
					// the user's finger.
					ensureVisible(mMotionHighlightView);
				}
			}
			break;
		}

		switch (event.getAction()) {
		case MotionEvent.ACTION_UP:
			center(true, true);
			break;
		case MotionEvent.ACTION_MOVE:
			// if we're not zoomed then there's no point in even allowing
			// the user to move the image around. This call to center puts
			// it back to the normalized location (with false meaning don't
			// animate).
			if (getScale() == 1F) {
				center(true, true);
			}
			break;
		}

		return true;
	}

	// Pan the displayed image to make sure the cropping rectangle is visible.
	private void ensureVisible(HighlightView hv) {
		Rect r = hv.mDrawRect;

		int panDeltaX1 = Math.max(0, getLeft() - r.left);
		int panDeltaX2 = Math.min(0, getRight() - r.right);

		int panDeltaY1 = Math.max(0, getTop() - r.top);
		int panDeltaY2 = Math.min(0, getBottom() - r.bottom);

		int panDeltaX = panDeltaX1 != 0 ? panDeltaX1 : panDeltaX2;
		int panDeltaY = panDeltaY1 != 0 ? panDeltaY1 : panDeltaY2;

		if (panDeltaX != 0 || panDeltaY != 0) {
			panBy(panDeltaX, panDeltaY);
		}
	}

	// If the cropping rectangle's size changed significantly, change the
	// view's center and scale according to the cropping rectangle.
	private void centerBasedOnHighlightView(HighlightView hv) {
		Rect drawRect = hv.mDrawRect;

		float width = drawRect.width();
		float height = drawRect.height();

		float thisWidth = getWidth();
		float thisHeight = getHeight();

		float z1 = thisWidth / width * .6F;
		float z2 = thisHeight / height * .6F;

		float zoom = Math.min(z1, z2);
		zoom = zoom * this.getScale();
		zoom = Math.max(1F, zoom);

		if ((Math.abs(zoom - getScale()) / zoom) > .1) {
			float[] coordinates = new float[] { hv.mCropRect.centerX(),
					hv.mCropRect.centerY() };
			getImageMatrix().mapPoints(coordinates);
			zoomTo(zoom, coordinates[0], coordinates[1], 300F); // CR: 300.0f.
		}

		ensureVisible(hv);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		for (int i = 0; i < mHighlightViews.size(); i++) {
			mHighlightViews.get(i).draw(canvas);
		}
	}

	public void add(HighlightView hv) {
		mHighlightViews.add(hv);
		invalidate();
	}
}