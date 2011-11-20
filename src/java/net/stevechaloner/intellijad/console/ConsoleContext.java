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

import org.jetbrains.annotations.NotNull;

/**
 * @author Steve Chaloner
 */
public interface ConsoleContext
{
    /**
     * Checks if the console contains any information that makes it interesting
     * to the user, e.g. errors.
     *
     * @return true iff worth displaying
     */
    boolean isWorthDisplaying();

    /**
     * Sets the worthDisplaying flag.
     *
     * @param worthDisplaying true iff worth displaying
     */
    void setWorthDisplaying(boolean worthDisplaying);

    /**
     * Adds a message to a subsection  within this operation's log.
     *
     * @param entryType the entry type
     * @param message resource bundle key key
     * @param parameters resource bundle parameters
     */
    void addMessage(ConsoleEntryType entryType,
                    String message,
                    Object... parameters);

    /**
     * Adds a context-level message to this operation's log.
     *
     * @param entryType the entry type
     * @param message resource bundle key key
     * @param parameters resource bundle parameters
     */
    void addSectionMessage(ConsoleEntryType entryType,
                           String message,
                           Object... parameters);

    /**
     * Close the console view.
     */
    void close();

    @NotNull
    ConsoleTreeNode getContextNode();
}
