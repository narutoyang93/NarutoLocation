package com.naruto.location;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private MyLocationNoBd myLocationNoBd;
    private MyLocation myLocation;
    private static final int PERMISSIONS_REQUEST_CODE_LOCATION = 100;
    private int locationType = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myLocationNoBd = new MyLocationNoBd(this);
        myLocation = new MyLocation(this, false);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE_LOCATION:
                boolean isAllGranted = true;
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        isAllGranted = false;
                        break;
                    }
                }
                if (isAllGranted) {
                    getLocationInfo(locationType);
                } else {
                    Toast.makeText(this, "授权失败", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case LocationHelper.REQUEST_CODE_GPS:
                getLocationInfo(locationType);
                break;
        }
    }

    /**
     * 获取位置信息
     *
     * @param type
     */
    public void getLocationInfo(int type) {
        locationType = type;
        switch (type) {
            case 1:
                if (myLocationNoBd != null) {
                    myLocationNoBd.getLocation();
                }
                break;
            case 2:
                if (myLocation != null) {
                    myLocation.locating();
                }
                break;
        }
    }

    public void getLocationInfoByPrimordial(View view) {
        getLocationInfo(1);
    }

    public void getLocationInfoByBaidu(View view) {
        getLocationInfo(2);
    }
}
