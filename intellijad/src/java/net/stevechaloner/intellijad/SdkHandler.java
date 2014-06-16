package net.stevechaloner.intellijad;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import net.stevechaloner.intellijad.util.AppInvoker;
import org.jetbrains.annotations.NotNull;

public class SdkHandler {
    private final Logger LOG = Logger.getInstance(getClass());

    private final Application application;

    private SdkHandler(Application _application) {
        application = _application;
    }

    public static SdkHandler create(@NotNull Application application) {
        return new SdkHandler(application);
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
    public void checkSDKRoot(final Project project, final VirtualFile root) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Checking SDK root: " + project.getName() + " -> " + root.getPresentableUrl());
        }
        final Sdk projectJdk = ProjectRootManager.getInstance(project).getProjectSdk();
        if (projectJdk != null && !checkSourceRootAttached(projectJdk, root)) {
            AppInvoker.create(application).runWriteActionAndWait(new Runnable() {
                public void run() {
                    attachSourceRoot(root, project);
                }
            });
        }
    }

    private boolean checkSourceRootAttached(Sdk projectJdk, VirtualFile root) {
        VirtualFile[] files = projectJdk.getRootProvider().getFiles(OrderRootType.SOURCES);
        for (VirtualFile file : files) {
            if (file.equals(root)) {
                return true;
            }
        }
        return false;
    }

    private void attachSourceRoot(final VirtualFile root, Project project) {
        application.assertWriteAccessAllowed();
        Sdk projectJdk = ProjectRootManager.getInstance(project).getProjectSdk();
        if (projectJdk != null) {
            SdkModificator sdkModificator = projectJdk.getSdkModificator();
            sdkModificator.addRoot(root, OrderRootType.SOURCES);
            sdkModificator.commitChanges();
            IntelliJadConstants.SDK_SOURCE_ROOT_ATTACHED.set(project, true);
            addDetachClosingTask(root, project);
        }
    }

    private void addDetachClosingTask(final VirtualFile root, final Project project) {
        ProjectClosingTasks.getInstance(project).addTask(
            new Runnable() {
                public void run() {
                    Sdk projectJdk = ProjectRootManager.getInstance(project).getProjectSdk();
                    if (projectJdk != null) {
                        SdkModificator sdkModificator = projectJdk.getSdkModificator();
                        sdkModificator.removeRoot(root, OrderRootType.SOURCES);
                        sdkModificator.commitChanges();
                    }
                }
            }
        );
    }
}
