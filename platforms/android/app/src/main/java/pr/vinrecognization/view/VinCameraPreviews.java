package pr.vinrecognization.view;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.kernal.smartvisionocr.utils.KernalLSCXMLInformation;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import pr.Constant;
import pr.vinrecognization.utils.CameraParametersUtils;
import pr.vinrecognization.utils.CameraSetting;
import pr.vinrecognization.utils.RecogOpera;
import pr.vinrecognization.utils.VINRecogParameter;
import pr.vinrecognization.utils.VINRecogResult;

/**
 * Software:
 * Version: 1.0.0
 * Company: eastime
 *
 * @author LMM
 * @time:2021/1/19 14
 */
public class VinCameraPreviews extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {
    Activity activity;

    private int srcWidth = 0;
    private int srcHeight = 0;
    private SurfaceHolder surfaceHolder;
    private Camera camera;
    private ArrayList<Integer> srcList = new ArrayList<Integer>();// 拍照分辨率集合
    private int selectedTemplateTypePosition = 0;
    private Vibrator mVibrator;
    private int rotation = 0;// 屏幕取景方向
    private CameraParametersUtils cameraParametersUtils;
    private VinFinderView myVinFinderView;
    private KernalLSCXMLInformation wlci;
    private boolean isRecogSuccess = false;
    private Camera.Size size;
    private boolean isFirstIn = true;
    private boolean islandscape = false;// 是否为横向
    private boolean isSetZoom = false;
    private RecogOpera mRecogOpera;
    private Handler mAutoFocusHandler;

    private  Runnable initCameraParams;

    public int sum = 0;
    private ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1, 1, 3,
            TimeUnit.MILLISECONDS, new ArrayBlockingQueue(1));

    private VINRecogResult vinRecogResult;
    private VINRecogParameter vinRecogParameter = new VINRecogParameter();
    int number;

    public VinCameraPreviews(Activity activity, Handler handler) {
        super(activity);
        this.activity = activity;
        init(activity);
    }

    public void  init(final Activity activity){
        mRecogOpera = new RecogOpera(activity);
         mAutoFocusHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 100) {
                    CameraSetting.getInstance(activity).autoFocus(camera);
                    this.sendEmptyMessageDelayed(100, 2500);
                }
            }
        };
    }

    @Override
    public void onPreviewFrame(final byte[] bytes, Camera camera) {
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
                            Log.d("recogResult", recogResult.toString());
                            isRecogSuccess = true;
                            mVibrator = (Vibrator) activity.getApplication().getSystemService(Service.VIBRATOR_SERVICE);
                            mVibrator.vibrate(200l);
                            //                            savePath = vinRecogResult.savePath;
                            //                            httpContent = vinRecogResult.httpContent;
                            //                            VinOcrActivity.recogResultModelList.get(ViewfinderView.fieldsPosition).resultValue = recogResult;
                            //                            VinOcrActivity.this.runOnUiThread(updateUI);
                            Intent intent = new Intent();
                            intent.putExtra(Constant.EXTRA_TYPE_CODE,4);
                            intent.putExtra(Constant.EXTRA_VIN_CODE, recogResult);
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("result", vinRecogResult);
                            intent.putExtras(bundle);
                            activity.setResult(Activity.RESULT_OK, intent);
                            activity.finish();
                        }

                    } else {
//                    ZhugeTrack(mContext).put("来源", "车架号流识别-SDK").track("OCR-车架号识别失败")
                    }
                    sum -= 1;
                }
            });
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
//        activity.runOnUiThread(initCameraParams);
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
}
