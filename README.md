# mupdflib

##### 1、添加build.gradle 

```
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```
##### 2、添加dependency

```
dependencies {
    compile 'com.github.shwenjin:mupdflib:V1.0.2'
}
```
##### 3、在XML布局中添加自定义View

```
<com.artifex.mupdfdemo.WJPdfView 
    android:id="@+id/mupdf"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
</com.artifex.mupdfdemo.WJPdfView>
```
##### 4、调用

1. 执行
    ```
    WJPdfView pdfView= (WJPdfView) findViewById(R.id.mupdf);
    pdfView.openPDF("xxx.pdf");
    ```
2. 销毁
    
    ```
    pdfView.onDestroy();
    ```
3. 横竖屏切换
    
    ```
    private ScreenSwitchUtils instance;
    
    instance = ScreenSwitchUtils.init(this.getApplicationContext());
    
    mPdfView.setOnPdfListener(new WJPdfView.OnPdfListener() {
                @Override
                public void finish() {
                    MainActivity.this.finish();
                }
    
                @Override
                public void onCompletion() {
                    //观看完成
                }
    
                @Override
                public void toggleScreen() {
                    //横竖屏切换
                    instance.toggleScreen();
                }
    
                @Override
                public void onstart() {
                    //启动换横屏监听
                    instance.start(MainActivity.this);
                }
            });
            
        @Override
        public void onConfigurationChanged(Configuration newConfig) {
            super.onConfigurationChanged(newConfig);
            if (instance.isPortrait()) {
                this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                if(mPdfView.getVisibility()==View.VISIBLE) {
                    mPdfView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));//设置显示的高度
                    mPdfView.requestLayout();
                    mPdfView.updateAdapter();
                }
            } else  {
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
                if(mPdfView.getVisibility()==View.VISIBLE) {
                    mPdfView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
                    mPdfView.requestLayout();
                    mPdfView.updateAdapter();
                }
            }
        }
        
        <activity
            android:name=".MainActivity"
            android:screenOrientation="portrait"
            android:configChanges="screenSize|keyboardHidden|orientation" />
    ```

##### 5、在proguard-rules.pro中添加混淆
```
-keep class com.artifex.mupdfdemo.** {*;}
```
