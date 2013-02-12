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

import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;

import org.jetbrains.annotations.Nullable;

/**
 * Tree component for displaying checkbox tree structures.
 */
public class CheckBoxTree extends JTree
{
    /**
     * Initialises a new instance of this class.
     *
     * @param model the underlying model
     */
    public CheckBoxTree(TreeModel model)
    {
        super(model);

        setCellRenderer(new CheckBoxTreeNodeRenderer());
        setCellEditor(new CheckBoxTreeNodeEditor(this));
        getModel().addTreeModelListener(new CheckBoxTreeModelListener());
    }

    /** {@inheritDoc} */
    public void setModel(TreeModel newModel)
    {
        newModel.addTreeModelListener(new CheckBoxTreeModelListener());
        super.setModel(newModel);
    }

    /**
     * Listens for changes to the tree in order to propagate selection events.
     */
    private class CheckBoxTreeModelListener implements TreeModelListener
    {
        /**
         * {@inheritDoc}
         */
        public void treeNodesChanged(TreeModelEvent e)
        {
            DefaultMutableTreeNode node = getNode(e);
            if (node != null)
            {
                CheckBoxTreeNode cbtn = (CheckBoxTreeNode) node.getUserObject();
                toggleChildren(node,
                               cbtn.isSelected());
            }
            CheckBoxTree.this.repaint(0);
        }

        /**
         * Gets the clicked node from the event.
         *
         * @param e the event
         * @return the clicked node
         */
        @Nullable
        private DefaultMutableTreeNode getNode(TreeModelEvent e)
        {
            DefaultMutableTreeNode node = null;

            Object[] children = e.getChildren();
            if (children != null && children.length > 0)
            {
                node = (DefaultMutableTreeNode) children[0];
            }
            else
            {
                Object[] path = e.getPath();
                if (path != null && path.length > 0)
                {
                    node = (DefaultMutableTreeNode) path[path.length - 1];
                }
            }

            return node;
        }

        /**
         * Toggles the selection state of all descendents of the node.
         *
         * @param node the parent node
         * @param selected the selection state
         */
        private void toggleChildren(DefaultMutableTreeNode node,
                                    boolean selected)
        {
            int count = node.getChildCount();
            for (int i = 0; i < count; i++)
            {
                DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
                CheckBoxTreeNode childCbtn = (CheckBoxTreeNode) child.getUserObject();
                childCbtn.setSelected(selected);
                toggleChildren(child,
                               selected);
            }
        }

        /**
         * {@inheritDoc}
         */
        public void treeNodesInserted(TreeModelEvent e)
        {
            // no-op
        }

        /**
         * {@inheritDoc}
         */
        public void treeNodesRemoved(TreeModelEvent e)
        {
            // no-op
        }

        /**
         * {@inheritDoc}
         */
        public void treeStructureChanged(TreeModelEvent e)
        {
            // no-op
        }
    }
}
