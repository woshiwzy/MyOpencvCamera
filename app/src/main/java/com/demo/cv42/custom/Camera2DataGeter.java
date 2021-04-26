package com.demo.cv42.custom;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.SessionConfiguration;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;

import com.demo.cv42.App;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;


public class Camera2DataGeter extends CameraDataGeterBase {

    private static final String LOGTAG = App.tag;

    private ImageReader mImageReader;
    private int mPreviewFormat = ImageFormat.YUV_420_888;

    private CameraDevice mCameraDevice;
    private CameraCaptureSession mCaptureSession;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private String mCameraID;
    private android.util.Size mPreviewSize = new android.util.Size(-1, -1);

    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;

    public static boolean isOpencvInitSuccess = false;

    static {
        isOpencvInitSuccess = OpenCVLoader.initDebug();
        Log.e(App.tag, "isinit success:" + isOpencvInitSuccess);
    }

    private boolean isPortrait;

    public Camera2DataGeter(Context context, int cameraId, int defaultConnectWidth, int defaultConnectHeight) {
        super(context, cameraId, defaultConnectWidth, defaultConnectHeight);
    }


    public void configOrientation(Configuration mConfiguration) {
        int ori = mConfiguration.orientation;
        boolean tempOri = Configuration.ORIENTATION_LANDSCAPE != ori;
        if (tempOri != isPortrait) {
            isPortrait = tempOri;
            restartCamera();
        }
    }


    public void restartCamera() {
        disconnectCamera();
        connectCamera(defaultConnectWidth, defaultConnectHeight);
    }

    /**
     * 是否使用了前置摄像头
     *
     * @return
     */
    public boolean isFrontCamera() {
        return getCameraIndex() == CameraDataGeterBase.CAMERA_ID_FRONT;
    }


    /**
     * 切换摄像头
     */
    public void toogleCamera() {
        if (this.mCameraIndex == CameraBridgeViewBase.CAMERA_ID_BACK) {
            setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT);
        } else if (this.mCameraIndex == CameraBridgeViewBase.CAMERA_ID_FRONT) {
            setCameraIndex(CameraBridgeViewBase.CAMERA_ID_BACK);
        }
    }


    /**
     * 设置相机分辨率
     *
     * @param width
     * @param height
     */
    public void setResolution(int width, int height) {
        this.defaultConnectWidth = width;
        this.defaultConnectHeight = height;
        disconnectCamera();
        connectCamera(this.defaultConnectWidth, this.defaultConnectHeight);
    }


    @Override
    public void setCameraIndex(int cameraIndex) {
        if (this.mCameraIndex != cameraIndex) {
            disconnectCamera();
            this.mCameraIndex = cameraIndex;
            connectCamera(defaultConnectWidth, defaultConnectHeight);
        }
    }

    private void startBackgroundThread() {
        Log.i(LOGTAG, "startBackgroundThread");
        stopBackgroundThread();
        mBackgroundThread = new HandlerThread("OpenCVCameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        Log.i(LOGTAG, "stopBackgroundThread");
        if (mBackgroundThread == null)
            return;
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            Log.e(LOGTAG, "stopBackgroundThread", e);
        }
    }

    protected boolean initializeCamera() {
        Log.i(LOGTAG, "initializeCamera");
        CameraManager manager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
        try {
            String camList[] = manager.getCameraIdList();
            if (camList.length == 0) {
                Log.e(LOGTAG, "Error: camera isn't detected.");
                return false;
            }
            if (mCameraIndex == CameraBridgeViewBase.CAMERA_ID_ANY) {
                mCameraID = camList[0];
            } else {
                for (String cameraID : camList) {
                    CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraID);
                    if ((mCameraIndex == CameraBridgeViewBase.CAMERA_ID_BACK &&
                            characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) ||
                            (mCameraIndex == CameraBridgeViewBase.CAMERA_ID_FRONT &&
                                    characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT)
                    ) {
                        mCameraID = cameraID;
                        break;
                    }
                }
            }
            if (mCameraID != null) {
                Log.i(LOGTAG, "Opening camera: " + mCameraID);
                manager.openCamera(mCameraID, mStateCallback, mBackgroundHandler);
            } else { // make JavaCamera2View behaves in the same way as JavaCameraView
                Log.i(LOGTAG, "Trying to open camera with the value (" + mCameraIndex + ")");
                if (mCameraIndex < camList.length) {
                    mCameraID = camList[mCameraIndex];
                    manager.openCamera(mCameraID, mStateCallback, mBackgroundHandler);
                } else {
                    // CAMERA_DISCONNECTED is used when the camera id is no longer valid
                    throw new CameraAccessException(CameraAccessException.CAMERA_DISCONNECTED);
                }
            }
            return true;
        } catch (CameraAccessException e) {
            Log.e(LOGTAG, "OpenCamera - Camera Access Exception", e);
        } catch (IllegalArgumentException e) {
            Log.e(LOGTAG, "OpenCamera - Illegal Argument Exception", e);
        } catch (SecurityException e) {
            Log.e(LOGTAG, "OpenCamera - Security Exception", e);
        }
        return false;
    }

    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(CameraDevice cameraDevice, int error) {
            cameraDevice.close();
            mCameraDevice = null;
        }

    };

    Mat rotatedMat = null;
    JavaCamera2Frame tempFrame = null;

    private void createCameraPreviewSession() {
        final int w = mPreviewSize.getWidth(), h = mPreviewSize.getHeight();
        Log.i(LOGTAG, "createCameraPreviewSession(" + w + "x" + h + ")");
        if (w < 0 || h < 0)
            return;
        try {
            if (null == mCameraDevice) {
                Log.e(LOGTAG, "createCameraPreviewSession: camera isn't opened");
                return;
            }
            if (null != mCaptureSession) {
                Log.e(LOGTAG, "createCameraPreviewSession: mCaptureSession is already started");
                return;
            }

            mImageReader = ImageReader.newInstance(w, h, mPreviewFormat, 2);
            mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Log.e(App.tag, "获得rgb帧1");
                    Image image = reader.acquireLatestImage();
                    image.close();

//                    Image image = reader.acquireLatestImage();
//                    if (image == null)
//                        return;
//
//                    // sanity checks - 3 planes
//                    Image.Plane[] planes = image.getPlanes();
//                    assert (planes.length == 3);
//                    assert (image.getFormat() == mPreviewFormat);


//                    if (null == tempFrame) {
//                        tempFrame = new JavaCamera2Frame(image);
//                    }
//                    tempFrame.mImage = image;

//                    Mat srcMat = tempFrame.rgba();

//                    deliverAndDrawFrame(srcMat);

                    //=======================================================

                    /*
                    if (isPortrait()) { //竖屏
                        if (isFrontCamera()) {
                            Core.flip(srcMat, srcMat, 1);//使用了前置摄像头，需要翻转左右，不然旋转90度之后会有问题
                        }
                        if (null == rotatedMat || rotatedMat.width() != srcMat.rows() || rotatedMat.height() != srcMat.cols()) {
//                            if(null!=rotatedMat){
//                                rotatedMat.release();
//                            }
                            rotatedMat = new Mat(srcMat.cols(), srcMat.rows(), srcMat.type());//注意旋转90度后，宽高变化
                        }
                        Core.rotate(srcMat, rotatedMat, Core.ROTATE_90_CLOCKWISE);//旋转之后得到正确的预览图像
                        deliverAndDrawFrame(rotatedMat);
//                        rotatedMat.release();
                    } else {
                        deliverAndDrawFrame(srcMat);
                    }
*/

//                    srcMat.release();
                    //=======================================================
//                    tempFrame.release();
//                    image.close();
                }
            }, mBackgroundHandler);
            Surface surface = mImageReader.getSurface();

            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(surface);


            mCameraDevice.createCaptureSession(Arrays.asList(surface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                            Log.i(App.tag, "createCaptureSession::onConfigured");
                            if (null == mCameraDevice) {
                                return; // camera is already closed
                            }
                            mCaptureSession = cameraCaptureSession;
                            try {
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                                        CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);

                                CaptureRequest rets = mPreviewRequestBuilder.build();

                                mCaptureSession.setRepeatingRequest(rets, null, mBackgroundHandler);
                                Log.i(App.tag, "CameraPreviewSession has been started");
                            } catch (Exception e) {
                                Log.e(App.tag, "createCaptureSession failed", e);
                            }
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                            Log.e(App.tag, "createCameraPreviewSession failed");
                        }
                    },
                    null
            );
        } catch (CameraAccessException e) {
            Log.e(App.tag, "createCameraPreviewSession", e);
        }
    }

    @Override
    protected void disconnectCamera() {
        Log.i(App.tag, "close camera");
        try {
            CameraDevice c = mCameraDevice;
            mCameraDevice = null;
            if (null != mCaptureSession) {
                mCaptureSession.close();
                mCaptureSession = null;
            }
            if (null != c) {
                c.close();
            }
        } finally {

            if (null != tempFrame) {
                tempFrame.release();
                tempFrame = null;
            }


            stopBackgroundThread();
            if (null != mImageReader) {
                mImageReader.close();
                mImageReader = null;
            }
        }
        Log.i(App.tag, "camera closed!");
    }

    public static class JavaCameraSizeAccessor implements ListItemAccessor {
        @Override
        public int getWidth(Object obj) {
            android.util.Size size = (android.util.Size) obj;
            return size.getWidth();
        }

        @Override
        public int getHeight(Object obj) {
            android.util.Size size = (android.util.Size) obj;
            return size.getHeight();
        }
    }

    boolean calcPreviewSize(final int width, final int height) {
        Log.i(LOGTAG, "calcPreviewSize: " + width + "x" + height);
        if (mCameraID == null) {
            Log.e(LOGTAG, "Camera isn't initialized!");
            return false;
        }
        CameraManager manager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
        try {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(mCameraID);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            android.util.Size[] sizes = map.getOutputSizes(ImageReader.class);
            List<android.util.Size> sizes_list = Arrays.asList(sizes);
            Size frameSize = calculateCameraFrameSize(sizes_list, new JavaCameraSizeAccessor(), width, height);
            Log.i(LOGTAG, "Selected preview size to " + Integer.valueOf((int) frameSize.width) + "x" + Integer.valueOf((int) frameSize.height));
            assert (!(frameSize.width == 0 || frameSize.height == 0));
            if (mPreviewSize.getWidth() == frameSize.width && mPreviewSize.getHeight() == frameSize.height)
                return false;
            else {
                mPreviewSize = new android.util.Size((int) frameSize.width, (int) frameSize.height);
                return true;
            }
        } catch (CameraAccessException e) {
            Log.e(App.tag, "calcPreviewSize - Camera Access Exception", e);
        } catch (IllegalArgumentException e) {
            Log.e(App.tag, "calcPreviewSize - Illegal Argument Exception", e);
        } catch (SecurityException e) {
            Log.e(App.tag, "calcPreviewSize - Security Exception", e);
        }
        return false;
    }

    @Override
    protected boolean connectCamera(int width, int height) {
        Log.i(LOGTAG, "setCameraPreviewSize(" + width + "x" + height + ")");
        startBackgroundThread();
        initializeCamera();
        try {
            boolean needReconfig = calcPreviewSize(width, height);
            mFrameWidth = mPreviewSize.getWidth();
            mFrameHeight = mPreviewSize.getHeight();

            if (needReconfig) {
                if (null != mCaptureSession) {
                    Log.d(LOGTAG, "closing existing previewSession");
                    mCaptureSession.close();
                    mCaptureSession = null;
                }
                createCameraPreviewSession();
            }
        } catch (RuntimeException e) {
            throw new RuntimeException("Interrupted while setCameraPreviewSize.", e);
        }
        return true;
    }

    private class JavaCamera2Frame implements CvCameraViewFrame {
        @Override
        public Mat gray() {
            Image.Plane[] planes = mImage.getPlanes();
            int w = mImage.getWidth();
            int h = mImage.getHeight();
            assert (planes[0].getPixelStride() == 1);
            ByteBuffer y_plane = planes[0].getBuffer();
            int y_plane_step = planes[0].getRowStride();
            mGray = new Mat(h, w, CvType.CV_8UC1, y_plane, y_plane_step);
            return mGray;
        }

        @Override
        public Mat rgba() {
            Image.Plane[] planes = mImage.getPlanes();
            int w = mImage.getWidth();
            int h = mImage.getHeight();
            int chromaPixelStride = planes[1].getPixelStride();

            if (chromaPixelStride == 2) { // Chroma channels are interleaved
                assert (planes[0].getPixelStride() == 1);
                assert (planes[2].getPixelStride() == 2);
                ByteBuffer y_plane = planes[0].getBuffer();
                int y_plane_step = planes[0].getRowStride();
                ByteBuffer uv_plane1 = planes[1].getBuffer();
                int uv_plane1_step = planes[1].getRowStride();
                ByteBuffer uv_plane2 = planes[2].getBuffer();
                int uv_plane2_step = planes[2].getRowStride();

                Mat y_mat = new Mat(h, w, CvType.CV_8UC1, y_plane, y_plane_step);
                Mat uv_mat1 = new Mat(h / 2, w / 2, CvType.CV_8UC2, uv_plane1, uv_plane1_step);
                Mat uv_mat2 = new Mat(h / 2, w / 2, CvType.CV_8UC2, uv_plane2, uv_plane2_step);
                long addr_diff = uv_mat2.dataAddr() - uv_mat1.dataAddr();
                if (addr_diff > 0) {
                    assert (addr_diff == 1);
                    Imgproc.cvtColorTwoPlane(y_mat, uv_mat1, mRgba, Imgproc.COLOR_YUV2RGBA_NV12);
                } else {
                    assert (addr_diff == -1);
                    Imgproc.cvtColorTwoPlane(y_mat, uv_mat2, mRgba, Imgproc.COLOR_YUV2RGBA_NV21);
                }


                y_mat.release();
                uv_mat1.release();
                uv_mat2.release();

                y_mat = null;
                uv_mat1 = null;
                uv_mat2 = null;


                return mRgba;
            } else { // Chroma channels are not interleaved
                byte[] yuv_bytes = new byte[w * (h + h / 2)];
                ByteBuffer y_plane = planes[0].getBuffer();
                ByteBuffer u_plane = planes[1].getBuffer();
                ByteBuffer v_plane = planes[2].getBuffer();

                int yuv_bytes_offset = 0;

                int y_plane_step = planes[0].getRowStride();
                if (y_plane_step == w) {
                    y_plane.get(yuv_bytes, 0, w * h);
                    yuv_bytes_offset = w * h;
                } else {
                    int padding = y_plane_step - w;
                    for (int i = 0; i < h; i++) {
                        y_plane.get(yuv_bytes, yuv_bytes_offset, w);
                        yuv_bytes_offset += w;
                        if (i < h - 1) {
                            y_plane.position(y_plane.position() + padding);
                        }
                    }
                    assert (yuv_bytes_offset == w * h);
                }

                int chromaRowStride = planes[1].getRowStride();
                int chromaRowPadding = chromaRowStride - w / 2;

                if (chromaRowPadding == 0) {
                    // When the row stride of the chroma channels equals their width, we can copy
                    // the entire channels in one go
                    u_plane.get(yuv_bytes, yuv_bytes_offset, w * h / 4);
                    yuv_bytes_offset += w * h / 4;
                    v_plane.get(yuv_bytes, yuv_bytes_offset, w * h / 4);
                } else {
                    // When not equal, we need to copy the channels row by row
                    for (int i = 0; i < h / 2; i++) {
                        u_plane.get(yuv_bytes, yuv_bytes_offset, w / 2);
                        yuv_bytes_offset += w / 2;
                        if (i < h / 2 - 1) {
                            u_plane.position(u_plane.position() + chromaRowPadding);
                        }
                    }
                    for (int i = 0; i < h / 2; i++) {
                        v_plane.get(yuv_bytes, yuv_bytes_offset, w / 2);
                        yuv_bytes_offset += w / 2;
                        if (i < h / 2 - 1) {
                            v_plane.position(v_plane.position() + chromaRowPadding);
                        }
                    }
                }

                Mat yuv_mat = new Mat(h + h / 2, w, CvType.CV_8UC1);
                yuv_mat.put(0, 0, yuv_bytes);
                Imgproc.cvtColor(yuv_mat, mRgba, Imgproc.COLOR_YUV2RGBA_I420, 4);
                return mRgba;
            }
        }


        public JavaCamera2Frame(Image image) {
            super();
            mImage = image;
            mRgba = new Mat();
            mGray = new Mat();
        }

        public void release() {
            mRgba.release();
            mGray.release();
        }

        private Image mImage;
        private Mat mRgba;
        private Mat mGray;
    }

    ;


    public boolean isPortrait() {
        return isPortrait;
    }

    public void setPortrait(boolean portrait) {
        isPortrait = portrait;
    }
}
