package com.tsak.ftb.mmmsearch.settings;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.tsak.ftb.mmmsearch.R;
import com.tsak.ftb.mmmsearch.settings.appselector.AppInfo;
import com.tsak.ftb.mmmsearch.settings.appselector.AppListAdapter;
import com.tsak.ftb.mmmsearch.settings.appselector.AppUtility;
import com.tsak.ftb.mmmsearch.utility.NetUtility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.tsak.ftb.mmmsearch.settings.appselector.AppUtility.UNSELECTED_APP;

public class SettingsActivity extends AppCompatActivity {

    public final static String SETTINGS_CHANGED_KEY = "SETTINGS_CHANGED_KEY";

    private final static String[] PROTOCOL_LIST = Collections.unmodifiableList(new ArrayList<String>() {{
        for (NetUtility.PROTOCOL protocol :NetUtility.PROTOCOL.values()) {
            add(protocol.name());
        }
    }}).toArray(new String[NetUtility.PROTOCOL.values().length]);

    private int protocolIndex = 0;
    private SpManager spManager;
    private ImageView openAppImageView;
    private TextView openAppNameTextView;
    private Button unSelectAppButton;
    private Thread collectThread;

    private boolean isChanged = false;

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


        unSelectAppButton = findViewById(R.id.unSelectAppButton);
        unSelectAppButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSelectedApp("", "", "");
            }
        });

        setSelectedAppView(spManager.getString(SpManager.STRING_KEY.APP_NAME),
                spManager.getString(SpManager.STRING_KEY.PACKAGE_NAME));

        final View chooseAppView = LayoutInflater.from(this).inflate(R.layout.list_for_dialog, null);
        final ListView chooseAppListView = chooseAppView.findViewById(R.id.forDialogListView);
        chooseAppListView.setFastScrollEnabled(true);
        chooseAppListView.setFastScrollAlwaysVisible(false);
        final AppListAdapter appListAdapter = new AppListAdapter(this, new ArrayList<AppInfo>());
        chooseAppListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        chooseAppListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                appListAdapter.setChecked(position);
            }
        });
        chooseAppListView.setAdapter(appListAdapter);
        chooseAppListView.setEnabled(false);

        final AlertDialog chooseAppDialog = new AlertDialog.Builder(this)
                .setTitle("Select Open App")
                .setView(chooseAppView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveSelectedApp(appListAdapter.getCheckedItem());
                    }
                })
                .setNegativeButton("Cancel", null)
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        if (collectThread.isAlive()) {
                            collectThread.interrupt();
                        }
                    }
                })
                .create();

        final AtomicBoolean isFinishCollect = new AtomicBoolean(false);
        Button chooseAppButton = findViewById(R.id.chooseAppButton);
        chooseAppButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Handler handler = new Handler();
                chooseAppDialog.show();
                if (!isFinishCollect.get()) {
                    appListAdapter.clear();
                    collectThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final Comparator<AppInfo> appSorter = new Comparator<AppInfo>() {
                                @Override
                                public int compare(AppInfo o1, AppInfo o2) {
                                    return o1.name().compareToIgnoreCase(o2.name());
                                }
                            };
                            AppUtility.collect(getApplicationContext(),
                                    new AppUtility.OnCollectListener() {
                                        @Override
                                        public void onCollect(final AppInfo appInfo) {
                                            handler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    appListAdapter.add(appInfo);
                                                    appListAdapter.sort(appSorter);
                                                    appListAdapter.notifyDataSetChanged();
                                                }
                                            });
                                        }

                                        @Override
                                        public void finish() {
                                            isFinishCollect.set(true);
                                            handler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    appListAdapter.setChecked(spManager.getString(SpManager.STRING_KEY.APP_NAME),
                                                            spManager.getString(SpManager.STRING_KEY.PACKAGE_NAME),
                                                            spManager.getString(SpManager.STRING_KEY.CLASS_NAME));
                                                    chooseAppListView.setEnabled(true);
                                                }
                                            });
                                        }
                                    });
                        }
                    });
                    collectThread.start();
                } else {
                    appListAdapter.setChecked(spManager.getString(SpManager.STRING_KEY.APP_NAME),
                            spManager.getString(SpManager.STRING_KEY.PACKAGE_NAME),
                            spManager.getString(SpManager.STRING_KEY.CLASS_NAME));
                }
            }
        });

        final TextView protocolTextView = findViewById(R.id.protocolTextView);
        NetUtility.PROTOCOL openProtocol = NetUtility.PROTOCOL.findProtocol(spManager.getString(SpManager.STRING_KEY.OPEN_PROTOCOL));
        protocolTextView.setText(openProtocol.name());
        protocolIndex = openProtocol.ordinal();

        Button chooseProtocolButton = findViewById(R.id.chooseProtocolButton);
        chooseProtocolButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(SettingsActivity.this)
                        .setTitle("Change Word")
                        .setSingleChoiceItems(PROTOCOL_LIST, protocolIndex, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                protocolIndex = which;
                            }
                        })
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (-1 < protocolIndex && protocolIndex < PROTOCOL_LIST.length) {
                                    spManager.putString(SpManager.STRING_KEY.OPEN_PROTOCOL, PROTOCOL_LIST[protocolIndex]);
                                    protocolTextView.setText(PROTOCOL_LIST[protocolIndex]);
                                    isChanged = true;
                                }
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });
    }

    private void saveSelectedApp(AppInfo appInfo) {
        saveSelectedApp(appInfo.name(), appInfo.packageName(), appInfo.className());
    }

    private void saveSelectedApp(String appName, String packageName, String className) {

        spManager.putString(SpManager.STRING_KEY.APP_NAME, appName);
        spManager.putString(SpManager.STRING_KEY.PACKAGE_NAME, packageName);
        spManager.putString(SpManager.STRING_KEY.CLASS_NAME, className);

        setSelectedAppView(appName, packageName);
        isChanged = true;
    }

    private void setSelectedAppView(String appName, String packageName) {

        if ("".equals(appName) && "".equals(packageName)) {
            openAppImageView.setImageDrawable(null);
            openAppNameTextView.setText(UNSELECTED_APP);
            unSelectAppButton.setEnabled(false);
            return;
        }
        try {
            openAppImageView.setImageDrawable(AppUtility.getApplicationIcon(this, packageName));
        } catch (PackageManager.NameNotFoundException e) {
        }
        openAppNameTextView.setText(appName);
        unSelectAppButton.setEnabled(true);
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
        setIntent(getIntent().putExtra(SETTINGS_CHANGED_KEY, isChanged));
        setResult(RESULT_OK, getIntent());
        finish();
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
