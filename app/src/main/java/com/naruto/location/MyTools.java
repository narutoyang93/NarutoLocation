package com.naruto.location;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MyTools {
    public interface OperationInterface {
        void done(Object o);
    }

    public static Context context;
    private int widthMeasureSpec;
    private int heightMeasureSpec;
    private final float scale;
    private DisplayMetrics dm;

    public MyTools(Context context) {
        super();
        this.context = context;
        scale = context.getResources().getDisplayMetrics().density;
        widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        WindowManager wManager = (WindowManager) context
                .getApplicationContext().getSystemService(
                        Context.WINDOW_SERVICE);
        dm = new DisplayMetrics();
        wManager.getDefaultDisplay().getMetrics(dm);
    }

    /**
     * 获取view高度
     *
     * @param v
     * @return
     */
    public int getViewHeight(View v) {// 单位：px
        v.measure(widthMeasureSpec, heightMeasureSpec);
        return v.getMeasuredHeight();
    }

    /**
     * 获取view宽度
     *
     * @param v
     * @return
     */
    public int getViewWidth(View v) {// 单位：px
        v.measure(widthMeasureSpec, heightMeasureSpec);
        return v.getMeasuredWidth();
    }

    /**
     * 获取屏幕高度
     *
     * @return
     */
    public int measureHeight() {// 单位：px
        return dm.heightPixels;
    }

    /**
     * 获取屏幕宽度
     *
     * @return
     */
    public int measureWidth() {// 单位：px
        return dm.widthPixels;
    }

    /**
     * 获取状态栏高度
     *
     * @return
     */
    public int getStatusBarHeight() {// 单位：px
        int result = 0;
        int resourceId = context.getResources().getIdentifier(
                "status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素) 注意：看仔细了，dip2px(x)!=dip2px(-x)
     *
     * @param dipValue
     * @return
     */
    public int dip2px(float dipValue) {
        return (int) (dipValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp 注意：看仔细了，px2dip(x)!=px2dip(-x)
     *
     * @param pxValue
     * @return
     */
    public int px2dip(float pxValue) {
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * @param view           要移动的view
     * @param Offset         位移（单位：px）
     * @param isLinearLayout 是否LinearLayout（目前仅支持LinearLayout和RelativeLayout）
     */
    public void horizontalMoveView(final View view, final int Offset,
                                   final boolean isLinearLayout,
                                   final OperationInterface operationInterface) {
        TranslateAnimation tAnim;
        tAnim = new TranslateAnimation(0, Offset, 0, 0);
        tAnim.setInterpolator(new DecelerateInterpolator());
        tAnim.setDuration(800);// 过程时长（毫秒）
        view.startAnimation(tAnim);// 开始动画
        // 在动画结束的时刻，移动view的位置，使view真正移动。
        tAnim.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // TODO Auto-generated method stub
                if (isLinearLayout) {
                    LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view
                            .getLayoutParams();
                    lp.leftMargin += Offset;
                    view.setLayoutParams(lp);
                } else {
                    RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) view
                            .getLayoutParams();
                    lp.leftMargin += Offset;
                    view.setLayoutParams(lp);
                }
                view.clearAnimation();
                operationInterface.done(null);
            }
        });
    }


    // 获取当前时间并格式化
    public static String getNowDateTimeString(String format) {
        String s = "";
        SimpleDateFormat df = new SimpleDateFormat(format);// 设置日期格式
        s = df.format(new Date());// new Date()为获取当前系统时间
        return s;
    }

    // 格式化日期
    public static String formateDate(String dateString, String formateFrom,
                                     String formateTo) {
        if ((dateString == null || dateString.equals(""))
                || (formateFrom == null || formateFrom.equals(""))
                || (formateTo == null || formateTo.equals(""))) {
            return "";
        }
        String string = "";
        SimpleDateFormat sdfF = new SimpleDateFormat(formateFrom,
                Locale.ENGLISH);
        SimpleDateFormat sdfT = new SimpleDateFormat(formateTo, Locale.ENGLISH);
        Date date;
        try {
            date = sdfF.parse(dateString);
            string = sdfT.format(date);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return string;
    }

    // 保存图片到本地
    public static void saveImg(Bitmap bitmap, String fileName, int quality,
                               File folderPath, Context context, boolean overwrite,
                               boolean refreshGallery) {
        // 保存图片
        if (!folderPath.exists()) {// 如果目标文件夹不存在，就自动创建
            folderPath.mkdir();
        }
        File file = new File(folderPath, fileName);
        if (file.exists()) {// 如果文件存在
            if (overwrite) {
                // 删除
                file.delete();
            } else {
                return;
            }
        }

        try {
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(CompressFormat.JPEG, quality, fos);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (refreshGallery) {
            // 通知图库更新
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri uri = Uri.fromFile(file);
            intent.setData(uri);
            context.sendBroadcast(intent);
        }
    }

    // 设置界面背景
    public static void setBackground(Context context, ImageView imageView) {
        String filePath = context.getCacheDir().getAbsolutePath()
                + "/file/background.png";
        File file = new File(filePath);
        if (file.exists()) {// 如果文件存在
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;// 图片高宽度都为原来的二分之一，即图片大小为原来的大小的四分之一
            options.inTempStorage = new byte[5 * 1024]; // 设置16MB的临时存储空间（不过作用还没看出来，待验证）
            Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
            imageView.setImageBitmap(bitmap);
        }
    }


    // 初始化日期时间
    public static void initCalendar(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }


    // 消息弹窗
    public static void showMyDialog(Context context, String title,
                                    String message, String okButtonText, String cancelButonText,
                                    boolean isCancelable, final OperationInterface okOperation,
                                    final OperationInterface cancelOperation) {
        LayoutInflater inflaterDl = LayoutInflater.from(context);
        LinearLayout layout = (LinearLayout) inflaterDl.inflate(
                R.layout.dialog, null);
        final Dialog dialog = new AlertDialog.Builder(context).create();
        dialog.setCancelable(isCancelable);
        dialog.show();
        dialog.getWindow().setContentView(layout);
        LinearLayout titleLayout = (LinearLayout) layout
                .findViewById(R.id.title);
        TextView messageTextView = (TextView) layout.findViewById(R.id.message);
        Button okButton = (Button) layout.findViewById(R.id.okButton);
        Button cancelButton = (Button) layout.findViewById(R.id.cancelButton);
        View line = layout.findViewById(R.id.line2);

        if (title == null || title.equals("")) {// 没有title
            titleLayout.setVisibility(View.GONE);
        } else {
            TextView titleTextView = (TextView) titleLayout.getChildAt(0);
            titleTextView.setText(title);
        }

        messageTextView.setText(message);// 设置弹窗消息内容

        // 没有按钮
        if ((cancelButonText == null || cancelButonText.equals(""))
                && (okButtonText == null || okButtonText.equals(""))) {
            LinearLayout buttonLayout = (LinearLayout) layout
                    .findViewById(R.id.button);
            buttonLayout.setVisibility(View.GONE);
        } else {
            // 没有取消按钮
            if (cancelButonText == null || cancelButonText.equals("")) {
                line.setVisibility(View.GONE);
                cancelButton.setVisibility(View.GONE);
            } else {
                cancelButton.setText(cancelButonText);
                cancelButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        // TODO Auto-generated method stub
                        dialog.dismiss();
                        if (cancelOperation != null) {
                            cancelOperation.done(null);
                        }

                    }
                });
            }

            okButton.setText(okButtonText);
            okButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    // TODO Auto-generated method stub
                    dialog.dismiss();
                    if (okOperation != null) {
                        okOperation.done(null);
                    }
                }
            });
        }

    }

    // 检查设备是否连接网络
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager
                .getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    // 检查设备是否连接wifi
    public static boolean isConnectedWithWifi(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifi.isConnected();
    }

    /**
     * 判断某个服务是否正在运行的方法
     *
     * @param context
     * @param serviceName 是包名+服务的类名（例如：net.loonggg.testbackstage.TestService）
     * @return true代表正在运行，false代表服务没有正在运行
     */
    public boolean isServiceWork(Context context, String serviceName) {
        boolean isWork = false;
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningServiceInfo> list = activityManager.getRunningServices(40);
        if (list.size() <= 0) {
            return false;
        }
        for (int i = 0; i < list.size(); i++) {
            String name = list.get(i).service.getClassName().toString();
            if (name.equals(serviceName)) {
                isWork = true;
                break;
            }
        }
        return isWork;
    }

    // 发送网络请求
    public static String sendData(String path, Map<String, Object> map,
                                  String encode) {
        String result = null;
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(path);
        List<BasicNameValuePair> list = new ArrayList<BasicNameValuePair>();
        try {
            if (map != null && !map.isEmpty()) {
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    String name = entry.getKey();
                    String value = entry.getValue().toString();
                    BasicNameValuePair nameValuePair = new BasicNameValuePair(
                            name, value);
                    list.add(nameValuePair);
                }
            }
            UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(
                    list, encode);
            httpPost.setEntity(urlEncodedFormEntity);
            HttpResponse response = httpClient.execute(httpPost);
            if (response.getStatusLine().getStatusCode() == 200) {
                result = EntityUtils.toString(response.getEntity(), encode);
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return result;
    }


    /**
     * GPS是否已打开
     *
     * @param context
     * @return
     */
    public static boolean isGpsOpen(Context context) {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean isGpsOpen = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return isGpsOpen;
    }

}
