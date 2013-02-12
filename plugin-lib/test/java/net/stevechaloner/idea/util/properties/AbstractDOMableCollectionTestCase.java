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

import org.jdom.Element;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

/**
 * @author Steve Chaloner
 */
public abstract class AbstractDOMableCollectionTestCase
{
    protected AbstractDOMableCollection<String> collection;

    public void tearDown()
    {
        collection = null;
    }

    @Test
    public abstract void testCollectionRead();

    @Test
    public void testCollectionWrite()
    {
        Element setElement = new Element("set");
        Element e1 = new Element("item");
        e1.setText("test value");
        setElement.addContent(e1);
        collection.read(setElement);
        
        Element writtenCollection = collection.write();
        assertNotNull(writtenCollection);
        assertNotNull(writtenCollection.getAttributeValue(DOMableType.TYPE));
        assertEquals(getCollectionType().getName(),
                     writtenCollection.getAttributeValue(DOMableType.TYPE));
        assertNotNull(writtenCollection.getAttributeValue(DOMableCollectionContentType.CONTENT_TYPE));
        assertEquals(DOMableType.STRING.getName(),
                     writtenCollection.getAttributeValue(DOMableCollectionContentType.CONTENT_TYPE));

        assertNotNull(writtenCollection.getChildren());
        assertEquals(1,
                     writtenCollection.getChildren().size());
        Element writtenE1 = (Element)writtenCollection.getChildren().get(0);
        assertEquals(e1.getName(),
                     writtenE1.getName());
        assertEquals(e1.getValue(),
                     writtenE1.getValue());
    }

    /**
     * Gets the collection type of the test collection.
     *
     * @return the collection type
     */
    protected abstract DOMableType getCollectionType();
}
