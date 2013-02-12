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
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.vfs.VirtualFile;

import net.stevechaloner.idea.util.events.DataContextUtil;
import net.stevechaloner.intellijad.IntelliJad;
import net.stevechaloner.intellijad.IntelliJadConstants;
import net.stevechaloner.intellijad.decompilers.DecompilationDescriptor;
import net.stevechaloner.intellijad.decompilers.DecompilationDescriptorFactory;
import net.stevechaloner.intellijad.environment.EnvironmentContext;
import net.stevechaloner.intellijad.util.PluginUtil;

/**
 * Action for triggering decompilation.
 */
public class DecompileAction extends AnAction
{
    /** {@inheritDoc} */
    public void update(AnActionEvent e)
    {
        super.update(e);

        String extension = DataContextUtil.getFileExtension(e.getDataContext());
        this.getTemplatePresentation().setEnabled(extension != null && "class".equals(extension));
    }

    /** {@inheritDoc} */
    public void actionPerformed(AnActionEvent e)
    {
        DataContext dataContext = e.getDataContext();
        if (IntelliJadConstants.CLASS_EXTENSION.equals(DataContextUtil.getFileExtension(dataContext)))
        {
            IntelliJad intelliJad = PluginUtil.getComponent(IntelliJad.class);
            VirtualFile file = DataKeys.VIRTUAL_FILE.getData(e.getDataContext());
            if (file != null)
            {
                DecompilationDescriptor descriptor = DecompilationDescriptorFactory.getFactoryForFile(file).create(file);
                intelliJad.decompile(new EnvironmentContext(DataKeys.PROJECT.getData(e.getDataContext())),
                                     descriptor);
            }
        }
    }
}
