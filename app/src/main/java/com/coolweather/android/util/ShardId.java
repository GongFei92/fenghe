package com.coolweather.android.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.Date;
import java.text.SimpleDateFormat;
/**
 * Created by Gong on 2016-11-22.
 */

public class ShardId {
    public static List<String> key=new ArrayList<>();
    public static List<String> describe=new ArrayList<>();
    public static List<String> imagName=new ArrayList<>();
    public static int[] aqi={51,101,151,201,301};
    public static String[] aqistr={"优","良","轻度污染","中度污染","重度污染","严重污染"};
    public static boolean able;
    static {
       key.add("weather"+key.size());
       key.add("weather"+key.size());
       key.add("weather"+key.size());
        key.add("weather"+key.size());
        key.add("weather"+key.size());
        key.add("weather"+key.size());
        key.add("weather"+key.size());
        key.add("weather"+key.size());
        key.add("weather"+key.size());
        key.add("weather"+key.size());

        describe.add("晴间多云");
        describe.add("晴");
        describe.add("多云");
        describe.add("阴");
        describe.add("少云");
        describe.add("阵雨");
        describe.add("大雨");
        describe.add("中雨");
        describe.add("小雨");
        describe.add("风");
        describe.add("雨");
        describe.add("雪");
        describe.add("沙");
        describe.add("雾");
        describe.add("霾");
        describe.add("热");
        describe.add("冷");

        imagName.add("qingyun");
        imagName.add("qing");
        imagName.add("duoyun");
        imagName.add("yin");
        imagName.add("shaoyun");
        imagName.add("zhenyu");
        imagName.add("dayu");
        imagName.add("zhongyu");
        imagName.add("xiaoyu");
        imagName.add("feng");
        imagName.add("yu");
        imagName.add("xue");
        imagName.add("sha");
        imagName.add("wu");
        imagName.add("mai");
        imagName.add("re");
        imagName.add("neng");
    }

    public static boolean isNumeric(String str){
        if(str==null||str.equals("")) return false;
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }
    public static String getTime(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");// HH:mm
        //获取当前时间
        Date date = new Date(System.currentTimeMillis());
        return simpleDateFormat.format(date);
    }

}
