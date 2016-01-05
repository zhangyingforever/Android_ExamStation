package com.example.tellyes.examstationshow;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class PictureActivity extends AppCompatActivity {
    static final String TAG =  "CAMERA ACTIVITY";

    //Camera object
    Camera mCamera;
    //Preview surface
    SurfaceView surfaceView;
    //Preview surface handle for callback
    SurfaceHolder surfaceHolder;
    //Camera button
    Button btnCapture;
    Button btnCancel;
    //Note if preview windows is on.
    boolean previewing;
    File imageFile;
    int mCurrentCamIndex = 0;
    Bitmap bm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);
        //拍照按钮，传回对应的文件路径
        btnCapture = (Button) findViewById(R.id.btn_capture);
        btnCapture.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View arg0) {
                if (previewing) {
                    mCamera.takePicture(shutterCallback, rawPictureCallback,
                            jpegPictureCallback);


                }
            }
        });
        //取消按钮
        btnCancel= (Button) findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(new Button.OnClickListener()
        {
            public void onClick(View arg0) {
                setResult(RESULT_CANCELED); //这理有2个参数(int resultCode, Intent intent)
                finish();
            }
        });
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView1);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(new SurfaceViewCallback());
        //surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }
    Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        @Override
        public void onShutter() {
        }
    };

    Camera.PictureCallback rawPictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] arg0, Camera arg1) {

        }
    };

    Camera.PictureCallback jpegPictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] arg0, Camera arg1) {

            String fileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                    .toString()+ "/CameraImage"
                    + File.separator
                    + "UserImage.jpg";
            imageFile = new File(fileName);
            if (!imageFile.getParentFile().exists()) {
                imageFile.getParentFile().mkdir();
            }

            try {
                bm=BitmapFactory.decodeByteArray(arg0, 0, arg0.length);
                Bitmap mBitmap = Bitmap.createBitmap(bm,200,50,900,900);
                FileOutputStream fos = new FileOutputStream(imageFile);
                mBitmap.compress(Bitmap.CompressFormat.JPEG, 50, fos);
                //BufferedOutputStream bos = new BufferedOutputStream(
                       // new FileOutputStream(imageFile));
                //bos.write(arg0);


                fos.flush();
                fos.close();
                bm.recycle();
                mBitmap.recycle();
                scanFileToPhotoAlbum(imageFile.getAbsolutePath());
                //以source为原图，创建新的图片，指定起始坐标以及新图像的高宽。

               // Toast.makeText(PictureActivity.this, "[Test] Photo take and store in" + imageFile.toString(), Toast.LENGTH_LONG).show();

                Intent intent=new Intent();
                if(imageFile!=null) {
                    intent.putExtra("imagePath", imageFile.getPath());
                }
                else
                {
                    intent.putExtra("imagePath", "");
                }
                    /* 将数据打包到aintent Bundle 的过程略 */
                setResult(RESULT_OK, intent); //这理有2个参数(int resultCode, Intent intent)
                finish();
            } catch (Exception e) {
                //Toast.makeText(PictureActivity.this, "Picture Failed" + e.toString(),
                       // Toast.LENGTH_LONG).show();
            }
        };
    };
    public void scanFileToPhotoAlbum(String path) {

        MediaScannerConnection.scanFile(PictureActivity.this,
                new String[]{path}, null,
                new MediaScannerConnection.OnScanCompletedListener() {

                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("TAG", "Finished scanning " + path);
                    }
                });
    }
    private final class SurfaceViewCallback implements android.view.SurfaceHolder.Callback {
        public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3)
        {
            if (previewing) {
                mCamera.stopPreview();
                previewing = false;
            }

            try {
                mCamera.setPreviewDisplay(arg0);
                mCamera.startPreview();
                previewing = true;
                //setCameraDisplayOrientation(PictureActivity.this, mCurrentCamIndex, mCamera);
            } catch (Exception e) {}
        }
        public void surfaceCreated(SurfaceHolder holder) {
//				mCamera = Camera.open();
            //change to front camera
            mCamera = openFrontFacingCameraGingerbread();
            // get Camera parameters
            Camera.Parameters params = mCamera.getParameters();
            //params.setPreviewSize(500, 700);
            //myParameters.setFocusMode("auto");

            //params.setPictureSize(500, 700);
            List<String> focusModes = params.getSupportedFocusModes();
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                // Autofocus mode is supported
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            previewing = false;
        }
    }

    private Camera openFrontFacingCameraGingerbread() {
        int cameraCount = 0;
        Camera cam = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();

        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                try {
                    cam = Camera.open(camIdx);
                    mCurrentCamIndex = camIdx;
                } catch (RuntimeException e) {
                    Log.e(TAG, "Camera failed to open: " + e.getLocalizedMessage());
                }
            }
        }

        return cam;
    }

    //Starting from API level 14, this method can be called when preview is active.
    private static void setCameraDisplayOrientation(Activity activity,int cameraId, Camera camera)
    {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();

        //degrees  the angle that the picture will be rotated clockwise. Valid values are 0, 90, 180, and 270.
        //The starting position is 0 (landscape).
        int degrees = 0;
        switch (rotation)
        {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
        {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        }
        else
        {
            // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

}
