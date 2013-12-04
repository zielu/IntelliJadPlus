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

package net.stevechaloner.idea.util.fs;

import java.awt.event.ActionEvent;

import javax.swing.JTextField;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Steve Chaloner
 */
public class ProjectFileSelectionAction extends AbstractFileSelectionAction
{
    /**
     * The project.
     */
    private final Project project;

    /**
     * @param project             the project
     * @param textField           the text field showing the user choice
     * @param selectionDescriptor the type of file chooser to be displayed
     */
    public ProjectFileSelectionAction(@NotNull Project project,
                                      @NotNull JTextField textField,
                                      @NotNull FileSelectionDescriptor selectionDescriptor)
    {
        super(textField,
              selectionDescriptor);
        this.project = project;
    }

    /** {@inheritDoc} */
    protected VirtualFile[] getFiles(@NotNull ActionEvent actionEvent,
                                     @Nullable VirtualFile existingSelection)
    {
        return FileChooser.chooseFiles(getSelectionDescriptor().getFileChooserDescriptor(),
                                       project,
                                       existingSelection);
    }
}
