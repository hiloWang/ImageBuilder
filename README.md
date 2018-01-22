# ImageBuilder
仿微信图库，支持原图选择，删除图片，压缩图片，无需写权限判断以及onActivityResult，采用Builder模式

# Usage
### Gradle
```groov
dependencies 
    compile 'me.iwf.photopicker:PhotoPicker:0.9.12@aar
    compile 'com.android.support:appcompat-v7:23.4.0
    compile 'com.android.support:recyclerview-v7:23.4.0
    compile 'com.android.support:design:23.4.0
    compile 'com.github.bumptech.glide:glide:4.1.1
}
```

* ```appcompat-v7```version >= 26.0.2

### 选择图片
```
ImageBuilder.builder()
              .gridColumnCount(3) // 图片显示密度
              .multiMode(true) // 多选 default
              .selected(originImgsPath) // 回显已选择的图片
              .imageLoader(new PicassoImageLoader()) // 默认GlideImageLoader
              .showOriginImageCheckbox(true) // 是否显示原图checkbox
              .previewEnabled(false) // 是否可预览大图
              .showCompressImageSizeLog(true) // 是否打印压缩前后大小，默认false，log：ImageBuilder   
              .showCamera(true) // 是否显示相机按钮 默认true
              .cropEnable(false) // 是否可以剪裁 默认false
              .start(ExampleActivity.this, new ActivityResultListener() {
                @Override public void onActivityResult(ArrayList<ImageItem> originImgsPath,
                    ArrayList<String> compressImgsPath, int resultCode, boolean isCompress) {
                  // 原始图
                  ExampleActivity.this.originImgsPath = originImgsPath;
                  // 压缩图
                  ExampleActivity.this.compressImgsPath = compressImgsPath;

                  if (resultCode == ImageBuilder.RESULT_CODE_SELECTED_IMAGE) {

                    // .. 加载图片
                  }
                }
              });               
               
 ```              
               
 ### 预览大图
```
ImageBuilder.builder().startPreviewActivity(...);             
```              
               
               
               
               
               
