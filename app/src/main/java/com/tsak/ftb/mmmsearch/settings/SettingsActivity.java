package com.tsak.ftb.mmmsearch.settings;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.tsak.ftb.mmmsearch.utility.StringUtility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.tsak.ftb.mmmsearch.settings.appselector.AppUtility.UNSELECTED_APP;

public class SettingsActivity extends AppCompatActivity {

    public final static String SETTINGS_CHANGED_KEY = "SETTINGS_CHANGED_KEY";

    private final static int MAX_VIEW_APP_NAME_BYTE = 12;

    private enum OPEN_FLAG {
        FLAG_ACTIVITY_BROUGHT_TO_FRONT(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT),
        FLAG_ACTIVITY_CLEAR_TASK(Intent.FLAG_ACTIVITY_CLEAR_TASK),
        FLAG_ACTIVITY_CLEAR_TOP(Intent.FLAG_ACTIVITY_CLEAR_TOP),
        FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS),
        FLAG_ACTIVITY_FORWARD_RESULT(Intent.FLAG_ACTIVITY_FORWARD_RESULT),
        FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY),
        FLAG_ACTIVITY_MULTIPLE_TASK(Intent.FLAG_ACTIVITY_MULTIPLE_TASK),
        FLAG_ACTIVITY_NEW_TASK(Intent.FLAG_ACTIVITY_NEW_TASK),
        FLAG_ACTIVITY_NO_ANIMATION(Intent.FLAG_ACTIVITY_NO_ANIMATION),
        FLAG_ACTIVITY_NO_HISTORY(Intent.FLAG_ACTIVITY_NO_HISTORY),
        FLAG_ACTIVITY_NO_USER_ACTION(Intent.FLAG_ACTIVITY_NO_USER_ACTION),
        FLAG_ACTIVITY_PREVIOUS_IS_TOP(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP),
        FLAG_ACTIVITY_REORDER_TO_FRONT(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT),
        FLAG_ACTIVITY_RESET_TASK_IF_NEEDED(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED),
        FLAG_ACTIVITY_SINGLE_TOP(Intent.FLAG_ACTIVITY_SINGLE_TOP),
        FLAG_ACTIVITY_TASK_ON_HOME(Intent.FLAG_ACTIVITY_TASK_ON_HOME),
        ;

        private int value;

        OPEN_FLAG(int value) {
            this.value = value;
        }

        public static OPEN_FLAG findFlag(String target) {

            for (OPEN_FLAG key : OPEN_FLAG.values()) {
                if (key.name().equals(target.toUpperCase())) {
                    return key;
                }
            }
            return null;
        }

        private static boolean isDefaultValue(String target) {
            OPEN_FLAG targetFlag = OPEN_FLAG.findFlag(target);
            if (null == targetFlag) {
                return false;
            }
            return 0 != (SpManager.INT_KEY.OPEN_APP_FLAG.defaultValue() & targetFlag.value);
        }

        static boolean isFlagEnabled(String target, int flag) {
            OPEN_FLAG targetFlag = OPEN_FLAG.findFlag(target);
            if (null == targetFlag) {
                return false;
            }
            return (targetFlag.value & flag) > 0;
        }

        private static List<String> NAME_LIST = Collections.unmodifiableList(new ArrayList<String>() {{
            for (OPEN_FLAG flag :OPEN_FLAG.values()) {
                add(flag.name());
            }
        }});

        static String[] NAMES = OPEN_FLAG.NAME_LIST.toArray(new String[OPEN_FLAG.values().length]);
    }

    public enum BOARD {
        may("may.2chan.net/b"),
        img("img.2chan.net/b"),
        V("dec.2chan.net/73"),
        ;

        private String value;

        public String value() {
            return value;
        }

        BOARD(String value) {
            this.value = value;
        }

        public static BOARD findBoard(String target) {

            for (BOARD board : BOARD.values()) {
                if (board.name().equals(target)) {
                    return board;
                }
            }
            return may;
        }

        static String[] NAMES = Collections.unmodifiableList(new ArrayList<String>() {{
            for (BOARD board :BOARD.values()) {
                add(board.name());
            }
        }}).toArray(new String[BOARD.values().length]);
    }

    private int protocolIndex = 0;
    private int boardIndex = 0;
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

        final AtomicBoolean isFinishCollect = new AtomicBoolean(false);

        final View chooseAppView = LayoutInflater.from(this).inflate(R.layout.list_for_dialog, null);
        ListView chooseAppListView = chooseAppView.findViewById(R.id.forDialogListView);
        chooseAppListView.setFastScrollEnabled(true);
        chooseAppListView.setFastScrollAlwaysVisible(false);
        final AppListAdapter appListAdapter = new AppListAdapter(this, new ArrayList<AppInfo>());
        chooseAppListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        chooseAppListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (isFinishCollect.get()) {
                    appListAdapter.setChecked(position);
                }
            }
        });
        chooseAppListView.setAdapter(appListAdapter);

        final AlertDialog chooseAppDialog = new AlertDialog.Builder(this)
                .setTitle("Select Open App")
                .setView(chooseAppView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (isFinishCollect.get()) {
                            saveSelectedApp(appListAdapter.getCheckedItem());
                        }
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

        Button chooseAppButton = findViewById(R.id.chooseAppButton);
        chooseAppButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Handler handler = new Handler();
                chooseAppDialog.show();
                if (!isFinishCollect.get()) {
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
                                                    if (appListAdapter.addNonDup(appInfo)) {
                                                        appListAdapter.sort(appSorter);
                                                        appListAdapter.notifyDataSetChanged();
                                                    }
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
                final AtomicInteger index = new AtomicInteger(protocolIndex);
                new AlertDialog.Builder(SettingsActivity.this)
                        .setTitle("Change Word")
                        .setSingleChoiceItems(NetUtility.PROTOCOL.NAMES, protocolIndex, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                index.set(which);
                            }
                        })
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (-1 < index.get() && index.get() < NetUtility.PROTOCOL.NAMES.length) {
                                    protocolIndex = index.get();
                                    spManager.putString(SpManager.STRING_KEY.OPEN_PROTOCOL, NetUtility.PROTOCOL.NAMES[protocolIndex]);
                                    protocolTextView.setText(NetUtility.PROTOCOL.NAMES[protocolIndex]);
                                    isChanged = true;
                                }
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        final AtomicInteger openFlag = new AtomicInteger(spManager.getInt(SpManager.INT_KEY.OPEN_APP_FLAG));
        final TextView openFlagTextView = findViewById(R.id.openFlagTextView);
        openFlagTextView.setText(StringUtility.toHexString(openFlag.get()));

        final CheckListAdapter checkListAdapter = new CheckListAdapter(this, Arrays.asList(OPEN_FLAG.NAMES));
        final View openFlagView = LayoutInflater.from(this).inflate(R.layout.list_for_dialog, null);
        final ListView openFlagListView = openFlagView.findViewById(R.id.forDialogListView);
        openFlagListView.setAdapter(checkListAdapter);
        openFlagListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                checkListAdapter.setChecked(position, !checkListAdapter.isChecked(position));
            }
        });
        final AlertDialog openFlagDialog = new AlertDialog.Builder(this)
                .setTitle("Select Open Flag")
                .setView(openFlagView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int newOpenFlag = openFlag.get();
                        for (int i = 0; i < checkListAdapter.getCount(); i++) {
                            OPEN_FLAG _openFlag = OPEN_FLAG.findFlag(checkListAdapter.getItem(i));
                            if (null != _openFlag) {
                                if (checkListAdapter.isChecked(i)) {
                                    newOpenFlag |= _openFlag.value;
                                } else {
                                    newOpenFlag &= ~_openFlag.value;
                                }
                            }
                        }
                        if (0 != (openFlag.get() ^ newOpenFlag)) {
                            isChanged = true;
                            openFlag.set(newOpenFlag);
                            spManager.putInt(SpManager.INT_KEY.OPEN_APP_FLAG, openFlag.get());
                            openFlagTextView.setText(StringUtility.toHexString(openFlag.get()));
                        }
                    }
                })
                .setNeutralButton("Default", null)
                .setNegativeButton("Cancel", null)
                .create();
        openFlagDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEUTRAL)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                for (int i = 0; i < checkListAdapter.getCount(); i++) {
                                    checkListAdapter.setChecked(i, OPEN_FLAG.isDefaultValue(checkListAdapter.getItem(i)));
                                }
                            }
                        });
            }
        });

        Button openFlagButton = findViewById(R.id.openFlagButton);
        openFlagButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < checkListAdapter.getCount(); i++) {
                    checkListAdapter.setChecked(i, OPEN_FLAG.isFlagEnabled(checkListAdapter.getItem(i), openFlag.get()));
                }
                openFlagDialog.show();
            }
        });

        final TextView openBoardTextView = findViewById(R.id.openBoardTextView);
        BOARD board = BOARD.findBoard(spManager.getString(SpManager.STRING_KEY.SEARCH_BOARD));
        openBoardTextView.setText(board.name());
        boardIndex = board.ordinal();

        Button chooseBoardButton = findViewById(R.id.chooseBoardButton);
        chooseBoardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AtomicInteger index = new AtomicInteger(boardIndex);
                new AlertDialog.Builder(SettingsActivity.this)
                        .setTitle("Change Board")
                        .setSingleChoiceItems(BOARD.NAMES, boardIndex, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                index.set(which);
                            }
                        })
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (-1 < index.get() && index.get() < BOARD.NAMES.length) {
                                    boardIndex = index.get();
                                    spManager.putString(SpManager.STRING_KEY.SEARCH_BOARD, BOARD.NAMES[boardIndex]);
                                    openBoardTextView.setText(BOARD.NAMES[boardIndex]);
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
        openAppNameTextView.setText(StringUtility.substringByByteLen(appName, MAX_VIEW_APP_NAME_BYTE));
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
