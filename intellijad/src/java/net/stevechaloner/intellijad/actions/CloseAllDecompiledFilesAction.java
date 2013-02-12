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
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import net.stevechaloner.intellijad.IntelliJadConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Closes all files marked as being decompiled by IntelliJad.
 */
public class CloseAllDecompiledFilesAction extends AnAction
{
    /** {@inheritDoc} */
    public void update(AnActionEvent e) {
        Project project = DataKeys.PROJECT.getData(e.getDataContext());
        boolean enabled = false;
        if (project != null)
        {
            List<VirtualFile> files = new ArrayList<VirtualFile>(Arrays.asList(FileEditorManager.getInstance(project).getOpenFiles()));

            for (int i = 0; !enabled && i < files.size(); i++) {
                if (files.get(i).getUserData(IntelliJadConstants.DECOMPILED_BY_INTELLIJAD) != null)
                {
                    enabled = true;
                }
            }
        }
        e.getPresentation().setEnabled(enabled);
    }

    /** {@inheritDoc} */
    public void actionPerformed(AnActionEvent e)
    {
        Project project = DataKeys.PROJECT.getData(e.getDataContext());
        List<VirtualFile> files = new ArrayList<VirtualFile>(Arrays.asList(FileEditorManager.getInstance(project).getOpenFiles()));

        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        for (VirtualFile file : files)
        {
            if (file.getUserData(IntelliJadConstants.DECOMPILED_BY_INTELLIJAD) != null)
            {
                fileEditorManager.closeFile(file);
            }
        }
    }
}
