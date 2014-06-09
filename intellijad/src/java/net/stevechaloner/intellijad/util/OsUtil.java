package net.stevechaloner.intellijad.util;

import java.io.File;

public enum OsUtil {
    instance;

    public static File getTempDir() {
        return new File(getTempDirPath());
    }

    public static String getTempDirPath() {
        return System.getProperty("java.io.tmpdir");
    }
}
