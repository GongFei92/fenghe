package com.coolweather.android;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.baidu.location.BDAbstractLocationListener;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.coolweather.android.db.City;
import com.coolweather.android.db.County;
import com.coolweather.android.db.Province;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {

    private static final String TAG = "ChooseAreaFragment";

    public static final int LEVEL_PROVINCE = 0;

    public static final int LEVEL_CITY = 1;

    public static final int LEVEL_COUNTY = 2;

    private ProgressDialog progressDialog;
    private ProgressDialog waitingDialog;
    private TextView titleText;

    private Button backButton;
    private Button locButton;

    private ListView listView;

    private ArrayAdapter<String> adapter;

    private List<String> dataList = new ArrayList<>();
    private LocationClient mLocationClient=null;
    /**
     * 省列表
     */
    private List<Province> provinceList;

    /**
     * 市列表
     */
    private List<City> cityList;

    /**
     * 县列表
     */
    private List<County> countyList;

    /**
     * 选中的省份
     */
    private Province selectedProvince;

    /**
     * 选中的城市
     */
    private City selectedCity;

    /**
     * 当前选中的级别
     */
    private int currentLevel;
    //private MyLocationListener myListener = new MyLocationListener();
    private boolean timeOut=false;
    private Timer timer = new Timer();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        titleText = (TextView) view.findViewById(R.id.title_text);
        backButton = (Button) view.findViewById(R.id.back_button);
        locButton = (Button) view.findViewById(R.id.loc_button);
        listView = (ListView) view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
        mLocationClient=new LocationClient(getActivity());
        mLocationClient.registerLocationListener(mListener);

        return view;
    }

/*    @Override
    public void onResume(){
        super.onResume();
        Log.e(TAG, "onResume: 进来");
        if (getActivity() instanceof WeatherActivity) {
            WeatherActivity activity = (WeatherActivity) getActivity();
            if(activity.myFlag) {
                locButton.setVisibility(View.GONE);
                Log.e(TAG, "on: "+activity.myFlag);
            }
        }
    }*/
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);



        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
               if(isNetworkAvailable(getActivity())==false){
                   Toast.makeText(getContext(), "网络无法连接,请您连接网络", Toast.LENGTH_SHORT).show();
                   if (getActivity() instanceof WeatherActivity) {
                       WeatherActivity activity = (WeatherActivity) getActivity();
                       activity.drawerLayout.closeDrawers();
                       if(activity.myFlag) {
                           activity.juLi(2);
                           activity.myFlag=false;
                       }
                   }
                   return;

               }

                   if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provinceList.get(position);
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = cityList.get(position);
                    queryCounties();
                } else if (currentLevel == LEVEL_COUNTY) {
                    String weatherId = countyList.get(position).getWeatherId();
                    System.out.println(countyList.get(position).getCityId());
                    System.out.println(countyList.get(position).getCountyName());
                    String name=countyList.get(position).getCountyName();
                    System.out.println(weatherId);
                    if (getActivity() instanceof MainActivity) {
                        Intent intent = new Intent(getActivity(), WeatherActivity.class);
                        intent.putExtra("weather_id", weatherId);
                        intent.putExtra("city_name", name);
                        Log.e(TAG, "onItemClick: "+weatherId);
                        Utility.panDuan(getActivity(),weatherId);
                        startActivity(intent);
                        getActivity().finish();
                        getActivity().overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
                    } else if (getActivity() instanceof WeatherActivity) {
                        WeatherActivity activity = (WeatherActivity) getActivity();
                        activity.drawerLayout.closeDrawers();
                        if(activity.myFlag) {
                            locButton.setVisibility(View.VISIBLE);
                            activity.juLi(2);
                            if(activity.cityName.contains(name)) {
                                activity.showToast(name+"您已收藏过");
                            }
                            else{
                                activity.cityName.add(name);
                                activity.addFragment(weatherId, null);

                                activity.requestWeather(weatherId, activity.cityName.size() - 1);
                            }
                            activity.myFlag = false;
                            Log.e(TAG, "end: "+activity.myFlag);
                        }
                        else {
                            activity.cityName.set(0,name);
                            activity.homePageUpdate(weatherId);
                            activity.switem[2]=1;
                            activity.swipeRefresh.setRefreshing(true);

                        }

                    }
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLevel == LEVEL_COUNTY) {
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    queryProvinces();
                }
            }
        });
        locButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isNetworkAvailable(getActivity())==false){
                    Toast.makeText(getContext(), "网络无法连接,请您连接网络", Toast.LENGTH_SHORT).show();
                    if (getActivity() instanceof WeatherActivity) {
                        WeatherActivity activity = (WeatherActivity) getActivity();
                        activity.drawerLayout.closeDrawers();
                    }
                    return;

                }
                getPersimmions();
                Log.e(TAG, String.format("Hi,%s", "王力"));
                timeOut=true;
                mTimerTask = new MyTimerTask();
                timer.schedule(mTimerTask, 5000);
            }
        });
        queryProvinces();
    }

    private  MyTimerTask mTimerTask;
    public  class MyTimerTask extends TimerTask{
        @Override
        public void run(){

            if(timeOut){

                mLocationClient.stop();
                if (waitingDialog != null)
                    waitingDialog.dismiss();
                showToast("网络无法连接,请您检查网络");
                if (mTimerTask != null){
                    mTimerTask.cancel();  //将原任务从队列中移除
                }
                timeOut=false;

            }
            //execute the task

        }

    }
    public void showToast(final String msg) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
    /**
     * 查询全国所有的省，优先从数据库查询，如果没有查询到再去服务器上查询。
     */
    private void queryProvinces() {
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);
        provinceList = DataSupport.findAll(Province.class);
        if (provinceList.size() > 0) {
            dataList.clear();
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        } else {
            String address = "http://guolin.tech/api/china";
            queryFromServer(address, "province");
        }
    }

    /**
     * 查询选中省内所有的市，优先从数据库查询，如果没有查询到再去服务器上查询。
     */
    private void queryCities() {
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceid = ?", String.valueOf(selectedProvince.getId())).find(City.class);
        if (cityList.size() > 0) {
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/" + provinceCode;
            queryFromServer(address, "city");
        }
    }

    /**
     * 查询选中市内所有的县，优先从数据库查询，如果没有查询到再去服务器上查询。
     */
    private void queryCounties() {
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityid = ?", String.valueOf(selectedCity.getId())).find(County.class);
        if (countyList.size() > 0) {
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
            queryFromServer(address, "county");
        }
    }

    /**
     * 根据传入的地址和类型从服务器上查询省市县数据。
     */
    private void queryFromServer(String address, final String type) {
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                    boolean result = false;
                if ("province".equals(type)) {
                    result = Utility.handleProvinceResponse(responseText);
                } else if ("city".equals(type)) {
                    result = Utility.handleCityResponse(responseText, selectedProvince.getId());
                } else if ("county".equals(type)) {
                    result = Utility.handleCountyResponse(responseText, selectedCity.getId());
                }
                if (result) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)) {
                                queryProvinces();
                            } else if ("city".equals(type)) {
                                queryCities();
                            } else if ("county".equals(type)) {
                                queryCounties();
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                // 通过runOnUiThread()方法回到主线程处理逻辑
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(), "加载失败,请检查网络连接", Toast.LENGTH_SHORT).show();
                        if (getActivity() instanceof WeatherActivity) {
                            WeatherActivity activity = (WeatherActivity) getActivity();
                            activity.drawerLayout.closeDrawers();
                            if(activity.myFlag) {
                                activity.juLi(2);
                                activity.myFlag=false;
                            }
                        }
                    }
                });
            }
        });
    }

    /**
     * 显示进度对话框
     */
    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /**
     * 关闭进度对话框
     */
    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    private BDAbstractLocationListener mListener = new BDAbstractLocationListener() {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            Log.e(TAG, "百度地图进来");
            timeOut=false;

            if (mTimerTask != null){
                mTimerTask.cancel();  //将原任务从队列中移除
            }
            if (bdLocation == null||bdLocation.getLocType() == BDLocation.TypeServerError) {
                showToast("定位失败，请手动定位！");
                if (waitingDialog != null)
                    waitingDialog.dismiss();
                return;
            }
            String privince=bdLocation.getProvince().replaceAll("省","").replaceAll("市","");
            String city=bdLocation.getCity().replaceAll("市","");
            String district=bdLocation.getDistrict().replaceAll("县","").replaceAll("区","");

            mLocationClient.stop();
            // mLocationClient=null;

            Toast.makeText(getContext(),privince+city+district,Toast.LENGTH_SHORT).show();


            queryProvince(privince,city,district);
        }
    };

    private void initLocation(){

        LocationClientOption option=new LocationClientOption();
        option.setIsNeedAddress(true);
        //option.setTimeOut(400);
        mLocationClient.setLocOption(option);
        mLocationClient.start();
        Log.e(TAG, "定位选项");
    }
    private   void   showWaitingDialog() {

       /* 等待Dialog具有屏蔽其他控件的交互能力

        * @setCancelable 为使屏幕不可点击，设置为不可取消(false)

        * 下载等事件完成后，主动调用函数关闭该Dialog

        */

        waitingDialog=

                new   ProgressDialog(getActivity()  );

        waitingDialog.setTitle(  "定位中"  );

        waitingDialog.setMessage(  "请等待..."  );

        waitingDialog.setIndeterminate(  true  );

        waitingDialog.setCancelable(  false  );

        waitingDialog.show();

    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        if(mLocationClient!=null) {
            mLocationClient.unRegisterLocationListener(mListener); //注销掉监听
            mLocationClient.stop();
            timer.cancel();
        }


    }

    private void queryProvince(String province,String city,String county){
        List<Province> provinceList;
        Province oProvince;
        City oCity;
        County oCounty ;
        provinceList= DataSupport.where("provinceName=?",province).find(Province.class);
        if(provinceList.size() > 0){
            oProvince=provinceList.get(0);
            Log.e(TAG, oProvince.getProvinceName());
            List<City> cityList;
            cityList = DataSupport.where("provinceid = ?", String.valueOf(oProvince.getId())).find(City.class);

            if (cityList.size() > 0) {
                oCity =cityList.get(0);
                cityList = DataSupport.where("provinceid = ? and cityName=?", String.valueOf(oProvince.getId()),city).find(City.class);
                if (cityList.size() > 0) oCity=cityList.get(0);
                Log.e(TAG, oCity.getCityName());
                List<County> countyList;
                countyList = DataSupport.where("cityid = ?", String.valueOf(oCity.getId())).find(County.class);
                if (countyList.size() > 0) {
                    oCounty=countyList.get(0);
                    countyList = DataSupport.where("cityid = ? and countyName=?", String.valueOf(oCity.getId()),county).find(County.class);
                    if (countyList.size() > 0) oCounty=countyList.get(0);
                    Log.e(TAG, oCounty.getCountyName());

                    String weatherId = oCounty.getWeatherId();
                    String name=oCounty.getCountyName();
                    if (getActivity() instanceof MainActivity) {
                        Intent intent = new Intent(getActivity(), WeatherActivity.class);
                        intent.putExtra("weather_id", weatherId);
                        intent.putExtra("city_name", name);
                        Utility.panDuan(getActivity(),weatherId);

                        startActivity(intent);
                        getActivity().overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
                        getActivity().finish();
                        if (waitingDialog != null)
                            waitingDialog.dismiss();
                    } else if (getActivity() instanceof WeatherActivity) {
                        WeatherActivity activity = (WeatherActivity) getActivity();
                        activity.drawerLayout.closeDrawers();

                        activity.swipeRefresh.setRefreshing(true);
                        if (waitingDialog != null)
                            waitingDialog.dismiss();
                        activity.cityName.set(0,name);
                        activity.homePageUpdate(weatherId);
                        activity.switem[2]=1;
                    }
                }
                else {
                    queryProFromServer("http://guolin.tech/api/china/"+oProvince.getProvinceCode()+"/" + oCity.getCityCode(),"county",province,city,county,oProvince.getId(),oCity.getId());
                }
            }
            else {
                queryProFromServer("http://guolin.tech/api/china/"+oProvince.getProvinceCode(),"city",province,city,county,oProvince.getId(),0);
            }
        }

        else{
            queryProFromServer("http://guolin.tech/api/china","province",province,city,county,0,0);
        }

    }

    /**
     * 根据传入的地址和类型从服务器上查询省市县数据。
     */
    private void queryProFromServer(String address, final String type,final String mprovince,final String mCity,final String mCounty,final int city,final int county) {

        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                if ("province".equals(type)) {
                    result = Utility.handleProvinceResponse(responseText);
                } else if ("city".equals(type)) {
                    result = Utility.handleCityResponse(responseText, city);
                } else if ("county".equals(type)) {
                    result = Utility.handleCountyResponse(responseText, county);
                }
                if (result) {
                    queryProvince(mprovince,mCity,mCounty);
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                // 通过runOnUiThread()方法回到主线程处理逻辑
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        waitingDialog.dismiss();
                        Toast.makeText(getActivity(), "加载失败,请检查网络连接", Toast.LENGTH_SHORT).show();
                        if (getActivity() instanceof WeatherActivity) {
                            WeatherActivity activity = (WeatherActivity) getActivity();
                            activity.drawerLayout.closeDrawers();
                        }
                    }
                });
            }
        });
    }


    private void getPersimmions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArrayList<String> permissions = new ArrayList<String>();
            /***
             * 定位权限为必须权限，用户如果禁止，则每次进入都会申请
             */
            // 定位精确位置
            if(ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            if(ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            }
			/*
			 * 读写权限和电话状态权限非必要权限(建议授予)只会申请一次，用户同意或者禁止，只会弹一次
			 */
            // 读写权限
            if (ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            // 读取电话状态权限
            if (ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_PHONE_STATE);
            }

            if (permissions.size() > 0) {
                requestPermissions(permissions.toArray(new String[permissions.size()]), 1);
            }
            else{
                showWaitingDialog();
                initLocation();
                System.out.println("locationwu");
            }

        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode){

            case 1:
                if(grantResults.length>0){
                    for(int result:grantResults){
                        if(result!=PackageManager.PERMISSION_GRANTED)
                            Toast.makeText(getActivity(),"您已拒绝自动定位，请手动定位！",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    showWaitingDialog();
                    initLocation();
                    System.out.println("locationqe");
                }
                else {
                    Toast.makeText(getActivity(),"发生未知错误，请手动定位！",Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }

    }

    //判断网络连接是否可用
    public  boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
        } else {
            NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();
            if (networkInfo != null&&networkInfo.length>0) {
                for (int i = 0; i < networkInfo.length; i++) {
                    if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }






}
