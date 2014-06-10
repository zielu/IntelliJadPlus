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
import java.util.HashMap;
import java.util.Map;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.JarFile;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import net.stevechaloner.intellijad.config.CodeStyle;
import net.stevechaloner.intellijad.config.Config;
import net.stevechaloner.intellijad.console.ConsoleContext;
import net.stevechaloner.intellijad.console.ConsoleEntryType;
import net.stevechaloner.intellijad.format.SourceReorganiser;
import net.stevechaloner.intellijad.format.StyleReformatter;
import net.stevechaloner.intellijad.util.ProcessGobbler;
import net.stevechaloner.intellijad.vfs.MemoryVF;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The generic decompilation operations required to decompile and display a class.
 *
 * @author Steve Chaloner
 */
public abstract class AbstractDecompiler implements Decompiler {
    /**
     * Operational continue/cancel flags.
     */
    protected enum OperationStatus {
        CONTINUE, ABORT
    }

    private final Logger LOG = Logger.getInstance(getClass());

    /**
     *
     */
    private final Map<ResultType, DecompilationAftermathHandler> decompilationAftermathHandlers = new HashMap<ResultType, DecompilationAftermathHandler>() {
        {
            put(ResultType.NON_FATAL_ERROR,
                    new DecompilationAftermathHandler() {
                        @Nullable
                        public VirtualFile execute(@NotNull DecompilationContext context,
                                                   @NotNull DecompilationDescriptor descriptor,
                                                   @NotNull File targetClass,
                                                   @NotNull ByteArrayOutputStream output,
                                                   @NotNull ByteArrayOutputStream err) throws DecompilationException {
                            VirtualFile file = get(ResultType.SUCCESS).execute(context,
                                    descriptor,
                                    targetClass,
                                    output,
                                    err);
                            context.getConsoleContext().addMessage(ConsoleEntryType.DECOMPILATION_OPERATION,
                                    "error",
                                    err.toString());
                            return file;
                        }
                    });
            put(ResultType.FATAL_ERROR,
                    new DecompilationAftermathHandler() {
                        @Nullable
                        public VirtualFile execute(@NotNull DecompilationContext context,
                                                   @NotNull DecompilationDescriptor descriptor,
                                                   @NotNull File targetClass,
                                                   @NotNull ByteArrayOutputStream output,
                                                   @NotNull ByteArrayOutputStream err) throws DecompilationException {
                            ConsoleContext consoleContext = context.getConsoleContext();
                            consoleContext.addMessage(ConsoleEntryType.DECOMPILATION_OPERATION,
                                    "error",
                                    err.toString());
                            consoleContext.setWorthDisplaying(true);
                            return null;
                        }
                    });
        }
    };

    /**
     * Class preparation handlers.
     */
    private final Map<DecompilationDescriptor.ClassPathType, ClassPreparer> classPreparers = new HashMap<DecompilationDescriptor.ClassPathType, ClassPreparer>() {
        {
            put(DecompilationDescriptor.ClassPathType.FS,
                    new ClassPreparer() {
                        public boolean execute(DecompilationContext context,
                                               DecompilationDescriptor descriptor) throws DecompilationException {
                            // no preparation required, class files already accessible.
                            return true;
                        }
                    });
            put(DecompilationDescriptor.ClassPathType.JAR,
                    new ClassPreparer() {
                        public boolean execute(DecompilationContext context,
                                               DecompilationDescriptor descriptor) throws DecompilationException {
                            JarDecompilationDescriptor jarDD = (JarDecompilationDescriptor) descriptor;
                            VirtualFile jarFile = jarDD.getJarFile();
                            boolean successful = false;
                            if (jarFile != null) {
                                extractClassFiles(jarFile,
                                        context,
                                        descriptor);
                                successful = true;
                            }
                            return successful;
                        }
                    });
        }
    };

    /**
     * Sets the aftermath handler for successful decompilations.
     *
     * @param handler the aftermath handler
     */
    protected void setSuccessfulDecompilationAftermathHandler(@NotNull DecompilationAftermathHandler handler) {
        decompilationAftermathHandlers.put(ResultType.SUCCESS,
                handler);
    }

    /**
     * Gets a handler for the result of the decompilation.
     *
     * @param resultType the result of the decompilation operation.
     * @return a handler
     */
    @NotNull
    protected DecompilationAftermathHandler getDecompilationAftermathHandler(@NotNull ResultType resultType) {
        return decompilationAftermathHandlers.get(resultType);
    }

    /**
     * Perform pre-compilation operations.
     *
     * @param descriptor the decompilation descriptor
     * @param context    the decompilation context
     * @return the last chance to abort the operation before decompilation
     * @throws DecompilationException if the operation fails
     */
    protected abstract OperationStatus setup(DecompilationDescriptor descriptor,
                                             DecompilationContext context) throws DecompilationException;

    /**
     * Reformats the content of the file to match the preferred code style.
     *
     * @param context the decompilation context
     * @param file    the file to reformat
     */
    protected void reformatToStyle(@NotNull final DecompilationContext context,
                                   @NotNull final MemoryVF file) {
        Config config = context.getConfig();
        switch (CodeStyle.getByName(config.getReformatStyle())) {
            case DEBUGGABLE_STYLE:
                SourceReorganiser.reorganise(context, file);
                break;
            case PREFERRED_STYLE:
            default:
                StyleReformatter.reformat(context, file.asVirtualFile());
        }
    }

    /**
     * {@inheritDoc}
     */
    public VirtualFile decompile(DecompilationDescriptor descriptor,
                                 DecompilationContext context) throws DecompilationException {
        LOG.debug("About to decompile");

        VirtualFile decompiledFile = null;
        try {
            boolean prepared = classPreparers.get(descriptor.getClassPathType()).execute(context,
                    descriptor);

            if (prepared) {
                ConsoleContext consoleContext = context.getConsoleContext();
                File targetClass = descriptor.getSourceFile(context.getTargetDirectory());

                StringBuilder command = new StringBuilder(context.getCommand());

                String path = targetClass.getAbsolutePath();
                if (path.indexOf(' ') != -1) {
                    path = "\"" + path + "\"";
                }
                command.append(path);
                consoleContext.addMessage(ConsoleEntryType.DECOMPILATION_OPERATION,
                        "message.executing-jad",
                        command.toString());

                try {
                    OperationStatus status = setup(descriptor,
                            context);
                    if (status == OperationStatus.CONTINUE) {
                        ByteArrayOutputStream output = new ByteArrayOutputStream();
                        ByteArrayOutputStream err = new ByteArrayOutputStream();
                        ResultType resultType = runExternalDecompiler(command.toString(),
                                context,
                                output,
                                err);

                        // occasionally the result will be empty - there's no point in endlessly
                        // decompiling, so it gives a couple of more chances
                        int count = 0;
                        while (output.size() == 0 && count++ < 3) {
                            consoleContext.addMessage(ConsoleEntryType.DECOMPILATION_OPERATION,
                                    "message.reexecuting-jad",
                                    command.toString());
                            resultType = runExternalDecompiler(command.toString(),
                                    context,
                                    output,
                                    err);
                        }
                        decompiledFile = getDecompilationAftermathHandler(resultType).execute(context,
                                descriptor,
                                targetClass,
                                output,
                                err);
                    }
                } catch (IOException e) {
                    throw new DecompilationException(e);
                } catch (InterruptedException e) {
                    throw new DecompilationException(e);
                }
            }
        } finally {
            FileUtil.delete(context.getTargetDirectory());
        }

        return decompiledFile;
    }

    /**
     * Run the external decompiler (i.e. Jad) to obtain the decompiled content.
     *
     * @param command the command to execute in the process
     * @param context the context of the decompilation
     * @param output  stream containing the process's output
     * @param err     stream containing the process's error output
     * @return the result of the operation
     * @throws IOException          if an IO exception occurs at any point
     * @throws InterruptedException if the stream pumping operations fail
     */
    private ResultType runExternalDecompiler(String command,
                                             DecompilationContext context,
                                             ByteArrayOutputStream output,
                                             ByteArrayOutputStream err) throws IOException,
            InterruptedException {
        boolean debug = LOG.isDebugEnabled();
        if (debug) {
            LOG.debug("Running external decompiler: " + command);
        }

        Process process = Runtime.getRuntime().exec(command);
        ProcessGobbler gobbler = new ProcessGobbler(process);

        //magic code indicating InterruptedException
        int exitCode = gobbler.waitFor(context, output, err);

        return checkDecompilationStatus(exitCode, err, output);
    }

    /**
     * Calcuates the success of the process execution.
     *
     * @param exitCode the exit code of the process
     * @param err      the error stream of the process
     * @param output   the output of the process
     * @return a result based on the execution of the process
     */
    protected abstract ResultType checkDecompilationStatus(int exitCode,
                                                           ByteArrayOutputStream err,
                                                           ByteArrayOutputStream output);

    /**
     * Extract the class files from the library to the target directory.
     *
     * @param jarFile                 the library containing the class files
     * @param context                 the context
     * @param decompilationDescriptor the decompilation descriptor
     * @throws DecompilationException if an error occurs extracting the class files
     */
    private void extractClassFiles(VirtualFile jarFile,
                                   DecompilationContext context,
                                   DecompilationDescriptor decompilationDescriptor) throws DecompilationException {
        try {
            JarFile lib = JarFileSystem.getInstance().getJarFile(jarFile);
            context.getConsoleContext().addMessage(ConsoleEntryType.JAR_OPERATION,
                    "message.examining",
                    jarFile.getPath());
            JarExtractor jarExtractor = new JarExtractor();
            jarExtractor.extract(context,
                    lib,
                    decompilationDescriptor.getPackageNameAsPath(),
                    decompilationDescriptor.getClassName());
        } catch (IOException e) {
            throw new DecompilationException(e);
        }
    }

    /**
     * Handles the output/state result of the decompilation process.
     */
    protected interface DecompilationAftermathHandler {
        /**
         * Handle the aftermath of the decompilation process.
         *
         * @param context     the decompilation context
         * @param descriptor  the decompilation descriptor
         * @param targetClass the decompiled class
         * @param output      the output of the process
         * @param err         the error stream of the process
         * @return a virtual file representing the decompiled output
         * @throws DecompilationException if something goes awry.
         */
        @Nullable
        VirtualFile execute(@NotNull DecompilationContext context,
                            @NotNull DecompilationDescriptor descriptor,
                            @NotNull File targetClass,
                            @NotNull ByteArrayOutputStream output,
                            @NotNull ByteArrayOutputStream err) throws DecompilationException;
    }

    /**
     * Prepares classes for decompilation.
     */
    private interface ClassPreparer {
        /**
         * Prepares the class for decompilation.
         *
         * @param context    the decompilation context
         * @param descriptor the descriptor of the class to decompile
         * @return true if the class was prepared successfully
         * @throws DecompilationException if the class can't be prepared
         */
        boolean execute(DecompilationContext context,
                        DecompilationDescriptor descriptor) throws DecompilationException;
    }
}
