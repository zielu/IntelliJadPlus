/* 
 * @(#) $Id:  $
 */
package net.stevechaloner.intellijad.util;

import java.io.File;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import net.stevechaloner.intellijad.config.Config;

/**
 * <p></p>
 * <br/>
 * <p>Created on 18.01.12.</p>
 *
 * @author Lukasz Zielinski
 */
public class FileSystemUtil {
    
    public static String generateTempDirName(Project project) {
        return project.getName();    
    }
    
    public static String generateTempOutputDir(Project project) {
        return generateTempOutputDir() + File.separator + generateTempDirName(project);
    } 
    
    public static String generateTempOutputDir() {
        String tempDirPath = OsUtil.tempDirPath();
        if (!tempDirPath.endsWith(File.separator)) {
            tempDirPath = tempDirPath + File.separator;
        }
        return tempDirPath + "intellijad";
    } 
    
    public static File createTargetDir(Config config) {
        String outputDirPath = config.getOutputDirectory();
        if (!StringUtil.isEmptyOrSpaces(outputDirPath)) {
            File outputDirectory = new File(outputDirPath);
            boolean outputDirExists = outputDirectory.exists();
            if (!outputDirExists && config.isCreateOutputDirectory()) {
                if (outputDirectory.mkdirs()) {
                    return outputDirectory;
                }
            }
        }
        return null;
    }
}
