package com.lak.imagebuilder.ui;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.lak.imagebuilder.R;
import com.lak.imagebuilder.adapter.ImagePageAdapter;
import com.lak.imagebuilder.bean.ImageItem;
import com.lak.imagebuilder.core.DataHolder;
import com.lak.imagebuilder.core.ImageBuilderConfig;
import com.lak.imagebuilder.util.Utils;
import com.lak.imagebuilder.util.imageloader.GlideImageLoader;
import com.lak.imagebuilder.widget.ViewPagerFixed;
import java.util.ArrayList;

public abstract class ImagePreviewBaseActivity extends BaseActivity {

  protected ImageBuilderConfig imageBuilderConfig;
  protected ArrayList<ImageItem> mImageItems;      //跳转进ImagePreviewFragment的图片文件夹
  protected int mCurrentPosition = 0;              //跳转进ImagePreviewFragment时的序号，第几个图片
  protected TextView mTitleCount;                  //显示当前图片的位置  例如  5/31
  protected ArrayList<ImageItem> selectedImages;   //所有已经选中的图片
  protected View content;
  protected View topBar;
  protected RelativeLayout checkboxContainer;
  protected ViewPagerFixed mViewPager;
  protected ImagePageAdapter mAdapter;
  protected boolean isFromWeakReferenceItems, isShowBottomBar, isShowTopBar;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.__lakimage_activity_image_preview);

    imageBuilderConfig = ImageBuilderConfig.getInstance();

    mCurrentPosition = getIntent().getIntExtra(ImageBuilderConfig.EXTRA_SELECTED_IMAGE_POSITION, 0);
    isFromWeakReferenceItems =
        getIntent().getBooleanExtra(ImageBuilderConfig.EXTRA_FROM_WEAK_REFERENCE_ITEMS, true);
    isShowBottomBar =
        getIntent().getBooleanExtra(ImageBuilderConfig.EXTRA_FROM_SHOW_BOTTOM_BAR, true);
    isShowTopBar = getIntent().getBooleanExtra(ImageBuilderConfig.EXTRA_FROM_SHOW_TOP_BAR, true);

    if (isFromWeakReferenceItems) {
      // 默认方式：采用弱引用方式, 仅限于选择照片时启用，为了处理大量照片可能导致崩溃的问题。
      mImageItems = (ArrayList<ImageItem>) DataHolder.getInstance()
          .retrieve(DataHolder.DH_CURRENT_IMAGE_FOLDER_ITEMS);
    } else {
      // 仅限回显图片时启动
      mImageItems = (ArrayList<ImageItem>) getIntent().getSerializableExtra(
          ImageBuilderConfig.EXTRA_RESULT_ITEMS_ORIGIN);

      // 当通过其他途径删除图片时，用户此时会传进来新的已选中图片的集合，这时需要重新设置已选中图片的，保证同步。
      if (mImageItems != null && mImageItems.size() > 0) {
        ArrayList<String> compressSelectedImages = new ArrayList();
        for (ImageItem imageItem : mImageItems) {
          compressSelectedImages.add(imageItem.path);
        }
        imageBuilderConfig.setSelectedImages(mImageItems);
        imageBuilderConfig.setCompressSelectedImages(compressSelectedImages);
      }
    }

    // 插件化时会为null
    if (imageBuilderConfig.getImageLoader() == null) {
      imageBuilderConfig.setImageLoader(new GlideImageLoader());
    }
    boolean isShowCompressImageSizeLog =
        getIntent().getBooleanExtra(ImageBuilderConfig.EXTRA_IS_SHOW_COMPRESS_IMAGE_SIZE_LOG,
            false);
    imageBuilderConfig.setIsShowCompressImageSizeLog(isShowCompressImageSizeLog);
    selectedImages = imageBuilderConfig.getSelectedImages();

    //初始化控件
    content = findViewById(R.id.content);

    //因为状态栏透明后，布局整体会上移，所以给头部加上状态栏的margin值，保证头部不会被覆盖
    topBar = findViewById(R.id.top_bar);
    checkboxContainer = (RelativeLayout) findViewById(R.id.checkbox_container);
    if (!isShowTopBar) {
      // 相册选择
      topBar.setVisibility(View.GONE);
      mTitleCount.setVisibility(View.VISIBLE);
      checkboxContainer.setVisibility(View.VISIBLE);
    } else {
      // 查看大图
      topBar.setVisibility(View.VISIBLE);
      mTitleCount.setVisibility(View.GONE);
      checkboxContainer.setVisibility(View.GONE);
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) topBar.getLayoutParams();
      params.topMargin = Utils.getStatusHeight(this);
      topBar.setLayoutParams(params);
    }
    topBar.findViewById(R.id.btn_ok).setVisibility(View.GONE);
    topBar.findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        finish();
      }
    });

    mTitleCount = (TextView) findViewById(R.id.tv_des);

    mViewPager = (ViewPagerFixed) findViewById(R.id.viewpager);
    mAdapter = new ImagePageAdapter(this, mImageItems);
    mAdapter.setPhotoViewClickListener(new ImagePageAdapter.PhotoViewClickListener() {
      @Override public void OnPhotoTapListener(View view, float v, float v1) {
        onImageSingleTap();
      }
    });
    mViewPager.setAdapter(mAdapter);
    mViewPager.setCurrentItem(mCurrentPosition, false);

    //初始化当前页面的状态
    mTitleCount.setText(getString(R.string.__lakimage_preview_image_count, mCurrentPosition + 1,
        mImageItems.size()));
  }

  /** 单击时，隐藏头和尾 */
  public abstract void onImageSingleTap();

  @Override protected void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    ImageBuilderConfig.getInstance().restoreInstanceState(savedInstanceState);
  }

  @Override protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    ImageBuilderConfig.getInstance().saveInstanceState(outState);
  }
}