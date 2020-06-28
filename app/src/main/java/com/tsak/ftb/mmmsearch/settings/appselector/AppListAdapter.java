package com.tsak.ftb.mmmsearch.settings.appselector;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tsak.ftb.mmmsearch.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AppListAdapter extends ArrayAdapter<AppInfo> {
    private LayoutInflater layoutInflater;
    private List<AppInfo> appList;
    private int selectedIndex = -1;

    public AppListAdapter(Context context, final List<AppInfo> appList) {
        super(context, R.layout.app_list_item);

        this.layoutInflater = LayoutInflater.from(context);
        this.appList = new ArrayList<AppInfo>(){{
            for (AppInfo appInfo : appList) {
                add(appInfo);
            }
        }};
    }

    @Override
    public void add(@Nullable AppInfo object) {
        super.add(object);
        appList.add(object);
    }

    @Override
    public void addAll(AppInfo... items) {
        super.addAll(items);
        appList.addAll(Arrays.asList(items));
    }

    @Override
    public void clear() {
        super.clear();
        appList.clear();
        selectedIndex = -1;
    }

    @Override
    public void remove(@Nullable AppInfo object) {
        super.remove(object);
        appList.remove(object);
    }

    @Override
    public void addAll(@NonNull Collection<? extends AppInfo> collection) {
        super.addAll(collection);
        appList.addAll(collection);
    }

    @Override
    public void insert(@Nullable AppInfo object, int index) {
        super.insert(object, index);
        appList.add(index, object);
    }

    @Override
    public int getCount() {
        return appList.size();
    }

    @Override
    public AppInfo getItem(int position) {
        return appList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void sort(@NonNull Comparator<? super AppInfo> comparator) {
        super.sort(comparator);
        Collections.sort(appList, comparator);
    }

    public void setChecked(int position) {
        selectedIndex = position;
        notifyDataSetChanged();
    }

    public void setChecked(String appName, String packageName, String className) {
        selectedIndex = appList.indexOf(new AppInfo.AppInfoBuilder(appName, packageName, className).build());
        notifyDataSetChanged();
    }

    public AppInfo getCheckedItem() {
        if (-1 < selectedIndex && selectedIndex <  appList.size()) {
            return appList.get(selectedIndex);
        }
        return new AppInfo.AppInfoBuilder(null, null, null).build();
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        convertView = layoutInflater.inflate(R.layout.app_list_item, parent,false);

        final String labelName = appList.get(position).name();
        final Drawable appIcon = appList.get(position).icon();

        ((TextView) convertView.findViewById(R.id.appName)).setText(labelName);
        ((ImageView) convertView.findViewById(R.id.appIcon)).setImageDrawable(appIcon);
        ((RadioButton) convertView.findViewById(R.id.appRadioButton)).setChecked(position == selectedIndex);

        return convertView;
    }
}
