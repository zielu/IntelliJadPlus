package net.stevechaloner.intellijad.decompilers.procyon;

import com.intellij.openapi.project.Project;
import net.stevechaloner.intellijad.decompilers.DecompilationEngine;
import org.jetbrains.annotations.NotNull;

public class ProcyonEngine implements DecompilationEngine {
    @Override
    public String prepareCommand(@NotNull Project project) {
        throw new Error("Not yet implemented");
    }

    @Override
    public String waterMark() {
        throw new Error("Not yet implemented");
    }
}
