package com.yio.customview.model;

/*
 *  @创建者:   Yio
 *  @创建时间:  2017/11/27 11:22
 *  @描述：    TODO
 */

import android.support.annotation.LayoutRes;

public class ViewModel {
    private String title;
    private @LayoutRes int resource;

    public String getTitle() {
        return title;
    }

    public int getResource() {
        return resource;
    }

    public ViewModel(String title, int resource) {
        this.title = title;
        this.resource = resource;
    }

}
