/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.cameraview;

import android.content.res.Resources;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.View;

import java.util.Set;

abstract class CameraViewImpl {

    protected final Callback mCallback;

    protected final PreviewImpl mPreview;

    CameraViewImpl(Callback callback, PreviewImpl preview) {
        mCallback = callback;
        mPreview = preview;
    }

    View getView() {
        return mPreview.getView();
    }

    /**
     * @return {@code true} if the implementation was able to start the camera session.
     */
    abstract boolean start();

    abstract void stop();

    abstract boolean isCameraOpened();

    abstract void setFacing(int facing);

    abstract int getFacing();

    abstract Set<AspectRatio> getSupportedAspectRatios();

    /**
     * @return {@code true} if the aspect ratio was changed.
     */
    abstract boolean setAspectRatio(AspectRatio ratio);

    abstract AspectRatio getAspectRatio();

    abstract void setAutoFocus(boolean autoFocus);

    abstract boolean getAutoFocus();

    abstract void setFlash(int flash);

    abstract int getFlash();

    abstract void takePicture();

    abstract void setExceptPictureConfig(Size size, int format);

    abstract void takePreviewFrame();

    abstract void setOneShotPreviewCallback(PreviewCallback callback, Looper looper);

    /**
     * 优先级高于setAspectRatio
     * 如果使用此方法设置 则不应该再调用setAspectRatio
     */
    abstract void setExceptAspectRatio(AspectRatio ratio);

    abstract AspectRatio getExceptAspectRatio();

    abstract void setDisplayOrientation(int displayOrientation);

    protected AspectRatio chooseOptimalAspectRatio(AspectRatio exceptAspectRatio, SizeMap sizeMap) {
        if (exceptAspectRatio == null) {
            return null;
        }
        DisplayMetrics dm = Resources.getSystem().getDisplayMetrics();
        int screenLonger = dm.widthPixels;
        int screenShorter = dm.heightPixels;
        if (screenLonger < screenShorter) {
            screenLonger = screenShorter;
            screenShorter = dm.widthPixels;
        }
        Set<AspectRatio> ratios = getSupportedAspectRatios();
        float exceptAspectRatioF = exceptAspectRatio.toFloat();
        float minDiff = Float.MAX_VALUE;
        float retDiff = Float.MAX_VALUE;
        final float MAX_ASPECT_DISTORTION = 0.15f;
        AspectRatio retRatio = null;
        AspectRatio retRatio1 = null;
        int sizeLonger;
        int sizeShorter;
        for (AspectRatio ratio : ratios) {
            float diff = Math.abs(ratio.toFloat() - exceptAspectRatioF);
            if (diff < MAX_ASPECT_DISTORTION && diff < retDiff) {
                retRatio1 = ratio;
                // 优先选择具有比屏幕像素大的比例
                for (Size size : sizeMap.sizes(ratio)) {
                    if (size.getWidth() > size.getHeight()) {
                        sizeLonger = size.getWidth();
                        sizeShorter = size.getHeight();
                    } else {
                        sizeLonger = size.getHeight();
                        sizeShorter = size.getWidth();
                    }
                    if (sizeLonger >= screenLonger && sizeShorter >= screenShorter) {
                        retDiff = diff;
                        retRatio = ratio;
                        break;
                    }
                }
            }
            if (diff < minDiff) {
                minDiff = diff;
                retRatio1 = ratio;
            }
        }
        if (retRatio != null) {
            return retRatio;
        }
        return retRatio1;
    }

    interface Callback {

        void onCameraOpened();

        void onCameraClosed();

        void onPictureTaken(ImageData imageData);

        void onPreviewFrame(ImageData imageData);

        void onError(Throwable error, String desc);

    }
}
