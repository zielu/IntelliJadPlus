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

package net.stevechaloner.idea.util.paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

import java.io.File;

/**
 * Test case for path utilities.
 *
 * @author Steve Chaloner
 */
public class PathTestCase
{
    @Test
    public void testNormaliseWindowsPath()
    {
        String normalisedPath = Path.normalisePath("c:\\blah");
        assertNotNull(normalisedPath);
        assertEquals("c:/blah",
                     normalisedPath);
    }

    @Test
    public void testNormaliseWindowsFile()
    {
        String normalisedPath = Path.normalisePath(new File("c:\\blah"));
        assertNotNull(normalisedPath);
        assertEquals("c:/blah",
                     normalisedPath);
    }

    @Test
    public void testNormalisedPath()
    {
        String normalisedPath = Path.normalisePath(new File("c:\\blah"));
        assertEquals(normalisedPath,
                     Path.normalisePath(new File(normalisedPath)));
    }
}
