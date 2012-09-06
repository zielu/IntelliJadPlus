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

package net.stevechaloner.intellijad.console;

import com.intellij.openapi.ui.MultiLineLabelUI;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.Component;
import java.util.HashMap;
import java.util.Map;

import net.stevechaloner.intellijad.gui.IntelliJadIcon;

/**
 * Console tree node renderer to provide a more attractive view on what happened.
 * @author Steve Chaloner
 */
class ConsoleTreeCellRenderer extends DefaultTreeCellRenderer
{
    /**
     * Mapping for console entry types to icons.
     */
    private static final Map<ConsoleEntryType, Icon> ICONS = new HashMap<ConsoleEntryType, Icon>()
    {
        {
            put(ConsoleEntryType.ROOT, null);
            put(ConsoleEntryType.INTELLIJAD, IntelliJadIcon.INTELLIJAD_LOGO_12X12.get());
            put(ConsoleEntryType.JAR_OPERATION, IntelliJadIcon.ARCHIVE.get());
            put(ConsoleEntryType.DECOMPILATION_OPERATION, IntelliJadIcon.JAVA.get());
            put(ConsoleEntryType.LIBRARY_OPERATION, IntelliJadIcon.LIBRARIES.get());
            put(ConsoleEntryType.MESSAGE, IntelliJadIcon.INFO.get());
            put(ConsoleEntryType.INFO, IntelliJadIcon.INFO.get());
            put(ConsoleEntryType.ERROR, IntelliJadIcon.ERROR.get());
        }
    };

    /**
     * Initialises a new instance of this class.
     */
    public ConsoleTreeCellRenderer()
    {
        setUI(new MultiLineLabelUI());
    }

    /** {@inheritDoc} */
    public Component getTreeCellRendererComponent(JTree tree,
                                                  Object value,
                                                  boolean sel,
                                                  boolean expanded,
                                                  boolean leaf,
                                                  int row,
                                                  boolean hasFocus)
    {
        super.getTreeCellRendererComponent(tree,
                                           value,
                                           sel,
                                           expanded,
                                           leaf,
                                           row,
                                           hasFocus);
        ConsoleTreeNode node = (ConsoleTreeNode)value;
        setIcon(ICONS.get(node.getType()));
        return this;
    }
}
