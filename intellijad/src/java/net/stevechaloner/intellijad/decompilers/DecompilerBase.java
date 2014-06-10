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
import java.util.List;

import com.google.common.base.Optional;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.vfs.VirtualFile;
import net.stevechaloner.intellijad.IntelliJadConstants;
import net.stevechaloner.intellijad.IntelliJadResourceBundle;
import net.stevechaloner.intellijad.config.CodeStyle;
import net.stevechaloner.intellijad.config.Config;
import net.stevechaloner.intellijad.console.ConsoleEntryType;
import net.stevechaloner.intellijad.util.LibraryUtil;
import net.stevechaloner.intellijad.util.OsUtil;
import net.stevechaloner.intellijad.vfs.LightMemoryVF;
import net.stevechaloner.intellijad.vfs.MemoryVF;
import net.stevechaloner.intellijad.vfs.MemoryVFS;
import net.stevechaloner.intellijad.vfs.TempMemoryVFS;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Common decompilation logic.
 *
 * @author Steve Chaloner
 */
public abstract class DecompilerBase extends AbstractDecompiler {
    private final Logger LOG = Logger.getInstance(getClass());

    /**
     * Initialises a new instance of this class.
     */
    public DecompilerBase() {
        setSuccessfulDecompilationAftermathHandler(new DecompilationAftermathHandler() {
            @Nullable
            public VirtualFile execute(@NotNull DecompilationContext context,
                                       @NotNull DecompilationDescriptor descriptor,
                                       @NotNull File targetClass,
                                       @NotNull ByteArrayOutputStream output,
                                       @NotNull ByteArrayOutputStream err) throws DecompilationException {
                StringBuilder sb = new StringBuilder(output.toString());
                sb.insert(0, OsUtil.lineSeparator());
                sb.insert(0, context.getEngine().waterMark());
                String content = sb.toString();
                if (DecompilationDescriptor.ClassPathType.FS == descriptor.getClassPathType()) {
                    DecompilationDescriptorFactory.getFactoryForFile(targetClass).update(descriptor,
                            content);
                }
                return processOutput(descriptor,
                        context,
                        content);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    protected OperationStatus setup(DecompilationDescriptor descriptor,
                                    DecompilationContext context) throws DecompilationException {
        return OperationStatus.CONTINUE;
    }

    /**
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
        IntelliJadConstants.DECOMPILED_BY_INTELLIJAD.set(file.asVirtualFile(), true);

        Optional<VirtualFile> actualFile = insertIntoFileSystem(descriptor, context, file);
        if (actualFile.isPresent()) {
            reformatToStyle(context, new LightMemoryVF(actualFile.get()));

            lockFile(context, file);

            Project project = context.getProject();
            List<Library> libraries = LibraryUtil.findLibrariesByClass(descriptor.getFullyQualifiedName(),
                    project);

            if (!libraries.isEmpty()) {
                attachSourceToLibraries(descriptor, context, libraries);
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
     * @param file    the file to lock
     */
    protected abstract void lockFile(@NotNull DecompilationContext context, @NotNull MemoryVF file);

    /**
     * Inserts the file into the file system.
     *
     * @param descriptor the decompilation descriptor
     * @param context    the decompilation context
     * @param file       the file to insert
     * @return the file inserted into the file system
     */
    protected abstract Optional<VirtualFile> insertIntoFileSystem(@NotNull DecompilationDescriptor descriptor,
                                                         @NotNull DecompilationContext context,
                                                         @NotNull MemoryVF file);

    /**
     * Attaches the decompiled source to the relevant libraries.
     *
     * @param descriptor the decompilation descriptor
     * @param context    the decompilation context
     * @param libraries  the libraries containing class files that match the decompiled source
     */
    protected abstract void attachSourceToLibraries(@NotNull final DecompilationDescriptor descriptor,
                                           @NotNull final DecompilationContext context,
                                           @NotNull final List<Library> libraries);

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
                                                  ByteArrayOutputStream output) {
        ResultType resultType = ResultType.SUCCESS;
        switch (exitCode) {
            case 0:
                if (err.size() > 0 && output.size() > 0) {
                    resultType = ResultType.NON_FATAL_ERROR;
                } else if (err.size() > 0) {
                    resultType = ResultType.FATAL_ERROR;
                }
                break;
            default:
                resultType = ResultType.FATAL_ERROR;

        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Decompilation status: " + resultType);
        }

        return resultType;
    }
}
