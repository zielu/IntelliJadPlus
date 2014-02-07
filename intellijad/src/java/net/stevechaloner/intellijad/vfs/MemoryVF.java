/* 
 * @(#) $Id:  $
 */
package net.stevechaloner.intellijad.vfs;

import java.io.IOException;
import java.io.InputStream;

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
    String CharsetName = "UTF-8";
    
    @NotNull
    String getContent();
    @NotNull
    InputStream getInputStream() throws IOException;
    VirtualFile asVirtualFile();
    String getName();
    MemoryVF addChild(MemoryVF file);

    void setWritable(boolean writable) throws IOException;
    void setContent(@NotNull String content);
    
    long size();
}
