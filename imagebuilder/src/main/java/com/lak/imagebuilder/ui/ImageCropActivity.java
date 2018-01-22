package com.lak.imagebuilder.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.lak.imagebuilder.R;
import com.lak.imagebuilder.bean.ImageItem;
import com.lak.imagebuilder.core.ImageBuilderConfig;
import com.lak.imagebuilder.util.BitmapUtil;
import com.lak.imagebuilder.widget.CropImageView;
import java.io.File;
import java.util.ArrayList;

public class ImageCropActivity extends BaseActivity
    implements View.OnClickListener, CropImageView.OnBitmapSaveCompleteListener {

  private CropImageView mCropImageView;
  private Bitmap mBitmap;
  private boolean mIsSaveRectangle;
  private int mOutputX;
  private int mOutputY;
  private ArrayList<ImageItem> mImageItems;
  private ImageBuilderConfig imageBuilderConfig;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.__lakimage_activity_image_crop);

    imageBuilderConfig = ImageBuilderConfig.getInstance();

    //初始化View
    findViewById(R.id.btn_back).setOnClickListener(this);
    Button btn_ok = (Button) findViewById(R.id.btn_ok);
    btn_ok.setText(getString(R.string.__lakimage_complete));
    btn_ok.setOnClickListener(this);
    TextView tv_des = (TextView) findViewById(R.id.tv_des);
    tv_des.setText(getString(R.string.__lakimage_photo_crop));
    mCropImageView = (CropImageView) findViewById(R.id.cv_crop_image);
    mCropImageView.setOnBitmapSaveCompleteListener(this);

    //获取需要的参数
    mOutputX = imageBuilderConfig.getOutPutX();
    mOutputY = imageBuilderConfig.getOutPutY();
    mIsSaveRectangle = imageBuilderConfig.isSaveRectangle();
    mImageItems = imageBuilderConfig.getSelectedImages();
    String imagePath = mImageItems.get(0).path;

    mCropImageView.setFocusStyle(imageBuilderConfig.getStyle());
    mCropImageView.setFocusWidth(imageBuilderConfig.getFocusWidth());
    mCropImageView.setFocusHeight(imageBuilderConfig.getFocusHeight());

    //缩放图片
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;
    BitmapFactory.decodeFile(imagePath, options);
    DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
    options.inSampleSize =
        calculateInSampleSize(options, displayMetrics.widthPixels, displayMetrics.heightPixels);
    options.inJustDecodeBounds = false;
    mBitmap = BitmapFactory.decodeFile(imagePath, options);
    //设置默认旋转角度
    mCropImageView.setImageBitmap(
        mCropImageView.rotate(mBitmap, BitmapUtil.getBitmapDegree(imagePath)));
  }

  public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
    int width = options.outWidth;
    int height = options.outHeight;
    int inSampleSize = 1;
    if (height > reqHeight || width > reqWidth) {
      if (width > height) {
        inSampleSize = width / reqWidth;
      } else {
        inSampleSize = height / reqHeight;
      }
    }
    return inSampleSize;
  }

  @Override public void onClick(View v) {
    int id = v.getId();
    if (id == R.id.btn_back) {
      setResult(RESULT_CANCELED);
      finish();
    } else if (id == R.id.btn_ok) {
      mCropImageView.saveBitmapToFile(imageBuilderConfig.getCropCacheFolder(this), mOutputX,
          mOutputY, mIsSaveRectangle);
    }
  }

  @Override public void onBitmapSaveSuccess(File file) {
    //        Toast.makeText(ImageCropActivity.this, "裁剪成功:" + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();

    //裁剪后替换掉返回数据的内容，但是不要改变全局中的选中数据
    mImageItems.remove(0);
    ImageItem imageItem = new ImageItem();
    imageItem.path = file.getAbsolutePath();
    mImageItems.add(imageItem);
    ArrayList<String> imageOnlyPath = new ArrayList<>();
    imageOnlyPath.add(file.getAbsolutePath());

    Intent intent = new Intent();
    intent.putExtra(ImageBuilderConfig.EXTRA_RESULT_ITEMS_ORIGIN, mImageItems);
    intent.putExtra(ImageBuilderConfig.EXTRA_RESULT_ITEMS_COMPRESS, imageOnlyPath);
    intent.putExtra(ImageBuilderConfig.EXTRA_FROM_ORIGIN_IMAGE, true);
    setResult(ImageBuilderConfig.RESULT_CODE_ITEMS, intent);   //单选不需要裁剪，返回数据
    finish();
  }

  @Override public void onBitmapSaveError(File file) {

  }

  @Override protected void onDestroy() {
    super.onDestroy();
    mCropImageView.setOnBitmapSaveCompleteListener(null);
    if (null != mBitmap && !mBitmap.isRecycled()) {
      mBitmap.recycle();
      mBitmap = null;
    }
  }
}
