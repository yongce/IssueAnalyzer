package me.ycdev.android.issue;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.*;
import android.os.Process;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.IOException;

import eu.chainfire.libsuperuser.*;
import me.ycdev.android.issue.utils.Constants;
import me.ycdev.androidlib.utils.IoUtils;


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
        new MyTask(getString(R.string.dump_power_log_ongoing), new Runnable() {
            @Override
            public void run() {
                doDumpPowerLog();
            }
        }).execute();
    }

    private void doDumpPowerLog() {
        final String dataPowerDir = getDir(Constants.DATA_DIR_POWER, Context.MODE_PRIVATE).getAbsolutePath();
        final File sdRoot = Environment.getExternalStorageDirectory();
        final File sdcardPowerDir = new File(sdRoot, Constants.EXTERNAL_STORAGE_DIR_POWER);
        sdcardPowerDir.mkdirs();

        // first, save the info to /data
        String batteryStatsFile = new File(dataPowerDir, "batterystats").getAbsolutePath();
        String batteryInfoFile = new File(dataPowerDir, "batteryinfo").getAbsolutePath();
        String powerFile = new File(dataPowerDir, "power").getAbsolutePath();
        String deviceFile = new File(dataPowerDir, "device").getAbsolutePath();
        String serviceFile = new File(dataPowerDir, "service").getAbsolutePath();
        int myUid = Process.myUid();
        String[] cmds = new String[] {
                "service list > " + serviceFile,
                "chown " + myUid + ":" + myUid + " " + serviceFile,
                "dumpsys batterystats > " + batteryStatsFile,
                "chown " + myUid + ":" + myUid + " " + batteryStatsFile,
                "dumpsys batteryinfo > " + batteryInfoFile,
                "chown " + myUid + ":" + myUid + " " + batteryInfoFile,
                "dumpsys power > " + powerFile,
                "chown " + myUid + ":" + myUid + " " + powerFile,
                "getprop > " + deviceFile,
                "chown " + myUid + ":" + myUid + " " + deviceFile,
        };
        Shell.SU.run(cmds);

        // second, copy the files to /sdcard
        try {
            IoUtils.copyFile(batteryStatsFile, new File(sdcardPowerDir, "batterystats").getAbsolutePath());
            IoUtils.copyFile(batteryInfoFile, new File(sdcardPowerDir, "batteryinfo").getAbsolutePath());
            IoUtils.copyFile(powerFile, new File(sdcardPowerDir, "power").getAbsolutePath());
            IoUtils.copyFile(deviceFile, new File(sdcardPowerDir, "device").getAbsolutePath());
            IoUtils.copyFile(serviceFile, new File(sdcardPowerDir, "service").getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class MyTask extends AsyncTask<Void, Void, Void> {
        private String mTips;
        private Runnable mTargetTask;
        private ProgressDialog mDialog;

        public MyTask(String tips, Runnable task) {
            mTips = tips;
            mTargetTask = task;
        }

        @Override
        protected void onPreExecute() {
            mDialog = new ProgressDialog(MainActivity.this);
            mDialog.setMessage(mTips);
            mDialog.setCancelable(false);
            mDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            mTargetTask.run();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            mDialog.dismiss();
        }
    }
}
