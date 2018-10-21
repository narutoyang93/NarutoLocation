package com.naruto.location;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

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
public class LocationHelper {
    private LocationClient mLocationClient = null;
    private MyLocationListener myListener;
    private ProgressDialog progressDialog;
    private Context context;
    private boolean isOffline = false;
    private boolean isRunInBackground = true;
    private int locatingCount;
    private int requestCode_permissions;
    private OperationInterface onLocatingSuccessCallBack;
    private OperationInterface onLocatingErrorCallBack;
    private OperationInterface locationPermissionDeniedCallBack;
    private String errorMessage = "";
    private long currentLocatingOperationKey;
    private static final int TIME_OUT = 5000;//定位超时限制，单位：毫秒
    private BDLocation bdLocation;
    private static final String TAG = "LocationHelper";


    public LocationHelper(final Context context, boolean isRunInBackground, int permissionsRequestCode_location) {
        this.context = context;
        this.isRunInBackground = isRunInBackground;
        this.requestCode_permissions = permissionsRequestCode_location;
        mLocationClient = new LocationClient(context.getApplicationContext());// 声明LocationClient类
        myListener = new MyLocationListener();
        mLocationClient.registerLocationListener(myListener); // 注册监听函数
        setLocationOption();
        if (!isRunInBackground) {// 运行于前台才需要dialog
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog = new ProgressDialog(context, AlertDialog.THEME_HOLO_LIGHT);
                    progressDialog.setMessage("正在获取位置信息，请稍候...");
                    progressDialog.setCancelable(false);
                }
            });
        }
    }


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


    /**
     * 外部调用的定位方法
     */
    public void getLocationInfo() {
        //检查并申请权限
        if (!LocationTool.checkAndRequestPermissions(context)) {
            return;
        }

        if (!LocationTool.checkGps(context)) {
            return;
        }
        onLocatingStart();
        locatingCount = 0;
        doLocating();
    }


    /**
     * 权限申请回调
     *
     * @param grantResults
     */
    public void permissionRequestCallBack(int[] grantResults) {
        boolean isAllGranted = true;
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                isAllGranted = false;
                break;
            }
        }
        if (isAllGranted) {
            getLocationInfo();
        } else {
            if (locationPermissionDeniedCallBack == null) {
                Toast.makeText(context, "授权失败", Toast.LENGTH_SHORT).show();
            } else {
                locationPermissionDeniedCallBack.done(null);
            }

        }
    }


    /**
     * 执行定位
     */
    private void doLocating() {
        if (currentLocatingOperationKey == -1) {
            Log.d(TAG, "doLocating: 定位超时，已取消本次定位操作");
            return;
        }
        locatingCount++;
        bdLocation = null;
        Log.d(TAG, "doLocating: locatingCount=" + locatingCount);
        isOffline = (!(MyTools.isNetworkConnected(context) || MyTools.isConnectedWithWifi(context)));
        if (isOffline) {
            mLocationClient.requestOfflineLocation();
        } else {
            mLocationClient.requestLocation();
        }
    }


    /*    *//**
     * 检查并申请权限
     *//*
    private boolean checkAndRequestPermissions(Context context) {
        String[] permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
        return MyTools.checkPermissions((Activity) context, requestCode_permissions, permissions);
    }*/

    /**
     * 定位开始
     */
    private void onLocatingStart() {
        Log.d(TAG, "onLocatingStart: 开始定位");
        if (!isRunInBackground && progressDialog != null) {
            progressDialog.show();
            final long key = System.currentTimeMillis();
            currentLocatingOperationKey = key;
            new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (currentLocatingOperationKey == key && progressDialog != null && progressDialog.isShowing()) {
                        onLocatingFinish(false);
                    }
                }
            }, TIME_OUT);
        }
        errorMessage = "";
        mLocationClient.start();// 开始定位
    }

    /**
     * 定位结束
     *
     * @param isLocatedSuccess 定位是否成功
     */
    private void onLocatingFinish(boolean isLocatedSuccess) {
        Log.d(TAG, "finishOrContinueLocating: 停止定位");
        mLocationClient.stop();// 停止定位
        currentLocatingOperationKey = -1;

        if (!isRunInBackground) {
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                }
            });
        }

        if (isLocatedSuccess) {//定位成功
            if (onLocatingSuccessCallBack == null) {
                double latitude = bdLocation.getLatitude();
                double longitude = bdLocation.getLongitude();
                String title = "定位成功";
                String message = "经度：" + longitude + "\n纬度：" + latitude;
                if (isOffline) {
                    title = "离线" + title;
                } else {
                    message += "\n地址：" + bdLocation.getAddrStr();
                }
                showDialog(title, message);
                Log.e(TAG, "onLocatingFinish: " + title + "--->" + message.replace("\n", ";"));
            } else {
                onLocatingSuccessCallBack.done(bdLocation);
            }
        } else {//定位失败
            if (onLocatingErrorCallBack == null) {
                Log.e(TAG, "onLocatingFinish: 定位失败");
                String message = TextUtils.isEmpty(errorMessage) ? "请检查网络连接否正常或者GPS是否正常开启，尝试重新请求定位" : errorMessage;
                showDialog("定位失败", message);
            } else {
                onLocatingErrorCallBack.done(bdLocation);
            }
        }
    }


    /**
     * 显示弹窗
     *
     * @param title
     * @param message
     */
    private void showDialog(String title, String message) {
        if (!isRunInBackground) {
            MyTools.showMyDialog(context, title, message, "确定", null, false, null, null);
        }
    }

    /**
     * 定位成功回调
     *
     * @param onLocatingSuccessCallBack
     */
    public void setOnLocatingSuccessCallBack(OperationInterface onLocatingSuccessCallBack) {
        this.onLocatingSuccessCallBack = onLocatingSuccessCallBack;
    }

    /**
     * 定位失败回调
     *
     * @param onLocatingErrorCallBack
     */
    public void setOnLocatingErrorCallBack(OperationInterface onLocatingErrorCallBack) {
        this.onLocatingErrorCallBack = onLocatingErrorCallBack;
    }

    /**
     * 权限拒绝回调
     *
     * @param locationPermissionDeniedCallBack
     */
    public void setLocationPermissionDeniedCallBack(OperationInterface locationPermissionDeniedCallBack) {
        this.locationPermissionDeniedCallBack = locationPermissionDeniedCallBack;
    }

    /**
     * @Purpose 定位监听
     * @Author Naruto Yang
     * @CreateDate 2018/10/11
     * @Note
     */
    private class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            Log.d(TAG, "onReceiveLocation: ");
            boolean isLocatedSuccess = false;
            bdLocation = location;
            if (location == null) {
                Log.d(TAG, "onReceiveLocation: location为null");
                errorMessage = "";
            } else {
                int locType = location.getLocType();
                Log.d(TAG, "onReceiveLocation: locType=" + locType);
                isLocatedSuccess = (locType == BDLocation.TypeGpsLocation || locType == BDLocation.TypeNetWorkLocation
                        || locType == BDLocation.TypeOffLineLocation);
            }

            if (!isLocatedSuccess && locatingCount < 3) {
                //1秒后再次定位
                new android.os.Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        doLocating();
                    }
                }, 1000);
            } else {
                onLocatingFinish(isLocatedSuccess);
            }
        }

        @Override
        public void onLocDiagnosticMessage(int i, int i1, String s) {
            super.onLocDiagnosticMessage(i, i1, s);
            errorMessage = s;
        }
    }

    /**
     * @Purpose
     * @Author Naruto Yang
     * @CreateDate 2018/10/12
     * @Note
     */
    public interface OperationInterface {
        void done(Object o);
    }

}
