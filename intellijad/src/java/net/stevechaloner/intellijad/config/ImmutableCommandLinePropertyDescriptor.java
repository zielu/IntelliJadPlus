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
import net.stevechaloner.idea.util.properties.ImmutablePropertyDescriptor;
import net.stevechaloner.intellijad.config.rules.RenderRule;
import net.stevechaloner.intellijad.config.rules.RenderRuleFactory;
import net.stevechaloner.intellijad.config.rules.RuleContext;
import org.jetbrains.annotations.NotNull;

/**
 * An immutable version of the command line property descriptor.
 *
 * @author Steve Chaloner
 */
public class ImmutableCommandLinePropertyDescriptor<T> extends ImmutablePropertyDescriptor<T> implements CommandLinePropertyDescriptor<T>
{
    /**
     * Render type.
     */
    private final RenderType renderType;

    /**
     * The rendering rule.
     */
    private final RenderRule renderRule;

    /**
     * Initialises a new instance of this class with the given arguments.
     *
     * @param name the name of the property
     */
    public ImmutableCommandLinePropertyDescriptor(String name)
    {
        this(name,
             null,
             RenderRuleFactory.getDefaultRenderRule(),
             RenderType.NAME_ONLY);
    }

    /**
     * Initialises a new instance of this class with the given arguments.
     *
     * @param name       the name of the property
     * @param renderRule the rule to control rendering of this property
     */
    public ImmutableCommandLinePropertyDescriptor(String name,
                                                  RenderRule renderRule)
    {
        this(name,
             null,
             renderRule,
             RenderType.NAME_ONLY);
    }

    /**
     * Initialises a new instance of this class with the given arguments.
     *
     * @param name       the name of the property
     * @param defaultValue the default value of the property
     * @param renderRule the rule to control rendering of this property
     */
    public ImmutableCommandLinePropertyDescriptor(String name,
                                                  T defaultValue,
                                                  RenderRule renderRule)
    {
        this(name,
             defaultValue,
             renderRule,
             RenderType.NAME_ONLY);
    }

    /**
     * Initialises a new instance of this class with the given arguments.
     *
     * @param name         the name of the property
     * @param defaultValue the default value of the property
     * @param renderRule   the rule to control rendering of this property
     * @param renderType   the render type
     */
    public ImmutableCommandLinePropertyDescriptor(String name,
                                                  T defaultValue,
                                                  RenderRule renderRule,
                                                  RenderType renderType)
    {
        super(name,
              defaultValue);
        this.renderType = renderType;
        this.renderRule = renderRule;
    }

    /** {@inheritDoc} */
    public RenderRule getRenderRule()
    {
        return renderRule;
    }

    /** {@inheritDoc} */
    public String getOption(@NotNull RuleContext ruleContext,
                            DOMable<T> domable)
    {
        String s = "";
        if (renderRule.evaluate(ruleContext,
                                domable))
        {
            StringBuilder sb = new StringBuilder();
            switch (renderType)
            {
                case NAME_ONLY:
                    sb.append('-').append(getName()).append(' ');
                    break;
                case VALUE:
                    sb.append('-').append(getName()).append(' ').append(getValue(domable)).append(' ');
                    break;
                case VALUE_NO_SPACE:
                    sb.append('-').append(getName());
                    sb.append(getValue(domable)).append(' ');
                    break;
            }
            s = sb.toString();
        }
        return s;
    }
}
