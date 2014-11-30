package me.ycdev.android.issue.logger;

import android.content.Context;
import android.os.*;

import java.io.File;
import java.io.IOException;

import eu.chainfire.libsuperuser.Shell;
import me.ycdev.android.issue.utils.Constants;
import me.ycdev.android.lib.common.utils.IoUtils;

/**
 * Dump power consumption stats.
 * <p>Note: Need root permission.</p>
 */
public class PowerLogger extends LoggerBase {
    private static final String FILE_NAME_BATTERY = "battery";
    private static final String FILE_NAME_POWER = "power";

    public PowerLogger(Context cxt) {
        super(cxt);
    }

    @Override
    protected String getLogsDirName() {
        return Constants.DIR_NAME_POWER;
    }

    @Override
    protected void doDumpLog(File dataLogsDir, File sdcardLogsDir) {
        // first, save the info to /data
        String batteryService = "batteryinfo";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            batteryService = "batterystats";
        }

        String batteryStatsFile = new File(dataLogsDir, FILE_NAME_BATTERY).getAbsolutePath();
        String powerFile = new File(dataLogsDir, FILE_NAME_POWER).getAbsolutePath();
        int myUid = android.os.Process.myUid();
        String[] cmds = new String[] {
                "dumpsys " + batteryService + " > " + batteryStatsFile,
                "chown " + myUid + ":" + myUid + " " + batteryStatsFile,
                "dumpsys power > " + powerFile,
                "chown " + myUid + ":" + myUid + " " + powerFile,
        };
        Shell.SU.run(cmds);

        // second, copy the files to /sdcard
        try {
            IoUtils.copyFile(batteryStatsFile, new File(sdcardLogsDir, FILE_NAME_BATTERY).getAbsolutePath());
            IoUtils.copyFile(powerFile, new File(sdcardLogsDir, FILE_NAME_POWER).getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
