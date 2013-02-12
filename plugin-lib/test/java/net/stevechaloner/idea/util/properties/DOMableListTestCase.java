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

import java.util.List;

/**
 * Test case for persistable lists.
 * 
 * @author Steve Chaloner
 */
public class DOMableListTestCase extends AbstractDOMableCollectionTestCase
{
    @Before
    public void setUp()
    {
        collection = new DOMableList<String>(new ImmutablePropertyDescriptor<String>("desc-name"),
                                            ConverterFactory.getStringConverter());
    }

    @Test
    public void testCollectionRead()
    {
        Element listElement = new Element("list");
        Element e1 = new Element("item");
        e1.setText("test value");
        listElement.addContent(e1);
        collection.read(listElement);

        List<String> strings = ((DOMableList<String>)collection).getValue();
        assertNotNull(strings);
        assertEquals(1,
                     strings.size());

        collection.read(listElement);
        assertEquals(1,
                     strings.size());

        Element e2 = new Element("item");
        e2.setText("test value");
        listElement.addContent(e2);
        collection.read(listElement);
        assertEquals(2,
                     strings.size());
    }

    /** {@inheritDoc} */
    protected DOMableType getCollectionType()
    {
        return DOMableType.LIST;
    }
}