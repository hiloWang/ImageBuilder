package com.lak.imagebuilder;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import com.lak.imagebuilder.bean.ImageItem;
import com.lak.imagebuilder.core.ImageBuilderConfig;
import com.lak.imagebuilder.listener.ActivityResultListener;
import com.lak.imagebuilder.listener.ImageLoader;
import com.lak.imagebuilder.ui.ImageGridActivity;
import com.lak.imagebuilder.ui.ImagePreviewActivity;
import com.lak.imagebuilder.ui.ImagePreviewDelActivity;
import com.lak.imagebuilder.ui.ProxyFragmentResult;
import com.lak.imagebuilder.util.PermissionsUtils;
import com.lak.imagebuilder.util.imageloader.GlideImageLoader;
import java.util.ArrayList;

import static com.lak.imagebuilder.ui.ProxyFragmentResult.TAG_ACTIVITY_RESULT_FRAGMENT;

public class ImageBuilder {

  public static final String TAG = ImageBuilder.class.getSimpleName();

  private static final int REQUEST_CODE_SELETED_IMAGE = 0x110;
  private static final int REQUEST_CODE_PREVIEW = 0x111;
  public static final int RESULT_CODE_SELECTED_IMAGE = ImageBuilderConfig.RESULT_CODE_ITEMS;
  public static final int RESULT_CODE_PREVIEW_IMAGE = ImageBuilderConfig.RESULT_CODE_BACK;

  public final static int DEFAULT_MAX_COUNT = 9;
  public final static int DEFAULT_COLUMN_NUMBER = -1;

  public static final String EXTRA_RESULT_ITEMS_ORIGIN =
      ImageBuilderConfig.EXTRA_RESULT_ITEMS_ORIGIN;
  public static final String EXTRA_RESULT_ITEMS_COMPRESS =
      ImageBuilderConfig.EXTRA_RESULT_ITEMS_COMPRESS;
  public static final String EXTRA_SELECTED_IMAGE_POSITION =
      ImageBuilderConfig.EXTRA_SELECTED_IMAGE_POSITION;
  public static final String EXTRA_FROM_WEAK_REFERENCE_ITEMS =
      ImageBuilderConfig.EXTRA_FROM_WEAK_REFERENCE_ITEMS;
  public static final String EXTRA_IS_SHOW_COMPRESS_IMAGE_SIZE_LOG =
      ImageBuilderConfig.EXTRA_IS_SHOW_COMPRESS_IMAGE_SIZE_LOG;
  public static final String EXTRA_IS_SHOW_ORIGIN_IMAGE_CHECKBOX =
      ImageBuilderConfig.EXTRA_IS_SHOW_ORIGIN_IMAGE_CHECKBOX;

  public final static String EXTRA_MAX_COUNT = "MAX_COUNT";
  public final static String EXTRA_SHOW_CAMERA = "SHOW_CAMERA";
  public final static String EXTRA_CROP_ENABLE = "CROP_ENABLE";
  public final static String EXTRA_RECTANGLE = "RECTANGLE";
  public final static String EXTRA_GRID_COLUMN = "column";
  public final static String EXTRA_ORIGINAL_PHOTOS = "ORIGINAL_PHOTOS";
  public final static String EXTRA_PREVIEW_ENABLED = "PREVIEW_ENABLED";
  public final static String EXTRA_MULTI_MODE = "MULTI_MODE";

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private Bundle optionsBundle;
    private Intent pickerIntent;
    private ImageLoader imageLoader;

    public Builder() {
      optionsBundle = new Bundle();
      pickerIntent = new Intent();
    }

    /**
     * Send the crop Intent from an Activity
     *
     * @param activity Activity to receive result
     */
    public void start(@NonNull Activity activity,
        @NonNull ActivityResultListener activityResultListener) {
      start(activity, REQUEST_CODE_SELETED_IMAGE, activityResultListener);
    }

    /**
     * Send the Intent from an Activity with a custom request code
     *
     * @param activity Activity to receive result
     * @param requestCode requestCode for result
     */
    public void start(@NonNull Activity activity, int requestCode,
        @NonNull ActivityResultListener activityResultListener) {
      if (PermissionsUtils.checkReadStoragePermission(activity)) {
        getActivityResultFragment(activity, activityResultListener).startActivityForResult(
            getIntent(activity), requestCode);
      }
    }

    /**
     * @param requestCode requestCode for result
     */
    public void start(@NonNull Fragment fragment, int requestCode,
        @NonNull ActivityResultListener activityResultListener) {
      start(fragment.getActivity(), requestCode, activityResultListener);
    }

    /**
     * Send the Intent with a custom request code
     *
     * @param fragment Fragment to receive result
     */
    public void start(@NonNull Fragment fragment,
        @NonNull ActivityResultListener activityResultListener) {
      start(fragment.getActivity(), REQUEST_CODE_SELETED_IMAGE, activityResultListener);
    }

    public void startPreviewActivity(@NonNull Activity activity,
        @NonNull ArrayList<ImageItem> originImages, int selectedImagePosition,
        @NonNull ActivityResultListener activityResultListener) {
      startPreviewActivity(activity, originImages, selectedImagePosition, REQUEST_CODE_PREVIEW,
          activityResultListener);
    }

    public void startPreviewActivity(@NonNull Activity activity,
        @NonNull ArrayList<ImageItem> originImages, int selectedImagePosition,
        boolean showDelButton, ActivityResultListener activityResultListener) {
      startPreviewActivity(activity, originImages, selectedImagePosition, REQUEST_CODE_PREVIEW,
          showDelButton, activityResultListener);
    }

    public void startPreviewActivity(@NonNull Activity activity,
        @NonNull ArrayList<ImageItem> originImages, int selectedImagePosition) {
      startPreviewActivity(activity, originImages, selectedImagePosition, REQUEST_CODE_PREVIEW,
          false, null);
    }

    public void startPreviewActivity(@NonNull Activity activity,
        @NonNull ArrayList<ImageItem> originImages, int selectedImagePosition, int requestCode,
        boolean showDelButton, @NonNull ActivityResultListener activityResultListener) {
      initializedImageLoader();

      pickerIntent.putExtra(ImageBuilder.EXTRA_RESULT_ITEMS_ORIGIN, originImages);
      pickerIntent.putExtra(ImageBuilder.EXTRA_MAX_COUNT, originImages.size());
      pickerIntent.putExtra(ImageBuilder.EXTRA_SELECTED_IMAGE_POSITION, selectedImagePosition);
      pickerIntent.putExtra(ImageBuilder.EXTRA_FROM_WEAK_REFERENCE_ITEMS, false);
      pickerIntent.putExtra(ImageBuilderConfig.EXTRA_FROM_SHOW_BOTTOM_BAR, false);
      if (activityResultListener != null) {
        pickerIntent.putExtra(ImageBuilderConfig.EXTRA_FROM_SHOW_TOP_BAR, false);
        if (showDelButton) {
          pickerIntent.setClass(activity, ImagePreviewDelActivity.class);
          getActivityResultFragment(activity, activityResultListener).startActivityForResult(
              pickerIntent, requestCode);
        } else {
          pickerIntent.setClass(activity, ImagePreviewActivity.class);
          getActivityResultFragment(activity, activityResultListener).startActivityForResult(
              pickerIntent, requestCode);
        }
      } else {
        if (showDelButton) {
          pickerIntent.setClass(activity, ImagePreviewDelActivity.class);
          activity.startActivity(pickerIntent);
        } else {
          pickerIntent.setClass(activity, ImagePreviewActivity.class);
          activity.startActivity(pickerIntent);
        }
      }
    }

    public void startPreviewActivity(@NonNull Activity activity,
        @NonNull ArrayList<ImageItem> originImages, int selectedImagePosition, int requestCode,
        @NonNull ActivityResultListener activityResultListener) {
      startPreviewActivity(activity, originImages, selectedImagePosition, requestCode, false,
          activityResultListener);
    }

    /**
     * Get Intent to start {@link ImageGridActivity}
     *
     * @return Intent for {@link ImageGridActivity}
     */
    public Intent getIntent(@NonNull Activity activity) {

      initializedImageLoader();

      pickerIntent.setClass(activity, ImageGridActivity.class);
      pickerIntent.putExtras(optionsBundle);
      return pickerIntent;
    }

    /**
     * 设置ImageLoad
     *
     * @param imageLoader glide、picasso
     */
    public Builder imageLoader(ImageLoader imageLoader) {
      this.imageLoader = imageLoader;
      return this;
    }

    public Builder showOriginImageCheckbox(boolean isShowOriginImageCheckbox) {
      optionsBundle.putBoolean(EXTRA_IS_SHOW_ORIGIN_IMAGE_CHECKBOX, isShowOriginImageCheckbox);
      return this;
    }

    /**
     * 最多显示图片的数量
     *
     * @param photoCount 图片数量
     */
    public Builder photoCount(int photoCount) {
      optionsBundle.putInt(EXTRA_MAX_COUNT, photoCount);
      return this;
    }

    /**
     * gridview显示的几列
     *
     * @param columnCount 列数
     */
    public Builder gridColumnCount(int columnCount) {
      optionsBundle.putInt(EXTRA_GRID_COLUMN, columnCount);
      return this;
    }

    /**
     * 是否显示压缩图片前后大小的日执行信息
     */
    public Builder showCompressImageSizeLog(boolean isShowCompressImageSizeLog) {
      optionsBundle.putBoolean(EXTRA_IS_SHOW_COMPRESS_IMAGE_SIZE_LOG, isShowCompressImageSizeLog);
      return this;
    }

    /**
     * 拍照按钮
     *
     * @param showCamera true 显示 false不显示
     */
    public Builder showCamera(boolean showCamera) {
      optionsBundle.putBoolean(EXTRA_SHOW_CAMERA, showCamera);
      return this;
    }

    /**
     * 裁剪图片
     *
     * @param cropEnable true 支持裁剪 false不支持裁剪
     */
    public Builder cropEnable(boolean cropEnable) {
      optionsBundle.putBoolean(EXTRA_CROP_ENABLE, cropEnable);
      return this;
    }

    /**
     * 是否按矩形区域保存裁剪图片
     *
     * @param rectangle true 是 false 否
     */
    public Builder rectangle(boolean rectangle) {
      optionsBundle.putBoolean(EXTRA_RECTANGLE, rectangle);
      return this;
    }

    /**
     * 已选择图片
     *
     * @param imagesUri 已选择图片的集合
     */
    public Builder selected(ArrayList<ImageItem> imagesUri) {
      optionsBundle.putSerializable(EXTRA_ORIGINAL_PHOTOS, imagesUri);
      return this;
    }

    /**
     * 预览大图
     *
     * @param previewEnabled true可预览 false不可预览
     */
    public Builder previewEnabled(boolean previewEnabled) {
      optionsBundle.putBoolean(EXTRA_PREVIEW_ENABLED, previewEnabled);
      return this;
    }

    public Builder multiMode(boolean multiMode) {
      optionsBundle.putBoolean(EXTRA_MULTI_MODE, multiMode);
      return this;
    }

    /**
     * 通过TAG查找ActivityResultFragment
     */
    private Fragment findActivityResultFragment(Activity activity) {
      return activity.getFragmentManager().findFragmentByTag(TAG_ACTIVITY_RESULT_FRAGMENT);
    }

    private ProxyFragmentResult getActivityResultFragment(Activity activity,
        ActivityResultListener activityResultListener) {
      Fragment activityResultFragment = findActivityResultFragment(activity);

      if (activityResultFragment == null) {
        FragmentManager fragmentManager = activity.getFragmentManager();
        activityResultFragment = new ProxyFragmentResult();
        fragmentManager.beginTransaction()
            .add(activityResultFragment, TAG_ACTIVITY_RESULT_FRAGMENT)
            .commitAllowingStateLoss();
        fragmentManager.executePendingTransactions();
      }
      ((ProxyFragmentResult) activityResultFragment).setActivityResultListener(
          activityResultListener);

      return (ProxyFragmentResult) activityResultFragment;
    }

    /**
     * 如果是插件化必须自己提供ImageLoader不可以使用默认提供的PicassoImageLoader，
     * 会失效，原因：会造成ImageBuilderConfig.java单例类存在多份。
     */
    private void initializedImageLoader() {
      ImageBuilderConfig imageBuilderConfig = ImageBuilderConfig.getInstance();
      if (imageLoader != null) {
        imageBuilderConfig.setImageLoader(imageLoader);
      } else {
        imageBuilderConfig.setImageLoader(new GlideImageLoader());
      }
    }
  }
}
