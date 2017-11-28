package com.yio.customview;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;


/*
 *  @创建者:   Yio
 *  @创建时间:  2017/11/27 11:26
 *  @描述：    pager的fragment容器
 */

public class PagerFragment extends Fragment {

    private int resource;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        resource = arguments.getInt("resource");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_pager, container, false);
        ViewStub viewStub = (ViewStub) inflate.findViewById(R.id.view_stub);
        viewStub.setLayoutResource(resource);
        viewStub.inflate();
        return inflate;
    }

}
