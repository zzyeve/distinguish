package pr.zxingrecognization;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.eastime.distinguish.R;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.ResultPointCallback;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

import pr.ActivityManager;
import pr.Constant;
import pr.ImageUtils;
import pr.SharedUtil;
import pr.zxingrecognization.camera.CameraManager;
import pr.zxingrecognization.view.ScannerView;

/**
 * 二维码
 */
public class CaptureActivity extends Activity implements SurfaceHolder.Callback {
    private static final long AUTO_FOCUS_INTERVAL_MS = 2500L;
    private final CameraManager cameraManager = new CameraManager();
    protected ScannerView scannerView;
    private SurfaceHolder surfaceHolder;
    private HandlerThread cameraThread;
    private Handler cameraHandler;

    private ImageView iv_back;
    private ImageView iv_flash;
    private TextView create_tv;//创建
    private LinearLayout input_ll;//输入
    private TextView input_tv;
    private LinearLayout car_plate_ll;
    private LinearLayout vin_code_ll;
    private LinearLayout qr_code_ll;
    private ImageView qr_code_img;
    private TextView qr_code_tv;

    private boolean fromGallery;
    private boolean isOneAct = false;
    private boolean lightIsOn = false;

    private boolean isPlate = true;
    private boolean isVin = true;
    private boolean isCapture = true;
    private boolean isCreate = true;
    private int plateNum;
    private double confidence;

    private static boolean DISABLE_CONTINUOUS_AUTOFOCUS = Build.MODEL.equals("GT-I9100") //
            // Galaxy S2
            || Build.MODEL.equals("SGH-T989") // Galaxy S2
            || Build.MODEL.equals("SGH-T989D") // Galaxy S2 X
            || Build.MODEL.equals("SAMSUNG-SGH-I727") // Galaxy S2 Skyrocket
            || Build.MODEL.equals("GT-I9300") // Galaxy S3
            || Build.MODEL.equals("GT-N7000"); // Galaxy Note

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_capture);

        Intent intent = getIntent();
        isOneAct = intent.getBooleanExtra(Constant.EXTRA_ONEACTIVITY_CODE, false);
        isPlate = intent.getBooleanExtra(Constant.EXTRA_IS_PLATE_CODE, true);
        isVin = intent.getBooleanExtra(Constant.EXTRA_IS_VIN_CODE, true);
        isCapture = intent.getBooleanExtra(Constant.EXTRA_IS_CAPTURE_CODE, true);
        isCreate = intent.getBooleanExtra(Constant.EXTRA_IS_CREATE_CODE, true);
        plateNum = intent.getIntExtra(Constant.EXTRA_PLATE_NUM_CODE, Constant.PLATE_NUM);
        confidence =intent.getDoubleExtra(Constant.EXTRA_CONFIDENCE_CODE, Constant.CONFIDENCE);
        fromGallery = false;
        initView();
        initClick();
    }

    private void initView() {
        scannerView = findViewById(R.id.scan_activity_mask);

        iv_back = findViewById(R.id.iv_back);
        iv_flash = findViewById(R.id.iv_flash);
        input_ll = findViewById(R.id.input_ll);
        input_tv = findViewById(R.id.input_tv);
        create_tv = findViewById(R.id.create_tv);
        car_plate_ll = findViewById(R.id.car_plate_ll);
        vin_code_ll = findViewById(R.id.vin_code_ll);
        qr_code_ll = findViewById(R.id.qr_code_ll);
        qr_code_img = findViewById(R.id.qr_code_img);
        qr_code_tv = findViewById(R.id.qr_code_tv);
        qr_code_img.setImageResource(R.mipmap.icon_qr_code);
        qr_code_tv.setTextColor(getResources().getColor(R.color.color_app));
        input_tv.setText("手动输入二维码");
        input_ll.setVisibility(View.GONE);
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
        iv_flash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    boolean isSuccess = cameraManager.setFlashLight(!lightIsOn);
                    if (!isSuccess) {
                        Toast.makeText(CaptureActivity.this, "暂时无法开启闪光灯", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (lightIsOn) {
                        // 关闭闪光灯
                        iv_flash.setImageResource(R.mipmap.ic_ocr_flashlight_off);
                        lightIsOn = false;
                    } else {
                        // 开启闪光灯
                        iv_flash.setImageResource(R.mipmap.ic_ocr_flashlight_on);
                        lightIsOn = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        input_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chengeActivity(7);
            }
        });
        car_plate_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chengeActivity(2);
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
        ActivityManager.getInstance().resultActivity(CaptureActivity.this, type, isOneAct, isPlate, isVin, isCapture, isCreate,plateNum,confidence);
    }

    @Override
    protected void onResume() {
        super.onResume();

        cameraThread = new HandlerThread("cameraThread", Process.THREAD_PRIORITY_BACKGROUND);
        cameraThread.start();
        cameraHandler = new Handler(cameraThread.getLooper());

        final SurfaceView surfaceView = (SurfaceView) findViewById(R.id.scan_activity_preview);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        lightIsOn = false;
    }

    @Override
    public void surfaceCreated(final SurfaceHolder holder) {
        cameraHandler.post(openRunnable);
    }

    @Override
    public void surfaceDestroyed(final SurfaceHolder holder) {
    }

    @Override
    public void surfaceChanged(final SurfaceHolder holder, final int format, final int width, final int height) {

    }

    @Override
    protected void onPause() {
        cameraHandler.post(closeRunnable);

        surfaceHolder.removeCallback(this);

        super.onPause();
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_FOCUS:
            case KeyEvent.KEYCODE_CAMERA:
                // don't launch camera app
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_UP:
                cameraHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        cameraManager.setTorch(keyCode == KeyEvent.KEYCODE_VOLUME_UP);
                    }
                });
                return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    public void handleResult(final Result scanResult, Bitmap thumbnailImage,
                             final float thumbnailScaleFactor) {
        // superimpose dots to highlight the key features of the qr code
        final ResultPoint[] points = scanResult.getResultPoints();
        if (points != null && points.length > 0) {
            final Paint paint = new Paint();
            paint.setColor(getResources().getColor(R.color.scan_result_dots));
            paint.setStrokeWidth(10.0f);

            final Canvas canvas = new Canvas(thumbnailImage);
            canvas.scale(thumbnailScaleFactor, thumbnailScaleFactor);
            for (final ResultPoint point : points)
                canvas.drawPoint(point.getX(), point.getY(), paint);
        }

        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        thumbnailImage = Bitmap.createBitmap(thumbnailImage, 0, 0,
                thumbnailImage.getWidth(), thumbnailImage.getHeight(), matrix,
                false);
        scannerView.drawResultBitmap(thumbnailImage);

        final Intent result = getIntent();
        Log.i("ansen", "扫描结果:" + scanResult.getText());
        result.putExtra(Constant.EXTRA_TYPE_CODE, 5);
        result.putExtra(Constant.EXTRA_ERCODE_CODE, scanResult.getText());
//        result.putExtra(Constant.EXTRA_IMGPATH_CODE,  new ImageUtils().savePicture(thumbnailImage).getPath());
        SharedUtil.getInstance(this).put(Constant.EXTRA_BASE64_CODE, ImageUtils.bitmapToBase64(thumbnailImage));
        setResult(RESULT_OK, result);

//        ImageView viewById = findViewById(R.id.image);
//        viewById.setImageBitmap(thumbnailImage);
//        viewById.setVisibility(View.VISIBLE);
        finish();
    }

    private final Runnable openRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                final Camera camera = cameraManager.open(surfaceHolder,
                        !DISABLE_CONTINUOUS_AUTOFOCUS);

                final Rect framingRect = cameraManager.getFrame();
                final Rect framingRectInPreview = cameraManager
                        .getFramePreview();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        scannerView.setFraming(framingRect,
                                framingRectInPreview);
                    }
                });

                final String focusMode = camera.getParameters().getFocusMode();
                final boolean nonContinuousAutoFocus = Camera.Parameters.FOCUS_MODE_AUTO
                        .equals(focusMode)
                        || Camera.Parameters.FOCUS_MODE_MACRO.equals(focusMode);

                if (nonContinuousAutoFocus)
                    cameraHandler.post(new AutoFocusRunnable(camera));

                cameraHandler.post(fetchAndDecodeRunnable);
            } catch (final IOException x) {
                Log.i("problem opening camera", x.toString());
                finish();
            } catch (final RuntimeException x) {
                Log.i("problem opening camera", x.toString());
                finish();
            }
        }
    };

    private final Runnable closeRunnable = new Runnable() {
        @Override
        public void run() {
            cameraManager.close();

            // cancel background thread
            cameraHandler.removeCallbacksAndMessages(null);
            cameraThread.quit();
        }
    };

    private final class AutoFocusRunnable implements Runnable {
        private final Camera camera;

        public AutoFocusRunnable(final Camera camera) {
            this.camera = camera;
        }

        @Override
        public void run() {
            camera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(final boolean success,
                                        final Camera camera) {
                    // schedule again
                    cameraHandler.postDelayed(AutoFocusRunnable.this,
                            AUTO_FOCUS_INTERVAL_MS);
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        setResult(Activity.RESULT_OK, data);
        finish();
    }

    private final Runnable fetchAndDecodeRunnable = new Runnable() {
        private final QRCodeReader reader = new QRCodeReader();
        private final Map<DecodeHintType, Object> hints = new EnumMap<DecodeHintType,
                Object>(DecodeHintType.class);

        @Override
        public void run() {
            if (fromGallery) {
                cameraHandler.postDelayed(fetchAndDecodeRunnable, 500);
                return;
            }
            cameraManager.requestPreviewFrame(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(final byte[] data, final Camera camera) {
                    decode(data);
                }
            });
        }

        private void decode(final byte[] data) {
            final PlanarYUVLuminanceSource source = cameraManager.buildLuminanceSource(data);
            final BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            try {
                hints.put(DecodeHintType.NEED_RESULT_POINT_CALLBACK,
                        new ResultPointCallback() {
                            @Override
                            public void foundPossibleResultPoint(
                                    final ResultPoint dot) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        scannerView.addDot(dot);
                                    }
                                });
                            }
                        });
                final Result scanResult = reader.decode(bitmap, hints);
                if (!resultValid(scanResult.getText())) {
                    cameraHandler.post(fetchAndDecodeRunnable);
                    return;
                }
                final int thumbnailWidth = source.getThumbnailWidth();
                final int thumbnailHeight = source.getThumbnailHeight();
                final float thumbnailScaleFactor = (float) thumbnailWidth
                        / source.getWidth();

                final Bitmap thumbnailImage = Bitmap.createBitmap(
                        thumbnailWidth, thumbnailHeight,
                        Bitmap.Config.ARGB_8888);
                thumbnailImage.setPixels(source.renderThumbnail(), 0,
                        thumbnailWidth, 0, 0, thumbnailWidth, thumbnailHeight);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        handleResult(scanResult, thumbnailImage,
                                thumbnailScaleFactor);
                    }
                });
            } catch (final Exception x) {
                cameraHandler.post(fetchAndDecodeRunnable);
            } finally {
                reader.reset();
            }
        }
    };

    public boolean resultValid(String result) {
        return true;
    }

    public void finish() {
        super.finish();
//        overridePendingTransition(R.anim.scanner_out_enter, 0);
    }
}
