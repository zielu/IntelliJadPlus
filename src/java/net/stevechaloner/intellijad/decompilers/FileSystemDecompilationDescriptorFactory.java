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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.stevechaloner.intellijad.IntelliJadConstants;

import org.jetbrains.annotations.NotNull;

/**
 * Factory for creating {@link FileSystemDecompilationDescriptor}s based on a virtual file representing the target class.
 *
 * @author Steve Chaloner
 */
class FileSystemDecompilationDescriptorFactory extends DecompilationDescriptorFactory
{
    /**
     * The pattern for a package declaration within a class.
     */
    private static final Pattern PACKAGE_PATTERN = Pattern.compile("package [\\w|\\.]*;");

    /** {@inheritDoc} */
    @NotNull
    public DecompilationDescriptor create(@NotNull VirtualFile target)
    {
        return new FileSystemDecompilationDescriptor(target);
    }

    /** {@inheritDoc} */
    public void update(@NotNull DecompilationDescriptor dd,
                       @NotNull String classContent)
    {
        Matcher packageNameMatcher = PACKAGE_PATTERN.matcher(classContent);
        if (packageNameMatcher.find())
        {
            String packageName = classContent.substring("package ".length() + packageNameMatcher.start(),
                                                        packageNameMatcher.end() - 1);
            dd.setPackageName(packageName);
            String asPath = packageName.replaceAll("\\.", "/");
            if (!asPath.endsWith("/"))
            {
                asPath = asPath + '/';
            }
            dd.setPackageNameAsPath(asPath);
            dd.setFqName(packageName + '.' + dd.getClassName());
            dd.setFqNameAsPath(asPath + dd.getClassName() + IntelliJadConstants.DOT_JAVA_EXTENSION);
        }
        else
        {
            dd.setPackageName("");
            dd.setPackageNameAsPath("");
            dd.setFqName(dd.getClassName());
            dd.setFqNameAsPath(dd.getClassName() + IntelliJadConstants.DOT_JAVA_EXTENSION);
        }
    }

}
