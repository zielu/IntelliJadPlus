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
package net.stevechaloner.intellijad.vfs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.VirtualFileSystem;
import net.stevechaloner.intellijad.IntelliJad;
import net.stevechaloner.intellijad.IntelliJadConstants;
import net.stevechaloner.intellijad.IntelliJadResourceBundle;
import net.stevechaloner.intellijad.gui.Visitable;
import net.stevechaloner.intellijad.gui.Visitor;
import net.stevechaloner.intellijad.gui.tree.CheckBoxTree;
import net.stevechaloner.intellijad.gui.tree.CheckBoxTreeNode;
import net.stevechaloner.intellijad.gui.tree.CheckBoxTreeNodeListener;
import net.stevechaloner.intellijad.gui.tree.TreeEvent;
import net.stevechaloner.intellijad.gui.tree.VisitableTreeNode;
import net.stevechaloner.intellijad.util.AppInvoker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A basic management GUI for the memory file system, allowing users to see and delete files.
 */
public class MemoryFileSystemManager implements CheckBoxTreeNodeListener
{
    /**
     * The content pane.
     */
    private JPanel root;

    /**
     * The file tree.
     */
    private JTree fsTree;

    /**
     * The node expansion button.
     */
    private JButton expandButton;

    /**
     * The node collapse button.
     */
    private JButton collapseButton;

    /**
     * The node delete button.
     */
    private JButton deleteButton;

    /**
     * Button for attaching the memory VFS to the SDK source root.
     */
    private JButton attachIntelliJadRootButton;

    /**
     * Button for detaching the memory VFS from the SDK source root.
     */
    private JButton detachIntelliJadRootButton;

    /**
     * Used to display total/selected file sizes.
     */
    private JLabel fileSizeLabel;

    /**
     * Initialises a new instance of this
     * @param project the associated project, if any
     */
    public MemoryFileSystemManager(@Nullable final Project project)
    {
        expandButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                Object o = fsTree.getLastSelectedPathComponent();
                expand(o == null ? (VisitableTreeNode)fsTree.getModel().getRoot() : (VisitableTreeNode)o);
            }
        });
        collapseButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                Object o = fsTree.getLastSelectedPathComponent();
                collapse(o == null ? (VisitableTreeNode)fsTree.getModel().getRoot() : (VisitableTreeNode)o);
            }
        });
        deleteButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                delete(project);
            }
        });

        if (project == null)
        {
            attachIntelliJadRootButton.setEnabled(false);
            detachIntelliJadRootButton.setEnabled(false);
        }
        else
        {
            initSourceRootControls(project);
        }

        updateFileSizeInfo();
    }

    /**
     * Initialises the controls to attach/detach the memory VFS root as
     * a source root to the project SDK.
     *
     * @param project the project
     */
    private void initSourceRootControls(@NotNull final Project project)
    {
        boolean attached = false;
        VirtualFileSystem vfs = (VirtualFileSystem) VirtualFileManager.getInstance().getFileSystem(IntelliJadConstants.INTELLIJAD_PROTOCOL);
        final VirtualFile sourceRoot = vfs.findFileByPath(IntelliJadConstants.INTELLIJAD_ROOT);
        Sdk projectJdk = ProjectRootManager.getInstance(project).getProjectSdk();
        final SdkModificator sdkModificator = (projectJdk != null) ? projectJdk.getSdkModificator() : null;
        if (sdkModificator != null)
        {
            VirtualFile[] files = sdkModificator.getRoots(OrderRootType.SOURCES);
            for (int i = 0; !attached && i < files.length; i++)
            {
                if (files[i].equals(sourceRoot))
                {
                    attached = true;
                }
            }
        }

        attachIntelliJadRootButton.setEnabled(!attached);
        detachIntelliJadRootButton.setEnabled(attached);

        final AppInvoker appInvoker = AppInvoker.get();
        detachIntelliJadRootButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                appInvoker.runWriteActionAndWait(new Runnable()
                {
                    public void run()
                    {
                        if (sdkModificator != null)
                        {
                            sdkModificator.removeRoot(sourceRoot,
                                                      OrderRootType.SOURCES);
                            sdkModificator.commitChanges();
                        }
                    }
                });
                attachIntelliJadRootButton.setEnabled(true);
                detachIntelliJadRootButton.setEnabled(false);
                IntelliJadConstants.SDK_SOURCE_ROOT_ATTACHED.set(project, true);
            }
        });
        attachIntelliJadRootButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                appInvoker.runWriteActionAndWait(new Runnable()
                {
                    public void run()
                    {
                        if (sdkModificator != null)
                        {
                            sdkModificator.addRoot(sourceRoot,
                                                   OrderRootType.SOURCES);
                            sdkModificator.commitChanges();
                        }
                    }
                });
                attachIntelliJadRootButton.setEnabled(false);
                detachIntelliJadRootButton.setEnabled(true);
                IntelliJadConstants.SDK_SOURCE_ROOT_ATTACHED.set(project, false);                
            }
        });
    }

    /**
     * Pending user confirmation, delete selected files from the memory file system.  All associated editors
     * are closed in the process.
     *
     * @param project the project
     */
    private void delete(@Nullable Project project)
    {
        List<MemoryVF> files = getSelectedFiles();
        if (files.size() > 0)
        {
            int option = JOptionPane.showConfirmDialog(root,
                                                       IntelliJadResourceBundle.message("message.confirm-delete-memory"),
                                                       IntelliJadResourceBundle.message("message.confirm-delete"),
                                                       JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION)
            {
                try
                {
                    MemoryVFS vfs = (MemoryVFS) VirtualFileManager.getInstance().getFileSystem(IntelliJadConstants.INTELLIJAD_PROTOCOL);
                    if (project != null)
                    {
                        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
                        for (MemoryVF file : files)
                        {
                            fileEditorManager.closeFile(file.asVirtualFile());
                            vfs.deleteFile(this, file.asVirtualFile());
                        }
                    }
                    else
                    {
                        for (MemoryVF file : files)
                        {
                            vfs.deleteFile(this, file.asVirtualFile());
                        }
                    }
                    rebuildTreeModel();
                }
                catch (IOException ioe)
                {
                    IntelliJad.getLogger().error(ioe);
                }
            }
            updateFileSizeInfo();
        }
    }

    /**
     * Get the selected files from the tree.  They will be ordered deepest-first, to ensure
     * files lower down in the system are deleted before their ancestors.
     *
     * @return a list of selected files
     */
    @NotNull
    private List<MemoryVF> getSelectedFiles()
    {
        List<MemoryVF> files = new ArrayList<MemoryVF>();

        VisitableTreeNode root = (VisitableTreeNode)fsTree.getModel().getRoot();
        int count = root.getChildCount();
        for (int i = 0; i < count; i++)
        {
            getSelectedFiles((VisitableTreeNode)root.getChildAt(i),
                             files);
        }

        Collections.sort(files,
                         new Comparator<MemoryVF>() {
                             public int compare(MemoryVF o1,
                                                MemoryVF o2)
                             {
                                 return o1.asVirtualFile().getPath().compareTo(o2.asVirtualFile().getPath());
                             }
                         });
        Collections.reverse(files);
        return files;
    }

    /**
     * Recursively gets all selected files from the given node.
     *
     * @param node the root node
     * @param files the list of files
     */
    private void getSelectedFiles(VisitableTreeNode node,
                                  List<MemoryVF> files)
    {
        CheckBoxTreeNode cbtn = (CheckBoxTreeNode)node.getUserObject();
        if (cbtn.isSelected())
        {
            files.add((MemoryVF)cbtn.getUserObject());
        }
        int count = node.getChildCount();
        for (int i = 0; i < count; i++)
        {
            getSelectedFiles((VisitableTreeNode)node.getChildAt(i),
                             files);
        }
    }

    /**
     * Fully expand the given tree node.
     *
     * @param node the node to expand
     */
    private void expand(VisitableTreeNode node)
    {
        if (node.isLeaf() && node.getParent() != null)
        {
            fsTree.expandPath(new TreePath(((VisitableTreeNode)node.getParent()).getPath()));
        }
        else
        {
            int count = node.getChildCount();
            for (int i = 0; i < count; i++)
            {
                expand((VisitableTreeNode)node.getChildAt(i));
            }
        }
    }

    /**
     * Fully collapses the given tree node.
     *
     * @param node the node to collapse
     */
    public void collapse(VisitableTreeNode node)
    {
        int count = node.getChildCount();
        for (int i = 0; i < count; i++)
        {
            collapse((VisitableTreeNode)node.getChildAt(i));
        }
        fsTree.collapsePath(new TreePath(node.getPath()));
    }

    /**
     * Recursively populates the tree representing the memory file system.
     *
     * @param node the current node
     */
    private void populateChildren(VisitableTreeNode node)
    {
        CheckBoxTreeNode cbtn = (CheckBoxTreeNode)node.getUserObject();
        VirtualFile file = (VirtualFile)cbtn.getUserObject();
        for (VirtualFile childFile : file.getChildren())
        {
            CheckBoxTreeNode childPayload = new CheckBoxTreeNode(childFile);
            childPayload.addListener(this);
            VisitableTreeNode child = new VisitableTreeNode(childPayload);
            node.add(child);
            populateChildren(child);
        }
    }

    /**
     * Gets the content pane containing the manager GUI.
     *
     * @return the content pane
     */
    public JPanel getRoot()
    {
        return root;
    }

    private void createUIComponents()
    {
        rebuildTreeModel();
    }

    private void updateFileSizeInfo()
    {
        VisitableTreeNode root = (VisitableTreeNode)fsTree.getModel().getRoot();
        FileByteCounter fileByteCounter = new FileByteCounter();
        root.accept(fileByteCounter);

        SelectedByteCounter selectedByteCounter = new SelectedByteCounter();
        root.accept(selectedByteCounter);

        StringBuilder sb = new StringBuilder();
        sb.append(IntelliJadResourceBundle.message("message.total-file-size"));
        sb.append(fileByteCounter.getByteCount());
        sb.append("b     ");
        sb.append(IntelliJadResourceBundle.message("message.selected-file-size"));
        sb.append(selectedByteCounter.getByteCount());
        sb.append('b');
        fileSizeLabel.setText(sb.toString());
    }

    /**
     * (Re)builds the tree model based on the memory file system.
     */
    private void rebuildTreeModel()
    {
        VirtualFileSystem vfs = VirtualFileManager.getInstance().getFileSystem(IntelliJadConstants.INTELLIJAD_PROTOCOL);
        VirtualFile rootFile = vfs.findFileByPath(IntelliJadConstants.INTELLIJAD_ROOT);

        CheckBoxTreeNode rootPayload = new CheckBoxTreeNode(rootFile);
        rootPayload.addListener(this);
        VisitableTreeNode root = new VisitableTreeNode(rootPayload);
        populateChildren(root);
        DefaultTreeModel model = new DefaultTreeModel(root);

        if (fsTree == null)
        {
            fsTree = new CheckBoxTree(model);
        }
        else
        {
            fsTree.setModel(model);
        }
    }

    /** {@inheritDoc} */
    public void nodeSelected(TreeEvent<CheckBoxTreeNode> e)
    {
        updateFileSizeInfo();
    }

    /** {@inheritDoc} */
    public void nodeDeselected(TreeEvent<CheckBoxTreeNode> e)
    {
        updateFileSizeInfo();
    }

    /**
     * Counts the bytes in every file encountered.
     */
    private class FileByteCounter implements Visitor
    {
        private long byteCount;

        public void visit(@NotNull Visitable visitable)
        {
            VisitableTreeNode node = (VisitableTreeNode)visitable;
            CheckBoxTreeNode cbtn = (CheckBoxTreeNode)node.getUserObject();
            if (cbtn != null)
            {
                byteCount += getByteCountForNode(cbtn);
            }

            for (Enumeration children = node.children(); children.hasMoreElements();)
            {
                ((VisitableTreeNode)children.nextElement()).accept(this);
            }
        }

        protected long getByteCountForNode(CheckBoxTreeNode node)
        {
            VirtualFile file = (VirtualFile) node.getUserObject();
            long bc = 0;
            if (file != null)
            {
                bc = file.getLength();
            }
            return bc;
        }

        long getByteCount()
        {
            return byteCount;
        }
    }

    private class SelectedByteCounter extends FileByteCounter
    {
        protected long getByteCountForNode(CheckBoxTreeNode node) {
            long byteCount = 0;
            if (node.isSelected())
            {
                byteCount = super.getByteCountForNode(node);
            }
            return byteCount;
        }
    }
}
