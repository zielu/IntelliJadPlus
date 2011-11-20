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

import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Factory for creating {@link DecompilationDescriptor}s based on a virtual file representing the target class.
 *
 * @author Steve Chaloner
 */
public abstract class DecompilationDescriptorFactory
{
    /**
     * Pattern describing a class within a jar file.
     */
    private static final Pattern JARRED_CLASS_PATTERN = Pattern.compile("[.[^!]]*!(.*)");

    private static final DecompilationDescriptorFactory JAR_FACTORY = new JarDecompilationDescriptorFactory();
    private static final DecompilationDescriptorFactory FS_FACTORY = new FileSystemDecompilationDescriptorFactory();

    /**
     * Gets a {@link DecompilationDescriptorFactory} for the file.
     *
     * @param file the file to decompile
     * @return a factory for the file descriptor
     */
    @NotNull
    public static DecompilationDescriptorFactory getFactoryForFile(@NotNull VirtualFile file)
    {
        return getFactoryForFile(file.getPath());
    }

    /**
     * Gets a {@link DecompilationDescriptorFactory} for the file.
     *
     * @param file the file to decompile
     * @return a factory for the file descriptor
     */
    @NotNull
    public static DecompilationDescriptorFactory getFactoryForFile(@NotNull File file)
    {
        return getFactoryForFile(file.getPath());
    }

    /**
     * Gets a {@link DecompilationDescriptorFactory} for the file at the given path.
     *
     * @param path the path of the file to decompile
     * @return a factory for the file descriptor
     */
    @NotNull
    public static DecompilationDescriptorFactory getFactoryForFile(@NotNull String path)
    {
        Matcher isJarFile = JARRED_CLASS_PATTERN.matcher(path);
        return isJarFile.matches() ? JAR_FACTORY : FS_FACTORY;
    }


    /**
     * Creates a {@link DecompilationDescriptor} for the target.
     *
     * @param target the class to decompile
     * @return a decompilation descriptor for the target
     */
    @NotNull
    public abstract DecompilationDescriptor create(@NotNull VirtualFile target);

    /**
     * Update the decompilation descriptor to fill in any information previously unavailable.
     * @param dd the {@link DecompilationDescriptor} to update.
     * @param classContent the content of the decompiled class
     */
    public abstract void update(@NotNull DecompilationDescriptor dd,
                                @NotNull String classContent);
}
