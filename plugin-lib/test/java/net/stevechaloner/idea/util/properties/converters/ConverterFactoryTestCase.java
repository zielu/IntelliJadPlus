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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import org.junit.Test;

/**
 * @author Steve Chaloner
 */
public class ConverterFactoryTestCase
{
    @Test
    public void testBooleanConverterGetter()
    {
        Converter<Boolean> converter = ConverterFactory.getBooleanConverter();
        assertNotNull(converter);
    }

    @Test
    public void testBooleanConverterInstance()
    {
        Converter<Boolean> converter = ConverterFactory.getBooleanConverter();
        Converter<Boolean> converter2 = ConverterFactory.getBooleanConverter();
        assertEquals(converter,
                     converter2);
        assertSame("Same converter instance should always be returned",
                   converter,
                   converter2);
    }

    @Test
    public void testIntegerConverterGetter()
    {
        Converter<Integer> converter = ConverterFactory.getIntegerConverter();
        assertNotNull(converter);
    }

    @Test
    public void testIntegerConverterInstance()
    {
        Converter<Integer> converter = ConverterFactory.getIntegerConverter();
        Converter<Integer> converter2 = ConverterFactory.getIntegerConverter();
        assertEquals(converter,
                     converter2);
        assertSame("Same converter instance should always be returned",
                   converter,
                   converter2);
    }

    @Test
    public void testStringConverterGetter()
    {
        Converter<String> converter = ConverterFactory.getStringConverter();
        assertNotNull(converter);
    }

    @Test
    public void testStringConverterInstance()
    {
        Converter<String> converter = ConverterFactory.getStringConverter();
        Converter<String> converter2 = ConverterFactory.getStringConverter();
        assertEquals(converter,
                     converter2);
        assertSame("Same converter instance should always be returned",
                   converter,
                   converter2);
    }
}
