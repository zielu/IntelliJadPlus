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
 * <p>Created on 22.03.12.</p>
 *
 * @author Lukasz Zielinski
 */
public interface MemoryVF {
    @NotNull
    String getContent();
    VirtualFile asVirtualFile();
    String getName();
    void addChild(MemoryVF file);

    void setWritable(boolean writable) throws IOException;
    void setContent(@NotNull String content);
}
