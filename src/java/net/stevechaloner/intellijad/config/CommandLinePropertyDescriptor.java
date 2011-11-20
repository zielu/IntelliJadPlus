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

import net.stevechaloner.idea.util.properties.DOMable;
import net.stevechaloner.idea.util.properties.PropertyDescriptor;
import net.stevechaloner.intellijad.config.rules.RenderRule;
import net.stevechaloner.intellijad.config.rules.RuleContext;

/**
 * A command line property descriptor can be rendered in a form suitable for
 * a command-line parameter.
 *
 * @author Steve Chaloner
 */
public interface CommandLinePropertyDescriptor<T> extends PropertyDescriptor<T> {
    /**
     * Gets the option of the property.
     *
     * @param ruleContext the context the rule is evaluating in
     * @param domable     the domable
     * @return the option name
     */
    String getOption(RuleContext ruleContext,
                     DOMable<T> domable);

    /**
     * Gets the render rule of the property.
     *
     * @return the render rule
     */
    RenderRule getRenderRule();
}
