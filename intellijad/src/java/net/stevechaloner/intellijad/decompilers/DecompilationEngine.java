package net.stevechaloner.intellijad.decompilers;

import com.intellij.openapi.project.Project;
import net.stevechaloner.intellijad.decompilers.jad.JadEngine;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Lukasz on 2014-06-10.
 */
public interface DecompilationEngine {
    public final Selector selector = new Selector();

    String prepareCommand(@NotNull Project project);
    String waterMark();

    public static final class Selector {
        private Selector() {}

        public DecompilationEngine get(@NotNull Project project) {
            return new JadEngine();
        }
    }
}
