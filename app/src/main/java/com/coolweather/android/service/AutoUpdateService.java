package com.coolweather.android.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.coolweather.android.R;
import com.coolweather.android.WeatherActivity;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.ShardId;
import com.coolweather.android.util.Utility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {
    private static final String TAG = "AutoUpdateService";
    private static int i=0;
    List<String> mycity=new ArrayList<>();
    List<String> weaid=new ArrayList<>();
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int idv=1;
        allUpdateWeather();
        updateBingPic();
        idv=getFraq();
        if(idv==0) idv=1;
        Log.e(TAG, "idpinlv="+idv);
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour = idv * 60 * 60 * 1000; // 这是8小时的毫秒数
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        Intent i = new Intent(this, AutoUpdateService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        if(ShardId.able)
        new Handler().postDelayed(new Runnable(){
            public void run() {
                //execute the task
                setNotificationBuilder() ;
            }
        }, 3000);
        return super.onStartCommand(intent, flags, startId);
    }

    private  int getFraq() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this);
        int id = prefs.getInt("fraquency", 0);
        return id;
    }
    /**
     * 更新天气信息。
     */

    private void updateWeather(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this);
        int id=prefs.getInt("length",0);
        if(id==0) return;
        Log.e(TAG, "updateWeather: id="+id);

        if(i>=id) i=0;
         String weatherString = prefs.getString(ShardId.key.get(i), null);
            if (weatherString != null) {
                // 有缓存时直接解析天气数据
                Weather weather = Utility.handleWeatherResponse(weatherString);
                String weatherId = weather.basic.weatherId;
                String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=bc0418b57b2d4918819d3974ac1285d9";
                HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String responseText = response.body().string();
                        Weather weather = Utility.handleWeatherResponse(responseText);
                        if (weather != null && "ok".equals(weather.status)) {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();

                            editor.putString(ShardId.key.get(i), responseText);
                            editor.apply();
                            Log.e(TAG, "updateWeather: i="+i);
                            i++;
                            Log.e(TAG, "updateWeather: i="+i);
                        }
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();

                    }
                });
            }

    }
    private void allUpdateWeather(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this);
        int id=prefs.getInt("length",0);
        if(id==0) return;
        Log.e(TAG, "updateWeather: id="+id);

        //String citystr=prefs.getString("city", null);
        String idstr=prefs.getString("weaid", null);
        //if(citystr!=null) mycity=java.util.Arrays.asList(citystr.split(","));
        if(idstr!=null) weaid=java.util.Arrays.asList(idstr.split(","));
        Log.e(TAG,weaid.size()+"数量");

        for (int k=0;k<id;k++)
            updateWeather1(k);
        //updateWeather1(prefs,1);
    }
    private void updateWeather1(final int iq){

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this);
        String weatherString = prefs.getString(ShardId.key.get(iq), null);
        String weatherId;
        if (weatherString != null) {


            // 有缓存时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
             weatherId = weather.basic.weatherId;
        }
        else{
            if (weaid.size()>iq) weatherId=weaid.get(iq);
            else return;
        }
            String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=aa2ea1e448064d55b4d48b2e766f9930";
            HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseText = response.body().string();
                    //Log.e(TAG, "responseText="+responseText);
                    Weather weather = Utility.handleWeatherResponse(responseText);
                    if (weather != null && "ok".equals(weather.status)) {
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();

                        editor.putString(ShardId.key.get(iq), responseText);
                        editor.apply();
                        Log.e(TAG, "updateWeather: iq="+iq+"city"+weather.basic.cityName);


                    }
                    else Log.e(TAG, "updateWeathererrormy: iq="+iq);
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "updateWeathererror: iq="+iq);
                }
            });


    }

    /**
     * 更新必应每日一图
     */
    private void updateBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void setNotificationBuilder() {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this);
        String weatherString = prefs.getString(ShardId.key.get(0), null);

        if (weatherString == null) return;

        Weather weather = Utility.handleWeatherResponse(weatherString);
        String degree=weather.now.temperature + "℃";
        String temp=weather.forecastList.get(0).temperature.min+"°/"+weather.forecastList.get(0).temperature.max+"°";
        String aqiBiao=weather.aqi.city.aqi;
        String zhiStr="";
        if(ShardId.isNumeric(aqiBiao)){
            int index=-1;
            int value=Integer.valueOf(aqiBiao);
            for(int i=0;i<ShardId.aqi.length;i++){
                if(value<ShardId.aqi[i]) {index=i;break;}
            }
            if(index==-1) zhiStr=ShardId.aqistr[ShardId.aqistr.length-1];
            else zhiStr=ShardId.aqistr[index];
        }
        String kongqi=zhiStr+"  "+aqiBiao;
        String weatherInfo = weather.now.more.info;
        String icityname=weather.basic.cityName;
        String time=ShardId.getTime();
        // TODO Auto-generated method stub
        Intent intent = new Intent(this, WeatherActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//传入当前项目的包名，和你通知栏上要显示的自定义布局的ID
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notification);
        //下面都是设置通知栏布局里面控件的属性
        //remoteViews.setImageViewResource(R.id.imageView1, R.drawable.feheee);
        remoteViews.setTextViewText(R.id.textView1, degree);
        remoteViews.setTextViewText(R.id.textView2, temp);
        remoteViews.setTextViewText(R.id.textView3, kongqi);
        remoteViews.setTextViewText(R.id.textView4, weatherInfo);
        remoteViews.setTextViewText(R.id.textView5, icityname);
        remoteViews.setTextViewText(R.id.textView6, time);
        Notification.Builder builder=new Notification.Builder(getApplication());
        builder.setSmallIcon(R.mipmap.fehee)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.fehee))
                .setTicker("您有新的天气信息")
                //.setWhen(System.currentTimeMillis())
                //.setContentTitle("标题")
                //.setContentText("内容")
                //.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDefaults(Notification.DEFAULT_SOUND|Notification.DEFAULT_VIBRATE)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setVibrate(new long[]{1000,1000,1000,1000})
                .setLights(Color.RED, 0, 1)
                .setContentIntent(pi)
                .setAutoCancel(true);
        Notification notification=builder.build();
        notification.contentView=remoteViews;
        NotificationManager manager=(NotificationManager) getSystemService(getApplicationContext().NOTIFICATION_SERVICE);
        manager.notify(1, notification);
    }


}
