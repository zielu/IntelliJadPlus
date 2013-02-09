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

import com.intellij.openapi.vfs.newvfs.NewVirtualFile;
import com.intellij.openapi.vfs.newvfs.NewVirtualFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.newvfs.impl.VirtualFileSystemEntry;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import net.stevechaloner.intellijad.IntelliJadConstants;

/**
 * @author Steve Chaloner
 */
public class NewMemoryVirtualFile extends NewVirtualFile implements MemoryVF
{
    /**
     * The name of the file.
     */
    private final String name;

    private final String nameWithoutExtension;

    private final Map<Integer, Boolean> flags = new HashMap<Integer, Boolean>();

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
    private final Map<String, NewMemoryVirtualFile> children = new HashMap<String, NewMemoryVirtualFile>();

    /**
     * The parent of this file.  If this file is at the root of the file
     * system, it will not have a parent.
     */
    @Nullable
    private NewVirtualFile parent;

    /**
     * Immutability flag
     */
    private boolean writable = true;

    private boolean dirty = false;

    private long timestamp = 0L;
    private final int id;

    /**
     * Initialises a new instance of this class.
     *
     * @param name    the name of the file
     * @param content the content of the file
     */
    NewMemoryVirtualFile(@NotNull String name,
                         String content, int id) {
        this(name,
                content,
                false, id);
    }

    /**
     * Initialises a new instance of this class.
     *
     * @param name the name of the file
     */
    NewMemoryVirtualFile(@NotNull String name, int id) {
        this(name,
                null,
                true, id);
    }

    /**
     * Initialises a new instance of this class.
     *
     * @param name        the name of the file
     * @param content     the content of the file.  This is mutually exclusive with
     *                    <code>isDirectory</code>.
     * @param isDirectory true iff this file is a directory.  This is mutually exclusive
     *                    with <code>content<code>.
     */
    private NewMemoryVirtualFile(@NotNull String name,
                                 @Nullable String content,
                                 boolean isDirectory, int id) {
        this.name = name;
        nameWithoutExtension = FileUtil.getNameWithoutExtension(name);
        this.content = content;
        this.isDirectory = isDirectory;
        this.id = id;
    }

    @NotNull
    public NewVirtualFileSystem getFileSystem() {
        return (NewVirtualFileSystem) VirtualFileManager.getInstance().getFileSystem(IntelliJadConstants.INTELLIJAD_PROTOCOL);
    }

    @Override
    public NewVirtualFile findChildByIdIfCached(int id) {
        return findChildById(id);
    }

    @Override
    public Iterable<VirtualFile> iterInDbChildren() {
        return ContainerUtil.emptyIterable();
    }

    // todo
    public NewVirtualFile findChild(@NotNull String s) {
        return children.get(s);
    }

    // todo
    public NewVirtualFile refreshAndFindChild(String s) {
        return children.get(s);
    }

    // todo
    public NewVirtualFile findChildIfCached(String s) {
        return children.get(s);
    }

    /**
     * {@inheritDoc}
     */
    public void setTimeStamp(long timestamp) throws IOException {
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public void setWritable(boolean writable) throws IOException {
        this.writable = writable;
    }

    public void markDirty() {
        this.dirty = true;
    }

    public void markDirtyRecursively() {
        markDirty();
        for (NewMemoryVirtualFile virtualFile : getChildren()) {
            virtualFile.markDirty();
        }
    }

    public boolean isDirty() {
        return dirty;
    }

    public void markClean() {
        this.dirty = false;
    }

    public Collection<VirtualFile> getCachedChildren() {
        return new ArrayList<VirtualFile>(Arrays.asList(getChildren()));
    }

    public NewVirtualFile getParent() {
        return parent;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public String getUrl() {
        return IntelliJadConstants.INTELLIJAD_SCHEMA + getPath();
    }

    public String getPath() {
        VirtualFile parent = getParent();
        return parent == null ? name : parent.getPath() + '/' + name;
    }

    public boolean isWritable() {
        return writable;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public NewMemoryVirtualFile[] getChildren() {
        return children.values().toArray(new NewMemoryVirtualFile[children.size()]);
    }

    public OutputStream getOutputStream(Object o,
                                        long l,
                                        long l1) throws IOException {
        return new ByteArrayOutputStream();
    }

    public long getLength() {
        return content == null ? 0 : content.getBytes().length;
    }

    public long getTimeStamp() {
        return timestamp;
    }

    public InputStream getInputStream() throws IOException {
        String s = content == null ? "" : content;
        return new ByteArrayInputStream(s.getBytes());
    }

    /**
     * Add the given file to the child list of this directory.
     *
     * @param p_file the file to add to the list of children
     * @throws IllegalStateException if this file is not a directory
     */
    @Override
    public void addChild(MemoryVF p_file) throws IllegalStateException {
        NewMemoryVirtualFile file = (NewMemoryVirtualFile) p_file;
        if (isDirectory) {
            file.setParent(this);
            children.put(file.getName(),
                    file);
        } else {
            throw new IllegalStateException("files can only be added to a directory");
        }
    }

    @Override
    public VirtualFile asVirtualFile() {
        return this;
    }

    /**
     * Sets the parent of this file.
     *
     * @param parent the parent
     */
    public void setParent(@Nullable NewMemoryVirtualFile parent) {
        this.parent = parent;
    }

    /**
     * Sets the content of the file.
     *
     * @param content the content
     */
    public void setContent(@NotNull String content) {
        this.content = content;
    }

    /**
     * Gets the content of the file.
     *
     * @return the content of the file
     */
    @NotNull
    public String getContent() {
        return content;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    public void setFlag(int i, boolean b) {
        flags.put(i, b);
    }

    public boolean getFlag(int i) {
        Boolean b = flags.get(i);
        return b == null ? false : b;
    }

    @Override
    public NewVirtualFile getCanonicalFile() {
        return this;
    }

    @Override
    public NewVirtualFile findChildById(int i) {
        NewVirtualFile child = null;
        Collection<NewMemoryVirtualFile> files = children.values();
        for (Iterator<NewMemoryVirtualFile> it = files.iterator(); child == null && it.hasNext(); ) {
            NewMemoryVirtualFile file = it.next();
            if (file != null && file.getId() == i) {
                child = file;
            }
        }
        return child;
    }

    public void deleteChild(NewMemoryVirtualFile virtualFile) {
        children.remove(virtualFile.getName());
    }
}
