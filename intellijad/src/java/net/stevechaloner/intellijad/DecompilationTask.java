package net.stevechaloner.intellijad;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import net.stevechaloner.intellijad.config.Config;
import net.stevechaloner.intellijad.console.ConsoleContext;
import net.stevechaloner.intellijad.console.ConsoleEntryType;
import net.stevechaloner.intellijad.console.ConsoleManager;
import net.stevechaloner.intellijad.console.IntelliJadConsole;
import net.stevechaloner.intellijad.decompilers.DecompilationContext;
import net.stevechaloner.intellijad.decompilers.DecompilationDescriptor;
import net.stevechaloner.intellijad.decompilers.DecompilationEngine;
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
import org.jetbrains.annotations.NotNull;

/**
 * Created by Lukasz on 25.02.14.
 */
public class DecompilationTask extends Task.Modal implements Callable<DecompilationResult> {
    private final Logger LOG = Logger.getInstance(getClass());

    private final IntelliJad intelliJad;
    private final EnvironmentContext envContext;
    private final DecompilationDescriptor descriptor;
    private final AppInvoker appInvoker;
    private final ConsoleManager consoleManager;

    private FutureTask<DecompilationResult> result;

    private ProgressIndicator indicator;

    public DecompilationTask(IntelliJad intelliJad, EnvironmentContext envContext, DecompilationDescriptor descriptor) {
        super(envContext.getProject(), IntelliJadResourceBundle.message("message.decompile.text.busyText"), false);
        this.intelliJad = intelliJad;
        this.envContext = envContext;
        this.descriptor = descriptor;
        appInvoker = intelliJad.getAppInvoker();
        consoleManager = intelliJad.getConsoleManager();

        result = new FutureTask<DecompilationResult>(this);
    }

    public Future<DecompilationResult> result() {
        return result;
    }

    private void reopenEditor(final FileEditorManager editorManager, final VirtualFile file) {
        appInvoker.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                editorManager.closeFile(descriptor.getClassFile());
                editorManager.openFile(file, true);
            }
        });
    }

    private SdkHandler sdkHandler() {
        return SdkHandler.create(ApplicationManager.getApplication());
    }
    
    @Override
    public DecompilationResult call() throws Exception {
        indicator.setFraction(0.0);
        indicator.setIndeterminate(true);

        final boolean debug = LOG.isDebugEnabled();

        long startTime = System.currentTimeMillis();
        DecompilationResult result = new DecompilationResult();
        Project project = envContext.getProject();

        // this allows recovery from a canProjectClose method vetoed by another manager
        if (!intelliJad.isPrimed(project)) {
            intelliJad.primeProject(project);
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
                intelliJad.handleDisabledVirtualFs(lfs, config, project);
            } else {
                VirtualFile outDirFile = lfs.findFileByPath(outputDir);
                if (outDirFile == null && config.isCreateOutputDirectory()) {
                    File targetDir = FileSystemUtil.createTargetDir(config);
                    if (targetDir != null) {
                        outDirFile = lfs.refreshAndFindFileByIoFile(targetDir);
                        if (debug) {
                            LOG.debug("Will decompile to created directory: "+outDirFile);
                        }
                        sdkHandler().checkSDKRoot(project, outDirFile);
                    } else {
                        if (debug) {
                            LOG.debug("Output directory creation failed");
                        }
                        intelliJad.handleDisabledVirtualFs(lfs, config, project);
                    }
                } else if (outDirFile == null) {
                    intelliJad.handleDisabledVirtualFs(lfs, config, project);
                } else {
                    sdkHandler().checkSDKRoot(project, outDirFile);
                }
            }
            if (IntelliJadConstants.DECOMPILATION_DISABLED.get(project, false)) {
                consoleContext.addSectionMessage(ConsoleEntryType.ERROR,
                        "error",
                        "Target directory "+config.getOutputDirectory()+" creation failed");
            } else {
                DecompilationEngine engine = DecompilationEngine.selector.get(project);
                DecompilationContext context = new DecompilationContext(project, consoleContext, engine);
                Decompiler decompiler = new FileSystemDecompiler(appInvoker);
                if (debug) {
                    LOG.debug("Decompiler engine in use: "+engine.getClass().getSimpleName()
                            +"/"+decompiler.getClass().getSimpleName());
                }
                try {
                    final VirtualFile file = decompiler.getVirtualFile(descriptor, context);
                    final FileEditorManager editorManager = FileEditorManager.getInstance(project);
                    if (file != null && editorManager.isFileOpen(file)) {
                        result = new DecompilationResult(file);
                        console.closeConsole();
                        reopenEditor(editorManager, file);
                    } else if (!CurrentDecompilation.isInProgress(project, descriptor)) {
                        CurrentDecompilation.set(project, descriptor);
                        final VirtualFile decompiledFile = decompiler.decompile(descriptor, context);
                        if (decompiledFile != null) {
                            result = new DecompilationResult(decompiledFile);
                            reopenEditor(editorManager, decompiledFile);
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
            intelliJad.checkConsole(config, console, consoleContext);
        }
        if (debug) {
            LOG.debug("Decompilation finished: "+descriptor.getClassFile().getPath());
        }

        indicator.setIndeterminate(false);
        indicator.setFraction(1.0);

        return result;
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        this.indicator = indicator;
        result.run();
    }
}
