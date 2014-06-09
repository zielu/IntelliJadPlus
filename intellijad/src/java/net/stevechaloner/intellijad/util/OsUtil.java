package net.stevechaloner.intellijad.util;

import java.io.File;

public enum OsUtil {
    instance;

    public static File tempDir() {
        return new File(tempDirPath());
    }

    public static String tempDirPath() {
        return System.getProperty("java.io.tmpdir");
    }

    public static String lineSeparator() {
        return System.getProperty("line.separator");
    }
}
