/* 
 * @(#) $Id:  $
 */
package net.stevechaloner.intellijad.vfs;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * <p></p>
 * <br/>
 * <p>Created on 22.03.12.</p>
 *
 * @author Lukasz Zielinski
 */
public interface MemoryVFS {
    void deleteFile(Object requestor, VirtualFile virtualFile) throws IOException;

    MemoryVF getFileForPackage(String packageName);

    void addFile(MemoryVF file);

    VirtualFileSystem asVirtualFileSystem();

    MemoryVF newMemoryFV(@NotNull String name, String content);
}
