package com.demo.cv42.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.util.Log;


import com.demo.cv42.App;

import org.opencv.android.JavaCameraView;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.List;

/**
 * 自定义JavaCamera View
 */
public class CustomJavaCameraView extends JavaCameraView {

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

    //接受Bitmap回调，可以在其他的地方显示
    private OnFrameReadCallBack onFrameReadCallBack;

    public CustomJavaCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void swithCamera(boolean useFront) {
        useFrontCamera = useFront;
        mCameraIndex = useFront ? CAMERA_ID_FRONT : CAMERA_ID_BACK;
        disconnectCamera();
        connectCamera(getWidth(), getHeight());
    }

    @Override
    protected void deliverAndDrawFrame(CvCameraViewFrame frame) {
        int width = getWidth();
        int height = getHeight();
        if (drawUseDefaultMethod) {
            super.deliverAndDrawFrame(frame);
        } else {
            //使用自定义绘制方法

            //如果需要CvLisenter 回调方法，可以在此自定义或者反射获取父类的listener
//            if (mListener != null) {
//                modified = mListener.onCameraFrame(frame);
//            } else {
//                modified = frame.rgba();
//            }

            Mat srcMat = frame.rgba();
            Mat rotatedMat = null;

            if (isPortrait) {
                //竖屏
                if (useFrontCamera) {
                    Core.flip(srcMat, srcMat, 1);//使用了前置摄像头，需要翻转左右，不然旋转90度之后会有问题
                    rotatedMat = new Mat(srcMat.cols(), srcMat.rows(), srcMat.type());
                    Core.rotate(srcMat, rotatedMat, Core.ROTATE_90_CLOCKWISE);//旋转之后得到正确的预览图像
                    srcMat.release();
                    srcMat = rotatedMat;
                } else {
                    rotatedMat = new Mat(srcMat.cols(), srcMat.rows(), srcMat.type());
                    Core.rotate(srcMat, rotatedMat, Core.ROTATE_90_CLOCKWISE);//旋转之后得到正确的预览图像
                    srcMat.release();
                    srcMat = rotatedMat;
                }

            } else {
                //横屏则不需要处理
                if (useFrontCamera) {

                } else {

                }
            }

            //经过上述步骤得到正确方向的Mat信息
            if (null == customCacheBitmap || (customCacheBitmap.getWidth() != srcMat.cols() || customCacheBitmap.getHeight() != srcMat.rows())) {
                if (null != customCacheBitmap && !customCacheBitmap.isRecycled()) {
                    customCacheBitmap.recycle();
                }
                customCacheBitmap = Bitmap.createBitmap(srcMat.cols(), srcMat.rows(), Bitmap.Config.ARGB_8888);
            }

            //自动缩放到全屏，原生的Opencv mscale 参数自动计算出来有缺点不能自动铺满屏幕
            if (autoFullScreen) {
                if (srcMat.cols() < width || srcMat.rows() < height) {

                    float scaleWidth = width * 1.0f / srcMat.cols();
                    float scaleHeight = height * 1.0f / srcMat.rows();
                    float maxScale = Math.max(scaleHeight, scaleWidth);

                    mScale = maxScale;//用自带的缩放系数（当然也可以自己来缩放Mat 或者bitmap达到同样的效果）
                } else {
                    mScale = 1.0f;
                }
            } else {
                mScale = 1.0f;
            }


            boolean bmpValid = true;
            if (srcMat != null) {

                try {
                    Imgproc.cvtColor(srcMat,srcMat,Imgproc.COLOR_RGB2RGBA);//需要强制设置一个格式否则dlib无法识别landmark
                    Utils.matToBitmap(srcMat, customCacheBitmap);//这一步骤很容易出错，每次根据Mat的实际大小创建Bitmap缓存，但是太浪费时间，所以要事先创建好
                } catch (Exception e) {
                    bmpValid = false;
                }


                if (null != onFrameReadCallBack) {
                    onFrameReadCallBack.OnFrameRead(customCacheBitmap,srcMat);//别的地方获Bitmap可以在别的地方显示
                }
                srcMat.release();//用完释放
            }



            //如果屏蔽下面的代码不会绘制
            if (bmpValid && customCacheBitmap != null && !customCacheBitmap.isRecycled()) {
                Canvas canvas = getHolder().lockCanvas();

                if (canvas != null) {
                    canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);
                    Log.e(App.tag, "mscale" + mScale);


                    if (mScale != 0) {
                        canvas.drawBitmap(customCacheBitmap, new Rect(0, 0, customCacheBitmap.getWidth(), customCacheBitmap.getHeight()),
                                new Rect((int) ((canvas.getWidth() - mScale * customCacheBitmap.getWidth()) / 2),
                                        (int) ((canvas.getHeight() - mScale * customCacheBitmap.getHeight()) / 2),
                                        (int) ((canvas.getWidth() - mScale * customCacheBitmap.getWidth()) / 2 + mScale * customCacheBitmap.getWidth()),
                                        (int) ((canvas.getHeight() - mScale * customCacheBitmap.getHeight()) / 2 + mScale * customCacheBitmap.getHeight())), null);
                    } else {
                        canvas.drawBitmap(customCacheBitmap, new Rect(0, 0, customCacheBitmap.getWidth(), customCacheBitmap.getHeight()),
                                new Rect((canvas.getWidth() - customCacheBitmap.getWidth()) / 2,
                                        (canvas.getHeight() - customCacheBitmap.getHeight()) / 2,
                                        (canvas.getWidth() - customCacheBitmap.getWidth()) / 2 + customCacheBitmap.getWidth(),
                                        (canvas.getHeight() - customCacheBitmap.getHeight()) / 2 + customCacheBitmap.getHeight()), null);
                    }


                    if (mFpsMeter != null) {
                        mFpsMeter.setResolution(srcMat.width(), srcMat.height());//使用真实的图片分辨率
                        mFpsMeter.measure();
                        mFpsMeter.draw(canvas, 20, 30);
                    }
                    getHolder().unlockCanvasAndPost(canvas);
                }
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (null != customCacheBitmap && !customCacheBitmap.isRecycled()) {
            customCacheBitmap.recycle();
        }
    }

    public void setResolution(Size resolution) {
        disconnectCamera();
        mMaxHeight = resolution.height;
        mMaxWidth = resolution.width;
        connectCamera(getWidth(), getHeight());
    }

    public void restartCamera() {
        disconnectCamera();
        connectCamera(getWidth(), getHeight());
    }

    @Override
    protected org.opencv.core.Size calculateCameraFrameSize(List<?> supportedSizes, ListItemAccessor accessor, int surfaceWidth, int surfaceHeight) {
        return super.calculateCameraFrameSize(supportedSizes, accessor, surfaceWidth, surfaceHeight);
    }

    public List<Size> getResolutionList() {
        return mCamera.getParameters().getSupportedPreviewSizes();
    }

    public Size getResolution() {
        return mCamera.getParameters().getPreviewSize();
    }

    public boolean isUseFrontCamera() {
        return useFrontCamera;
    }

    public boolean isDrawUseDefaultMethod() {
        return drawUseDefaultMethod;
    }

    public void setDrawUseDefaultMethod(boolean drawUseDefaultMethod) {
        this.drawUseDefaultMethod = drawUseDefaultMethod;
    }

    public boolean isPortrait() {
        return isPortrait;
    }

    public void setPortrait(boolean portrait) {
        isPortrait = portrait;
    }


    public static interface OnFrameReadCallBack {
        public void OnFrameRead(Bitmap bitmap,Mat mat);
    }

    public OnFrameReadCallBack getOnFrameReadCallBack() {
        return onFrameReadCallBack;
    }

    public void setOnFrameReadCallBack(OnFrameReadCallBack onFrameReadCallBack) {
        this.onFrameReadCallBack = onFrameReadCallBack;
    }

    public boolean isAutoFullScreen() {
        return autoFullScreen;
    }

    public void setAutoFullScreen(boolean autoFullScreen) {
        this.autoFullScreen = autoFullScreen;
    }
}
