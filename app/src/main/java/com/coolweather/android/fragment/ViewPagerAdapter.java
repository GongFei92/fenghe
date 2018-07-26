package com.coolweather.android.fragment;

import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.Fragment;

import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by Gong on 2016-11-18.
 */


public class ViewPagerAdapter extends FragmentPagerAdapter {
    private  final  String TAG=this.hashCode()+"";
        private FragmentManager mfragmentManager;
        private List<OneFragment> mlist;

        private int myId=0;
        public ViewPagerAdapter(FragmentManager fm, List<OneFragment> list) {
            super(fm);
            this.mlist = list;
        }

        @Override
        public Fragment getItem(int arg0) {
            return mlist.get(arg0);//显示第几个页面
        }

        @Override
        public int getCount() {
            return mlist.size();//有几个页面
        }

  /*  @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Log.e(TAG, "instantiateItem:\n" );
        return super.instantiateItem(container, position);


    }*/

    public void setMyId(int sid){
           this.myId= sid;
        Log.e("myId", "："+myId);
        Log.e("size", "："+getCount());
    }
    public int getMyId(){
        return  myId;
    }
    @Override
    public int getItemPosition(Object object) {
        if(mlist!=null&& !mlist.isEmpty()) {

            if(!mlist.contains((OneFragment)object)) {
                Log.e(TAG, "getItemPosition: 没有找到");
                return POSITION_NONE;

            }
            else if(myId<getCount()) {
                if (mlist.get(myId) == (OneFragment) object) {
                    Log.e(TAG, "getItemPosition: 找到");
                    return POSITION_NONE;
                } else {

                    Log.e(TAG, "getItemPosition: 非更新项不变");
                    return POSITION_UNCHANGED;
                }
            }
            else{
                Log.e(TAG, "getItemPosition: 非删除项不变");
                return POSITION_UNCHANGED;}
        }
        Log.e(TAG, "getItemPosition: 超出范围");
        return POSITION_UNCHANGED;

    }




/*
    @Override
    public void destroyItem(ViewGroup container, int position, Object object){

        super.destroyItem(container, position, object);

        Log.e("adapter", "destroyItem进去没");

    }

*/



}

