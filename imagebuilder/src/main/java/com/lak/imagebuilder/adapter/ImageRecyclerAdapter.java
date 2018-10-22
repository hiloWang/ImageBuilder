package com.lak.imagebuilder.adapter;

import android.Manifest;
import android.app.Activity;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.Toast;
import com.lak.imagebuilder.R;
import com.lak.imagebuilder.bean.ImageItem;
import com.lak.imagebuilder.core.ImageBuilderConfig;
import com.lak.imagebuilder.listener.OnImageItemClickListener;
import com.lak.imagebuilder.ui.BaseActivity;
import com.lak.imagebuilder.ui.ImageGridActivity;
import com.lak.imagebuilder.util.Utils;
import com.lak.imagebuilder.widget.SuperCheckBox;
import java.util.ArrayList;

public class ImageRecyclerAdapter extends RecyclerView.Adapter<ViewHolder> {

  private static final int ITEM_TYPE_CAMERA = 0;  //第一个条目是相机
  private static final int ITEM_TYPE_NORMAL = 1;  //第一个条目不是相机
  private ImageBuilderConfig imageBuilderConfig;
  private Activity mActivity;
  private ArrayList<ImageItem> images;       //当前需要显示的所有的图片数据
  private ArrayList<ImageItem> mSelectedImages; //全局保存的已经选中的图片数据
  private boolean isShowCamera;         //是否显示拍照按钮
  private int mImageSize;               //每个条目的大小
  private LayoutInflater mInflater;
  private OnImageItemClickListener listener;   //图片被点击的监听
  private int mGridColumn; // GridColumn列数

  public void setOnImageItemClickListener(OnImageItemClickListener listener) {
    this.listener = listener;
  }

  public void refreshData(ArrayList<ImageItem> images) {
    if (images == null || images.size() == 0) {
      this.images = new ArrayList<>();
    } else {
      this.images = images;
    }
    notifyDataSetChanged();
  }

  /**
   * 构造方法
   */
  public ImageRecyclerAdapter(Activity activity, ArrayList<ImageItem> images) {
    this.mActivity = activity;
    if (images == null || images.size() == 0) {
      this.images = new ArrayList<>();
    } else {
      this.images = images;
    }

    imageBuilderConfig = ImageBuilderConfig.getInstance();

    mGridColumn = imageBuilderConfig.getGridColumn();
    if (mGridColumn != -1) {
      mImageSize = Utils.getImageItemWidth(mActivity, mGridColumn);
    } else {
      mImageSize = Utils.getImageItemWidth(mActivity);
    }

    isShowCamera = imageBuilderConfig.isShowCamera();
    mSelectedImages = imageBuilderConfig.getSelectedImages();
    mInflater = LayoutInflater.from(activity);
  }

  @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    if (viewType == ITEM_TYPE_CAMERA) {
      return new CameraViewHolder(
          mInflater.inflate(R.layout.__lakimage_adapter_camera_item, parent, false));
    }
    return new ImageViewHolder(
        mInflater.inflate(R.layout.__lakimage_adapter_image_list_item, parent, false));
  }

  @Override public void onBindViewHolder(ViewHolder holder, int position) {
    if (holder instanceof CameraViewHolder) {
      ((CameraViewHolder) holder).bindCamera();
    } else if (holder instanceof ImageViewHolder) {
      ((ImageViewHolder) holder).bind(position);
    }
  }

  @Override public int getItemViewType(int position) {
    if (isShowCamera) return position == 0 ? ITEM_TYPE_CAMERA : ITEM_TYPE_NORMAL;
    return ITEM_TYPE_NORMAL;
  }

  @Override public long getItemId(int position) {
    return position;
  }

  @Override public int getItemCount() {
    return isShowCamera ? images.size() + 1 : images.size();
  }

  public ImageItem getItem(int position) {
    if (isShowCamera) {
      if (position == 0) return null;
      return images.get(position - 1);
    } else {
      return images.get(position);
    }
  }

  private class ImageViewHolder extends ViewHolder {

    View rootView;
    ImageView ivThumb;
    View mask;
    View checkView;
    SuperCheckBox cbCheck;

    ImageViewHolder(View itemView) {
      super(itemView);
      rootView = itemView;
      ivThumb = (ImageView) itemView.findViewById(R.id.iv_thumb);
      mask = itemView.findViewById(R.id.mask);
      checkView = itemView.findViewById(R.id.checkView);
      cbCheck = (SuperCheckBox) itemView.findViewById(R.id.cb_check);
      itemView.setLayoutParams(
          new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mImageSize)); //让图片是个正方形
    }

    void bind(final int position) {
      final ImageItem imageItem = getItem(position);
      ivThumb.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View v) {
          if (listener != null) listener.onImageItemClick(rootView, imageItem, position);
        }
      });
      checkView.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View v) {
          cbCheck.setChecked(!cbCheck.isChecked());
          int selectLimit = imageBuilderConfig.getSelectLimit();
          if (cbCheck.isChecked() && mSelectedImages.size() >= selectLimit) {
            Toast.makeText(mActivity.getApplicationContext(),
                mActivity.getString(R.string.__lakimage_select_limit, selectLimit),
                Toast.LENGTH_SHORT).show();
            cbCheck.setChecked(false);
            mask.setVisibility(View.GONE);
          } else {
            imageBuilderConfig.addSelectedImageItem(position, imageItem, cbCheck.isChecked());
            mask.setVisibility(View.VISIBLE);
          }
        }
      });
      //根据是否多选，显示或隐藏checkbox
      if (imageBuilderConfig.isMultiMode()) {
        cbCheck.setVisibility(View.VISIBLE);
        boolean checked = mSelectedImages.contains(imageItem);
        if (checked) {
          mask.setVisibility(View.VISIBLE);
          cbCheck.setChecked(true);
        } else {
          mask.setVisibility(View.GONE);
          cbCheck.setChecked(false);
        }
      } else {
        cbCheck.setVisibility(View.GONE);
      }
      imageBuilderConfig.getImageLoader()
          .displayImage(mActivity, imageItem.path, ivThumb, mImageSize, mImageSize); //显示图片
    }
  }

  private class CameraViewHolder extends ViewHolder {

    View mItemView;

    CameraViewHolder(View itemView) {
      super(itemView);
      mItemView = itemView;
    }

    void bindCamera() {
      mItemView.setLayoutParams(
          new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mImageSize)); //让图片是个正方形
      mItemView.setTag(null);
      mItemView.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View v) {
          if (!((BaseActivity) mActivity).checkPermission(Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(mActivity,
                new String[] { Manifest.permission.CAMERA },
                ImageGridActivity.REQUEST_PERMISSION_CAMERA);
          } else {
            imageBuilderConfig.takePicture(mActivity, ImageBuilderConfig.REQUEST_CODE_TAKE);
          }
        }
      });
    }
  }
}
