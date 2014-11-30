package me.ycdev.android.issue.logger;

import android.content.Context;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import me.ycdev.android.issue.utils.Constants;
import me.ycdev.android.lib.common.utils.IoUtils;

public class TrafficStatsLogger extends LoggerBase {
    private static final String DEV_FILE = "/proc/self/net/dev";
    private static final String UIDS_DIR = "/proc/uid_stat/";

    public TrafficStatsLogger(Context cxt) {
        super(cxt);
    }

    @Override
    protected String getLogsDirName() {
        return Constants.DIR_NANE_TRAFFIC_STATS;
    }

    @Override
    protected void doDumpLog(File dataLogsDir, File sdcardLogsDir) {
        // dump "/proc/self/net/dev"
        try {
            File devDumpFile = new File(sdcardLogsDir, "dev_file");
            IoUtils.copyFile(DEV_FILE, devDumpFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // dump "/proc/uid_stat/"
        File uidDumpFile = new File(sdcardLogsDir, "uid_file");
        FileWriter writer = null;
        try {
            writer = new FileWriter(uidDumpFile);
            File uidsDir = new File(UIDS_DIR);
            writer.write(UIDS_DIR + " exist? " + uidsDir.exists() + "\n");
            if (uidsDir.exists()) {
                File[] uidsList = uidsDir.listFiles();
                writer.write("uids list: " + (uidsList != null ? uidsList.length : -1) + ", ");
                if (uidsList != null && uidsList.length > 0) {
                    for (File uidFile : uidsList) {
                        writer.write(uidFile.getName() + ",");
                    }
                    writer.append("\n");

                    File firstUidDir = uidsList[0];
                    File[] trafficList = firstUidDir.listFiles();
                    writer.write("traffic list: " + (trafficList != null ? trafficList.length : -1) + "\n");
                    if (trafficList != null && trafficList.length > 0) {
                        for (File trafficFile : trafficList) {
                            writer.write("traffic type: " + trafficFile.getName()
                                    + ", content: " + IoUtils.readAllLines(trafficFile.getAbsolutePath())
                                    + "#\n");
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IoUtils.closeQuietly(writer);
        }
    }
}
