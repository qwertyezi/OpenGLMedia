package com.yezi.openglmedia.utils;

import android.content.Context;
import android.opengl.GLES20;
import android.text.TextUtils;
import android.util.Log;

import com.yezi.openglmedia.utils.enums.FilterType;

import static com.yezi.openglmedia.utils.TextResourceUtils.formatFragmentSource;
import static com.yezi.openglmedia.utils.TextResourceUtils.formatVertexSource;
import static com.yezi.openglmedia.utils.TextResourceUtils.readTextFileFromResource;

public class ShaderUtils {

    private static final String TAG = ShaderUtils.class.getName();

    private ShaderUtils() {
    }

    public static void checkGLError(String op) {
        Log.e(TAG, op);
    }

    public static int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        if (0 != shader) {
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {
                Log.e(TAG, "Could not compile shader:" + shaderType);
                Log.e(TAG, "GLES20 Error:" + GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
        }
        return shader;
    }

    public static int loadShader(Context context, int shaderType, int res) {
        return loadShader(shaderType, readTextFileFromResource(context, res));
    }

    public static int createProgram(String vertexSource, String fragmentSource) {
        if (TextUtils.isEmpty(vertexSource) || TextUtils.isEmpty(fragmentSource)) {
            return 0;
        }
        int vertex = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertex == 0) return 0;
        int fragment = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (fragment == 0) return 0;
        int program = GLES20.glCreateProgram();
        if (program != 0) {
            GLES20.glAttachShader(program, vertex);
            checkGLError("Attach Vertex Shader");
            GLES20.glAttachShader(program, fragment);
            checkGLError("Attach Fragment Shader");
            GLES20.glLinkProgram(program);
            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GLES20.GL_TRUE) {
                Log.e(TAG, "Could not link program:" + GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program = 0;
            }
        }
        return program;
    }

    public static int createProgram(Context context, FilterType filterType, int vertexRes, int fragmentRes) {
        return createProgram(formatVertexSource(filterType, readTextFileFromResource(context, vertexRes)),
                formatFragmentSource(filterType, readTextFileFromResource(context, fragmentRes)));
    }

}
