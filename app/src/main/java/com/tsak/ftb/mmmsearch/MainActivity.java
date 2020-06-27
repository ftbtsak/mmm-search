package com.tsak.ftb.mmmsearch;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tsak.ftb.mmmsearch.searcher.ThreadInfo;
import com.tsak.ftb.mmmsearch.searcher.ThreadSearcher;
import com.tsak.ftb.mmmsearch.settings.SettingsActivity;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity {

    private final static int SETTINGS_REQ_CODE = 810;
    private final String[] TARGET_WORDS = {"めめめ", "MMM"};
    private final List<String> boards = Collections.unmodifiableList(
            Collections.singletonList("may.2chan.net/b"));

    private int wordIndex = 0;
    private AtomicBoolean isSearching = new AtomicBoolean(false);

    private TextView targetWordTextView;
    private Button changeWordButton;
    private Button searchButton;
    private ListView searchResultListView;
    private SearchResultListAdapter searchResultListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        targetWordTextView = findViewById(R.id.targetWordTextView);
        targetWordTextView.setText(TARGET_WORDS[wordIndex]);

        changeWordButton = findViewById(R.id.changeWordButton);
        changeWordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Change Word")
                        .setSingleChoiceItems(TARGET_WORDS, wordIndex, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                wordIndex = which;
                            }
                        })
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (-1 < wordIndex && wordIndex < TARGET_WORDS.length) {
                                    targetWordTextView.setText(TARGET_WORDS[wordIndex]);
                                }
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Handler handler = new Handler();
                changeSearchState(handler, true);
                new Thread() {
                    @Override
                    public void run() {
                        Thread searcherThread = new Thread() {
                            @Override
                            public void run() {
                                ThreadSearcher.newInstance(boards, new ThreadSearcher.ThreadSearcherCallback() {
                                    @Override
                                    public void notify(final ThreadInfo threadInfo) {
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                searchResultListAdapter.add(threadInfo);
                                                searchResultListAdapter.notifyDataSetChanged();
                                            }
                                        });
                                    }
                                }).find(TARGET_WORDS[wordIndex]);
                            }
                        };
                        searcherThread.start();
                        try {
                            searcherThread.join();
                        } catch (InterruptedException e) {}
                        changeSearchState(handler, false);
                    }
                }.start();
            }
        });

        searchResultListView = findViewById(R.id.searchResultListView);
        searchResultListAdapter = new SearchResultListAdapter(this);
        searchResultListView.setAdapter(searchResultListAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        super.onPrepareOptionsMenu(menu);

        menu.findItem(R.id.settings_menu).setEnabled(!isSearching.get());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch(item.getItemId()) {
            case R.id.settings_menu:
                startActivityForResult(new Intent(this, SettingsActivity.class), SETTINGS_REQ_CODE);
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            case SETTINGS_REQ_CODE:
                break;
            default:
                break;
        }
    }

    private void changeSearchState(Handler handler, final boolean isSearching) {

        if (this.isSearching.get() == isSearching) {
            return;
        }
        this.isSearching.set(isSearching);
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isSearching) {
                    searchResultListAdapter.clear();
                } else {
                    String finishMsg = "Search finished.";
                    Toast.makeText(getApplicationContext(), finishMsg, Toast.LENGTH_LONG).show();
                }
                changeWordButton.setEnabled(!isSearching);
                searchButton.setEnabled(!isSearching);
            }
        });
    }
}
