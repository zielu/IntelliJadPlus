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

package net.stevechaloner.intellijad.environment;

/**
 * Contains the result of an environmental validation.
 *
 * @author Steve Chaloner
 */
public class ValidationResult
{
    /**
     * The validity state of the configuration.
     */
    private final boolean valid;

    /**
     * Indicates if the user cancelled the whole operation.
     */
    private final boolean cancelled;

    /**
     * Initialises a new instance of this class.
     *
     * @param valid true if the config is valid
     * @param cancelled true if the operation should be cancelled
     */
    ValidationResult(boolean valid,
                     boolean cancelled)
    {
        this.valid = valid;
        this.cancelled = cancelled;
    }

    /**
     * Gets the validity state of the configuration.  True implies a valid config.
     *
     * @return true if the configuration is valid
     */
    public boolean isValid()
    {
        return valid;
    }

    /**
     * Returns if the operation was cancelled or not.  This takes priority over anything else in
     * this result, i.e. even if the config is valid (which theoretically can't occur in the current
     * GUI flow) the decompilation should be considered cancelled if this method returns true.
     *
     * @return true if the operation is cancelled
     */
    public boolean isCancelled()
    {
        return cancelled;
    }
}
