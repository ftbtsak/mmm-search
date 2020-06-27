package com.tsak.ftb.mmmsearch;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tsak.ftb.mmmsearch.searcher.ThreadInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

class SearchResultListAdapter extends ArrayAdapter<ThreadInfo> {

    private LayoutInflater layoutInflater;
    private List<ThreadInfo> threadInfoList;

    SearchResultListAdapter(@NonNull Context context) {
        super(context, R.layout.result_list_item);

        this.layoutInflater = LayoutInflater.from(context);
        this.threadInfoList = new ArrayList<>();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = layoutInflater.inflate(R.layout.result_list_item, parent,false);

        TextView resultThreadURLTextView = convertView.findViewById(R.id.resultThreadURLTextView);
        resultThreadURLTextView.setText(threadInfoList.get(position).threadURL().toString());

        TextView resultThreadTitleTextView = convertView.findViewById(R.id.resultThreadTitleTextView);
        resultThreadTitleTextView.setText(String.valueOf(threadInfoList.get(position).titleEscapeHtml()));

        return convertView;
    }

    @Override
    public void add(@Nullable ThreadInfo object) {
        super.add(object);
        threadInfoList.add(object);
    }
    @Override
    public void addAll(ThreadInfo... items) {
        super.addAll(items);
        threadInfoList.addAll(Arrays.asList(items));
    }

    @Override
    public void clear() {
        super.clear();
        threadInfoList.clear();
    }

    @Override
    public void remove(@Nullable ThreadInfo object) {
        super.remove(object);
        threadInfoList.remove(object);
    }

    @Override
    public void addAll(@NonNull Collection<? extends ThreadInfo> collection) {
        super.addAll(collection);
        threadInfoList.addAll(collection);
    }

    @Override
    public void insert(@Nullable ThreadInfo object, int index) {
        super.insert(object, index);
        threadInfoList.add(index, object);
    }

    @Override
    public int getCount() {
        return threadInfoList.size();
    }

    @Override
    public ThreadInfo getItem(int position) {
        return threadInfoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

}
