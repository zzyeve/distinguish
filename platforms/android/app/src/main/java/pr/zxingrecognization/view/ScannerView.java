/*
 * Copyright 2014 http://Bither.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pr.zxingrecognization.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.eastime.distinguish.R;
import com.google.zxing.ResultPoint;

import java.util.HashMap;
import java.util.Map;

import pr.vinrecognization.utils.DimensionsUtils;
import pr.vinrecognization.utils.ImageUtils;

public class ScannerView extends View {
	private final Map<ResultPoint, Long> dots = new HashMap<ResultPoint, Long>(
			16);
	private Rect frame, framePreview;

	Bitmap line;
	int mWidth;
	int mHeight;
	Paint paint;
	Paint shadowPaint;
	Paint textPaint;
	float strokeWidth;
	int lineLength;
	float scrollLength;
	int speed;
	String tips = "请扫描二维码";
	boolean down = true;

	public ScannerView(final Context context, final AttributeSet attrs) {
		super(context, attrs);

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
	}

	public void setFraming(final Rect frame,
			final Rect framePreview) {
		this.frame = frame;
		this.framePreview = framePreview;

		invalidate();
	}

	public void drawResultBitmap(final Bitmap bitmap) {
		invalidate();
	}

	public void addDot(final ResultPoint dot) {
		dots.put(dot, System.currentTimeMillis());

		invalidate();
	}

	@Override
	public void onDraw(final Canvas c) {
		if (frame == null)
			return;
		mWidth = c.getWidth();
		mHeight = c.getHeight();


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
//        c.drawText(title, (mWidth - textPaint.measureText(title)) / 2, (frame.top - DimensionsUtils.dip2px(getContext(), 40)), textPaint);

		textPaint.setColor(0xffcccccc);
		textPaint.setTextSize(DimensionsUtils.dip2px(getContext(), 10));
		c.drawText(tips, (mWidth - textPaint.measureText(tips)) / 2, (frame.top - DimensionsUtils.dip2px(getContext(), 20)), textPaint);
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
