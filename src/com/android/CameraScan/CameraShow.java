package com.android.CameraScan;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;
import com.android.util.ImageContainer;
import com.android.util.TypeAdapter;

import java.io.*;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by sky063486 on 2016/5/14.
 */
public class CameraShow extends Activity implements View.OnClickListener {
    private ImageView mPicView;
    private ImageButton mSaveBtn,mDelBtn,mCutBtn,mPopMenuBtn;
    //private String url=null;
    private String  TAG="CameraShow";
    private Context mContext;
    private PopupWindow popupWindow;
    private View mView;
    private ListView mList;
    private TypeAdapter mAdapter;
    private Bitmap mBitmap;
    private String image_name = null;
    private static int mode = 0;
    private static boolean mImage_cropped = false;
    private static Bitmap cropped_image = null;
    private static int CROPIMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camerapreshow);
        mContext=getApplicationContext();
        // Make UI fullscreen.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        initView();

        mAdapter=new TypeAdapter(mContext);
        initAdapter();
        mList.setAdapter(mAdapter);

        Intent intent=getIntent();
        mode = intent.getIntExtra("mode", 0);
        image_name = intent.getStringExtra("image_name");
        mBitmap = ImageContainer.instance().getBitmapFromContainer(image_name);
        //Uri uri =  intent.getData();
        //url = uri.toString().substring(uri.toString().indexOf("///")+2);
        //Log.v(TAG,"onCreate url = " + url + " mode = " + mode);

        if(image_name != null && mBitmap != null && mPicView!=null){
            Log.v(TAG,"show pic");
            //mPicView.setContentDescription(url);
            mPicView.setImageBitmap(mBitmap);
        }
    }

    /**
     * @param url
     * @return
     */
    public static Bitmap getBitmapByUrl(String url) {
        FileInputStream fis = null;
        Bitmap bitmap = null;

        try {
            fis = new FileInputStream(url);
            bitmap = BitmapFactory.decodeStream(fis);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            bitmap = null;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                fis = null;
            }
        }
        return bitmap;
    }
//
//    public static Bitmap getBitmapByUri(Uri uri,ContentResolver cr){
//        Bitmap bitmap = null;
//        try {
//            bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
//        } catch (FileNotFoundException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//            bitmap = null;
//        }
//        return bitmap;
//    }

    private void setPopBtnImage(int i){
        if (mPopMenuBtn != null){
            Log.v(TAG,"setPopBtnImage pos = " + i);
            switch(i){
                case 0:
                    mPopMenuBtn.setImageResource(R.drawable.ic_document_mode);
                    break;
                case 1:
                    mPopMenuBtn.setImageResource(R.drawable.ic_whiteboard_mode);
                    break;
                case 2:
                    mPopMenuBtn.setImageResource(R.drawable.ic_photo_mode);
                    break;
                case 3:
                    mPopMenuBtn.setImageResource(R.drawable.ic_businesscard_mode);
                    break;
                default:
                    Log.e(TAG,"iPosition Error, use default pic");
                    mPopMenuBtn.setImageResource(R.drawable.ic_document_mode);
                    break;
            }
        }
    }

    private void initView(){
        mPicView=(ImageView)findViewById(R.id.pic_show);
        mSaveBtn=(ImageButton)findViewById(R.id.save_icon);
        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                saveImage();
                finish();
            }
        });
        mCutBtn=(ImageButton)findViewById(R.id.cut_icon);
        if(mCutBtn!=null)mCutBtn.setOnClickListener(this);
        mDelBtn=(ImageButton)findViewById(R.id.delete_icon);
        if(mDelBtn!=null)mDelBtn.setOnClickListener(this);
        mPopMenuBtn=(ImageButton)findViewById(R.id.popmenu_icon);
        if(mPopMenuBtn!=null){
            mPopMenuBtn.setOnClickListener(this);
            setPopBtnImage(mode);
        }
        mView=getLayoutInflater().inflate(R.layout.grouplist, null);
        mList=(ListView)mView.findViewById(R.id.typelist);


}
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(mPopMenuBtn!=null){
            setPopBtnImage(mode);
        }
    }
    private void initAdapter(){
        String [] str_name=getResources().getStringArray(R.array.picture_types);
        Log.v(TAG,"lhy str_name length="+str_name.length);
        mAdapter.getList().clear();
        for(int i=0;i<str_name.length;i++){
            mAdapter.getList().add(str_name[i]);
            //Log.v(TAG,"lhy str_name="+mAdapter.getList().get(i));
        }
        mAdapter.setpos(mode);
        mAdapter.notifyDataSetChanged();
    }
    private void showPopupWindow()
    {    initAdapter();
        if (popupWindow==null)
            popupWindow = new PopupWindow(mView,250, 370);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_bgbar));
        //popupWindow.setBackgroundDrawable(new ColorDrawable(Color.argb(50, 52, 53, 55)));
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);

        popupWindow.showAsDropDown(mPopMenuBtn, -115,15);//set the position of popwindow, x,y;

        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String content=mAdapter.getList().get(i);
                Log.v(TAG,"content="+content);
                setPopBtnImage(i);
                mode = i;
                dismissPopupWindow();
            }
        });
    }

    public void dismissPopupWindow() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
    }

    public  boolean deleteFile(String fileName) {
//        File file = new File(fileName);
//        if (file.exists() && file.isFile()) {
//            if (file.delete()) {
//                   Log.v(TAG,"delete "+fileName+"success!");
//                return true;
//            } else {
//
//                return false;
//            }
//        } else {
//
//            return false;
//        }
        ImageContainer.instance().removeBitmapFromContainer(fileName);
        return true;
    }

    public void CropImage(String name){
        //store the image to ImageContainer for CropImageActivity to crop
        //Bitmap bt = getBitmapByUrl(url);
        //ImageContainer.instance().putBitmap2Container("image_name",bt);
        Intent intent = new Intent();
        intent.putExtra("image_name", name);
        intent.setClass(this, CropImageActivity.class);
        startActivityForResult(intent, CROPIMAGE);
    }

    /**
     * save the cropped image
     * @param image
     */
    private void saveOutput(Bitmap image) {
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "MyCameraApp");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.e("MyCameraApp", "failed to create directory");
            }
        }
        String name = DateFormat.format("yyyyMMdd_hhmmss",
                Calendar.getInstance(Locale.CHINA)).toString();
        String saved_name = null;
        if(mImage_cropped)
            saved_name = "IMG_" + name + "_crop.jpg";
        else
            saved_name = "IMG_" + name + ".jpg";
        File file = new File(mediaStorageDir.getPath() + File.separator
                    + saved_name);

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        OutputStream outStream;
        try {
            outStream = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            outStream.flush();
            outStream.close();
            Toast.makeText(mContext, "bitmap saved to " + file.toString(), Toast.LENGTH_LONG).show();
            Log.i(TAG, "bitmap saved to sd path:" + file.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveImage(){
        if(mImage_cropped){
            //save image being cropped
            saveOutput(cropped_image);
        }else{
            //save the  original image
            saveOutput(mBitmap);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CROPIMAGE && resultCode == RESULT_OK) {
            String result_image_name = data.getStringExtra("result_image_name");
            cropped_image = ImageContainer.instance().getBitmapFromContainer(result_image_name);
            Log.v(TAG,"onActivityResult OK, set mPicView result_image_name = " + result_image_name);
            //set mImage_cropped true, so we can know that user have cropped this image
            //and we can add "_crop" to the image name when it being saved
            mImage_cropped = true;
            //set preView image to the one after being cropped
            mPicView.setImageBitmap(cropped_image);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        mImage_cropped = false;

        super.onPause();
    }

    @Override
    public void onClick(View view) {
        int id=view.getId();
        switch (id){
            case R.id.delete_icon:
                deleteFile(image_name);
                this.finish();
                break;
            case R.id.cut_icon:
                CropImage(image_name);
                break;
            case R.id.popmenu_icon:
                showPopupWindow();
                break;
//            case R.id.save_icon:
//                Log.v(TAG,"save_icon pressed !");
//                saveImage();
//                this.finish();
//                break;
            default:
                break;
        }

    }
}