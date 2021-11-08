package pr.platerecognization;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pr.ImageUtils;

/**
 * @author by hs-johnny
 * Created on 2019/6/17
 */
public class CameraPreviews extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {

    private static final String TAG = "CameraPreview";
    public Camera mCamera;
    private SurfaceHolder mHolder;
    public long handle;
    private byte[] lock = new byte[0];
    private List<String> mResultList = new ArrayList<>();
    private String currentPlate = "";
    private Paint mPaint;
    private float oldDist = 1f;
    private int w;//切图的宽高
    private int h;

    int mWidth;//屏幕的宽高
    int mHeight;
    /**
     * 停止识别
     */
    private boolean isStopReg;
    private Context context;

    //    private String str = "将车牌号置入框内";
    Camera.Size previewSize;
    Handler handler;

    int frameNum;
    private int plateNum;
    private double confidence;
    private int discernNum;
    private List<PlateInfo> plates = new ArrayList<>();
    int left, top, right, bottom;


    public CameraPreviews(Context context, int plateNum, double confidence, Handler handler) {
        super(context);
        this.handler = handler;
        mHolder = getHolder();
        mHolder.addCallback(this);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStrokeWidth(2);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(0xFF4081);
        this.context = context;
        this.plateNum = plateNum;
        this.confidence = confidence;

        DisplayMetrics dm = getResources().getDisplayMetrics();
        mWidth = dm.widthPixels;
        mHeight = dm.heightPixels;
        w = mWidth / 8 * 3;
        h = mHeight / 10;
    }

    public Camera getCameraInstance() {
        if (mCamera == null) {
            try {
                CameraHandlerThread mThread = new CameraHandlerThread("camera thread");
                synchronized (mThread) {
                    mThread.openCamera();
                }
            } catch (Exception e) {
                Log.e(TAG, "camera is not available");
            }
        }
        return mCamera;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mCamera = getCameraInstance();
        previewSize = mCamera.getParameters().getPreviewSize();
        left = (mHeight / 3 - h) * previewSize.width / mHeight;
        top = (mWidth / 2 - w) * previewSize.height / mWidth;
        right = (mHeight / 3 + h) * previewSize.width / mHeight;
        bottom = (mWidth / 2 + w) * previewSize.height / mWidth;
        mCamera.setPreviewCallback(this);
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
            setPreviewFocus(mCamera);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        int rotation = getDisplayOrientation();
        mCamera.setDisplayOrientation(rotation);
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setRotation(rotation);
        mCamera.setParameters(parameters);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mHolder.removeCallback(this);
        mCamera.setPreviewCallback(null);
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    public int getDisplayOrientation() {
        Display display = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int rotation = display.getRotation();
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
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, info);
        int result = (info.orientation - degrees + 360) % 360;
        return result;
    }

    @Override
    public void onPreviewFrame(final byte[] data, final Camera camera) {
        synchronized (lock) {
            //处理data
            frameNum++;
            if (frameNum % 2 != 0) {
                return;
            }
            BitmapFactory.Options newOpts = new BitmapFactory.Options();
            newOpts.inJustDecodeBounds = true;
            YuvImage yuvimage = new YuvImage(data, ImageFormat.NV21, previewSize.width, previewSize.height, null);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            yuvimage.compressToJpeg(new Rect(left, top, right, bottom), 100, baos);

//            yuvimage.compressToJpeg(new Rect(0, 0,
//                    previewSize.width , previewSize.height ), 100, baos);
            byte[] rawImage = baos.toByteArray();
            //将rawImage转换成bitmap
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            Bitmap bitmap = BitmapFactory.decodeByteArray(rawImage, 0, rawImage.length, options);
            Bitmap bmp = ImageUtils.rotateBitmap(bitmap);
            PlateInfo result = DeepAssetUtil.simpleRecog(bmp, 8);
            if (!isStopReg && result != null && !TextUtils.isEmpty(result.plateName) && DeepAssetUtil.PlateMatcher(result.plateName)) {
                if (result.confidence < confidence) {
                    bitmap.recycle();
                    return;
                }
//                result.imagePath = new ImageUtils().savePicture(new ImageUtils().compressImage(bmp)).getPath();
//                result.base64 = ImageUtils.bitmapToBase64(bmp);
                result.bitmap=null;
                plates.add(result);
                discernNum++;
                if (discernNum >= plateNum) {
                    isStopReg = true;
                    sendPlate(plates);
                }
            }
            bitmap.recycle();
        }
    }

    private void sendPlate(List<PlateInfo> plates) {
        if (handler != null) {
            Message msg = new Message();
            msg.obj = plates;
            handler.sendMessage(msg);
        }
//        EventBus.getDefault().post(plate);
    }

    private void openCameraOriginal() {
        try {
            mCamera = Camera.open();
        } catch (Exception e) {
            Log.e(TAG, "camera is not available");
        }
    }

    private class CameraHandlerThread extends HandlerThread {
        Handler handler;

        public CameraHandlerThread(String name) {
            super(name);
            start();
            handler = new Handler(getLooper());
        }

        synchronized void notifyCameraOpened() {
            notify();
        }

        void openCamera() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    openCameraOriginal();
                    notifyCameraOpened();
                }
            });
            try {
                wait();
            } catch (Exception e) {
                Log.e(TAG, "wait was interrupted");
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getPointerCount() == 2) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_POINTER_DOWN:
                    oldDist = getFingerSpacing(event);
                    break;
                case MotionEvent.ACTION_MOVE:
                    float newDist = getFingerSpacing(event);
                    if (newDist > oldDist) {
                        handleZoom(true, mCamera);
                    } else if (newDist < oldDist) {
                        handleZoom(false, mCamera);
                    }
                    oldDist = newDist;
                    break;
            }
        }
        return true;
    }

    private float getFingerSpacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private void handleZoom(boolean isZoomIn, Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        if (parameters.isZoomSupported()) {
            int maxZoom = parameters.getMaxZoom();
            int zoom = parameters.getZoom();
            if (isZoomIn && zoom < maxZoom) {
                zoom++;
            } else if (zoom > 0) {
                zoom--;
            }
            parameters.setZoom(zoom);
            camera.setParameters(parameters);
        } else {
            Log.e(TAG, "handleZoom: " + "the device is not support zoom");
        }
    }

    private void setPreviewFocus(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        List<String> focusList = parameters.getSupportedFocusModes();
        if (focusList.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        camera.setParameters(parameters);
    }


    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        Paint paint = new Paint();
//        paint.setAlpha(100);
//        paint.setTextSize(40);
//        //上
//        paint.setColor(0x99000000);
//        canvas.drawRect(0, 0, mWidth, mHeight / 3 - h, paint);
//        //下
//        canvas.drawRect(0, mHeight / 3 + h, mWidth, mHeight, paint);
//        //左
//        canvas.drawRect(0, mHeight / 3 - h, mWidth / 2 - w, mHeight / 3 + h, paint);
//        //右
//        canvas.drawRect(mWidth / 2 + w, mHeight / 3 - h, mWidth, mHeight / 3 + h, paint);
//
//        paint.setColor(0xFFFFFFFF);
//        canvas.drawText(str, mWidth / 2 - 35 * str.length() / 2, mHeight / 3 - w + dip2px(context, 20), paint);

    }
}
