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

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.openapi.vfs.VirtualFileManager;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JTextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Steve Chaloner
 */
abstract class AbstractFileSelectionAction implements ActionListener
{
    /**
     * The text field showing the user choice.
     */
    private final JTextField textField;

    /**
     * The descriptor indicating the type of file chooser to be displayed.
     */
    private final FileSelectionDescriptor selectionDescriptor;

    /**
     *
     * @param textField the text field showing the user choice
     * @param selectionDescriptor the type of file chooser to be displayed
     */
    protected AbstractFileSelectionAction(@NotNull JTextField textField,
                                          @NotNull FileSelectionDescriptor selectionDescriptor)
    {
        this.textField = textField;
        this.selectionDescriptor = selectionDescriptor;
    }

    // javadoc unnecessary
    @NotNull
    protected JTextField getTextField()
    {
        return textField;
    }

    // javadoc unnecessary
    @NotNull
    protected FileSelectionDescriptor getSelectionDescriptor()
    {
        return selectionDescriptor;
    }

    /** {@inheritDoc} */
    public void actionPerformed(ActionEvent actionEvent)
    {
        String path = textField.getText();
        VirtualFile existingSelection = null;
        if (!StringUtil.isEmptyOrSpaces(path))
        {
            existingSelection = getFileSystem().findFileByPath(path);
        }
        VirtualFile[] files = getFiles(actionEvent,
                                       existingSelection);
        if (files != null && files.length > 0)
        {
            textField.setText(files[0].getPath());
        }
    }

    private VirtualFileSystem getFileSystem()
    {
        return VirtualFileManager.getInstance().getFileSystem("file");
    }

    /**
     * Gets the selected files.
     *
     * @param actionEvent the underlying event
     * @param existingSelection the existing selection, if any
     * @return the selected virtual files
     */
    protected abstract VirtualFile[] getFiles(@NotNull ActionEvent actionEvent,
                                              @Nullable VirtualFile existingSelection);
}
