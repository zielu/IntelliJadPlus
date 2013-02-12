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

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * A wrapper for tree node user objects that adds selection state.
 */
public class CheckBoxTreeNode
{
    /**
     * The user object.
     */
    @NotNull
    private final Object userObject;

    /**
     * The selection state of the node.
     */
    private boolean selected = false;

    /**
     * Selection event listeners.
     */
    private final List<CheckBoxTreeNodeListener> listeners = new ArrayList<CheckBoxTreeNodeListener>();

    /**
     * Initialises a new instance of this class.
     *
     * @param userObject the user object
     */
    public CheckBoxTreeNode(@NotNull Object userObject)
    {
        this(userObject,
             false);
    }

    /**
     * Initialises a new instance of this class.
     *
     * @param userObject the user object
     * @param selected the selection state of the node
     */
    public CheckBoxTreeNode(@NotNull Object userObject,
                            boolean selected)
    {
        this.userObject = userObject;
        this.selected = selected;
    }

    /**
     * Gets the selection state of the node.
     *
     * @return true if the node is selected
     */
    public boolean isSelected()
    {
        return selected;
    }

    /**
     * Sets the selection state of the node.
     *
     * @param selected true if the node is selected
     */
    public void setSelected(boolean selected)
    {
        this.selected = selected;
        TreeEvent<CheckBoxTreeNode> e = new TreeEvent<CheckBoxTreeNode>()
        {
            public CheckBoxTreeNode getSource()
            {
                return CheckBoxTreeNode.this;
            }
        };
        for (CheckBoxTreeNodeListener listener : listeners)
        {
            if (selected)
            {
                listener.nodeSelected(e);
            }
            else
            {
                listener.nodeDeselected(e);
            }
        }
    }

    /**
     * Gets a text representation of the node, provided by the user object.
     *
     * @return a text representation
     */
    public String getText()
    {
        return userObject.toString();
    }

    /**
     * Gets the user object.
     *
     * @return the user object
     */
    @NotNull
    public Object getUserObject()
    {
        return userObject;
    }

    /** {@inheritDoc} */
    public String toString()
    {
        return getText();
    }

    /**
     * Registers a listener with this node.
     *
     * @param listener the listener
     */
    public void addListener(@NotNull CheckBoxTreeNodeListener listener)
    {
        listeners.add(listener);
    }

    public void removeListener(CheckBoxTreeNodeListener listener)
    {
        listeners.remove(listener);
    }
}
