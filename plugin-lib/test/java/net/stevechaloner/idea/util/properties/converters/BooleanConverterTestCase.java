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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static net.stevechaloner.idea.util.Assert.assertEqualsIgnoreCase;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.jdom.Element;

/**
 * Test case for a boolean converter.
 * 
 * @author Steve Chaloner
 */
public class BooleanConverterTestCase
{
    private Converter<Boolean> converter;

    @Before
    public void setUp()
    {
        converter = ConverterFactory.getBooleanConverter();
    }

    @After
    public void tearDown()
    {
        converter = null;
    }

    @Test
    public void testToTypeWithStringForTrue()
    {

        assertTrue(converter.toType("true"));
        assertTrue(converter.toType("TRUE"));
        assertTrue(converter.toType("TrUe"));
        assertFalse(converter.toType("xxxTRUExxx"));
    }

    @Test
    public void testToTypeWithStringForFalse()
    {
        assertFalse(converter.toType("false"));
        assertFalse(converter.toType("FALSE"));
        assertFalse(converter.toType("FaLsE"));
        assertFalse(converter.toType("troo"));
        assertFalse(converter.toType(""));
    }

    @Test
    public void testToTypeWithElementForTrue()
    {
        Element e = new Element("name");
        e.setText("true");
        assertTrue(converter.toType(e));
        e.setText("TRUE");
        assertTrue(converter.toType(e));
        e.setText("TrUe");
        assertTrue(converter.toType(e));
    }

    @Test
    public void testToTypeWithElementForFalse()
    {
        Element e = new Element("name");
        e.setText("false");
        assertFalse(converter.toType(e));
        e.setText("FALSE");
        assertFalse(converter.toType(e));
        e.setText("FaLsE");
        assertFalse(converter.toType(e));
        e.setText("troo");
        assertFalse(converter.toType(e));
        e.setText("");
        assertFalse(converter.toType(e));
    }

    @Test
    public void testToStringForTrue()
    {
        assertEqualsIgnoreCase("true",
                               converter.toString(Boolean.TRUE));
        assertEqualsIgnoreCase("true",
                               converter.toString(new Boolean("true")));
        assertEqualsIgnoreCase("true",
                               converter.toString(true));
    }

    @Test
    public void testToStringForFalse()
    {
        assertEqualsIgnoreCase("false",
                               converter.toString(Boolean.FALSE));
        assertEqualsIgnoreCase("false",
                               converter.toString(new Boolean("false")));
        assertEqualsIgnoreCase("false",
                               converter.toString(false));
    }

    @Test
    public void testToElementForTrue()
    {
        Element e = converter.toElement("name", true);
        assertEqualsIgnoreCase("true",
                               e.getText());
        e = converter.toElement("name", Boolean.TRUE);
        assertEqualsIgnoreCase("true",
                               e.getText());
    }

    @Test
    public void testToElementForFalse()
    {
        Element e = converter.toElement("name", false);
        assertEqualsIgnoreCase("false",
                               e.getText());
        e = converter.toElement("name", Boolean.FALSE);
        assertEqualsIgnoreCase("false",
                               e.getText());
    }
}
