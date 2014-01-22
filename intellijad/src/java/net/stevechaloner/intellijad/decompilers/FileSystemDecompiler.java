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

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import net.stevechaloner.intellijad.IntelliJadConstants;
import net.stevechaloner.intellijad.IntelliJadResourceBundle;
import net.stevechaloner.intellijad.config.Config;
import net.stevechaloner.intellijad.console.ConsoleContext;
import net.stevechaloner.intellijad.console.ConsoleEntryType;
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
public class FileSystemDecompiler extends MemoryDecompiler
{
    private final Logger LOG = Logger.getInstance(getClass());
    
    private static final Key<Boolean> STORE_IN_MEMORY = new Key<Boolean>("FileSystemDecompiler.store-in-memory");

    private static final Key<VirtualFile> LOCAL_FS_FILE = new Key<VirtualFile>("FileSystemDecompiler.local-fs-file");

    /** {@inheritDoc} */
    protected OperationStatus setup(DecompilationDescriptor descriptor,
                                    DecompilationContext context) throws DecompilationException
    {
        boolean debug = LOG.isDebugEnabled();

        OperationStatus status = super.setup(descriptor,
                                             context);

        Map<String, Object[]> messages = new HashMap<String, Object[]>();
        boolean storeInMemory = false;

        if (status == OperationStatus.CONTINUE)
        {
            Config config = context.getConfig();
            String outputDirPath = config.getOutputDirectory();
            if (!StringUtil.isEmptyOrSpaces(outputDirPath))
            {
                File outputDirectory = new File(outputDirPath);
                boolean outputDirExists = outputDirectory.exists();
                if (!outputDirExists && config.isCreateOutputDirectory())
                {
                    if (!outputDirectory.mkdirs())
                    {
                        storeInMemory = true;
                        messages.put("error.could-not-create-output-directory", new String[] { config.getOutputDirectory() });
                        if (debug) {
                            LOG.debug("Output directory could not be created");
                        }
                    }
                }
                else if (!outputDirExists)
                {
                    storeInMemory = true;
                    messages.put("error.non-existant-output-directory", new String[] { config.getOutputDirectory() });
                    if (debug) {
                        LOG.debug("Output directory does not exist");
                    }
                }
            }
            else
            {
                storeInMemory = true;
                messages.put("error.output-directory-not-set", new String[] { config.getOutputDirectory() });
                if (debug) {
                    LOG.debug("Directory not set");
                }
            }
        }

        context.addUserData(STORE_IN_MEMORY,
                            storeInMemory);
        if (storeInMemory)
        {
            if (debug) {
                LOG.debug("Result will be stored in memory");
            }

            ConsoleContext consoleContext = context.getConsoleContext();
            for (String key : messages.keySet())
            {
                consoleContext.addMessage(ConsoleEntryType.DECOMPILATION_OPERATION,
                                          key,
                                          messages.get(key));
            }
            consoleContext.addMessage(ConsoleEntryType.DECOMPILATION_OPERATION,
                                      "error.storing-class-in-memory",
                                      descriptor.getClassName());
            consoleContext.setWorthDisplaying(true);
        }

        return status;
    }

    /* {@inheritDoc} */
    protected VirtualFile insertIntoFileSystem(@NotNull DecompilationDescriptor descriptor,
                                               @NotNull final DecompilationContext context,
                                               @NotNull MemoryVFS vfs,
                                               @NotNull MemoryVF file)
    {
        final boolean debug = LOG.isDebugEnabled();
        
        VirtualFile insertFile;
        if (context.getUserData(STORE_IN_MEMORY))
        {
            insertFile = super.insertIntoFileSystem(descriptor, context, vfs, file);
        }
        else
        {
            if (debug) {
                LOG.debug("Inserting into local file system");
            }
            
            final LocalFileSystem lvfs = getLocalFileSystem();
            Config config = context.getConfig();
            File localPath = new File(config.getOutputDirectory() + '/' + descriptor.getPackageNameAsPath());
            
            if (debug) {
                LOG.debug("Insert into "+localPath.getAbsolutePath());
            }
            
            if (localPath.exists() & localPath.canWrite() || localPath.mkdirs())
            {
                try
                {
                    final File localFile = new File(localPath,
                                                    descriptor.getClassName() + IntelliJadConstants.DOT_JAVA_EXTENSION);
                    if (debug) {
                        LOG.debug("Insert into local file "+localFile.getAbsolutePath());
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
                    ApplicationManager.getApplication().runWriteAction(new Runnable()
                    {
                        public void run()
                        {
                            if (debug) {
                                LOG.debug("Looking for file: "+localFile.getAbsolutePath());
                            }
                            files[0] = lvfs.refreshAndFindFileByIoFile(localFile);
                            if (debug) {
                                LOG.debug("Found "+String.valueOf(files[0]));
                            }
                        }
                    });

                    insertFile = files[0];
                    context.addUserData(LOCAL_FS_FILE, files[0]);
                }
                catch (IOException e)
                {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Could not save file", e);
                    }
                    insertFile = super.insertIntoFileSystem(descriptor, context, vfs, file);
                    context.addUserData(STORE_IN_MEMORY, true);
                }
            }
            else
            {
                insertFile = super.insertIntoFileSystem(descriptor, context, vfs, file);
                context.addUserData(STORE_IN_MEMORY, true);
            }
        }

        return insertFile;
    }

    /* {@inheritDoc} */
    protected void attachSourceToLibraries(@NotNull DecompilationDescriptor descriptor,
                                           @NotNull DecompilationContext context,
                                           @NotNull MemoryVFS vfs,
                                           @NotNull List<Library> libraries)
    {
        if (context.getUserData(STORE_IN_MEMORY))
        {
            // something has occurred to make storing the file on disk a problem, so
            // keep it in memory instead
            super.attachSourceToLibraries(descriptor,
                                          context,
                                          vfs,
                                          libraries);
        }
        else
        {
            attachSource(descriptor,
                         context,
                         context.getUserData(LOCAL_FS_FILE));
        }
    }

    private VirtualFile attachSource(@NotNull final DecompilationDescriptor descriptor,
                                     @NotNull final DecompilationContext context,
                                     @NotNull final VirtualFile file)
    {
        final Project project = context.getProject();
        LocalFileSystem vfs = getLocalFileSystem();
        Config config = context.getConfig();
        File td = new File(config.getOutputDirectory());
        final VirtualFile targetDirectory = vfs.findFileByIoFile(td);

        ApplicationManager.getApplication().runWriteAction(new Runnable()
        {
            public void run()
            {
                final List<Library> libraries = LibraryUtil.findLibrariesByClass(descriptor.getFullyQualifiedName(),
                                                                                 project);
                if (!libraries.isEmpty())
                {
                    ApplicationManager.getApplication().runWriteAction(new Runnable()
                    {
                        public void run()
                        {
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

                                project.getUserData(IntelliJadConstants.GENERATED_SOURCE_LIBRARIES).add(library);
                                consoleContext.addMessage(ConsoleEntryType.LIBRARY_OPERATION,
                                                          "message.associating-source-with-library",
                                                          descriptor.getClassName(),
                                                          library.getName() == null ? IntelliJadResourceBundle.message("message.unnamed-library") : library.getName());
                            }
                        }
                    });
                }
                else
                {
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
     * @param context
     * @param file
     */
    protected void lockFile(@NotNull DecompilationContext context,
                            @NotNull MemoryVF file)
    {
        if (context.getUserData(STORE_IN_MEMORY))
        {
            super.lockFile(context, file);
        }
        else
        {
            Config config = context.getConfig();
            if (config.isReadOnly())
            {
                VirtualFile fsFile = context.getUserData(LOCAL_FS_FILE);
                new File(fsFile.getPath()).setReadOnly();
            }
        }
    }

    private LocalFileSystem getLocalFileSystem()
    {
        return (LocalFileSystem)VirtualFileManager.getInstance().getFileSystem(LocalFileSystem.PROTOCOL);
    }

    /** {@inheritDoc} */
    @Nullable
    public VirtualFile getVirtualFile(@NotNull DecompilationDescriptor descriptor,
                                      @NotNull DecompilationContext context)
    {
        VirtualFile file = null;
        if (context.containsUserData(STORE_IN_MEMORY) && context.getUserData(STORE_IN_MEMORY))
        {
            file = super.getVirtualFile(descriptor,
                                        context);
        }
        else if (context.containsUserData(LOCAL_FS_FILE))
        {
            file = context.getUserData(LOCAL_FS_FILE);
        }
        return file;
    }
}
