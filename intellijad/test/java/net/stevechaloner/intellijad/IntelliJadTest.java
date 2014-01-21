/* 
 * @(#) $Id:  $
 */
package net.stevechaloner.intellijad;

import java.io.File;

import com.google.common.base.Preconditions;

/**
 * <p></p>
 * <br/>
 * <p>Created on 21.01.14</p>
 *
 * @author Lukasz Zielinski
 */
public enum IntelliJadTest {
    instance;
    
    private static File checkDir(File dir) {
        Preconditions.checkState(dir.exists());
        Preconditions.checkState(dir.isDirectory());        
        return dir;
    }
    
    public static File getTestDataDir() {
        File dir = new File(".", "intellijad" + File.separator + "testdata").getAbsoluteFile();
        checkDir(dir);
        return dir;
    }
    
    public static File getTestLibDir() {
        File dir = new File(getTestDataDir(), "lib");
        checkDir(dir);        
        return dir;
    }
    
    public static String getJarLibPath(String libName) {
        return getTestLibDir().getAbsolutePath() + File.separator + libName + "!/";        
    }
}
