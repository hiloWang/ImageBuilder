package com.lak.imagebuilder.util.compress;

import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;
import com.lak.imagebuilder.ImageBuilder;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Checker {
  private static List<String> format = new ArrayList<>();
  private static final String JPG = "jpg";
  private static final String JPEG = "jpeg";
  private static final String PNG = "png";
  private static final String WEBP = "webp";
  private static final String GIF = "gif";

  static {
    format.add(JPG);
    format.add(JPEG);
    format.add(PNG);
    format.add(WEBP);
    format.add(GIF);
  }

  public static boolean isImage(String path) {
    if (TextUtils.isEmpty(path)) {
      return false;
    }

    String suffix = path.substring(path.lastIndexOf(".") + 1, path.length());
    return format.contains(suffix.toLowerCase());
  }

  public static boolean isJPG(String path) {
    if (TextUtils.isEmpty(path)) {
      return false;
    }

    String suffix = path.substring(path.lastIndexOf("."), path.length()).toLowerCase();
    return suffix.contains(JPG) || suffix.contains(JPEG);
  }

  public static String checkSuffix(String path) {
    if (TextUtils.isEmpty(path)) {
      return ".jpg";
    }

    return path.substring(path.lastIndexOf("."), path.length());
  }

  public static boolean isNeedCompress(int leastCompressSize, String path) {
    if (leastCompressSize > 0) {
      File source = new File(path);
      if (!source.exists()) {
        return false;
      }

      if (source.length() <= (leastCompressSize << 10)) {
        return false;
      }
    }
    return true;
  }

  /**
   * 打印压缩前后大小
   *
   * @param originImg 原始图片File
   * @param compressFile 压缩后的图片File
   */
  public static void printLogAboutCompressFileSize(File originImg, File compressFile) {
    int[] originSize = computeSize(originImg.getAbsolutePath());
    int[] thumbSize = computeSize(compressFile.getAbsolutePath());
    String originArg = String.format(Locale.CHINA, "原图参数：%d*%d, %dk", originSize[0], originSize[1],
        originImg.length() >> 10);
    String thumbArg = String.format(Locale.CHINA, "压缩后参数：%d*%d, %dk", thumbSize[0], thumbSize[1],
        compressFile.length() >> 10);
    Log.d(ImageBuilder.TAG, originArg + "\n" + thumbArg);
  }

  /**
   * 计算压缩前后大小
   */
  public static int[] computeSize(String srcImg) {
    int[] size = new int[2];

    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;
    options.inSampleSize = 1;

    BitmapFactory.decodeFile(srcImg, options);
    size[0] = options.outWidth;
    size[1] = options.outHeight;

    return size;
  }
}
