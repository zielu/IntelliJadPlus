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

package net.stevechaloner.idea.util.properties.converters;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;
import net.stevechaloner.idea.util.properties.DOMableCollectionContentType;

/**
 * @author Steve Chaloner
 */
public interface Converter<T>
{
    /**
     * Encapsulate the given object in an {@link Element}.  If the
     * given object can't be converted to a valid string, its default
     * value will be used.
     *
     * @param name the name of the element
     * @param t the object to encapsulate
     * @return an element containing a string representation of the given object
     */
    @NotNull
    Element toElement(@NotNull @NonNls String name,
                      @Nullable T t);

    /**
     * Convert the given object to a string.  If the object
     * cannot be converted, an empty string is returned.
     *
     * @param t the object to convert
     * @return a string representation of the object.
     */
    @NotNull
    String toString(@Nullable T t);

    /**
     * Convert the string to the required type.  If the string cannot
     * be converted, the default value of the type will be returned.
     *
     * @param s the string to convert
     * @return an object created from the string
     */
    @NotNull
    T toType(@Nullable String s);

    /**
     * Convert the string value encapsulated in the element to the
     * required type.  If the string cannot be converted, the default
     * value of the type will be returned.
     *
     * @param e the element containing the value
     * @return an object created from the encapsulated string
     */
    @NotNull
    T toType(@Nullable Element e);

    /**
     * Gets the content type the converter works with.
     *
     * @return the content type
     */
    DOMableCollectionContentType getContentType();
}
