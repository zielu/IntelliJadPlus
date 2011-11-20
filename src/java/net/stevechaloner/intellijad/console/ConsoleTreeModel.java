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

import net.stevechaloner.intellijad.IntelliJadConstants;

import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import java.util.List;

/**
 * Tree model for the console entries, generally of a single project.
 *
 * @author Steve Chaloner
 */
class ConsoleTreeModel extends DefaultTreeModel
{
    /**
     * The node handler used to control node operations.
     */
    @NotNull
    private final NodeHandler nodeHandler;

    /**
     * Initialises a new instance of this class.
     *
     * @param nodeHandler the node handler
     */
    public ConsoleTreeModel(@NotNull NodeHandler nodeHandler)
    {
        super(new ConsoleTreeNode(IntelliJadConstants.INTELLIJAD_ROOT,
                                  ConsoleEntryType.ROOT));
        this.nodeHandler = nodeHandler;
    }

    /**
     * Inserts the given child into the given parent.
     *
     * @param child  the child
     * @param parent the parent
     */
    void insertNodeInto(@NotNull ConsoleTreeNode child,
                        @NotNull ConsoleTreeNode parent)
    {
        super.insertNodeInto(child,
                             parent,
                             parent.getChildCount());
    }

    /**
     * Gets the root node of the model.
     *
     * @return the root node
     */
    @NotNull
    ConsoleTreeNode getRootNode()
    {
        return (ConsoleTreeNode) getRoot();
    }

    /**
     * Clears all entries from the model except the root node.
     */
    void clear()
    {
        ConsoleTreeNode root = getRootNode();
        while (root.getChildCount() > 0)
        {
            MutableTreeNode child = (MutableTreeNode) root.getFirstChild();
            this.removeNodeFromParent(child);
        }
        this.nodeChanged(root);
    }

    /**
     * Creates a console context (second-level node) within the model.
     *
     * @param name the name of the context
     * @return the context
     */
    ConsoleContext createConsoleContext(@NotNull String name)
    {
        ConsoleTreeNode root = getRootNode();
        ConsoleTreeNode contextNode = new ConsoleTreeNode(name,
                                                          ConsoleEntryType.INTELLIJAD);
        this.insertNodeInto(contextNode,
                            root);
        this.nodesWereInserted(root,
                               new int[]{root.getChildCount() - 1});

        return new ConsoleContextImpl(this,
                                      contextNode,
                                      nodeHandler);
    }

    /**
     * Adds a subsection to the given context.
     *
     * @param message        the message to display as the subsection title
     * @param consoleContext the context to add the subsection to
     * @param type           the type of the subsection
     * @return a node containing the subsection
     */
    @NotNull
    private ConsoleTreeNode addSubsection(@NotNull String message,
                                          @NotNull ConsoleContext consoleContext,
                                          @NotNull ConsoleEntryType type)
    {
        ConsoleTreeNode section = consoleContext.getContextNode();
        ConsoleTreeNode subsection = new ConsoleTreeNode(message,
                                                         type);
        this.insertNodeInto(subsection,
                            section);
        this.nodesWereInserted(section,
                               new int[]{section.getChildCount() - 1});
        return subsection;
    }

    /**
     * Gets a subsection of the given type from the context.
     *
     * @param consoleContext the context containing the subsection
     * @param entryType      the entry type of the subsection
     * @return a node containing the subsection
     */
    @NotNull
    private ConsoleTreeNode getSubsection(@NotNull ConsoleContext consoleContext,
                                          @NotNull ConsoleEntryType entryType)
    {
        ConsoleTreeNode section = consoleContext.getContextNode();
        List<ConsoleTreeNode> children = section.getChildren();
        ConsoleTreeNode subsection = null;
        for (int i = 0; subsection == null && i < children.size(); i++)
        {
            ConsoleTreeNode child = children.get(i);
            if (entryType.equals(child.getType()))
            {
                subsection = child;
            }
        }
        if (subsection == null)
        {
            subsection = addSubsection(entryType.getMessage(),
                                       consoleContext,
                                       entryType);
        }
        return subsection;
    }

    /**
     * Adds a message to the indicated subsection.
     *
     * @param entryType      the entry type indicating the subsection
     * @param consoleContext the context to place the message in
     * @param message        the message
     */
    void addMessage(@NotNull ConsoleEntryType entryType,
                    @NotNull ConsoleContext consoleContext,
                    @NotNull String message)
    {
        addMessage(getSubsection(consoleContext,
                                 entryType),
                   message);
    }

    /**
     * Adds a context-level message.
     *
     * @param consoleContext the context to place the message in
     * @param message        the message
     */
    void addSectionMessage(@NotNull ConsoleContext consoleContext,
                           @NotNull String message)
    {
        addMessage(consoleContext.getContextNode(),
                   message);
    }

    /**
     * Inserts a message node into the given parent.
     *
     * @param parent  the parent
     * @param message the message
     */
    private void addMessage(@NotNull ConsoleTreeNode parent,
                            @NotNull String message)
    {
        this.insertNodeInto(new ConsoleTreeNode(message,
                                                ConsoleEntryType.MESSAGE),
                            parent);
        this.nodesWereInserted(parent,
                               new int[]{parent.getChildCount() - 1});
    }
}
