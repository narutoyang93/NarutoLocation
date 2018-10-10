package com.naruto.location;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

/**
 * @Purpose 百度定位
 * @Author Naruto Yang
 * @CreateDate Sep 16, 2016
 * @Note -------------------------------------------------
 * <p>
 * M1 Jun 16, 2017 NarutoYang 1：the first version
 * <p>
 * the latest update: Sep 27, 2016 10:30:25 AM
 */
public class MyLocation {
    private LocationClient mLocationClient = null;
    private BDLocationListener myListener;
    private String latitude = "";
    private String longitude = "";
    private ProgressDialog dialog;
    private int count = 0;
    private int count2 = 0;
    private String countryName = "";
    private String address = "";
    private String locatingState = "locating";
    private Context context;
    private boolean isOffline = false;
    private boolean isRunInBackground = true;
    private final static String ak = "TmoNwjftaSokTtTZZ6G9n59lw1mrXkcl";
    private MyHandler handler;
    private static final String TAG = "MyLocation";

    public MyLocation(Context context, Context context2, boolean isRunInBackground) {
        super();
        this.context = context2;
        this.isRunInBackground = isRunInBackground;
        handler = new MyHandler();
        mLocationClient = new LocationClient(context);// 声明LocationClient类
        myListener = new MyLocationListener();
        mLocationClient.registerLocationListener(myListener); // 注册监听函数
        setLocationOption();
        if (!isRunInBackground) {// 运行与前台才需要dialog
            dialog = new ProgressDialog(context2, AlertDialog.THEME_HOLO_LIGHT);
            dialog.setMessage("Please wait while the permissions are verified by positioning......");
            dialog.setCancelable(true);
        }
    }

    // -------------------------------------------------

    /**
     * 设置相关参数
     */
    private void setLocationOption() {
        LocationClientOption option = new LocationClientOption();

        option.setCoorType("bd09ll");// 可选，默认gcj02，设置返回的定位结果坐标系
        option.setIsNeedAddress(true);// 可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);// 可选，默认false,设置是否使用gps
        option.SetIgnoreCacheException(false);// 可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);// 可选，默认false，设置是否需要过滤GPS仿真结果，默认需要

        // option.setLocationMode(LocationMode.Device_Sensors);//
        // 可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        // option.setScanSpan(2000);//
        // 可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        // option.setLocationNotify(true);//
        // 可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        // option.setIsNeedLocationDescribe(true);//
        // 可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        // option.setIsNeedLocationPoiList(true);//
        // 可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        // option.setIgnoreKillProcess(false);//
        // 可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死

        option.disableCache(true);// 禁止启用缓存定位
        mLocationClient.setLocOption(option);
    }


    // 外部调用的定位方法
    public void locating() {
        //检查并申请权限
        if (!LocationHelper.checkAndRequestPermissions(context)) {
            return;
        }

        if (!LocationHelper.checkGps(context)) {
            return;
        }
        locatingState = "locating";
        showOrHideProgressDialog(true);
        countryName = "";
        count2 = 0;
        isOffline = (!(MyTools.isNetworkConnected(context) || MyTools.isConnectedWithWifi(context)));
        doLocating();
    }

    // ------------------------------------------
    // 执行定位
    private void doLocating() {
        count2++;
        System.out.println("--->count2=" + count2);
        mLocationClient.start();// 开始定位
        if (isOffline) {
            mLocationClient.requestOfflineLocation();
        } else {
            mLocationClient.requestLocation();
        }

        System.out.println("--->开始定位");
        count = 0;
        locatingAgain();
    }

    // ----------------------------------------------
    // 再次定位
    private void locatingAgain() {
        count++;
        System.out.println("--->count=" + count);
        if (isRunInBackground) {
            // Looper.prepare();
            new Handler(context.getMainLooper()).postDelayed(new Runnable() {
                public void run() {
                    finishOrContinueLocating();
                }
            }, 1000);
            // Looper.loop();
        } else {
            new Handler(context.getMainLooper()).postDelayed(new Runnable() {
                public void run() {
                    finishOrContinueLocating();
                }
            }, 1000);
        }

    }

    // -------------------------------------------------------

    // 是否继续定位
    private void finishOrContinueLocating() {
        if (latitude.equals("") || longitude.equals("")) {
            if (count < 4) {
                locatingAgain();
            } else {
                if (count2 < 4) {
                    doLocating();
                } else {
                    countryName = "";
                    mLocationClient.stop();// 停止定位
                    System.out.println("--->停止定位");
                    showOrHideProgressDialog(false);
                    showDialog("定位失败",
                            "Positioning failure!Please check whether the GPS is open and network is connect,then try again.");
                    locatingState = "located";
                    System.out.println("--->经纬度为空，定位失败");
                }
            }
        } else {
            System.out.println("--->latitude=" + latitude);
            System.out.println("--->longitude=" + longitude);
            mLocationClient.stop();// 停止定位
            System.out.println("--->停止定位");

            // 获取当前时间
            Calendar calendar = Calendar.getInstance();
            final String date = calendar.get(Calendar.YEAR) + "-"
                    + String.format("%02d", (calendar.get(Calendar.MONTH) + 1)) + "-"
                    + String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH));
            String time = String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":"
                    + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":"
                    + String.format("%02d", calendar.get(Calendar.SECOND));


            //writeDataToFile();

            String message = "经度：" + longitude + "\n纬度：" + latitude;
            if (isOffline) {
                showDialog("离线定位成功", message);
                latitude = "";
                longitude = "";
            } else {
                final String time0 = calendar.get(Calendar.HOUR_OF_DAY) > 12 ? "PM" : "AM";
                showDialog("定位成功", message + "\n当前地区：" + countryName + "\n地址：" + address);
            }
            showOrHideProgressDialog(false);
        }
    }

    // 执行网络请求后
    private void afterNetWork(String result, String time, String date) {
        if (result != null && !result.equals("")) {
            countryName = JsonParser(result);
            if (countryName.equals("")) {
                System.out.println("--->国家信息获取失败，定位失败");
                showDialog("定位失败", "Failed to get the Country, please try again.");
            } else {
                showDialog("定位成功", "经度：" + longitude + "\n纬度：" + longitude + "\n当前地区：" + countryName);
            }
            showOrHideProgressDialog(false);
        } else {
            showOrHideProgressDialog(false);
            countryName = "";
            System.out.println("--->网络请求失败！");
            showDialog("网络请求失败", "Network request failed, please try again.");
        }
        locatingState = "located";
        longitude = "";
        longitude = "";
    }


    // ----------------------------------------------------

    // json解析
    private static String JsonParser(String jsonString) {
        System.out.println("--->jsonString=" + jsonString);
        String countryName = "";
        int countryCode = -1;
        int cityCode = -1;
        try {
            JSONObject rootJsonObject = new JSONObject(jsonString);
            if (rootJsonObject.has("result")) {
                JSONObject resultJsonObject = rootJsonObject.getJSONObject("result");
                if (resultJsonObject.has("cityCode")) {
                    cityCode = resultJsonObject.getInt("cityCode");
                }
                if (resultJsonObject.has("addressComponent")) {
                    JSONObject addressComponentJsonObject = resultJsonObject.getJSONObject("addressComponent");
                    if (addressComponentJsonObject.has("country_code")) {
                        countryCode = addressComponentJsonObject.getInt("country_code");
                    }
                }
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (countryCode == 0) {
            switch (cityCode) {
                case 2912:
                    countryName = "HK";// 香港
                    break;
                case 2911:
                    countryName = "MC";// 澳门
                    break;
                default:
                    countryName = "PRC";// 大陆
                    break;
            }
        } else {
            countryName = "OS";// 海外
        }
        return countryName;
    }

    /**
     * @param isShow
     */
    private void showOrHideProgressDialog(boolean isShow) {
        Message message = new Message();
        message.what = MyHandler.ACTION_DIALOG;
        message.arg1 = isShow ? MyHandler.SHOW_DIALOG : MyHandler.DISMISS_DIALOG;
        handler.handleMessage(message);
    }

    // 显示弹窗
    private void showDialog(String title, String message) {
        if (!isRunInBackground) {
            MyTools.showMyDialog(context, title, message, "OK", null, false, null, null);
        }
    }

    private void writeDataToFile() {
        File folderPath = new File(Environment.getExternalStorageDirectory(), "01Text");
        if (!folderPath.exists()) {// 如果目标文件夹不存在，就自动创建
            folderPath.mkdir();
        }
        File file = new File(folderPath, "location.txt");
        try {
            if (!file.exists()) {// 如果文件不存在
                file.createNewFile();
            }
            BufferedWriter bw;
            bw = new BufferedWriter(new FileWriter(file, true));
            String info = "\n-------\n" + MyTools.getNowDateTimeString("yyyy-MM-dd HH:mm:ss\n") + "latitude=" + latitude
                    + ", longitude=" + longitude + "\n-------";
            bw.write(info);
            bw.write("\r\n");
            bw.flush();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // 定位监听
    private class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location == null) {
                System.out.println("--->location为null");
                latitude = "";
                longitude = "";
                return;
            }
            int locType = location.getLocType();
            System.out.println("--->locType=" + locType);
            if (locType == BDLocation.TypeGpsLocation || locType == BDLocation.TypeNetWorkLocation
                    || locType == BDLocation.TypeOffLineLocation) {
                latitude = String.valueOf(location.getLatitude());
                longitude = String.valueOf(location.getLongitude());
                String cityCode = location.getCityCode();
                String countryCode = location.getCountryCode();
                address = location.getAddrStr();
                if (cityCode == null || countryCode == null) {
                    cityCode = "";
                    countryCode = "";
                } else {
                    if (countryCode.equals("0")) {
                        switch (cityCode) {
                            case "2912":
                                countryName = "HK";// 香港
                                break;
                            case "2911":
                                countryName = "MC";// 澳门
                                break;
                            default:
                                countryName = "PRC";// 大陆
                                break;
                        }
                    } else {
                        countryName = "OS";// 海外
                    }
                }

            }
        }

    }

    /**
     * @Purpose
     * @Author Naruto Yang
     * @CreateDate 2018/10/10
     * @Note
     */
    private class MyHandler extends Handler {
        public static final int SHOW_DIALOG = 100;
        public static final int DISMISS_DIALOG = 101;
        public static final int ACTION_DIALOG = 10;

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case ACTION_DIALOG:
                    if (!isRunInBackground && dialog != null) {
                        try {
                            switch (msg.arg1) {
                                case SHOW_DIALOG:
                                    dialog.show();
                                    break;
                                case DISMISS_DIALOG:
                                    dialog.dismiss();
                                    break;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
        }
    }

}
