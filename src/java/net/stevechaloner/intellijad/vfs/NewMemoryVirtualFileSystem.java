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
import com.intellij.openapi.util.io.FileUtil.FileBooleanAttributes;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.openapi.vfs.newvfs.NewVirtualFileSystem;
import com.intellij.openapi.vfs.newvfs.NewVirtualFile;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;

import net.stevechaloner.intellijad.IntelliJadConstants;

/**
 * A file system for content that resides only in memory.
 *
 * @author Steve Chaloner
 */
public class NewMemoryVirtualFileSystem extends NewVirtualFileSystem implements ApplicationComponent, MemoryVFS
{
    /**
     * The name of the component.
     */
    private static final String COMPONENT_NAME = "IntelliJad-8-MemoryFileSystem";

    /**
     * The files.
     */
    private final Map<String, NewMemoryVirtualFile> files = new HashMap<String, NewMemoryVirtualFile>();

    /**
     * Listeners for file system events.
     */
    private final List<VirtualFileListener> listeners = new ArrayList<VirtualFileListener>();

    /** {@inheritDoc} */
    public boolean isCaseSensitive()
    {
        return true;
    }

    protected String extractRootPath(@NotNull String s)
    {
        return s;
    }

    public int getRank()
    {
        return 0;
    }

    @Override
    public VirtualFileSystem asVirtualFileSystem() {
        return this;
    }

    @Override
    public MemoryVF newMemoryFV(String name, String content) {
        return new NewMemoryVirtualFile(name, content);
    }

    @Override
    public void refresh(boolean asynchronous) {
        //TODO: auto-generated method implementation
    }

    // todo
    public VirtualFile copyFile(Object o,
                                VirtualFile virtualFile,
                                VirtualFile virtualFile1,
                                String s) throws IOException
    {
        return null;
    }

    public NewMemoryVirtualFile createChildDirectory(Object object,
                                                     VirtualFile parent,
                                                     String name) throws IOException
    {
        NewMemoryVirtualFile file = new NewMemoryVirtualFile(name);
        ((NewMemoryVirtualFile)parent).addChild(file);
        addFile(file);
        return file;
    }

    public VirtualFile createChildFile(Object o,
                                       VirtualFile virtualFile,
                                       String s) throws IOException
    {
        return null;
    }

    public void deleteFile(Object o, VirtualFile virtualFile) throws IOException {
        files.remove(virtualFile.getName());
        NewMemoryVirtualFile parent = (NewMemoryVirtualFile) virtualFile.getParent();
        if (parent != null) {
            parent.deleteChild((NewMemoryVirtualFile)virtualFile);
        }
    }

    public void moveFile(Object o,
                         VirtualFile virtualFile,
                         VirtualFile virtualFile1) throws IOException
    {
    }

    public void renameFile(Object o,
                           VirtualFile virtualFile,
                           String s) throws IOException
    {
    }

    @Override
    public int getBooleanAttributes(@NotNull VirtualFile file, @FileBooleanAttributes int flags) {
        return 0;  //TODO: auto-generated method implementation
    }

    public String getProtocol()
    {
        return IntelliJadConstants.INTELLIJAD_PROTOCOL;
    }

    /** {@inheritDoc} */
    public boolean exists(VirtualFile virtualFile)
    {
        return files.containsValue(virtualFile);
    }

    /** {@inheritDoc} */
    public String[] list(VirtualFile virtualFile)
    {
        return new String[0];
    }

    /** {@inheritDoc} */
    public boolean isDirectory(VirtualFile virtualFile)
    {
        return virtualFile.isDirectory();
    }

    public long getTimeStamp(VirtualFile virtualFile)
    {
        return virtualFile.getTimeStamp();
    }

    public boolean isWritable(VirtualFile virtualFile)
    {
        return virtualFile.isWritable();
    }

    public void setTimeStamp(VirtualFile virtualFile,
                             long l) throws IOException
    {
        ((NewVirtualFile)virtualFile).setTimeStamp(l);
    }

    public void setWritable(VirtualFile virtualFile,
                            boolean b) throws IOException
    {
        // no-op
    }

    public byte[] contentsToByteArray(VirtualFile virtualFile) throws IOException
    {
        return virtualFile.contentsToByteArray();
    }

    public InputStream getInputStream(VirtualFile virtualFile) throws IOException
    {
        return virtualFile.getInputStream();
    }

    public OutputStream getOutputStream(VirtualFile virtualFile,
                                        Object o,
                                        long l,
                                        long l1) throws IOException
    {
        return virtualFile.getOutputStream(o, l, l1);
    }

    public long getLength(VirtualFile virtualFile)
    {
        return virtualFile.getLength();
    }

    @NotNull
    public String getComponentName()
    {
        return COMPONENT_NAME;
    }

    public void initComponent()
    {
        NewMemoryVirtualFile root = new NewMemoryVirtualFile(IntelliJadConstants.INTELLIJAD_ROOT);
        addFile(root);
    }

    public void disposeComponent()
    {
        files.clear();
    }

    /** {@inheritDoc} */
    public void addVirtualFileListener(VirtualFileListener virtualFileListener)
    {
        super.addVirtualFileListener(virtualFileListener);
        if (virtualFileListener != null)
        {
            listeners.add(virtualFileListener);
        }
    }

    /** {@inheritDoc} */
    public void removeVirtualFileListener(VirtualFileListener virtualFileListener)
    {
        super.removeVirtualFileListener(virtualFileListener);
        listeners.remove(virtualFileListener);
    }

    /**
     * Notifies listeners of a new file.
     *
     * @param file the new file
     */
    private void fireFileCreated(NewMemoryVirtualFile file)
    {
        VirtualFileEvent e = new VirtualFileEvent(this,
                                                  file,
                                                  file.getName(),
                                                  file.getParent());
        for (VirtualFileListener listener : listeners)
        {
            listener.fileCreated(e);
        }
    }

    /**
     * Add a file to the file system.
     *
     * @param p_file the file to add
     */
    public void addFile(@NotNull MemoryVF p_file)
    {
        NewMemoryVirtualFile file = (NewMemoryVirtualFile) p_file;
        files.put(file.getName(), file);
        fireFileCreated(file);
    }

/**
     * For a given package, e.g. net.stevechaloner.intellijad, get the file corresponding
     * to the last element, e.g. intellijad.  If the file or any part of the directory tree
     * does not exist, it is created dynamically.
     *
     * @param packageName the name of the package
     * @return the file corresponding to the final location of the package
     */
    public NewMemoryVirtualFile getFileForPackage(@NotNull String packageName)
    {
        StringTokenizer st = new StringTokenizer(packageName, ".");
        List<String> names = new ArrayList<String>();
        while (st.hasMoreTokens())
        {
            names.add(st.nextToken());
        }
        return getFileForPackage(names,
                                files.get(IntelliJadConstants.INTELLIJAD_ROOT));
    }

    /**
     * Recursively search for, and if necessary create, the final file in the
     * name list.
     *
     * @param names  the name list
     * @param parent the parent file
     * @return a file corresponding to the last entry in the name list
     */
    private NewMemoryVirtualFile getFileForPackage(@NotNull List<String> names,
                                                   @NotNull NewMemoryVirtualFile parent)
    {
        NewMemoryVirtualFile child = null;
        if (!names.isEmpty())
        {
            String name = names.remove(0);
            child = (NewMemoryVirtualFile)parent.findChild(name);
            if (child == null)
            {
                try
                {
                    child = createChildDirectory(null,
                                                 parent,
                                                 name);
                }
                catch (IOException e)
                {
                    Logger.getInstance(getClass().getName()).error(e);
                }
            }
        }

        if (child != null && !names.isEmpty())
        {
            child = getFileForPackage(names,
                                     child);
        }
        return child;
    }

    @Override
    public VirtualFile findFileByPath(@NotNull String s)
    {
        return files.get(s);
    }

    @Override
    public VirtualFile findFileByPathIfCached(@NotNull String s)
    {
        return findFileByPath(s);
    }

    @Override
    public VirtualFile refreshAndFindFileByPath(String s)
    {
        return findFileByPath(s);
    }
}