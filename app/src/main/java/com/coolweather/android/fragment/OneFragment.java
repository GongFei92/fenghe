package com.coolweather.android.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.coolweather.android.R;
import com.coolweather.android.gson.Forecast;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.service.AutoUpdateService;
import com.coolweather.android.util.ShardId;
import com.coolweather.android.util.Utility;



/**
 * Created by Gong on 2016-11-18.
 */

public class OneFragment extends Fragment {

    private  final  String TAG=this.hashCode()+"";



    private ScrollView weatherLayout;
    private TextView titleUpdateTime;
    private TextView titleCity;
    private TextView degreeText;

    private TextView weatherInfoText;

    private LinearLayout forecastLayout;

    private ImageView imageView;
    private TextView aqiText;

    private TextView pm25Text;

    private TextView comfortText;

    private TextView carWashText;

    private TextView sportText;
    public String weatherString=null;
    private Weather weather=null;
    private String cityName="";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.onefra_layout, null);
        // View view = inflater.inflate(R.layout.onefra_layout, container,false);
        initView(view);

        return view;
    }

    private void  initView(View view){
        weatherLayout = (ScrollView) view.findViewById(R.id.weather_layout);
        titleCity = (TextView) view.findViewById(R.id.title_city);
        titleUpdateTime = (TextView) view.findViewById(R.id.title_update_time);
        degreeText = (TextView) view.findViewById(R.id.degree_text);
        imageView=(ImageView)view.findViewById(R.id.image_view);
        //weatherInfoText = (TextView) view.findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout) view.findViewById(R.id.forecast_layout);

        aqiText = (TextView) view.findViewById(R.id.aqi_text);
        pm25Text = (TextView) view.findViewById(R.id.pm25_text);
        comfortText = (TextView) view.findViewById(R.id.comfort_text);
        carWashText = (TextView) view.findViewById(R.id.car_wash_text);
        sportText = (TextView) view.findViewById(R.id.sport_text);

        shuaXin();
        Log.e(TAG, weather+"oncreate");
    }

/*    @Override
    public void onStart() {
        super.onStart();

    }*/

    public void setWeather(Weather ther){
        weather=ther;
        Log.e(TAG, weather+"");

    }
    public void setCity(String ther){
        weather=null;
        cityName=ther;
        Log.e(TAG, cityName+"");

    }
    public void shuaXin( ){
        if(weather==null)
        {
          if(cityName==null)
              cityName="";
           titleCity.setText(cityName);
            //weatherLayout.setVisibility(View.INVISIBLE);
            }

       else showWeatherInfo(weather);

    }
    private void getArgs(){
        //判断Fragment已经依附Activity
            Bundle args = getArguments();
            if (args != null) {
                 weatherString = getArguments().getString("weather");

            }


    }
    public void updateArguments(String  weatherString) {

          this.weatherString=weatherString;
        Bundle args = getArguments();
        if (args != null) {
            args.putString("weather", weatherString);

        }
    }

    private  int findStr(String str){
        for(int i=0;i< ShardId.describe.size();i++){
            if(str.contains(ShardId.describe.get(i)))
                return i;
        }
        return -1;
    }

    private void city(String  weatherString){
        titleCity.setText(weatherString);
    }
    /**
     * 处理并展示Weather实体类中的数据。
     */
    private void showWeatherInfo(Weather weather) {
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.more.info;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        //weatherInfoText.setText(weatherInfo);
        int keyvalue=findStr(weatherInfo);
        if(keyvalue>-1) {

            Log.e(TAG, ShardId.describe.get(keyvalue)+cityName+weatherInfo);
            int resId = getResources().getIdentifier(ShardId.imagName.get(keyvalue), "drawable", getActivity().getPackageName());
            imageView.setImageResource(resId);
            imageView.setVisibility(View.VISIBLE);
        }
        else {
            imageView.setVisibility(View.GONE);
        }

        forecastLayout.removeAllViews();
        for (Forecast forecast : weather.forecastList) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.forecast_item, forecastLayout, false);
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

    }
    public void startMySer(){
        Intent intent = new Intent(getActivity(), AutoUpdateService.class);
        getActivity().startService(intent);
    }

}
