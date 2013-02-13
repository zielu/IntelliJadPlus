/* 
 * @(#) $Id:  $
 */
package net.stevechaloner.intellijad.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import net.stevechaloner.intellijad.config.Config;
import net.stevechaloner.intellijad.decompilers.DecompilationContext;

import java.io.File;

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
        String fileSeparator = System.getProperty("file.separator");
        return generateTempOutputDir() + fileSeparator + generateTempDirName(project);
    } 
    
    public static String generateTempOutputDir() {
        String tempDirPath = System.getProperty("java.io.tmpdir");
        String fileSeparator = System.getProperty("file.separator");
        if (!tempDirPath.endsWith(fileSeparator)) {
            tempDirPath = tempDirPath + fileSeparator;
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
