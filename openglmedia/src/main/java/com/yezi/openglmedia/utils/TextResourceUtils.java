package com.yezi.openglmedia.utils;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;

import com.yezi.openglmedia.utils.enums.FilterType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class TextResourceUtils {

    public static String readTextFileFromResource(Context context, int resourceId) {
        StringBuilder body = new StringBuilder();

        if (context == null) {
            return null;
        }
        try {
            InputStream inputStream = context.getResources().openRawResource(resourceId);
            InputStreamReader inputStreamReader = new InputStreamReader(
                    inputStream);
            BufferedReader bufferedReader = new BufferedReader(
                    inputStreamReader);

            String nextLine;

            while ((nextLine = bufferedReader.readLine()) != null) {
                body.append(nextLine);
                body.append('\n');
            }
        } catch (IOException e) {
            throw new RuntimeException(
                    "Could not open resource: " + resourceId, e);
        } catch (Resources.NotFoundException nfe) {
            throw new RuntimeException("Resource not found: "
                    + resourceId, nfe);
        }

        return body.toString();
    }

    public static String formatVertexSource(FilterType filterType, String vertex) {
        if (TextUtils.isEmpty(vertex)) {
            return null;
        }
        return vertex.replace("%s", filterType == FilterType.IMAGE ? "1.0" : "uSTMatrix");
    }

    public static String formatFragmentSource(FilterType filterType, String fragment) {
        if (TextUtils.isEmpty(fragment)) {
            return null;
        }
        return fragment.replace("%1s", filterType == FilterType.IMAGE ?
                " " : "#extension GL_OES_EGL_image_external : require").
                replace("%2s", filterType == FilterType.IMAGE ?
                        "sampler2D" : "samplerExternalOES");
    }
}
