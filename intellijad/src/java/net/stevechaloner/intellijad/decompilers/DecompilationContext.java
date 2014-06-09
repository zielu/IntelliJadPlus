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

import java.io.File;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.UserDataHolder;
import net.stevechaloner.intellijad.config.Config;
import net.stevechaloner.intellijad.console.ConsoleContext;
import net.stevechaloner.intellijad.util.OsUtil;
import net.stevechaloner.intellijad.util.PluginUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The context a decompilation occurs in.
 *
 * @author Steve Chaloner
 */
public class DecompilationContext implements UserDataHolder
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
    private final Map<Key, Object> userData = Maps.newConcurrentMap();

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
        this.targetDirectory = prepareTargetDir(project);
        targetDirectory.mkdir();
        targetDirectory.deleteOnExit();
    }

    private File prepareTargetDir(@NotNull Project project) {
        Config config = PluginUtil.getConfig(project);
        File tempDir;
        if (config.isUseCustomTempDir()) {
            tempDir = new File(config.getCustomTempDirPath());
            if (!tempDir.exists()) {
                Preconditions.checkState(tempDir.mkdirs(), "Could not mkdirs: "+tempDir.getAbsolutePath());
            }
        } else {
            tempDir = OsUtil.getTempDir();
        }
        return new File(tempDir, "ij" + System.currentTimeMillis());
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

    @Override
    public <T> void putUserData(@NotNull Key<T> key, @Nullable T value) {
        userData.put(key, value);
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
