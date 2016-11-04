package org.opencv.android;

import java.util.List;

import org.opencv.android.CameraBridgeViewBase.ListItemAccessor;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;

public class PortraitCameraView extends CameraBridgeViewBase implements PreviewCallback {

private static final int MAGIC_TEXTURE_ID = 10;
private static final String TAG = "JavaCameraView";

private byte mBuffer[];
private Mat[] mFrameChain;
private int mChainIdx = 0;
private Thread mThread;
private boolean mStopThread;

protected Camera mCamera;
protected JavaCameraFrame[] mCameraFrame;
private SurfaceTexture mSurfaceTexture;
private int mCameraId;
private boolean cameraFacing;

public static class JavaCameraSizeAccessor implements ListItemAccessor {

    public int getWidth(Object obj) {
        Camera.Size size = (Camera.Size) obj;
        return size.width;
    }

    public int getHeight(Object obj) {
        Camera.Size size = (Camera.Size) obj;
        return size.height;
    }
}

public PortraitCameraView(Context context, int cameraId) {
    super(context, cameraId);
}

public PortraitCameraView(Context context, AttributeSet attrs) {
    super(context, attrs);
}

protected boolean initializeCamera(int width, int height) {
    Log.d(TAG, "Initialize java camera");
    boolean result = true;
    synchronized (this) {
        mCamera = null;

        boolean connected = false;
        int numberOfCameras = android.hardware.Camera.getNumberOfCameras();
        android.hardware.Camera.CameraInfo cameraInfo = new android.hardware.Camera.CameraInfo();
        
        mCamera = Camera.open(mCameraIndex);
        mCameraId = mCameraIndex;
        connected = true;
        cameraFacing = false;
        android.hardware.Camera.getCameraInfo(mCameraId, cameraInfo);
        cameraFacing = cameraInfo.facing == android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT;
        for (int i = 0; i < numberOfCameras && false; i++) {
            android.hardware.Camera.getCameraInfo(i, cameraInfo);
//            if (cameraInfo.facing == android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT) {
//                try {
//                    mCamera = Camera.open(i);
//                    mCameraId = i;
//                    connected = true;
//                } catch (RuntimeException e) {
//                    Log.e(TAG, "Camera #" + i + "failed to open: " + e.getMessage());
//                }
//                if (connected) break;
//            }
        }

        if (mCamera == null) return false;

        /* Now set camera parameters */
        try {
            Camera.Parameters params = mCamera.getParameters();
            Log.d(TAG, "getSupportedPreviewSizes()");
            List<Camera.Size> sizes = params.getSupportedPreviewSizes();

            if (sizes != null) {
                /* Select the size that fits surface considering maximum size allowed */
                Size frameSize = calculateCameraFrameSize(sizes, new JavaCameraSizeAccessor(), height, width); //use turn around values here to get the correct prev size for portrait mode

                params.setPreviewFormat(ImageFormat.NV21);
                Log.d(TAG, "Set preview size to " + Integer.valueOf((int)frameSize.width) + "x" + Integer.valueOf((int)frameSize.height));
                params.setPreviewSize((int)frameSize.width, (int)frameSize.height);
                //params.setPreviewSize((int)frameSize.width, (int)frameSize.width);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                    params.setRecordingHint(true);

                List<String> FocusModes = params.getSupportedFocusModes();
                if (FocusModes != null && FocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO))
                {
                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                }

                mCamera.setParameters(params);
                params = mCamera.getParameters();

                mFrameWidth = params.getPreviewSize().height; //the frame width and height of the super class are used to generate the cached bitmap and they need to be the size of the resulting frame
                mFrameHeight = params.getPreviewSize().width;

                int realWidth = mFrameHeight; //the real width and height are the width and height of the frame received in onPreviewFrame
                int realHeight = mFrameWidth;

                if ((getLayoutParams().width == LayoutParams.MATCH_PARENT) && (getLayoutParams().height == LayoutParams.MATCH_PARENT))
                    mScale = Math.min(((float)height)/mFrameHeight, ((float)width)/mFrameWidth);
                else
                    mScale = 0;

                if (mFpsMeter != null) {
                    mFpsMeter.setResolution(mFrameWidth, mFrameHeight);
                }

                int size = mFrameWidth * mFrameHeight;
                size  = size * ImageFormat.getBitsPerPixel(params.getPreviewFormat()) / 8;
                mBuffer = new byte[size];

                mCamera.addCallbackBuffer(mBuffer);
                mCamera.setPreviewCallbackWithBuffer(this);

                mFrameChain = new Mat[2];
                mFrameChain[0] = new Mat(realHeight + (realHeight/2), realWidth, CvType.CV_8UC1); //the frame chane is still in landscape
                mFrameChain[1] = new Mat(realHeight + (realHeight/2), realWidth, CvType.CV_8UC1);

                AllocateCache();

                mCameraFrame = new JavaCameraFrame[2];
                mCameraFrame[0] = new JavaCameraFrame(mFrameChain[0], mFrameWidth, mFrameHeight); //the camera frame is in portrait
                mCameraFrame[1] = new JavaCameraFrame(mFrameChain[1], mFrameWidth, mFrameHeight);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    mSurfaceTexture = new SurfaceTexture(MAGIC_TEXTURE_ID);
                    mCamera.setPreviewTexture(mSurfaceTexture);
                } else
                   mCamera.setPreviewDisplay(null);

                /* Finally we are ready to start the preview */
                Log.d(TAG, "startPreview");
                mCamera.startPreview();
            }
            else
                result = false;
        } catch (Exception e) {
            result = false;
            e.printStackTrace();
        }
    }

    return result;
}

protected Size calculateCameraFrameSize(List<?> supportedSizes, ListItemAccessor accessor, int surfaceWidth, int surfaceHeight) {
    int calcWidth = 0;
    int calcHeight = 0;
    
    int minSize = Math.min(surfaceWidth, surfaceHeight);

    //int maxAllowedWidth = (mMaxWidth != MAX_UNSPECIFIED && mMaxWidth < surfaceWidth)? mMaxWidth : surfaceWidth;
    //int maxAllowedHeight = (mMaxHeight != MAX_UNSPECIFIED && mMaxHeight < surfaceHeight)? mMaxHeight : surfaceHeight;

    for (Object size : supportedSizes) {
        int width = accessor.getWidth(size);
        int height = accessor.getHeight(size);

        if (width <= minSize || height <= minSize) {
            if (width >= calcWidth && height >= calcHeight) {
                calcWidth = (int) width;
                calcHeight = (int) height;
            }
        }
    }

    return new Size(calcWidth, calcHeight);
}

protected void releaseCamera() {
    synchronized (this) {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);

            mCamera.release();
        }
        mCamera = null;
        if (mFrameChain != null) {
            mFrameChain[0].release();
            mFrameChain[1].release();
        }
        if (mCameraFrame != null) {
            mCameraFrame[0].release();
            mCameraFrame[1].release();
        }
    }
}

@Override
protected boolean connectCamera(int width, int height) {

    /* 1. We need to instantiate camera
     * 2. We need to start thread which will be getting frames
     */
    /* First step - initialize camera connection */
    Log.d(TAG, "Connecting to camera");
    if (!initializeCamera(width, height))
        return false;

    /* now we can start update thread */
    Log.d(TAG, "Starting processing thread");
    mStopThread = false;
    mThread = new Thread(new CameraWorker());
    mThread.start();

    return true;
}

protected void disconnectCamera() {
    /* 1. We need to stop thread which updating the frames
     * 2. Stop camera and release it
     */
    Log.d(TAG, "Disconnecting from camera");
    try {
        mStopThread = true;
        Log.d(TAG, "Notify thread");
        synchronized (this) {
            this.notify();
        }
        Log.d(TAG, "Wating for thread");
        if (mThread != null)
            mThread.join();
    } catch (InterruptedException e) {
        e.printStackTrace();
    } finally {
        mThread =  null;
    }

    /* Now release camera */
    releaseCamera();
}

public void onPreviewFrame(byte[] frame, Camera arg1) {
    Log.d(TAG, "Preview Frame received. Frame size: " + frame.length);
    synchronized (this) {
        mFrameChain[1 - mChainIdx].put(0, 0, frame);
        this.notify();
    }
    if (mCamera != null)
        mCamera.addCallbackBuffer(mBuffer);
}

private class JavaCameraFrame implements CvCameraViewFrame {
    private Mat mYuvFrameData;
    private Mat mRgba;
    private int mWidth;
    private int mHeight;
    private Mat mRotatedGray;
    private Mat mRotated;

    public Mat gray() {
        if (mRotatedGray != null) mRotatedGray.release();
        mRotatedGray = mYuvFrameData.submat(0, mWidth, 0, mHeight); //submat with reversed width and height because its done on the landscape frame
        mRotatedGray = mRotatedGray.t();
        Core.flip(mRotatedGray, mRotatedGray, cameraFacing? -1 : 1);
        return mRotatedGray;
    }

    public Mat rgba() {
        Imgproc.cvtColor(mYuvFrameData, mRgba, Imgproc.COLOR_YUV2BGR_NV12, 4);
        if (mRotated != null) mRotated.release();
        mRotated = mRgba.t();
        Core.flip(mRotated, mRotated, cameraFacing? -1 : 1);
        return mRotated;
    }

    public JavaCameraFrame(Mat Yuv420sp, int width, int height) {
        super();
        mWidth = width;
        mHeight = height;
        mYuvFrameData = Yuv420sp;
        mRgba = new Mat();
    }

    public void release() {
        mRgba.release();
        if (mRotated != null) mRotated.release();
        if (mRotatedGray != null) mRotatedGray.release();
    }


};

private class CameraWorker implements Runnable {

    public void run() {
        do {
            synchronized (PortraitCameraView.this) {
                try {
                    PortraitCameraView.this.wait();
                } catch (InterruptedException e) {
                    Log.e(TAG, "CameraWorker interrupted", e);
                }
            }

            if (!mStopThread) {
                if (!mFrameChain[mChainIdx].empty())
                    deliverAndDrawFrame(mCameraFrame[mChainIdx]);
                mChainIdx = 1 - mChainIdx;
            }
        } while (!mStopThread);
        Log.d(TAG, "Finish processing thread");
    }
}
}
