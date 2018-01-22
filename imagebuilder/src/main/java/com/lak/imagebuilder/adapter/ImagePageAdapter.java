package com.lak.imagebuilder.adapter;

import android.app.Activity;
import android.support.v4.view.PagerAdapter;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.PhotoView;
import com.lak.imagebuilder.core.ImageBuilderConfig;
import com.lak.imagebuilder.bean.ImageItem;
import com.lak.imagebuilder.util.Utils;
import java.util.ArrayList;

public class ImagePageAdapter extends PagerAdapter {

  private int screenWidth;
  private int screenHeight;
  private ImageBuilderConfig imageBuilderConfig;
  private ArrayList<ImageItem> images = new ArrayList<>();
  private Activity mActivity;
  public PhotoViewClickListener listener;

  public ImagePageAdapter(Activity activity, ArrayList<ImageItem> images) {
    this.mActivity = activity;
    this.images = images;

    DisplayMetrics dm = Utils.getScreenPix(activity);
    screenWidth = dm.widthPixels;
    screenHeight = dm.heightPixels;
    imageBuilderConfig = ImageBuilderConfig.getInstance();
  }

  public void setData(ArrayList<ImageItem> images) {
    this.images = images;
  }

  public void setPhotoViewClickListener(PhotoViewClickListener listener) {
    this.listener = listener;
  }

  @Override public Object instantiateItem(ViewGroup container, int position) {
    PhotoView photoView = new PhotoView(mActivity);
    ImageItem imageItem = images.get(position);
    imageBuilderConfig.getImageLoader()
        .displayImagePreview(mActivity, imageItem.path, photoView, screenWidth, screenHeight);

    photoView.setOnPhotoTapListener(new OnPhotoTapListener() {
      @Override public void onPhotoTap(ImageView view, float x, float y) {
        if (listener != null) listener.OnPhotoTapListener(view, x, y);
      }
    });
    container.addView(photoView);
    return photoView;
  }

  @Override public int getCount() {
    return images.size();
  }

  @Override public boolean isViewFromObject(View view, Object object) {
    return view == object;
  }

  @Override public void destroyItem(ViewGroup container, int position, Object object) {
    container.removeView((View) object);
  }

  @Override public int getItemPosition(Object object) {
    return POSITION_NONE;
  }

  public interface PhotoViewClickListener {
    void OnPhotoTapListener(View view, float v, float v1);
  }
}
