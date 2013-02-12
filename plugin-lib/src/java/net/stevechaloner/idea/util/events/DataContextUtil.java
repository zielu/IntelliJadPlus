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

package net.stevechaloner.idea.util.events;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Utility class for extracting objects from the {@link DataContext}.
 * 
 * @author Steve Chaloner
 */
public class DataContextUtil
{
    /**
     * Gets the extension of the selected file.
     *
     * @param dataContext the data context of this operation
     * @return the extension, or null if no file is selected
     */
    @Nullable
    public static String getFileExtension(@NotNull DataContext dataContext)
    {
        VirtualFile file = DataKeys.VIRTUAL_FILE.getData(dataContext);
        return file == null ? null : file.getExtension();
    }
}