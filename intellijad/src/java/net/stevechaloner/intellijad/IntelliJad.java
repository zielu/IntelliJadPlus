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

import com.intellij.openapi.application.Application;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import net.stevechaloner.intellijad.actions.NavigationListener;
import net.stevechaloner.intellijad.config.Config;
import net.stevechaloner.intellijad.console.ConsoleContext;
import net.stevechaloner.intellijad.console.ConsoleManager;
import net.stevechaloner.intellijad.console.IntelliJadConsole;
import net.stevechaloner.intellijad.decompilers.DecompilationChoiceListener;
import net.stevechaloner.intellijad.decompilers.DecompilationDescriptor;
import net.stevechaloner.intellijad.decompilers.DecompilationResult;
import net.stevechaloner.intellijad.environment.EnvironmentContext;
import net.stevechaloner.intellijad.util.AppInvoker;
import net.stevechaloner.intellijad.util.FileSystemUtil;
import net.stevechaloner.intellijad.util.PluginUtil;
import net.stevechaloner.intellijad.vfs.TempMemoryVFS;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    private static final Logger LOG = Logger.getInstance(IntelliJad.class);

    private final Application application;
    private final AppInvoker appInvoker;
    
    public IntelliJad(Application _application) {
        application = _application;
        appInvoker = AppInvoker.create(application);
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    public String getComponentName()
    {
        return COMPONENT_NAME;
    }
    
    private String setupTempOutputDir(Config config, Project project, boolean enable) {
        if (enable) {
            if (config.isUseProjectSpecificSettings()) {
                if (StringUtil.isEmptyOrSpaces(config.getOutputDirectory())) {
                    String outputDir = FileSystemUtil.generateTempOutputDir(project);
                    _setupTempOutputDir(config, outputDir, enable);
                    return outputDir;
                } else {
                    return config.getOutputDirectory();
                }
            } else {
                return setupTempOutputDir(PluginUtil.getApplicationConfig(), enable);
            }
        } else {
            if (config.isUseProjectSpecificSettings()) {
                String outputDir = FileSystemUtil.generateTempOutputDir(project);
                _setupTempOutputDir(config, outputDir, enable);
            } else {
                setupTempOutputDir(PluginUtil.getApplicationConfig(), enable);
            }
            return null;
        }
    }
    
    private String setupTempOutputDir(Config config, boolean enable) {
        if (enable) {
            if (StringUtil.isEmptyOrSpaces(config.getOutputDirectory())) {
                String outputDir = FileSystemUtil.generateTempOutputDir(config);
                _setupTempOutputDir(config, outputDir, enable);
                return outputDir;
            } else {
                return config.getOutputDirectory();
            }
        } else {
            String outputDir = FileSystemUtil.generateTempOutputDir(config);
            if (outputDir.equals(config.getOutputDirectory())) {
                _setupTempOutputDir(config, outputDir, enable);    
            }
            return null;
        }
    }
    
    private void _setupTempOutputDir(Config config, String outputDir, boolean enable) {
        if (enable) {
            config.setOutputDirectory(outputDir);
            config.setCreateOutputDirectory(true);
            LOG.info("Enabled decompilation to temporary directory: "+outputDir);
        } else {
            config.setOutputDirectory("");
            config.setCreateOutputDirectory(false);
        }
    }
    
    private void forceDecompilationToDirectory(Config config, Project project) {
        if (config.isUseProjectSpecificSettings() && config.isDecompileToMemory()) {
            LOG.info("Forcing decompilation to filesystem for project");
            config.setDecompileToMemory(false);
            config.setKeepDecompiledToMemory(false);
            LOG.info("Disabled decompilation to memory for project");
            setupTempOutputDir(config, project, true);
            config.setForcedDecompileToMemory(true);
            saveAppSettings(); 
        } else {
            LOG.info("Decompilation to directory is already enabled for project");    
        }
    }
    
    private void saveAppSettings() {        
        if (!application.isUnitTestMode()) {
            LOG.info("Saving settings");            
            appInvoker.saveSettings();
        }
    }
    
    public void forceDecompilationToDirectory(Config config) {
        if (config.isDecompileToMemory()) {
            LOG.info("Forcing decompilation to filesystem");
            config.setDecompileToMemory(false);
            config.setKeepDecompiledToMemory(false);
            LOG.info("Disabled decompilation to memory");
            setupTempOutputDir(config, true);
            config.setForcedDecompileToMemory(true);
            saveAppSettings();        
        } else {
            LOG.info("Decompilation to directory is already enabled for IDE");
        }
    }
    
    private void reverseForcedDecompilationToDirectory(Config config) {
        if (config.isForcedDecompileToMemory()) {
            config.setDecompileToMemory(true);
            setupTempOutputDir(config, false);
            config.setForcedDecompileToMemory(false);
            saveAppSettings();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void projectOpened(final Project project) {
        primeProject(project);
    }

    /**
     * Primes the project for any alterations and updates made by IntelliJad.
     *
     * @param project the project
     */
    public void primeProject(final Project project)
    {
        IntelliJadConstants.GENERATED_SOURCE_LIBRARIES.set(project, new ArrayList<Library>());
        IntelliJadConstants.DECOMPILATION_DISABLED.set(project, false);
        NavigationListener navigationListener = new NavigationListener(project, this);
        project.getMessageBus().connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, navigationListener);
        IntelliJadConstants.DECOMPILE_LISTENER.set(project, navigationListener);

        ProjectClosingTasks.getInstance(project).addTask(new Runnable()
        {
            public void run()
            {
                List<Library> list = IntelliJadConstants.GENERATED_SOURCE_LIBRARIES.get(project);
                for (Library library : list) {
                    Library.ModifiableModel model = library.getModifiableModel();
                    VirtualFile[] files = model.getFiles(OrderRootType.SOURCES);
                    for (VirtualFile file : files) {
                        if (file.getParent() == null && IntelliJadConstants.ROOT_URI.equals(file.getUrl())) {
                            model.removeRoot(file.getUrl(), OrderRootType.SOURCES);
                        }
                    }
                    if (files.length > 0) {
                        model.commit();
                    }
                }
            }
        });
        IntelliJadConstants.INTELLIJAD_PRIMED.set(project, true);
    }

    /**
     * {@inheritDoc}
     */
    public boolean canCloseProject(Project project) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public void projectClosed(Project project) {
        consoleManager.disposeConsole(project);
        IntelliJadConstants.DECOMPILE_LISTENER.set(project, null);
        TempMemoryVFS.dispose(project);        
        List<Library> libraries = IntelliJadConstants.GENERATED_SOURCE_LIBRARIES.get(project);
        if (libraries != null) {
            libraries.clear();
            IntelliJadConstants.GENERATED_SOURCE_LIBRARIES.set(project, null);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void projectClosing(final Project project) {
        IntelliJadConstants.INTELLIJAD_PRIMED.set(project, false);
    }

    public void onStartup() {
        Config config = PluginUtil.getApplicationConfig();        
        forceDecompilationToDirectory(config);            
    }
    
    public void onStartup(Project project) {
        Config config = PluginUtil.getConfig(project);
        //this will reconfigure project to decompile to file system
        forceDecompilationToDirectory(config, project);        
    }
    /**
     * {@inheritDoc}
     */
    public void initComponent() {
        ProjectManager.getInstance().addProjectManagerListener(this);
    }

    /**
     * {@inheritDoc}
     */
    public void disposeComponent() {
        ProjectManager.getInstance().removeProjectManagerListener(this);
    }

    @Nullable
    VirtualFile handleDisabledVirtualFs(LocalFileSystem lfs, Config config, Project project) {
        String outputDir = config.getOutputDirectory();
        if (StringUtil.isEmptyOrSpaces(outputDir)) {
            outputDir = setupTempOutputDir(config, project, true);
        }
        VirtualFile outDirFile = lfs.findFileByPath(outputDir);
        if (outDirFile == null) {
            File targetDir = FileSystemUtil.createTargetDir(config);
            if (targetDir != null) {
                outDirFile = lfs.refreshAndFindFileByIoFile(targetDir);
            } else {
                LOG.error("Output directory creation failed: "+outputDir);
                IntelliJadConstants.DECOMPILATION_DISABLED.set(project, true);
            }
        }
        if (!IntelliJadConstants.DECOMPILATION_DISABLED.get(project, false)) {
            SdkHandler.create(application).checkSDKRoot(project, outDirFile);
        }
        return outDirFile;
    } 
    
    public static boolean isPrimed(Project project) {
        Boolean isPrimed = IntelliJadConstants.INTELLIJAD_PRIMED.get(project);
        return isPrimed != null && isPrimed;
    }
    
    /**
     * {@inheritDoc}
     */
    public Future<DecompilationResult> decompile(EnvironmentContext envContext, DecompilationDescriptor descriptor) {
        DecompilationTask task = new DecompilationTask(this, envContext, descriptor);
        task.queue();
        return task.result();
    }

    /**
     * Check if the console can be closed.
     *
     * @param config         the plugin configuration
     * @param console        the console
     * @param consoleContext the console context
     */
    void checkConsole(Config config,
                              IntelliJadConsole console,
                              ConsoleContext consoleContext) {
        if (consoleContext.isWorthDisplaying() || !config.isClearAndCloseConsoleOnSuccess()) {
            console.openConsole();
        }
        else if (config.isClearAndCloseConsoleOnSuccess() && !consoleContext.isWorthDisplaying()) {
            console.clearConsoleContent();
            console.closeConsole();
        }
    }

    /**
     * Get the logger for this plugin.
     *
     * @return the logger
     */
    public static Logger getLogger() {
        return Logger.getInstance(IntelliJadConstants.INTELLIJAD);
    }

    public ConsoleManager getConsoleManager() {
        return consoleManager;
    }

    public AppInvoker getAppInvoker() {
        return appInvoker;
    }
}
