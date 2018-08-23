package com.coolweather.android.fragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Gong on 2018-08-12.
 */

public class MyViewPagerAdapter extends FragmentPagerAdapter {
    private  final  String TAG=this.hashCode()+"";
    private FragmentManager fm;
    private List<Fragment> mlist=new ArrayList<>();//fragment列表
    private int action=0; //不需要增加或删除fragment
    private int updateId=0;//需要更新的第几个fragment
    private boolean flag=false;//false表示只更新一个fragment，true表示全部更新
    private List<Integer> itemID=new ArrayList<>();//用来返回getItemId的值

    public MyViewPagerAdapter(FragmentManager fm) {
        super(fm);
        this.fm=fm;

    }

    public void addFragment(Fragment fg){
        mlist.add(fg);
        action=1;//末尾增加一个Fragment
        itemID.add(mlist.size()-1);

    }

    public void addFragment(Fragment fg ,int position){
        mlist.add(position,fg);
        action=2;//非末尾插入一个Fragment
        itemID.add(position,mlist.size()-1);

    }

    public void removeFragment(){
        mlist.remove(mlist.get(mlist.size()-1));
        action=3;//末尾删除一个Fragment
        itemID.remove(itemID.size()-1);
        notifyDataSetChanged();
    }

    public void removeFragment(int position){
        mlist.remove(mlist.get(position));
        action=4;//非末尾删除一个Fragment
        itemID.remove(position);
        notifyDataSetChanged();
    }

    public void updateFragment(int position,String str,boolean flag){
        updateId=position;
        this.flag=flag;
        Fragment fragment =getItem(position);
        if(fragment instanceof UpdateAble){//这里唯一的要求是Fragment要实现UpdateAble接口
            ((UpdateAble)fragment).update(str);
            notifyDataSetChanged();
        }
    }

    public interface UpdateAble {
        public void update(String str);//更新数据
    }

    @Override
    public Fragment getItem(int arg0) {
        return mlist.get(arg0);//显示第几个页面

    }

    @Override
    public long getItemId(int position) {

        return itemID.get(position);

    }
    @Override
    public int getCount() {
        return mlist.size();//有几个页面
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Log.e(TAG, "instantiateItem:" );
        //int viewPagerId=container.getId();
        //makeFragmentName(viewPagerId,getItemId(position));
        return super.instantiateItem(container, position);
    }

    @Override
    public int getItemPosition(Object object) {
        if(mlist!=null&& !mlist.isEmpty()) {

            if(flag) return POSITION_NONE;
            else {
                if(!mlist.contains((Fragment)object)) {
                    Log.e(TAG, "getItemPosition: 没有找到");
                    return POSITION_NONE;

                }
                else {

                    Fragment fragment = getItem(updateId);
                    if(fragment==null) return POSITION_NONE;
                    if (fragment== (Fragment) object) {
                        Log.e(TAG, "getItemPosition: 找到");
                        return POSITION_NONE;
                    } else {

                        Log.e(TAG, "getItemPosition: 非更新项不变");
                        return POSITION_UNCHANGED;
                    }
                }

            }

        }
        Log.e(TAG, "getItemPosition: 超出范围");
        return POSITION_UNCHANGED;

    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object){

        super.destroyItem(container, position, object);

        Log.e(TAG, "destroyItem");
        if (action >2) {
            FragmentTransaction transaction = fm.beginTransaction();

            transaction.remove((Fragment)object);
            transaction.commit();
            action=0;
        }


    }


    private static String makeFragmentName(int viewId, long id) {
        Log.e("viewId=", ""+viewId);
    return "android:switcher:" + viewId + ":" + id;
    }

}
