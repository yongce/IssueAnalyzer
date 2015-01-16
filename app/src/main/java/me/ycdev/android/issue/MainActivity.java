package me.ycdev.android.issue;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import me.ycdev.android.issue.logger.LoggerBase;
import me.ycdev.android.issue.logger.PowerLogger;
import me.ycdev.android.issue.logger.TrafficStatsLogger;
import me.ycdev.android.issue.logger.TrafficTagsLogger;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        eu.chainfire.libsuperuser.Debug.setDebug(true);

        Button powerLogBtn = (Button) findViewById(R.id.dump_power_log);
        powerLogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dumpPowerLog();
            }
        });

        Button trafficStatsLogBtn = (Button) findViewById(R.id.dump_traffic_stats_log);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            trafficStatsLogBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dumpTrafficStatsLog();
                }
            });
            trafficStatsLogBtn.setVisibility(View.VISIBLE);
        } else {
            trafficStatsLogBtn.setVisibility(View.GONE);
        }

        Button trafficTagsLogBtn = (Button) findViewById(R.id.dump_traffic_tags_log);
        trafficTagsLogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dumpTrafficTagsLog();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void dumpPowerLog() {
        new MyTask(getString(R.string.dump_log_ongoing), new PowerLogger(this)).execute();
    }

    private void dumpTrafficStatsLog() {
        new MyTask(getString(R.string.dump_log_ongoing), new TrafficStatsLogger(this)).execute();
    }

    private void dumpTrafficTagsLog() {
        new MyTask(getString(R.string.dump_log_ongoing), new TrafficTagsLogger(this)).execute();
    }

    private class MyTask extends AsyncTask<Void, Void, String> {
        private String mTips;
        private LoggerBase mLogger;
        private ProgressDialog mDialog;

        public MyTask(String tips, LoggerBase logger) {
            mTips = tips;
            mLogger = logger;
        }

        @Override
        protected void onPreExecute() {
            mDialog = new ProgressDialog(MainActivity.this);
            mDialog.setMessage(mTips);
            mDialog.setCancelable(false);
            mDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            return mLogger.dumpLog();
        }

        @Override
        protected void onPostExecute(String result) {
            String tips = getString(R.string.dump_toast_logs_dir, result);
            Toast.makeText(MainActivity.this, tips, Toast.LENGTH_LONG).show();
            mDialog.dismiss();
        }
    }
}
