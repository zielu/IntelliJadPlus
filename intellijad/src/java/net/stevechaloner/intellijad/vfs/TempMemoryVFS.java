/* 
 * @(#) $Id:  $
 */
package net.stevechaloner.intellijad.vfs;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.openapi.vfs.ex.temp.TempFileSystem;
import net.stevechaloner.intellijad.util.FileSystemUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * <p></p>
 * <br/>
 * <p>Created on 12.02.13</p>
 *
 * @author Lukasz Zielinski
 */
public class TempMemoryVFS implements MemoryVFS {
    
    private final TempFileSystem fs;
    private final VirtualFile root;
    
    private TempMemoryVFS(TempFileSystem fs, Project project) {
        this.fs = fs;
        root = initRoot(project);
    }

    private VirtualFile initRoot(Project project) {
        VirtualFile fsRoot = fs.findFileByPath("/");
        String rootName = FileSystemUtil.generateTempDirName(project);
        VirtualFile root = fs.findFileByPath("/intellijad/" + rootName);
        if (root == null) {
            try {
                VirtualFile commonRoot = fs.findFileByPath("/intellijad");
                if (commonRoot == null) {
                    commonRoot = fs.createChildDirectory(null, fsRoot, "intellijad");
                }
                return fs.createChildDirectory(null, commonRoot, rootName);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            return root;
        }
    }
    
    public static MemoryVFS getInstance(Project project) {
        return new TempMemoryVFS(TempFileSystem.getInstance(), project);    
    }
    
    @Override
    public void deleteFile(Object requestor, VirtualFile virtualFile) throws IOException {
        fs.deleteFile(requestor, virtualFile);
    }

    @Override
    public MemoryVF getFileForPackage(String packageName) {
        StringTokenizer st = new StringTokenizer(packageName, ".");
        List<String> names = new ArrayList<String>();
        while (st.hasMoreTokens()) {
            names.add(st.nextToken());
        }
        return new TempMemoryVF(getFileForPackage(names, root), fs);
    }

    private VirtualFile getFileForPackage(@NotNull List<String> names,
                                                    @NotNull VirtualFile parent) {
        VirtualFile child = null;
        if (!names.isEmpty()) {
            String name = names.remove(0);
            child = fs.findFileByPath(parent.getPath()+"/"+name);
            if (child == null) {
                try {
                    child = fs.createChildDirectory(null,
                            parent,
                            name);
                } catch (IOException e) {
                    Logger.getInstance(getClass().getName()).error(e);
                }
            }
        }

        if (child != null && !names.isEmpty()) {
            child = getFileForPackage(names, child);
        }
        return child;
    }
    
    @Override
    public void addFile(MemoryVF file) {}

    @Override
    public VirtualFileSystem asVirtualFileSystem() {
        return fs;
    }

    @Override
    public MemoryVF newMemoryFV(@NotNull String name, String content) {
        try {
            VirtualFile file = fs.createChildFile(null, root, name);
            TempMemoryVF memoryVF = new TempMemoryVF(file, fs);
            memoryVF.setContent(content);
            return memoryVF;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
