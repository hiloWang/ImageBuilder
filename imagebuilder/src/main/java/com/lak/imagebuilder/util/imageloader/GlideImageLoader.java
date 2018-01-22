package com.lak.imagebuilder.util.imageloader;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.webkit.URLUtil;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.lak.imagebuilder.R;
import com.lak.imagebuilder.listener.ImageLoader;
import java.io.File;

public class GlideImageLoader implements ImageLoader {

  public GlideImageLoader() {
  }

  @Override public void displayImage(Activity activity, String path, ImageView imageView, int width,
      int height) {

    final RequestOptions options = new RequestOptions();
    final RequestBuilder<Drawable> load;

    options.centerCrop()
        .dontAnimate()
        .override(width, height)
        //.priority(Priority.HIGH)
        .placeholder(R.drawable.__lakimage_ic_default_image)
        .error(R.drawable.__lakimage_ic_default_image)
        .diskCacheStrategy(DiskCacheStrategy.NONE);

    if (URLUtil.isFileUrl(path)) {
      load = Glide.with(activity).load(new File(path));
    } else {
      // 设置图片路径(文件名包含%符号 无法识别和显示)
      load = Glide.with(activity).load(path);
    }

    load.apply(options)
        .thumbnail(0.5f)
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(imageView);
  }

  @Override
  public void displayImagePreview(Activity activity, String path, ImageView imageView, int width,
      int height) {

    final RequestOptions options = new RequestOptions();
    final RequestBuilder<Drawable> load;

    options.dontAnimate().diskCacheStrategy(DiskCacheStrategy.ALL);

    if (URLUtil.isFileUrl(path)) {
      load = Glide.with(activity).load(new File(path));
    } else {
      // 设置图片路径(文件名包含%符号 无法识别和显示)
      load = Glide.with(activity).load(path);
    }

    load.apply(options).transition(DrawableTransitionOptions.withCrossFade()).into(imageView);
  }

  @Override public void clearMemoryCache() {
  }
}
