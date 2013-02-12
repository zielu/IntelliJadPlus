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
import net.stevechaloner.idea.util.properties.converters.ConverterFactory;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Steve Chaloner
 */
public class DOMableMap<V> extends AbstractDOMableCollection<V>
{
    /**
     * The standard name of a persisted item.
     */
    private static final String ELEMENT_NAME = "item";

    /**
     * The standard name of a persisted key.
     */
    private static final String KEY_NAME = "key";

    /**
     * The standard name of a persisted value.
     */
    private static final String VALUE_NAME = "value";

    private static final Converter<String> KEY_CONVERTER = ConverterFactory.getStringConverter();

    /**
     * The map.
     */
    private final Map<String, V> map = new HashMap<String, V>();

    /**
     * Initialises a new instance of this class.
     *
     * @param propertyDescriptor the property descriptor of the map
     * @param converter the persistence converter for values
     */
    public DOMableMap(@NotNull PropertyDescriptor propertyDescriptor,
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
        for (String s : map.keySet())
        {
            Element e = new Element(ELEMENT_NAME);
            e.addContent(KEY_CONVERTER.toElement(KEY_NAME,
                                                 s));
            e.addContent(converter.toElement(VALUE_NAME,
                                             map.get(s)));
            parent.addContent(e);
        }

        return parent;
    }

    /** {@inheritDoc} */
    public void read(@NotNull Element element)
    {
        map.clear();
        List children = element.getChildren();
        Converter<V> converter = getConverter();
        for (Object child : children)
        {
            Element kv = (Element)child;
            map.put(KEY_CONVERTER.toType(kv.getChild(KEY_NAME)),
                    converter.toType(kv.getChild(VALUE_NAME)));
        }
    }


    @NotNull
    protected DOMableType getDOMableType()
    {
        return DOMableType.MAP;
    }

    // javadoc unnecessary
    public Map<String, V> getValue()
    {
        return map;
    }
}
