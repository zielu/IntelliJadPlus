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

import net.stevechaloner.idea.util.properties.converters.Converter;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Steve Chaloner
 */
public class DOMableSet<V> extends AbstractDOMableCollection<V>
{
    /**
     * The standard name of a persisted set item.
     */
    private static final String ELEMENT_NAME = "item";

    /**
     * The set.
     */
    private final Set<V> set = new HashSet<V>();

    /**
     * Initialise a new instance of this class.
     *
     * @param propertyDescriptor the property descriptor of the set.
     * @param converter the persistence converter
     */
    public DOMableSet(@NotNull PropertyDescriptor propertyDescriptor,
                      @NotNull Converter<V> converter)
    {
        super(propertyDescriptor,
              converter);
    }

    /** {@inheritDoc} */
    @NotNull
    public Element write()
    {
        Element parent = createCollectionElement();

        Converter<V> converter = getConverter();
        for (V v : set)
        {
            Element e = converter.toElement(ELEMENT_NAME,
                                            v);
            parent.addContent(e);
        }

        return parent;
    }

    /** {@inheritDoc} */
    public void read(@NotNull Element element)
    {
        set.clear();
        List children = element.getChildren();
        Converter<V> converter = getConverter();
        for (Object child : children)
        {
            set.add(converter.toType((Element)child));
        }
    }


    @NotNull
    protected DOMableType getDOMableType()
    {
        return DOMableType.SET;
    }

    // javadoc unnecessary
    @Nullable
    public Set<V> getValue()
    {
        return set;
    }
}
