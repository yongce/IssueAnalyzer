package me.ycdev.android.issue.logger;

import android.content.Context;

import java.io.File;
import java.io.IOException;

import eu.chainfire.libsuperuser.Shell;
import me.ycdev.android.issue.utils.Constants;
import me.ycdev.android.lib.common.utils.IoUtils;

/**
 * Dump traffic usage tags.
 * <p>Note: Need root permission.</p>
 */
public class TrafficTagsLogger extends LoggerBase {
    private static final String TRAFFIC_TAGS_STATS_FILE = "/proc/net/xt_qtaguid/stats";

    private static final String FILE_NAME_TAGS_STATS = "tags_stats";

    public TrafficTagsLogger(Context cxt) {
        super(cxt);
    }

    @Override
    protected String getLogsDirName() {
        return Constants.DIR_NAME_TRAFFIC_TAGS;
    }

    @Override
    protected void doDumpLog(File dataLogsDir, File sdcardLogsDir) {
        // first, save the info to /data
        String tagsStatsFile = new File(dataLogsDir, FILE_NAME_TAGS_STATS).getAbsolutePath();
        int myUid = android.os.Process.myUid();
        String[] cmds = new String[] {
                "cat " + TRAFFIC_TAGS_STATS_FILE + " > " + tagsStatsFile,
                "chown " + myUid + ":" + myUid + " " + tagsStatsFile,
        };
        Shell.SU.run(cmds);

        // second, copy the files to /sdcard
        try {
            IoUtils.copyFile(tagsStatsFile, new File(sdcardLogsDir, FILE_NAME_TAGS_STATS).getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
