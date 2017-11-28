package com.yio.customview.pager2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/*
 *  @创建者:   Yio
 *  @创建时间:  2017/11/27 10:03
 *  @描述：    TODO
 */

public class CircleView extends View {


    @IntDef({PROGRESS_EFFECT, ROTATO_EFFECT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Effect {
    }

    public static final int PROGRESS_EFFECT = 0;
    public static final int ROTATO_EFFECT = 1;

    public static final int LINE_WIDTH = 3;
    private Paint paint;
    private float raduis = 300;

    float[] startHsv = new float[3];
    float[] endHsv = new float[3];
    float[] outHsv = new float[3];
    private int startValue;
    private int endValue;
    float fraction;
    private float sweepAngle = 0;
    private float shortLineLength = 10;
    private float longLineLength = 20;
    private Rect bounds1;
    private Rect bounds2;
    private float angleRange;
    private int effect = ROTATO_EFFECT;

    public CircleView(Context context) {
        this(context, null);
    }

    public CircleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeWidth(LINE_WIDTH);
        bounds1 = new Rect();
        bounds2 = new Rect();

        startValue = 0xffff0000;
        endValue = 0xff00ff00;
        // 把 ARGB 转换成 HSV
        Color.colorToHSV(startValue, startHsv);
        Color.colorToHSV(endValue, endHsv);
        // 计算当前动画完成度（fraction）所对应的颜色值
        if (endHsv[0] - startHsv[0] > 180) {
            endHsv[0] -= 360;
        } else if (endHsv[0] - startHsv[0] < -180) {
            endHsv[0] += 360;
        }
    }

    @SuppressWarnings("unused")
    public float getSweepAngle() {
        return sweepAngle;
    }

    @SuppressWarnings("unused")
    public void setSweepAngle(int progress) {
        this.sweepAngle = progress * 3.6f;
        invalidate();
    }

    @SuppressWarnings("unused")
    public float getAngleRange() {
        return angleRange;
    }

    @SuppressWarnings("unused")
    public void setAngleRange(int angleRange) {
        this.angleRange = angleRange * 3.6f;
        invalidate();
    }

    public void setEffect(@Effect int effect) {
        this.effect = effect;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.translate(getWidth() / 2, getHeight() / 2);
        if (effect == PROGRESS_EFFECT) {
            scoreDraw(canvas);
        } else {
            scanDraw(canvas);
        }
    }

    /**
     * 扫描绘制
     *
     * @param canvas
     */
    private void scanDraw(Canvas canvas) {
        canvas.save();
        for (float angle = 0; angle <= 360; angle += 3.6f) {
            boolean colorRange;
            if (angleRange >= 135 && angleRange < 180) {
                colorRange = (angle >= 0 && angle <= angleRange - 135) || (angle >= 180 + angleRange && angle < 360) || (angle >= angleRange && angle <= 45 + angleRange);
            } else if (angleRange >= 180 && angleRange < 315) {
                colorRange = (angle > angleRange && angle < 45 + angleRange) || (angle >= angleRange - 180 && angle <= angleRange - 135);
            } else if (angleRange >= 315 && angleRange < 360) {
                colorRange = (angle >= 0 && angle <= angleRange - 315) || (angle < 360 && angle >= angleRange) || (angle >= angleRange - 180 && angle <= angleRange - 135);
            } else {
                colorRange = (angle >= angleRange + 180 && angle <= angleRange + 225) || (angle >= angleRange && angle <= 45 + angleRange);
            }
            if (!colorRange) {
                //灰色短线
                paint.setColor(Color.GRAY);
                canvas.drawLine(0, -raduis, 0, -raduis + shortLineLength, paint);
            } else {
                int color = getARGBColor(angle);
                //彩色长线
                paint.setColor(color);
                canvas.drawLine(0, -raduis, 0, -raduis + longLineLength, paint);
            }
            canvas.rotate(3.6f);
        }
        canvas.restore();
    }

    /**
     * 打分绘制
     *
     * @param canvas
     */
    private void scoreDraw(Canvas canvas) {
        canvas.save();
        for (float angle = 0; angle <= 360; angle += 3.6f) {
            if (angle > sweepAngle) {
                //灰色短线
                paint.setColor(Color.GRAY);
                canvas.drawLine(0, -raduis, 0, -raduis + shortLineLength, paint);
            } else {
                int color = getARGBColor(angle);
                //彩色长线
                paint.setColor(color);
                canvas.drawLine(0, -raduis, 0, -raduis + longLineLength, paint);
            }
            canvas.rotate(3.6f);
        }
        canvas.restore();
        //画进度数字
        paint.setColor(0xff0000f0);
        String text = Math.round(sweepAngle / 360.0f * 100) + "";
        paint.setTextSize(60);
        paint.getTextBounds(text, 0, text.length(), bounds1);
        int xOffset = (bounds1.right - bounds1.left) / 2;
        int yOffset = -(bounds1.top + bounds1.bottom) / 2;
        canvas.drawText(text, -xOffset, yOffset, paint);
        //画"分"字
        String text1 = "分";
        paint.setTextSize(30);
        paint.getTextBounds(text1, 0, text1.length(), bounds2);
        float x = paint.measureText(text) + 5;
        yOffset = bounds1.bottom - bounds2.bottom;
        canvas.drawText(text1, x, -yOffset, paint);
    }

    private int getARGBColor(float angle) {
        if (angle <= 180) {
            fraction = angle / 180.0f;
        } else {
            fraction = (360 - angle) / 180.0f;
        }
        outHsv[0] = startHsv[0] + (endHsv[0] - startHsv[0]) * fraction;
        if (outHsv[0] > 360) {
            outHsv[0] -= 360;
        } else if (outHsv[0] < 0) {
            outHsv[0] += 360;
        }
        outHsv[1] = startHsv[1] + (endHsv[1] - startHsv[1]) * fraction;
        outHsv[2] = startHsv[2] + (endHsv[2] - startHsv[2]) * fraction;

        // 计算当前动画完成度（fraction）所对应的透明度
        int alpha = startValue >> 24 + (int) ((endValue >> 24 - startValue >> 24) * fraction);

        // 把 HSV 转换回 ARGB 返回
        return Color.HSVToColor(alpha, outHsv);
    }
}
