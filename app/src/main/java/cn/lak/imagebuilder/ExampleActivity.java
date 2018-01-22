package cn.lak.imagebuilder;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import com.lak.imagebuilder.ImageBuilder;
import com.lak.imagebuilder.bean.ImageItem;
import com.lak.imagebuilder.listener.ActivityResultListener;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import java.io.File;
import java.util.ArrayList;

public class ExampleActivity extends AppCompatActivity {

  private ArrayList<ImageItem> originImgsPath;
  private ArrayList<String> compressImgsPath;
  public static final int REQUEST_CODE_PREVIEW = 101;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(cn.lak.imagebuilder.R.layout.activity_example);

    final ImageView image_view = findViewById(R.id.image_view);
    final ImageView image_view2 = findViewById(R.id.image_view2);

    image_view.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        if (originImgsPath != null && originImgsPath.size() > 0) {
          /*ImageBuilder.builder()
              .startPreviewActivity(ExampleActivity.this, originImgsPath, 0,
                  new ActivityResultListener() {
                    @Override public void onActivityResult(ArrayList<ImageItem> originImagesPath,
                        ArrayList<String> compressImagesPath, int resultCode) {
                      if (resultCode == ImageBuilder.RESULT_CODE_PREVIEW_IMAGE) {

                        Toast.makeText(ExampleActivity.this, originImagesPath.size() + "",
                            Toast.LENGTH_LONG).show();
                      }
                    }
                  });*/
          ImageBuilder.builder().startPreviewActivity(ExampleActivity.this, originImgsPath, 1);
        }
      }
    });

    image_view2.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        if (originImgsPath != null && originImgsPath.size() > 0) {
          ImageBuilder.builder()
              .gridColumnCount(3)
              .multiMode(true)
              .selected(originImgsPath)
              .imageLoader(new PicassoImageLoader())
              .showOriginImageCheckbox(true)
              .previewEnabled(false)
              .start(ExampleActivity.this, new ActivityResultListener() {
                @Override public void onActivityResult(ArrayList<ImageItem> originImgsPath,
                    ArrayList<String> compressImgsPath, int resultCode, boolean isCompress) {
                  // 原始图
                  ExampleActivity.this.originImgsPath = originImgsPath;
                  // 压缩图
                  ExampleActivity.this.compressImgsPath = compressImgsPath;

                  if (resultCode == ImageBuilder.RESULT_CODE_SELECTED_IMAGE) {

                    Picasso.with(ExampleActivity.this)
                        .load(Uri.fromFile(new File(originImgsPath.get(0).path)))
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .resize(512, 512)
                        .centerInside()
                        .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                        .into(image_view);

                    if (compressImgsPath != null && compressImgsPath.size() > 0) {
                      Picasso.with(ExampleActivity.this)
                          .load(Uri.fromFile(new File(compressImgsPath.get(0))))
                          .placeholder(R.drawable.ic_launcher_background)
                          .error(R.drawable.ic_launcher_background)
                          .resize(512, 512)
                          .centerInside()
                          .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                          .into(image_view2);
                    }
                  }
                }
              });
        }
      }
    });

    findViewById(cn.lak.imagebuilder.R.id.text_view).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        ImageBuilder.builder()
            .gridColumnCount(3)
            .multiMode(true)
            .previewEnabled(true)
            .showCompressImageSizeLog(true)
            .start(ExampleActivity.this, new ActivityResultListener() {
              @Override public void onActivityResult(ArrayList<ImageItem> originImgsPath,
                  ArrayList<String> compressImgsPath, int resultCode, boolean isCompress) {
                ExampleActivity.this.originImgsPath = originImgsPath;
                ExampleActivity.this.compressImgsPath = compressImgsPath;
                if (resultCode == ImageBuilder.RESULT_CODE_SELECTED_IMAGE) {

                  Picasso.with(ExampleActivity.this)
                      .load(Uri.fromFile(new File(originImgsPath.get(0).path)))
                      .placeholder(R.drawable.ic_launcher_background)
                      .error(R.drawable.ic_launcher_background)
                      .resize(512, 512)
                      .centerInside()
                      .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                      .into(image_view);

                  if (compressImgsPath != null && compressImgsPath.size() > 0) {
                    Picasso.with(ExampleActivity.this)
                        .load(Uri.fromFile(new File(compressImgsPath.get(0))))
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .resize(512, 512)
                        .centerInside()
                        .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                        .into(image_view2);
                  }
                }
              }
            });
      }
    });
  }
}
