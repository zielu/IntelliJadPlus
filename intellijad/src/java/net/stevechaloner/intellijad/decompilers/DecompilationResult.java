/* 
 * @(#) $Id:  $
 */
package net.stevechaloner.intellijad.decompilers;

import com.intellij.openapi.vfs.VirtualFile;

/**
 * <p></p>
 * <br/>
 * <p>Created on 22.01.14</p>
 *
 * @author Lukasz Zielinski
 */
public class DecompilationResult {
    private final VirtualFile resultFile;

    public DecompilationResult() {
        this(null);
    }
    
    public DecompilationResult(VirtualFile resultFile) {
        this.resultFile = resultFile;
    }

    public boolean isSuccessful() {
        return resultFile != null;
    }
    
    public VirtualFile getResultFile() {
        return resultFile;
    }
}
