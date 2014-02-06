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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import com.google.common.base.Optional;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.VirtualFileSystem;
import net.stevechaloner.intellijad.IntelliJadConstants;
import net.stevechaloner.intellijad.IntelliJadResourceBundle;
import net.stevechaloner.intellijad.config.CodeStyle;
import net.stevechaloner.intellijad.config.Config;
import net.stevechaloner.intellijad.console.ConsoleContext;
import net.stevechaloner.intellijad.console.ConsoleEntryType;
import net.stevechaloner.intellijad.util.LibraryUtil;
import net.stevechaloner.intellijad.vfs.LightMemoryVF;
import net.stevechaloner.intellijad.vfs.MemoryVF;
import net.stevechaloner.intellijad.vfs.MemoryVFS;
import net.stevechaloner.intellijad.vfs.TempMemoryVFS;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An in-memory decompiler that catches the piped output of Jad and
 * builds a class from it.
 *
 * @author Steve Chaloner
 */
public class MemoryDecompiler extends AbstractDecompiler
{
    private final Logger LOG = Logger.getInstance(getClass());
    
    /**
     * Initialises a new instance of this class.
     */
    public MemoryDecompiler()
    {
        setSuccessfulDecompilationAftermathHandler(new DecompilationAftermathHandler()
        {
            @Nullable
            public VirtualFile execute(@NotNull DecompilationContext context,
                                       @NotNull DecompilationDescriptor descriptor,
                                       @NotNull File targetClass,
                                       @NotNull ByteArrayOutputStream output,
                                       @NotNull ByteArrayOutputStream err) throws DecompilationException
            {
                StringBuilder sb = new StringBuilder(output.toString());
                sb.insert(0,
                          System.getProperty("line.separator"));
                sb.insert(0,
                          IntelliJadResourceBundle.message("message.decompiled-through-intellijad"));

                String content = sb.toString();
                if (DecompilationDescriptor.ClassPathType.FS == descriptor.getClassPathType())
                {
                    DecompilationDescriptorFactory.getFactoryForFile(targetClass).update(descriptor,
                                                                                         content);
                }
                return processOutput(descriptor,
                                     context,
                                     content);
            }
        });
    }

    /** {@inheritDoc} */
    protected OperationStatus setup(DecompilationDescriptor descriptor,
                                    DecompilationContext context) throws DecompilationException
    {
        return OperationStatus.CONTINUE;
    }

    /**
     *
     * @param descriptor the decompilation descriptor
     * @param context    the decompilation context
     * @param content    the content of the decompiled file
     * @return a file representing the decompiled file
     * @throws DecompilationException if the processing fails
     */
    @Nullable
    protected VirtualFile processOutput(@NotNull final DecompilationDescriptor descriptor,
                                        @NotNull final DecompilationContext context,
                                        @NotNull final String content) throws DecompilationException {
        MemoryVFS vfs = TempMemoryVFS.getInstance(context.getProject());        
        MemoryVF file = vfs.newMemoryFV(descriptor.getClassName() + IntelliJadConstants.DOT_JAVA_EXTENSION, content);
        file.asVirtualFile().putUserData(IntelliJadConstants.DECOMPILED_BY_INTELLIJAD, true);

        Optional<VirtualFile> actualFile = insertIntoFileSystem(descriptor, context, vfs, file);
        if (actualFile.isPresent()) {
            reformatToStyle(context, new LightMemoryVF(actualFile.get()));
                    
            lockFile(context, file);
    
            Project project = context.getProject();
            List<Library> libraries = LibraryUtil.findLibrariesByClass(descriptor.getFullyQualifiedName(),
                                                                       project);
    
            if (!libraries.isEmpty()) {
                attachSourceToLibraries(descriptor, context, vfs, libraries);
            } else {
                context.getConsoleContext().addMessage(ConsoleEntryType.LIBRARY_OPERATION,
                                                       "message.library-not-found-for-class",
                                                       descriptor.getClassName());
            }
    
            lockFile(context, file);
            return actualFile.get();
        } else {
            return null;
        }        
    }

    /**
     * Locks the file to prevent source code changes.
     *
     * @param context the decompilation context
     * @param file the file to lock
     */
    protected void lockFile(@NotNull DecompilationContext context,
                            @NotNull MemoryVF file)
    {
        try {
            file.setWritable(false);
        } catch (IOException e) {
            LOG.error("Error while locking file: "+file.asVirtualFile().getPath(), e);
        }
    }

    /**
     * Inserts the file into the file system.
     *
     * @param descriptor the decompilation descriptor
     * @param context the decompilation context
     * @param vfs the virtual file system
     * @param file the file to insert
     * @return the file inserted into the file system
     */
    protected Optional<VirtualFile> insertIntoFileSystem(@NotNull DecompilationDescriptor descriptor,
                                               @NotNull final DecompilationContext context,
                                               @NotNull MemoryVFS vfs,
                                               @NotNull MemoryVF file) {
        vfs.addFile(file);
        MemoryVF parentFile = "".equals(descriptor.getPackageName()) ? (MemoryVF) vfs.asVirtualFileSystem().findFileByPath(IntelliJadConstants.INTELLIJAD_ROOT) : vfs.getFileForPackage(descriptor.getPackageName());
        MemoryVF child = parentFile.addChild(file);
        return Optional.of(child.asVirtualFile());
    }

    /**
     * Attaches the decompiled source to the relevant libraries.
     *
     * @param descriptor the decompilation descriptor
     * @param context the decompilation context
     * @param vfs the memory virtual file system
     * @param libraries the libraries containing class files that match the decompiled source
     */
    protected void attachSourceToLibraries(@NotNull final DecompilationDescriptor descriptor,
                                           @NotNull final DecompilationContext context,
                                           @NotNull final MemoryVFS vfs,
                                           @NotNull final List<Library> libraries)
    {
//        CommandProcessor.getInstance().execute
        ApplicationManager.getApplication().runWriteAction(new Runnable()
        {
            public void run()
            {
                ConsoleContext consoleContext = context.getConsoleContext();
                for (Library library : libraries)
                {
                    Library.ModifiableModel model = library.getModifiableModel();
                    String[] urls = model.getUrls(OrderRootType.SOURCES);
                    boolean found = false;
                    for (int i = 0; !found && i < urls.length; i++)
                    {
                        found = IntelliJadConstants.ROOT_URI.equals(urls[i]);
                    }
                    if (!found)
                    {
                        model.addRoot(vfs.asVirtualFileSystem().findFileByPath(IntelliJadConstants.INTELLIJAD_ROOT),
                                      OrderRootType.SOURCES);
                        model.commit();
                    }
                    consoleContext.addMessage(ConsoleEntryType.LIBRARY_OPERATION,
                                              "message.associating-source-with-library",
                                              descriptor.getClassName(),
                                              library.getName() == null ? IntelliJadResourceBundle.message("message.unnamed-library") : library.getName());
                    context.getProject().getUserData(IntelliJadConstants.GENERATED_SOURCE_LIBRARIES).add(library);
                }
            }
        });
    }

    /** {@inheritDoc} */
    protected void updateCommand(StringBuilder command,
                                 Config config)
    {
        command.append(" -p ");
        if (command.indexOf(" -lnc ") == -1 &&
            CodeStyle.DEBUGGABLE_STYLE.getName().equals(config.getReformatStyle()))
        {
            // technically it wouldn't hurt to have this present twice, but this is neater
            command.append(" -lnc ");
        }
    }

    /**
     * Calculates the success of the process execution.
     *
     * @param exitCode the exit code of the process
     * @param err      the error stream of the process
     * @param output   the output of the process
     * @return a result based on the execution of the process
     */
    protected ResultType checkDecompilationStatus(int exitCode,
                                                  ByteArrayOutputStream err,
                                                  ByteArrayOutputStream output)
    {
        ResultType resultType = ResultType.SUCCESS;
        switch (exitCode)
        {
            case 0:
                if (err.size() > 0 && output.size() > 0)
                {
                    resultType = ResultType.NON_FATAL_ERROR;
                }
                else if (err.size() > 0)
                {
                    resultType = ResultType.FATAL_ERROR;
                }
                break;
            default:
                resultType = ResultType.FATAL_ERROR;

        }
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Decompilation status: "+resultType);
        }
        
        return resultType;
    }

    /** {@inheritDoc} */
    @Nullable
    public VirtualFile getVirtualFile(DecompilationDescriptor descriptor,
                                      DecompilationContext context)
    {
        String fqNameAsPath = descriptor.getFullyQualifiedNameAsPath();
        VirtualFile file = null;
        if (fqNameAsPath != null)
        {
            VirtualFileSystem vfs = VirtualFileManager.getInstance().getFileSystem(IntelliJadConstants.INTELLIJAD_PROTOCOL);
            file = vfs.findFileByPath(fqNameAsPath);

        }
        return file;
    }
}
