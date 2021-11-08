package pr.platerecognization;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Software:
 * Version: 1.0.0
 * Company: eastime
 *
 * @author LMM
 * @time:2020/11/27 15
 */
public class DeepAssetUtil {
    public static String rule="([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼]{1}(([A-HJ-Z]{1}[A-HJ-NP-Z0-9]{5})|([A-HJ-Z]{1}(([DF]{1}[A-HJ-NP-Z0-9]{1}[0-9]{4})|([0-9]{5}[DF]{1})))|([A-HJ-Z]{1}[A-D0-9]{1}[0-9]{3}警)))|([0-9]{6}使)|((([沪粤川云桂鄂陕蒙藏黑辽渝]{1}A)|鲁B|闽D|蒙E|蒙H)[0-9]{4}领)|(WJ[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼·•]{1}[0-9]{4}[TDSHBXJ0-9]{1})|([VKHBSLJNGCE]{1}[A-DJ-PR-TVY]{1}[0-9]{5})";

    public static String MSG_DOWNLOAD_FILE_SAVE_PATH=Environment.getDownloadCacheDirectory().toString() + File.separator;

    public static final String ApplicationDir = "pr";
    public static final String CASCADE_FILENAME = "cascade.xml";
    public static final String FINEMAPPING_PROTOTXT = "HorizonalFinemapping.prototxt";
    public static final String FINEMAPPING_CAFFEMODEL = "HorizonalFinemapping.caffemodel";
    public static final String SEGMENTATION_PROTOTXT = "Segmentation.prototxt";
    public static final String SEGMENTATION_CAFFEMODEL = "Segmentation.caffemodel";
    public static final String RECOGNIZATION_PROTOTXT = "CharacterRecognization.prototxt";
    public static final String RECOGNIZATION_CAFFEMODEL = "CharacterRecognization.caffemodel";
    public static final String FREE_INCEPTION_PROTOTXT = "SegmenationFree-Inception.prototxt";
    public static final String FREE_INCEPTION_CAFFEMODEL = "SegmenationFree-Inception.caffemodel";

    public static final String SDCARD_DIR =  Environment.getExternalStorageDirectory()
            + File.separator + ApplicationDir; //解压文件存放位置

    public static long handle;


    private static void CopyAssets(Context context, String assetDir, String dir) {
        String[] files;
        try {
            // 获得Assets一共有几多文件
            files = context.getAssets().list(assetDir);
        } catch (IOException e1) {
            return;
        }
        File mWorkingPath = new File(dir);
        // 如果文件路径不存在
        if (!mWorkingPath.exists()) {
            // 创建文件夹
            if (!mWorkingPath.mkdirs()) {
                // 文件夹创建不成功时调用
            }
        }

        for (String file : files) {
            try {
                // 根据路径判断是文件夹还是文件
                if (!file.contains(".")) {
                    if (0 == assetDir.length()) {
                        CopyAssets(context, file, dir + file + "/");
                    } else {
                        CopyAssets(context, assetDir + "/" + file, dir + "/" + file + "/");
                    }
                    continue;
                }
                File outFile = new File(mWorkingPath, file);
                if (outFile.exists())
                    continue;
                InputStream in;
                if (0 != assetDir.length()) {
                    in = context.getAssets().open(assetDir + "/" + file);
                } else {
                    in = context.getAssets().open(file);
                }

                OutputStream out = new FileOutputStream(outFile);
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }

                in.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void copyFilesFromAssets(Context context) {
        DeepAssetUtil.CopyAssets(context, ApplicationDir, SDCARD_DIR);
    }

    //初始化识别资源
    public static long initRecognizer(Context context) {
        String cascade_filename = SDCARD_DIR + File.separator + CASCADE_FILENAME;
        String finemapping_prototxt = SDCARD_DIR + File.separator + FINEMAPPING_PROTOTXT;
        String finemapping_caffemodel = SDCARD_DIR + File.separator + FINEMAPPING_CAFFEMODEL;
        String segmentation_prototxt = SDCARD_DIR + File.separator + SEGMENTATION_PROTOTXT;
        String segmentation_caffemodel = SDCARD_DIR + File.separator + SEGMENTATION_CAFFEMODEL;
        String character_prototxt = SDCARD_DIR + File.separator + RECOGNIZATION_PROTOTXT;
        String character_caffemodel = SDCARD_DIR + File.separator + RECOGNIZATION_CAFFEMODEL;
        String segmentation_free_prototxt = SDCARD_DIR + File.separator + FREE_INCEPTION_PROTOTXT;
        String segmentation_free_caffemodel = SDCARD_DIR + File.separator + FREE_INCEPTION_CAFFEMODEL;
        copyFilesFromAssets(context);
        //调用JNI 加载资源函数
        return PlateRecognition.InitPlateRecognizer(
                cascade_filename,
                finemapping_prototxt, finemapping_caffemodel,
                segmentation_prototxt, segmentation_caffemodel,
                character_prototxt, character_caffemodel,
                segmentation_free_prototxt, segmentation_free_caffemodel);
    }


    public static PlateInfo simpleRecog(Bitmap bmp, int dp) {

        float dp_asp = dp / 10.f;
//        imgv.setImageBitmap(bmp);
        Mat mat_src = new Mat(bmp.getWidth(), bmp.getHeight(), CvType.CV_8UC4);

        float new_w = bmp.getWidth() * dp_asp;
        float new_h = bmp.getHeight() * dp_asp;
        Size sz = new Size(new_w, new_h);
        Utils.bitmapToMat(bmp, mat_src);
        Imgproc.resize(mat_src, mat_src, sz);
//        Log.e("static SimpleRecog","sz"+sz.height+"  "+sz.height);
//        long currentTime1 = System.currentTimeMillis();
//        String res = PlateRecognition.SimpleRecognization(mat_src.getNativeObjAddr(),handle);
//        resbox.setText("识别结果:"+res);

        PlateInfo plateInfo = PlateRecognition.PlateInfoRecognization(mat_src.getNativeObjAddr(), handle);
        return plateInfo;

    }

    public static boolean PlateMatcher(String plate){
        Pattern p = Pattern.compile(rule);
        Matcher m = p.matcher(plate);
        return m.matches();
    }
}
