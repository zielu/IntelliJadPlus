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
package net.stevechaloner.intellijad.console;

import com.intellij.openapi.project.Project;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

/**
 * Manages consoles on a per-project basis.
 */
public class ConsoleManager {

    /**
     * The consoles.
     */
    private final Map<Project, IntelliJadConsole> consoles = new HashMap<Project, IntelliJadConsole>();

    /**
     * Creates and binds a console to the given project.
     *
     * @param project the project to bind the console to
     * @return the new console
     */
    @NotNull
    private IntelliJadConsole addConsole(@NotNull Project project)
    {
        IntelliJadConsole console = new IntelliJadConsole(project);
        consoles.put(project,
                     console);
        return console;
    }

    /**
     * Gets the console for the project.  If one doesn't exist, it is created
     * and bound.
     *
     * @param project the project whose console is required
     * @return the console
     */
    @NotNull
    public IntelliJadConsole getConsole(@NotNull Project project)
    {
        IntelliJadConsole console = consoles.get(project);
        if (console == null)
        {
            console = addConsole(project);
        }
        return console;
    }

    /**
     * Removes the console from the manager, and disposes of it.
     *
     * @param project the project whose console should be removed
     */
    public void disposeConsole(@NotNull Project project)
    {
        synchronized (consoles)
        {
            IntelliJadConsole console = consoles.get(project);
            if (console != null)
            {
                console.disposeConsole();
            }
            consoles.remove(project);
        }
    }
}
