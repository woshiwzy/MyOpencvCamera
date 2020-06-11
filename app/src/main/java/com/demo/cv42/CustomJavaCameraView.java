package com.demo.cv42;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.hardware.Camera.Size;
import android.util.AttributeSet;

import org.opencv.android.JavaCameraView;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;

import java.util.List;

public class CustomJavaCameraView extends JavaCameraView {

    //是否使用前置摄像头
    private boolean useFrontCamera = false;
    //是否使用原生的view来绘制
    private boolean drawInThisView = false;
    //显示Mat用的Bitmap
    private Bitmap customCacheBitmapPor;
    private Bitmap customCacheBitmapLad;
    //当前是否竖屏
    private boolean isPortrait = true;
    private OnFrameReadCallBack onFrameReadCallBack;

    private int lastWidth, lastHeight;

    public CustomJavaCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        useFrontCamera = true;
        mCameraIndex = useFrontCamera ? CAMERA_ID_FRONT : CAMERA_ID_BACK;
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

        if (drawInThisView) {
            super.deliverAndDrawFrame(frame);
        } else {
            //使用自定义绘制方法
            Mat modified = frame.rgba();
            Mat rotatedMat = null;


            if(isPortrait){
             //竖屏
                if (useFrontCamera) {
                    Core.flip(modified, modified, 1);//使用了前置摄像头，又设置了drawSource=false,需要翻转左右，不然旋转90度之后会有问题
                    rotatedMat = new Mat(modified.cols(), modified.rows(), modified.type());
                    Core.rotate(modified, rotatedMat, Core.ROTATE_90_CLOCKWISE);//旋转之后得到正确的预览图像
                    modified = rotatedMat;
                }else{
                    rotatedMat = new Mat(modified.cols(), modified.rows(), modified.type());
                    Core.rotate(modified, rotatedMat, Core.ROTATE_90_CLOCKWISE);//旋转之后得到正确的预览图像
                    modified = rotatedMat;
                }

            }else{
             //横屏
                if (useFrontCamera) {
//                    Core.flip(modified, modified, 1);//使用了前置摄像头，又设置了drawSource=false,需要翻转左右，不然旋转90度之后会有问题
//                    rotatedMat = new Mat(modified.cols(), modified.rows(), modified.type());
//                    Core.rotate(modified, rotatedMat, Core.ROTATE_90_CLOCKWISE);//旋转之后得到正确的预览图像
//                    modified = rotatedMat;

                }else{
//                    rotatedMat = new Mat(modified.cols(), modified.rows(), modified.type());
//                    Core.rotate(modified, rotatedMat, Core.ROTATE_90_CLOCKWISE);//旋转之后得到正确的预览图像
//                    modified = rotatedMat;
                }
            }

            Bitmap customCacheBitmap = null;
            if ((isPortrait == true && null == customCacheBitmapPor) || (modified.rows() != lastHeight || modified.cols() != lastWidth)) {
                if (null != customCacheBitmapPor && !customCacheBitmapPor.isRecycled()) {
                    customCacheBitmapPor.recycle();
                }

                customCacheBitmapPor = Bitmap.createBitmap(modified.cols(), modified.rows(), Bitmap.Config.ARGB_8888);
                lastWidth = customCacheBitmapPor.getWidth();
                lastHeight = customCacheBitmapPor.getHeight();

            } else if ((isPortrait == false && null == customCacheBitmapLad) || (modified.rows() != lastHeight || modified.cols() != lastWidth)) {
                if (null != customCacheBitmapLad && !customCacheBitmapLad.isRecycled()) {
                    customCacheBitmapLad.recycle();
                }

                customCacheBitmapLad = Bitmap.createBitmap(modified.cols(), modified.rows(), Bitmap.Config.ARGB_8888);
                lastWidth = customCacheBitmapLad.getWidth();
                lastHeight = customCacheBitmapLad.getHeight();
            }
            customCacheBitmap = isPortrait ? customCacheBitmapPor : customCacheBitmapLad;

            boolean bmpValid = true;
            if (modified != null) {
                try {
                    Utils.matToBitmap(modified, customCacheBitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                    bmpValid = false;
                }
            }


            if (null != onFrameReadCallBack) {
                onFrameReadCallBack.OnFrameRead(customCacheBitmap);
            }


            if (bmpValid && customCacheBitmap != null && !customCacheBitmap.isRecycled()) {

                Canvas canvas = getHolder().lockCanvas();

                if (canvas != null) {
                    canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);

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
        if (null != customCacheBitmapPor && !customCacheBitmapPor.isRecycled()) {
            customCacheBitmapPor.recycle();
        }

        if (null != customCacheBitmapLad && !customCacheBitmapLad.isRecycled()) {
            customCacheBitmapLad.recycle();
        }
    }

    public void setResolution(Size resolution) {
        disconnectCamera();
        mMaxHeight = resolution.height;
        mMaxWidth = resolution.width;
        connectCamera(getWidth(), getHeight());
    }

    public void restartCamera(){
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

    public boolean isDrawInThisView() {
        return drawInThisView;
    }

    public void setDrawInThisView(boolean drawInThisView) {
        this.drawInThisView = drawInThisView;
    }

    public boolean isPortrait() {
        return isPortrait;
    }

    public void setPortrait(boolean portrait) {
        isPortrait = portrait;
    }


    public static interface OnFrameReadCallBack {
        public void OnFrameRead(Bitmap bitmap);
    }

    public OnFrameReadCallBack getOnFrameReadCallBack() {
        return onFrameReadCallBack;
    }

    public void setOnFrameReadCallBack(OnFrameReadCallBack onFrameReadCallBack) {
        this.onFrameReadCallBack = onFrameReadCallBack;
    }
}
