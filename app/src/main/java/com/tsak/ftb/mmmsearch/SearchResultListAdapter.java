package com.tsak.ftb.mmmsearch;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tsak.ftb.mmmsearch.searcher.ThreadInfo;
import com.tsak.ftb.mmmsearch.utility.ImageUtility;
import com.tsak.ftb.mmmsearch.utility.NetUtility;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

class SearchResultListAdapter extends ArrayAdapter<ThreadInfo> {

    public enum COLUMN {
        URL,
        IMAGE,
        MAIL,
        TITLE,
        ALL,
        ;
    }

    private LayoutInflater layoutInflater;
    private List<ThreadInfo> threadInfoList;
    private ItemListener itemListener;
    private Map<ThreadInfo, Drawable> threadImages;
    private Map<URL, AtomicBoolean> threadImageReadingMap;

    SearchResultListAdapter(@NonNull Context context, ItemListener itemListener) {
        super(context, R.layout.result_list_item);

        this.layoutInflater = LayoutInflater.from(context);
        this.threadInfoList = new ArrayList<>();
        this.itemListener = itemListener;
        this.threadImages = new LinkedHashMap<>();
        this.threadImageReadingMap = new LinkedHashMap<>();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        convertView = layoutInflater.inflate(R.layout.result_list_item, parent,false);

        View.OnClickListener allClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemListener.onClickItem(v, COLUMN.ALL, threadInfoList.get(position));
            }
        };

        convertView.setOnClickListener(allClickListener);

        final TextView resultThreadURLTextView = convertView.findViewById(R.id.resultThreadURLTextView);
        resultThreadURLTextView.setText(threadInfoList.get(position).threadURL().toString());
        resultThreadURLTextView.setOnClickListener(allClickListener);
        resultThreadURLTextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                itemListener.onLongClickItem(v, COLUMN.URL, threadInfoList.get(position));
                return true;
            }
        });

        final URL imageURL = threadInfoList.get(position).imgUrl();
        final ImageView resultThreadImageView = convertView.findViewById(R.id.resultThreadImageView);
        if (threadImages.containsKey(threadInfoList.get(position))) {
            resultThreadImageView.setImageDrawable(threadImages.get(threadInfoList.get(position)));
        } else if (null != imageURL) {
            final Handler handler = new Handler();
            final View finalConvertView = convertView;
            if (!threadImageReadingMap.containsKey(imageURL)) {
                threadImageReadingMap.put(imageURL, new AtomicBoolean(false));
                notifyDataSetChanged();
            }
            try {
                if (!threadImageReadingMap.get(imageURL).get()) {
                    threadImageReadingMap.get(imageURL).set(true);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final Drawable drawable = NetUtility.readImage(imageURL);
                            if (null != drawable) {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        threadImages.put(threadInfoList.get(position), ImageUtility.resize(
                                                finalConvertView.getResources(), drawable, 50, 50));
                                        resultThreadImageView.setImageDrawable(threadImages.get(threadInfoList.get(position)));
                                        notifyDataSetChanged();
                                    }
                                });
                            }
                        }
                    }).start();
                }
            } catch (NullPointerException e) {}
        }
        resultThreadImageView.setOnClickListener(allClickListener);
        resultThreadImageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                itemListener.onLongClickItem(v, COLUMN.IMAGE, threadInfoList.get(position));
                return true;
            }
        });

        final TextView resultThreadMailTextView = convertView.findViewById(R.id.resultThreadMailTextView);
        resultThreadMailTextView.setText(threadInfoList.get(position).mail());
        resultThreadMailTextView.setOnClickListener(allClickListener);
        resultThreadMailTextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                itemListener.onLongClickItem(v, COLUMN.MAIL, threadInfoList.get(position));
                return true;
            }
        });

        final TextView resultThreadTitleTextView = convertView.findViewById(R.id.resultThreadTitleTextView);
        resultThreadTitleTextView.setText(String.valueOf(threadInfoList.get(position).titleEscapeHtml()));
        resultThreadTitleTextView.setOnClickListener(allClickListener);
        resultThreadTitleTextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                itemListener.onLongClickItem(v, COLUMN.TITLE, threadInfoList.get(position));
                return true;
            }
        });

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

    public interface ItemListener {
        void onClickItem(View v, COLUMN column, ThreadInfo threadInfo);
        void onLongClickItem(View v, COLUMN column, ThreadInfo threadInfo);
    }

}
