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

import java.util.Map;
import java.util.HashMap;

/**
 * @author Steve Chaloner
 */
public enum DOMableCollectionContentType
{
    STRING("string"),
    BOOLEAN("boolean"),
    INTEGER("integer");

    public static final String CONTENT_TYPE = "content-type";
    public static final String CONTENT_TYPES = "content-types";

    private final static Map<String, DOMableCollectionContentType> MAP = new HashMap<String, DOMableCollectionContentType>()
    {
        {
            put(STRING.getName(),
                STRING);
            put(BOOLEAN.getName(),
                BOOLEAN);
            put(INTEGER.getName(),
                INTEGER);
        }
    };

    private final String name;

    private DOMableCollectionContentType(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public static DOMableCollectionContentType getByName(String name)
    {
        DOMableCollectionContentType domableType = MAP.get(name);
        return domableType == null ? STRING : domableType;
    }
}
