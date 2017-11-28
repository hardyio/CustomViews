package com.yio.customview.pager2;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.yio.customview.R;


/*
 *  @创建者:   Yio
 *  @创建时间:  2017/11/27 13:51
 *  @描述：    第2页Fragment的根视图
 */

public class Pager2RelativeLayout extends RelativeLayout implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private CircleView circleView;
    private Button btn1;
    private Button btn2;
    private TextView tvScore;
    private SeekBar seekBar;
    private int progress;
    private ObjectAnimator objectAnimator2;

    public Pager2RelativeLayout(Context context) {
        super(context);
    }

    public Pager2RelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        circleView = (CircleView) findViewById(R.id.circle_view);
        btn1 = (Button) findViewById(R.id.btn_1);
        btn2 = (Button) findViewById(R.id.btn_2);
        seekBar = (SeekBar) findViewById(R.id.seekbar);
        progress = seekBar.getProgress();
        tvScore = (TextView) findViewById(R.id.tv_score);
        tvScore.setText(getResources().getString(R.string.score, progress));
        seekBar.setOnSeekBarChangeListener(this);
        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_1:
                circleView.setEffect(CircleView.ROTATO_EFFECT);
                objectAnimator2 = ObjectAnimator.ofInt(circleView, "angleRange", 100);
                //使ObjectAnimator动画匀速平滑旋转
                objectAnimator2.setInterpolator(new LinearInterpolator());
                objectAnimator2.setRepeatCount(ValueAnimator.INFINITE);
                objectAnimator2.setDuration(1000);
                objectAnimator2.start();
                break;
            case R.id.btn_2:
                if (objectAnimator2 != null) {
                    objectAnimator2.end();
                    objectAnimator2 = null;
                }
                circleView.setEffect(CircleView.PROGRESS_EFFECT);
                ObjectAnimator objectAnimator = ObjectAnimator.ofInt(circleView, "sweepAngle", progress);
                objectAnimator.setDuration(600);
                objectAnimator.start();
                break;
            default:
                break;
        }

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        this.progress = progress;
        tvScore.setText(getResources().getString(R.string.score, progress));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
