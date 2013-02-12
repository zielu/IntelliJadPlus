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

package net.stevechaloner.idea.util.paths;

import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Path utilities.
 * 
 * @author Steve Chaloner
 */
public class Path
{
    /**
     * Normalise the path to use a UNIX-style syntax.
     *
     * @param path the path to normalise
     * @return the normalised path
     */
    @NotNull
    public static String normalisePath(@NotNull String path)
    {
        return path.replaceAll("\\\\",
                               "/");
    }

    /**
     * Normalise the path to use a UNIX-style syntax.
     *
     * @param file the file whose path should be normalised
     * @return the normalised path
     */
    @NotNull
    public static String normalisePath(@NotNull File file)
    {
        return file.getAbsolutePath().replaceAll("\\\\",
                                                 "/");
    }
}