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
import org.jetbrains.annotations.NonNls;

/**
 * @author Steve Chaloner
 */
public abstract class AbstractDOMable<T> implements DOMable<T>
{
    /**
     * The property descriptor.
     */
    private final PropertyDescriptor<T> propertyDescriptor;

    /**
     * Initialises a new instance of this class.
     *
     * @param propertyDescriptor the property descriptor
     */
    protected AbstractDOMable(@NotNull PropertyDescriptor<T> propertyDescriptor)
    {
        this.propertyDescriptor = propertyDescriptor;
    }

    /** {@inheritDoc} */
    @NonNls
    @NotNull
    public PropertyDescriptor<T> getPropertyDescriptor()
    {
        return propertyDescriptor;
    }
}

