/* 
 * $Id$
 */
package net.stevechaloner.intellijad;

import com.intellij.openapi.vfs.VirtualFile;
import net.stevechaloner.intellijad.config.Config;
import net.stevechaloner.intellijad.decompilers.DecompilationChoiceListener;

/**
 * <p></p>
 * <br/>
 * <p>Created on 05.03.14</p>
 *
 * @author Lukasz Zielinski
 */
public class PreparedDecompilation {
    public final VirtualFile testFile;
    public final DecompilationChoiceListener listener;
    public final Config config;
    
    public PreparedDecompilation(VirtualFile testFile, DecompilationChoiceListener listener, Config config) {
        this.testFile = testFile;
        this.listener = listener;
        this.config = config;
    }
}
