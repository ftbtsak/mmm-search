package com.tsak.ftb.mmmsearch.settings.appselector;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

public class AppInfo {
    private String name = "";
    private String packageName = "";
    private String className = "";
    private Drawable icon = new ColorDrawable(Color.TRANSPARENT);

    private AppInfo(){}

    private AppInfo(String name, String packageName, String className, Drawable icon) {
        this.name = name;
        this.packageName = packageName;
        this.className = className;
        this.icon = icon.mutate();
    }

    public String name() {
        return name;
    }

    public String packageName() {
        return packageName;
    }

    public String className() {
        return className;
    }

    public Drawable icon() {
        return icon;
    }

    public static class AppInfoBuilder {
        private AppInfo appInfo = new AppInfo();

        public AppInfoBuilder(String name, String packageName, String className) {
            if (null != name) {
                appInfo.name = name;
            }
            if (null != packageName) {
                appInfo.packageName = packageName;
            }
            if (null != className) {
                appInfo.className = className;
            }
        }

        public AppInfoBuilder setIcon(Drawable icon) {
            if (null != icon) {
                appInfo.icon = icon.mutate();
            }
            return this;
        }

        public AppInfo build() {
            return appInfo.clone();
        }
    }

    public AppInfo clone() {
        return new AppInfo(name, packageName, className, icon);
    }

    public boolean equals(Object obj) {
        return name.equals(((AppInfo) obj).name)
                && packageName.equals(((AppInfo) obj).packageName)
                && className.equals(((AppInfo) obj).className);
    }
}
