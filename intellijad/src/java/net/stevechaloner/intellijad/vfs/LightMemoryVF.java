/* 
 * @(#) $Id:  $
 */
package net.stevechaloner.intellijad.vfs;

import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * <p></p>
 * <br/>
 * <p>Created on 12.02.13</p>
 *
 * @author Lukasz Zielinski
 */
public class LightMemoryVF implements MemoryVF {
    private final VirtualFile virtualFile;

    public LightMemoryVF(VirtualFile virtualFile) {
        this.virtualFile = virtualFile;
    }

    @NotNull
    @Override
    public String getContent() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public VirtualFile asVirtualFile() {
        return virtualFile;
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void addChild(MemoryVF file) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void setWritable(boolean writable) throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void setContent(@NotNull String content) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
