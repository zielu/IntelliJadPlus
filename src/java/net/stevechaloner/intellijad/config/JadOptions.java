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

import net.stevechaloner.intellijad.config.rules.RenderRuleFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Configuration options supported by the Jad application.
 *
 * @author Steve Chaloner
 */
class JadOptions
{
    static final CommandLinePropertyDescriptor<Boolean> ANNOTATE = createBooleanProperty("a");
    static final CommandLinePropertyDescriptor<Boolean> ANNOTATE_FULLY = createBooleanProperty("af");
    static final CommandLinePropertyDescriptor<Boolean> CLEAR_PREFIXES = createBooleanProperty("clear");
    static final CommandLinePropertyDescriptor<Boolean> DEAD = createBooleanProperty("dead");
    static final CommandLinePropertyDescriptor<Boolean> DEFAULT_INITIALIZERS = createBooleanProperty("i");
    static final CommandLinePropertyDescriptor<Boolean> DISASSEMBLER_ONLY = createBooleanProperty("dis");
    static final CommandLinePropertyDescriptor<Boolean> FIELDS_FIRST = createBooleanProperty("ff");
    static final CommandLinePropertyDescriptor<String> FILE_EXTENSION = createStringProperty("s", "java");
    static final CommandLinePropertyDescriptor<Boolean> FULLY_QUALIFIED_NAMES = createBooleanProperty("f");
    static final CommandLinePropertyDescriptor<Boolean> LINE_NUMBERS_AS_COMMENTS = createBooleanProperty("lnc");
    static final CommandLinePropertyDescriptor<Boolean> NOCAST = createBooleanProperty("nocast");
    static final CommandLinePropertyDescriptor<Boolean> NOCLASS = createBooleanProperty("noclass");
    static final CommandLinePropertyDescriptor<Boolean> NOCODE = createBooleanProperty("nocode");
    static final CommandLinePropertyDescriptor<Boolean> NOCONV = createBooleanProperty("noconv");
    static final CommandLinePropertyDescriptor<Boolean> NOCTOR = createBooleanProperty("noctor");
    static final CommandLinePropertyDescriptor<Boolean> NODOS = createBooleanProperty("nodos");
    static final CommandLinePropertyDescriptor<Boolean> NOFD = createBooleanProperty("nofd");
    static final CommandLinePropertyDescriptor<Boolean> NOINNER = createBooleanProperty("noinner");
    static final CommandLinePropertyDescriptor<Boolean> NOLVT = createBooleanProperty("nolvt");
    static final CommandLinePropertyDescriptor<Boolean> NONLB = createBooleanProperty("nonlb");
    static final CommandLinePropertyDescriptor<String> OUTPUT_DIRECTORY = createStringProperty("d");
    static final CommandLinePropertyDescriptor<Boolean> OVERWRITE = createBooleanProperty("o");
    static final CommandLinePropertyDescriptor<Integer> LIMIT_INT_RADIX = createIntegerProperty("radix", 10);
    static final CommandLinePropertyDescriptor<Integer> LIMIT_LONG_RADIX = createIntegerProperty("lradix", 10);
    static final CommandLinePropertyDescriptor<Integer> LIMIT_MAX_STRING_LENGTH = createIntegerProperty("l", 64);
    static final CommandLinePropertyDescriptor<Integer> LIMIT_PACK_FIELDS = createIntegerProperty("pv", 3);
    static final CommandLinePropertyDescriptor<String> PREFIX_NUMERICAL_CLASSES = createStringProperty("pc", "_cls");
    static final CommandLinePropertyDescriptor<String> PREFIX_NUMERICAL_FIELDS = createStringProperty("pf", "_fld");
    static final CommandLinePropertyDescriptor<String> PREFIX_NUMERICAL_LOCALS = createStringProperty("pl", "_lcl");
    static final CommandLinePropertyDescriptor<String> PREFIX_NUMERICAL_METHODS = createStringProperty("pm", "_mth");
    static final CommandLinePropertyDescriptor<String> PREFIX_NUMERICAL_PARAMETERS = createStringProperty("pp", "_prm");
    static final CommandLinePropertyDescriptor<String> PREFIX_PACKAGES = createStringProperty("pa");
    static final CommandLinePropertyDescriptor<String> PREFIX_UNUSED_EXCEPTIONS = createStringProperty("pe", "_ex");
    static final CommandLinePropertyDescriptor<Boolean> REDUNDANT_BRACES = createBooleanProperty("b");
    static final CommandLinePropertyDescriptor<Boolean> RESTORE_PACKAGES = createBooleanProperty("r");
    static final CommandLinePropertyDescriptor<Boolean> SPLIT_STRINGS_AT_NEWLINE = createBooleanProperty("nl");
    static final CommandLinePropertyDescriptor<Boolean> SAFE = createBooleanProperty("safe");
    static final CommandLinePropertyDescriptor<Boolean> SPACE_AFTER_KEYWORD = createBooleanProperty("space");
    static final CommandLinePropertyDescriptor<Boolean> STATISTICS = createBooleanProperty("stat");
    static final CommandLinePropertyDescriptor<Boolean> USE_TABS = createBooleanProperty("t");
    static final CommandLinePropertyDescriptor<Boolean> VERBOSE = createBooleanProperty("v");

    /**
     *
     */
    private JadOptions()
    {}

    /**
     * Creates a standard boolean property that will be rendered when its value is not empty.
     *
     * @param name the name of the property
     * @return the property
     */
    private static CommandLinePropertyDescriptor<String> createStringProperty(@NotNull String name)
    {
        return createStringProperty(name,
                                    null);
    }

    /**
     * Creates a standard boolean property that will be rendered when its value is not empty.
     *
     * @param name         the name of the property
     * @param defaultValue the default value of the property
     * @return the property
     */
    private static CommandLinePropertyDescriptor<String> createStringProperty(@NotNull String name,
                                                                              @Nullable String defaultValue)
    {
        return new ImmutableCommandLinePropertyDescriptor<String>(name,
                                                                  defaultValue,
                                                                  RenderRuleFactory.getRenderRule(RenderRuleFactory.StringRules.NOT_EMPTY),
                                                                  RenderType.VALUE);
    }

    /**
     * Creates a standard boolean property that will be rendered when its value is true.
     *
     * @param name the name of the property
     * @return the property
     */
    private static CommandLinePropertyDescriptor<Boolean> createBooleanProperty(@NotNull String name)
    {
        return new ImmutableCommandLinePropertyDescriptor<Boolean>(name,
                                                                   false,
                                                                   RenderRuleFactory.getRenderRule(RenderRuleFactory.BooleanRules.TRUE));
    }

    /**
     * Creates a standard boolean property that will be rendered when its value is not negative.
     *
     * @param name         the name of the property
     * @param defaultValue the default value the property
     * @return the property
     */
    private static CommandLinePropertyDescriptor<Integer> createIntegerProperty(@NotNull String name,
                                                                                @Nullable Integer defaultValue)
    {
        return new ImmutableCommandLinePropertyDescriptor<Integer>(name,
                                                                   defaultValue,
                                                                   RenderRuleFactory.getRenderRule(RenderRuleFactory.IntegerRules.NON_NEGATIVE),
                                                                   RenderType.VALUE_NO_SPACE);
    }
}
