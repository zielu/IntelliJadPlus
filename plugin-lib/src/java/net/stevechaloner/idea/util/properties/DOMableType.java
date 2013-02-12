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
public enum DOMableType
{
    STRING("string"),
    BOOLEAN("boolean"),
    INTEGER("integer"),
    LIST("list"),
    MAP("map"),
    SET("set"),
    TABLE_MODEL("table-model");

    public static final String TYPE = "type";

    private final static Map<String, DOMableType> REF_MAP = new HashMap<String, DOMableType>()
    {
        {
            put(STRING.getName(),
                STRING);
            put(BOOLEAN.getName(),
                BOOLEAN);
            put(INTEGER.getName(),
                INTEGER);
            put(LIST.getName(),
                LIST);
            put(MAP.getName(),
                MAP);
            put(SET.getName(),
                SET);
            put(TABLE_MODEL.getName(),
                TABLE_MODEL);
        }
    };

    private final static Map<Class, DOMableType> ANALAGOUS_TYPE_MAP = new HashMap<Class, DOMableType>()
    {
        {
            put(String.class,
                STRING);
            put(Boolean.class,
                BOOLEAN);
            put(Integer.class,
                INTEGER);
        }
    };

    private final String name;

    private DOMableType(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public static DOMableType getByName(String name)
    {
        DOMableType domableType = REF_MAP.get(name);
        return domableType == null ? STRING : domableType;
    }

    public static DOMableType getByAnalagousType(Class clazz)
    {
        return ANALAGOUS_TYPE_MAP.get(clazz);
    }
}
