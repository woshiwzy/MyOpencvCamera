package com.demo.cv42.custom;

import android.content.Context;
import android.util.Log;

import com.demo.cv42.App;

import org.opencv.core.Mat;
import org.opencv.core.Size;

import java.util.List;

public abstract class CameraDataGeterBase {

    private static final String TAG = "CameraBridge";
    protected static final int MAX_UNSPECIFIED = -1;
    private static final int STOPPED = 0;
    private static final int STARTED = 1;

    private int mState = STOPPED;
    protected CvCameraViewListener2 mListener;
    private final Object mSyncObject = new Object();

    protected int mFrameWidth;
    protected int mFrameHeight;

    protected int mMaxHeight;
    protected int mMaxWidth;

    protected int mCameraIndex = CAMERA_ID_ANY;
    protected boolean mEnabled = true;

    public static final int CAMERA_ID_ANY = -1;
    public static final int CAMERA_ID_BACK = 99;
    public static final int CAMERA_ID_FRONT = 98;
    public static final int RGBA = 1;
    public static final int GRAY = 2;

    private Context context;

    protected int defaultConnectWidth = 1080, defaultConnectHeight = 1920;

    public CameraDataGeterBase(Context context, int cameraId, int defaultConnectWidth, int defaultConnectHeight) {
        this.context = context;
        this.mCameraIndex = cameraId;
        this.defaultConnectWidth = defaultConnectWidth;
        this.defaultConnectHeight = defaultConnectHeight;
        this.mMaxWidth = MAX_UNSPECIFIED;
        this.mMaxHeight = MAX_UNSPECIFIED;
    }

    public void setCameraIndex(int cameraIndex) {
        this.mCameraIndex = cameraIndex;
    }

    public int getCameraIndex() {
        return mCameraIndex;
    }

    public interface CvCameraViewListener {
        public void onCameraViewStarted(int width, int height);

        public void onCameraViewStopped();

        public Mat onCameraFrame(Mat inputFrame);
    }

    public interface CvCameraViewListener2 {
        public void onCameraViewStarted(int width, int height);

        public void onCameraViewStopped();

        public Mat onCameraFrame(Mat rgbaMat);
    }


    protected class CvCameraViewListenerAdapter implements CvCameraViewListener2 {
        public CvCameraViewListenerAdapter(CvCameraViewListener oldStypeListener) {
            mOldStyleListener = oldStypeListener;
        }

        public void onCameraViewStarted(int width, int height) {
            mOldStyleListener.onCameraViewStarted(width, height);
        }

        public void onCameraViewStopped() {
            mOldStyleListener.onCameraViewStopped();
        }

        public Mat onCameraFrame(Mat rgbaMat) {
            Log.e(App.tag, "获得数据帧");
            Mat result = null;
            result = mOldStyleListener.onCameraFrame(rgbaMat);
            return result;
        }

        public void setFrameFormat(int format) {
            mPreviewFormat = format;
        }

        private int mPreviewFormat = RGBA;
        private CvCameraViewListener mOldStyleListener;
    }

    ;

    /**
     * This class interface is abstract representation of single frame from camera for onCameraFrame callback
     * Attention: Do not use objects, that represents this interface out of onCameraFrame callback!
     */
    public interface CvCameraViewFrame {

        /**
         * This method returns RGBA Mat with frame
         */
        public Mat rgba();

        /**
         * This method returns single channel gray scale Mat with frame
         */
        public Mat gray();
    }


    /**
     * This method is provided for clients, so they can enable the camera connection.
     * The actual onCameraViewStarted callback will be delivered only after setCameraPermissionGranted
     * and enableView have been called and surface is available
     */
    public void enableView() {
        synchronized (mSyncObject) {
            mEnabled = true;
            checkCurrentState();
        }
    }

    /**
     * This method is provided for clients, so they can disable camera connection and stop
     * the delivery of frames even though the surface view itself is not destroyed and still stays on the scren
     */
    public void disableView() {
        synchronized (mSyncObject) {
            mEnabled = false;
            checkCurrentState();
        }
    }


    /**
     * @param listener
     */
    public void setCvCameraViewListener(CvCameraViewListener2 listener) {
        mListener = listener;
    }


    /**
     * This method sets the maximum size that camera frame is allowed to be. When selecting
     * size - the biggest size which less or equal the size set will be selected.
     * As an example - we set setMaxFrameSize(200,200) and we have 176x152 and 320x240 sizes. The
     * preview frame will be selected with 176x152 size.
     * This method is useful when need to restrict the size of preview frame for some reason (for example for video recording)
     *
     * @param maxWidth  - the maximum width allowed for camera frame.
     * @param maxHeight - the maximum height allowed for camera frame
     */
    public void setMaxFrameSize(int maxWidth, int maxHeight) {
        mMaxWidth = maxWidth;
        mMaxHeight = maxHeight;
    }


    /**
     * Called when mSyncObject lock is held
     */
    private void checkCurrentState() {
        Log.d(TAG, "call checkCurrentState");
        int targetState;

        if (mEnabled) {
            targetState = STARTED;
        } else {
            targetState = STOPPED;
        }

        if (targetState != mState) {
            /* The state change detected. Need to exit the current state and enter target state */
            processExitState(mState);
            mState = targetState;
            processEnterState(mState);
        }
    }

    private void processEnterState(int state) {
        Log.d(TAG, "call processEnterState: " + state);
        switch (state) {
            case STARTED:
                onEnterStartedState(defaultConnectWidth, defaultConnectHeight);
                if (mListener != null) {
                    mListener.onCameraViewStarted(mFrameWidth, mFrameHeight);
                }
                break;
            case STOPPED:
                onEnterStoppedState();
                if (mListener != null) {
                    mListener.onCameraViewStopped();
                }
                break;
        }
        ;
    }

    private void onEnterStartedState(int width, int height) {
        Log.d(TAG, "call onEnterStartedState");
        boolean success = connectCamera(width, height);
        Log.d(TAG, "connect camera is success :" + (success ? " success " : " fail "));
    }

    private void processExitState(int state) {
        Log.d(TAG, "call processExitState: " + state);
        switch (state) {
            case STARTED:
                onExitStartedState();
                break;
            case STOPPED:
                onExitStoppedState();
                break;
        }
    }

    private void onEnterStoppedState() {
    }

    private void onExitStoppedState() {
    }


    private void onExitStartedState() {
        disconnectCamera();
    }

    protected void deliverAndDrawFrame(Mat rgbaMat) {
        if (mListener != null) {
            mListener.onCameraFrame(rgbaMat);
        }
    }

    protected abstract boolean connectCamera(int width, int height);

    protected abstract void disconnectCamera();


    public interface ListItemAccessor {
        public int getWidth(Object obj);

        public int getHeight(Object obj);
    }

    /**
     * This helper method can be called by subclasses to select camera preview size.
     * It goes over the list of the supported preview sizes and selects the maximum one which
     * fits both values set via setMaxFrameSize() and surface frame allocated for this view
     *
     * @param supportedSizes
     * @param surfaceWidth
     * @param surfaceHeight
     * @return optimal frame size
     */
    protected Size calculateCameraFrameSize(List<?> supportedSizes, ListItemAccessor accessor, int surfaceWidth, int surfaceHeight) {
        int calcWidth = 0;
        int calcHeight = 0;

        int maxAllowedWidth = (mMaxWidth != MAX_UNSPECIFIED && mMaxWidth < surfaceWidth) ? mMaxWidth : surfaceWidth;
        int maxAllowedHeight = (mMaxHeight != MAX_UNSPECIFIED && mMaxHeight < surfaceHeight) ? mMaxHeight : surfaceHeight;

        for (Object size : supportedSizes) {
            int width = accessor.getWidth(size);
            int height = accessor.getHeight(size);
            Log.d(TAG, "trying size: " + width + "x" + height);

            if (width <= maxAllowedWidth && height <= maxAllowedHeight) {
                if (width >= calcWidth && height >= calcHeight) {
                    calcWidth = (int) width;
                    calcHeight = (int) height;
                }
            }
        }
        if ((calcWidth == 0 || calcHeight == 0) && supportedSizes.size() > 0) {
            Log.i(TAG, "fallback to the first frame size");
            Object size = supportedSizes.get(0);
            calcWidth = accessor.getWidth(size);
            calcHeight = accessor.getHeight(size);
        }

        return new Size(calcWidth, calcHeight);
//        return new Size(480, 320);
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}