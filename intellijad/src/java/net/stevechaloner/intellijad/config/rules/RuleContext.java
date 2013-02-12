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

package net.stevechaloner.intellijad.config.rules;

import net.stevechaloner.intellijad.config.CommandLinePropertyDescriptor;
import net.stevechaloner.intellijad.config.Config;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Steve Chaloner
 */
public class RuleContext
{
    /**
     * The properties.
     */
    private final Map<String, CommandLinePropertyDescriptor> properties = new HashMap<String, CommandLinePropertyDescriptor>();

    /**
     * The configuration.
     */
    private Config config;

    // javadoc unnecessary
    public void addProperty(@NotNull CommandLinePropertyDescriptor property)
    {
        addProperty(property.getName(),
                    property);
    }

    // javadoc unnecessary
    public void addProperty(@NotNull String name,
                            @NotNull CommandLinePropertyDescriptor property)
    {
        properties.put(name,
                       property);
    }

    // javadoc unnecessary
    @Nullable
    public CommandLinePropertyDescriptor removePropertyDescriptor(String name)
    {
        return properties.remove(name);
    }

    // javadoc unnecessary
    @Nullable
    public CommandLinePropertyDescriptor getPropertyDescriptor(String name)
    {
        return properties.get(name);
    }

    // javadoc unnecessary
    public boolean hasProperty(String name)
    {
        return properties.get(name) != null;
    }

    // javadoc unnecessary
    public Config getConfig()
    {
        return config;
    }

    // javadoc unnecessary
    public void setConfig(Config config)
    {
        this.config = config;
    }
}
