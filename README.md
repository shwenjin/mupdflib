# mupdflib
mupdf库
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

##### 5、在proguard-rules.pro中添加混淆
```
-keep class com.artifex.mupdfdemo.** {*;}
```
