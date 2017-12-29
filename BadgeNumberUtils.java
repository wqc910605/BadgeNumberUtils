package com.zhongyuan.myapplication;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

import com.zhongyuan.myapplication.badgenumberlibrary.MyLifecycleHandler;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by zy01060 on 2017/12/29.
 */

public class BadgeNumberUtils {
    private static Handler sHandler = new Handler();

    public static void setBadgeNumber(final Context context, final int number) {
        String brand = Build.MANUFACTURER;
        if (brand.equalsIgnoreCase(MobileBrand.HUAWEI)) {
            setBadgeNumberForHuaWei(context, number);
        } else if (brand.equalsIgnoreCase(MobileBrand.XIAOMI)) {
            if (!MyLifecycleHandler.isApplicationVisible()) {
                sHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setXiaomiBadgeNumber(context, number);
                        sHandler = null;
                    }
                }, 2000);
            }
        } else if (brand.equalsIgnoreCase(MobileBrand.OPPO)) {
            setBadgeNumberForOppo(context, number);
        } else if (brand.equalsIgnoreCase(MobileBrand.VIVO)) {
            setBadgeNumberForVivo(context, number);
        } else {

        }
    }

    //华为 需要权限 <uses-permission android:name="com.huawei.android.launcher.permission.CHANGE_BADGE"/>
    private static void setBadgeNumberForHuaWei(Context context, int number) {
        try {
            if (number < 0) number = 0;
            Bundle bundle = new Bundle();
            bundle.putString("package", context.getPackageName());
            String launchClassName = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName()).getComponent().getClassName();
            bundle.putString("class", launchClassName);
            bundle.putInt("badgenumber", number);
            context.getContentResolver().call(Uri.parse("content://com.huawei.android.launcher.settings/badge/"), "change_badge", null, bundle);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //oppo
    private static void setBadgeNumberForOppo(Context context, int number) {
        try {
            if (number == 0) {
                number = -1;
            }
            Intent intent = new Intent("com.oppo.unsettledevent");
            intent.putExtra("pakeageName", context.getPackageName());
            intent.putExtra("number", number);
            intent.putExtra("upgradeNumber", number);
            if (canResolveBroadcast(context, intent)) {
                context.sendBroadcast(intent);
            } else {
                try {
                    Bundle extras = new Bundle();
                    extras.putInt("app_badge_count", number);
                    context.getContentResolver().call(Uri.parse("content://com.android.badge/badge"), "setAppBadgeCount", null, extras);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean canResolveBroadcast(Context context, Intent intent) {
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> receivers = packageManager.queryBroadcastReceivers(intent, 0);
        return receivers != null && receivers.size() > 0;
    }

    //vivo
    private static void setBadgeNumberForVivo(Context context, int number) {
        try {
            Intent intent = new Intent("launcher.action.CHANGE_APPLICATION_NOTIFICATION_NUM");
            intent.putExtra("packageName", context.getPackageName());
            String launchClassName = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName()).getComponent().getClassName();
            intent.putExtra("className", launchClassName);
            intent.putExtra("notificationNum", number);
            context.sendBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //小米
    //在调用NotificationManager.notify(notifyID, notification)这个方法之前先设置角标显示的数目
    private static void setBadgeNumberForXiaoMi(Notification notification, int number) {
        try {
            Field field = notification.getClass().getDeclaredField("extraNotification");
            Object extraNotification = field.get(notification);
            Method method = extraNotification.getClass().getDeclaredMethod("setMessageCount", int.class);
            method.invoke(extraNotification, number);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void setXiaomiBadgeNumber(Context context, int number) {
        NotificationManager notificationManager = (NotificationManager) context.
                getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new NotificationCompat.Builder(context)
                .setSmallIcon(context.getApplicationInfo().icon)
                .setWhen(System.currentTimeMillis())
                .setContentTitle("推送标题")
                .setContentText("我是推送内容")
                .setTicker("ticker")
                .setAutoCancel(true)
                .build();
        //相邻的两次角标设置如果数字相同的话，好像下一次会不生效
        setBadgeNumberForXiaoMi(notification, number);
        notificationManager.notify(1000, notification);
        Toast.makeText(context, "设置桌面角标成功", Toast.LENGTH_SHORT).show();

    }

    private static class MobileBrand {

        public final static String HUAWEI = "Huawei";
        public final static String MEIZU = "Meizu";
        public final static String XIAOMI = "Xiaomi";
        public final static String SONY = "Sony";
        public final static String OPPO = "OPPO";
        public final static String VIVO = "vivo";
        public final static String SAMSUNG = "samsung";
        public final static String LG = "LG";
        public final static String LETV = "Letv";
        public final static String ZTE = "ZTE";
        public final static String YULONG = "YuLong";
        public final static String LENOVO = "LENOVO";

    }
}
