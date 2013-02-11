/*
 * Copyright 2007 Steve Chaloner
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package net.stevechaloner.intellijad.vfs;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.openapi.vfs.newvfs.VirtualFileFilteringListener;
import net.stevechaloner.intellijad.IntelliJadConstants;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * A file system for content that resides only in memory.
 *
 * @author Steve Chaloner
 */
public class MemoryVirtualFileSystem extends VirtualFileSystem implements ApplicationComponent, MemoryVFS {
    private final Logger LOG = Logger.getInstance(getClass());
    
    /**
     * The name of the component.
     */
    private static final String COMPONENT_NAME = "MemoryFileSystem";

    /**
     * The files.
     */
    private final Map<String, MemoryVirtualFile> files = new HashMap<String, MemoryVirtualFile>();

    /**
     * Listeners for file system events.
     */
    private final List<VirtualFileListener> listeners = new ArrayList<VirtualFileListener>();

    /** {@inheritDoc} */
    public void addVirtualFileListener(VirtualFileListener virtualFileListener) {
        if (virtualFileListener != null) {
            listeners.add(virtualFileListener);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Listener added: "+virtualFileListener);    
            }
        }
    }

    /** {@inheritDoc} */
    public void removeVirtualFileListener(VirtualFileListener virtualFileListener) {
        listeners.remove(virtualFileListener);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Listener removed: "+virtualFileListener);    
        }
    }

    /**
     * Add a file to the file system.
     *
     * @param p_file the file to add
     */
    @Override
    public void addFile(@NotNull MemoryVF p_file) {
        MemoryVirtualFile file = (MemoryVirtualFile) p_file;
        MemoryVirtualFile old = files.put(file.getName(), file);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Add file: "+old+" -> "+file);    
        }
        fireFileCreated(file);
    }

    /**
     * Notifies listeners of a new file.
     *
     * @param file the new file
     */
    private void fireFileCreated(VirtualFile file) {
        VirtualFileEvent e = new VirtualFileEvent(this,
                                                  file,
                                                  file.getName(),
                                                  file.getParent());
        for (VirtualFileListener listener : listeners)
        {
            listener.fileCreated(e);
        }
    }

    private void fireFileDeleted(VirtualFile file) {
        VirtualFileEvent e = new VirtualFileEvent(this,
                                                  file,
                                                  file.getName(),
                                                  file.getParent());
        for (VirtualFileListener listener : listeners)
        {
            listener.fileDeleted(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getProtocol() {
        return IntelliJadConstants.INTELLIJAD_PROTOCOL;
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    public VirtualFile findFileByPath(@NotNull String string) {
        // todo rewrite this so it doesn't look like crap
        VirtualFile file = null;
        if (!StringUtil.isEmptyOrSpaces(string)) {
            String path = VirtualFileManager.extractPath(string);
            StringTokenizer st = new StringTokenizer(path, "/");
            VirtualFile currentFile = files.get(IntelliJadConstants.INTELLIJAD_ROOT);
            boolean keepLooking = true;
            String targetName = null;
            while (keepLooking && st.hasMoreTokens()) {
                String element = st.nextToken();
                if (!st.hasMoreTokens()) {
                    targetName = element;
                }
                VirtualFile child = currentFile.findChild(element);
                if (child != null) {
                    currentFile = child;
                } else {
                    keepLooking = false;
                }
            }

            if (currentFile != null &&
                    targetName != null &&
                    targetName.equals(currentFile.getName())) {
                file = currentFile;
            }
        }
        return file;
    }

    /**
     * {@inheritDoc}
     */
    public void refresh(boolean b) {
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    public VirtualFile refreshAndFindFileByPath(String string) {
        return files.get(string);
    }

    /**
     * {@inheritDoc}
     */
    public void deleteFile(Object requestor, VirtualFile virtualFile) throws IOException {
        MemoryVirtualFile file = (MemoryVirtualFile) virtualFile;
        MemoryVirtualFile old = files.remove(file.getName());
        if (LOG.isDebugEnabled()) {
            LOG.debug("Delete file: "+old+", requestor="+requestor);    
        }
        MemoryVirtualFile parent = (MemoryVirtualFile) file.getParent();
        if (parent != null) {
            parent.deleteChild(file);
        }
        file.invalidate();
        fireFileDeleted(virtualFile);
    }

    /**
     * {@inheritDoc}
     */
    public void moveFile(Object requestor,
                         VirtualFile virtualFile,
                         VirtualFile newParentVirtualFile) throws IOException {
        /*MemoryVirtualFile file = (MemoryVirtualFile) virtualFile;
        MemoryVirtualFile parent = (MemoryVirtualFile) file.getParent();
        if (parent != null) {
            parent.deleteChild(file);
        }
        MemoryVirtualFile newParent = (MemoryVirtualFile) newParentVirtualFile;
        newParent.addChild(file);      
        if (LOG.isDebugEnabled()) {
            LOG.debug("Move file: "+file+" ->"+newParent+", requestor="+requestor);    
        }*/
    }

    /**
     * {@inheritDoc}
     */
    public void renameFile(Object requestor,
                           VirtualFile virtualFile,
                           String newName) throws IOException {
        /*files.remove(virtualFile.getName());
        if (LOG.isDebugEnabled()) {
            LOG.debug("Rename file: "+virtualFile+" ->"+newName+", requestor="+requestor);    
        }
        MemoryVirtualFile file = (MemoryVirtualFile) virtualFile;
        file.setName(newName);
        files.put(newName, file);*/
    }

    /**
     * {@inheritDoc}
     */
    public MemoryVirtualFile createChildFile(Object requestor,
                                             VirtualFile parent,
                                             String name) throws IOException {
        MemoryVirtualFile file = new MemoryVirtualFile(name, null);
        file.setParent(parent);
        addFile(file);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Create child file: "+parent+" ->"+name+", requestor="+requestor);    
        }
        return file;
    }

    /**
     * {@inheritDoc}
     */
    public MemoryVirtualFile createChildDirectory(Object requestor,
                                                  VirtualFile parent,
                                                  String name) throws IOException {
        MemoryVirtualFile file = new MemoryVirtualFile(name);
        ((MemoryVirtualFile) parent).addChild(file);
        addFile(file);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Create child directory: "+parent+" ->"+name+", requestor="+requestor);    
        }
        return file;
    }

    /**
     * {@inheritDoc}
     */
    @NonNls
    @NotNull
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    /**
     * {@inheritDoc}
     */
    public void initComponent() {
        MemoryVirtualFile root = new MemoryVirtualFile(IntelliJadConstants.INTELLIJAD_ROOT);
        addFile(root);
    }

    /**
     * {@inheritDoc}
     */
    public void disposeComponent() {
        files.clear();
        listeners.clear();
    }

    /**
     * For a given package, e.g. net.stevechaloner.intellijad, get the file corresponding
     * to the last element, e.g. intellijad.  If the file or any part of the directory tree
     * does not exist, it is created dynamically.
     *
     * @param packageName the name of the package
     * @return the file corresponding to the final location of the package
     */
    public MemoryVirtualFile getFileForPackage(@NotNull String packageName) {
        StringTokenizer st = new StringTokenizer(packageName, ".");
        List<String> names = new ArrayList<String>();
        while (st.hasMoreTokens()) {
            names.add(st.nextToken());
        }
        return getFileForPackage(names,
                files.get(IntelliJadConstants.INTELLIJAD_ROOT));
    }

    @Override
    public VirtualFileSystem asVirtualFileSystem() {
        return this;
    }

    @Override
    public MemoryVF newMemoryFV(@NotNull String name, String content) {
        return new MemoryVirtualFile(name, content);
    }

    /**
     * Recursively search for, and if necessary create, the final file in the
     * name list.
     *
     * @param names  the name list
     * @param parent the parent file
     * @return a file corresponding to the last entry in the name list
     */
    private MemoryVirtualFile getFileForPackage(@NotNull List<String> names,
                                                @NotNull MemoryVirtualFile parent) {
        MemoryVirtualFile child = null;
        if (!names.isEmpty()) {
            String name = names.remove(0);
            child = parent.getChild(name);
            if (child == null) {
                try {
                    child = createChildDirectory(null,
                            parent,
                            name);
                } catch (IOException e) {
                    Logger.getInstance(getClass().getName()).error(e);
                }
            }
        }

        if (child != null && !names.isEmpty()) {
            child = getFileForPackage(names,
                    child);
        }
        return child;
    }

    /**
     * {@inheritDoc}
     */
    public VirtualFile copyFile(Object o,
                                VirtualFile virtualFile,
                                VirtualFile virtualFile1,
                                String s) throws IOException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isReadOnly() {
        return true;
    }
}
