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
import net.stevechaloner.idea.util.properties.DOMableCollectionContentType;
import net.stevechaloner.idea.util.properties.DOMableGeneric;
import net.stevechaloner.idea.util.properties.DOMablePropertyContainer;
import net.stevechaloner.idea.util.properties.DOMableTableModel;
import net.stevechaloner.idea.util.properties.ImmutablePropertyDescriptor;
import net.stevechaloner.idea.util.properties.PropertyContainer;
import net.stevechaloner.idea.util.properties.PropertyDescriptor;
import net.stevechaloner.idea.util.properties.converters.ConverterFactory;
import net.stevechaloner.intellijad.config.rules.RuleContext;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * The IntelliJad configuration.
 *
 * @author Steve Chaloner
 */
public class Config implements DOMable
{
    private static final PropertyDescriptor<String> DECOMPILE_ON_NAVIGATION = new ImmutablePropertyDescriptor<String>("decompile-on-navigation",
                                                                                                                      NavigationTriggeredDecompile.ALWAYS.getName());
    private static final PropertyDescriptor<Boolean> CREATE_OUTPUT_DIRECTORY = new ImmutablePropertyDescriptor<Boolean>("create-output-directory");
    private static final PropertyDescriptor<Boolean> ALWAYS_EXCLUDE_RECURSIVELY = new ImmutablePropertyDescriptor<Boolean>("always-exclude-recursively");
    private static final PropertyDescriptor<Boolean> CLEAR_AND_CLOSE_CONSOLE_ON_SUCCESS = new ImmutablePropertyDescriptor<Boolean>("clear-and-close-console-on-success");
    private static final PropertyDescriptor<Boolean> DECOMPILE_TO_MEMORY = new ImmutablePropertyDescriptor<Boolean>("decompile-to-memory", Boolean.TRUE);
    private static final PropertyDescriptor<ExclusionTableModel> EXCLUSION_TABLE_MODEL = new ImmutablePropertyDescriptor<ExclusionTableModel>("exclusion-table-model");
    private static final PropertyDescriptor<String> JAD_PATH = new ImmutablePropertyDescriptor<String>("jad-path");
    private static final PropertyDescriptor<Integer> LIMIT_INDENTATION = new ImmutablePropertyDescriptor<Integer>("indentation", 4);
    private static final PropertyDescriptor<Boolean> READ_ONLY = new ImmutablePropertyDescriptor<Boolean>("read-only");
    private static final PropertyDescriptor<Boolean> SORT = new ImmutablePropertyDescriptor<Boolean>("sort");
    private static final PropertyDescriptor<Boolean> USE_PROJECT_SPECIFIC_SETTINGS = new ImmutablePropertyDescriptor<Boolean>("use-project-specific-settings");
    private static final PropertyDescriptor<String> REFORMAT_STYLE = new ImmutablePropertyDescriptor<String>("reformat-style",
                                                                                                             CodeStyle.PREFERRED_STYLE.getName());
    private static final PropertyDescriptor<Boolean> CLEANUP_SOURCE_ROOTS = new ImmutablePropertyDescriptor<Boolean>("cleanup-source-roots",
                                                                                                                     Boolean.TRUE);

    /**
     * The persistence model.
     */
    private final DOMable domable;

    /**
     * The properties.
     */
    private final PropertyContainer<PropertyDescriptor, DOMable> propertyContainer;

    private final List<CommandLinePropertyDescriptor> commandLinePropertyDescriptors = new ArrayList<CommandLinePropertyDescriptor>();

    private final RuleContext ruleContext;

    public Config(RuleContext ruleContext)
    {
        this.ruleContext = ruleContext;
        ruleContext.setConfig(this);

        DOMablePropertyContainer dpc = new DOMablePropertyContainer(new ImmutablePropertyDescriptor("config"));
        registerBooleanProperty(JadOptions.ANNOTATE, dpc);
        registerBooleanProperty(JadOptions.ANNOTATE_FULLY, dpc);
        registerBooleanProperty(JadOptions.CLEAR_PREFIXES, dpc);
        registerBooleanProperty(CREATE_OUTPUT_DIRECTORY, dpc);
        registerStringProperty(DECOMPILE_ON_NAVIGATION, dpc);
        registerBooleanProperty(CLEAR_AND_CLOSE_CONSOLE_ON_SUCCESS, dpc);
        registerBooleanProperty(ALWAYS_EXCLUDE_RECURSIVELY, dpc);
        registerBooleanProperty(JadOptions.DEAD, dpc);
        registerBooleanProperty(DECOMPILE_TO_MEMORY, dpc);
        registerBooleanProperty(JadOptions.DEFAULT_INITIALIZERS, dpc);
        registerBooleanProperty(JadOptions.DISASSEMBLER_ONLY, dpc);
        registerStringProperty(JadOptions.FILE_EXTENSION, dpc);
        registerBooleanProperty(JadOptions.FIELDS_FIRST, dpc);
        registerBooleanProperty(JadOptions.FULLY_QUALIFIED_NAMES, dpc);
        registerIntegerProperty(LIMIT_INDENTATION, dpc);
        registerIntegerProperty(JadOptions.LIMIT_INT_RADIX, dpc);
        registerIntegerProperty(JadOptions.LIMIT_LONG_RADIX, dpc);
        registerIntegerProperty(JadOptions.LIMIT_MAX_STRING_LENGTH, dpc);
        registerIntegerProperty(JadOptions.LIMIT_PACK_FIELDS, dpc);
        registerBooleanProperty(JadOptions.LINE_NUMBERS_AS_COMMENTS, dpc);
        registerBooleanProperty(JadOptions.NOCONV, dpc);
        registerBooleanProperty(JadOptions.NOCAST, dpc);
        registerBooleanProperty(JadOptions.NOCLASS, dpc);
        registerBooleanProperty(JadOptions.NOCODE, dpc);
        registerBooleanProperty(JadOptions.NODOS, dpc);
        registerBooleanProperty(JadOptions.NOCTOR, dpc);
        registerBooleanProperty(JadOptions.NOFD, dpc);
        registerBooleanProperty(JadOptions.NOINNER, dpc);
        registerBooleanProperty(JadOptions.NOLVT, dpc);
        registerBooleanProperty(JadOptions.NONLB, dpc);
        registerStringProperty(JadOptions.OUTPUT_DIRECTORY, dpc);
        registerBooleanProperty(JadOptions.OVERWRITE, dpc);
        registerStringProperty(JadOptions.PREFIX_NUMERICAL_CLASSES, dpc);
        registerStringProperty(JadOptions.PREFIX_NUMERICAL_FIELDS, dpc);
        registerStringProperty(JadOptions.PREFIX_NUMERICAL_LOCALS, dpc);
        registerStringProperty(JadOptions.PREFIX_NUMERICAL_METHODS, dpc);
        registerStringProperty(JadOptions.PREFIX_NUMERICAL_PARAMETERS, dpc);
        registerStringProperty(JadOptions.PREFIX_PACKAGES, dpc);
        registerStringProperty(JadOptions.PREFIX_UNUSED_EXCEPTIONS, dpc);
        registerBooleanProperty(READ_ONLY, dpc);
        registerBooleanProperty(JadOptions.REDUNDANT_BRACES, dpc);
        registerBooleanProperty(JadOptions.RESTORE_PACKAGES, dpc);
        registerBooleanProperty(JadOptions.SAFE, dpc);
        registerBooleanProperty(SORT, dpc);
        registerBooleanProperty(JadOptions.SPACE_AFTER_KEYWORD, dpc);
        registerBooleanProperty(JadOptions.SPLIT_STRINGS_AT_NEWLINE, dpc);
        registerBooleanProperty(JadOptions.STATISTICS, dpc);
        registerBooleanProperty(JadOptions.USE_TABS, dpc);
        registerBooleanProperty(JadOptions.VERBOSE, dpc);
        registerStringProperty(JAD_PATH, dpc);
        registerBooleanProperty(USE_PROJECT_SPECIFIC_SETTINGS, dpc);
        registerStringProperty(REFORMAT_STYLE, dpc);
        registerBooleanProperty(CLEANUP_SOURCE_ROOTS, dpc);

        dpc.put(EXCLUSION_TABLE_MODEL,
                new DOMableTableModel(EXCLUSION_TABLE_MODEL,
                                      new ExclusionTableModel()));

        this.domable = dpc;
        this.propertyContainer = dpc;
    }

    /**
     * Register the property with the container.
     *
     * @param pd  the property's descriptor
     * @param dpc the property container
     */
    private void registerBooleanProperty(PropertyDescriptor<Boolean> pd,
                                         DOMablePropertyContainer dpc)
    {
        dpc.put(pd,
                new DOMableGeneric<Boolean>(pd,
                                            ConverterFactory.getBooleanConverter(),
                                            DOMableCollectionContentType.BOOLEAN));
    }

    /**
     * Register the property with the container, the rule context and
     * the command line property manager.
     *
     * @param clpd the command line property's descriptor
     * @param dpc  the property container
     */
    private void registerBooleanProperty(CommandLinePropertyDescriptor<Boolean> clpd,
                                         DOMablePropertyContainer dpc)
    {
        dpc.put(clpd,
                new DOMableGeneric<Boolean>(clpd,
                                            ConverterFactory.getBooleanConverter(),
                                            DOMableCollectionContentType.BOOLEAN));
        ruleContext.addProperty(clpd);
        commandLinePropertyDescriptors.add(clpd);
    }

    /**
     * Register the property with the container.
     *
     * @param pd  the property's descriptor
     * @param dpc the property container
     */
    private void registerIntegerProperty(PropertyDescriptor<Integer> pd,
                                         DOMablePropertyContainer dpc)
    {
        dpc.put(pd,
                new DOMableGeneric<Integer>(pd,
                                            ConverterFactory.getIntegerConverter(),
                                            DOMableCollectionContentType.INTEGER));
    }

    /**
     * Register the property with the container, the rule context and
     * the command line property manager.
     *
     * @param clpd the command line property's descriptor
     * @param dpc  the property container
     */
    private void registerIntegerProperty(CommandLinePropertyDescriptor<Integer> clpd,
                                         DOMablePropertyContainer dpc)
    {
        dpc.put(clpd,
                new DOMableGeneric<Integer>(clpd,
                                            ConverterFactory.getIntegerConverter(),
                                            DOMableCollectionContentType.INTEGER));
        ruleContext.addProperty(clpd);
        commandLinePropertyDescriptors.add(clpd);
    }

    /**
     * Register the property with the container.
     *
     * @param pd  the property's descriptor
     * @param dpc the property container
     */
    private void registerStringProperty(PropertyDescriptor<String> pd,
                                        DOMablePropertyContainer dpc)
    {
        dpc.put(pd,
                new DOMableGeneric<String>(pd,
                                           ConverterFactory.getStringConverter(),
                                           DOMableCollectionContentType.STRING));
    }

    /**
     * Register the property with the container, the rule context and
     * the command line property manager.
     *
     * @param clpd the command line property's descriptor
     * @param dpc  the property container
     */
    private void registerStringProperty(CommandLinePropertyDescriptor<String> clpd,
                                        DOMablePropertyContainer dpc)
    {
        dpc.put(clpd,
                new DOMableGeneric<String>(clpd,
                                           ConverterFactory.getStringConverter(),
                                           DOMableCollectionContentType.STRING));
        ruleContext.addProperty(clpd);
        commandLinePropertyDescriptors.add(clpd);
    }

    public boolean isNocast()
    {
        return JadOptions.NOCAST.getValue(propertyContainer.get(JadOptions.NOCAST));
    }

    public void setNocast(boolean nocast)
    {
        DOMableGeneric<Boolean> value = (DOMableGeneric<Boolean>) propertyContainer.get(JadOptions.NOCAST);
        value.setValue(nocast);
    }

    public boolean isNoclass()
    {
        return JadOptions.NOCLASS.getValue(propertyContainer.get(JadOptions.NOCLASS));
    }

    public void setNoclass(boolean noclass)
    {
        DOMableGeneric<Boolean> value = (DOMableGeneric<Boolean>) propertyContainer.get(JadOptions.NOCLASS);
        value.setValue(noclass);
    }

    public String getDecompileOnNavigation()
    {
        return DECOMPILE_ON_NAVIGATION.getValue(propertyContainer.get(DECOMPILE_ON_NAVIGATION));
    }

    public void setDecompileOnNavigation(String confirmNavigationTriggeredDecompile)
    {
        DOMableGeneric<String> value = (DOMableGeneric<String>) propertyContainer.get(DECOMPILE_ON_NAVIGATION);
        value.setValue(confirmNavigationTriggeredDecompile);
    }

    public boolean isReadOnly()
    {
        return READ_ONLY.getValue(propertyContainer.get(READ_ONLY));
    }

    public void setReadOnly(boolean readOnly)
    {
        DOMableGeneric<Boolean> value = (DOMableGeneric<Boolean>) propertyContainer.get(READ_ONLY);
        value.setValue(readOnly);
    }

    public boolean isAnnotate()
    {
        return JadOptions.ANNOTATE.getValue(propertyContainer.get(JadOptions.ANNOTATE));
    }

    public void setAnnotate(boolean annotate)
    {
        DOMableGeneric<Boolean> value = (DOMableGeneric<Boolean>) propertyContainer.get(JadOptions.ANNOTATE);
        value.setValue(annotate);
    }

    public boolean isAnnotateFully()
    {
        return JadOptions.ANNOTATE_FULLY.getValue(propertyContainer.get(JadOptions.ANNOTATE_FULLY));
    }

    public void setAnnotateFully(boolean annotateFully)
    {
        DOMableGeneric<Boolean> value = (DOMableGeneric<Boolean>) propertyContainer.get(JadOptions.ANNOTATE_FULLY);
        value.setValue(annotateFully);
    }

    public boolean isRedundantBraces()
    {
        return JadOptions.REDUNDANT_BRACES.getValue(propertyContainer.get(JadOptions.REDUNDANT_BRACES));
    }

    public void setRedundantBraces(boolean redundantBraces)
    {
        DOMableGeneric<Boolean> value = (DOMableGeneric<Boolean>) propertyContainer.get(JadOptions.REDUNDANT_BRACES);
        value.setValue(redundantBraces);
    }

    public boolean isClearPrefixes()
    {
        return JadOptions.CLEAR_PREFIXES.getValue(propertyContainer.get(JadOptions.CLEAR_PREFIXES));
    }

    public void setClearPrefixes(boolean clearPrefixes)
    {
        DOMableGeneric<Boolean> value = (DOMableGeneric<Boolean>) propertyContainer.get(JadOptions.CLEAR_PREFIXES);
        value.setValue(clearPrefixes);
    }

    public String getOutputDirectory()
    {
        return JadOptions.OUTPUT_DIRECTORY.getValue(propertyContainer.get(JadOptions.OUTPUT_DIRECTORY));
    }

    public void setOutputDirectory(String outputDirectory)
    {
        DOMableGeneric<String> value = (DOMableGeneric<String>) propertyContainer.get(JadOptions.OUTPUT_DIRECTORY);
        value.setValue(outputDirectory);
    }

    public boolean isDead()
    {
        return JadOptions.DEAD.getValue(propertyContainer.get(JadOptions.DEAD));
    }

    public void setDead(boolean dead)
    {
        DOMableGeneric<Boolean> value = (DOMableGeneric<Boolean>) propertyContainer.get(JadOptions.DEAD);
        value.setValue(dead);
    }

    public boolean isDissassemblerOnly()
    {
        return JadOptions.DISASSEMBLER_ONLY.getValue(propertyContainer.get(JadOptions.DISASSEMBLER_ONLY));
    }

    public void setDissassemblerOnly(boolean dissassemblerOnly)
    {
        DOMableGeneric<Boolean> value = (DOMableGeneric<Boolean>) propertyContainer.get(JadOptions.DISASSEMBLER_ONLY);
        value.setValue(dissassemblerOnly);
    }

    public boolean isFullyQualifiedNames()
    {
        return JadOptions.FULLY_QUALIFIED_NAMES.getValue(propertyContainer.get(JadOptions.FULLY_QUALIFIED_NAMES));
    }

    public void setFullyQualifiedNames(boolean fullyQualifiedNames)
    {
        DOMableGeneric<Boolean> value = (DOMableGeneric<Boolean>) propertyContainer.get(JadOptions.FULLY_QUALIFIED_NAMES);
        value.setValue(fullyQualifiedNames);
    }

    public boolean isFieldsFirst()
    {
        return JadOptions.FIELDS_FIRST.getValue(propertyContainer.get(JadOptions.FIELDS_FIRST));
    }

    public void setFieldsFirst(boolean fieldsFirst)
    {
        DOMableGeneric<Boolean> value = (DOMableGeneric<Boolean>) propertyContainer.get(JadOptions.FIELDS_FIRST);
        value.setValue(fieldsFirst);
    }

    public boolean isDefaultInitializers()
    {
        return JadOptions.DEFAULT_INITIALIZERS.getValue(propertyContainer.get(JadOptions.DEFAULT_INITIALIZERS));
    }

    public void setDefaultInitializers(boolean defaultInitializers)
    {
        DOMableGeneric<Boolean> value = (DOMableGeneric<Boolean>) propertyContainer.get(JadOptions.DEFAULT_INITIALIZERS);
        value.setValue(defaultInitializers);
    }

    public Integer getMaxStringLength()
    {
        return JadOptions.LIMIT_MAX_STRING_LENGTH.getValue(propertyContainer.get(JadOptions.LIMIT_MAX_STRING_LENGTH));
    }

    public void setMaxStringLength(Integer maxStringLength)
    {
        DOMableGeneric<Integer> value = (DOMableGeneric<Integer>) propertyContainer.get(JadOptions.LIMIT_MAX_STRING_LENGTH);
        value.setValue(maxStringLength);
    }

    public boolean isLineNumbersAsComments()
    {
        return JadOptions.LINE_NUMBERS_AS_COMMENTS.getValue(propertyContainer.get(JadOptions.LINE_NUMBERS_AS_COMMENTS));
    }

    public void setLineNumbersAsComments(boolean lineNumbersAsComments)
    {
        DOMableGeneric<Boolean> value = (DOMableGeneric<Boolean>) propertyContainer.get(JadOptions.LINE_NUMBERS_AS_COMMENTS);
        value.setValue(lineNumbersAsComments);
    }

    public Integer getLongRadix()
    {
        return JadOptions.LIMIT_LONG_RADIX.getValue(propertyContainer.get(JadOptions.LIMIT_LONG_RADIX));
    }

    public void setLongRadix(Integer longRadix)
    {
        DOMableGeneric<Integer> value = (DOMableGeneric<Integer>) propertyContainer.get(JadOptions.LIMIT_LONG_RADIX);
        value.setValue(longRadix);
    }

    public boolean isSplitStringsAtNewline()
    {
        return JadOptions.SPLIT_STRINGS_AT_NEWLINE.getValue(propertyContainer.get(JadOptions.SPLIT_STRINGS_AT_NEWLINE));
    }

    public void setSplitStringsAtNewline(boolean splitStringsAtNewline)
    {
        DOMableGeneric<Boolean> value = (DOMableGeneric<Boolean>) propertyContainer.get(JadOptions.SPLIT_STRINGS_AT_NEWLINE);
        value.setValue(splitStringsAtNewline);
    }

    public boolean isNoconv()
    {
        return JadOptions.NOCONV.getValue(propertyContainer.get(JadOptions.NOCONV));
    }

    public void setNoconv(boolean noconv)
    {
        DOMableGeneric<Boolean> value = (DOMableGeneric<Boolean>) propertyContainer.get(JadOptions.NOCONV);
        value.setValue(noconv);
    }

    public boolean isNocode()
    {
        return JadOptions.NOCODE.getValue(propertyContainer.get(JadOptions.NOCODE));
    }

    public void setNocode(boolean nocode)
    {
        DOMableGeneric<Boolean> value = (DOMableGeneric<Boolean>) propertyContainer.get(JadOptions.NOCODE);
        value.setValue(nocode);
    }

    public boolean isNoctor()
    {
        return JadOptions.NOCTOR.getValue(propertyContainer.get(JadOptions.NOCTOR));
    }

    public void setNoctor(boolean noctor)
    {
        DOMableGeneric<Boolean> value = (DOMableGeneric<Boolean>) propertyContainer.get(JadOptions.NOCTOR);
        value.setValue(noctor);
    }

    public boolean isNodos()
    {
        return JadOptions.NODOS.getValue(propertyContainer.get(JadOptions.NODOS));
    }

    public void setNodos(boolean nodos)
    {
        DOMableGeneric<Boolean> value = (DOMableGeneric<Boolean>) propertyContainer.get(JadOptions.NODOS);
        value.setValue(nodos);
    }

    public boolean isNofd()
    {
        return JadOptions.NOFD.getValue(propertyContainer.get(JadOptions.NOFD));
    }

    public void setNofd(boolean nofd)
    {
        DOMableGeneric<Boolean> value = (DOMableGeneric<Boolean>) propertyContainer.get(JadOptions.NOFD);
        value.setValue(nofd);
    }

    public boolean isNoinner()
    {
        return JadOptions.NOINNER.getValue(propertyContainer.get(JadOptions.NOINNER));
    }

    public void setNoinner(boolean noinner)
    {
        DOMableGeneric<Boolean> value = (DOMableGeneric<Boolean>) propertyContainer.get(JadOptions.NOINNER);
        value.setValue(noinner);
    }

    public boolean isNolvt()
    {
        return JadOptions.NOLVT.getValue(propertyContainer.get(JadOptions.NOLVT));
    }

    public void setNolvt(boolean nolvt)
    {
        DOMableGeneric<Boolean> value = (DOMableGeneric<Boolean>) propertyContainer.get(JadOptions.NOLVT);
        value.setValue(nolvt);
    }

    public boolean isNonlb()
    {
        return JadOptions.NONLB.getValue(propertyContainer.get(JadOptions.NONLB));
    }

    public void setNonlb(boolean nonlb)
    {
        DOMableGeneric<Boolean> value = (DOMableGeneric<Boolean>) propertyContainer.get(JadOptions.NONLB);
        value.setValue(nonlb);

    }

    public Integer getIntRadix()
    {
        return JadOptions.LIMIT_INT_RADIX.getValue(propertyContainer.get(JadOptions.LIMIT_INT_RADIX));
    }

    public void setIntRadix(Integer intRadix)
    {
        DOMableGeneric<Integer> value = (DOMableGeneric<Integer>) propertyContainer.get(JadOptions.LIMIT_INT_RADIX);
        value.setValue(intRadix);
    }

    public String getFileExtension()
    {
        return JadOptions.FILE_EXTENSION.getValue(propertyContainer.get(JadOptions.FILE_EXTENSION));
    }

    public boolean isSafe()
    {
        return JadOptions.SAFE.getValue(propertyContainer.get(JadOptions.SAFE));
    }

    public void setSafe(boolean safe)
    {
        DOMableGeneric<Boolean> value = (DOMableGeneric<Boolean>) propertyContainer.get(JadOptions.SAFE);
        value.setValue(safe);
    }

    public boolean isSpaceAfterKeyword()
    {
        return JadOptions.SPACE_AFTER_KEYWORD.getValue(propertyContainer.get(JadOptions.SPACE_AFTER_KEYWORD));
    }

    public void setSpaceAfterKeyword(boolean spaceAfterKeyword)
    {
        DOMableGeneric<Boolean> value = (DOMableGeneric<Boolean>) propertyContainer.get(JadOptions.SPACE_AFTER_KEYWORD);
        value.setValue(spaceAfterKeyword);
    }

    public Integer getIndentation()
    {
        return LIMIT_INDENTATION.getValue(propertyContainer.get(LIMIT_INDENTATION));
    }

    public void setIndentation(Integer indentation)
    {
        DOMableGeneric<Integer> value = (DOMableGeneric<Integer>) propertyContainer.get(LIMIT_INDENTATION);
        value.setValue(indentation);
    }

    public boolean isUseTabs()
    {
        return JadOptions.USE_TABS.getValue(propertyContainer.get(JadOptions.USE_TABS));
    }

    public void setUseTabs(boolean useTabs)
    {
        DOMableGeneric<Boolean> value = (DOMableGeneric<Boolean>) propertyContainer.get(JadOptions.USE_TABS);
        value.setValue(useTabs);
    }

    public String getPrefixPackages()
    {
        return JadOptions.PREFIX_PACKAGES.getValue(propertyContainer.get(JadOptions.PREFIX_PACKAGES));
    }

    public void setPrefixPackages(String prefixPackages)
    {
        DOMableGeneric<String> value = (DOMableGeneric<String>) propertyContainer.get(JadOptions.PREFIX_PACKAGES);
        value.setValue(prefixPackages);
    }

    public String getPrefixNumericalClasses()
    {
        return JadOptions.PREFIX_NUMERICAL_CLASSES.getValue(propertyContainer.get(JadOptions.PREFIX_NUMERICAL_CLASSES));
    }

    public void setPrefixNumericalClasses(String prefixNumericalClasses)
    {
        DOMableGeneric<String> value = (DOMableGeneric<String>) propertyContainer.get(JadOptions.PREFIX_NUMERICAL_CLASSES);
        value.setValue(prefixNumericalClasses);
    }

    public String getPrefixUnusedExceptions()
    {
        return JadOptions.PREFIX_UNUSED_EXCEPTIONS.getValue(propertyContainer.get(JadOptions.PREFIX_UNUSED_EXCEPTIONS));
    }

    public void setPrefixUnusedExceptions(String prefixUnusedExceptions)
    {
        DOMableGeneric<String> value = (DOMableGeneric<String>) propertyContainer.get(JadOptions.PREFIX_UNUSED_EXCEPTIONS);
        value.setValue(prefixUnusedExceptions);
    }

    public String getPrefixNumericalFields()
    {
        return JadOptions.PREFIX_NUMERICAL_FIELDS.getValue(propertyContainer.get(JadOptions.PREFIX_NUMERICAL_FIELDS));
    }

    public void setPrefixNumericalFields(String prefixNumericalFields)
    {
        DOMableGeneric<String> value = (DOMableGeneric<String>) propertyContainer.get(JadOptions.PREFIX_NUMERICAL_FIELDS);
        value.setValue(prefixNumericalFields);
    }

    public String getPrefixNumericalLocals()
    {
        return JadOptions.PREFIX_NUMERICAL_LOCALS.getValue(propertyContainer.get(JadOptions.PREFIX_NUMERICAL_LOCALS));
    }

    public void setPrefixNumericalLocals(String prefixNumericalLocals)
    {
        DOMableGeneric<String> value = (DOMableGeneric<String>) propertyContainer.get(JadOptions.PREFIX_NUMERICAL_LOCALS);
        value.setValue(prefixNumericalLocals);
    }

    public String getPrefixNumericalMethods()
    {
        return JadOptions.PREFIX_NUMERICAL_METHODS.getValue(propertyContainer.get(JadOptions.PREFIX_NUMERICAL_METHODS));
    }

    public void setPrefixNumericalMethods(String prefixNumericalMethods)
    {
        DOMableGeneric<String> value = (DOMableGeneric<String>) propertyContainer.get(JadOptions.PREFIX_NUMERICAL_METHODS);
        value.setValue(prefixNumericalMethods);
    }

    public String getPrefixNumericalParameters()
    {
        return JadOptions.PREFIX_NUMERICAL_PARAMETERS.getValue(propertyContainer.get(JadOptions.PREFIX_NUMERICAL_PARAMETERS));
    }

    public void setPrefixNumericalParameters(String prefixNumericalParameters)
    {
        DOMableGeneric<String> value = (DOMableGeneric<String>) propertyContainer.get(JadOptions.PREFIX_NUMERICAL_PARAMETERS);
        value.setValue(prefixNumericalParameters);
    }

    public Integer getPackFields()
    {
        return JadOptions.LIMIT_PACK_FIELDS.getValue(propertyContainer.get(JadOptions.LIMIT_PACK_FIELDS));
    }

    public void setPackFields(Integer packFields)
    {
        DOMableGeneric<Integer> value = (DOMableGeneric<Integer>) propertyContainer.get(JadOptions.LIMIT_PACK_FIELDS);
        value.setValue(packFields);

    }

    public boolean isSort()
    {
        return SORT.getValue(propertyContainer.get(SORT));
    }

    public void setSort(boolean sort)
    {
        DOMableGeneric<Boolean> value = (DOMableGeneric<Boolean>) propertyContainer.get(SORT);
        value.setValue(sort);
    }

    public boolean isVerbose()
    {
        return JadOptions.VERBOSE.getValue(propertyContainer.get(JadOptions.VERBOSE));
    }

    public void setVerbose(boolean verbose)
    {
        DOMableGeneric<Boolean> value = (DOMableGeneric<Boolean>) propertyContainer.get(JadOptions.VERBOSE);
        value.setValue(verbose);
    }

    public boolean isOverwrite()
    {
        return JadOptions.OVERWRITE.getValue(propertyContainer.get(JadOptions.OVERWRITE));
    }

    public void setOverwrite(boolean overwrite)
    {
        DOMableGeneric<Boolean> value = (DOMableGeneric<Boolean>) propertyContainer.get(JadOptions.OVERWRITE);
        value.setValue(overwrite);
    }

    public boolean isStatistics()
    {
        return JadOptions.STATISTICS.getValue(propertyContainer.get(JadOptions.STATISTICS));
    }

    public void setStatistics(boolean statistics)
    {
        DOMableGeneric<Boolean> value = (DOMableGeneric<Boolean>) propertyContainer.get(JadOptions.STATISTICS);
        value.setValue(statistics);
    }

    public boolean isRestorePackages()
    {
        return JadOptions.RESTORE_PACKAGES.getValue(propertyContainer.get(JadOptions.RESTORE_PACKAGES));
    }

    public void setRestorePackages(boolean restorePackages)
    {
        DOMableGeneric<Boolean> value = (DOMableGeneric<Boolean>) propertyContainer.get(JadOptions.RESTORE_PACKAGES);
        value.setValue(restorePackages);
    }

    public String getJadPath()
    {
        return JAD_PATH.getValue(propertyContainer.get(JAD_PATH));
    }

    public void setJadPath(String jadPath)
    {
        DOMableGeneric<String> value = (DOMableGeneric<String>) propertyContainer.get(JAD_PATH);
        value.setValue(jadPath);
    }

    public boolean isDecompileToMemory()
    {
        return DECOMPILE_TO_MEMORY.getValue(propertyContainer.get(DECOMPILE_TO_MEMORY));
    }

    public void setDecompileToMemory(boolean decompileToMemory)
    {
        DOMableGeneric<Boolean> value = (DOMableGeneric<Boolean>) propertyContainer.get(DECOMPILE_TO_MEMORY);
        value.setValue(decompileToMemory);
    }

    public boolean isCreateOutputDirectory()
    {
        return CREATE_OUTPUT_DIRECTORY.getValue(propertyContainer.get(CREATE_OUTPUT_DIRECTORY));
    }

    public void setCreateOutputDirectory(boolean create)
    {
        DOMableGeneric<Boolean> value = (DOMableGeneric<Boolean>) propertyContainer.get(CREATE_OUTPUT_DIRECTORY);
        value.setValue(create);
    }

    public boolean isAlwaysExcludeRecursively()
    {
        return ALWAYS_EXCLUDE_RECURSIVELY.getValue(propertyContainer.get(ALWAYS_EXCLUDE_RECURSIVELY));
    }

    public void setAlwaysExcludeRecursively(boolean alwaysExcludeRecursively)
    {
        DOMableGeneric<Boolean> value = (DOMableGeneric<Boolean>) propertyContainer.get(ALWAYS_EXCLUDE_RECURSIVELY);
        value.setValue(alwaysExcludeRecursively);
    }

    public boolean isClearAndCloseConsoleOnSuccess()
    {
        return CLEAR_AND_CLOSE_CONSOLE_ON_SUCCESS.getValue(propertyContainer.get(CLEAR_AND_CLOSE_CONSOLE_ON_SUCCESS));
    }

    public void setClearAndCloseConsoleOnSuccess(boolean clearAndClose)
    {
        DOMableGeneric<Boolean> value = (DOMableGeneric<Boolean>) propertyContainer.get(CLEAR_AND_CLOSE_CONSOLE_ON_SUCCESS);
        value.setValue(clearAndClose);
    }

    public boolean isUseProjectSpecificSettings()
    {
        return USE_PROJECT_SPECIFIC_SETTINGS.getValue(propertyContainer.get(USE_PROJECT_SPECIFIC_SETTINGS));
    }

    public void setUseProjectSpecificSettings(boolean useProjectSpecificSettings)
    {
        DOMableGeneric<Boolean> value = (DOMableGeneric<Boolean>) propertyContainer.get(USE_PROJECT_SPECIFIC_SETTINGS);
        value.setValue(useProjectSpecificSettings);
    }

    public String getReformatStyle()
    {
        return REFORMAT_STYLE.getValue(propertyContainer.get(REFORMAT_STYLE));
    }

    public void setReformatStyle(String reformatStyle)
    {
        DOMableGeneric<String> value = (DOMableGeneric<String>) propertyContainer.get(REFORMAT_STYLE);
        value.setValue(reformatStyle);
    }

    public boolean isCleanupSourceRoots()
    {
        return CLEANUP_SOURCE_ROOTS.getValue(propertyContainer.get(CLEANUP_SOURCE_ROOTS));
    }

    public void setCleanupSourceRoots(boolean cleanupSourceRoots)
    {
        DOMableGeneric<Boolean> value = (DOMableGeneric<Boolean>) propertyContainer.get(CLEANUP_SOURCE_ROOTS);
        value.setValue(cleanupSourceRoots);
    }

    @NotNull
    public PropertyDescriptor getPropertyDescriptor()
    {
        return domable.getPropertyDescriptor();
    }

    @NotNull
    public Element write()
    {
        return domable.write();
    }

    /**
     * {@inheritDoc}
     */
    public void read(@NotNull Element element)
    {
        domable.read(element);
    }

    /**
     * {@inheritDoc}
     */
    public Object getValue()
    {
        return null;
    }

    public List<CommandLinePropertyDescriptor> getCommandLinePropertyDescriptors()
    {
        return commandLinePropertyDescriptors;
    }

    public String renderCommandLinePropertyDescriptors()
    {
        StringBuilder sb = new StringBuilder();

        for (CommandLinePropertyDescriptor pd : commandLinePropertyDescriptors)
        {
            sb.append(pd.getOption(ruleContext,
                                   propertyContainer.get(pd)));
        }

        return sb.toString();
    }

    public ExclusionTableModel getExclusionTableModel()
    {
        return EXCLUSION_TABLE_MODEL.getValue(propertyContainer.get(EXCLUSION_TABLE_MODEL));
    }

    public List<String> getArguments()
    {
        List<String> arguments = new ArrayList<String>();
        for (CommandLinePropertyDescriptor pd : commandLinePropertyDescriptors)
        {
            String option = pd.getOption(ruleContext,
                                         propertyContainer.get(pd));
            if (option != null)
            {
                arguments.add(option);
            }
        }
        return arguments;
    }

    public void copyFrom(Config config)
    {
        setNocast(config.isNocast());
        setNoclass(config.isNoclass());
        setDecompileOnNavigation(config.getDecompileOnNavigation());
        setReadOnly(config.isReadOnly());
        setAnnotate(config.isAnnotate());
        setAnnotateFully(config.isAnnotateFully());
        setRedundantBraces(config.isRedundantBraces());
        setClearPrefixes(config.isClearPrefixes());
        setOutputDirectory(config.getOutputDirectory());
        setDead(config.isDead());
        setDissassemblerOnly(config.isDissassemblerOnly());
        setFullyQualifiedNames(config.isFullyQualifiedNames());
        setFieldsFirst(config.isFieldsFirst());
        setDefaultInitializers(config.isDefaultInitializers());
        setMaxStringLength(config.getMaxStringLength());
        setLineNumbersAsComments(config.isLineNumbersAsComments());
        setLongRadix(config.getLongRadix());
        setSplitStringsAtNewline(config.isSplitStringsAtNewline());
        setNoconv(config.isNoconv());
        setNocode(config.isNocode());
        setNoctor(config.isNoctor());
        setNodos(config.isNodos());
        setNofd(config.isNofd());
        setNoinner(config.isNoinner());
        setNolvt(config.isNolvt());
        setNonlb(config.isNonlb());
        setIntRadix(config.getIntRadix());
        setSafe(config.isSafe());
        setSpaceAfterKeyword(config.isSpaceAfterKeyword());
        setIndentation(config.getIndentation());
        setUseTabs(config.isUseTabs());
        setPrefixPackages(config.getPrefixPackages());
        setPrefixNumericalClasses(config.getPrefixNumericalClasses());
        setPrefixUnusedExceptions(config.getPrefixUnusedExceptions());
        setPrefixNumericalFields(config.getPrefixNumericalFields());
        setPrefixNumericalLocals(config.getPrefixNumericalLocals());
        setPrefixNumericalMethods(config.getPrefixNumericalMethods());
        setPrefixNumericalParameters(config.getPrefixNumericalParameters());
        setPackFields(config.getPackFields());
        setSort(config.isSort());
        setVerbose(config.isVerbose());
        setOverwrite(config.isOverwrite());
        setStatistics(config.isStatistics());
        setRestorePackages(config.isRestorePackages());
        setJadPath(config.getJadPath());
        setDecompileToMemory(config.isDecompileToMemory());
        setCreateOutputDirectory(config.isCreateOutputDirectory());
        setAlwaysExcludeRecursively(config.isAlwaysExcludeRecursively());
        setClearAndCloseConsoleOnSuccess(config.isClearAndCloseConsoleOnSuccess());
        setUseProjectSpecificSettings(config.isUseProjectSpecificSettings());
        setReformatStyle(config.getReformatStyle());
        setCleanupSourceRoots(config.isCleanupSourceRoots());
    }
}