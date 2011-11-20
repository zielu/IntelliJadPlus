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

package net.stevechaloner.intellijad;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import net.stevechaloner.intellijad.actions.NavigationListener;
import net.stevechaloner.intellijad.config.Config;
import net.stevechaloner.intellijad.console.ConsoleContext;
import net.stevechaloner.intellijad.console.ConsoleEntryType;
import net.stevechaloner.intellijad.console.ConsoleManager;
import net.stevechaloner.intellijad.console.IntelliJadConsole;
import net.stevechaloner.intellijad.decompilers.DecompilationChoiceListener;
import net.stevechaloner.intellijad.decompilers.DecompilationContext;
import net.stevechaloner.intellijad.decompilers.DecompilationDescriptor;
import net.stevechaloner.intellijad.decompilers.DecompilationException;
import net.stevechaloner.intellijad.decompilers.Decompiler;
import net.stevechaloner.intellijad.decompilers.FileSystemDecompiler;
import net.stevechaloner.intellijad.decompilers.MemoryDecompiler;
import net.stevechaloner.intellijad.environment.EnvironmentContext;
import net.stevechaloner.intellijad.environment.EnvironmentValidator;
import net.stevechaloner.intellijad.environment.ValidationResult;
import net.stevechaloner.intellijad.util.PluginUtil;
import net.stevechaloner.intellijad.vfs.MemoryVirtualFileSystem;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The central component of the plugin.
 */
public class IntelliJad implements ApplicationComponent,
                                   DecompilationChoiceListener,
                                   ProjectManagerListener
{
    /**
     * The name of the component.
     */
    public static final String COMPONENT_NAME = "net.stevechaloner.intellijad.IntelliJad";

    /**
     * The manager for projects' consoles.
     */
    private final ConsoleManager consoleManager = new ConsoleManager();

    /**
     * The per-project map of closing tasks.
     */
    private final Map<Project, List<Runnable>> projectClosingTasks = new HashMap<Project, List<Runnable>>()
    {
        /**
         * Gets the list for the project.  If it doesn't exist, it's created and placed into the map.
         *
         * @param key the map key
         * @return the list
         */
        @Override
        @NotNull
        public List<Runnable> get(@NotNull Object key)
        {
            List<Runnable> list = super.get(key);
            if (list == null)
            {
                list = new ArrayList<Runnable>();
                put((Project) key,
                    list);
            }
            return list;
        }
    };

    /**
     * {@inheritDoc}
     */
    @NotNull
    public String getComponentName()
    {
        return COMPONENT_NAME;
    }

    /**
     * {@inheritDoc}
     */
    public void projectOpened(final Project project)
    {
        primeProject(project);
    }

    /**
     * Primes the project for any alterations and updates made by IntelliJad.
     *
     * @param project the project
     */
    public void primeProject(final Project project)
    {
        project.putUserData(IntelliJadConstants.GENERATED_SOURCE_LIBRARIES,
                            new ArrayList<Library>());

        NavigationListener navigationListener = new NavigationListener(project,
                                                                       this);
        FileEditorManager.getInstance(project).addFileEditorManagerListener(navigationListener);
        project.putUserData(IntelliJadConstants.DECOMPILE_LISTENER,
                            navigationListener);

        projectClosingTasks.get(project).add(new Runnable()
        {
            public void run()
            {
                List<Library> list = project.getUserData(IntelliJadConstants.GENERATED_SOURCE_LIBRARIES);
                for (Library library : list)
                {
                    Library.ModifiableModel model = library.getModifiableModel();
                    VirtualFile[] files = model.getFiles(OrderRootType.SOURCES);
                    for (VirtualFile file : files)
                    {
                        if (file.getParent() == null && IntelliJadConstants.ROOT_URI.equals(file.getUrl()))
                        {
                            model.removeRoot(file.getUrl(),
                                             OrderRootType.SOURCES);
                        }
                    }
                    if (files.length > 0)
                    {
                        model.commit();
                    }
                }
            }
        });
        project.putUserData(IntelliJadConstants.INTELLIJAD_PRIMED,
                            true);
    }

    /**
     * {@inheritDoc}
     */
    public boolean canCloseProject(Project project)
    {
        Config config = PluginUtil.getConfig(project);
        if (config.isCleanupSourceRoots())
        {
            List<Runnable> tasks = projectClosingTasks.get(project);
            for (Runnable task : tasks)
            {
                ApplicationManager.getApplication().runWriteAction(task);
            }
        }
        project.putUserData(IntelliJadConstants.INTELLIJAD_PRIMED,
                            false);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public void projectClosed(Project project)
    {
        NavigationListener listener = project.getUserData(IntelliJadConstants.DECOMPILE_LISTENER);
        FileEditorManager.getInstance(project).removeFileEditorManagerListener(listener);
        consoleManager.disposeConsole(project);
        projectClosingTasks.remove(project);
    }

    /**
     * {@inheritDoc}
     */
    public void projectClosing(final Project project)
    {
        // no-op
    }

    /**
     * {@inheritDoc}
     */
    public void initComponent()
    {
        ProjectManager.getInstance().addProjectManagerListener(this);
    }

    /**
     * {@inheritDoc}
     */
    public void disposeComponent()
    {
        ProjectManager.getInstance().removeProjectManagerListener(this);
    }

    /**
     * {@inheritDoc}
     */
    public void decompile(EnvironmentContext envContext,
                          DecompilationDescriptor descriptor)
    {
        long startTime = System.currentTimeMillis();

        Project project = envContext.getProject();

        // this allows recovery from a canProjectClose method vetoed by another manager
        Boolean isPrimed = project.getUserData(IntelliJadConstants.INTELLIJAD_PRIMED);
        if (isPrimed == null || !isPrimed)
        {
            primeProject(project);
        }

        IntelliJadConsole console = consoleManager.getConsole(project);
        ConsoleContext consoleContext = console.createConsoleContext("message.class",
                                                                     descriptor.getClassName());
        Config config = PluginUtil.getConfig(project);
        ValidationResult validationResult = EnvironmentValidator.validateEnvironment(config,
                                                                                     envContext,
                                                                                     consoleContext);
        if (!validationResult.isCancelled() && validationResult.isValid())
        {
            if (config.isDecompileToMemory())
            {
                checkSDKRoot(project);
            }
            else
            {
                LocalFileSystem lfs = (LocalFileSystem) VirtualFileManager.getInstance().getFileSystem(LocalFileSystem.PROTOCOL);
                checkSDKRoot(project,
                             lfs.findFileByPath(config.getOutputDirectory()));
            }

            StringBuilder sb = new StringBuilder();
            sb.append(config.getJadPath()).append(' ');
            sb.append(config.renderCommandLinePropertyDescriptors());
            DecompilationContext context = new DecompilationContext(project,
                                                                    consoleContext,
                                                                    sb.toString());
            Decompiler decompiler = config.isDecompileToMemory() ? new MemoryDecompiler() : new FileSystemDecompiler();
            try
            {
                VirtualFile file = decompiler.getVirtualFile(descriptor,
                                                             context);
                FileEditorManager editorManager = FileEditorManager.getInstance(project);
                if (file != null && editorManager.isFileOpen(file))
                {
                    console.closeConsole();
                    FileEditorManager.getInstance(project).closeFile(descriptor.getClassFile());
                    editorManager.openFile(file,
                                           true);
                }
                else
                {
                    file = decompiler.decompile(descriptor,
                                                context);
                    if (file != null)
                    {
                        editorManager.closeFile(descriptor.getClassFile());
                        editorManager.openFile(file,
                                               true);
                    }
                    consoleContext.addSectionMessage(ConsoleEntryType.INFO,
                                                     "message.operation-time",
                                                     System.currentTimeMillis() - startTime);
                }
            }
            catch (DecompilationException e)
            {
                consoleContext.addSectionMessage(ConsoleEntryType.ERROR,
                                                 "error",
                                                 e.getMessage());
            }
            consoleContext.close();
            checkConsole(config,
                         console,
                         consoleContext);
        }
    }


    /**
     * Checks if the project SDK has the IntelliJad source root attached, and attaches it if it is not.
     * <p>
     * This has to be done just-in-time to ensure the SDK directory index has been initialised; it can't be done in the
     * {@link IntelliJad#projectOpened} method.
     * </p>
     *
     * @param project the project
     */
    private void checkSDKRoot(final Project project)
    {
        if (project.getUserData(IntelliJadConstants.SDK_SOURCE_ROOT_ATTACHED) != Boolean.TRUE)
        {
            MemoryVirtualFileSystem vfs = (MemoryVirtualFileSystem) VirtualFileManager.getInstance().getFileSystem(
                    IntelliJadConstants.INTELLIJAD_PROTOCOL);
            checkSDKRoot(project,
                         vfs.findFileByPath(IntelliJadConstants.INTELLIJAD_ROOT));
            project.putUserData(IntelliJadConstants.SDK_SOURCE_ROOT_ATTACHED,
                                Boolean.TRUE);
        }
    }

    /**
     * Checks if the project SDK has the given source root attached, and attaches it if it is not.
     * <p>
     * This has to be done just-in-time to ensure the SDK directory index has been initialised; it can't be done in the
     * {@link IntelliJad#projectOpened} method.
     * </p>
     *
     * @param project the project
     * @param root    the source root
     */
    private void checkSDKRoot(final Project project,
                              final VirtualFile root)
    {
        ApplicationManager.getApplication().runWriteAction(new Runnable()
        {
            public void run()
            {
                final Sdk projectJdk = ProjectRootManager.getInstance(project).getProjectJdk();
                if (projectJdk != null)
                {
                    SdkModificator sdkModificator = projectJdk.getSdkModificator();
                    if (sdkModificator != null)
                    {
                        VirtualFile[] files = sdkModificator.getRoots(OrderRootType.SOURCES);
                        boolean attached = false;
                        for (int i = 0; !attached && i < files.length; i++)
                        {
                            if (files[i].equals(root))
                            {
                                attached = true;
                            }
                        }
                        if (!attached)
                        {
                            sdkModificator.addRoot(root,
                                                   OrderRootType.SOURCES);
                            sdkModificator.commitChanges();
                            project.putUserData(IntelliJadConstants.SDK_SOURCE_ROOT_ATTACHED,
                                                Boolean.TRUE);
                            
                            projectClosingTasks.get(project).add(new Runnable()
                            {
                                public void run()
                                {
                                    if (projectJdk != null)
                                    {
                                        SdkModificator sdkModificator = projectJdk.getSdkModificator();
                                        if (sdkModificator != null)
                                        {
                                            sdkModificator.removeRoot(root,
                                                                      OrderRootType.SOURCES);
                                            sdkModificator.commitChanges();
                                        }
                                    }
                                }
                            });
                        }
                    }
                }
            }
        });

    }

    /**
     * Check if the console can be closed.
     *
     * @param config         the plugin configuration
     * @param console        the console
     * @param consoleContext the console context
     */
    private void checkConsole(Config config,
                              IntelliJadConsole console,
                              ConsoleContext consoleContext)
    {
        if (consoleContext.isWorthDisplaying() || !config.isClearAndCloseConsoleOnSuccess())
        {
            console.openConsole();
        }
        else if (config.isClearAndCloseConsoleOnSuccess() && !consoleContext.isWorthDisplaying())
        {
            console.clearConsoleContent();
            console.closeConsole();
        }
    }

    /**
     * Get the logger for this plugin.
     *
     * @return the logger
     */
    public static Logger getLogger()
    {
        return Logger.getInstance(IntelliJadConstants.INTELLIJAD);
    }
}