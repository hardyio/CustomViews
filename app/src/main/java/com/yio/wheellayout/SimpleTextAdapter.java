package com.yio.wheellayout;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;


/**
 * Created by yio on 16/4/24.
 */
public class SimpleTextAdapter
        implements CursorWheelLayout.CycleWheelAdapter {
    private final Context mContext;
    private String[] mMenuItemDatas;

    public SimpleTextAdapter(Context context, String[] menuItemDatas) {
        mContext = context;
        mMenuItemDatas = menuItemDatas;
    }

    @Override
    public int getCount() {
        return mMenuItemDatas == null ? 0 : mMenuItemDatas.length;
    }

    @Override
    public View getView(View parent, int position) {
        String text = getItem(position);
        TextView textView = new TextView(mContext);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        textView.setTextColor(mContext.getResources().getColor(R.color.textColor));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,18);
        textView.setText(text);
        return textView;
    }

    @Override
    public String getItem(int position) {
        return mMenuItemDatas[position];
    }

}
