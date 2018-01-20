package com.yezi.openglmedia.filter;

public interface IRendererCallback {

    void onSurfaceCreated();

    void onSurfaceChanged(int width, int height);

    void onDrawFrame();
}
