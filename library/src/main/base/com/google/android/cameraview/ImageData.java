package com.google.android.cameraview;

import android.annotation.TargetApi;
import android.graphics.ImageFormat;
import android.media.Image;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by yinhf on 2017/7/4.
 */

public class ImageData {
    private static final String TAG = "ImageData";

    private int width;
    private int height;
    private int format;
    private byte[] data;
    private Image image;

    public ImageData(byte[] data, int width, int height, int format) {
        this.data = data;
        this.width = width;
        this.height = height;
        this.format = format;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public ImageData(Image image) {
        this.image = image;
        this.width = image.getWidth();
        this.height = image.getHeight();
        this.format = image.getFormat();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getFormat() {
        return format;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void close() {
        if (image != null) {
            image.close();
        }
    }

    public byte[] getNV21() {
        if (data != null) {
            if (format != ImageFormat.NV21) {
                Log.e(TAG, "getNV21: data != null && format != NV21");
                return null;
            }
            return data;
        } else if (image != null) {
            if (format != ImageFormat.YUV_420_888) {
                Log.e(TAG, "getNV21: image != null && format != YUV_420_888");
                return null;
            }
            return ImageUtils.YUV_420_888toNV21(image);
        }
        return null;
    }

    public byte[] getJPEG() {
        if (format != ImageFormat.JPEG) {
            Log.e(TAG, "getJPEG: format != JPEG");
            return null;
        }
        byte[] jpegData = data;
        if (jpegData == null && image != null) {
            jpegData = ImageUtils.imageToJPEG(image);
        }
        return jpegData;
    }

    public boolean compressToJpeg(File file) {
        byte[] jpegData = getJPEG();
        return jpegData != null && writeToFile(jpegData, file);
    }

    private boolean writeToFile(byte[] data, File file) {
        OutputStream os = null;
        try {
            os = new FileOutputStream(file);
            os.write(data);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }
}
