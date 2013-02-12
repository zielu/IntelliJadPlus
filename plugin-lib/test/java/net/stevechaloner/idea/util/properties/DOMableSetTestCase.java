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

import java.util.Set;

/**
 * Test case for persistable sets.
 * 
 * @author Steve Chaloner
 */
public class DOMableSetTestCase extends AbstractDOMableCollectionTestCase
{
    @Before
    public void setUp()
    {
        collection = new DOMableSet<String>(new ImmutablePropertyDescriptor<String>("desc-name"),
                                            ConverterFactory.getStringConverter());
    }

    @Test
    public void testCollectionRead()
    {
        Element setElement = new Element("set");
        Element e1 = new Element("item");
        e1.setText("test value");
        setElement.addContent(e1);
        collection.read(setElement);

        Set<String> strings = ((DOMableSet<String>)collection).getValue();
        assertNotNull(strings);
        assertEquals(1,
                     strings.size());

        collection.read(setElement);
        assertEquals(1,
                     strings.size());

        Element e2 = new Element("item");
        e2.setText("test value");
        setElement.addContent(e2);
        collection.read(setElement);
        assertEquals(1,
                     strings.size());

        e2.setText("another test value");
        collection.read(setElement);
        assertEquals(2,
                     strings.size());
    }

    /** {@inheritDoc} */
    protected DOMableType getCollectionType()
    {
        return DOMableType.SET;
    }
}