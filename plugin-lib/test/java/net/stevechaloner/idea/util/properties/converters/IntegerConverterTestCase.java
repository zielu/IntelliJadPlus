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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Steve Chaloner
 */
public class IntegerConverterTestCase
{
    private Converter<Integer> converter;

    @Before
    public void setUp()
    {
        converter = ConverterFactory.getIntegerConverter();
    }

    @After
    public void tearDown()
    {
        converter = null;
    }

    @Test
    public void testToString()
    {
        String s = converter.toString(Integer.MIN_VALUE);
        assertEquals(Integer.toString(Integer.MIN_VALUE),
                     s);
    }
}
