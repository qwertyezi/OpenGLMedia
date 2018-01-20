package com.yezi.openglmedia.utils.enums;

public enum ScaleType {
    CENTER_INSIDE("Center_Inside"),//将图片的内容完整居中显示
    CENTER_CROP("Center_Crop"),//按比例扩大图片全屏居中显示
    FIT_XY("Fit_XY");//把图片塞满整个View

    private String name;

    ScaleType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
