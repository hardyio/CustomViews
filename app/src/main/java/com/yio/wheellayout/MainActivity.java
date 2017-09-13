package com.yio.wheellayout;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements CursorWheelLayout.OnMenuSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

        SimpleTextAdapter simpleTextAdapter = new SimpleTextAdapter(this, menuItemDatas);
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
