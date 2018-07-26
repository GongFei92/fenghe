package com.coolweather.android.util;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

/**
 * Created by Gong on 2016-11-24.
 */

public class MySwipeRefreshLayout extends android.support.v4.widget.SwipeRefreshLayout{

    private static final String TAG = MySwipeRefreshLayout.class.getSimpleName();
    private final int mScaledTouchSlop;
    private int mLastMotionX,mLastMotionY;
    public MySwipeRefreshLayout(Context context)
    {
        super(context);
        mScaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        Log.e(TAG, "最小触控距离"+mScaledTouchSlop);
    }

    public MySwipeRefreshLayout(Context context,AttributeSet attrs)
    {
        super(context,attrs);
        mScaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        Log.e(TAG, "最小触控距离"+mScaledTouchSlop);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        super.onInterceptTouchEvent(e);
        int y = (int) e.getY();
        int x = (int) e.getX();
        boolean resume = false;
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 发生down事件时,记录y坐标
                mLastMotionY = y;
                mLastMotionX = x;
                resume = false;
                break;
            case MotionEvent.ACTION_MOVE:
                // deltaY > 0 是向下运动,< 0是向上运动
                int deltaY = y - mLastMotionY;
                int deleaX = x - mLastMotionX;

                if (Math.abs(deleaX) > Math.abs(deltaY)) {
                    resume = false;
                } else {
                    //当前正处于滑动
                    if (deltaY>0&&deltaY>mScaledTouchSlop*20) {
                        resume = true;
                    }
                    else resume = false;
                }
                break;
            case MotionEvent.ACTION_UP:
                resume = false;
                break;
            default:
                break;
        }

        return resume;
    }



}
