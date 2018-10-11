package com.naruto.location;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class MyLocationNoBd {
    private String address;
    private String country;
    private String province;
    private String city;
    private String district;
    private String street;
    private String street_number;
    private String sematic_description;
    private double longitude;
    private double latitude;
    private Context context;
    private boolean isGetLocationSuccessful;
    public static ProgressDialog dialog;
    private final static String LOCATION_URL = "http://api.map.baidu.com/geocoder/v2/?ak=86D6dcKlHzqKpqS03HiGtPhqtql7Nlfj&output=json";
    private static final String TAG = "MyLocationNoBd";

    public MyLocationNoBd(Context context) {
        super();
        this.context = context;
        dialog = new ProgressDialog(context, AlertDialog.THEME_HOLO_LIGHT);
        dialog.setMessage("正在获取位置信息，请稍候...");
        dialog.setCancelable(true);
        getLocation();
    }

    /**
     * 初始化
     */
    private void init() {
        isGetLocationSuccessful = false;
        address = "";
        country = "";
        province = "";
        city = "";
        district = "";
        street = "";
        street_number = "";
        sematic_description = "";
        longitude = 0;
        latitude = 0;
    }

    /**
     * 对外提供的获取定位的方法
     */
    public void getLocation() {
        //检查并申请权限
        if (!LocationHelper.checkAndRequestPermissions(context)) {
            return;
        }

        if (!LocationHelper.checkGps(context)) {
            return;
        }

        init();
        dialog.show();
        LocationManager locationManager = (LocationManager) context
                .getSystemService(context.LOCATION_SERVICE);

        //检查权限
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        // 获取GPS支持
        Location location = locationManager
                .getLastKnownLocation(locationManager.GPS_PROVIDER);
        if (location == null) {
            // 获取NETWORK支持
            location = locationManager
                    .getLastKnownLocation(locationManager.NETWORK_PROVIDER);
        }
        if (location != null) {
            longitude = location.getLongitude();
            latitude = location.getLatitude();
            Log.d(TAG, "getLocation: 经度=" + longitude + ";纬度=" + latitude + "location=" + location.toString());
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("location", location.getLatitude() + "," + location.getLongitude());
            try {
                String jsonString = new Mytask().execute(map).get();
                Log.d(TAG, "getLocation: json=" + jsonString);
                if (jsonString != null && !jsonString.equals("")) {
                    JsonParser(jsonString);
                    isGetLocationSuccessful = true;
                    showDialog("定位成功", country + address + sematic_description);
                } else {
                    isGetLocationSuccessful = false;
                    Log.e(TAG, "getLocation: 网络请求失败！");
                    showDialog("ERROR", "网络请求失败！");
                }
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                isGetLocationSuccessful = false;
                showDialog("ERROR", e.getMessage());
            } catch (ExecutionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                isGetLocationSuccessful = false;
                showDialog("ERROR", e.getMessage());
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT);
            }
        } else {
            isGetLocationSuccessful = false;
            Log.d(TAG, "getLocation: 定位失败！");
            showDialog("ERROR", "定位失败！");
        }
        dialog.dismiss();

    }

    public static class Mytask extends
            AsyncTask<Map<String, Object>, Void, String> {

        @Override
        protected String doInBackground(Map<String, Object>... arg0) {
            // TODO Auto-generated method stub
            String result = "";
            try {
                result = MyTools.sendData(LOCATION_URL, arg0[0],
                        "utf-8");
                // result = requestByHttpGet(arg0[0]);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return result;
        }

    }

    // json解析
    private void JsonParser(String jsonString) {
        // String key[] = { "country", "province", "city", "district", "street",
        // "street_number" };
        try {
            JSONObject rootJsonObject = new JSONObject(jsonString);
            if (rootJsonObject.has("result")) {
                JSONObject resultJsonObject = rootJsonObject
                        .getJSONObject("result");
                if (resultJsonObject.has("formatted_address")) {
                    address = resultJsonObject.getString("formatted_address");
                }
                if (resultJsonObject.has("sematic_description")) {
                    sematic_description = resultJsonObject
                            .getString("sematic_description");
                }
                if (resultJsonObject.has("addressComponent")) {
                    JSONObject addressComponentJsonObject = resultJsonObject
                            .getJSONObject("addressComponent");
                    // for (int i = 0; i < key.length; i++) {
                    // if (addressComponentJsonObject.has(key[i])) {
                    // map.put(key[i], resultJsonObject.getString(key[i]));
                    // }
                    // }
                    if (addressComponentJsonObject.has("country")) {
                        country = addressComponentJsonObject
                                .getString("country");
                    }
                    if (addressComponentJsonObject.has("province")) {
                        province = addressComponentJsonObject
                                .getString("province");
                    }
                    if (addressComponentJsonObject.has("city")) {
                        city = addressComponentJsonObject.getString("city");
                    }
                    if (addressComponentJsonObject.has("district")) {
                        district = addressComponentJsonObject
                                .getString("district");
                    }
                    if (addressComponentJsonObject.has("street")) {
                        street = addressComponentJsonObject.getString("street");
                    }
                    if (addressComponentJsonObject.has("street_number")) {
                        street_number = addressComponentJsonObject
                                .getString("street_number");
                    }
                }
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // 显示弹窗
    private void showDialog(String title, String message) {
        MyTools.showMyDialog(context, title, message, "OK", null, false, null,
                null);
    }

    public boolean isGetLocationSuccessful() {
        return isGetLocationSuccessful;
    }

    public void setGetLocationSuccessful(boolean isGetLocationSuccessful) {
        this.isGetLocationSuccessful = isGetLocationSuccessful;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getStreet_number() {
        return street_number;
    }

    public void setStreet_number(String street_number) {
        this.street_number = street_number;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public String getSematic_description() {
        return sematic_description;
    }

    public void setSematic_description(String sematic_description) {
        this.sematic_description = sematic_description;
    }

}
