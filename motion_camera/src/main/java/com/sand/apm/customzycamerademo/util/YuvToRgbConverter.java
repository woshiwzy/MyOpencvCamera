package com.sand.apm.customzycamerademo.util;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.media.Image;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;

import java.nio.ByteBuffer;

public final class YuvToRgbConverter {

    private RenderScript renderScript;
    private ScriptIntrinsicYuvToRGB scriptYuvToRgb;

    private int pixelCount = -1;
    private ByteBuffer yuvBuffer;
    //private Allocation inputAllocation;
    //private Allocation outputAllocation;
    private byte[] rowData;

    public YuvToRgbConverter(Context context) {
        this.renderScript = RenderScript.create(context);
        this.scriptYuvToRgb = ScriptIntrinsicYuvToRGB.create(renderScript, Element.U8_4(renderScript));
    }

    private void check(Image image) {
        if (yuvBuffer == null) {
            pixelCount = image.getCropRect().width() * image.getCropRect().height();
            yuvBuffer = ByteBuffer.allocateDirect(
                    pixelCount * ImageFormat.getBitsPerPixel(ImageFormat.YUV_420_888) / 8);
        }
    }

//    public synchronized byte[] imageToByteBuffer(Image image) {
//        return imageToByteBuffer(image, yuvBuffer.array());
//    }
//
//    public synchronized byte[] yuvToRgb(Image image, boolean isMirror, Bitmap output) {
//
//        // Ensure that the intermediate output byte buffer is allocated
//        check(image);
//
//        // Get the YUV data in byte array form
//        imageToByteBuffer(image);
//
//        // yuv
//        byte[] yuv = yuvBuffer.array();
//        if (isMirror) {
//            ZyYUVUtil.nv21Mirror(yuv, image.getWidth(), image.getHeight());
//        }
//
//        // Ensure that the RenderScript inputs and outputs are allocated
//        if (inputAllocation == null) {
//            inputAllocation = Allocation.createSized(renderScript, Element.U8(renderScript), yuv.length);
//        }
//
//        if (outputAllocation == null) {
//            outputAllocation = Allocation.createFromBitmap(renderScript, output);
//        }
//
//        // Convert YUV to RGB
//        inputAllocation.copyFrom(yuv);
//        scriptYuvToRgb.setInput(inputAllocation);
//        scriptYuvToRgb.forEach(outputAllocation);
//        outputAllocation.copyTo(output);
//
//        return yuv;
//    }

    private byte[] yuvBufferByte=null;
    public synchronized byte[] imageToByteBuffer(Image image){
        int i420Size = image.getWidth() * image.getHeight() * 3 / 2;
        if(null==yuvBufferByte){
            yuvBufferByte=new byte[i420Size];
        }
        if(null!=yuvBufferByte && yuvBufferByte.length!=i420Size){
            yuvBufferByte=null;
            yuvBufferByte=new byte[i420Size];
        }

        return imageToByteBuffer(image,yuvBufferByte);
    }

    public synchronized byte[] imageToByteBuffer(Image image, byte[] out) {
        assert (image.getFormat() == ImageFormat.YUV_420_888);

        check(image);
        Rect imageCrop = image.getCropRect();
        Image.Plane[] imagePlanes = image.getPlanes();
        if (rowData == null) rowData = new byte[imagePlanes[0].getRowStride()];

        for (int planeIndex = 0; planeIndex < imagePlanes.length; planeIndex++) {

            Image.Plane plane = imagePlanes[planeIndex];

            // How many values are read in input for each output value written
            // Only the Y plane has a value for every pixel, U and V have half the resolution i.e.
            //
            // Y Plane            U Plane    V Plane
            // ===============    =======    =======
            // Y Y Y Y Y Y Y Y    U U U U    V V V V
            // Y Y Y Y Y Y Y Y    U U U U    V V V V
            // Y Y Y Y Y Y Y Y    U U U U    V V V V
            // Y Y Y Y Y Y Y Y    U U U U    V V V V
            // Y Y Y Y Y Y Y Y
            // Y Y Y Y Y Y Y Y
            // Y Y Y Y Y Y Y Y

            // The index in the output buffer the next value will be written at
            // For Y it's zero, for U and V we start at the end of Y and interleave them i.e.
            //
            // First chunk        Second chunk
            // ===============    ===============
            // Y Y Y Y Y Y Y Y    U V U V U V U V
            // Y Y Y Y Y Y Y Y    U V U V U V U V
            // Y Y Y Y Y Y Y Y    U V U V U V U V
            // Y Y Y Y Y Y Y Y    U V U V U V U V
            // Y Y Y Y Y Y Y Y
            // Y Y Y Y Y Y Y Y
            // Y Y Y Y Y Y Y Y
            int outputStride = 0;
            int outputOffset = 0;

            if (planeIndex == 0) {
                outputStride = 1;
                outputOffset = 0;
            } else if (planeIndex == 1) {
                outputStride = 2;
                outputOffset = pixelCount + 1;
            } else if (planeIndex == 2) {
                outputStride = 2;
                outputOffset = pixelCount;
            } else {
                return null;
            }

            ByteBuffer buffer = plane.getBuffer();
            int rowStride = plane.getRowStride();
            int pixelStride = plane.getPixelStride();

            // We have to divide the width and height by two if it's not the Y plane
            Rect planeCrop;
            if (planeIndex == 0) {
                planeCrop = imageCrop;
            } else {
                planeCrop = new Rect(
                        imageCrop.left / 2,
                        imageCrop.top / 2,
                        imageCrop.right / 2,
                        imageCrop.bottom / 2
                );
            }

            int planeWidth = planeCrop.width();
            int planeHeight = planeCrop.height();

            buffer.position(rowStride * planeCrop.top + pixelStride * planeCrop.left);
            for (int row = 0; row < planeHeight; row++) {
                int length;
                if (pixelStride == 1 && outputStride == 1) {
                    // When there is a single stride value for pixel and output, we can just copy
                    // the entire row in a single step
                    length = planeWidth;
                    buffer.get(out, outputOffset, length);
                    outputOffset += length;
                } else {
                    // When either pixel or output have a stride > 1 we must copy pixel by pixel
                    length = (planeWidth - 1) * pixelStride + 1;
                    buffer.get(rowData, 0, length);
                    for (int col = 0; col < planeWidth; col++) {
                        out[outputOffset] = rowData[col * pixelStride];
                        outputOffset += outputStride;
                    }
                }

                if (row < planeHeight - 1) {
                    buffer.position(buffer.position() + rowStride - length);
                }
            }
        }

        return out;
    }
}
