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

package net.stevechaloner.intellijad.config.rules;

import net.stevechaloner.idea.util.properties.DOMable;
import org.jetbrains.annotations.NotNull;

/**
 * A render rule is used to contol property usage based on the context and bound
 * property at the point of evaluation.
 * @author Steve Chaloner
 */
public interface RenderRule<T> {

    /**
     * Evaluates based on the context and property to ascertain if the property
     * should be considered valid for use at the point of invokation.
     *
     * @param ruleContext the context the rule is evaluated in
     * @param domable the property this rule is bound to
     * @return true if the property should be considered valid for use
     */
    boolean evaluate(@NotNull RuleContext ruleContext,
                     DOMable<T> domable);
}
