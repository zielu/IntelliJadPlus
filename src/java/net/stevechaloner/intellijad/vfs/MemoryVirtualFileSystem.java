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
import com.intellij.openapi.vfs.DeprecatedVirtualFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFileManager;
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
import java.util.StringTokenizer;

/**
 * A file system for content that resides only in memory.
 *
 * @author Steve Chaloner
 */
public class MemoryVirtualFileSystem extends DeprecatedVirtualFileSystem implements ApplicationComponent
{
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
     * Add a file to the file system.
     *
     * @param file the file to add
     */
    public void addFile(@NotNull MemoryVirtualFile file)
    {
        files.put(file.getName(),
                  file);
        fireFileCreated(file);
    }

    /**
     * Notifies listeners of a new file.
     *
     * @param file the new file
     */
    private void fireFileCreated(MemoryVirtualFile file)
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

    /** {@inheritDoc} */
    public String getProtocol()
    {
        return IntelliJadConstants.INTELLIJAD_PROTOCOL;
    }

    /** {@inheritDoc} */
    @Nullable
    public VirtualFile findFileByPath(@NotNull String string)
    {
        // todo rewrite this so it doesn't look like crap
        VirtualFile file = null;
        if (!StringUtil.isEmptyOrSpaces(string))
        {
            String path = VirtualFileManager.extractPath(string);
            StringTokenizer st = new StringTokenizer(path, "/");
            VirtualFile currentFile = files.get(IntelliJadConstants.INTELLIJAD_ROOT);
            boolean keepLooking = true;
            String targetName = null;
            while (keepLooking && st.hasMoreTokens())
            {
                String element = st.nextToken();
                if (!st.hasMoreTokens())
                {
                    targetName = element;
                }
                VirtualFile child = currentFile.findChild(element);
                if (child != null)
                {
                    currentFile = child;
                }
                else
                {
                    keepLooking = false;
                }
            }

            if (currentFile != null &&
                targetName != null &&
                targetName.equals(currentFile.getName()))
            {
                file = currentFile;
            }
        }
        return file;
    }

    /** {@inheritDoc} */
    public void refresh(boolean b)
    {
    }

    /** {@inheritDoc} */
    @Nullable
    public VirtualFile refreshAndFindFileByPath(String string)
    {
        return files.get(string);
    }

    /** {@inheritDoc} */
    public void deleteFile(Object object,
                           VirtualFile virtualFile) throws IOException
    {
        files.remove(virtualFile.getName());

        MemoryVirtualFile parent = (MemoryVirtualFile)virtualFile.getParent();
        if (parent != null)
        {
            parent.deleteChild((MemoryVirtualFile)virtualFile);
        }
    }

    /** {@inheritDoc} */
    public void moveFile(Object object,
                         VirtualFile virtualFile,
                         VirtualFile virtualFile1) throws IOException
    {
        files.remove(virtualFile.getName());
        files.put(virtualFile1.getName(),
                  (MemoryVirtualFile) virtualFile1);
    }

    /** {@inheritDoc} */
    public void renameFile(Object object,
                           VirtualFile virtualFile,
                           String string) throws IOException
    {
        files.remove(virtualFile.getName());
        files.put(string,
                  (MemoryVirtualFile) virtualFile);
    }

    /** {@inheritDoc} */
    public MemoryVirtualFile createChildFile(Object object,
                                             VirtualFile parent,
                                             String name) throws IOException
    {
        MemoryVirtualFile file = new MemoryVirtualFile(name,
                                                       null);
        file.setParent(parent);
        addFile(file);
        return file;
    }

    /** {@inheritDoc} */
    public MemoryVirtualFile createChildDirectory(Object object,
                                                  VirtualFile parent,
                                                  String name) throws IOException
    {
        MemoryVirtualFile file = new MemoryVirtualFile(name);
        ((MemoryVirtualFile)parent).addChild(file);
        addFile(file);
        return file;
    }

    /** {@inheritDoc} */
    @NonNls
    @NotNull
    public String getComponentName()
    {
        return COMPONENT_NAME;
    }

    /** {@inheritDoc} */
    public void initComponent()
    {
        MemoryVirtualFile root = new MemoryVirtualFile(IntelliJadConstants.INTELLIJAD_ROOT);
        addFile(root);
    }

    /** {@inheritDoc} */
    public void disposeComponent()
    {
        files.clear();
    }

    /**
     * For a given package, e.g. net.stevechaloner.intellijad, get the file corresponding
     * to the last element, e.g. intellijad.  If the file or any part of the directory tree
     * does not exist, it is created dynamically.
     *
     * @param packageName the name of the package
     * @return the file corresponding to the final location of the package
     */
    public MemoryVirtualFile getFileForPackage(@NotNull String packageName)
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
    private MemoryVirtualFile getFileForPackage(@NotNull List<String> names,
                                                @NotNull MemoryVirtualFile parent)
    {
        MemoryVirtualFile child = null;
        if (!names.isEmpty())
        {
            String name = names.remove(0);
            child = parent.getChild(name);
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

    /** {@inheritDoc} */
    public void projectOpened()
    {
    }

    /** {@inheritDoc} */
    public void projectClosed()
    {
        files.clear();
    }

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

    /** {@inheritDoc} */
    public VirtualFile copyFile(Object o,
                                VirtualFile virtualFile,
                                VirtualFile virtualFile1,
                                String s) throws IOException {
        return null;
    }

    /** {@inheritDoc} */
    public boolean isReadOnly() {
        return false;
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

    /** {@inheritDoc} */
    public long getTimeStamp(VirtualFile virtualFile)
    {
        return virtualFile.getTimeStamp();
    }

    /** {@inheritDoc} */
    public void setTimeStamp(VirtualFile virtualFile,
                             long l) throws IOException
    {
        // no-op
    }

    /** {@inheritDoc} */
    public boolean isWritable(VirtualFile virtualFile)
    {
        return virtualFile.isWritable();
    }

    /** {@inheritDoc} */
    public void setWritable(VirtualFile virtualFile,
                            boolean b) throws IOException
    {
        // no-op
    }

    /** {@inheritDoc} */
    public InputStream getInputStream(VirtualFile virtualFile) throws IOException
    {
        return virtualFile.getInputStream();
    }

    /** {@inheritDoc} */
    public OutputStream getOutputStream(VirtualFile virtualFile,
                                        Object o,
                                        long l,
                                        long l1) throws IOException
    {
        return virtualFile.getOutputStream(o, l, l1);
    }

    /** {@inheritDoc} */
    public long getLength(VirtualFile virtualFile)
    {
        return virtualFile.getLength();
    }
}