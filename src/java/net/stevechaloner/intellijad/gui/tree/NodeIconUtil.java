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

package net.stevechaloner.intellijad.gui.tree;

import net.stevechaloner.intellijad.gui.IntelliJadIcons;
import net.stevechaloner.intellijad.vfs.MemoryVirtualFile;

import javax.swing.Icon;
import javax.swing.JTree;

/**
 * Utils for tree node icons.
 */
class NodeIconUtil
{
    static Icon getIconFor(JTree jTree,
                           Object value,
                           boolean expanded)
    {
        Icon icon = IntelliJadIcons.JAVA;
        if (value.equals(jTree.getModel().getRoot()))
        {
            icon = IntelliJadIcons.INTELLIJAD_LOGO_16X16;
        }
        else
        {
            if (isDirectory((VisitableTreeNode)value))
            {
                icon = expanded ? IntelliJadIcons.PACKAGE_OPEN : IntelliJadIcons.PACKAGE_CLOSED;
            }
        }
        return icon;
    }

    private static boolean isDirectory(VisitableTreeNode node)
    {
        boolean isDirectory = false;
        Object o = node.getUserObject();
        if (o instanceof CheckBoxTreeNode)
        {
            MemoryVirtualFile file = (MemoryVirtualFile)((CheckBoxTreeNode)o).getUserObject();
            isDirectory = file.isDirectory();
        }
        return isDirectory;
    }
}
