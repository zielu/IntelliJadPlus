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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * @author Steve Chaloner
 */
public class ImmutablePropertyDescriptorTestCase
{
    @Test
    public void testName()
    {
        ImmutablePropertyDescriptor<String> descriptor = new ImmutablePropertyDescriptor<String>("desc-name");
        assertNotNull("desc-name");
        assertEquals("desc-name",
                     descriptor.getName());
    }

    @Test
    public void testExplicitNullDefault()
    {
        ImmutablePropertyDescriptor<String> descriptor = new ImmutablePropertyDescriptor<String>("desc-name",
                                                                                                 null);
        assertNull(descriptor.getDefault());
    }

    @Test
    public void testImplicitNullDefault()
    {
        ImmutablePropertyDescriptor<String> descriptor = new ImmutablePropertyDescriptor<String>("desc-name");
        assertNull(descriptor.getDefault());
    }

    @Test
    public void testDefault()
    {
        ImmutablePropertyDescriptor<String> descriptor = new ImmutablePropertyDescriptor<String>("desc-name",
                                                                                                 "the default value");
        assertNotNull(descriptor.getDefault());
        assertEquals("the default value",
                     descriptor.getDefault());
    }

    @Test
    public void testValueWithDefault()
    {
        ImmutablePropertyDescriptor<String> descriptor = new ImmutablePropertyDescriptor<String>("desc-name",
                                                                                                 "the default value");
        DOMableGeneric<String> domable = new DOMableGeneric<String>(descriptor,
                                                             ConverterFactory.getStringConverter(),
                                                             DOMableCollectionContentType.STRING);
        assertNotNull(descriptor.getValue(domable));
        assertEquals("the default value",
                     descriptor.getValue(domable));

        domable.setValue("new value");
        assertNotNull(descriptor.getValue(domable));
        assertEquals("new value",
                     descriptor.getValue(domable));

        domable.setValue(null);
        assertNotNull(descriptor.getValue(domable));
        assertEquals("the default value",
                     descriptor.getValue(domable));

        domable.setValue("");
        assertNotNull(descriptor.getValue(domable));
        assertEquals("the default value",
                     descriptor.getValue(domable));
    }

    @Test
    public void testValueWithNoDefault()
    {
        ImmutablePropertyDescriptor<String> descriptor = new ImmutablePropertyDescriptor<String>("desc-name");
        DOMableGeneric<String> domable = new DOMableGeneric<String>(descriptor,
                                                             ConverterFactory.getStringConverter(),
                                                             DOMableCollectionContentType.STRING);
        assertNull(descriptor.getValue(domable));

        domable.setValue("new value");
        assertNotNull(descriptor.getValue(domable));
        assertEquals("new value",
                     descriptor.getValue(domable));

        domable.setValue(null);
        assertNull(descriptor.getValue(domable));

        domable.setValue("");
        assertNull(descriptor.getValue(domable));
    }
}
