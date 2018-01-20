package com.yezi.openglmedia.utils;

import android.opengl.Matrix;

import java.nio.IntBuffer;

public class GL2Utils {

    public static float[] flip(float[] m, boolean x, boolean y) {
        if (x || y) {
            Matrix.scaleM(m, 0, x ? -1 : 1, y ? -1 : 1, 1);
        }
        return m;
    }

    public static float[] getOriginalMatrix() {
        return new float[]{
                1, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1
        };
    }

    public static IntBuffer convertMirroredImage(IntBuffer image, int width, int height) {
        int[] pixelArray = image.array();
        int[] pixelMirroredArray = new int[width * height];
        for (int i = 0; i < height; i++) {
            System.arraycopy(pixelArray, i * width, pixelMirroredArray, (height - i - 1) * width, width);
        }
        return IntBuffer.wrap(pixelMirroredArray);
    }

    public static final float[] VERTEX_POSITION = {
            -1.0f, 1.0f,
            -1.0f, -1.0f,
            1.0f, 1.0f,
            1.0f, -1.0f
    };

    public static final float[] FRAGMENT_POSITION_VIDEO = {
            0.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,
    };

    public static final float[] FRAGMENT_POSITION_IMAGE = {
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
    };
}
