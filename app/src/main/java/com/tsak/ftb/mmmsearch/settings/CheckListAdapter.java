package com.tsak.ftb.mmmsearch.settings;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.tsak.ftb.mmmsearch.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class CheckListAdapter extends BaseAdapter {

    private LayoutInflater layoutInflater;
    private List<String> itemList;
    private List<Boolean> checkList;
    private Map<String, CheckBox> checkBoxMap;

    CheckListAdapter(@NonNull Context context, Map<String, Boolean> itemMap) {

        this.layoutInflater = LayoutInflater.from(context);
        this.itemList = Collections.unmodifiableList(new ArrayList<>(itemMap.keySet()));
        this.checkList = new ArrayList<>(itemMap.values());
        this.checkBoxMap = new LinkedHashMap<>();
    }

    void setChecked(String item, boolean checked) {

        int index = itemList.indexOf(item);
        if (-1 != index && checkBoxMap.containsKey(item)) {
            try {
                checkBoxMap.get(item).setChecked(checked);
                checkList.set(index, checked);
            } catch (NullPointerException e) {}
        }
    }

    boolean isChecked(int position) {
        return checkList.get(position);
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public String getItem(int position) {
        return itemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        convertView = layoutInflater.inflate(R.layout.check_list_item, parent,false);

        final CheckBox generalItemCheckBox = convertView.findViewById(R.id.generalItemCheckBox);
        generalItemCheckBox.setChecked(checkList.get(position));
        generalItemCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checkList.set(position, isChecked);
            }
        });
        TextView generalCheckItemTextView = convertView.findViewById(R.id.generalCheckItemTextView);
        generalCheckItemTextView.setText(itemList.get(position));
        generalCheckItemTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generalItemCheckBox.setChecked(!generalItemCheckBox.isChecked());
            }
        });
        checkBoxMap.put(itemList.get(position), generalItemCheckBox);
        return convertView;
    }
}
