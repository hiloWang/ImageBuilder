package com.lak.imagebuilder.listener;

import android.view.View;
import com.lak.imagebuilder.bean.ImageItem;

public interface OnImageItemClickListener {
  void onImageItemClick(View view, ImageItem imageItem, int position);
}