package com.yezi.openglmedia.filter;


import com.yezi.openglmedia.utils.enums.FilterType;

public class NoFilter extends BaseFilter {

    public NoFilter() {
        this(FilterType.IMAGE);
    }

    public NoFilter(FilterType filterType) {
        super(0, 0);
        setFilterType(filterType);
    }

    @Override
    public void onDraw() {

    }

    @Override
    public void onCreated(int mProgram) {

    }

    @Override
    public void onChanged(int width, int height) {

    }
}
