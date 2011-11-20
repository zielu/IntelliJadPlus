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
import net.stevechaloner.intellijad.IntelliJadConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * Defines the target class and supporting data required to target a file for decompilation.
 *
 * @author Steve Chaloner
 */
public abstract class DecompilationDescriptor
{
    /**
     * The type of path the class exists on, jar file or file system.
     */
    public enum ClassPathType
    {
        JAR, FS
    }

    /**
     * The class file to decompile.
     */
    @NotNull
    private final VirtualFile classFile;

    /**
     * The fully-qualified name of the class.
     */
    @Nullable
    private String fqName;

    /**
     * The fully-qualified name of the class represented as a path, including extension.
     */
    @Nullable
    private String fqNameAsPath;

    /**
     * The name of the class.
     */
    @Nullable
    private final String className;

    /**
     * The extension of the class (typically "class").
     */
    @Nullable
    private final String extension;

    /**
     * The name of the class's package.
     */
    @Nullable
    private String packageName;

    /**
     * The class's package as a path, i.e. with / instead of .
     */
    @Nullable
    private String packageNameAsPath;

    /**
     * The path to the class file.
     */
    @NotNull
    private String path;

    /**
     * Initialises a new instance of this class.
     *
     * @param classFile         the file pointing to the target class
     * @param fqName            the fully-qualified name of the class
     * @param fqNameAsPath      the fully-qualified name of the class as a path
     * @param packageName       the package (e.g. net.stevechaloner.intellijad)
     * @param packageNameAsPath the package as a path (e.g. net/stevechaloner/intellijad/)
     */
    DecompilationDescriptor(@NotNull VirtualFile classFile,
                            @NotNull String fqName,
                            @NotNull String fqNameAsPath,
                            @NotNull String packageName,
                            @NotNull String packageNameAsPath)
    {
        this.classFile = classFile;
        this.fqName = fqName;
        setFqNameAsPath(fqNameAsPath);
        this.className = classFile.getNameWithoutExtension();
        this.extension = classFile.getExtension();
        this.packageName = packageName;
        this.packageNameAsPath = packageNameAsPath;
        this.path = classFile.getPath();
    }

    /**
     * Initialises a new instance of this class.
     *
     * @param classFile the file pointing to the target class
     */
    DecompilationDescriptor(@NotNull VirtualFile classFile)
    {
        this.classFile = classFile;
        this.path = classFile.getPath();
        this.className = classFile.getNameWithoutExtension();
        this.extension = classFile.getExtension();
    }

    @Nullable
    public abstract ClassPathType getClassPathType();

    /**
     * Gets the path to the source file.
     *
     * @param availableDirectory a directory available for temporary extractions.
     * @return the source file
     */
    @NotNull
    public abstract File getSourceFile(@NotNull File availableDirectory);

    // javadoc unnecessary
    @NotNull
    public VirtualFile getClassFile()
    {
        return classFile;
    }

    // javadoc unnecessary
    @Nullable
    public String getFullyQualifiedName()
    {
        return fqName;
    }


    @Nullable
    public String getFullyQualifiedNameAsPath()
    {
        return fqNameAsPath;
    }

    public final void setFqNameAsPath(String fqNameAsPath)
    {
        if (!fqNameAsPath.endsWith(IntelliJadConstants.DOT_JAVA_EXTENSION))
        {
            fqNameAsPath += IntelliJadConstants.DOT_JAVA_EXTENSION;
        }
        this.fqNameAsPath = fqNameAsPath;

    }

    // javadoc unnecessary
    @Nullable
    public String getClassName()
    {
        return className;
    }

    // javadoc unnecessary
    @Nullable
    public String getExtension()
    {
        return extension;
    }

    // javadoc unnecessary
    @NotNull
    public String getPath()
    {
        return path;
    }

    // javadoc unnecessary
    @Nullable
    public String getPackageName()
    {
        return packageName;
    }

    // javadoc unnecessary
    @Nullable
    public String getPackageNameAsPath()
    {
        return packageNameAsPath;
    }

    // javadoc unnecessary
    public final void setFqName(String fqName)
    {
        this.fqName = fqName;
    }

    // javadoc unnecessary
    public final void setPackageName(String packageName)
    {
        this.packageName = packageName;
    }

    // javadoc unnecessary
    public final void setPackageNameAsPath(String packageNameAsPath)
    {
        this.packageNameAsPath = packageNameAsPath;
    }
}