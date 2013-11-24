/* 
 * @(#) $Id:  $
 */
package net.stevechaloner.intellijad.vfs;

import java.io.IOException;

import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

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
    MemoryVF addChild(MemoryVF file);

    void setWritable(boolean writable) throws IOException;
    void setContent(@NotNull String content);
}
