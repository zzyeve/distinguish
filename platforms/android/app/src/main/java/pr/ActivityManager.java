package pr;

import android.app.Activity;
import android.content.Intent;

import pr.platerecognization.CameraActivity;
import pr.vinrecognization.VinOcrActivity;
import pr.zxingrecognization.CaptureActivity;

/**
 * Software:
 * Version: 1.0.0
 * Company: eastime
 *
 * @author LMM
 * @time:2021/1/20 16
 */
public class ActivityManager {
    /**
     * CrashHandler实例
     */
    private static ActivityManager INSTANCE;

    /**
     * 获取CrashHandler实例 ,单例模式
     */
    public static ActivityManager getInstance() {
        if (INSTANCE == null)
            INSTANCE = new ActivityManager();
        return INSTANCE;
    }

    public void resultActivity(Activity activity, int type, boolean isOneAct, boolean isPlate, boolean isVin, boolean isCapture, boolean isCreate, int plateNum, double confidence) {
        if (isOneAct) {
            toActivity(activity, type, isPlate, isVin, isCapture, isCreate,plateNum,confidence);
        } else {
            endActivity(activity, type);
        }
    }

    public void endActivity(Activity activity, int type) {
        Intent intent = new Intent();
        intent.putExtra(Constant.EXTRA_TYPE_CODE, type);
        activity.setResult(Activity.RESULT_OK, intent);
        activity.finish();
    }

    public void toActivity(Activity activity, int type, boolean isPlate, boolean isVin, boolean isCapture, boolean isCreate, int plateNum, double confidence) {
        Intent intent = new Intent();
        if (type == 2) {
            intent.setClass(activity, CameraActivity.class);
        } else if (type == 4) {
            intent.setClass(activity, VinOcrActivity.class);
        } else if (type == 5) {
            intent.setClass(activity, CaptureActivity.class);
        } else {
            intent.putExtra(Constant.EXTRA_TYPE_CODE, type);
            activity.setResult(Activity.RESULT_OK, intent);
            activity.finish();
            return;
        }

        intent.putExtra(Constant.EXTRA_IS_PLATE_CODE, isPlate);
        intent.putExtra(Constant.EXTRA_IS_VIN_CODE, isVin);
        intent.putExtra(Constant.EXTRA_IS_CAPTURE_CODE, isCapture);
        intent.putExtra(Constant.EXTRA_IS_CREATE_CODE, isCreate);
        intent.putExtra(Constant.EXTRA_PLATE_NUM_CODE, plateNum);
        intent.putExtra(Constant.EXTRA_CONFIDENCE_CODE, confidence);

        intent.putExtra(Constant.EXTRA_ONEACTIVITY_CODE, true);
        activity.startActivityForResult(intent, Constant.REQUEST_CODE);
    }
}
