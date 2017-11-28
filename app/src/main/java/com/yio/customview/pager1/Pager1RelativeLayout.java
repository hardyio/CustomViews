package com.yio.customview.pager1;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yio.customview.R;

/*
 *  @创建者:   Yio
 *  @创建时间:  2017/11/27 13:51
 *  @描述：    第1页Fragment的根视图
 */
public class Pager1RelativeLayout extends RelativeLayout implements CursorWheelLayout.OnMenuSelectedListener {

    public Pager1RelativeLayout(Context context) {
        super(context);
    }

    public Pager1RelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        CursorWheelLayout wheelLayout = (CursorWheelLayout) findViewById(R.id.wheel_layout);
        String[] menuItemDatas = new String[]{
                "K\nT\nV",
                "温\n暖",
                "磁\n性",
                "空\n灵",
                "悠\n远",
                "3D\n迷\n幻",
                "老\n唱\n片",
                "请\n期\n待",
                "录\n音\n棚",
        };

        SimpleTextAdapter simpleTextAdapter = new SimpleTextAdapter(getContext(), menuItemDatas);
        wheelLayout.setAdapter(simpleTextAdapter);
        wheelLayout.setOnMenuSelectedListener(this);
        wheelLayout.setSelection(0);
    }

    @Override
    public void onItemSelected(CursorWheelLayout parent, View view, View lastSelectedView, int pos) {
        ////当前被选中的TextView变色
        ((TextView) view).setTextColor(Color.WHITE);
        //上次被选中的TextView颜色值还原
        if (lastSelectedView != null) {
            ((TextView) lastSelectedView).setTextColor(getResources().getColor(R.color.textColor));
        }
    }
}
