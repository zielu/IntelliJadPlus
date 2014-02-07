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

package net.stevechaloner.intellijad.decompilers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import net.stevechaloner.intellijad.IntelliJadConstants;
import net.stevechaloner.intellijad.IntelliJadResourceBundle;
import net.stevechaloner.intellijad.config.Config;
import net.stevechaloner.intellijad.console.ConsoleContext;
import net.stevechaloner.intellijad.console.ConsoleEntryType;
import net.stevechaloner.intellijad.util.AppInvoker;
import net.stevechaloner.intellijad.util.LibraryUtil;
import net.stevechaloner.intellijad.vfs.MemoryVF;
import net.stevechaloner.intellijad.vfs.MemoryVFS;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A decompiler that takes the file created by the memory decompiler and copies it to the file system.  This allows
 * for (a) a greater degree of control over what is done to the file, and (b) reduces the operations required of Jad.
 * <p>
 * If for whatever reason it's not possible to copy the file to the file system (permissions, etc) the file is placed
 * into the memory file system to make the process more robust.
 * </p>
 *
 * @author Steve Chaloner
 */
public class FileSystemDecompiler extends DecompilerBase {
    private final Logger LOG = Logger.getInstance(getClass());

    private static final Key<Boolean> CANNOT_STORE = new Key<Boolean>("FileSystemDecompiler.cannot-store-in-fs");

    private static final Key<VirtualFile> LOCAL_FS_FILE = new Key<VirtualFile>("FileSystemDecompiler.local-fs-file");

    private final AppInvoker appInvoker;

    public FileSystemDecompiler(AppInvoker appInvoker) {
        this.appInvoker = appInvoker;
    }

    /**
     * {@inheritDoc}
     */
    protected OperationStatus setup(DecompilationDescriptor descriptor,
                                    DecompilationContext context) throws DecompilationException {
        boolean debug = LOG.isDebugEnabled();

        OperationStatus status = super.setup(descriptor, context);

        Map<String, Object[]> messages = new HashMap<String, Object[]>();

        if (status == OperationStatus.CONTINUE) {
            Config config = context.getConfig();
            String outputDirPath = config.getOutputDirectory();
            if (!StringUtil.isEmptyOrSpaces(outputDirPath)) {
                File outputDirectory = new File(outputDirPath);
                boolean outputDirExists = outputDirectory.exists();
                if (!outputDirExists && config.isCreateOutputDirectory()) {
                    if (!outputDirectory.mkdirs()) {
                        messages.put("error.could-not-create-output-directory", new String[]{config.getOutputDirectory()});
                        if (debug) {
                            LOG.debug("Output directory could not be created");
                        }
                    }
                } else if (!outputDirExists) {
                    messages.put("error.non-existant-output-directory", new String[]{config.getOutputDirectory()});
                    if (debug) {
                        LOG.debug("Output directory does not exist");
                    }
                }
            } else {
                messages.put("error.output-directory-not-set", new String[]{config.getOutputDirectory()});
                if (debug) {
                    LOG.debug("Directory not set");
                }
            }
        }

        boolean cannotStore = !messages.isEmpty();
        CANNOT_STORE.set(context, cannotStore);
        if (cannotStore) {
            status = OperationStatus.ABORT;
            LOG.error("Result cannot be stored");
        }

        ConsoleContext consoleContext = context.getConsoleContext();
        for (Map.Entry<String, Object[]> entry : messages.entrySet()) {
            String errorMessage = consoleContext.addMessage(ConsoleEntryType.DECOMPILATION_OPERATION, entry.getKey(), entry.getValue());
            LOG.error(errorMessage);
        }

        if (cannotStore) {
            consoleContext.addMessage(ConsoleEntryType.DECOMPILATION_OPERATION,
                    "error.storing-class-in-fs",
                    descriptor.getClassName());
            consoleContext.setWorthDisplaying(true);
        }

        return status;
    }

    /* {@inheritDoc} */
    protected Optional<VirtualFile> insertIntoFileSystem(@NotNull DecompilationDescriptor descriptor,
                                                         @NotNull final DecompilationContext context,
                                                         @NotNull MemoryVFS vfs,
                                                         @NotNull MemoryVF file) {
        final boolean debug = LOG.isDebugEnabled();

        if (debug) {
            LOG.debug("Inserting into local file system");
        }

        final LocalFileSystem lvfs = getLocalFileSystem();
        Config config = context.getConfig();
        File localPath = new File(config.getOutputDirectory() + File.separator + descriptor.getPackageNameAsPath());

        if (debug) {
            LOG.debug("Insert into " + localPath.getAbsolutePath());
        }

        Optional<VirtualFile> insertFile;
        boolean exists = localPath.exists();
        boolean canWrite = localPath.canWrite();
        boolean mkDirs = true;
        if (!exists) {
            mkDirs = localPath.mkdirs();
        }
        if ((exists & canWrite) || mkDirs) {
            try {
                final File localFile = new File(localPath,
                        descriptor.getClassName() + IntelliJadConstants.DOT_JAVA_EXTENSION);
                if (debug) {
                    LOG.debug("Insert into local file " + localFile.getAbsolutePath());
                }
                FileWriter writer = new FileWriter(localFile);
                if (debug) {
                    LOG.debug("Writing...");
                }
                writer.write(file.getContent());
                if (debug) {
                    LOG.debug("Written");
                }
                writer.close();
                if (debug) {
                    LOG.debug("Closed");
                }
                final VirtualFile[] files = new VirtualFile[1];
                appInvoker.runWriteActionAndWait(new Runnable() {
                    public void run() {
                        if (debug) {
                            LOG.debug("Looking for file: " + localFile.getAbsolutePath());
                        }
                        files[0] = lvfs.refreshAndFindFileByIoFile(localFile);
                        if (debug) {
                            LOG.debug("Found " + String.valueOf(files[0]));
                        }
                    }
                });

                insertFile = Optional.of(files[0]);
                LOCAL_FS_FILE.set(context, Preconditions.checkNotNull(files[0]));
                if (debug) {
                    LOG.debug("Key [" + LOCAL_FS_FILE.toString() + "] is " + LOCAL_FS_FILE.get(context));
                }
            } catch (IOException e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Could not save file", e);
                }
                CANNOT_STORE.set(context, true);
                insertFile = Optional.absent();
            }
        } else {
            LOG.warn("Path: " + localPath.getAbsolutePath() + ", exists=" + exists + ", canWrite=" + canWrite + ", mkDirs=" + mkDirs);
            CANNOT_STORE.set(context, true);
            insertFile = Optional.absent();
        }

        return insertFile;
    }

    /* {@inheritDoc} */
    protected void attachSourceToLibraries(@NotNull DecompilationDescriptor descriptor,
                                           @NotNull DecompilationContext context,
                                           @NotNull MemoryVFS vfs,
                                           @NotNull List<Library> libraries) {
        if (CANNOT_STORE.get(context, false)) {
            // something has occurred to make storing the file on disk a problem            
            LOG.error("Cannot attach source: name=" + descriptor.getClassName() + ", vfs=" + vfs);
        } else {
            attachSource(descriptor, context, LOCAL_FS_FILE.get(context));
        }
    }

    private VirtualFile attachSource(@NotNull final DecompilationDescriptor descriptor,
                                     @NotNull final DecompilationContext context,
                                     @NotNull final VirtualFile file) {
        final Project project = context.getProject();
        LocalFileSystem vfs = getLocalFileSystem();
        Config config = context.getConfig();
        File td = new File(config.getOutputDirectory());
        final VirtualFile targetDirectory = vfs.findFileByIoFile(td);

        appInvoker.runWriteActionAndWait(new Runnable() {
            public void run() {
                final List<Library> libraries = LibraryUtil.findLibrariesByClass(descriptor.getFullyQualifiedName(),
                        project);
                if (!libraries.isEmpty()) {
                    ConsoleContext consoleContext = context.getConsoleContext();
                    for (Library library : libraries) {
                        String[] urls = library.getUrls(OrderRootType.SOURCES);
                        boolean found = false;
                        for (int i = 0; !found && i < urls.length; i++) {
                            found = targetDirectory.getUrl().equals(urls[i]);
                        }
                        if (!found) {
                            Library.ModifiableModel model = library.getModifiableModel();
                            model.addRoot(targetDirectory, OrderRootType.SOURCES);
                            model.commit();
                        }

                        IntelliJadConstants.GENERATED_SOURCE_LIBRARIES.get(project).add(library);
                        consoleContext.addMessage(ConsoleEntryType.LIBRARY_OPERATION,
                                "message.associating-source-with-library",
                                descriptor.getClassName(),
                                library.getName() == null ? IntelliJadResourceBundle.message("message.unnamed-library") : library.getName());
                    }
                } else {
                    // there are two reasons we could be in here - the class file was opened from an arbitrary location
                    // outside of the project, or the class is found in the SDK.  In the first instance, there is no
                    // library
                    context.getConsoleContext().addMessage(ConsoleEntryType.LIBRARY_OPERATION,
                            "message.library-not-found-for-class",
                            descriptor.getClassName());
                }
            }
        });


        return file;
    }

    /**
     * Locks the file against source changes
     *
     * @param context
     * @param file
     */
    protected void lockFile(@NotNull DecompilationContext context,
                            @NotNull MemoryVF file) {
        if (CANNOT_STORE.get(context, false)) {
            LOG.error("Cannot lock file: " + file);
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("About to lock file: " + file);
            }
            final VirtualFile fsFile = LOCAL_FS_FILE.get(context);
            appInvoker.runWriteActionAndWait(new Runnable() {
                @Override
                public void run() {
                try {
                    fsFile.setWritable(false);
                } catch (IOException e) {
                    LOG.error("Could not lock " + fsFile, e);
                }
                }
            });
            
            Config config = context.getConfig();
            if (config.isReadOnly()) {                
                File ioFile = VfsUtilCore.virtualToIoFile(fsFile);
                boolean roSet = ioFile.setReadOnly();
                
                if (!roSet && LOG.isDebugEnabled()) {
                    LOG.debug("Could not set " + ioFile.getAbsolutePath() + " as read-only");
                }
            }
        }
    }

    private LocalFileSystem getLocalFileSystem() {
        return (LocalFileSystem) VirtualFileManager.getInstance().getFileSystem(LocalFileSystem.PROTOCOL);
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    public VirtualFile getVirtualFile(@NotNull DecompilationDescriptor descriptor,
                                      @NotNull DecompilationContext context) {
        VirtualFile file;
        if (context.containsUserData(CANNOT_STORE) && context.getUserData(CANNOT_STORE)) {
            file = null;
        } else if (context.containsUserData(LOCAL_FS_FILE)) {
            file = context.getUserData(LOCAL_FS_FILE);
        } else {
            file = null;
        }
        return file;
    }
}
