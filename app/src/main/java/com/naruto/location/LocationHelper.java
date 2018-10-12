package com.naruto.location;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * @Purpose
 * @Author Naruto Yang
 * @CreateDate 2018/10/10
 * @Note
 */
public class LocationHelper {
    public final static int REQUEST_CODE_PERMISSIONS = 100;
    public final static int REQUEST_CODE_GPS = 200;
    private static final String TAG = "LocationHelper";


    /**
     * 检查并申请权限
     */
    public static boolean checkAndRequestPermissions(Context context) {
        // 打开定位服务（Android6.0及以上需要动态申请权限，6.0以下执行动态申请可能会炸）
        if ((int) Build.VERSION.SDK_INT < 23) {
            return true;
        }
        String[] permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};

        List<String> requestPermissionsList = new ArrayList<>();
        for (String p : permissions) {
            if (ContextCompat.checkSelfPermission(context, p) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionsList.add(p);
            }
        }
        if (!requestPermissionsList.isEmpty()) {
            String[] requestPermissionsArray = requestPermissionsList.toArray(new String[requestPermissionsList.size()]);
            ActivityCompat.requestPermissions((Activity) context, requestPermissionsArray, REQUEST_CODE_PERMISSIONS);
            return false;
        } else {
            return true;
        }
    }


    //检查GPS开关状态
    public static boolean checkGps(final Context context) {
        if (!MyTools.isGpsOpen(context)) {
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MyTools.showMyDialog(context, null, "Are you sure refuse to use GPS for Location Log 2.0?", "Setting", "Cancel", false, new MyTools.OperationInterface() {
                        @Override
                        public void done(Object o) {
                            // 跳转GPS设置界面
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            try {
                                ((Activity) context).startActivityForResult(intent, REQUEST_CODE_GPS);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, null);
                }
            });
            Log.d(TAG, "checkGps: false");
            return false;
        } else {
            Log.d(TAG, "checkGps: true");
            return true;
        }
    }
}
