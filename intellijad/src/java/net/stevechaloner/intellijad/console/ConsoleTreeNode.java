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

import java.util.ArrayList;
import java.util.List;
import javax.swing.tree.DefaultMutableTreeNode;

import org.jetbrains.annotations.NotNull;

/**
 * @author Steve Chaloner
 */
class ConsoleTreeNode extends DefaultMutableTreeNode
{
    private final ConsoleEntryType type;

    private final String message;

    ConsoleTreeNode(@NotNull String message,
                    @NotNull ConsoleEntryType type)
    {
        this.message = message;
        this.type = type;
    }

    @NotNull
    public Object getUserObject()
    {
        return super.getUserObject();
    }

    @NotNull
    ConsoleEntryType getType()
    {
        return type;
    }

    List<ConsoleTreeNode> getChildren()
    {
        List<ConsoleTreeNode> children = new ArrayList<ConsoleTreeNode>();
        for (int i = 0; i < this.getChildCount(); i++)
        {
            children.add((ConsoleTreeNode)getChildAt(i));
        }
        return children;
    }

    /** {@inheritDoc} */
    public String toString()
    {
        return message;
    }
}
