/* 
 * @(#) $Id:  $
 */
package net.stevechaloner.intellijad.util;

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
