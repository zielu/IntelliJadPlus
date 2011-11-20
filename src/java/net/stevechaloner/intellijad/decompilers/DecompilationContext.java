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

package net.stevechaloner.intellijad.decompilers;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;

import net.stevechaloner.intellijad.config.Config;
import net.stevechaloner.intellijad.console.ConsoleContext;
import net.stevechaloner.intellijad.util.PluginUtil;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * The context a decompilation occurs in.
 *
 * @author Steve Chaloner
 */
public class DecompilationContext
{
    /**
     * The console context to use for reporting.
     */
    private final ConsoleContext consoleContext;

    /**
     * The command to execute.
     */
    private final String command;

    /**
     * The directory available for placing temporary files.
     */
    private final File targetDirectory;

    /**
     * The project this decompiation is occurring in.
     */
    private final Project project;

    /**
     * User data.
     */
    private final Map<Key, Object> userData = new HashMap<Key, Object>();

    /**
     * Initialises a new instance of this class.
     *
     * @param project the project
     * @param consoleContext the reporting console context
     * @param command the command to execute
     */
    public DecompilationContext(@NotNull Project project,
                                @NotNull ConsoleContext consoleContext,
                                @NotNull String command)
    {
        this.project = project;
        this.consoleContext = consoleContext;
        this.command = command;
        this.targetDirectory = new File(new File(System.getProperty("java.io.tmpdir")),
                                        "ij" + System.currentTimeMillis());
        targetDirectory.mkdir();
        targetDirectory.deleteOnExit();
    }

    // javadoc unnecessary
    public ConsoleContext getConsoleContext()
    {
        return consoleContext;
    }

    // javadoc unnecessary
    public String getCommand()
    {
        return command;
    }

    // javadoc unnecessary
    public File getTargetDirectory()
    {
        return targetDirectory;
    }

    // javadoc unnecessary
    public Project getProject()
    {
        return project;
    }

    // javadoc unnecessary
    public Config getConfig()
    {
        return PluginUtil.getConfig(project);
    }

    // javadoc unnecessary
    public <T> void addUserData(@NotNull Key<T> key,
                                @NotNull T value)
    {
        userData.put(key,
                     value);
    }

    // javadoc unnecessary
    public <T> T getUserData(@NotNull Key<T> key)
    {
        return (T)userData.get(key);
    }

    // javadoc unnecessary
    public <T> T removeUserData(@NotNull Key<T> key)
    {
        return (T)userData.remove(key);
    }

    // javadoc unnecessary
    public <T> boolean containsUserData(Key<T> key)
    {
        return userData.containsKey(key);
    }
}
