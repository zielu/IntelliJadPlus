package net.stevechaloner.intellijad;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CountDownLatch;

/**
 * Created by Lukasz on 25.02.14.
 */
public class DecompilationTask extends Task.Backgroundable {
    private final Logger LOG = Logger.getInstance(getClass());

    private final CountDownLatch finished = new CountDownLatch(1);

    public DecompilationTask(@NotNull Project project) {
        super(project, IntelliJadResourceBundle.message("message.decompile.text.busyText"), false);
    }

    public void finish() {
        if (!isHeadless()) {
            finished.countDown();
        }
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        indicator.setFraction(0.0);
        indicator.setIndeterminate(true);
        try {
            if (!isHeadless()) {
                finished.await();
            }
        } catch (InterruptedException e) {
            LOG.error("Decompilation interrupted", e);
        } finally {
            indicator.setIndeterminate(false);
            indicator.setFraction(1.0);
        }
    }
}
