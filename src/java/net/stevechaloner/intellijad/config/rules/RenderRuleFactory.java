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

import com.intellij.openapi.util.text.StringUtil;

import net.stevechaloner.idea.util.properties.DOMable;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Steve Chaloner
 */
public class RenderRuleFactory {
    public enum BooleanRules {
        TRUE, FALSE
    }

    public enum StringRules {
        NOT_EMPTY
    }

    public enum IntegerRules {
        NON_NEGATIVE
    }

    public enum RuleRules {
        EVALUATE_TO_TRUE, EVALUATE_TO_FALSE
    }

    private static final RenderRule DEFAULT_RENDER_RULE = new RenderRule() {
        public boolean evaluate(@NotNull RuleContext ruleContext,
                                DOMable domable) {
            return true;
        }
    };

    private static final Map<StringRules, RenderRule<String>> STRING_RULES = new HashMap<StringRules, RenderRule<String>>() {
        {
            put(StringRules.NOT_EMPTY,
                    new RenderRule<String>() {
                        public boolean evaluate(@NotNull RuleContext ruleContext,
                                                DOMable<String> domable) {
                            return StringUtil.isNotEmpty(domable.getValue());
                        }
                    });
        }
    };

    private static final Map<IntegerRules, RenderRule<Integer>> INTEGER_RULES = new HashMap<IntegerRules, RenderRule<Integer>>() {
        {
            put(IntegerRules.NON_NEGATIVE,
                    new RenderRule<Integer>() {
                        public boolean evaluate(@NotNull RuleContext ruleContext,
                                                DOMable<Integer> domable) {
                            Integer value = domable.getValue();
                            return value != null && value > -1;
                        }
                    });
        }
    };

    private static final Map<BooleanRules, RenderRule<Boolean>> BOOLEAN_RULES = new HashMap<BooleanRules, RenderRule<Boolean>>() {
        {
            put(BooleanRules.TRUE,
                    new RenderRule<Boolean>() {
                        public boolean evaluate(@NotNull RuleContext ruleContext,
                                                DOMable<Boolean> domable) {
                            Boolean value = domable.getValue();
                            return value != null && value;
                        }
                    });
            put(BooleanRules.FALSE,
                    new RenderRule<Boolean>() {
                        public boolean evaluate(@NotNull RuleContext ruleContext,
                                                DOMable<Boolean> domable) {
                            Boolean value = domable.getValue();
                            return value != null && !value;
                        }
                    });
        }
    };

    @NotNull
    public static RenderRule getDefaultRenderRule() {
        return DEFAULT_RENDER_RULE;
    }

    @NotNull
    public static RenderRule<Boolean> getRenderRule(@NotNull BooleanRules ruleType) {
        return BOOLEAN_RULES.get(ruleType);
    }

    @NotNull
    public static RenderRule<String> getRenderRule(@NotNull StringRules ruleType) {
        return STRING_RULES.get(ruleType);
    }

    @NotNull
    public static RenderRule<Integer> getRenderRule(@NotNull IntegerRules ruleType) {
        return INTEGER_RULES.get(ruleType);
    }
}
