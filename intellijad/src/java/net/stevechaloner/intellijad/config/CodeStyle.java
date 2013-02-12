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

package net.stevechaloner.intellijad.config;

import net.stevechaloner.intellijad.IntelliJadResourceBundle;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines the possible reformatting styles available to decompiled source code.
 * 
 * @author Steve Chaloner
 */
public enum CodeStyle
{
    /**
     * The preferred (IDE-defined) code style.
     */
    PREFERRED_STYLE("Preferred-Style",
                    IntelliJadResourceBundle.message("config.reformat.preferred-style")),

    /**
     * The debuggable style - lots of empty lines, inlined code, etc.
     */
    DEBUGGABLE_STYLE("Debuggable-Style",
                    IntelliJadResourceBundle.message("config.reformat.debuggable-style"));

    /**
     * Mapping of the entry name to the entry.
     */
    private static final Map<String, CodeStyle> MAP = new HashMap<String, CodeStyle>() {
        {
            put(PREFERRED_STYLE.getName(),
                PREFERRED_STYLE);
            put(DEBUGGABLE_STYLE.getName(),
                DEBUGGABLE_STYLE);
        }

        /**
         * Gets the option by its name.
         *
         * @param key the name
         * @return the option, or PREFERRED_STYLE if an invalid key is specified
         */
        public CodeStyle get(Object key) {
            CodeStyle style = super.get(key);
            return style == null ? PREFERRED_STYLE : style;
        }
    };

    /**
     * The name of the option.
     */
    private final String name;

    /**
     * The display name of the option.
     */
    private final String displayName;

    /**
     * Initialises a new instance of this class.
     *
     * @param name the name of the option
     * @param displayName the display name of the option
     */
    CodeStyle(String name,
              String displayName)
    {
        this.name = name;
        this.displayName = displayName;
    }

    /**
     * Gets the name of the option.
     *
     * @return the name of the option
     */
    public String getName()
    {
        return name;
    }

    /**
     * Gets the display name of the option.
     *
     * @return the display name of the option
     */
    public String getDisplayName()
    {
        return displayName;
    }

    /**
     * Look up the enum entry by name.
     *
     * @param name the name of the type
     * @return the enum entry
     */
    public static CodeStyle getByName(String name) {
        return MAP.get(name);
    }

    /** {@inheritDoc} */
    public String toString() {
        return displayName;
    }
}
