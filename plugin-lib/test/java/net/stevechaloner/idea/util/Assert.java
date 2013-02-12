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

package net.stevechaloner.idea.util;

import static org.junit.Assert.fail;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Assertion methods for unit testing.
 * 
 * @author Steve Chaloner
 */
public class Assert
{
    /**
     * Checks the equality of the given strings, ignoring case.
     *
     * @param s1 string to compare
     * @param s2 string to compare
     */
    public static void assertEqualsIgnoreCase(@NotNull String s1,
                                              @NotNull String s2)
    {
        assertEqualsIgnoreCase(null,
                               s1,
                               s2);
    }

    /**
     * Checks the equality of the given strings, ignoring case.
     *
     * @param message the failure message
     * @param s1 string to compare
     * @param s2 string to compare
     */
    public static void assertEqualsIgnoreCase(@Nullable String message,
                                              @NotNull String s1,
                                              @NotNull String s2)
    {
        if (!s1.equalsIgnoreCase(s2))
        {
            fail(message);
        }
    }
}
