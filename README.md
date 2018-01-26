# ImageBuilder
仿微信图库，支持原图选择，删除图片，压缩图片，无需写权限判断以及onActivityResult，采用Builder模式

# Usage
### Gradle
```groov

ext {
  supportLibVersion = "26.1.0"
  photoViewVersion = "2.1.3"
  glideVersion = "4.2.0"
  picassoVersion = "2.5.2"
}

// ** 使用者需引入以下依赖包 **
dependencies 
    implementation "com.android.support:appcompat-v7:$supportLibVersion"
    implementation "com.android.support:recyclerview-v7:$supportLibVersion"
    implementation "com.github.chrisbanes:PhotoView:$photoViewVersion"
    implementation "com.github.bumptech.glide:glide:$glideVersion"
}
```

* ```appcompat-v7```version >= 26.0.2

### 引入
Add it in your root build.gradle at the end of repositories:
```
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```
Step 2. Add the dependency
```
	dependencies {
	        compile 'com.github.hiloWang:ImageBuilder:1.0'
	}
```

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
```ImageBuilder.builder().startPreviewActivity(...);  ```

           
             
               
### 参考
* [https://github.com/donglua/PhotoPicker](https://github.com/donglua/PhotoPicker) 
* [https://github.com/jeasonlzy/ImagePicker](https://github.com/jeasonlzy/ImagePicker)
               

喜欢原作的可以去使用。同时欢迎大家下载体验本项目，如果使用过程中遇到什么问题，欢迎反馈。

## 联系方式
 * email： developerlak@gmail.com