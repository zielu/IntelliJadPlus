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

package net.stevechaloner.idea.util.properties;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * .
 *
 * @author Steve Chaloner
 */
public class ImmutablePropertyDescriptor<T> implements PropertyDescriptor<T>
{
    /**
     * The name of the property.
     */
    private final String name;

    /**
     * The default value of the property.
     */
    private final T defaultValue;

    /**
     *
     * @param name the property name
     */
    public ImmutablePropertyDescriptor(@NotNull String name)
    {
        this(name,
             null);
    }

    /**
     *
     * @param name the property name
     * @param defaultValue the default value of the property
     */
    public ImmutablePropertyDescriptor(@NotNull String name,
                                       @Nullable T defaultValue)
    {
        this.name = name;
        this.defaultValue = defaultValue;
    }

    /** {@inheritDoc} */
    @NotNull
    public String getName()
    {
        return name;
    }

    /** {@inheritDoc} */
    @Nullable
    public T getValue(@Nullable DOMable<T> domable)
    {
        return (domable == null) ? defaultValue : (domable.getValue() == null || "".equals(domable.getValue())) ? defaultValue : domable.getValue();
    }

    /** {@inheritDoc} */
    public T getDefault()
    {
        return defaultValue;
    }
}
