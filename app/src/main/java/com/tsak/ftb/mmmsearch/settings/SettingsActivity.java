package com.tsak.ftb.mmmsearch.settings;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.tsak.ftb.mmmsearch.R;
import com.tsak.ftb.mmmsearch.settings.appselector.AppInfo;
import com.tsak.ftb.mmmsearch.settings.appselector.AppListAdapter;
import com.tsak.ftb.mmmsearch.settings.appselector.AppUtility;
import com.tsak.ftb.mmmsearch.settings.appselector.SpManager;

import java.util.ArrayList;
import java.util.Comparator;

import static com.tsak.ftb.mmmsearch.settings.appselector.AppUtility.UNSELECTED_APP;

public class SettingsActivity extends AppCompatActivity {
    private SpManager spManager;
    private ImageView openAppImageView;
    private TextView openAppNameTextView;
    private Thread collectThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        if (null != getSupportActionBar()) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        spManager = SpManager.newInstance(this);
        openAppImageView = findViewById(R.id.openAppImageView);
        openAppNameTextView = findViewById(R.id.openAppNameTextView);

        Button unSelectAppButton = findViewById(R.id.unSelectAppButton);
        unSelectAppButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSelectedApp("", "", "");
            }
        });
        setSelectedAppView(spManager.getString(SpManager.STRING_KEY.APP_NAME),
                spManager.getString(SpManager.STRING_KEY.PACKAGE_NAME));

        final Handler handler = new Handler();
        final ListView openAppListView = findViewById(R.id.openAppListView);
        openAppListView.setFastScrollEnabled(true);
        openAppListView.setFastScrollAlwaysVisible(false);
        final AppListAdapter appListAdapter = AppListAdapter.newInstance(
                getApplicationContext(), new ArrayList<AppInfo>(),
                new AppListAdapter.AppSelectionListener() {
                    @Override
                    public void onSelect(AppInfo appInfo) {
                        saveSelectedApp(appInfo.name(), appInfo.packageName(), appInfo.className());
                    }
                });
        openAppListView.setAdapter(appListAdapter);

        collectThread = new Thread(new Runnable() {
            @Override
            public void run() {AppUtility.collect(getApplicationContext(),
                    new AppUtility.OnCollectListener() {
                        @Override
                        public void onCollect(final AppInfo appInfo) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    appListAdapter.add(appInfo);
                                    appListAdapter.sort(new Comparator<AppInfo>() {
                                        @Override
                                        public int compare(AppInfo o1, AppInfo o2) {
                                            return o1.name().compareToIgnoreCase(o2.name());
                                        }
                                    });
                                    appListAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    });
            }
        });
        collectThread.start();
    }

    private void saveSelectedApp(String appName, String packageName, String className) {

        spManager.putString(SpManager.STRING_KEY.APP_NAME, appName);
        spManager.putString(SpManager.STRING_KEY.PACKAGE_NAME, packageName);
        spManager.putString(SpManager.STRING_KEY.CLASS_NAME, className);

        setSelectedAppView(appName, packageName);

    }

    private void setSelectedAppView(String appName, String packageName) {

        if ("".equals(appName) && "".equals(packageName)) {
            openAppImageView.setImageDrawable(null);
            openAppNameTextView.setText(UNSELECTED_APP);
            return;
        }
        try {
            openAppImageView.setImageDrawable(AppUtility.getApplicationIcon(this, packageName));
        } catch (PackageManager.NameNotFoundException e) {
        }
        openAppNameTextView.setText(appName);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (android.R.id.home == id) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        collectThread.interrupt();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onRestart() {
        super.onRestart();
    }
}
