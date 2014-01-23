/* 
 * @(#) $Id:  $
 */
package net.stevechaloner.intellijad.vfs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.openapi.vfs.ex.temp.TempFileSystem;
import net.stevechaloner.intellijad.IntelliJadConstants;
import net.stevechaloner.intellijad.util.FileSystemUtil;
import org.jetbrains.annotations.NotNull;

/**
 * <p></p>
 * <br/>
 * <p>Created on 12.02.13</p>
 *
 * @author Lukasz Zielinski
 */
public class TempMemoryVFS implements MemoryVFS {
    private final Logger LOG = Logger.getInstance(getClass());
    
    private final TempFileSystem fs;
    private final VirtualFile root;
    
    private final Map<String, VirtualFile> files = new HashMap<String, VirtualFile>();
    
    private TempMemoryVFS(TempFileSystem fs, Project project) {
        this.fs = fs;
        root = initRoot(this.fs, project);
    }

    private VirtualFile initRoot(TempFileSystem fs, Project project) {
        VirtualFile fsRoot = fs.getRoot();
        String rootName = FileSystemUtil.generateTempDirName(project);
        VirtualFile commonRoot = fsRoot.findChild("intellijad");
        
        try {
            if (commonRoot != null) {
                VirtualFile root = commonRoot.findChild(rootName);
                if (root != null) {
                    return root;
                } else {
                    return fs.createChildDirectory(null, commonRoot, rootName);    
                }
            } else {
                commonRoot = fs.createChildDirectory(null, fsRoot, "intellijad");
                return fs.createChildDirectory(null, commonRoot, rootName);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
            
    }
    
    public static MemoryVFS getInstance(Project project) {
        MemoryVFS memoryVFS = project.getUserData(IntelliJadConstants.MEMORY_VFS);
        if (memoryVFS == null) {
            memoryVFS = new TempMemoryVFS(TempFileSystem.getInstance(), project);
            project.putUserData(IntelliJadConstants.MEMORY_VFS, memoryVFS);
        }
        return memoryVFS;    
    }
    
    @Override
    public void deleteFile(Object requestor, VirtualFile virtualFile) throws IOException {
        fs.deleteFile(requestor, virtualFile);
        files.remove(virtualFile.getPath());
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
            String path = parent.getPath() + "/" + name;
            child = files.get(path);
            if (child == null) {
                try {
                    child = fs.createChildDirectory(null,
                            parent,
                            name);
                    files.put(child.getPath(), child);
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
            String path = root.getPath() + "/" + name;
            VirtualFile fileByPath = files.get(path);
            if (fileByPath != null) {
                deleteFile(null, fileByPath);
            }
            VirtualFile file = fs.createChildFile(null, root, name);
            files.put(file.getPath(), file);
            TempMemoryVF memoryVF = new TempMemoryVF(file, fs);
            memoryVF.setContent(content);
            return memoryVF;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void dispose() {
        files.clear();
        try {
            fs.deleteFile(null, root);
            LOG.info("Disposed "+root.getPath());
        } catch (IOException e) {
            LOG.error("Failed to dispose "+root.getPath(), e);
        }
    }
}
