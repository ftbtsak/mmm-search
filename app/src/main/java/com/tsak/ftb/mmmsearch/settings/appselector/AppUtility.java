package com.tsak.ftb.mmmsearch.settings.appselector;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import java.util.List;

public class AppUtility {

    public final static String UNSELECTED_APP = "未選択";

    private AppUtility() {
        throw new AssertionError();
    }

    public static Drawable getApplicationIcon(Context context, String packageName)
            throws PackageManager.NameNotFoundException {

        PackageManager pm = context.getPackageManager();
        return pm.getApplicationIcon(packageName);
    }

    public interface OnCollectListener {
        void onCollect(AppInfo appInfo);
    }

    public static void collect(Context context, OnCollectListener onCollectListener) {

        PackageManager pm = context.getPackageManager();
        final List<ApplicationInfo> installedAppList = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo installedApp : installedAppList) {
            if (null != pm.getLaunchIntentForPackage(installedApp.packageName)) {
                String labelName = pm.getApplicationLabel(installedApp).toString();
                String packageName = installedApp.packageName;
                Intent launchIntent = pm.getLaunchIntentForPackage(installedApp.packageName);
                String className = null != launchIntent ?
                        (null != launchIntent.getComponent() ? launchIntent.getComponent().getClassName() : null)
                        : null;
                if (null != className) {
                    try {
                        onCollectListener.onCollect(new AppInfo.AppInfoBuilder(labelName, packageName, className)
                                .setIcon(pm.getApplicationIcon(installedApp.packageName)).build());
                    } catch (PackageManager.NameNotFoundException ignored) {}
                }
            }
        }
    }
}
