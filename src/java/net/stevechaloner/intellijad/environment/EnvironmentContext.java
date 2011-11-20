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

package net.stevechaloner.intellijad.environment;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * The environment context used to localise values within a project.
 *
 * @author Steve Chaloner
 */
public class EnvironmentContext
{
    /**
     * The project.
     */
    @NotNull private final Project project;

    /**
     * User data.
     */
    private final Map<Key, Object> userData = new HashMap<Key, Object>();

    /**
     * Initialises a new instance of this class.
     *
     * @param project the current project
     */
    public EnvironmentContext(@NotNull Project project)
    {
        this.project = project;
    }


    /**
     * Gets the environment's project.
     *
     * @return the project
     */
    @NotNull
    public Project getProject()
    {
        return project;
    }

    /**
     * Sets the user data identified by the given key.
     *
     * @param key the key
     * @param value the value
     */
    public <T> void setUserData(@NotNull Key<T> key,
                                @NotNull T value)
    {
        userData.put(key,
                     value);
    }

    /**
     * Gets the user data identified by the given key.
     *
     * @param key the key
     * @return the value the key is mapped to, if any
     */
    @Nullable
    public <T> T getUserData(@NotNull Key<T> key)
    {
        return (T)userData.get(key);
    }

    /**
     * Removes the user data identified by the given key.
     *
     * @param key the key
     * @return the value the key is mapped to, if any
     */
    @Nullable
    public <T> T removeUserData(@NotNull Key<T> key)
    {
        return (T)userData.remove(key);
    }
}
