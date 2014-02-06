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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.util.text.StringUtil;
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
import net.stevechaloner.intellijad.decompilers.DecompilationResult;
import net.stevechaloner.intellijad.decompilers.Decompiler;
import net.stevechaloner.intellijad.decompilers.FileSystemDecompiler;
import net.stevechaloner.intellijad.environment.EnvironmentContext;
import net.stevechaloner.intellijad.environment.EnvironmentValidator;
import net.stevechaloner.intellijad.environment.ValidationResult;
import net.stevechaloner.intellijad.util.AppInvoker;
import net.stevechaloner.intellijad.util.FileSystemUtil;
import net.stevechaloner.intellijad.util.PluginUtil;
import net.stevechaloner.intellijad.vfs.MemoryVFS;
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
            if (list == null) {
                list = new ArrayList<Runnable>();
                put((Project) key, list);
            }
            return list;
        }
    };

    private final Application application;
    private final AppInvoker appInvoker;
    
    public IntelliJad(Application application) {
        this.application = application;
        this.appInvoker = new AppInvoker(application);
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
                String outputDir = FileSystemUtil.generateTempOutputDir();
                _setupTempOutputDir(config, outputDir, enable);
                return outputDir;
            } else {
                return config.getOutputDirectory();
            }
        } else {
            String outputDir = FileSystemUtil.generateTempOutputDir();
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

        projectClosingTasks.get(project).add(new Runnable()
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
        Config config = PluginUtil.getConfig(project);
        if (config.isCleanupSourceRoots()) {
            List<Runnable> tasks = projectClosingTasks.get(project);
            for (Runnable task : tasks) {
                appInvoker.runWriteActionAndWait(task);
            }
        }
        IntelliJadConstants.INTELLIJAD_PRIMED.set(project, false);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public void projectClosed(Project project)
    {
        consoleManager.disposeConsole(project);
        projectClosingTasks.remove(project);
        IntelliJadConstants.DECOMPILE_LISTENER.set(project, null);
        MemoryVFS vfs = IntelliJadConstants.MEMORY_VFS.get(project);
        if (vfs != null) {
            IntelliJadConstants.MEMORY_VFS.set(project, null);
            vfs.dispose();            
        }
        List<Library> libraries = IntelliJadConstants.GENERATED_SOURCE_LIBRARIES.get(project);
        if (libraries != null) {
            libraries.clear();
            IntelliJadConstants.GENERATED_SOURCE_LIBRARIES.set(project, null);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void projectClosing(final Project project)
    {
        // no-op
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
    private VirtualFile handleDisabledVirtualFs(LocalFileSystem lfs, Config config, Project project) {
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
            checkSDKRoot(project, outDirFile);
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
    public DecompilationResult decompile(EnvironmentContext envContext, DecompilationDescriptor descriptor) {
        final boolean debug = LOG.isDebugEnabled();

        long startTime = System.currentTimeMillis();
        DecompilationResult result = new DecompilationResult();
        Project project = envContext.getProject();

        // this allows recovery from a canProjectClose method vetoed by another manager
        if (!isPrimed(project)) {
            primeProject(project);
        }

        IntelliJadConsole console = consoleManager.getConsole(project);
        ConsoleContext consoleContext = console.createConsoleContext("message.class",
                                                                     descriptor.getClassName());
        Config config = PluginUtil.getConfig(project);
        ValidationResult validationResult = EnvironmentValidator.validateEnvironment(config,
                                                                                     envContext,
                                                                                     consoleContext);
        
        if (!validationResult.isCancelled() && validationResult.isValid()) {            
            LocalFileSystem lfs = (LocalFileSystem) VirtualFileManager.getInstance().getFileSystem(LocalFileSystem.PROTOCOL);
            String outputDir = config.getOutputDirectory();

            if (debug) {
                LOG.debug("Will decompile to directory: "+String.valueOf(outputDir));
            }

            boolean emptyOutDir = StringUtil.isEmptyOrSpaces(outputDir);
            if (emptyOutDir) {
                if (debug) {
                    LOG.debug("Output directory not set");
                }
                handleDisabledVirtualFs(lfs, config, project);                    
            } else {
                VirtualFile outDirFile = lfs.findFileByPath(outputDir);
                if (outDirFile == null && config.isCreateOutputDirectory()) {
                    File targetDir = FileSystemUtil.createTargetDir(config);
                    if (targetDir != null) {
                        outDirFile = lfs.refreshAndFindFileByIoFile(targetDir);
                        if (debug) {
                            LOG.debug("Will decompile to created directory: "+outDirFile);
                        }
                        checkSDKRoot(project, outDirFile);
                    } else {
                        if (debug) {
                            LOG.debug("Output directory creation failed");
                        }
                        handleDisabledVirtualFs(lfs, config, project);                                                     
                    }
                } else if (outDirFile == null) {
                    handleDisabledVirtualFs(lfs, config, project);                        
                } else {
                    checkSDKRoot(project, outDirFile);
                }
            }
            if (IntelliJadConstants.DECOMPILATION_DISABLED.get(project, false)) {
                consoleContext.addSectionMessage(ConsoleEntryType.ERROR,
                                                "error",
                                                "Target directory "+config.getOutputDirectory()+" creation failed");    
            } else {
                String info = config.getJadPath() + " " + config.renderCommandLinePropertyDescriptors();                
                DecompilationContext context = new DecompilationContext(project,
                                                                        consoleContext,
                                                                        info);
                Decompiler decompiler = new FileSystemDecompiler(appInvoker);
                if (debug) {
                    LOG.debug("Decompiler in use: "+decompiler.getClass().getSimpleName());
                }
                try {
                    VirtualFile file = decompiler.getVirtualFile(descriptor, context);
                    FileEditorManager editorManager = FileEditorManager.getInstance(project);
                    if (file != null && editorManager.isFileOpen(file)) {
                        result = new DecompilationResult(file);
                        console.closeConsole();
                        editorManager.closeFile(descriptor.getClassFile());
                        editorManager.openFile(file, true);
                    } else if (!CurrentDecompilation.isInProgress(project, descriptor)) {
                        CurrentDecompilation.set(project, descriptor);
                        file = decompiler.decompile(descriptor, context);
                        if (file != null) {
                            result = new DecompilationResult(file);
                            editorManager.closeFile(descriptor.getClassFile());
                            editorManager.openFile(file, true);
                        }
                        consoleContext.addSectionMessage(ConsoleEntryType.INFO,
                                                         "message.operation-time",
                                                         System.currentTimeMillis() - startTime);
                    }
                } catch (DecompilationException e) {
                    consoleContext.addSectionMessage(ConsoleEntryType.ERROR,
                                                     "error",
                                                     e.getMessage());
                } finally {
                    CurrentDecompilation.clear(project, descriptor);
                }
            }
            consoleContext.close();
            checkConsole(config, console, consoleContext);
        }
        if (debug) {
            LOG.debug("Decompilation finished: "+descriptor.getClassFile().getPath());
        }
        return result;
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
                              final VirtualFile root) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Checking SDK root: "+project.getName()+" -> "+root.getPresentableUrl());
        }
        appInvoker.runWriteActionAndWait(new Runnable() {
            public void run() {
                final Sdk projectJdk = ProjectRootManager.getInstance(project).getProjectSdk();
                if (projectJdk != null) {
                    SdkModificator sdkModificator = projectJdk.getSdkModificator();
                    VirtualFile[] files = sdkModificator.getRoots(OrderRootType.SOURCES);
                    boolean attached = false;
                    for (int i = 0; !attached && i < files.length; i++) {
                        if (files[i].equals(root)) {
                            attached = true;
                        }
                    }
                    if (!attached) {
                        sdkModificator.addRoot(root,
                                OrderRootType.SOURCES);
                        sdkModificator.commitChanges();
                        IntelliJadConstants.SDK_SOURCE_ROOT_ATTACHED.set(project, true);

                        projectClosingTasks.get(project).add(new Runnable() {
                            public void run() {
                                if (projectJdk != null) {
                                    SdkModificator sdkModificator = projectJdk.getSdkModificator();
                                    if (sdkModificator != null) {
                                        sdkModificator.removeRoot(root, OrderRootType.SOURCES);
                                        sdkModificator.commitChanges();
                                    }
                                }
                            }
                        });
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
}
