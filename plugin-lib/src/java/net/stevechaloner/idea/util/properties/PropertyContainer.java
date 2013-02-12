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

/**
 * @author Steve Chaloner
 */
public interface PropertyContainer<K extends PropertyDescriptor, P>
{
    /**
     * Gets the value mapped to the given key.  If the key cannot be found,
     * the value will be null.
     *
     * @param key the key
     * @return the value
     */
    P get(@NotNull K key);

    /**
     * Maps the value to the key.
     *
     * @param key the key
     * @param persistable the value
     */
    void put(@NotNull K key,
             @NotNull P persistable);

    /**
     * Removes the value mapped to the key.
     *
     * @param key the key
     * @return the value, if the key was present
     */
    P remove(@NotNull K key);
}