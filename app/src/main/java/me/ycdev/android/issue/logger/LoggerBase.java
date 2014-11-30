package me.ycdev.android.issue.logger;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.IOException;

import eu.chainfire.libsuperuser.Shell;
import me.ycdev.android.issue.utils.Constants;
import me.ycdev.android.lib.common.utils.IoUtils;

public abstract class LoggerBase {
    private static final String FILE_NAME_SYS_PROPERTY = "sysprop";
    private static final String FILE_NAME_SERVICE = "service";
    private static final String FILE_NAME_PACKAGES = "packages";
    private static final String FILE_NAME_PROCESSES = "processes";

    protected Context mAppContext;

    public LoggerBase(Context cxt) {
        mAppContext = cxt.getApplicationContext();
    }

    final public void dumpLog() {
        // create dirs if needed
        String logsDirName = getLogsDirName();
        File dataLogsDir = mAppContext.getDir(logsDirName, Context.MODE_PRIVATE);

        File sdRoot = Environment.getExternalStorageDirectory();
        File sdcardAppDir = new File(sdRoot, Constants.EXTERNAL_STORAGE_DIR_APP_ROOT);
        File sdcardLogsDir = new File(sdcardAppDir, logsDirName);
        sdcardLogsDir.mkdirs();

        /*
         * Dump common info
         */
        // first, dump info to /data
        String propertyFile = new File(dataLogsDir, FILE_NAME_SYS_PROPERTY).getAbsolutePath();
        String serviceFile = new File(dataLogsDir, FILE_NAME_SERVICE).getAbsolutePath();
        String packagesFile = new File(dataLogsDir, FILE_NAME_PACKAGES).getAbsolutePath();
        String processesFile = new File(dataLogsDir, FILE_NAME_PROCESSES).getAbsolutePath();
        String[] cmds = new String[] {
                "getprop > " + propertyFile,
                "service list > " + serviceFile,
                "pm list packages > " + packagesFile,
                "ps > " + processesFile,
        };
        Shell.SH.run(cmds);

        // second, copy the files to /sdcard
        try {
            IoUtils.copyFile(propertyFile, new File(sdcardLogsDir, FILE_NAME_SYS_PROPERTY).getAbsolutePath());
            IoUtils.copyFile(serviceFile, new File(sdcardLogsDir, FILE_NAME_SERVICE).getAbsolutePath());
            IoUtils.copyFile(packagesFile, new File(sdcardLogsDir, FILE_NAME_PACKAGES).getAbsolutePath());
            IoUtils.copyFile(processesFile, new File(sdcardLogsDir, FILE_NAME_PROCESSES).getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // dump specific logs
        doDumpLog(dataLogsDir, sdcardLogsDir);
    }

    protected abstract String getLogsDirName();

    protected abstract void doDumpLog(File dataLogsDir, File sdcardLogsDir);
}
