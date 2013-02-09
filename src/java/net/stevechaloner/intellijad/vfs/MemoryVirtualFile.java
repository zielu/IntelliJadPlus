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

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.openapi.vfs.VirtualFileWithId;
import net.stevechaloner.intellijad.IntelliJadConstants;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A memory-based file.
 *
 * @author Steve Chaloner
 */
public class MemoryVirtualFile extends VirtualFile implements MemoryVF, VirtualFileWithId
{
    private static AtomicInteger ID_GEN = new AtomicInteger(1);


    /**
     * The name of the file.
     */
    private final String name;

    private final String nameWithoutExtension;

    /**
     * The content of the file.
     */
    private String content;

    /**
     * A flag to indicate if this file represents a directory.
     */
    private final boolean isDirectory;

    /**
     * The children of this file, if the file is a directory.
     */
    private final Map<String, MemoryVirtualFile> children = new HashMap<String, MemoryVirtualFile>();

    /**
     * The parent of this file.  If this file is at the root of the file
     * system, it will not have a parent.
     */
    @Nullable
    private VirtualFile parent;

    /**
     * Immutability flag
     */
    private boolean writable = true;

    private final int id;

    private boolean valid = true;

    /**
     * Initialises a new instance of this class.
     *
     * @param name the name of the file
     * @param content the content of the file
     */
    MemoryVirtualFile(@NotNull String name,
                             String content)
    {
        this(name,
             content,
             false);
    }

    /**
     * Initialises a new instance of this class.
     *
     * @param name the name of the file
     */
    MemoryVirtualFile(@NotNull String name)
    {
        this(name,
             null,
             true);
    }

    /**
     * Initialises a new instance of this class.
     *
     * @param name the name of the file
     * @param content the content of the file.  This is mutually exclusive with
     * <code>isDirectory</code>.
     * @param isDirectory true iff this file is a directory.  This is mutually exclusive
     * with <code>content<code>.
     */
    private MemoryVirtualFile(@NotNull String name,
                              String content,
                              boolean isDirectory)
    {
        this.name = name;
        nameWithoutExtension = FileUtil.getNameWithoutExtension(name);
        this.content = content;
        this.isDirectory = isDirectory;
        this.id = ID_GEN.incrementAndGet();
    }

    /** {@inheritDoc} */
    @NotNull
    @NonNls
    public String getName()
    {
        return name;
    }

    /** {@inheritDoc} */
    @NotNull
    public VirtualFileSystem getFileSystem()
    {
        return VirtualFileManager.getInstance().getFileSystem(IntelliJadConstants.INTELLIJAD_PROTOCOL);
    }

    /** {@inheritDoc} */
    public String getPath()
    {
        VirtualFile parent = getParent();
        return parent == null ? name : parent.getPath() + '/' + name;
    }

    /**
     * Sets the writable status of the file.
     *
     * @param writable true if the file is writable
     */
    public void setWritable(boolean writable) throws IOException
    {
        this.writable = writable;
    }

    /** {@inheritDoc} */
    public boolean isWritable()
    {
        return writable;
    }

    /** {@inheritDoc} */
    public boolean isDirectory()
    {
        return isDirectory;
    }

    /** {@inheritDoc} */
    public boolean isValid()
    {
        return valid;
    }

    public void invalidate() {
        valid = false;
    }

    /**
     * Sets the parent of this file.
     *
     * @param parent the parent
     */
    public void setParent(@Nullable VirtualFile parent)
    {
        this.parent = parent;
    }

    /** {@inheritDoc} */
    @Nullable
    public VirtualFile getParent()
    {
        return parent;
    }

    /**
     * Add the given file to the child list of this directory.
     *
     * @param p_file the file to add to the list of children
     * @throws IllegalStateException if this file is not a directory
     */
    public void addChild(MemoryVF p_file) throws IllegalStateException
    {
        MemoryVirtualFile file = (MemoryVirtualFile) p_file;
        if (isDirectory)
        {
            file.setParent(this);
            children.put(file.getName(),
                         file);
        }
        else
        {
            throw new IllegalStateException("files can only be added to a directory");
        }
    }

    /** {@inheritDoc} */
    public VirtualFile[] getChildren()
    {
        return children.values().toArray(new VirtualFile[children.size()]);
    }

    /** {@inheritDoc} */
    public OutputStream getOutputStream(Object object,
                                        long l,
                                        long l1) throws IOException
    {
        return new ByteArrayOutputStream();
    }

    /** {@inheritDoc} */
    public byte[] contentsToByteArray() throws IOException
    {
        return content == null ? new byte[0] : content.getBytes();
    }

    /** {@inheritDoc} */
    public long getTimeStamp()
    {
        return 0L;
    }

    /** {@inheritDoc} */
    public long getLength()
    {
        return content == null ? 0 : content.getBytes().length;
    }

    /** {@inheritDoc} */
    public void refresh(boolean b,
                        boolean b1,
                        Runnable runnable)
    {
    }

    /** {@inheritDoc} */
    public InputStream getInputStream() throws IOException
    {
        return new ByteArrayInputStream(contentsToByteArray());
    }

    /**
     * Sets the content of the file.
     *
     * @param content the content
     */
    public void setContent(@NotNull String content)
    {
        this.content = content;
    }

    /**
     * Gets the content of the file.
     *
     * @return the content of the file
     */
    @NotNull
    public String getContent()
    {
        return content;
    }

    @Override
    public VirtualFile asVirtualFile() {
        return this;
    }

    /**
     * Gets the file from this directory's children.
     *
     * @param name the name of the child to retrieve
     * @return the file, or null if it cannot be found
     */
    @Nullable
    public MemoryVirtualFile getChild(String name)
    {
        return children.get(name);
    }

    /** {@inheritDoc} */
    public long getModificationStamp()
    {
        return 0L;
    }

    /** {@inheritDoc} */
    @NotNull
    public String getUrl() {
        return IntelliJadConstants.INTELLIJAD_SCHEMA + getPath();
    }

    /**
     * Deletes the specified file.
     *
     * @param file the file to delete
     */
    public void deleteChild(MemoryVirtualFile file)
    {
        children.remove(file.getName());
    }

    @NonNls
    public String toString()
    {
        return nameWithoutExtension;
    }

    @Override
    public int getId() {
        return id;
    }
}
