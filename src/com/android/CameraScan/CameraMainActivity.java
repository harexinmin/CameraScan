package com.android.CameraScan;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.android.util.*;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CameraMainActivity extends Activity implements
		SurfaceHolder.Callback, View.OnClickListener,
		android.hardware.Camera.PictureCallback {
	/**
	 * Called when the activity is first created.
	 */
	private static final String TAG = "CameraMainActivity";
	private static int MEDIA_TYPE_IMAGE = -1;
	private SurfaceView mSurfaceView;
	private ImageButton mFlashBtn, mCameraBtn, mPopBtn;
	private SurfaceHolder mSurfaceHolder;
	private Camera mCamera;
	private Context mContext;
	private PopupWindow popupWindow;
	private View mView;
	private ListView mList;
	private ImageView autoImage;
	private TypeAdapter mAdapter;
	private int surfacewidth;
	private boolean auto = false;
	private Camera.Parameters parameters;
	private SharedPrefsUtil sp = null;
	private Size suitableSize = null;
	private Bitmap mBitmap = null;

	public String [] Cammode = {
			"DOCUMENT", "WHITEBOARD", "PHOTO", "BUSNIESSCARD"
	};

	private static final String mLastMode = "LastMode";					//XML file name
	private static final String mode = "mode";							//XML key name
	private static int iPosition = 0;									//user selected focused position
	/*
	 * 前后置摄像头id* 默认为后置
	 */
	private int cameraPosition = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v(TAG,"onCreate");
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.cameramain);
		mContext = getApplicationContext();
		// Make UI fullscreen.
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		initView();
		initShareper();

		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.addCallback(this);

		if (this.checkCameraHardware() && (mCamera == null)) {
			mCamera = getCamera();
			try{
			mCamera.setPreviewDisplay(mSurfaceHolder);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			auto = false;
		}
		autoImage.setBackgroundResource(R.drawable.ic_camfocuswhite);
	}

	private void initShareper() {
		sp = new SharedPrefsUtil(mContext);
		String lastmode = sp.getValue(mode, null);
		Log.v(TAG,"initShareper lastmode = " + lastmode);
		if(lastmode == null){
			Log.v(TAG,"lastmode = null ,create one xml");
			sp.putValue(mode, Cammode[0]);
			iPosition = 0;										//Default mode
		}else if(lastmode.equalsIgnoreCase(Cammode[0])){
			iPosition = 0;										//DOCUMENT
		}else if(lastmode.equalsIgnoreCase(Cammode[1])){
			iPosition = 1;										//WHITEBOARD
		}else if(lastmode.equalsIgnoreCase(Cammode[2])){
			iPosition = 2;										//PHOTO
		}else if(lastmode.equalsIgnoreCase(Cammode[3])){
			iPosition = 3;										//BUSNIESSCARD
		}
		Log.v(TAG,"iPosition = " + iPosition);

	}

	private boolean checkCameraHardware() {
		if (mContext.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA)) {
			return true;
		}
		return false;
	}

	private void setDisplayOrientation() {
		int displayOrientation = getCorrectOrientation();

		mCamera.setDisplayOrientation(displayOrientation);
	}

	/**
	 * 我们用4比3的比例设置预览图片
	 */
	private void setPreviewSize() {
		parameters = mCamera.getParameters();
		List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
//		for (Camera.Size size : sizes) {
//			Log.d(TAG, "previewSize width:" + size.width + " height "
//					+ size.height);
//		}
		surfacewidth = mSurfaceView.getWidth();
		suitableSize = MyCamPara.getInstance().getPreviewSize(sizes,
				surfacewidth);
		parameters.setPreviewSize(suitableSize.width, suitableSize.height);
		//Log.d(TAG, "previewSize SET width:" + suitableSize.width + " height "
		//		+ suitableSize.height);
		mCamera.setParameters(parameters);

	}

	private void initView() {
		mCameraBtn = (ImageButton) findViewById(R.id.camera_take);
		if (mCameraBtn != null)
			mCameraBtn.setOnClickListener(this);
		mFlashBtn = (ImageButton) findViewById(R.id.flash);
		if (mFlashBtn != null)
			mFlashBtn.setOnClickListener(this);
		mPopBtn = (ImageButton) findViewById(R.id.popmenu);
		if (mPopBtn != null){
			mPopBtn.setOnClickListener(this);
			setPopBtnImage(iPosition);
		}
		mSurfaceView = (SurfaceView) findViewById(R.id.capture_preview);
		mView = getLayoutInflater().inflate(R.layout.grouplist, null);
		mList = (ListView) mView.findViewById(R.id.typelist);
		autoImage = (ImageView) findViewById(R.id.autofoucs);
	}

	/**
	 * 让预览跟照片符合正确的方向。<br/>
	 * 因为预览默认是横向的。如果是一个竖向的应用，就需要把预览转90度<br/>
	 * 比如横着时1280*960的尺寸时，1280是宽.<br/>
	 * 竖着的时候1280就是高了<br/>
	 * 这段代码来自官方API。意思就是让拍出照片的方向和预览方向正确的符合设备当前的方向（有可能是竖向的也可能使横向的）
	 * 
	 */
	private int getCorrectOrientation() {
		android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
		android.hardware.Camera.getCameraInfo(cameraPosition, info);
		int rotation = this.getWindowManager().getDefaultDisplay()
				.getRotation();
		int degrees = 0;
		switch (rotation) {
		case Surface.ROTATION_0:
			degrees = 0;
			break;
		case Surface.ROTATION_90:
			degrees = 90;
			break;
		case Surface.ROTATION_180:
			degrees = 180;
			break;
		case Surface.ROTATION_270:
			degrees = 270;
			break;
		}

		int result;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + degrees) % 360;
			result = (360 - result) % 360; // compensate the mirror
		} else { // back-facing
			result = (info.orientation - degrees + 360) % 360;
		}
		Log.d(TAG, "orientationResult = " + result);
		return result;
	}

	private void releaseCamera() {
		if (mCamera != null) {
			Log.v(TAG,"releaseCamera");
			mCamera.setPreviewCallback(null);
			mCamera.stopPreview();//stop camera preview
			mCamera.release();
			mCamera = null;
		}
	}

	@Override
	protected void onResume() {
		Log.v(TAG,"onResume");

		mAdapter = new TypeAdapter(mContext);
		initAdapter();
		mList.setAdapter(mAdapter);

		setPopBtnImage(iPosition);

		//clear Container when onResume, so the max size of ImageContainer is 2
		//ImageContainer.instance().printSize();
		ImageContainer.instance().clear();

		super.onResume();
	}

	@Override
	protected void onPause() {
		Log.v(TAG,"onPause");
		dismissPopupWindow();					//dismiss popwindow
		super.onPause();
	}

	private void initAdapter() {
		String[] str_name = getResources()
				.getStringArray(R.array.picture_types);
		//Log.v(TAG, "lhy str_name length=" + str_name.length);
		mAdapter.getList().clear();
		for (int i = 0; i < str_name.length; i++) {
			mAdapter.getList().add(str_name[i]);
			//Log.v(TAG, "lhy str_name=" + mAdapter.getList().get(i));
		}
		mAdapter.setpos(iPosition);
		mAdapter.notifyDataSetChanged();
	}

	private Camera getCamera() {
		Camera camera = null;
		try {
			camera = Camera.open();
		} catch (Exception e) {
			// Camera is not available (in use or does not exist)
			camera = null;
			Log.e(TAG, "Camera is not available (in use or does not exist)");
		}
		return camera;
	}

	@Override
	protected void onDestroy() {
		Log.v(TAG,"onDestroy");
		super.onDestroy();
	}

	@Override
	public void surfaceCreated(SurfaceHolder surfaceHolder) {
		mSurfaceHolder = surfaceHolder;
		Log.v(TAG,"surfaceCreated");
		try {
			if(mCamera == null) {
				mCamera = getCamera();
			}
			setDisplayOrientation();
			mCamera.setPreviewDisplay(mSurfaceHolder);
		} catch (IOException exception) {
			releaseCamera();
			// TODO: add more exception handling logic here
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1,
			int i2) {
		setPreviewSize();
		mCamera.startPreview();
		Log.v(TAG,"surfaceChanged");
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
		releaseCamera();
		mSurfaceHolder = null;
		Log.v(TAG,"surfaceDestroyed");

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// 屏幕触摸事件
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			// 按下时自动对焦
			mCamera.autoFocus(new AutoFocusCallback() {
				@Override
				public void onAutoFocus(boolean success, Camera arg1) {
					// TODO Auto-generated method stub
					if (success) {
						auto = true;
						autoImage
								.setBackgroundResource(R.drawable.ic_camfocusgreen);
					}
				}
			});

		}
		// if (event.getAction() == MotionEvent.ACTION_UP && auto == true) {
		// //放开后拍照
		// mCamera.takePicture(null, null, pictureCallBack);
		// auto =false;
		// }
		return true;
	}

//	private void OpenLightOn() {
//		if (null == mCamera) {
//			mCamera = Camera.open();
//		}
//
//		parameters = mCamera.getParameters();
//		parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
//		mCamera.setParameters(parameters);
//		mCamera.autoFocus(new Camera.AutoFocusCallback() {
//			public void onAutoFocus(boolean success, Camera camera) {
//			}
//		});
//		mCamera.startPreview();
//	}
//
//	private void CloseLightOff() {
//		if (mCamera != null) {
//			mCamera = Camera.open();
//			parameters = mCamera.getParameters();
//			parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
//			mCamera.setParameters(parameters);
//
//			mCamera.stopPreview();
//		}
//	}

	@Override
	public void onClick(View view) {
		int id = view.getId();
		switch (id) {
		case R.id.flash:
			if (null == mCamera) {
				mCamera = Camera.open();
			}

			parameters = mCamera.getParameters();
			String mFlashMode = parameters.getFlashMode();
			Log.v(TAG, "onClick camera FlashMode:" + mFlashMode);
			if (mFlashMode.equalsIgnoreCase(Camera.Parameters.FLASH_MODE_OFF)) {
				parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
				mFlashBtn.setImageResource(R.drawable.ic_action_flash_on);
				mCamera.setParameters(parameters);
				mCamera.startPreview();
			} else if (mFlashMode
					.equalsIgnoreCase(Camera.Parameters.FLASH_MODE_ON)) {
				parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
				mFlashBtn
						.setImageResource(R.drawable.ic_action_flash_automatic);
				mCamera.setParameters(parameters);
				mCamera.startPreview();
			} else if (mFlashMode
					.equalsIgnoreCase(Camera.Parameters.FLASH_MODE_AUTO)) {
				parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
				mFlashBtn.setImageResource(R.drawable.ic_action_flash_off);
				mCamera.setParameters(parameters);
				mCamera.stopPreview();
			}

			break;
		case R.id.popmenu:
			showPopupWindow();
			break;
		case R.id.camera_take:
			parameters = mCamera.getParameters();
			parameters.setPictureFormat(ImageFormat.JPEG);
			// 自动对焦
			// params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
			mCamera.setParameters(parameters);
			mCamera.takePicture(null, null, this);
			break;
		}

	}

//	private static File getOutputMediaFile(int type) {
//		// To be safe, you should check that the SDCard is mounted
//		// using Environment.getExternalStorageState() before doing this.
//		File mediaStorageDir = new File(
//				Environment
//						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
//				"MyCameraApp");
//		// This location works best if you want the created images to be shared
//		// between applications and persist after your app has been uninstalled.
//		// Create the storage directory if it does not exist
//		if (!mediaStorageDir.exists()) {
//			if (!mediaStorageDir.mkdirs()) {
//				Log.d("MyCameraApp", "failed to create directory");
//				return null;
//			}
//		}
//		// Create a media file name
//		// String timeStamp = new Date().toLocaleString() ;
//		File mediaFile;
//		if (type == MEDIA_TYPE_IMAGE) {
//			//new DateFormat();
//			String name = DateFormat.format("yyyyMMdd_hhmmss",
//					Calendar.getInstance(Locale.CHINA)).toString();
//			mediaFile = new File(mediaStorageDir.getPath() + File.separator
//					+ "IMG_" + name + ".jpg");
//		} else {
//			return null;
//		}
//		return mediaFile;
//	}

	@Override
	public void onPictureTaken(byte[] data, android.hardware.Camera camera) {

		if (data == null) {
			return;
		}

		BitmapFactory.Options  options = new BitmapFactory.Options();
		options.inJustDecodeBounds = false;
		options.outWidth = (int)(suitableSize.width * 0.5f);
		options.outWidth = (int)(suitableSize.height * 0.5f);
		mBitmap = BitmapFactory.decodeByteArray(data, 0, data.length,options);
		mBitmap = Util.rotate(mBitmap, 90);

		String image_name ="IMG_" + DateFormat.format("yyyyMMdd_hhmmss",
				Calendar.getInstance(Locale.CHINA)).toString() + ".jpg";
		ImageContainer.instance().putBitmap2Container(image_name, mBitmap);
		Intent intent = new Intent();
		intent.setClass(this, CameraShow.class);
		intent.putExtra("image_name", image_name);
		intent.putExtra("mode", iPosition);			//show the right icon in CameraShow Activity
		startActivity(intent);




//		File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
//		if (pictureFile == null) {
//			Log.d(TAG,
//			"Error creating media file, check storage permissions: ");
//			return;
//		}
//		try {
//			FileOutputStream fos = new FileOutputStream(pictureFile);
//			fos.write(data);
//			fos.close();
//			Log.v(TAG, "picture has saved to " + pictureFile.getAbsolutePath());
//			Intent intent = new Intent();
//			intent.setClass(this, CameraShow.class);
//			intent.setData(Uri.fromFile(pictureFile));
//			intent.putExtra("mode", iPosition);			//show the right icon in CameraShow Activity
//			startActivity(intent);
//		} catch (FileNotFoundException e) {
//			Log.d(TAG, "File not found: " + e.getMessage());
//		} catch (IOException e) {
//			Log.d(TAG, "Error accessing file: " + e.getMessage());
//		}
	}

	private void showPopupWindow() {
		initAdapter();
		if (popupWindow == null)
			popupWindow = new PopupWindow(mView, 250, 390);
		popupWindow.setFocusable(true);
		popupWindow.setOutsideTouchable(true);
		popupWindow.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.ic_bgbar));
		// popupWindow.setBackgroundDrawable(new ColorDrawable(Color.argb(50,
		// 52, 53, 55)));
		popupWindow.setOutsideTouchable(true);
		popupWindow.setFocusable(true);

		popupWindow.showAsDropDown(mPopBtn, -90, 15);// set the position of
		// popwindow, x,y;

		mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view,
									int i, long l) {
				String content = mAdapter.getList().get(i);
				Log.v(TAG, "content = " + content);
				mAdapter.setpos(i);
				mAdapter.notifyDataSetChanged();

				sp.putValue(mode, Cammode[i]);
				Log.v(TAG, "popwindow onClick mode = " + Cammode[i] + " i = " + i);

				setPopBtnImage(i);
				iPosition = i;                            //change iPosition value
				dismissPopupWindow();
			}
		});
	}

	/**
	 * set popwindow icon image according user selected mode
	 */
	private void setPopBtnImage(int i){
		if (mPopBtn != null){
			Log.v(TAG,"setPopBtnImage pos = " + i);
			switch(i){
				case 0:
					mPopBtn.setImageResource(R.drawable.ic_document_mode);
					break;
				case 1:
					mPopBtn.setImageResource(R.drawable.ic_whiteboard_mode);
					break;
				case 2:
					mPopBtn.setImageResource(R.drawable.ic_photo_mode);
					break;
				case 3:
					mPopBtn.setImageResource(R.drawable.ic_businesscard_mode);
					break;
				default:
					Log.e(TAG,"iPosition Error, use default pic");
					mPopBtn.setImageResource(R.drawable.ic_document_mode);
					break;
			}
		}
	}

	public void dismissPopupWindow() {
		if (popupWindow != null && popupWindow.isShowing()) {
			popupWindow.dismiss();
		}
	}

}
