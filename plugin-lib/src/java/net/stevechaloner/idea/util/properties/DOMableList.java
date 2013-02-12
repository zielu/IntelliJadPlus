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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Steve Chaloner
 */
public class DOMableList<V> extends AbstractDOMableCollection<V>
{
    /**
     * The standard name of a persisted list item.
     */
    private static final String LIST_ELEMENT_NAME = "item";

    /**
     * The list.
     */
    private final List<V> list = new ArrayList<V>();

    /**
     * Initialise a new instance of this class.
     * 
     * @param propertyDescriptor the property descriptor of the list
     * @param converter the persistence converters
     */
    public DOMableList(@NotNull PropertyDescriptor propertyDescriptor,
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
        for (V v : list)
        {
            Element e = converter.toElement(LIST_ELEMENT_NAME,
                                            v);
            parent.addContent(e);
        }

        return parent;
    }

    /** {@inheritDoc} */
    public void read(@NotNull Element element)
    {
        list.clear();
        List children = element.getChildren();
        Converter<V> converter = getConverter();
        for (Object child : children)
        {
            list.add(converter.toType((Element)child));
        }
    }

    // javadoc unnecessary
    public List<V> getValue()
    {
        return list;
    }

    @NotNull
    protected DOMableType getDOMableType()
    {
        return DOMableType.LIST;
    }
}
