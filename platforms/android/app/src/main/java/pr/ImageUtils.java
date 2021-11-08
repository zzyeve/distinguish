package pr;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import pr.platerecognization.DeepAssetUtil;
import pr.vinrecognization.VinOcrActivity;


/**
 * Software:
 * Version: 1.0.0
 * Company: eastime
 *
 * @author LMM
 * @time:2019/11/25 10
 */
public class ImageUtils {
    String Tag = "ImageUtils";

    private int screenWidth;// 手机屏幕的宽（像素）
    private int screenHeight;// 手机屏幕的高（像素）

    public ImageUtils() {
        if (screenWidth == 0) {
            DisplayMetrics metric = new DisplayMetrics();
            screenWidth = metric.widthPixels; // 屏幕宽度（像素）
            screenHeight = metric.heightPixels; // 屏幕宽度（像素）
        }
    }

    /**
     * 根据路径获取图片并且压缩，适应view
     *
     * @param filePath    图片路径
     * @param contentView 适应的view
     * @return Bitmap 压缩后的图片
     */
    public Bitmap compressionFiller(String filePath, View contentView) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        Bitmap bitmap = BitmapFactory.decodeFile(filePath, opt);
        int layoutHeight = contentView.getHeight();
        float scale = 0f;
        int bitmapHeight = bitmap.getHeight();
        int bitmapWidth = bitmap.getWidth();
        scale = bitmapHeight > bitmapWidth
                ? layoutHeight / (bitmapHeight * 1f)
                : screenWidth / (bitmapWidth * 1f);
        Bitmap resizeBmp;
        if (scale != 0) {
            int bitmapheight = bitmap.getHeight();
            int bitmapwidth = bitmap.getWidth();
            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale); // 长和宽放大缩小的比例
            resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmapwidth,
                    bitmapheight, matrix, true);
        } else {
            resizeBmp = bitmap;
        }

        Log.e(Tag, resizeBmp.toString());
        return resizeBmp;
    }

    /**
     * 根据路径获取图片并且压缩，适应view
     *
     * @param filePath 图片路径
     * @return String 压缩后的保存的图片
     */
    public String compressionFiller(String filePath) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(filePath, newOpts);//此时返回bm为空

        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        //现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        float hh = 2076f;//这里设置高度为2076f
        float ww = 1920f;//这里设置宽度为3840f
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
//        newOpts.inPreferredConfig = Bitmap.Config.RGB_565;
        newOpts.inSampleSize = be;//设置缩放比例
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        bitmap = BitmapFactory.decodeFile(filePath, newOpts);
//        Bitmap resizeBmp = compressImage(bitmap);
        File file = savePicture(bitmap, true);
        return file.getPath();
    }

    /**
     * 根据路径获取图片并且压缩，适应view
     *
     * @param filePath 图片路径
     * @return resizeBmp 压缩后的保存的图片bitmap
     */
    public Bitmap compressionBitmap(String filePath) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(filePath, newOpts);//此时返回bm为空

        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        //现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        float hh = 800f;//这里设置高度为800f
        float ww = 480f;//这里设置宽度为480f
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;//设置缩放比例
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        bitmap = BitmapFactory.decodeFile(filePath, newOpts);
        Bitmap resizeBmp = compressImage(bitmap);
        return resizeBmp;
    }

    /**
     * 质量压缩
     *
     * @param image
     * @return
     */
    public Bitmap compressImage(Bitmap image) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while (baos.toByteArray().length / 1024 > 50) {    //循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            options -= 10;//每次都减少10
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中

        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
        return bitmap;
    }

    /**
     * 根据压缩图片并且适应view
     *
     * @param bitmap      压缩图片
     * @param contentView 适应的view
     * @return 压缩后的图片
     */
    public Bitmap compressionFiller(Bitmap bitmap, View contentView) {
        int layoutHeight = contentView.getHeight();
        float scale = 0f;
        int bitmapHeight = bitmap.getHeight();
        int bitmapWidth = bitmap.getWidth();
        scale = bitmapHeight > bitmapWidth
                ? layoutHeight / (bitmapHeight * 1f)
                : screenWidth / (bitmapWidth * 1f);
        Bitmap resizeBmp;
        if (scale != 0) {
            int bitmapheight = bitmap.getHeight();
            int bitmapwidth = bitmap.getWidth();
            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale); // 长和宽放大缩小的比例
            resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmapwidth,
                    bitmapheight, matrix, true);
        } else {
            resizeBmp = bitmap;
        }
        return resizeBmp;
    }

    public File savePicture(Bitmap bitmap, boolean needReturn) {
        String Path = DeepAssetUtil.MSG_DOWNLOAD_FILE_SAVE_PATH + "res/";
        File file = new File(Path);
        if (!file.exists()) {
            file.mkdir();
        }

        File image = new File(Path + "image_copy.jpg");
        if (image.exists()) {
            image.delete();
        }

        Log.i("TAG", "savePicture: ");
        FileOutputStream out;
        if (image.exists()) {
            image.delete();
        }
        try {
            image.createNewFile();
            out = new FileOutputStream(image);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
            out.flush();
            out.close();
            if (!needReturn) {
                return null;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    public File savePicture(Bitmap bitmap) {
        String Path = Environment.getExternalStorageDirectory() + File.separator + Environment.DIRECTORY_DCIM + File.separator + "Camera" + File.separator;
        File file = new File(Path);
        if (!file.exists()) {
            file.mkdir();
        }
        String fileName = "image" + System.currentTimeMillis() + ".jpg";
        File image = new File(Path + fileName);
        if (image.exists()) {
            image.delete();
        }

        Log.i("TAG", "savePicture: ");
        FileOutputStream out;
        try {
            image.createNewFile();
            out = new FileOutputStream(image);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i("savePicture", image.getPath());

        return image;
    }

    public File savePicture(Bitmap bitmap, Context context) {
        String Path = Environment.getExternalStorageDirectory() + File.separator + Environment.DIRECTORY_DCIM + File.separator + "Camera" + File.separator;
        File file = new File(Path);
        if (!file.exists()) {
            file.mkdir();
        }
        String fileName = "image" + System.currentTimeMillis() + ".jpg";
        File image = new File(Path + fileName);
        if (image.exists()) {
            image.delete();
        }

        Log.i("TAG", "savePicture: ");
        FileOutputStream out;
        try {
            image.createNewFile();
            out = new FileOutputStream(image);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        sendBroadcastFile(image, fileName, context);
        Log.i("savePicture", image.getPath());

        return image;
    }

    /**
     * 插入到系统图库 并通知图库更新
     *
     * @param file
     * @param fileName
     */
    public void sendBroadcastFile(File file, String fileName, Context context) {
        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), fileName, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(file.getPath()))));
        Intent intent = new Intent("com.intent.action.CAPTURE");
        intent.putExtra("filePath", file.getPath());
        context.sendBroadcast(intent);
    }


    /*
     * bitmap转base64
     * */
    public static String bitmapToBase64(Bitmap bitmap) {
        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                baos.flush();
                baos.close();

                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static Bitmap rotateBitmap(Bitmap bmp) {
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        Bitmap rotatedBitMap = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
        return rotatedBitMap;
    }
}
