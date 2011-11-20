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

package net.stevechaloner.intellijad.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;

import net.stevechaloner.intellijad.config.ApplicationConfigComponent;
import net.stevechaloner.intellijad.config.Config;
import net.stevechaloner.intellijad.config.ProjectConfigComponent;

import org.jetbrains.annotations.NotNull;

/**
 * Helper class for plugin interactions.
 *
 * @author Steve Chaloner
 */
public class PluginUtil
{
    /**
     * Static access only.
     */
    private PluginUtil()
    {
    }

    /**
     * Gets the component from the application context.
     *
     * @param clazz the class of the component
     * @return the component
     */
    public static <C> C getComponent(Class<C> clazz)
    {
        return ApplicationManager.getApplication().getComponent(clazz);
    }

    /**
     * Gets the application-level configuration.
     *
     * @return the application-level configuration
     */
    public static Config getApplicationConfig()
    {
        return getComponent(ApplicationConfigComponent.class).getConfig();
    }

    /**
     * Gets the plugin configuration.
     *
     * @param project the project to get the config from
     * @return the project configuration
     */
    public static Config getConfig(@NotNull Project project)
    {
        ProjectConfigComponent projectComponent = project.getComponent(ProjectConfigComponent.class);
        Config config = null;
        if (projectComponent != null)
        {
            config = projectComponent.getConfig();
        }
        if (config == null || !config.isUseProjectSpecificSettings())
        {
            config = getApplicationConfig();
        }
        return config;
    }
}
