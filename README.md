# MakeUp

## What is it?

This is android application that I wrote. It is VR application, it uses stream from camera, and add to face effects for make-up: it could paint lips, eyes, eyelashes.

# Table of Contests

- [What is it?](#what-is-it)
- [Settings](#settings)

## Settings

You should download trained shape predictor file from [here](http://dlib.net/files/shape_predictor_68_face_landmarks.dat.bz2) or could trian it by yourself. Unzip it, rename it to sp68.dat and put in dir [assets](https://github.com/oleg-sta/Masks/tree/master/assets). This file is very large so I don't store on version control. Yes, it's bad, but I made simple bad solution.
Make settings to submodule [commonLib](https://github.com/oleg-sta/commonLibMask).

## How it works

It is a very complex application that uses camera, machine learning libraries written on C, OpenGL scripts and it should work very fast because it works online. You could read about main logic in my submodule [here](https://github.com/oleg-sta/commonLibMask) with all description of logic in it.

Application could be described by picture below:

![image](./doc/diagram.png)

### Android Camera
To get image from camera there used Camera API. For this we should create view in layout:
```
<ru.flightlabs.masks.camera.FastCameraView
    android:id="@+id/fd_fase_surface_view"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```
Our class should extends SurfaceView:
```
public class FastCameraView extends SurfaceView
```
Before initializing camera you should find appropriate index for camera, you need to get CameraInfo and using number of cameras you should find suitable, e.g. front camera:
```
numberOfCameras = android.hardware.Camera.getNumberOfCameras();
android.hardware.Camera.CameraInfo cameraInfo = new android.hardware.Camera.CameraInfo();
for (int i = 0; i < numberOfCameras; i++) {
    android.hardware.Camera.getCameraInfo(i, cameraInfo);
    if (cameraInfo.facing == android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT) {
        cameraFacing = true;
        cameraIndex = i;
    }
}
```
To initialize camera you should provide parameters like: image type, size and callback for preview. We don't want to show preview on View, becuase we want to apply effect on the frame using other techniques.
```
mCamera = Camera.open(cameraIndex);
Camera.Parameters params = mCamera.getParameters();
```
NV21 format is more appropriate because it has grey and colour in separate parts of frame. More info about this you could find on [wikipedia](https://en.wikipedia.org/wiki/YUV)
```
params.setPreviewFormat(ImageFormat.NV21);
```
You can't just use any size of camera you can only use specific size, here is used camera helper to find such size, we try to find most appropriate size by size and proportion.
```
CameraHelper.calculateCameraPreviewSize(params, previewHeightLocal, previewWidthLocal);
cameraWidth = params.getPreviewSize().width;
cameraHeight = params.getPreviewSize().height;
mCamera.setParameters(params);
```
The next step is to find size for buffer in bytes
```
int size = cameraWidth * cameraHeight;
size  = size * ImageFormat.getBitsPerPixel(params.getPreviewFormat()) / 8;
mBuffer = new byte[size];
```
We set up callback and buffer for image
```
mCamera.addCallbackBuffer(mBuffer);
mCamera.setPreviewCallbackWithBuffer(this);
```
and don't preview
```
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
    mSurfaceTexture = new SurfaceTexture(MAGIC_TEXTURE_ID);
    mCamera.setPreviewTexture(mSurfaceTexture);
} else {
    mCamera.setPreviewDisplay(null);
}
```
and start camera to work
```
mCamera.startPreview();
```
For each frame camera's callnback will be called, we are using frameCamera as temporal buffer for other manipulation, our task here is to copy infomration from buffer and return result to wait for the next frame
```
@Override
public void onPreviewFrame(byte[] data, Camera camera) {
    frameCamera.cameraWidth = cameraWidth;
    frameCamera.cameraHeight = cameraHeight;
    frameCamera.facing = cameraFacing;
    if (frameCamera.bufferFromCamera == null || frameCamera.bufferFromCamera.length != data.length) {
        frameCamera.bufferFromCamera = new byte[data.length];
    }
    System.arraycopy(data, 0, frameCamera.bufferFromCamera, 0, data.length);
    // we should add buffer to queue, dut to buffer
    mCamera.addCallbackBuffer(mBuffer);
}
```