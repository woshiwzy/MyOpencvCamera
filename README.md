##  Android Opencv 4.2 摄像头二次开发之 横竖屏切换，前后摄像头切换，铺满全屏问题，相机无法启动问题，一次解决

## 问题1.横竖屏切换崩溃问题

 **描述**：
主要是原生的Opencv demo中没有解决这样的问题，横竖屏切换会导致屏幕不能铺满或者直接崩溃问题。
 
**崩溃原因:**
     主要是org.opencv.android.CameraBridgeViewBase#mCacheBitmap 这个mCacheBitmap 大小和Opencv 得到的CvCameraViewFrame 宽度和高度不一样，导致在
```
 org.opencv.android.CameraBridgeViewBase#deliverAndDrawFrame
```
  方法中调用的Utils.matToBitmap(modified, mCacheBitmap)方法时崩溃。这是个native方法，try-catch不起作用所以导致崩溃。
   
 **解决办法：**    
 要解决这个问题，主要是要在Utils.matToBitmap(modified, mCacheBitmap)调用之前，创建和CvCameraViewFrame中的mat一样大小的Bitmap。
 
 **解决办法1**:调用JavaCameraView的setCvCameraViewListener 在onCameraViewStarted的回调方法中创建对应大小的mCacheBitmap，这里又有两种方式：
 1.不想改上层源码的话可以用反射  
 2.扩展一下源码添加一个方法比如我在源码中加了一个方法
 org.opencv.android.CameraBridgeViewBase#AllocateCache2
在onCameraViewStarted 中调用保证在deliverAndDrawFrame调用的时候，mat和bitmap保证大小是一样的

**解决办法2**：继承org.opencv.android.CameraBridgeViewBase，在改造deliverAndDrawFrame方法，在调用  

```
Utils.matToBitmap(modified, mCacheBitmap);
```
之前保证mCacheBitmap和modified的大小一样，或者屏蔽掉opencv的方法自己重写,参考如下
```
com.demo.cv42.view.CustomJavaCameraView#deliverAndDrawFrame
```

## 问题2.前后摄像头切换问题

**描述**：后摄像头主要可能存在不能铺满全屏的问题(看设备)，前置摄像头可能会存在3个问题 1.不能铺满全屏，2.左右图像反转 3.被旋转90度
所以要在摄像头切换的时候解决这些问题：

问题1.不能铺满全屏的问题

解决办法1：修改mScale这个缩放参数
```
org.opencv.android.CameraBridgeViewBase#mScale
```
JavaCameraView 有用到这个缩放系数，但是代码有问题，源码如下
```
mScale = Math.min(((float)height)/mFrameHeight, ((float)width)/mFrameWidth);
```
要保证全屏，这里应该取最大而不是最小，而且比例是应该是JavaCameraView 的宽高比除以原始的mat的宽高比，opencv的源码中写反了，而且需要判断如果mat本身大于JavaCameraView宽高，只需要把mScale设置为1就可以了，源码中mScale==0 事不起作用，正确代码如下
```
   if (srcMat.cols() < width || srcMat.rows() < height) {

                    float scaleWidth = width * 1.0f / srcMat.cols();
                    float scaleHeight = height * 1.0f / srcMat.rows();
                    float maxScale = Math.max(scaleHeight, scaleWidth);

                    mScale = maxScale;//用自带的缩放系数（当然也可以自己来缩放Mat 或者bitmap达到同样的效果）
                } else {
                    mScale = 1.0f;
                }
```
这样在绘制的时候保证无论是宽度和高度，都可以放大到全屏，而在绘制的时时候都可以全屏绘制出来，可能会有超出屏幕的部分，但是始终能铺满全屏。

## 问题3.横竖屏切换自适应
横竖屏切换要解决几个问题
1.告诉JavaCameraView当前的手机方向
2.按照最新的分别率重新创建customCacheBitmap 
3.重启相机

做法
1. 配置Activity android:configChanges="orientation|screenSize"
2. 重新Activity onConfigurationChanged 方法，在屏幕方向发生改变时告诉JavaCameraView 并重启相机

针对以上3个问题的综合解决办法，我继承了JavaCameraView，增加如下字段

```
    //是否使用前置摄像头
    private boolean useFrontCamera = false;

    //是否使用opencv自己的方式绘制来绘制
    private boolean drawUseDefaultMethod = false;

    //显示Mat用的Bitmap
    private Bitmap customCacheBitmap = null;

    //当前是否竖屏
    private boolean isPortrait = true;

    //自动缩放到全屏取中间部分绘制
    private boolean autoFullScreen = true;
```

重写了deliverAndDrawFrame 方法
```
com.demo.cv42.view.CustomJavaCameraView#deliverAndDrawFrame
```

统一解决，前后摄像头切换，横竖屏切换带来的各种问题。

## 问题4.相机无法启动问题
1.检查是否给App授权使用相机的权限
2.很多情况下是因为JavaCameraView 方法
```
org.opencv.android.CameraBridgeViewBase#calculateCameraFrameSize

```

这个方法是计算出一个最合适的输出分辨率，但是这个方法会有个bug，有可能会计算出一个相机硬件不支持的分辨率（），这会导致相机无法启动，直接崩溃，看不到任何日志，需要debug跟踪才能发现问题。所有的相机分辨率应该在API返回的列表中（mCamera.getParameters().getSupportedPreviewSizes()）才行。


## 问题5.裁剪相机定制

 1.做到随意定制相机API(最后就是Camera2的API)
 2.前后摄像头任意切换
 3.横竖屏任意切换
 4.增加GPU实时滤镜
 5.使用自己的UI组件渲染(脱离Opencv束缚)更灵活
 6.分辨率自由切换支持
 7.正方形相机预览



