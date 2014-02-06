/* 
 * $Id$
 */
package net.stevechaloner.intellijad;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import net.stevechaloner.intellijad.decompilers.DecompilationDescriptor;

/**
 * <p></p>
 * <br/>
 * <p>Created on 06.02.14</p>
 *
 * @author Lukasz Zielinski
 */
public enum CurrentDecompilation {
    Instance;
    
    private static final Logger LOG = Logger.getInstance(CurrentDecompilation.class);
    private static final boolean ALLOW_SINGLE = true;
    
    public static boolean isInProgress(Project project, DecompilationDescriptor descriptor) {
        if (ALLOW_SINGLE) {
            boolean decompiling = IntelliJadConstants.CURRENTLY_DECOMPILING.isIn(project);
            if (decompiling && LOG.isDebugEnabled()) {
                LOG.debug("Decompilation of "+IntelliJadConstants.CURRENTLY_DECOMPILING.get(project)+" in progress");
            }
            return decompiling;
        } else {
            return false;
        }
    }
    
    public static void set(Project project, DecompilationDescriptor descriptor) {
        if (ALLOW_SINGLE) {
            IntelliJadConstants.CURRENTLY_DECOMPILING.set(project, descriptor.getClassName());
        }
    }
    
    public static void clear(Project project, DecompilationDescriptor descriptor) {
        if (ALLOW_SINGLE) {
            IntelliJadConstants.CURRENTLY_DECOMPILING.set(project, null);
        }
    }
}
