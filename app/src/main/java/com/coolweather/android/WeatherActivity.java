package com.coolweather.android;

import android.app.Activity;
import android.app.AlertDialog;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.ViewDragHelper;

import android.support.v7.app.AppCompatActivity;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.coolweather.android.fragment.OneFragment;
import com.coolweather.android.fragment.ViewPagerAdapter;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.service.AutoUpdateService;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.ShardId;
import com.coolweather.android.util.Utility;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import android.view.View.OnClickListener;
public class WeatherActivity extends AppCompatActivity implements OnClickListener{
    private static final String TAG = WeatherActivity.class.getSimpleName();
    private ViewPager myViewPager;
    private List<OneFragment> list;
    private List<OneFragment> myList;
    private ViewPagerAdapter adapter;
    private TextView usename,mail;
    public DrawerLayout drawerLayout;   //侧滑栏

    public SwipeRefreshLayout swipeRefresh;//下拉或上拉刷新

    private Button navButton;
    private Button popButton;

    private ImageView bingPicImg;


    private List<String> mWeatherIdlist;
    public List<String> cityName;
    public boolean myFlag=false;
    private ChooseAreaFragment myChooseAreaFragment;
    private Button mylocButton;
    public volatile int currtitem=0;
    public int[] switem={0,0,1};
    private boolean isNull=false;

    private static boolean isExit=false;

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            isExit=false;
        }
    };

    @Override
    public boolean onKeyDown(int keyCode,KeyEvent event){
        if(keyCode==KeyEvent.KEYCODE_BACK){
            exit();
            return false;
        }
        return super.onKeyDown(keyCode,event);
    }

    private void exit() {
        if (!isExit) {
            isExit = true;
            Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
            //利用handler延迟发送更改状态信息
            handler.sendEmptyMessageDelayed(0, 2000);
        } else {
            finish();
            //System.exit(0);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        }
    }
    private void initMyList(){
        myList = new ArrayList<>();
        myList.add(new OneFragment());
        myList.add(new OneFragment());
        myList.add(new OneFragment());
        myList.add(new OneFragment());
        myList.add(new OneFragment());
        myList.add(new OneFragment());
        myList.add(new OneFragment());
        myList.add(new OneFragment());
        myList.add(new OneFragment());
        myList.add(new OneFragment());
        mWeatherIdlist=new ArrayList<>();
        cityName=new ArrayList<>();

        Log.e(TAG, "shardId:"+ShardId.key);

        mWeatherIdlist.add("CN101250101");
        cityName.add("长沙");
//
    }
    private void addMyList(){

        myList.add(new OneFragment());
        ShardId.key.add("weather"+ShardId.key.size());

    }
    private void removeMyList(){

        myList.remove(myList.size()-1);
        ShardId.key.remove(ShardId.key.size()-1);

    }
            @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility( View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);  //状态栏设置为透明的
        }
        setContentView(R.layout.activity_weather);
        // 初始化各控件
        bingPicImg = (ImageView) findViewById(R.id.bing_pic_img);

        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
                swipeRefresh.setProgressViewEndTarget (true,500);
         //swipeRefresh.setProgressViewOffset (false,0,500);
         //swipeRefresh.setDistanceToTriggerSync(24);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        myChooseAreaFragment=(ChooseAreaFragment)getSupportFragmentManager().findFragmentById(R.id.choose_area_fragment);
        juLi(2);
        NavigationView navView=(NavigationView)findViewById(R.id.nav_view);
        View headerView = navView.getHeaderView(0);
        mail = (TextView)headerView.findViewById(R.id.mail);
        usename = (TextView)headerView.findViewById(R.id.usename);

        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id=item.getItemId();

                switch (id){
                    case R.id.deng:

                        login();
                        break;
                    case R.id.tui:
                        finish();
                        //setNotificationBuilder();
                        break;
                    case R.id.cgeng:
                        showListDialog();
                        break;
                    case R.id.jian:
                        jianDialog();
                        break;
                    case R.id.geng:
                        fraqListDialog();
                        break;
                    case R.id.guan:
                        yiDialog();
                        break;
                    case R.id.yu:
                        gyDialog();
                        break;
                    case R.id.xuan:
                        drawerLayout.closeDrawers();
                        if(list.size()>=myList.size()){
                            showToast("普通用户最多收藏"+myList.size()+"个城市哦");
                            return true;
                        }
                        mylocButton=(Button) myChooseAreaFragment.getView().findViewById(R.id.loc_button);
                        mylocButton.setVisibility(View.GONE);
                        juLi(0);
                        myFlag=true;
                        drawerLayout.openDrawer(GravityCompat.START);

                        break;
                }


                return true;
            }
        });

        //setDrawerLeftEdgeSize(this,drawerLayout,0.3f);

        navButton = (Button) findViewById(R.id.nav_button);
        popButton= (Button) findViewById(R.id.pop_button);
        initMyList();

        myViewPager = (ViewPager) findViewById(R.id.myViewPager);
        list = new ArrayList<>();
        list.add(myList.get(0));
        adapter = new ViewPagerAdapter(getSupportFragmentManager(), list);
        myViewPager.setAdapter(adapter);
        myViewPager.setCurrentItem(0);  //初始化显示第一个页面
        myViewPager.setOnPageChangeListener(new MyPagerChangeListener());
        ShardId.able=false;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
        String weatherString = prefs.getString(ShardId.key.get(0), null);
        int id=prefs.getInt("length",0);
        int  fraquency=prefs.getInt("fraquency", 0);
          myChoice=freqFind(fraquency);
        //id=1;

        if (weatherString != null) {
            // 有缓存时直接解析天气数据


            Log.e(TAG, "full");
            Weather weather = Utility.handleWeatherResponse(weatherString);
            upData(0,weather);
            /*Bundle bundle = new Bundle();
            bundle.putString("weather", weatherString);*/
            //adapter.addString(weatherString);
         /*   mFragment.setString(weatherString);
            adapter.notifyDataSetChanged();
            mFragment1.setString("beijing");
            adapter.setMyId(1);
            adapter.notifyDataSetChanged();
            mFragment2=   new OneFragment();
            mFragment2.setString("wuyan");
            list.add(mFragment2);
            adapter.setMyId(2);
            adapter.notifyDataSetChanged();*/
            /*mFragment.setArguments(bundle);
            mFragment1.setArguments(bundle);*/
            //showWeatherInfo(weather);

        } else {
            Log.e(TAG, "null");
            swipeRefresh.setRefreshing(true);
            // 无缓存时去服务器查询天气
            String chuanId=getIntent().getStringExtra("weather_id");
            String city_name=getIntent().getStringExtra("city_name");
            if(chuanId!=null){
            mWeatherIdlist.set(0,chuanId) ;
            cityName.set(0,city_name);
            //weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(mWeatherIdlist.get(0),0);}
            else {
                isNull=true;
            }
        }
        String bingPic = prefs.getString("bing_pic", null);
        if (bingPic != null) {
           Glide.with(this).load(bingPic).into(bingPicImg);
        } else {
            loadBingPic();
         }


      if(id>1||isNull) {
       String myweatherString;
          Weather  weather;
          List<String> mycity=new ArrayList<>();
          List<String> weaid=new ArrayList<>();
          String citystr=prefs.getString("city", null);
          String idstr=prefs.getString("weaid", null);
          if(citystr!=null) mycity=java.util.Arrays.asList(citystr.split(","));
          if(idstr!=null) weaid=java.util.Arrays.asList(idstr.split(","));
          Log.e(TAG,mycity.size()+"空值数量");
          Log.e(TAG,id+"数量");
          System.out.println(mycity);
          System.out.println(weaid);
          if(isNull){
              if(weaid.size()>0&&mycity.size()>0) {
                  mWeatherIdlist.set(0, weaid.get(0));
                  cityName.set(0, mycity.get(0));

              }
              requestWeather(mWeatherIdlist.get(0), 0);
              isNull=false;
          }


       for(int i=1;i<id;i++){
          myweatherString = prefs.getString(ShardId.key.get(i), null);

          if(myweatherString!=null) {
              weather = Utility.handleWeatherResponse(myweatherString);
              /* try{Thread.sleep(500);}
               catch (Exception e){}*/
                  if (weather != null&&"ok".equals(weather.status)) {
                      allAddFragment(weather);
                      Log.e(TAG, "weathercity" + weather.basic.cityName);
                  }
                  else{
                      Log.e(TAG,"waicejinlai");
                      if (mycity.size()>i&&(weaid.size()>i)) {
                          teAddFragment(mycity.get(i), weaid.get(i));
                          Log.e(TAG,"内cejinlai");
                      }
                  }
          }
          else{
              if (mycity.size()>i&&(weaid.size()>i)) {
                  teAddFragment(mycity.get(i), weaid.get(i));
                  Log.e(TAG,"xiacejinlai");
              }
          }
        }

          SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
          editor.putInt("length", list.size());
          editor.apply();
      }
      else {

          SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
          editor.putInt("length", 1);
          editor.apply();
      }


       new Handler().postDelayed(new Runnable(){
                    public void run() {
                        //execute the task
                        startMySer();
                    }
                }, 2000);




        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                switem[0]=currtitem;
                switem[1]=1;
                switem[2]=1;
                if(switem[0]<list.size()){
                    requestWeather(mWeatherIdlist.get(switem[0]),switem[0]);
                }
                else swipeRefresh.setRefreshing(false);
                //requestWeather(mWeatherId);

                /*list.remove(list.size()-1);
                adapter.setMyId(list.size());
                adapter.notifyDataSetChanged();*/
                //mFragment2=   new OneFragment();
                /*list.add(1,mFragment2);
                adapter.setMyId(1);
                adapter.notifyDataSetChanged();*/
                //upData(0,"xizang");

            }
        });
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("侧滑栏");
                drawerLayout.openDrawer(GravityCompat.START);  //从左向右显示侧滑栏
               /* adapter.setMyId(0);
                mFragment.setString("xinjiang");
                adapter.notifyDataSetChanged();*/
               /* mFragment1.setString("eran");
                adapter.setMyId(1);
                adapter.notifyDataSetChanged();*/

               //addFragment("hunan");
            }
        });
        popButton.setOnClickListener(new View.OnClickListener() {
                    @Override
          public void onClick(View v) {
           drawerLayout.openDrawer(GravityCompat.END);


                    }
                });

    }

    /**
     * 根据天气id请求城市天气信息。
     */
    public void requestWeather(final String weatherId,final int id) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=aa2ea1e448064d55b4d48b2e766f9930";

        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                if (weather != null && "ok".equals(weather.status)) {
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                    editor.putString(ShardId.key.get(id), responseText);
                    editor.apply();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            upData(id,weather);
                            swipeRefresh.setRefreshing(false);
                            switem[2]=0;
                        }
                    });

                    //showWeatherInfo(weather);

                } else {
                    requestWeather1(weatherId,id);
                   /* runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            upData(id,null);

                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                            swipeRefresh.setRefreshing(false);
                        }
                    });*/


                }



            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                if(idDuan(weatherId,id)==false){
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                    editor.putString(ShardId.key.get(id), null);
                    editor.apply();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            upData(id,null);
                            if(currtitem==0||switem[2]==1)
                                Toast.makeText(WeatherActivity.this, "没有获得天气信息", Toast.LENGTH_SHORT).show();
                            swipeRefresh.setRefreshing(false);
                            switem[2]=0;
                        }
                    });

                }
                else runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(currtitem==0||switem[2]==1)
                            Toast.makeText(WeatherActivity.this, "没有获得天气信息", Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                        switem[2]=0;
                    }
                });
               // requestWeather1(weatherId,id);
            }
        });
        loadBingPic();
    }

    public void requestWeather1(final String weatherId,final int id) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=aa2ea1e448064d55b4d48b2e766f9930";

        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                if (weather != null && "ok".equals(weather.status)) {
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                    editor.putString(ShardId.key.get(id), responseText);
                    editor.apply();
                    Log.e(TAG,"会变的key="+id);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            upData(id,weather);
                            swipeRefresh.setRefreshing(false);
                            switem[2]=0;
                        }
                    });

                    //showWeatherInfo(weather);

                } else {
                    if(idDuan(weatherId,id)==false){
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                        editor.putString(ShardId.key.get(id), null);
                        editor.apply();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                upData(id,null);
                                if(currtitem==0||switem[2]==1)
                                Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                                swipeRefresh.setRefreshing(false);
                                switem[2]=0;
                            }
                        });

                    }
                   else runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            switem[2]=0;
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                            swipeRefresh.setRefreshing(false);
                        }
                    });


                }



            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                if(idDuan(weatherId,id)==false){
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                    editor.putString(ShardId.key.get(id), null);
                    editor.apply();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            upData(id,null);
                            if(currtitem==0||switem[2]==1)
                            Toast.makeText(WeatherActivity.this, "没有获得天气信息", Toast.LENGTH_SHORT).show();
                            swipeRefresh.setRefreshing(false);
                            switem[2]=0;
                        }
                    });

                }
                else runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(currtitem==0||switem[2]==1)
                        Toast.makeText(WeatherActivity.this, "没有获得天气信息", Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                        switem[2]=0;
                    }
                });

            }
        });

    }

    /**
     * 加载必应每日一图
     */
    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 处理并展示Weather实体类中的数据。
     */
    /*private void showWeatherInfo(Weather weather) {
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.more.info;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        for (Forecast forecast : weather.forecastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = (TextView) view.findViewById(R.id.date_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);
        }
        if (weather.aqi != null) {
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度：" + weather.suggestion.comfort.info;
        String carWash = "洗车指数：" + weather.suggestion.carWash.info;
        String sport = "运行建议：" + weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }*/
    public void startMySer(){

        Intent intent = new Intent(WeatherActivity.this, AutoUpdateService.class);
        startService(intent);
    }

    private void upData(int id,Weather myweather){
        if(id<list.size()){
            if(myweather==null) {
                upDataCity(id);
            }
            else {
                myList.get(id).setWeather(myweather);
                adapter.setMyId(id);
                adapter.notifyDataSetChanged();
                mWeatherIdlist.set(id, myweather.basic.weatherId);
                cityName.set(id, myweather.basic.cityName);
            }
        }
        else Log.e(TAG, "upData: error"+id);
    }
    private void upDataCity(int id){
        if(id<list.size()){
            String city=cityName.get(id);
            myList.get(id).setCity(city);
            adapter.setMyId(id);
            adapter.notifyDataSetChanged();

        }
        else Log.e(TAG, "upData: error"+id);
    }
    public void addFragment(String weatherId,Weather myweather){
        int size=list.size();
        if(size>=myList.size()){
            Log.e(TAG, "addFragment: error"+list.size());
        return;
        }
        if(myweather==null){

            list.add(myList.get(size));
            mWeatherIdlist.add(weatherId);
            //myList.get(cityName.size()-1).setCity(city);
            adapter.setMyId(list.size()-1);
            adapter.notifyDataSetChanged();}

        else{
            myList.get(size).setWeather(myweather);
            list.add(myList.get(size));
            mWeatherIdlist.add(myweather.basic.weatherId);
            cityName.add(myweather.basic.cityName);
            adapter.setMyId(list.size()-1);
            adapter.notifyDataSetChanged();
        }
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
        editor.putInt("length", list.size());
        editor.apply();
        showToast(cityName.get(cityName.size()-1)+"收藏成功，向右滑动可查看");
    }
    private void allAddFragment(Weather myweather){
        int size=list.size();
        if(size>=myList.size()){
            Log.e(TAG, "allAddFragment: error"+list.size());
            return;
        }
        myList.get(size).setWeather(myweather);
        list.add(myList.get(size));
        mWeatherIdlist.add(myweather.basic.weatherId);
        Log.e(TAG,mWeatherIdlist+"");
        cityName.add(myweather.basic.cityName);
        Log.e(TAG,cityName+"");
        adapter.setMyId(list.size()-1);
        adapter.notifyDataSetChanged();
    }

    private void teAddFragment(String city,String weaid){
        int size=list.size();
        if(size>=myList.size()){
            Log.e(TAG, "allAddFragment: error"+list.size());
            return;
        }
        myList.get(size).setCity(city);
        list.add(myList.get(size));
        mWeatherIdlist.add(weaid);
        Log.e(TAG,mWeatherIdlist+"");
        cityName.add(city);
        Log.e(TAG,cityName+"");
        adapter.setMyId(list.size()-1);
        requestWeather(weaid,list.size()-1);
        adapter.notifyDataSetChanged();

    }
    private void removeFragment(){
        if(list.size()<2) return;
        list.remove(list.size()-1);
        adapter.setMyId(list.size());
        adapter.notifyDataSetChanged();

    }

    //利用反射修改drawerLayout滑动边距
    public  void setDrawerLeftEdgeSize(Activity activity, DrawerLayout drawerLayout, float displayWidthPercentage) {
        if (activity == null || drawerLayout == null) return;
        try {
            Field leftDraggerField = drawerLayout.getClass().getDeclaredField("mLeftDragger");
            leftDraggerField.setAccessible(true);
            ViewDragHelper leftDragger = (ViewDragHelper) leftDraggerField.get(drawerLayout);
            Field edgeSizeField = leftDragger.getClass().getDeclaredField("mEdgeSize");
            edgeSizeField.setAccessible(true);
            int edgeSize = edgeSizeField.getInt(leftDragger);
            DisplayMetrics dm = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
            edgeSizeField.setInt(leftDragger, Math.max(edgeSize, (int) (dm.widthPixels * displayWidthPercentage)));
        } catch (Exception e) {
        }
    }

    public  void juLi(int per){
        try
        {

            DisplayMetrics dm = this.getResources().getDisplayMetrics();
            int width = dm.widthPixels;
            Log.e("mheight", "mheight=" +dm.heightPixels*160/dm.densityDpi+"dpi="+dm.densityDpi);
            // 去看源码就可以知道，mMinDrawerMargin 默认是64dp
            // 用反射来设置划动出来的距离 mMinDrawerMargin
            Field mMinDrawerMarginField = DrawerLayout.class.getDeclaredField("mMinDrawerMargin");
            mMinDrawerMarginField.setAccessible(true);
            int minDrawerMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX,
                    width*per/5, dm);
            mMinDrawerMarginField.set(drawerLayout, minDrawerMargin);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        // 设置阴影颜色
        drawerLayout.setScrimColor(Color.parseColor("#55000000"));
// 设置边缘颜色
        drawerLayout.setDrawerShadow(new ColorDrawable(Color.parseColor("#22000000")), Gravity.RIGHT);

    }

    /**
     * 设置一个ViewPager的侦听事件，当左右滑动ViewPager时菜单栏被选中状态跟着改变
     *
     */
    public class MyPagerChangeListener implements OnPageChangeListener {

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageSelected(int arg0) {

            currtitem=arg0;
            Log.e("my", "currtitem=" +currtitem);
            if((switem[0]!=currtitem)&&(switem[1]==1)) {
                swipeRefresh.setRefreshing(false);
                switem[1]=0;
            }
        }
    }


    @Override

    public void onSaveInstanceState(Bundle outState) {

//将super调用取消即可，表明当意外(比如系统内存吃紧将应用杀死)发生我不需要保存Fragmentde状态和数据等

//super.onSaveInstanceState(outState);

    }
    public void homePageUpdate(String weatherId){
        mWeatherIdlist.set(0,weatherId);

        myViewPager.setCurrentItem(0,false);
        requestWeather(weatherId,0);
    }

    //弹出的对话框输入新的坐标
    private void login(){
        final View  login=getLayoutInflater().inflate(R.layout.denglu, null);


        new android.app.AlertDialog.Builder(WeatherActivity.this,AlertDialog.THEME_HOLO_LIGHT)
                .setView(login)
                .setTitle("请登陆/注册")
                .setCancelable(true)
                .setNegativeButton("取消",new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.cancel();


                    }

                })
                .setPositiveButton("确认",   new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {


                        EditText name = (EditText)login.findViewById(R.id.lat);
                        EditText mmail = (EditText)login.findViewById(R.id.lon);

                        String strname = name.getText().toString();
                        String strmail = mmail.getText().toString();

                        if("".equals(strname)||"".equals(strmail))
                        {   showToast("输入为空");
                            return;}

                        usename.setText(strname);
                        mail.setText(strmail);

                    }

                })
                .create().show();
    }



    private void showListDialog() {

        final String[] items = cityName.toArray(new String[cityName.size()]);
        AlertDialog.Builder listDialog =
                new AlertDialog.Builder(WeatherActivity.this,AlertDialog.THEME_HOLO_LIGHT);
        listDialog.setTitle("您所收藏的城市");
        listDialog.setItems(items, null);
        listDialog.show();
        Log.e(TAG, "chengshi "+cityName);
    }
    private List<String> cutHead(List<String> list){
        List<String> mycityName=new ArrayList<>();
        if(list.size()<2) return null;
        for(int i=1;i<list.size();i++){
            mycityName.add(list.get(i));
        }
        return mycityName;
    }

    private boolean[] defaultChoices(int size){
        boolean myChoiceSets[]=new boolean[size];
        for(int i=0;i<size;i++){
        myChoiceSets[i] = false;}
        return myChoiceSets;
    }


    ArrayList<Integer> yourChoices = new ArrayList<>();
        private void jianDialog() {
            List<String> mycityName=cutHead(cityName);
            String[] myitems;
             if(mycityName==null) myitems=null;
             else myitems=mycityName.toArray(new String[mycityName.size()]);
            final String[] items =myitems;

            // 设置默认选中的选项，全为false默认均未选中
            //final boolean initChoiceSets[]=defaultChoices(mycityName.size());
            yourChoices.clear();
            AlertDialog.Builder multiChoiceDialog =
                    new AlertDialog.Builder(WeatherActivity.this,AlertDialog.THEME_HOLO_LIGHT);
            multiChoiceDialog.setTitle("请选择要删除的城市");
            multiChoiceDialog.setMultiChoiceItems( items,null,
                    new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which,
                                            boolean isChecked) {
                            if (isChecked) {
                                yourChoices.add(which);
                            } else {
                                yourChoices.remove((Object)which);
                            }
                            Log.e(TAG, "变动选项 "+yourChoices);
                        }
                    });
            multiChoiceDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            multiChoiceDialog.setPositiveButton("确定",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            int size = yourChoices.size();
                            if(size==0) return;
                            //String str = "";
                          /*  for (int i = 0; i < size; i++) {
                                if((yourChoices.get(i)+1)<=currtitem) swipeRefresh.setRefreshing(true);
                                removeFragment();
                                cityName.remove(yourChoices.get(i)+1);
                                mWeatherIdlist.remove(yourChoices.get(i)+1);

                            }*/
                            Collections.sort(yourChoices);
                            if((yourChoices.get(0)+1)<=currtitem) {
                                swipeRefresh.setRefreshing(true);

                            }
                            for (int i = size-1; i >= 0; i--) {

                                removeFragment();
                                cityName.remove(yourChoices.get(i)+1);
                                mWeatherIdlist.remove(yourChoices.get(i)+1);

                            }
                            if(yourChoices.get(0)+1<list.size()){
                            for(int j=(yourChoices.get(0)+1);j<list.size();j++)
                            requestWeather(mWeatherIdlist.get(j),j);
                            }
                            else swipeRefresh.setRefreshing(false);
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putInt("length", list.size());
                            editor.apply();
                            Log.e(TAG, "选项 "+yourChoices+"size"+size);
                            Log.e(TAG, "剩余城市"+cityName);
                        }
                    });

            multiChoiceDialog.show();
        }

    public void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(WeatherActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
    Weather blweather;
    public  boolean idDuan(String weatherId,final int id){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);

            String weatherString = prefs.getString(ShardId.key.get(id), null);

            if (weatherString != null) {

             final   Weather weather = Utility.handleWeatherResponse(weatherString);
                if(weather != null && "ok".equals(weather.status)) {
                    String therId = weather.basic.weatherId;
                    String ciname=weather.basic.cityName;
                    if (weatherId.equals(therId)&& cityName.get(id).equals(ciname)) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                upData(id, weather);

                            }
                        });

                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                        editor.putString(ShardId.key.get(id), weatherString);
                        editor.apply();
                        return true;
                    }
                }
            }

        blweather=null;
        for(int i=0;i<ShardId.key.size();i++) {
            String    wweatherString = prefs.getString(ShardId.key.get(i), null);

            if (wweatherString != null) {

                 blweather = Utility.handleWeatherResponse(wweatherString);
                if(blweather != null && "ok".equals(blweather.status)) {
                    String therId = blweather.basic.weatherId;
                    String ciname=blweather.basic.cityName;
                    if (weatherId.equals(therId)&& cityName.get(id).equals(ciname)) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                upData(id, blweather);

                            }
                        });
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                        editor.putString(ShardId.key.get(id), wweatherString);
                        editor.apply();

                        return true;
                    }
                }
            }
        }


        return  false;
    }

    private void yiDialog() {
    /*@setView 装入一个EditView
     */
        final EditText editText = new EditText(WeatherActivity.this);
        AlertDialog.Builder inputDialog =
                new AlertDialog.Builder(WeatherActivity.this,AlertDialog.THEME_HOLO_LIGHT);
        inputDialog.setTitle("请提出您的建议").setView(editText);
        inputDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        inputDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        /*Toast.makeText(WeatherActivity.this,
                                editText.getText().toString(),
                                Toast.LENGTH_SHORT).show();*/
                        String neirong=editText.getText().toString();
                        if(isSKong(neirong)) return;
                      showToast("谢谢您的宝贵建议");
                    }
                }).show();
    }

    public  boolean isSKong(String str){  //判断是否全是空字符包括制表符等
        if(str==null) return true;
        Pattern pattern = Pattern.compile("[\\s]*");
        return pattern.matcher(str).matches();
    }

    private int freqFind(int myfreq){

        for(int i=0;i<fraq.length;i++){
            if(fraq[i]==myfreq) return i;
        }
        return  0;
    }

    //private AlertDialog mAlertDialog;
    private int   myChoice=0;
    private int[] fraq={1,2,4,8,12};
    private     void   fraqListDialog() {

        final   String[] items = {  "1个小时" ,"2个小时", "4个小时", "8个小时","12个小时"};

        AlertDialog.Builder listDialog =

                new   AlertDialog.Builder(WeatherActivity.this,AlertDialog.THEME_HOLO_LIGHT);

        listDialog.setTitle(  "请选择推送天气消息的更新频率"  );
        //listDialog.setCancelable(false);

        listDialog.setSingleChoiceItems(items,   myChoice  , new   DialogInterface.OnClickListener() {

            @Override

            public   void   onClick(DialogInterface dialog,   int   which) {

                myChoice = which;
                Log.e(TAG, "which"+which);
            }

        });
        listDialog.setPositiveButton(  "确定"  ,

                new   DialogInterface.OnClickListener() {

                    @Override

                    public   void   onClick(DialogInterface dialog,   int   which) {
                        if (myChoice <0) return;


                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                        editor.putInt("fraquency", fraq[myChoice]);
                        editor.apply();
                        startMySer();




                    }

                });

        listDialog.show();

    }

    @Override
    protected void onDestroy(){

        ShardId.able=true;
        save();
        setNotificationBuilder();
        Log.e(TAG, "onDestroy");
        super.onDestroy();
    }

    public  String listToString(List list, char separator) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i)).append(separator);
        }
        return sb.toString().substring(0, sb.toString().length() - 1);
    }

    private  void save(){
        String mycity=listToString(cityName,',');
        String myweaid=listToString(mWeatherIdlist,',');
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
        editor.putString("city", mycity);
        editor.putString("weaid", myweaid);
        editor.apply();
       /* Log.e(TAG, mycity);
        Log.e(TAG, myweaid);*/
    }



    public void setNotificationBuilder() {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
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
                .setTicker("将继续为您更新天气信息")
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

    //	PendingIntent有4种flag.
    //	- FLAG_ONE_SHOT 只执行一次
    //	- FLAG_NO_CREATE 若描述的Intent不存在则返回NULL值
    //	- FLAG_CANCEL_CURRENT 如果描述的PendingIntent已经存在，则在产生新的Intent之前会先取消掉当前的
    //	- FLAG_UPDATE_CURRENT 总是执行,这个flag用的最多

    private void gyDialog(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this,AlertDialog.THEME_HOLO_LIGHT);

        dialog.setTitle("风和天气结束").setMessage("本应用可以自动定位和手动查询全国各地的天气，收藏多个城市的天气翻页查看，通知栏定时推送更新的天气消息")
                .show();

    }






}
