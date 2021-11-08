package pr.vinrecognization.utils;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Binder;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.kernal.lisence.Common;
import com.kernal.lisence.DeviceFP;
import com.kernal.smartvisionocr.MathRandom;
import com.kernal.smartvisionocr.RecogOpera;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

/**
 * Software:
 * Version: 1.0.0
 * Company: eastime
 *
 * @author LMM
 * @time:2021/1/18 17
 */
public class RecogService extends Service {
    public static final String TAG = "RecogService";
    private static Common common = new Common();
    public MyBinder binder;
    private int iTH_InitSmartVisionSDK = -1;
    private int nRet = -1;
    private int nResultNum = 0;
    public String returnGetVersion = "";
    int imageformat = 1;
    int bVertFlip = 0;
    int bDwordAligned = 1;
    private Boolean new_lsc_Boolean = false;
    private Boolean isTF = false;
    private ModeAuthFileResult mafr = new ModeAuthFileResult();
    private String productType = "17";
    public static boolean initializeType = false;
    private byte[] data = null;
    private RecogOpera recogOpera;
    public static int response = -1;
    private String result = null;
    MathRandom mathRandom = new MathRandom();

    public RecogService() {
    }

    public IBinder onBind(Intent intent) {
        return this.binder;
    }

    public void onCreate() {
        super.onCreate();
        this.iTH_InitSmartVisionSDK = -10003;
        this.recogOpera = RecogOpera.getInstance(this.getApplicationContext());
        this.binder = new MyBinder();
        String miwenxml = this.readAssetFile(this.getAssets(), "smartvisition.lsc");
        if (miwenxml == null) {
            System.out.println("读取不到smartvisition.lsc文件");
        }

        ModeAuthFileOperate mafo = new ModeAuthFileOperate();
        this.mafr = mafo.ReadAuthFile(miwenxml);
        if (miwenxml != null && this.mafr.isTF(this.productType)) {
            this.isTF = true;
        } else {
            DeviceFP deviceFP = new DeviceFP();
            boolean fleg = false;
            if (miwenxml != null && this.mafr.isCheckPRJMode(this.productType)) {
                fleg = true;
                deviceFP.deviceid = "DeviceIdIsNull";
            }

            if (fleg) {
                this.iTH_InitSmartVisionSDK = this.recogOpera.initOCR();
            } else {
                Log.e("RecogService", "未匹配到授权方式");
                this.iTH_InitSmartVisionSDK = -10015;
            }
        }

    }

    private String readAssetFile(AssetManager am, String filename) {
        String typeModeString = null;

        try {
            InputStream iStream = am.open("SmartVisition/" + filename);
            int size_is = iStream.available();
            byte[] byte_new = new byte[size_is];
            iStream.read(byte_new);
            iStream.close();
            typeModeString = new String(byte_new);
        } catch (IOException var7) {
            typeModeString = null;
        } catch (Exception var8) {
            typeModeString = null;
        }

        return typeModeString;
    }

//    public String readInitFileString(String filePathString) {
//        String SysCertVersion = "wtversion5_5";
//        String deviceidString = "";
//        File dateInitFile = new File(filePathString);
//        if (dateInitFile.exists()) {
//            try {
//                BufferedReader bfReader = new BufferedReader(new FileReader(dateInitFile));
//                deviceidString = bfReader.readLine();
//                bfReader.close();
//                deviceidString = common.getSrcPassword(deviceidString, SysCertVersion);
//            } catch (FileNotFoundException var6) {
//                deviceidString = "";
//                var6.printStackTrace();
//            } catch (IOException var7) {
//                deviceidString = "";
//                var7.printStackTrace();
//            } catch (Exception var8) {
//                deviceidString = "";
//                var8.printStackTrace();
//            }
//        }
//
//        return deviceidString;
//    }

    private String int2string(int i) {
        String str = "";

        try {
            str = String.valueOf(i);
        } catch (Exception var4) {
        }

        return str;
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.iTH_InitSmartVisionSDK == 0) {
            RecogOpera.getInstance(this.getApplicationContext()).unInitOCR();
        }

    }

    public class MyBinder extends Binder {
        public MyBinder() {
        }

        public int getInitSmartVisionOcrSDK() {
            return RecogService.this.iTH_InitSmartVisionSDK;
        }

        public void UninitSmartVisionOcrSDK() {
            if (RecogService.this.iTH_InitSmartVisionSDK == 0) {
                RecogService.this.recogOpera.unInitOCR();
            }

        }

        public void SetROI(int[] regionPos, int width, int height) {
            RecogService.this.recogOpera.SetROI(regionPos, width, height);
        }

        public void LoadStreamNV21(byte[] ImageStreamNV21, int Width, int Height, int roate) {
            RecogService.this.recogOpera.LoadStreamNV21(ImageStreamNV21, Width, Height, roate);
        }

        public void LoadImageFile(String jstrImagePath, int roate) {
            RecogService.this.recogOpera.LoadImageFile(jstrImagePath, roate);
        }

        public void LoadImageFile(String jstrImagePath) {
            RecogService.this.recogOpera.LoadImageFile(jstrImagePath);
        }

        public void AddTemplateFile() {
            RecogService.this.recogOpera.AddTemplateFile();
        }

        public void SetCurrentTemplate(String jstrTempalgeID) {
            RecogService.this.recogOpera.SetCurrentTemplate(jstrTempalgeID);
        }

        public void RemoveTemplate(String jstrTempalgeID) {
            RecogService.this.recogOpera.RemoveTemplate(jstrTempalgeID);
        }

        public void RemoveAllTemplates() {
           RecogService.this.recogOpera.RemoveAllTemplates();
        }

        public void svSaveImage(String jstrSaveImageFileName) {
           RecogService.this.recogOpera.svSaveImage(jstrSaveImageFileName);
        }

        public int Recognize(String devcode, String TempalgeID) {
            int check = -1;
            if (RecogService.this.isTF) {
                check = 0;
            } else if (RecogService.this.mafr.isCheckPRJMode(RecogService.this.productType)) {
                check = -10600;

                String company_name = null;

                try {
                    int id_company_name = RecogService.this.getResources().getIdentifier("company_name", "string", RecogService.this.getPackageName());
                    company_name = RecogService.this.getResources().getString(id_company_name);
                } catch (Resources.NotFoundException var8) {
                    var8.printStackTrace();
                    check = -10609;
                }

                if ( company_name != null) {
                    check = RecogService.this.mafr.isCheckPRJOK(RecogService.this.productType, devcode,company_name);
                }

                if (check == -10090) {
                    check = 0;
                    if (RecogService.this.mathRandom.PercentageRandom() == 5) {
                        if ("main".equals(Thread.currentThread().getName())) {
                            Toast.makeText(RecogService.this.getApplicationContext(), "您的授权已于" + RecogService.this.mafr.ExpiratioTime + "到期，请更新授权，否则识别功能将停止使用！", Toast.LENGTH_SHORT).show();
                        } else {
                            Looper.prepare();
                            Toast.makeText(RecogService.this.getApplicationContext(), "您的授权已于" + RecogService.this.mafr.ExpiratioTime + "到期，请更新授权，否则识别功能将停止使用！", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        }
                    }
                }

                if (check == 0) {
                    check = RecogService.this.mafr.isCheckTemplatetypeOK(TempalgeID);
                }
            }

            if (check == 0) {
                RecogService.response = RecogService.this.recogOpera.Recognize();
            }

            return check;
        }

        public String GetResults(int[] nCharCount) {
           RecogService.this.result = RecogService.this.recogOpera.GetResults(nCharCount);
            return RecogService.this.result;
        }

        public void svGetResLinePos(int[] LeftUpPoint, int[] RightDownPoint) {
            RecogService.this.recogOpera.svGetResLinePos(LeftUpPoint, RightDownPoint);
        }

        public void svSaveImageResLine(String jstrSaveImageFileName) {
           RecogService.this.recogOpera.svSaveImageResLine(jstrSaveImageFileName);
        }
    }
}
