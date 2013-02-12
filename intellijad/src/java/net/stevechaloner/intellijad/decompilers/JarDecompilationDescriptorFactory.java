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

import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.vfs.VirtualFile;
import net.stevechaloner.intellijad.IntelliJadResourceBundle;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Factory for creating {@link JarDecompilationDescriptor}s based on a virtual file representing the target class.
 *
 * @author Steve Chaloner
 */
class JarDecompilationDescriptorFactory extends DecompilationDescriptorFactory
{
    private static final Pattern PACKAGE_AND_CLASS_PATTERN = Pattern.compile("!(.*)");
    private static final Pattern CLASS_PATTERN = Pattern.compile("/\\w*\\.class");

    /**
     * Creates a {@link DecompilationDescriptor} for the target.
     *
     * @param target the class to decompile
     * @return a decompilation descriptor for the target
     */
    @NotNull
    public DecompilationDescriptor create(@NotNull VirtualFile target)
    {
        String path = target.getPath();
        String fqName = getFullyQualifiedName(path);
        return new JarDecompilationDescriptor(target,
                                              fqName,
                                              fqName.replace('.', '/'),
                                              getPackageName(path),
                                              getPackageNameAsPath(path),
                                              getJarFile(target));
    }

    /** {@inheritDoc} */
    public void update(@NotNull DecompilationDescriptor dd,
                       @NotNull String classContent)
    {
        // no-op
    }

    /**
     * Gets the jar file containing the target class.
     *
     * @param file the file representing the target class
     * @return the jar file
     */
    @NotNull
    private static VirtualFile getJarFile(@NotNull VirtualFile file)
    {
        VirtualFile jarFile;
        if (file.getFileType() == StdFileTypes.ARCHIVE)
        {
            jarFile = file;
        }
        else
        {
            VirtualFile parent = file.getParent();
            if (parent != null)
            {
                jarFile = getJarFile(parent);
            }
            else
            {
                throw new IllegalArgumentException(IntelliJadResourceBundle.message("error.no-jar-in-path",
                                                                                    file.getPath()));
            }
        }
        return jarFile;
    }

    /**
     * Gets the package name of the target class.
     *
     * @param path the path to the target class
     * @return the package name
     */
    @NotNull
    private static String getPackageName(@NotNull String path)
    {
        Matcher classMatcher = CLASS_PATTERN.matcher(path);
        String packageName = null;
        if (classMatcher.find())
        {
            Matcher packageAndClassMatcher = PACKAGE_AND_CLASS_PATTERN.matcher(path);
            if (packageAndClassMatcher.find())
            {
                int packageStart = packageAndClassMatcher.start() + 2;
                if (packageStart <= classMatcher.start())
                {
                    packageName = path.substring(packageStart,
                                                 classMatcher.start()).replaceAll("/", ".");
                }
            }
        }
        return packageName == null ? "" : packageName;
    }

    /**
     * Gets the fully qualified name of the class, e.g. net.stevechaloner.intellijad.IntelliJad .
     *
     * @param path the path to extract the FQ name from
     * @return the FQ name
     */
    @NotNull
    private static String getFullyQualifiedName(@NotNull String path)
    {
        Matcher packageAndClassMatcher = PACKAGE_AND_CLASS_PATTERN.matcher(path);
        String fqName = null;
        if (packageAndClassMatcher.find())
        {
            fqName = path.substring(packageAndClassMatcher.start() + 2);
            fqName = fqName.substring(0, fqName.length() - ".class".length());
            fqName = fqName.replaceAll("/", ".");
        }
        return fqName == null ? "" : fqName;
    }

    /**
     * Gets the package name as a path, e.g. net/stevechaloner/intellijad.  Note this always ends in /.
     *
     * @param path the path of the target class
     * @return the package name as a string
     */
    @NotNull
    private static String getPackageNameAsPath(@NotNull String path)
    {
        String packageName = getPackageName(path);
        packageName = packageName.replaceAll("\\.", "/");
        if (!packageName.endsWith("/"))
        {
            packageName = packageName + "/";
        }
        return packageName;
    }
}
