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
import net.stevechaloner.idea.util.properties.converters.Converter;

/**
 * @author Steve Chaloner
 */
abstract class AbstractDOMableCollection<V> extends AbstractDOMable
{

    /**
     *
     */
    private final Converter<V> converter;

    /**
     * Initialise a new instance of this class.
     * 
     * @param propertyDescriptor the property descriptor
     * @param converter the persistence converters
     */
    public AbstractDOMableCollection(@NotNull PropertyDescriptor propertyDescriptor,
                                     @NotNull Converter<V> converter)
    {
        super(propertyDescriptor);
        this.converter = converter;
    }

    /**
     * Create a new element with the collection attributes populated.
     *
     * @return a new element
     */
    protected Element createCollectionElement()
    {
        Element e = new Element(getPropertyDescriptor().getName());
        e.setAttribute(DOMableType.TYPE,
                       getDOMableType().getName());
        e.setAttribute(DOMableCollectionContentType.CONTENT_TYPE,
                       converter.getContentType().getName());

        return e;
    }

    /**
     * Gets the domable type of the collection.
     *
     * @return the domable type of the collection
     */
    @NotNull
    protected abstract DOMableType getDOMableType();

    /**
     * Gets the converter for the content type.
     *
     * @return the converter
     */
    @NotNull
    protected Converter<V> getConverter()
    {
        return converter;
    }
}