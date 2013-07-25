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

import com.intellij.openapi.help.HelpManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentFactory.SERVICE;
import net.stevechaloner.intellijad.IntelliJadConstants;
import net.stevechaloner.intellijad.IntelliJadResourceBundle;
import net.stevechaloner.intellijad.gui.IntelliJadIcon;
import net.stevechaloner.intellijad.util.PluginUtil;

import org.jetbrains.annotations.NotNull;

/**
 * The console for IntelliJad messages to the user.
 *
 * @author Steve Chaloner
 */
public class IntelliJadConsole implements NodeHandler
{
    /**
     * The tool window ID.
     */
    private static final String TOOL_WINDOW_ID = "IntelliJad Console";

    /**
     * An empty implementation of {@link Runnable} for use with tool windows.
     */
    private static final Runnable EMPTY_RUNNABLE = new Runnable()
    {
        public void run()
        {
            // no-op
        }
    };

    private final ConsoleTreeModel treeModel;
    private JPanel root;
    private JToggleButton clearAndCloseOnSuccess;
    private JButton clearButton;
    private JToolBar toolbar;
    private JButton helpButton;
    private JTree consoleTree;
    private JButton expandAll;
    private JButton collapseAll;
    private JScrollPane scrollPane;

    /**
     * Initialisation flag for JIT setup.
     */
    private boolean initialised = false;

    /**
     * The project this console reports for.
     */
    private final Project project;

    /**
     * Initialise a new instance of this class.
     *
     * @param project the project this console reports for
     */
    public IntelliJadConsole(@NotNull Project project)
    {
        this.treeModel = new ConsoleTreeModel(this);
        this.project = project;
    }

    private void jitInit()
    {
        if (!initialised)
        {
            consoleTree.setModel(treeModel);
            consoleTree.setCellRenderer(new ConsoleTreeCellRenderer());
            consoleTree.setScrollsOnExpand(true);

            toolbar.setFloatable(false);
            clearAndCloseOnSuccess.setSelected(PluginUtil.getConfig(project).isClearAndCloseConsoleOnSuccess());
            clearAndCloseOnSuccess.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    PluginUtil.getConfig(project).setClearAndCloseConsoleOnSuccess(clearAndCloseOnSuccess.isSelected());
                }
            });
            clearButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    clearConsoleContent();
                }
            });
            helpButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    HelpManager.getInstance().invokeHelp(IntelliJadConstants.CONFIGURATION_HELP_TOPIC);
                }
            });
            expandAll.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    expand(treeModel.getRootNode());
                }
            });
            collapseAll.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    collapse(treeModel.getRootNode());
                }
            });
            initialised = true;
        }
    }

    /**
     * Opens the console.
     */
    public void openConsole()
    {
        jitInit();
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);

        ToolWindow window;
        if (toolWindowManager != null)
        {
            window = toolWindowManager.getToolWindow(TOOL_WINDOW_ID);
            if (window == null)
            {
                window = toolWindowManager.registerToolWindow(TOOL_WINDOW_ID,
                                                              true,
                                                              ToolWindowAnchor.BOTTOM);
                ContentFactory contentFactory = SERVICE.getInstance();

                Content content = contentFactory.createContent(getRoot(), TOOL_WINDOW_ID, false);
                window.getContentManager().addContent(content);
            }
            window.setIcon(IntelliJadIcon.INTELLIJAD_LOGO_12X12.get());
            window.show(EMPTY_RUNNABLE);
        }
    }

    /**
     * Closes the console.
     */
    public void closeConsole()
    {
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        ToolWindow window;
        if (toolWindowManager != null)
        {
            window = toolWindowManager.getToolWindow(TOOL_WINDOW_ID);
            if (window != null)
            {
                window.hide(EMPTY_RUNNABLE);
            }
        }
    }

    /**
     * Clears the console.
     */
    public void clearConsoleContent()
    {
        treeModel.clear();
    }

    /**
     * Creates a console context for an atomic decompilation.
     *
     * @param message the associated message
     * @param parameters any parameters used in the message
     * @return a new console context
     */
    public ConsoleContext createConsoleContext(String message,
                                               Object... parameters)
    {
        return treeModel.createConsoleContext(IntelliJadResourceBundle.message(message,
                                                                               parameters));
    }

    /**
     * Get the root component of the view.
     *
     * @return the root component
     */
    public JComponent getRoot()
    {
        return root;
    }

    public void disposeConsole()
    {
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        if (toolWindowManager != null)
        {
            try
            {
                toolWindowManager.unregisterToolWindow(TOOL_WINDOW_ID);
            }
            catch (IllegalArgumentException e)
            {
                // ignore - this can occur due to lazy initialization
            }
        }
    }


    public void expand(ConsoleTreeNode node)
    {
        if (node.isLeaf() && node.getParent() != null)
        {
            consoleTree.expandPath(new TreePath(((ConsoleTreeNode)node.getParent()).getPath()));
        }
        else
        {
            for (ConsoleTreeNode child : node.getChildren())
            {
                expand(child);
            }
        }
    }

    public void collapse(ConsoleTreeNode node)
    {
        if (node.isLeaf() && node.getParent() != null)
        {
            consoleTree.collapsePath(new TreePath(((ConsoleTreeNode)node.getParent()).getPath()));
        }
        else
        {
            for (ConsoleTreeNode child : node.getChildren())
            {
                collapse(child);
            }
        }
    }

    public void select(ConsoleTreeNode node)
    {
        TreePath path = new TreePath(node.getPath());
        consoleTree.expandPath(path);
        consoleTree.setSelectionPath(path);
        consoleTree.fireTreeExpanded(path);

        List<ConsoleTreeNode> children = node.getChildren();
        consoleTree.scrollPathToVisible(!children.isEmpty() ? path.pathByAddingChild(children.get(children.size() - 1)) : path);
    }
}
