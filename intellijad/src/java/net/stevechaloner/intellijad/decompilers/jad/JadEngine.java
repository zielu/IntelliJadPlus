package net.stevechaloner.intellijad.decompilers.jad;

import com.intellij.openapi.project.Project;
import net.stevechaloner.intellijad.IntelliJadResourceBundle;
import net.stevechaloner.intellijad.config.CodeStyle;
import net.stevechaloner.intellijad.config.Config;
import net.stevechaloner.intellijad.decompilers.DecompilationEngine;
import net.stevechaloner.intellijad.util.PluginUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Lukasz on 2014-06-10.
 */
public class JadEngine implements DecompilationEngine {

    @Override
    public String prepareCommand(@NotNull Project project) {
        Config config = PluginUtil.getConfig(project);
        StringBuilder command = new StringBuilder(config.getJadPath()
                + " " +
                config.renderCommandLinePropertyDescriptors());
        command.append(" -p ");
        if (command.indexOf(" -lnc ") == -1 &&
                CodeStyle.DEBUGGABLE_STYLE.getName().equals(config.getReformatStyle())) {
            // technically it wouldn't hurt to have this present twice, but this is neater
            command.append(" -lnc ");
        }
        return command.toString();
    }

    @Override
    public String waterMark() {
        return IntelliJadResourceBundle.message("message.decompiled-through-intellijad");
    }
}
