package pr.platerecognization;


import android.graphics.Bitmap;

/**
 * @author liuxuhui
 * @date 2019/6/20
 */
public class PlateInfo {

    /**
     * 车牌号
     */
    public String plateName;

    /**
     * 车牌号图片
     */
    public Bitmap bitmap;
    public String typeName;
    //置信度
    public double confidence;

    public String base64;
    public String imagePath;

    public PlateInfo() {
    }

    public PlateInfo(String plateName, Bitmap bitmap) {
        this.plateName = plateName;
        this.bitmap = bitmap;
    }
}
