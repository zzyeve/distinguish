package pr.vinrecognization.view;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import pr.vinrecognization.utils.DimensionsUtils;

/**
 * Software:
 * Version: 1.0.0
 * Company: eastime
 *
 * @author LMM
 * @time:2021/1/13 17
 */
//public class ScanTipsView extends LinearLayout {

//    private TextView tv_msg;
//    private View v_close;
//    String key = "";
//    Context context;
//
//    public ScanTipsView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
//        super(context, attrs, defStyleAttr);
//        this.context = context;
//        tv_msg = new TextView(context);
//        v_close = new View(context);
//        init();
//    }
//
//    private void init() {
//        setOrientation(HORIZONTAL);
//        setBackground(ContextCompat.getDrawable(context, R.mipmap.img_vin_scan_tips1));
//        this.setVisibility(View.GONE);
//
//        tv_msg.setPadding(DimensionsUtils.dip2px(context,10), DimensionsUtils.dip2px(context,5), 0, 0);
//        tv_msg.setTextSize(12f);
//        tv_msg.setTextColor(Color.WHITE);
//
//        v_close.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                key=false;
//            }
//        });
//        LayoutParams lay =new LayoutParams(DimensionsUtils.dip2px(context,30), DimensionsUtils.dip2px(context,30));
//        lay.gravity = Gravity.RIGHT;
//        lay.rightMargin = DimensionsUtils.dip2px(context,10);
//        addView(tv_msg, android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT);
//        addView(v_close, lay);
//
//    }
//
//    private String getVersionName(Context context){
//        PackageInfo pkgInfo;
//        try {
//            pkgInfo = context.getPackageManager().getPackageInfo(
//                    context.getPackageName(), PackageManager.GET_SIGNATURES);
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//            return "";
//        }
//        return pkgInfo.versionName;
//    }
//}
