package pr.vinrecognization;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.eastime.distinguish.R;
import com.kernal.smartvisionocr.utils.KernalLSCXMLInformation;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import pr.ActivityManager;
import pr.Constant;
import pr.ImageUtils;
import pr.SharedUtil;
import pr.vinrecognization.utils.CameraParametersUtils;
import pr.vinrecognization.utils.CameraSetting;
import pr.vinrecognization.utils.RecogOpera;
import pr.vinrecognization.utils.VINRecogParameter;
import pr.vinrecognization.utils.VINRecogResult;
import pr.vinrecognization.view.VinFinderView;

public class VinOcrActivity extends Activity implements SurfaceHolder.Callback {
    SurfaceView surface_view;
    ImageView iv_back;
    ImageView iv_flash;
    RelativeLayout rl_content;

    LinearLayout input_ll;
    TextView input_tv;
    TextView create_tv;
    LinearLayout car_plate_ll;
    ImageView car_plate_img;
    TextView car_plate_tv;
    LinearLayout vin_code_ll;
    ImageView vin_code_img;
    TextView vin_code_tv;
    LinearLayout qr_code_ll;

    private int srcWidth = 0;
    private int srcHeight = 0;
    private SurfaceHolder surfaceHolder;
    private Camera camera;
    private ArrayList<Integer> srcList = new ArrayList<Integer>();// 拍照分辨率集合
    private int selectedTemplateTypePosition = 0;
    //    private Vibrator mVibrator;
    private int rotation = 0;// 屏幕取景方向
    private CameraParametersUtils cameraParametersUtils;
    private VinFinderView myVinFinderView;
    private KernalLSCXMLInformation wlci;
    private boolean isRecogSuccess = false;
    private Camera.Size size;
    private boolean isFirstIn = true;
    private boolean islandscape = false;// 是否为横向
    private boolean isSetZoom = false;
    private RecogOpera mRecogOpera = new RecogOpera(this);
    private boolean isOneAct = false;

    private boolean isPlate = true;
    private boolean isVin = true;
    private boolean isCapture = true;
    private boolean isCreate = true;
    private int plateNum;
    private double confidence;
    Handler mAutoFocusHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 100) {
                CameraSetting.getInstance(VinOcrActivity.this).autoFocus(camera);
                this.sendEmptyMessageDelayed(100, 2500);
            }
        }
    };
    public int sum = 0;
    private ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1, 1, 3,
            TimeUnit.MILLISECONDS, new ArrayBlockingQueue(1));

    private VINRecogResult vinRecogResult;
    private VINRecogParameter vinRecogParameter = new VINRecogParameter();
    int number;

    Camera.PreviewCallback callback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(final byte[] bytes, final Camera camera) {
            number++;
            if (number % 3 != 0) {
                return;
            }
            if (isRecogSuccess) {
                return;
            }
            Log.d("iTH_InitSmartVisionSDK", mRecogOpera.iTH_InitSmartVisionSDK + "");
            if (mRecogOpera.iTH_InitSmartVisionSDK == 0 && sum == 0) {
                threadPoolExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        sum = sum + 1;
                        vinRecogParameter.data = bytes;
                        vinRecogParameter.islandscape = islandscape;//是否横屏
                        vinRecogParameter.rotation = rotation;//屏幕旋转角度
                        vinRecogParameter.selectedTemplateTypePosition = selectedTemplateTypePosition;//模板位置
                        vinRecogParameter.wlci = wlci;//配置文件解析内容
                        vinRecogParameter.size = size;//相机的预览分辨率
                        vinRecogResult = mRecogOpera.startOcr(vinRecogParameter);
//                        Log.d("1111111","result:"+vinRecogResult.result);
                        if (vinRecogResult != null && vinRecogResult.result != null) {
                            String recogResult = vinRecogResult.result;//Vin识别结果
//                    ZhugeTrack(mContext).put("来源", "车架号流识别-SDK").track("OCR-车架号识别成功")
                            if (recogResult != null && "" != recogResult) {
                                isRecogSuccess = true;
                                Bitmap bmp = saveBase64Img(bytes, camera);
                                Intent intent = new Intent();
                                intent.putExtra(Constant.EXTRA_VIN_CODE, recogResult);
                                intent.putExtra(Constant.EXTRA_TYPE_CODE, 4);
//                                intent.putExtra(Constant.EXTRA_IMGPATH_CODE,  new ImageUtils().savePicture(bmp).getPath());
                                SharedUtil.getInstance(VinOcrActivity.this).put(Constant.EXTRA_BASE64_CODE, ImageUtils.bitmapToBase64(bmp));
                                setResult(Activity.RESULT_OK, intent);
//                                Message msg = new Message();
//                                msg.obj = bmp;
//                                handler.sendMessage(msg);
                                finish();
                            }

                        } else {
//                    ZhugeTrack(mContext).put("来源", "车架号流识别-SDK").track("OCR-车架号识别失败")
                        }
                        sum -= 1;
                    }
                });
            }
        }
    };

    private int uiRot;
    private boolean lightIsOn = false;

    Runnable initCameraParams = new Runnable() {
        @Override
        public void run() {
            if (camera != null) {
                CameraSetting.getInstance(VinOcrActivity.this)
                        .setCameraParameters(callback,
                                surfaceHolder, VinOcrActivity.this, camera,
                                srcHeight / srcWidth, srcList, false,
                                rotation, isSetZoom);

                //				}
                size = camera.getParameters().getPreviewSize();

                lightIsOn = false;
                CameraSetting.getInstance(VinOcrActivity.this).closedCameraFlash(camera);
                iv_flash.setImageResource(R.mipmap.ic_ocr_flashlight_off);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_vin_ocr);
        Intent intent = getIntent();
        isOneAct = intent.getBooleanExtra(Constant.EXTRA_ONEACTIVITY_CODE, false);
        isPlate = intent.getBooleanExtra(Constant.EXTRA_IS_PLATE_CODE, true);
        isVin = intent.getBooleanExtra(Constant.EXTRA_IS_VIN_CODE, true);
        isCapture = intent.getBooleanExtra(Constant.EXTRA_IS_CAPTURE_CODE, true);
        isCreate = intent.getBooleanExtra(Constant.EXTRA_IS_CREATE_CODE, true);
        plateNum = intent.getIntExtra(Constant.EXTRA_PLATE_NUM_CODE, Constant.PLATE_NUM);
        confidence = intent.getDoubleExtra(Constant.EXTRA_CONFIDENCE_CODE, Constant.CONFIDENCE);
        initView();
        cameraParametersUtils = new CameraParametersUtils(this);
        uiRot = getWindowManager().getDefaultDisplay().getRotation();// 获取屏幕旋转的角度
        getPhoneSizeAndRotation();
//        mRecogOpera = new RecogOpera(this);
        mRecogOpera.initOcr();
        wlci = mRecogOpera.getWlci_Portrait();
        ClickEvent();
        AddView();
        surfaceHolder = surface_view.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    // 获取设备分辨率 不受虚拟按键影O响
    public void getPhoneSizeAndRotation() {
        cameraParametersUtils.setScreenSize(this);
        srcWidth = cameraParametersUtils.srcWidth;
        srcHeight = cameraParametersUtils.srcHeight;
    }

    @Override
    protected void onStart() {
        super.onStart();
        vinRecogParameter.isFirstProgram = true;
    }

    public void ClickEvent() {
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        iv_flash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!lightIsOn) {
                    lightIsOn = true;
                    CameraSetting.getInstance(VinOcrActivity.this).openCameraFlash(camera);
                    iv_flash.setImageResource(R.mipmap.ic_ocr_flashlight_on);
                } else {
                    lightIsOn = false;
                    CameraSetting.getInstance(VinOcrActivity.this).closedCameraFlash(camera);
                    iv_flash.setImageResource(R.mipmap.ic_ocr_flashlight_off);
                }
            }
        });
        input_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chengeActivity(6);
            }
        });
        car_plate_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chengeActivity(2);
            }
        });
        qr_code_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chengeActivity(5);
            }
        });
        create_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chengeActivity(3);
            }
        });
    }

    private void chengeActivity(int type) {
        ActivityManager.getInstance().resultActivity(VinOcrActivity.this, type, isOneAct, isPlate, isVin, isCapture, isCreate, plateNum, confidence);
    }


    public void RemoveView() {
        if (myVinFinderView != null) {
            myVinFinderView.destroyDrawingCache();
            rl_content.removeView(myVinFinderView);
            myVinFinderView = null;
        }
    }

    public void AddView() {
        myVinFinderView = new VinFinderView(
                VinOcrActivity.this, wlci,
                wlci.template.get(selectedTemplateTypePosition).templateType);
        rl_content.addView(myVinFinderView, 1);
    }

    public void initView() {
        rl_content = findViewById(R.id.rl_content);
        surface_view = findViewById(R.id.surface_view);
        iv_back = findViewById(R.id.iv_back);
        iv_flash = findViewById(R.id.iv_flash);
        input_ll = findViewById(R.id.input_ll);
        input_tv = findViewById(R.id.input_tv);
        create_tv = findViewById(R.id.create_tv);
        car_plate_ll = findViewById(R.id.car_plate_ll);
        car_plate_img = findViewById(R.id.car_plate_img);
        car_plate_tv = findViewById(R.id.car_plate_tv);
        vin_code_ll = findViewById(R.id.vin_code_ll);
        vin_code_img = findViewById(R.id.vin_code_img);
        vin_code_tv = findViewById(R.id.vin_code_tv);
        qr_code_ll = findViewById(R.id.qr_code_ll);
        vin_code_img.setImageResource(R.mipmap.icon_vin);
        vin_code_tv.setTextColor(getResources().getColor(R.color.color_app));
        input_tv.setText("手动输入VIN码");
        if (!isPlate)
            car_plate_ll.setVisibility(View.GONE);
        if (!isVin)
            vin_code_ll.setVisibility(View.GONE);
        if (!isCapture)
            qr_code_ll.setVisibility(View.GONE);
        if (!isCreate)
            create_tv.setVisibility(View.GONE);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        this.runOnUiThread(initCameraParams);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Message msg = new Message();
        msg.what = 100;
        mAutoFocusHandler.sendMessage(msg);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    // 小米PAD 解锁屏时执行surfaceChanged surfaceCreated，容易出现超时卡死现象，故在此处打开相机和设置参数
    @Override
    public void onResume() {
        super.onResume();
        if (wlci.fieldType.get(wlci.template.get(selectedTemplateTypePosition).templateType)
                .get(Constant.fieldsPosition).ocrId == "SV_ID_YYZZ_MOBILEPHONE") {
            isSetZoom = true;
        } else {
            isSetZoom = false;
        }
        OpenCameraAndSetParameters();

    }

    @Override
    protected void onPause() {
        super.onPause();
        CloseCameraAndStopTimer(0);
        lightIsOn = false;
        iv_flash.setImageResource(R.mipmap.ic_ocr_flashlight_off);
    }


    @Override
    protected void onDestroy() {
        threadPoolExecutor.shutdown();
        mRecogOpera.freeKernalOpera(this);
        mAutoFocusHandler.removeMessages(100);
        if (myVinFinderView != null) {
            rl_content.removeView(myVinFinderView);
            myVinFinderView.destroyDrawingCache();
            myVinFinderView = null;
        }
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            backLastActivtiy();
            return true;
        }
        return true;
    }

    public void backLastActivtiy() {
        isRecogSuccess = true;
        CloseCameraAndStopTimer(0);
        this.finish();
    }

    public void OpenCameraAndSetParameters() {
        try {
            if (null == camera) {
                camera = CameraSetting.getInstance(this).open(0,
                        camera);

                rotation = CameraSetting.getInstance(this)
                        .setCameraDisplayOrientation(uiRot);
                if (!isFirstIn) {
                    CameraSetting.getInstance(this)
                            .setCameraParameters(callback,
                                    surfaceHolder, VinOcrActivity.this,
                                    camera, srcHeight / srcWidth,
                                    srcList, false, rotation, isSetZoom);
                }
                isFirstIn = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void CloseCameraAndStopTimer(int type) {
        if (camera != null) {
            if (type == 1) {
                camera.setPreviewCallback(null);
                camera.stopPreview();
            } else {
                camera = CameraSetting.getInstance(this)
                        .closeCamera(camera);
            }
            Log.d("CloseCameraAndStopTimer", "CloseCameraAndStopTimer" + (camera == null));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        setResult(Activity.RESULT_OK, data);
        finish();
    }

    private Bitmap saveBase64Img(byte[] data, Camera camera) {
        Camera.Size previewSize = camera.getParameters().getPreviewSize();
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = true;
        YuvImage yuvimage = new YuvImage(data, ImageFormat.NV21, previewSize.width, previewSize.height, null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Rect rectImg = new Rect(0, 0, previewSize.width / 2, previewSize.height / 2);

        if (myVinFinderView != null && myVinFinderView.frame != null) {
            Rect frame = myVinFinderView.frame;
            int mWidth = myVinFinderView.mWidth;
            int mHeight = myVinFinderView.mHeight;

            rectImg = new Rect(frame.top * previewSize.width / mHeight, frame.left * previewSize.height / mWidth,
                    frame.bottom * previewSize.width / mHeight, frame.right * previewSize.height / mWidth);
        }
        yuvimage.compressToJpeg(rectImg, 100, baos);

        byte[] rawImage = baos.toByteArray();
        //将rawImage转换成bitmap
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap = BitmapFactory.decodeByteArray(rawImage, 0, rawImage.length, options);
        return ImageUtils.rotateBitmap(bitmap);
    }
}
