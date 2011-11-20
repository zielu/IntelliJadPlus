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

import net.stevechaloner.intellijad.gui.Visitable;
import net.stevechaloner.intellijad.gui.Visitor;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Visitable tree node, used for convenient tree walking.
 */
public class VisitableTreeNode extends DefaultMutableTreeNode implements Visitable
{
    /**
     * Initialises a new instance of this class.
     *
     * @param userObject the object contained in this node
     */
    public VisitableTreeNode(Object userObject)
    {
        super(userObject);
    }

    /**
     * Initialises a new instance of this class.
     *
     * @param userObject the object contained in this node
     * @param allowsChildren true iff children are allowed
     */
    public VisitableTreeNode(Object userObject,
                             boolean allowsChildren)
    {
        super(userObject,
              allowsChildren);
    }

    /** {@inheritDoc} */
    public void accept(@NotNull Visitor visitor)
    {
        visitor.visit(this);
    }
}
