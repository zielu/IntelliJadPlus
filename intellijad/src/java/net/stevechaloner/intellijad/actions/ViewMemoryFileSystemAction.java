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

package net.stevechaloner.intellijad.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;

import net.stevechaloner.intellijad.IntelliJadConstants;
import net.stevechaloner.intellijad.IntelliJadResourceBundle;
import net.stevechaloner.intellijad.vfs.MemoryFileSystemManager;

/**
 * Action for opening the memory VFS manager.
 */
public class ViewMemoryFileSystemAction extends AnAction
{
    /** {@inheritDoc} */
    public void actionPerformed(AnActionEvent e)
    {
        Project project = DataKeys.PROJECT.getData(e.getDataContext());
        if (project != null)
        {
            DialogBuilder builder = new DialogBuilder(project);
            builder.setTitle(IntelliJadResourceBundle.message("plugin.IntelliJad.name"));
            builder.addCloseButton();
            MemoryFileSystemManager mfsPopup = new MemoryFileSystemManager(DataKeys.PROJECT.getData(e.getDataContext()));
            builder.setCenterPanel(mfsPopup.getRoot());
            builder.setHelpId(IntelliJadConstants.CONFIGURATION_HELP_TOPIC);
            builder.setTitle(IntelliJadResourceBundle.message("message.intellijad-memory-fs-manager"));
            builder.show();
        }
    }
}
