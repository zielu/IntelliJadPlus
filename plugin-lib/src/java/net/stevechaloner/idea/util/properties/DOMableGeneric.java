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

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.stevechaloner.idea.util.properties.converters.Converter;

/**
 * A generic persistable value.
 * 
 * @author Steve Chaloner
 */
public class DOMableGeneric<T> extends AbstractDOMable<T>
{
    /**
     * The value.
     */
    private T t;

    /**
     * The converter used to convert between the actual type and {@link String}.
     */
    private final Converter<T> converter;

    /**
     * The value type.
     */
    private final DOMableCollectionContentType contentType;

    /**
     * Initialises a new instance of this class.
     *
     * @param propertyDescriptor the property descriptor
     * @param converter the converters for the generic type
     * @param contentType the constrained content type of the property
     */
    public DOMableGeneric(@NotNull PropertyDescriptor<T> propertyDescriptor,
                          @NotNull Converter<T> converter,
                          @NotNull DOMableCollectionContentType contentType)
    {
        super(propertyDescriptor);
        setValue(propertyDescriptor.getDefault());
        this.converter = converter;
        this.contentType = contentType;
    }

    /** {@inheritDoc} */
    @NotNull
    public Element write()
    {
        Element e = new Element(getPropertyDescriptor().getName());
        e.setAttribute("type",
                       contentType.getName());
        e.setText(t == null ? "" : converter.toString(t));
        return e;
    }

    /** {@inheritDoc} */
    public void read(@NotNull Element element)
    {
        this.t = converter.toType(element);
    }

    // javadoc unnecessary
    public void setValue(T t)
    {
        this.t = t;
    }

    // javadoc unnecessary
    @Nullable
    public T getValue()
    {
        T value = t == null ? getPropertyDescriptor().getDefault() : t;
        return value == null ? converter.toType(converter.toString(value)) : value;
    }

    /** {@inheritDoc} */
    public String toString()
    {
        return getPropertyDescriptor().getName() + ':' + ((converter == null) ? t : converter.toString(t));
    }
}
