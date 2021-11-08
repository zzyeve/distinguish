package com.eastime.plugin;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;

import pr.Constant;
import pr.SharedUtil;
import pr.platerecognization.CameraActivity;
import pr.vinrecognization.VinOcrActivity;
import pr.zxingrecognization.CaptureActivity;

/**
 * This class echoes a string called from JavaScript.
 */
public class distinguishPlugin extends CordovaPlugin {
    public static String TAG = "distinguishPlugin";
    public static CallbackContext callback;
    int type = 2;
    boolean isPlate = true;
    boolean isVin = true;
    boolean isCapture = true;
    boolean isCreate = true;
    int plateNum = Constant.PLATE_NUM;
    double confidence = Constant.CONFIDENCE;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("distinguish")) {
            String message = args.getString(0);
            this.distinguish(message,callbackContext);
            return true;
        }
        return false;
    }

    private void distinguish(String message, CallbackContext callback) {
        HashMap maps = JSON.parseObject(message, HashMap.class);
        type = (int) maps.get("type");
        isPlate = (boolean) maps.get("isPlate");
        isVin = (boolean) maps.get("isVin");
        isCapture = (boolean) maps.get("isCapture");
        isCreate = (boolean) maps.get("isCreate");
        plateNum = (int) maps.get("plateNum");
        confidence = Double.parseDouble(maps.get("confidence").toString());
        Log.d(TAG, "type:" + type + " isPlate:" + isPlate + " isVin:" + isVin + " isCapture:" + isCapture + " isCreate:" + isCreate
                + " plateNum:" + plateNum + " confidence:" + confidence);
        gotoFunctionActivity(callback, type, isPlate, isVin, isCapture, isCreate, plateNum, confidence);
    }

    public void gotoFunctionActivity(CallbackContext callback, int type, boolean isPlate, boolean isVin, boolean isCapture, boolean isCreate, int plateNum, double confidence) {
        this.callback = callback;
        Activity activity = cordova.getActivity();
        Intent intent = new Intent(activity, CameraActivity.class);
        if (type == 4) {
            intent.setClass(activity, VinOcrActivity.class);
        } else if (type == 5) {
            intent.setClass(activity, CaptureActivity.class);
        }
        intent.putExtra(Constant.EXTRA_IS_PLATE_CODE, isPlate);
        intent.putExtra(Constant.EXTRA_IS_VIN_CODE, isVin);
        intent.putExtra(Constant.EXTRA_IS_CAPTURE_CODE, isCapture);
        intent.putExtra(Constant.EXTRA_IS_CREATE_CODE, isCreate);

        intent.putExtra(Constant.EXTRA_PLATE_NUM_CODE, plateNum);
        intent.putExtra(Constant.EXTRA_CONFIDENCE_CODE, confidence);

        intent.putExtra(Constant.EXTRA_ONEACTIVITY_CODE, true);
        cordova.startActivityForResult(this, intent, Constant.REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        String message = "";
        if (requestCode == Constant.REQUEST_CODE) {
            Activity activity = cordova.getActivity();
            JSONObject dataJson = new JSONObject();
            if (callback != null) {
                if (data.hasExtra(Constant.EXTRA_VIN_CODE)) {
                    dataJson.put(Constant.EXTRA_VIN_CODE, data.getStringExtra(Constant.EXTRA_VIN_CODE));
                }
                if (data.hasExtra(Constant.EXTRA_ERCODE_CODE)) {
                    dataJson.put(Constant.EXTRA_ERCODE_CODE, data.getStringExtra(Constant.EXTRA_ERCODE_CODE));
                }
                if (data.hasExtra(Constant.EXTRA_TYPE_CODE)) {
                    dataJson.put(Constant.EXTRA_TYPE_CODE, data.getIntExtra(Constant.EXTRA_TYPE_CODE, 0));
                }
                String respond = SharedUtil.getInstance(activity).get(Constant.EXTRA_RESPOND_CODE);
                if(!TextUtils.isEmpty(respond)){
                    dataJson.put(Constant.EXTRA_RESPOND_CODE, respond);
                }
//                String base64 = SharedUtil.getInstance(activity).get(Constant.EXTRA_BASE64_CODE);
//                if(!TextUtils.isEmpty(base64)){
//                    dataJson.put(Constant.EXTRA_BASE64_CODE, base64);
//                }
                SharedUtil.getInstance(activity).clear();
                message=JSON.toJSONString(dataJson);
                callback.success(message);
            }
            Log.e(TAG, "原生页面返回----" + message);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
