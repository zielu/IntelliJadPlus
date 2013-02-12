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

import net.stevechaloner.idea.util.properties.converters.ConverterFactory;

import org.jdom.Element;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

/**
 * Test case for persistable maps.
 * 
 * @author Steve Chaloner
 */
public class DOMableMapTestCase extends AbstractDOMableCollectionTestCase
{
    @Before
    public void setUp()
    {
        collection = new DOMableMap<String>(new ImmutablePropertyDescriptor<String>("desc-name"),
                                            ConverterFactory.getStringConverter());
    }

    @Test
    public void testCollectionWrite()
    {
        Element mapElement = new Element("map");
        mapElement.addContent(createItem("test key 1",
                                         "test value 1"));
        collection.read(mapElement);

        Element writtenMap = collection.write();
        assertNotNull(writtenMap.getChildren());
        assertEquals(1,
                     writtenMap.getChildren().size());
        Element itemElement = (Element)writtenMap.getChildren().get(0);
        assertNotNull(itemElement.getChildren());
        assertEquals(2,
                     itemElement.getChildren().size());

        Element key = itemElement.getChild("key");
        assertNotNull(key);
        assertEquals("test key 1",
                     key.getText());

        Element value = itemElement.getChild("value");
        assertNotNull(value);
        assertEquals("test value 1",
                     value.getText());
    }

    @Test
    public void testCollectionRead()
    {
        Element mapElement = new Element("map");
        Element item1 = createItem("test key 1",
                                   "test value 1");
        mapElement.addContent(item1);
        collection.read(mapElement);

        Map<String, String> strings = ((DOMableMap<String>)collection).getValue();
        assertNotNull(strings);
        assertEquals(1,
                     strings.size());

        String mappedValue = strings.get("test key 1");
        assertNotNull(mappedValue);
        assertEquals("test value 1",
                     mappedValue);

        collection.read(mapElement);
        assertEquals(1,
                     strings.size());

        mappedValue = strings.get("test key 1");
        assertNotNull(mappedValue);
        assertEquals("test value 1",
                     mappedValue);

        Element item2 = createItem("test key 1",
                                   "test value 2");
        mapElement.addContent(item2);
        collection.read(mapElement);
        assertEquals(1,
                     strings.size());
        mappedValue = strings.get("test key 1");
        assertNotNull(mappedValue);
        assertEquals("test value 2",
                     mappedValue);

        Element item3 = createItem("test key 2",
                                   "test value 3");
        mapElement.addContent(item3);
        collection.read(mapElement);
        assertEquals(2,
                     strings.size());
        mappedValue = strings.get("test key 2");
        assertNotNull(mappedValue);
        assertEquals("test value 3",
                     mappedValue);
    }

    private Element createItem(String key,
                               String value)
    {
        Element itemElement = new Element("item");
        Element keyElement = new Element("key");
        itemElement.addContent(keyElement);
        Element valueElement = new Element("value");
        itemElement.addContent(valueElement);
        keyElement.setText(key);
        valueElement.setText(value);

        return itemElement;
    }

    /** {@inheritDoc} */
    protected DOMableType getCollectionType()
    {
        return DOMableType.MAP;
    }
}