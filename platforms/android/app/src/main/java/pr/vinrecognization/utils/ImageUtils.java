package pr.vinrecognization.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import java.io.InputStream;

/**
 * Software:
 * Version: 1.0.0
 * Company: eastime
 *
 * @author LMM
 * @time:2021/1/14 17
 */
public class ImageUtils {
    public static Bitmap getBitmap(Context context, int resId, int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        InputStream stream = context.getResources().openRawResource(resId);
        Bitmap bitmap = BitmapFactory.decodeStream(stream, null, options);
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Matrix matrix = new Matrix();
        float scale = (float) width / w;
        matrix.postScale(scale, scale);
        return Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);

    }
}
