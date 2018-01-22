package com.lak.imagebuilder.ui;

import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import com.lak.imagebuilder.ImageBuilder;
import com.lak.imagebuilder.bean.ImageItem;
import com.lak.imagebuilder.core.ImageBuilderConfig;
import com.lak.imagebuilder.listener.ActivityResultListener;
import com.lak.imagebuilder.util.compress.Checker;
import com.lak.imagebuilder.util.compress.ImgCompress;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author bo
 * @e-mail wangwb@13322.com
 * @time 2018/01/18
 * @desc activity结果代理fragment
 * @version:
 */
public class ProxyFragmentResult extends Fragment {

  public static final String TAG_ACTIVITY_RESULT_FRAGMENT = "tag_activity_result_fragment";

  private ActivityResultListener activityResultListener;

  public ProxyFragmentResult() {
  }

  public void setActivityResultListener(ActivityResultListener activityResultListener) {
    this.activityResultListener = activityResultListener;
  }

  @Override public void onActivityResult(int requestCode, final int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (data != null) {
      final ArrayList<ImageItem> originImages =
          (ArrayList<ImageItem>) data.getSerializableExtra(ImageBuilder.EXTRA_RESULT_ITEMS_ORIGIN);
      final ArrayList<String> compressSelectedImagesOnlyPath =
          data.getStringArrayListExtra(ImageBuilder.EXTRA_RESULT_ITEMS_COMPRESS);
      // 是否显示原图 默认为false压缩原图
      boolean isOriginImage =
          data.getBooleanExtra(ImageBuilderConfig.EXTRA_FROM_ORIGIN_IMAGE, false);
      final boolean isShowCompressLog =
          data.getBooleanExtra(ImageBuilderConfig.EXTRA_IS_SHOW_COMPRESS_IMAGE_SIZE_LOG, false);

      if (originImages != null && originImages.size() > 0) {
        if (!isOriginImage && compressSelectedImagesOnlyPath != null) {
          // 压缩
          AsyncTask.execute(new Runnable() {
            @Override public void run() {
              try {
                List<File> files =
                    ImgCompress.with(getActivity()).load(compressSelectedImagesOnlyPath).get();
                compressSelectedImagesOnlyPath.clear();
                for (int i = 0; i < files.size(); i++) {
                  File compressFile = files.get(i);
                  if (isShowCompressLog) {
                    String originImg = originImages.get(i).path;
                    Checker.printLogAboutCompressFileSize(new File(originImg), compressFile);
                  }
                  compressSelectedImagesOnlyPath.add(compressFile.getAbsolutePath());
                }

                getActivity().runOnUiThread(new Runnable() {
                  @Override public void run() {
                    if (activityResultListener != null) {
                      activityResultListener.onActivityResult(originImages,
                          compressSelectedImagesOnlyPath, resultCode, true);
                    }
                  }
                });
              } catch (IOException e) {
                e.printStackTrace();
              }
            }
          });
        } else {
          if (activityResultListener != null) {
            activityResultListener.onActivityResult(originImages, compressSelectedImagesOnlyPath, resultCode, false);
          }
        }
      }
    }
  }
}
