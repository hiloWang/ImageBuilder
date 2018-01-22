package com.lak.imagebuilder.listener;

import com.lak.imagebuilder.bean.ImageItem;
import java.util.ArrayList;

/**
 * @author bo
 * @e-mail wangwb@13322.com
 * @time 2018/01/18
 * @desc
 * @version:
 */
public interface ActivityResultListener {

  /**
   * 图片回调
   *
   * @param originImagesPath 原始图片的路径（一般用于回显，ImageItem包含了很多图片的信息）
   * @param compressImagesPath 压缩后图片的路径
   * @param resultCode 结果码
   * @param isCompress 是否压缩
   */
  void onActivityResult(ArrayList<ImageItem> originImagesPath, ArrayList<String> compressImagesPath,
      int resultCode, boolean isCompress);
}
