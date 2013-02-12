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
import org.jetbrains.annotations.Nullable;

/**
 * Converts between a String and a given type.
 *
 * @author Steve Chaloner
 */
abstract class AbstractConverter<T> implements Converter<T>
{
    /** {@inheritDoc} */
    @NotNull
    public Element toElement(@NotNull String name,
                             @Nullable T t)
    {
        Element e = new Element(name);
        e.setText(toString(t));
        return e;
    }

    /** {@inheritDoc} */
    @NotNull
    public T toType(@Nullable Element e)
    {
        return toType(e == null ? null : e.getText());
    }

    /** {@inheritDoc} */
    @NotNull
    public abstract T toType(@Nullable String s);
}
