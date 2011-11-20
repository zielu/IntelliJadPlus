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
 * Enum detailing the choices available to the decompiler when a class is navigated to.
 * 
 * @author Steve Chaloner
 */
public enum NavigationTriggeredDecompile {

    /**
     * Unless excluded, decompile the class automatically and immediately.
     */
    ALWAYS("Always",
           IntelliJadResourceBundle.message("option.always")),
    /**
     * Ignore the navigation event.
     */
    NEVER("Never",
          IntelliJadResourceBundle.message("option.never")),
    /**
     * Unless excluded, ask if the class should be decompiled.
     */
    ASK("Ask",
        IntelliJadResourceBundle.message("option.ask"));

    /**
     * Mapping of the entry name to the entry.
     */
    private static final Map<String, NavigationTriggeredDecompile> MAP = new HashMap<String, NavigationTriggeredDecompile>() {
        {
            put(ALWAYS.getName(),
                ALWAYS);
            put(NEVER.getName(),
                NEVER);
            put(ASK.getName(),
                ASK);
        }

        /**
         * Gets the option by its name.
         *
         * @param key the name
         * @return the option, or ASK if an invalid key is specified
         */
        public NavigationTriggeredDecompile get(Object key) {
            NavigationTriggeredDecompile option = super.get(key);
            return option == null ? ASK : option;
        }
    };

    /**
     * The name of the entry.
     */
    private final String name;

    /**
     * The display name of the entry.
     */
    private final String displayName;

    /**
     * Initialises a new instance of this class.
     *
     * @param name the name of the entry
     * @param displayName the display name of the entry
     */
    NavigationTriggeredDecompile(String name,
                                 String displayName) {
        this.name = name;
        this.displayName = displayName;
    }

    /** {@inheritDoc} */
    public String getName() {
        return name;
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Look up the enum entry by name.
     *
     * @param name the name of the type
     * @return the enum entry
     */
    public static NavigationTriggeredDecompile getByName(String name) {
        return MAP.get(name);
    }

    /** {@inheritDoc} */
    public String toString() {
        return displayName;
    }
}
