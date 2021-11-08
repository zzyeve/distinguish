package pr.vinrecognization.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import com.eastime.distinguish.R;
import com.kernal.smartvisionocr.model.ConfigParamsModel;
import com.kernal.smartvisionocr.utils.KernalLSCXMLInformation;

import java.util.List;

import pr.Constant;
import pr.vinrecognization.utils.DimensionsUtils;
import pr.vinrecognization.utils.ImageUtils;

/**
 * Software:
 *
 * @author gengqiquan
 * @date 2018/6/28 下午6:01
 * 扫描控件
 * @time:2021/1/13 14
 */
public class VinFinderView extends View {

    KernalLSCXMLInformation wlci;
    Bitmap line;
    public Rect frame;
    public int mWidth;
    public int mHeight;
    List<ConfigParamsModel> configParamsModel;
    float count = 1f;
    boolean isAdd = true;
    DisplayMetrics dm;
    Paint paint;
    Paint shadowPaint;
    Paint textPaint;
    float strokeWidth;
    int lineLength;
    float scrollLength;
    int speed;
//    String title = "请将车架号放置于扫描框内";
    String tips = "将VIN码置入框内";
    boolean down = true;

    public VinFinderView(Context context, KernalLSCXMLInformation wlci, String type) {
        super(context);
        this.wlci = wlci;
        strokeWidth = DimensionsUtils.dip2px(context, 1);
        speed = DimensionsUtils.dip2px(context, 2);
        lineLength = DimensionsUtils.dip2px(context, 15);
        shadowPaint = new Paint();
        shadowPaint.setColor(0x99000000);
        paint = new Paint();
        paint.setColor(0xff2a8cff);
        paint.setAntiAlias(true);
        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        if (type != null && type != "") {
            configParamsModel = wlci.fieldType.get(type);
        }
        count = 1f;
        isAdd = true;
        dm = new DisplayMetrics();
        dm = context.getApplicationContext().getResources().getDisplayMetrics();
    }

    @Override
    protected void onDraw(Canvas c) {
        super.onDraw(c);
        mWidth = c.getWidth();
        mHeight = c.getHeight();
        if (configParamsModel != null) {
            /**
             * 这个矩形就是中间显示的那个框框
             */
            ConfigParamsModel configParamsModel = this.configParamsModel.get(Constant.fieldsPosition);
            frame = new Rect((int) (configParamsModel.leftPointX * mWidth),
                    (int) (mHeight * configParamsModel.leftPointY),
                    (int) ((configParamsModel.leftPointX + configParamsModel.width) * mWidth),
                    (int) (mHeight * (configParamsModel.leftPointY + configParamsModel.height)));
            if (frame == null) {
                return;
            }


            //4 四个角，先画横线后画竖线，strokeWidth画笔宽度的一般作为偏移量，去除横竖交界部分的空白角
            paint.setStrokeWidth(strokeWidth * 2);
            float storke = strokeWidth;

            //左上
//            c.drawLine(frame !!.left.toFloat() - strokeWidth, frame !!.top.toFloat() + storke, frame
//            !!.left.toFloat() + lineLength, frame !!.top.toFloat() + storke, paint);
            c.drawLine(frame.left - strokeWidth, frame.top + storke, frame.left + lineLength, frame.top + storke, paint);
//            c.drawLine(frame !!.left.toFloat() + storke, frame !!.top.toFloat(), frame !!.
//            left.toFloat() + storke, frame !!.top.toFloat() + lineLength, paint)
            c.drawLine(frame.left + storke, frame.top, frame.left + storke, frame.top + lineLength, paint);
            //右上
//            c.drawLine(frame !!.right.toFloat() - lineLength, frame !!.top.toFloat() + storke, frame
//            !!.right.toFloat(), frame !!.top.toFloat() + storke, paint)
            c.drawLine(frame.right - lineLength, frame.top + storke, frame.right, frame.top + storke, paint);
//            c.drawLine(frame !!.right.toFloat() - storke, frame !!.top.toFloat(), frame !!.
//            right.toFloat() - storke, frame !!.top.toFloat() + lineLength, paint)
            c.drawLine(frame.right - storke, frame.top + storke, frame.right - storke, frame.top + lineLength, paint);
            //左下
            c.drawLine(frame.left, frame.bottom - storke, frame.left + lineLength,
                    frame.bottom - storke, paint);
            c.drawLine(frame.left + storke, frame.bottom - lineLength - strokeWidth,
                    frame.left + storke, frame.bottom, paint);
            //右下
            c.drawLine(frame.right - lineLength - strokeWidth, frame.bottom - storke,
                    frame.right, frame.bottom - storke, paint);
            c.drawLine(frame.right - storke, frame.bottom - lineLength - strokeWidth,
                    frame.right - storke, frame.bottom - strokeWidth, paint);
            //四条边,上下左右
            paint.setStrokeWidth(strokeWidth);
            c.drawLine(frame.left, frame.top, frame.right, frame.top, paint);
            c.drawLine(frame.left, frame.bottom, frame.right, frame.bottom, paint);
            c.drawLine(frame.left, frame.top, frame.left, frame.bottom, paint);
            c.drawLine(frame.right, frame.top, frame.right, frame.bottom, paint);
            //上下左右阴影
            c.drawRect(0f, 0f, mWidth, frame.top, shadowPaint);
            c.drawRect(0f, frame.bottom, mWidth, mHeight, shadowPaint);
            c.drawRect(0f, frame.top, frame.left, frame.bottom, shadowPaint);
            c.drawRect(frame.right, frame.top, mWidth, frame.bottom, shadowPaint);

            //文字
            textPaint.setColor(Color.WHITE);
            textPaint.setTextSize(DimensionsUtils.dip2px(getContext(), 16));
//            c.drawText(title, (mWidth - textPaint.measureText(title)) / 2, (frame.top - DimensionsUtils.dip2px(getContext(), 40)), textPaint);

            textPaint.setColor(0xffcccccc);
            textPaint.setTextSize(DimensionsUtils.dip2px(getContext(), 10));
            c.drawText(tips, (mWidth - textPaint.measureText(tips)) / 2, (frame.top - DimensionsUtils.dip2px(getContext(), 20)), textPaint);
        }
        line = ImageUtils.getBitmap(getContext(), R.mipmap.img_scan_line, frame.width(), DimensionsUtils.dip2px(getContext(), 4));
        //滚动线
        c.drawBitmap(line, frame.left, frame.top + scrollLength, paint);
        if (scrollLength > frame.bottom - frame.top - DimensionsUtils.dip2px(getContext(), 4)) {
            down = false;
        }
        if (scrollLength <= 0) {
            down = true;
        }
        if (down) {
            scrollLength += speed;
        } else {
            scrollLength = 0f;
        }
        postInvalidateDelayed(50, frame.left + 100, frame.top, frame.right - 100, frame.bottom);
    }

}
