package net.stevechaloner.intellijad;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.project.Project;
import java.util.List;
import net.stevechaloner.intellijad.config.Config;
import net.stevechaloner.intellijad.util.AppInvoker;
import net.stevechaloner.intellijad.util.PluginUtil;
import org.jetbrains.annotations.NotNull;

public class ProjectClosingTasks extends AbstractProjectComponent {
    /**
     * The per-project map of closing tasks.
     */
    private final List<Runnable> projectClosingTasks = Lists.newLinkedList();

    public ProjectClosingTasks(Project project) {
        super(project);
    }

    public static ProjectClosingTasks getInstance(@NotNull Project project) {
        return Preconditions.checkNotNull(project).getComponent(ProjectClosingTasks.class);
    }
    
    public void addTask(Runnable task) {
        projectClosingTasks.add(task);
    }

    @Override
    public void projectClosed() {
        Config config = PluginUtil.getConfig(myProject);
        if (config.isCleanupSourceRoots()) {
            AppInvoker appInvoker = AppInvoker.get();
            for (Runnable task : projectClosingTasks) {
                appInvoker.runWriteActionAndWait(task);
            }
        }       
    }
}
