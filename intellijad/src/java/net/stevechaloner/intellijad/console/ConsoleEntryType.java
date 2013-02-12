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

package net.stevechaloner.intellijad.console;

import net.stevechaloner.intellijad.IntelliJadResourceBundle;

/**
 * @author Steve Chaloner
 */
public enum ConsoleEntryType
{
    ROOT(""),
    INTELLIJAD("message.class"),
    DECOMPILATION_OPERATION("message.decompilation"),
    JAR_OPERATION("message.jar-extraction"),
    LIBRARY_OPERATION("message.library"),
    MESSAGE(""),
    INFO(""),
    ERROR("");

    private final String messageKey;

    /**
     * Initialises a new instance of this class.
     *
     * @param messageKey the key for the standard message of this entry type
     */
    ConsoleEntryType(String messageKey)
    {
        this.messageKey = messageKey;
    }

    /**
     * Gets the message, parameterised with the given parameters.
     *
     * @param params the parameters
     * @return the parameterised message
     */
    String getMessage(Object... params)
    {
        return IntelliJadResourceBundle.message(messageKey,
                                                params);
    }
}
