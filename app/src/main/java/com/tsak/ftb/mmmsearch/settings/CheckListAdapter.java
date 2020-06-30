package com.tsak.ftb.mmmsearch.settings;

import android.content.Context;
import android.os.Handler;
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
import java.util.List;

class CheckListAdapter extends BaseAdapter {

    private LayoutInflater layoutInflater;
    private List<String> itemList;
    private List<Boolean> checkList;

    CheckListAdapter(@NonNull Context context, final List<String> itemList) {

        this.layoutInflater = LayoutInflater.from(context);
        this.checkList = new ArrayList<>();
        this.itemList = new ArrayList<String>() {{
            for (String item : itemList) {
                add(item);
                checkList.add(false);
            }
        }};
    }

    void setChecked(int position, boolean checked) {
        if (-1 < position && position < checkList.size()) {
            checkList.set(position, checked);
            final Handler handler = new Handler();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            notifyDataSetChanged();
                        }
                    });
                }
            }).start();
        }
    }

    boolean isChecked(int position) {
        if (-1 < position && position < checkList.size()) {
            return checkList.get(position);
        }
        return false;
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

        CheckBox generalItemCheckBox = convertView.findViewById(R.id.generalItemCheckBox);
        generalItemCheckBox.setChecked(checkList.get(position));
        generalItemCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setChecked(position, isChecked);
            }
        });
        ((TextView) convertView.findViewById(R.id.generalCheckItemTextView)).setText(itemList.get(position));

        return convertView;
    }
}
