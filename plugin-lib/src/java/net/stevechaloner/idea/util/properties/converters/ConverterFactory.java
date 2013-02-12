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

package net.stevechaloner.idea.util.properties.converters;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.stevechaloner.idea.util.properties.DOMableCollectionContentType;

/**
 * .
 *
 * @author Steve Chaloner
 */
public class ConverterFactory
{
    private static final Converter<Boolean> BOOLEAN_CONVERTER = new AbstractConverter<Boolean>()
    {
        @NotNull
        public String toString(@Nullable Boolean t)
        {
            return t == null ? Boolean.FALSE.toString() : t.toString();
        }

        @NotNull
        public Boolean toType(@Nullable String s)
        {
            return Boolean.parseBoolean(s);
        }

        /** {@inheritDoc} */
        public DOMableCollectionContentType getContentType()
        {
            return DOMableCollectionContentType.BOOLEAN;
        }
    };

    private static final Converter<Integer> INTEGER_CONVERTER = new AbstractConverter<Integer>()
    {
        private final Integer standardInteger = 0;

        @NotNull
        public String toString(@Nullable Integer t)
        {
            return t == null ? standardInteger.toString() : t.toString();
        }

        @NotNull
        public Integer toType(@Nullable String s)
        {
            Integer i = standardInteger;
            if (s != null)
            {
                try
                {
                   i = Integer.parseInt(s);
                }
                catch (NumberFormatException e)
                {
                    i = standardInteger;
                }
            }
            return i;
        }

        /** {@inheritDoc} */
        public DOMableCollectionContentType getContentType()
        {
            return DOMableCollectionContentType.INTEGER;
        }
    };

    private static final Converter<String> STRING_CONVERTER = new AbstractConverter<String>()
    {
        @NotNull
        public String toString(@Nullable String t)
        {
            return t == null ?  "" : t;
        }

        @NotNull
        public String toType(@Nullable String s)
        {
            return toString(s);
        }

        /** {@inheritDoc} */
        public DOMableCollectionContentType getContentType()
        {
            return DOMableCollectionContentType.STRING;
        }
    };

    private ConverterFactory(){}

    public static Converter<Boolean> getBooleanConverter()
    {
        return BOOLEAN_CONVERTER;
    }

    public static Converter<Integer> getIntegerConverter()
    {
        return INTEGER_CONVERTER;
    }

    public static Converter<String> getStringConverter()
    {
        return STRING_CONVERTER;
    }
}
