package pr.platerecognization;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.eastime.distinguish.R;

import org.opencv.android.OpenCVLoader;

import java.util.List;

import pr.ActivityManager;
import pr.Constant;
import pr.SharedUtil;
import pr.vinrecognization.utils.CameraSetting;


/**
 * 扫描车牌
 *
 * @author by hs-johnny
 * Created on 2019/6/17
 */
public class CameraActivity extends Activity {
    private static final String TAG = "CameraActivity";
    RelativeLayout rl_content;
    ImageView iv_back;
    ImageView iv_flash;
    FrameLayout previewFl;
    CameraPreviews cameraPreview;

    PlateFinderView plateFinderView;

    LinearLayout input_ll;//输入
    TextView input_tv;
    TextView create_tv;//创建
    LinearLayout car_plate_ll;
    ImageView car_plate_img;
    TextView car_plate_tv;
    LinearLayout vin_code_ll;
    ImageView vin_code_img;
    TextView vin_code_tv;
    LinearLayout qr_code_ll;

//    PlateInfo plateResult;
    public static boolean isOne;
    AlertDialog alertDialog;
    private boolean lightIsOn = false;
    private boolean isOneAct = false;

    private boolean isPlate = true;
    private boolean isVin = true;
    private boolean isCapture = true;
    private boolean isCreate = true;
    private int plateNum ;
    private double confidence;
    List<PlateInfo> plateResult;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            List<PlateInfo> plates = (List<PlateInfo>) msg.obj;
            if (plates != null && plates.size()!=0) {
                plateResult = plates;
                stopPreview();
                if (lightIsOn) {
                    lightIsOn = false;
                    iv_flash.setImageResource(R.mipmap.ic_ocr_flashlight_off);
                }
                sureResult();
            }
        }
    };

    static {
        if (OpenCVLoader.initDebug()) {
            Log.d("Opencv", "opencv load_success");
        } else {
            Log.d("Opencv", "opencv can't load opencv .");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_camera);
        Intent intent = getIntent();
        isOne = intent.getBooleanExtra("isOne", false);
        isOneAct = intent.getBooleanExtra(Constant.EXTRA_ONEACTIVITY_CODE, false);
        isPlate = intent.getBooleanExtra(Constant.EXTRA_IS_PLATE_CODE, true);
        isVin = intent.getBooleanExtra(Constant.EXTRA_IS_VIN_CODE, true);
        isCapture = intent.getBooleanExtra(Constant.EXTRA_IS_CAPTURE_CODE, true);
        isCreate = intent.getBooleanExtra(Constant.EXTRA_IS_CREATE_CODE, true);
        plateNum = intent.getIntExtra(Constant.EXTRA_PLATE_NUM_CODE, Constant.PLATE_NUM);
        confidence =intent.getDoubleExtra(Constant.EXTRA_CONFIDENCE_CODE, Constant.CONFIDENCE);
        initView();
        addCameraView(this);
        initClick();

        if (DeepAssetUtil.handle == 0) {
            DeepAssetUtil.handle = DeepAssetUtil.initRecognizer(this);
        }
    }

    private void initView() {
        rl_content = findViewById(R.id.rl_content_plate);
        iv_back = findViewById(R.id.iv_back);
        iv_flash = findViewById(R.id.iv_flash);
        previewFl = findViewById(R.id.preview_fl);

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
        plateFinderView = new PlateFinderView(this);
        car_plate_img.setImageResource(R.mipmap.icon_car_plate);
        car_plate_tv.setTextColor(getResources().getColor(R.color.color_app));
        input_tv.setText("手动输入车牌号");
        if (!isPlate)
            car_plate_ll.setVisibility(View.GONE);
        if (!isVin)
            vin_code_ll.setVisibility(View.GONE);
        if (!isCapture)
            qr_code_ll.setVisibility(View.GONE);
        if (!isCreate)
            create_tv.setVisibility(View.GONE);
    }


    private void initClick() {
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        input_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chengeActivity(1);
            }
        });
        qr_code_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chengeActivity(5);
            }
        });
        vin_code_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chengeActivity(4);
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
        ActivityManager.getInstance().resultActivity(CameraActivity.this, type, isOneAct, isPlate, isVin, isCapture, isCreate,plateNum,confidence);
    }

    public void addCameraView(Context context) {
        int hasPermission = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            hasPermission = this.checkSelfPermission(Manifest.permission.CAMERA);
            if (hasPermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        110);
                return;
            }
        }

        cameraPreview = new CameraPreviews(this,  plateNum,  confidence, handler);
//        cameraPreview.setWillNotDraw(false);
        previewFl.addView(cameraPreview);
        previewFl.addView(plateFinderView);

        iv_flash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!lightIsOn) {
                    lightIsOn = true;
                    CameraSetting.getInstance(CameraActivity.this).openCameraFlash(cameraPreview.mCamera);
                    iv_flash.setImageResource(R.mipmap.ic_ocr_flashlight_on);
                } else {
                    lightIsOn = false;
                    CameraSetting.getInstance(CameraActivity.this).closedCameraFlash(cameraPreview.mCamera);
                    iv_flash.setImageResource(R.mipmap.ic_ocr_flashlight_off);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (cameraPreview == null) {
            addCameraView(this);
            initClick();
            if (alertDialog != null && alertDialog.isShowing()) {
                alertDialog.dismiss();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        lightIsOn = false;
        iv_flash.setImageResource(R.mipmap.ic_ocr_flashlight_off);
        previewFl.removeAllViews();
        cameraPreview = null;
    }

    private void stopPreview() {
        previewFl.removeAllViews();
    }

    private void showResult(PlateInfo plate) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("识别结果");
        builder.setMessage("车牌号：" + plate.plateName + "\n置信度：" + plate.confidence);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sureResult();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("重新识别", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                againRecognition();
                dialog.dismiss();
            }
        });
        alertDialog = builder.create();
        alertDialog.show();
    }

    private void sureResult() {
        if (plateResult != null) {
            Intent intent = new Intent();
            intent.putExtra(Constant.EXTRA_TYPE_CODE, 2);
            String value = JSON.toJSONString(plateResult);
//            intent.putExtra(Constant.EXTRA_RESPOND_CODE, value);
            SharedUtil.getInstance(this).put(Constant.EXTRA_RESPOND_CODE, value);
            setResult(Activity.RESULT_OK, intent);
            finish();
        } else {
            Toast.makeText(CameraActivity.this, "还未识别到车牌", Toast.LENGTH_SHORT).show();
        }
    }

    private void againRecognition() {
        if (previewFl.getChildCount() == 0) {
            addCameraView(CameraActivity.this);
        } else {
            Toast.makeText(CameraActivity.this, "正在识别...", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 110) {
            if (grantResults.length > 0) {
                for (int grantResult : grantResults) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        addCameraView(this);
                    }
                }
                //其他逻辑(这里当权限都同意的话就执行打电话逻辑)
//                addCameraView(this);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        setResult(Activity.RESULT_OK, data);
        finish();
    }
}
