package com.lak.imagebuilder.ui;

import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import com.lak.imagebuilder.R;
import com.lak.imagebuilder.core.ImageBuilderConfig;
import com.lak.imagebuilder.widget.SystemBarTintManager;

public class BaseActivity extends AppCompatActivity {

  protected SystemBarTintManager tintManager;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      setTranslucentStatus(true);
    }
    tintManager = new SystemBarTintManager(this);
    tintManager.setStatusBarTintEnabled(true);
    // 设置上方状态栏的颜色
    tintManager.setStatusBarTintResource(R.color.__lakimage_color_primary_dark);
  }

  @TargetApi(19) private void setTranslucentStatus(boolean on) {
    Window win = getWindow();
    WindowManager.LayoutParams winParams = win.getAttributes();
    final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
    if (on) {
      winParams.flags |= bits;
    } else {
      winParams.flags &= ~bits;
    }
    win.setAttributes(winParams);
  }

  public boolean checkPermission(@NonNull String permission) {
    return ActivityCompat.checkSelfPermission(this, permission)
        == PackageManager.PERMISSION_GRANTED;
  }

  public void showToast(String toastText) {
    Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT).show();
  }

  @Override protected void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    ImageBuilderConfig.getInstance().restoreInstanceState(savedInstanceState);
  }

  @Override protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    ImageBuilderConfig.getInstance().saveInstanceState(outState);
  }
}
