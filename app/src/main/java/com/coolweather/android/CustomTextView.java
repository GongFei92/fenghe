package com.coolweather.android;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Gong on 2016-012-10.
 */

public class CustomTextView extends View {
    private float mTextSize;//字体大小
    private int mTextColor;//字体颜色
    private int mBackgroudColor;//背景颜色

    private String mText;

    private Rect rect;
    private Paint paint;

    public CustomTextView(Context context) {
        super(context);
    }

    public CustomTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray array = null;
        try {
            array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CustomTextView, 0, 0);
            mTextSize = array.getDimension(R.styleable.CustomTextView_mTextSize, 14);
            mTextColor = array.getColor(R.styleable.CustomTextView_mTextColor, Color.WHITE);
            mBackgroudColor = array.getColor(R.styleable.CustomTextView_mBackgroudColor, Color.BLACK);

            mText = array.getString(R.styleable.CustomTextView_mText);
        } finally {
            array.recycle();
        }
        init();

    }

    public  void setmText(String str){
        mText=str;
        invalidate();
    }
    private void init() {
        paint = new Paint();
        paint.setColor(mTextColor);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setTextSize(mTextSize);
        paint.setAntiAlias(true);//抗锯齿
        paint.setDither(true);//防抖动
        rect = new Rect();
        paint.getTextBounds(mText, 0, mText.length(), rect);
        //setPadding(8, 8, 8, 8);

    }

    public CustomTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int width = 250;
        int height = 100;
        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else {
            width = Math.min(width, widthSize);
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            height = Math.min(height, heightSize);
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int towidth=getWidth();
        int toheight=getHeight();
        RectF banoval = new RectF(0, 0, towidth*2/5, towidth*2/5);// 设置个新的长方形，扫描测量
        paint.setColor(mBackgroudColor);

        canvas.drawArc(banoval,90,180,true,paint);
        banoval.set(towidth*1/5,0,towidth*4/5,towidth*2/5);
        canvas.drawRect(banoval, paint);// 长方形
        banoval.set(towidth*3/5,0,towidth,towidth*2/5);
        canvas.drawArc(banoval,270,180,true,paint);
        //canvas.drawCircle(getWidth() / 2, getHeight() / 2, getWidth() / 2, paint);
        paint.setColor(mTextColor);
        canvas.drawText(mText, getWidth() / 2 - rect.width() / 2-2, towidth*1/5 + rect.height() / 2-3, paint);

    }
}
