package com.yezi.openglmedia.utils;

import android.os.Environment;

import java.io.File;

public class FileUtils {

    public static String getFilePath() {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "OpenGLMedia");
        if (!file.exists()) {
            file.mkdirs();
        }
        return file.getAbsolutePath();
    }
}
