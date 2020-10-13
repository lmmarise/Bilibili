package org.tsb.bilibili.util.image;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.Display;
import android.view.WindowManager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import static android.content.Context.WINDOW_SERVICE;

public class ImageUtil {
    private int screenWidth = 0;
    private int screenHeight = 0;

    public ImageUtil(Context context) {
        // context的方法，获取windowManager
        WindowManager windowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);
        // 获取屏幕对象
        Display defaultDisplay = windowManager.getDefaultDisplay();
        // 获取屏幕的宽、高，单位是像素
        // 获取画布中间方框的宽度和高度
        screenWidth = defaultDisplay.getWidth();
        screenHeight = defaultDisplay.getHeight();
    }

    /**
     * 将 Bitmap 转为 InputStream
     */
    public static InputStream bitmap2InputStream(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return new ByteArrayInputStream(baos.toByteArray());
    }

    /**
     * 裁剪图片
     */
    public Bitmap cutPicFitOnScreen(Bitmap bm) {
        Bitmap bitmap = null;
        if (bm != null) {
            // 将图片按照屏幕比例裁剪
            int x = (bm.getWidth() - screenWidth) / 2;
            float rate = (float) screenWidth / screenHeight;
            bitmap = Bitmap.createBitmap(bm, x, 0, (int) (rate * bm.getHeight()), bm.getHeight());
        }
        return bitmap;
    }

    /**
     * 将指定路径的图片文件转为Bitmap
     */
    public Bitmap loadBitmap(String picPath) throws FileNotFoundException {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;
        Bitmap bitmap;
        // 获取到这个图片的原始宽度和高度
        int picWidth = opt.outWidth;
        int picHeight = opt.outHeight;
        // isSampleSize是表示对图片的缩放程度，比如值为2图片的宽度和高度都变为以前的1/2
        opt.inSampleSize = 1;
        // 根据屏的大小和图片大小计算出缩放比例
        if (picWidth > picHeight) {
            if (picWidth > screenWidth)
                opt.inSampleSize = picWidth / screenWidth;
        } else {
            if (picHeight > screenHeight)
                opt.inSampleSize = picHeight / screenHeight;
        }
        // 生成有像素经过缩放了的bitmap
        opt.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeFile(picPath, opt);
        if (bitmap == null) {
            throw new FileNotFoundException("Couldn't open " + picPath);
        }
        return bitmap;
    }

    /**
     * 资源文件转化为bitmap
     */
    public static Bitmap res2Bitmap(Context context, int image_res_id) {
        Resources res = context.getResources();
        return BitmapFactory.decodeResource(res, image_res_id, null);
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res,
                                                         int resId,
                                                         int reqWidth,
                                                         int reqHeight) {
        // 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);
        // 调用上面定义的方法计算inSampleSize值
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        // 使用获取到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    /**
     * 计算出一个inSampleSize大小当然这个大小不是越大越好，这里是根据你的imageView来进行相对比例的放缩
     */
    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth,
                                            int reqHeight) {
        // 源图片的高度和宽度
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;   // 数字越大分辨率越小  # 宽高等比列缩放
        if (height > reqHeight || width > reqWidth) {
            // 计算出实际宽高和目标宽高的比率
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            // 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
            // 一定都会大于等于目标的宽和高。
            inSampleSize = Math.min(heightRatio, widthRatio);
        }
        return inSampleSize;
    }

    /**
     * 图片转成string
     */
    public static String convertIconToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();       // outputstream
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] appicon = baos.toByteArray();// 转为byte数组
        return Base64.encodeToString(appicon, Base64.DEFAULT);
    }

    /**
     * string转成bitmap
     */
    public static Bitmap convertStringToIcon(String st) {
        // OutputStream out;
        Bitmap bitmap = null;
        try {
            // out = new FileOutputStream("/sdcard/aa.jpg");
            byte[] bitmapArray;
            bitmapArray = Base64.decode(st, Base64.DEFAULT);
            bitmap =
                    BitmapFactory.decodeByteArray(bitmapArray, 0,
                            bitmapArray.length);
            // bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }
}
