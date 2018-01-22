package com.lak.imagebuilder.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import com.lak.imagebuilder.R;
import com.lak.imagebuilder.adapter.ImageFolderAdapter;
import com.lak.imagebuilder.adapter.ImageRecyclerAdapter;
import com.lak.imagebuilder.bean.ImageFolder;
import com.lak.imagebuilder.bean.ImageItem;
import com.lak.imagebuilder.core.DataHolder;
import com.lak.imagebuilder.core.ImageBuilderConfig;
import com.lak.imagebuilder.core.ImageDataSource;
import com.lak.imagebuilder.listener.OnImageItemClickListener;
import com.lak.imagebuilder.util.Utils;
import com.lak.imagebuilder.util.imageloader.GlideImageLoader;
import com.lak.imagebuilder.widget.FolderPopUpWindow;
import com.lak.imagebuilder.widget.GridSpacingItemDecoration;
import java.util.ArrayList;
import java.util.List;

import static com.lak.imagebuilder.ImageBuilder.DEFAULT_COLUMN_NUMBER;
import static com.lak.imagebuilder.ImageBuilder.DEFAULT_MAX_COUNT;
import static com.lak.imagebuilder.ImageBuilder.EXTRA_CROP_ENABLE;
import static com.lak.imagebuilder.ImageBuilder.EXTRA_GRID_COLUMN;
import static com.lak.imagebuilder.ImageBuilder.EXTRA_MAX_COUNT;
import static com.lak.imagebuilder.ImageBuilder.EXTRA_MULTI_MODE;
import static com.lak.imagebuilder.ImageBuilder.EXTRA_ORIGINAL_PHOTOS;
import static com.lak.imagebuilder.ImageBuilder.EXTRA_PREVIEW_ENABLED;
import static com.lak.imagebuilder.ImageBuilder.EXTRA_RECTANGLE;
import static com.lak.imagebuilder.ImageBuilder.EXTRA_SHOW_CAMERA;

public class ImageGridActivity extends BaseActivity
    implements ImageDataSource.OnImagesLoadedListener, OnImageItemClickListener,
    ImageBuilderConfig.OnImageSelectedListener, View.OnClickListener {

  public static final int REQUEST_PERMISSION_STORAGE = 0x01;
  public static final int REQUEST_PERMISSION_CAMERA = 0x02;
  public static final String EXTRAS_TAKE_PICKERS = "TAKE";

  private ImageBuilderConfig imageBuilderConfig;

  private boolean isOrigin = false;  // 是否选中原图
  private View mFooterBar;     // 底部栏
  private Button mBtnOk;       // 确定按钮
  private AppCompatCheckBox mCbCheckOriginImageStatu; // 是否显示原图
  private View mllDir; // 文件夹切换按钮
  private TextView mtvDir; // 显示当前文件夹
  private TextView mBtnPre;      // 预览按钮
  private ImageFolderAdapter mImageFolderAdapter;    // 图片文件夹的适配器
  private FolderPopUpWindow mFolderPopupWindow;  // ImageSet的PopupWindow
  private List<ImageFolder> mImageFolders;   // 所有的图片文件夹
  private boolean directPhoto = false; // 默认不是直接调取相机
  private RecyclerView mRecyclerView;
  private ImageRecyclerAdapter mRecyclerAdapter;

  @Override protected void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    directPhoto = savedInstanceState.getBoolean(EXTRAS_TAKE_PICKERS, false);
  }

  @Override protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putBoolean(EXTRAS_TAKE_PICKERS, directPhoto);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.__lakimage_activity_image_grid);

    imageBuilderConfig = ImageBuilderConfig.getInstance();
    imageBuilderConfig.clear();
    imageBuilderConfig.addOnImageSelectedListener(this);

    Intent data = getIntent();

    // 插件化时会为null
    if (imageBuilderConfig.getImageLoader() == null) {
      imageBuilderConfig.setImageLoader(new GlideImageLoader());
    }
    int maxCount = data.getIntExtra(EXTRA_MAX_COUNT, DEFAULT_MAX_COUNT);
    imageBuilderConfig.setSelectLimit(maxCount);
    boolean showCamera = data.getBooleanExtra(EXTRA_SHOW_CAMERA, true);
    imageBuilderConfig.setShowCamera(showCamera);
    boolean cropEnable = data.getBooleanExtra(EXTRA_CROP_ENABLE, false);
    imageBuilderConfig.setCrop(cropEnable);
    boolean trctangle = data.getBooleanExtra(EXTRA_RECTANGLE, false);
    imageBuilderConfig.setSaveRectangle(trctangle);
    int columnNumber = data.getIntExtra(EXTRA_GRID_COLUMN, DEFAULT_COLUMN_NUMBER);
    imageBuilderConfig.setGridColumn(columnNumber);
    boolean previewEnabled = data.getBooleanExtra(EXTRA_PREVIEW_ENABLED, true);
    imageBuilderConfig.setPreviewEnabled(previewEnabled);
    boolean multiMode = data.getBooleanExtra(EXTRA_MULTI_MODE, true);
    imageBuilderConfig.setMultiMode(multiMode);
    boolean isShowCompressImageSizeLog =
        getIntent().getBooleanExtra(ImageBuilderConfig.EXTRA_IS_SHOW_COMPRESS_IMAGE_SIZE_LOG,
            false);
    imageBuilderConfig.setIsShowCompressImageSizeLog(isShowCompressImageSizeLog);
    boolean isShowOriginImageCheckbox =
        getIntent().getBooleanExtra(ImageBuilderConfig.EXTRA_IS_SHOW_ORIGIN_IMAGE_CHECKBOX, false);

    // 新增可直接拍照
    if (data != null && data.getExtras() != null) {
      directPhoto = data.getBooleanExtra(EXTRAS_TAKE_PICKERS, false); // 默认不是直接打开相机
      if (directPhoto) {
        if (!(checkPermission(Manifest.permission.CAMERA))) {
          ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.CAMERA },
              ImageGridActivity.REQUEST_PERMISSION_CAMERA);
        } else {
          imageBuilderConfig.takePicture(this, ImageBuilderConfig.REQUEST_CODE_TAKE);
        }
      }

      ArrayList<ImageItem> selectedImages =
          (ArrayList<ImageItem>) data.getSerializableExtra(EXTRA_ORIGINAL_PHOTOS);

      if (selectedImages != null && selectedImages.size() > 0) {
        ArrayList<String> compressSelectedImages = new ArrayList();
        for (ImageItem imageItem : selectedImages) {
          compressSelectedImages.add(imageItem.path);
        }
        imageBuilderConfig.setSelectedImages(selectedImages);
        imageBuilderConfig.setCompressSelectedImages(compressSelectedImages);
      }
    }

    mRecyclerView = (RecyclerView) findViewById(R.id.recycler);

    findViewById(R.id.btn_back).setOnClickListener(this);
    mCbCheckOriginImageStatu = (AppCompatCheckBox) findViewById(R.id.cb_check_origin_statu);
    if (isShowOriginImageCheckbox) {
      mCbCheckOriginImageStatu.setVisibility(View.VISIBLE);
    }
    mBtnOk = (Button) findViewById(R.id.btn_ok);
    mBtnOk.setOnClickListener(this);
    mBtnPre = (TextView) findViewById(R.id.btn_preview);
    mBtnPre.setOnClickListener(this);
    mFooterBar = findViewById(R.id.footer_bar);
    mllDir = findViewById(R.id.ll_dir);
    mllDir.setOnClickListener(this);
    mtvDir = (TextView) findViewById(R.id.tv_dir);
    if (imageBuilderConfig.isMultiMode()) {
      mBtnOk.setVisibility(View.VISIBLE);
      mBtnPre.setVisibility(View.VISIBLE);
    } else {
      mBtnOk.setVisibility(View.GONE);
      mBtnPre.setVisibility(View.GONE);
    }

    mImageFolderAdapter = new ImageFolderAdapter(this, null);
    mRecyclerAdapter = new ImageRecyclerAdapter(this, null);

    onImageSelected(0, null, false);

    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
      if (checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
        new ImageDataSource(this, null, this);
      } else {
        ActivityCompat.requestPermissions(this,
            new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE },
            REQUEST_PERMISSION_STORAGE);
      }
    } else {
      new ImageDataSource(this, null, this);
    }
  }

  @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == REQUEST_PERMISSION_STORAGE) {
      if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        new ImageDataSource(this, null, this);
      } else {
        showToast("权限被禁止，无法选择本地图片");
      }
    } else if (requestCode == REQUEST_PERMISSION_CAMERA) {
      if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        imageBuilderConfig.takePicture(this, ImageBuilderConfig.REQUEST_CODE_TAKE);
      } else {
        showToast("权限被禁止，无法打开相机");
      }
    }
  }

  @Override protected void onDestroy() {
    imageBuilderConfig.removeOnImageSelectedListener(this);
    super.onDestroy();
  }

  @Override public void onClick(View v) {
    int id = v.getId();
    if (id == R.id.btn_ok) {

      Intent intent = new Intent();
      intent.putExtra(ImageBuilderConfig.EXTRA_RESULT_ITEMS_ORIGIN,
          imageBuilderConfig.getSelectedImages());
      intent.putExtra(ImageBuilderConfig.EXTRA_RESULT_ITEMS_COMPRESS,
          imageBuilderConfig.getCompressSelectedImages());
      intent.putExtra(ImageBuilderConfig.EXTRA_FROM_ORIGIN_IMAGE,
          mCbCheckOriginImageStatu.isChecked());
      intent.putExtra(ImageBuilderConfig.EXTRA_IS_SHOW_COMPRESS_IMAGE_SIZE_LOG,
          imageBuilderConfig.isShowCompressImageSizeLog());
      setResult(ImageBuilderConfig.RESULT_CODE_ITEMS, intent);  // 多选不允许裁剪裁剪，返回数据
      finish();
    } else if (id == R.id.ll_dir) {
      if (mImageFolders == null) {
        Log.i("ImageGridActivity", "您的手机没有图片");
        return;
      }
      // 点击文件夹按钮
      createPopupFolderList();
      mImageFolderAdapter.refreshData(mImageFolders);  // 刷新数据
      if (mFolderPopupWindow.isShowing()) {
        mFolderPopupWindow.dismiss();
      } else {
        mFolderPopupWindow.showAtLocation(mFooterBar, Gravity.NO_GRAVITY, 0, 0);
        // 默认选择当前选择的上一个，当目录很多时，直接定位到已选中的条目
        int index = mImageFolderAdapter.getSelectIndex();
        index = index == 0 ? index : index - 1;
        mFolderPopupWindow.setSelection(index);
      }
    } else if (id == R.id.btn_preview) {
      Intent intent = new Intent(ImageGridActivity.this, ImagePreviewActivity.class);
      intent.putExtra(ImageBuilderConfig.EXTRA_SELECTED_IMAGE_POSITION, 0);
      intent.putExtra(ImageBuilderConfig.EXTRA_RESULT_ITEMS_ORIGIN,
          imageBuilderConfig.getSelectedImages());
      intent.putExtra(ImageBuilderConfig.EXTRA_IS_SHOW_COMPRESS_IMAGE_SIZE_LOG,
          imageBuilderConfig.isShowCompressImageSizeLog());
      intent.putExtra(ImagePreviewActivity.ISORIGIN, isOrigin);
      intent.putExtra(ImageBuilderConfig.EXTRA_FROM_WEAK_REFERENCE_ITEMS, false);
      startActivityForResult(intent, ImageBuilderConfig.REQUEST_CODE_PREVIEW);
    } else if (id == R.id.btn_back) {
      // 点击返回按钮
      finish();
    }
  }

  /**
   * 创建弹出的ListView
   */
  private void createPopupFolderList() {
    mFolderPopupWindow = new FolderPopUpWindow(this, mImageFolderAdapter);
    mFolderPopupWindow.setOnItemClickListener(new FolderPopUpWindow.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        mImageFolderAdapter.setSelectIndex(position);
        imageBuilderConfig.setCurrentImageFolderPosition(position);
        mFolderPopupWindow.dismiss();
        ImageFolder imageFolder = (ImageFolder) adapterView.getAdapter().getItem(position);
        if (null != imageFolder) {
          mRecyclerAdapter.refreshData(imageFolder.images);
          mtvDir.setText(imageFolder.name);
        }
      }
    });
    mFolderPopupWindow.setMargin(mFooterBar.getHeight());
  }

  @Override public void onImagesLoaded(List<ImageFolder> imageFolders) {
    this.mImageFolders = imageFolders;
    imageBuilderConfig.setImageFolders(imageFolders);
    if (imageFolders.size() == 0) {
      mRecyclerAdapter.refreshData(null);
    } else {
      mRecyclerAdapter.refreshData(imageFolders.get(0).images);
    }
    mRecyclerAdapter.setOnImageItemClickListener(this);
    mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
    mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(3, Utils.dp2px(this, 2), false));
    mRecyclerView.setAdapter(mRecyclerAdapter);
    mImageFolderAdapter.refreshData(imageFolders);
  }

  @Override public void onImageItemClick(View view, ImageItem imageItem, int position) {
    // 根据是否有相机按钮确定位置
    position = imageBuilderConfig.isShowCamera() ? position - 1 : position;
    if (imageBuilderConfig.isMultiMode()) {

      /**
       * 2017-03-20
       *
       * 依然采用弱引用进行解决，采用单例加锁方式处理
       */

      // 据说这样会导致大量图片的时候崩溃
      //            intent.putExtra(ImageBuilderConfig.EXTRA_IMAGE_ITEMS, imageBuilderConfig.getCurrentImageFolderItems());

      // 但采用弱引用会导致预览弱引用直接返回空指针
      DataHolder.getInstance()
          .save(DataHolder.DH_CURRENT_IMAGE_FOLDER_ITEMS,
              imageBuilderConfig.getCurrentImageFolderItems());

      if (imageBuilderConfig.isPreviewEnabled()) {
        Intent intent = new Intent(ImageGridActivity.this, ImagePreviewActivity.class);
        intent.putExtra(ImageBuilderConfig.EXTRA_SELECTED_IMAGE_POSITION, position);
        intent.putExtra(ImagePreviewActivity.ISORIGIN, isOrigin);
        startActivityForResult(intent,
            ImageBuilderConfig.REQUEST_CODE_PREVIEW);  // 如果是多选，点击图片进入预览界面
      } else {
        // todo 点击图片checkbox勾选的逻辑处理
      }
    } else {
      imageBuilderConfig.clearSelectedImages();
      imageBuilderConfig.addSelectedImageItem(position,
          imageBuilderConfig.getCurrentImageFolderItems().get(position), true);
      if (imageBuilderConfig.isCrop()) {
        Intent intent = new Intent(ImageGridActivity.this, ImageCropActivity.class);
        startActivityForResult(intent, ImageBuilderConfig.REQUEST_CODE_CROP);  // 单选需要裁剪，进入裁剪界面
      } else {
        Intent intent = new Intent();
        intent.putExtra(ImageBuilderConfig.EXTRA_RESULT_ITEMS_ORIGIN,
            imageBuilderConfig.getSelectedImages());
        intent.putExtra(ImageBuilderConfig.EXTRA_IS_SHOW_COMPRESS_IMAGE_SIZE_LOG,
            imageBuilderConfig.isShowCompressImageSizeLog());
        intent.putExtra(ImageBuilderConfig.EXTRA_RESULT_ITEMS_COMPRESS,
            imageBuilderConfig.getCompressSelectedImages());
        setResult(ImageBuilderConfig.RESULT_CODE_ITEMS, intent);   // 单选不需要裁剪，返回数据
        finish();
      }
    }
  }

  @SuppressLint("StringFormatMatches") @Override
  public void onImageSelected(int position, ImageItem item, boolean isAdd) {
    if (imageBuilderConfig.getSelectImageCount() > 0) {
      mBtnOk.setText(
          getString(R.string.__lakimage_select_complete, imageBuilderConfig.getSelectImageCount(),
              imageBuilderConfig.getSelectLimit()));
      mBtnOk.setEnabled(true);
      mBtnPre.setEnabled(true);
      mBtnPre.setText(getResources().getString(R.string.__lakimage_preview_count,
          imageBuilderConfig.getSelectImageCount()));
      mBtnPre.setTextColor(ContextCompat.getColor(this, R.color.__lakimage_text_primary_inverted));
      mBtnOk.setTextColor(ContextCompat.getColor(this, R.color.__lakimage_text_primary_inverted));
    } else {
      mBtnOk.setText(getString(R.string.__lakimage_complete));
      mBtnOk.setEnabled(false);
      mBtnPre.setEnabled(false);
      mBtnPre.setText(getResources().getString(R.string.__lakimage_preview));
      mBtnPre.setTextColor(
          ContextCompat.getColor(this, R.color.__lakimage_text_secondary_inverted));
      mBtnOk.setTextColor(ContextCompat.getColor(this, R.color.__lakimage_text_secondary_inverted));
    }
    for (int i = imageBuilderConfig.isShowCamera() ? 1 : 0; i < mRecyclerAdapter.getItemCount();
        i++) {
      if (mRecyclerAdapter.getItem(i).path != null && mRecyclerAdapter.getItem(i).path.equals(
          item.path)) {
        mRecyclerAdapter.notifyItemChanged(i);
        return;
      }
    }
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (data != null && data.getExtras() != null) {
      if (resultCode == ImageBuilderConfig.RESULT_CODE_BACK) {
        isOrigin = data.getBooleanExtra(ImagePreviewActivity.ISORIGIN, false);
      } else {
        // 从拍照界面返回
        // 点击 X , 没有选择照片
        if (data.getSerializableExtra(ImageBuilderConfig.EXTRA_RESULT_ITEMS_COMPRESS) == null) {
          // 什么都不做 直接调起相机
        } else {
          // 说明是从裁剪页面过来的数据，直接返回就可以
          setResult(ImageBuilderConfig.RESULT_CODE_ITEMS, data);
        }
        finish();
      }
    } else {
      // 如果是裁剪，因为裁剪指定了存储的Uri，所以返回的data一定为null
      if (resultCode == RESULT_OK && requestCode == ImageBuilderConfig.REQUEST_CODE_TAKE) {
        // 发送广播通知图片增加了
        ImageBuilderConfig.galleryAddPic(this, imageBuilderConfig.getTakeImageFile());

        /**
         * 对机型做旋转处理
         */
        String path = imageBuilderConfig.getTakeImageFile().getAbsolutePath();

        ImageItem imageItem = new ImageItem();
        imageItem.path = path;
        imageBuilderConfig.clearSelectedImages();
        imageBuilderConfig.addSelectedImageItem(0, imageItem, true);
        if (imageBuilderConfig.isCrop()) {
          Intent intent = new Intent(ImageGridActivity.this, ImageCropActivity.class);
          startActivityForResult(intent, ImageBuilderConfig.REQUEST_CODE_CROP);  // 单选需要裁剪，进入裁剪界面
        } else {
          Intent intent = new Intent();
          intent.putExtra(ImageBuilderConfig.EXTRA_RESULT_ITEMS_ORIGIN,
              imageBuilderConfig.getSelectedImages());
          intent.putExtra(ImageBuilderConfig.EXTRA_IS_SHOW_COMPRESS_IMAGE_SIZE_LOG,
              imageBuilderConfig.isShowCompressImageSizeLog());
          intent.putExtra(ImageBuilderConfig.EXTRA_RESULT_ITEMS_COMPRESS,
              imageBuilderConfig.getCompressSelectedImages());
          setResult(ImageBuilderConfig.RESULT_CODE_ITEMS, intent);   // 单选不需要裁剪，返回数据
          finish();
        }
      } else if (directPhoto) {
        finish();
      }
    }
  }
}