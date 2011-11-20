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

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.PackageChooser;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.peer.PeerFactory;
import com.intellij.psi.PsiPackage;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.List;

import net.stevechaloner.idea.util.fs.ApplicationFileSelectionAction;
import net.stevechaloner.idea.util.fs.FileSelectionDescriptor;
import net.stevechaloner.idea.util.fs.ProjectFileSelectionAction;
import net.stevechaloner.intellijad.IntelliJadResourceBundle;

import org.jetbrains.annotations.Nullable;

/**
 * IntelliJad configuration form.  This class deals with both application- and
 * project-level configurations.
 */
public class ConfigForm
{
    private JPanel root;
    private JCheckBox useProjectSpecificIntelliJadCheckBox;
    @Control private JTextField outputDirectoryTextField;
    @Control private JButton outputDirBrowseButton;
    @Control private JCheckBox markDecompiledFilesAsCheckBox;
    @Control private JTextField classesWithNumericalNamesTextField;
    @Control private JTextField fieldsWithNumericalNamesTextField;
    @Control private JTextField localsWithNumericalNamesTextField;
    @Control private JTextField methodsWithNumericalNamesTextField;
    @Control private JTextField parametersWithNumericalNamesTextField;
    @Control private JTextField allPackagesTextField;
    @Control private JTextField unusedExceptionNamesTextField;
    @Control private JSpinner packFieldsWithTheSpinner;
    @Control private JSpinner splitStringsIntoPiecesSpinner;
    @Control private JSpinner spacesForIndentationSpinner;
    @Control private JSpinner displayLongsUsingRadixSpinner;
    @Control private JSpinner displayIntegersUsingRadixSpinner;
    @Control private JCheckBox printDefaultInitializersForCheckBox;
    @Control private JCheckBox generateRedundantBracesCheckBox;
    @Control private JCheckBox generateFullyQualifiedNamesCheckBox;
    @Control private JCheckBox suppressEmptyConstructorsCheckBox;
    @Control private JCheckBox clearAllPrefixesIncludingCheckBox;
    @Control private JCheckBox donTGenerateAuxiliaryCheckBox;
    @Control private JCheckBox donTDisambiguateFieldsCheckBox;
    @Control private JCheckBox originalLineNumbersAsCheckBox;
    @Control private JCheckBox useTabsInsteadOfCheckBox;
    @Control private JCheckBox spaceBetweenKeywordAndCheckBox;
    @Control private JCheckBox insertANewlineBeforeCheckBox;
    @Control private JCheckBox outputFieldsBeforeMethodsCheckBox;
    @Control private JCheckBox splitStringsOnNewlineCheckBox;
    @Control private JTextField jadTextField;
    @Control private JButton removeExclusionButton;
    @Control private JButton addExclusionButton;
    @Control private JTable exclusionTable;
    @Control private JComboBox navTriggeredDecomp;
    @Control private JCheckBox decompileToMemoryCheckBox;
    @Control private JTextField excludePackageTextField;
    @Control private JButton browseButton1;
    @Control private JCheckBox createIfDirectoryDoesnCheckBox;
    @Control private JCheckBox alwaysExcludePackagesRecursivelyCheckBox;
    @Control private JComboBox reformatStyle;
    @Control private JButton packageChooserButton;
    @Control private JCheckBox cleanupSourceRootsCheckBox;
    @Control private JCheckBox clearAndCloseConsoleCheckBox;

    private ExclusionTableModel exclusionTableModel;

    @Nullable
    private final Project project;

    /**
     * Initialises a new instance of this class with no project,
     * forcing it into generic (application-level) behaviour.
     */
    public ConfigForm()
    {
        this(null);
    }

    /**
     * Initialises a new instance of this class.
     *
     * @param project the bound project
     */
    public ConfigForm(@Nullable final Project project)
    {
        this.project = project;

        navTriggeredDecomp.addItem(NavigationTriggeredDecompile.ALWAYS);
        navTriggeredDecomp.addItem(NavigationTriggeredDecompile.ASK);
        navTriggeredDecomp.addItem(NavigationTriggeredDecompile.NEVER);

        reformatStyle.addItem(CodeStyle.PREFERRED_STYLE);
        reformatStyle.addItem(CodeStyle.DEBUGGABLE_STYLE);

        packFieldsWithTheSpinner.setModel(createSpinnerModel());
        splitStringsIntoPiecesSpinner.setModel(createSpinnerModel());
        spacesForIndentationSpinner.setModel(createSpinnerModel());
        displayLongsUsingRadixSpinner.setModel(new SpinnerRadixModel());
        displayIntegersUsingRadixSpinner.setModel(new SpinnerRadixModel());

        if (project != null)
        {
            packageChooserButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    PackageChooser packageChooser = PeerFactory.getInstance().getUIHelper().createPackageChooser(IntelliJadResourceBundle.message("message.exclude-package"),
                                                                                                                 project);
                    packageChooser.show();
                    switch (packageChooser.getExitCode())
                    {
                        case PackageChooser.OK_EXIT_CODE:
                            List<PsiPackage> selectedPackages = packageChooser.getSelectedPackages();
                            for (PsiPackage selectedPackage : selectedPackages)
                            {
                                exclusionTableModel.addExclusion(selectedPackage.getQualifiedName(),
                                                                 alwaysExcludePackagesRecursivelyCheckBox.isSelected(),
                                                                 true);
                            }
                            break;
                    }
                }
            });
        }
        else
        {
            // setControlsEnabled() won't be invoked during application-level
            // config so no other special handling is required
            packageChooserButton.setEnabled(false);
        }

        outputDirBrowseButton.addActionListener(project == null ?
                                  new ApplicationFileSelectionAction(outputDirectoryTextField,
                                                                     FileSelectionDescriptor.DIRECTORIES_ONLY) :
                                                                                                               new ProjectFileSelectionAction(project,
                                                                                                                                              outputDirectoryTextField,
                                                                                                                                              FileSelectionDescriptor.DIRECTORIES_ONLY));
        browseButton1.addActionListener(project == null ?
                                        new ApplicationFileSelectionAction(jadTextField,
                                                                           FileSelectionDescriptor.FILES_ONLY) :
                                                                                                               new ProjectFileSelectionAction(project,
                                                                                                                                              jadTextField,
                                                                                                                                              FileSelectionDescriptor.FILES_ONLY));

        decompileToMemoryCheckBox.addChangeListener(new ChangeListener()
        {
            public void stateChanged(ChangeEvent e)
            {
                toggleToDiskControls(project);
            }
        });

        addExclusionButton.setEnabled(false);
        excludePackageTextField.addKeyListener(new KeyAdapter()
        {
            public void keyReleased(KeyEvent keyEvent)
            {
                switch (keyEvent.getKeyCode())
                {
                    case KeyEvent.VK_ENTER:
                        if (addExclusionButton.isEnabled())
                        {
                            addExclusion();
                        }
                        break;
                    default:
                        String s = excludePackageTextField.getText();
                        addExclusionButton.setEnabled(s != null && s.length() != 0 && Character.isJavaIdentifierPart(s.charAt(s.length() - 1)));
                }
            }
        });
        addExclusionButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent actionEvent)
            {
                addExclusion();
            }
        });
        removeExclusionButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent actionEvent)
            {
                if (exclusionTableModel != null)
                {
                    int selectedRow = exclusionTable.getSelectedRow();
                    if (selectedRow != -1)
                    {
                        exclusionTableModel.removeRow(selectedRow);
                    }
                }
            }
        });
        if (project == null)
        {
            useProjectSpecificIntelliJadCheckBox.setVisible(false);
        }
        else
        {
            useProjectSpecificIntelliJadCheckBox.addChangeListener(new ChangeListener()
            {
                public void stateChanged(ChangeEvent e)
                {
                    setControlsEnabled(project,
                                       useProjectSpecificIntelliJadCheckBox.isSelected());
                }
            });
        }
    }

    /**
     * Toggles the use of the to-disk controls.
     *
     * @param project the current project
     */
    private void toggleToDiskControls(@Nullable Project project)
    {
        if (project == null ||
            useProjectSpecificIntelliJadCheckBox.isSelected())
        {
            // prevents an annoying model-driven state switch
            boolean decompileToMemory = decompileToMemoryCheckBox.isSelected();
            outputDirBrowseButton.setEnabled(!decompileToMemory);
            createIfDirectoryDoesnCheckBox.setEnabled(!decompileToMemory);
            outputDirectoryTextField.setEnabled(!decompileToMemory);
            markDecompiledFilesAsCheckBox.setEnabled(!decompileToMemory);
        }
    }



    /**
     * Toggle the controls based on global inheritance.
     *
     * @param project the current project
     * @param enabled true iff the project has specific settings
     */
    private void setControlsEnabled(@Nullable Project project,
                                    boolean enabled)
    {
        Class<? extends ConfigForm> thisClass = this.getClass();
        Field[] fields = thisClass.getDeclaredFields();
        try
        {
            for (Field field : fields)
            {
                if (field.isAnnotationPresent(Control.class))
                {
                    Object o = field.get(this);
                    if (o != null)
                    {
                        ((Component) o).setEnabled(enabled);
                    }
                }
            }
        }
        catch (IllegalAccessException e)
        {
            Logger.getInstance(getClass().getName()).error(e);
        }

        // special case
        if (enabled && decompileToMemoryCheckBox.isSelected())
        {
            toggleToDiskControls(project);
        }

        root.validate();
    }

    /**
     * Exclude a package from decompilation.
     */
    private void addExclusion()
    {
        if (exclusionTableModel != null)
        {
            String path = excludePackageTextField.getText();
            if (!StringUtil.isEmptyOrSpaces(path))
            {
                exclusionTableModel.addExclusion(path,
                                                 alwaysExcludePackagesRecursivelyCheckBox.isSelected(),
                                                 true);
            }
            excludePackageTextField.setText("");
            addExclusionButton.setEnabled(false);
        }
    }

    /**
     * Gets the root component.
     *
     * @return the root component
     */
    public JComponent getRoot()
    {
        return root;
    }

    /**
     * Some components aren't bound via the data-binding wizard.  This takes care of them.
     *
     * @param data the config object to read state from
     */
    private void setUnboundData(Config data)
    {
        exclusionTableModel = data.getExclusionTableModel();
        exclusionTable.setModel(exclusionTableModel);
        packFieldsWithTheSpinner.setValue(data.getPackFields());
        splitStringsIntoPiecesSpinner.setValue(data.getMaxStringLength());
        spacesForIndentationSpinner.setValue(data.getIndentation());
        displayLongsUsingRadixSpinner.setValue(data.getLongRadix());
        displayIntegersUsingRadixSpinner.setValue(data.getIntRadix());
        navTriggeredDecomp.setSelectedItem(NavigationTriggeredDecompile.getByName(data.getDecompileOnNavigation()));
        reformatStyle.setSelectedItem(CodeStyle.getByName(data.getReformatStyle()));
        useProjectSpecificIntelliJadCheckBox.setSelected(data.isUseProjectSpecificSettings());
    }

    /**
     * Some components aren't bound via the data-binding wizard.  This takes care of them.
     *
     * @param data the config object to write state to
     */
    private void getUnboundData(Config data)
    {
        data.setPackFields((Integer) packFieldsWithTheSpinner.getValue());
        data.setMaxStringLength((Integer) splitStringsIntoPiecesSpinner.getValue());
        data.setIndentation((Integer) spacesForIndentationSpinner.getValue());
        data.setLongRadix((Integer) displayLongsUsingRadixSpinner.getValue());
        data.setIntRadix((Integer) displayIntegersUsingRadixSpinner.getValue());
        data.setDecompileOnNavigation(((NavigationTriggeredDecompile) navTriggeredDecomp.getSelectedItem()).getName());
        data.setReformatStyle(((CodeStyle) reformatStyle.getSelectedItem()).getName());
        data.setUseProjectSpecificSettings(useProjectSpecificIntelliJadCheckBox.isSelected());
    }

    /**
     * Some components aren't bound via the data-binding wizard.  This takes care of detecting
     * changes in them.
     *
     * @param data the config object to compare to the current state
     * @return true iff the data is modified
     */
    private boolean isUnboundDataModified(Config data)
    {
        if (!packFieldsWithTheSpinner.getValue().equals(data.getPackFields()))
        {
            return true;
        }
        if (!splitStringsIntoPiecesSpinner.getValue().equals(data.getMaxStringLength()))
        {
            return true;
        }
        if (!spacesForIndentationSpinner.getValue().equals(data.getIndentation()))
        {
            return true;
        }
        if (!displayLongsUsingRadixSpinner.getValue().equals(data.getLongRadix()))
        {
            return true;
        }
        if (!displayIntegersUsingRadixSpinner.getValue().equals(data.getIntRadix()))
        {
            return true;
        }
        if (!data.getDecompileOnNavigation().equals(((NavigationTriggeredDecompile)navTriggeredDecomp.getSelectedItem()).getName()))
        {
            return true;
        }
        if (!data.getReformatStyle().equals(((CodeStyle)reformatStyle.getSelectedItem()).getName()))
        {
            return true;
        }
        if (exclusionTableModel != null && exclusionTableModel.equals(data.getExclusionTableModel()))
        {
            return true;
        }
        if (useProjectSpecificIntelliJadCheckBox.isSelected() != data.isUseProjectSpecificSettings())
        {
            return true;
        }
        return false;
    }

    /**
     * Convenience method to create a numeric {@link SpinnerModel}.
     *
     * @return a new spinner model
     */
    private static SpinnerModel createSpinnerModel()
    {
        SpinnerNumberModel model = new SpinnerNumberModel();
        model.setMinimum(0);
        return model;
    }

    /**
     * Checks if the two strings are equal, equating null and empty strings as the same.
     *
     * @param s1 comparison string
     * @param s2 comparison string
     * @return true if the strings have content that is different
     */
    private static boolean isModified(@Nullable String s1,
                                      @Nullable String s2)
    {
        s1 = s1 == null ? "" : s1;
        s2 = s2 == null ? "" : s2;

        return !s1.equals(s2);
    }

    public boolean isModified(Config data)
    {
        if (isUnboundDataModified(data))
        {
            return true;
        }
        if (isModified(jadTextField.getText(), data.getJadPath()))
        {
            return true;
        }
        if (isModified(outputDirectoryTextField.getText(), data.getOutputDirectory()))
        {
            return true;
        }
        if (createIfDirectoryDoesnCheckBox.isSelected() != data.isCreateOutputDirectory())
        {
            return true;
        }
        if (decompileToMemoryCheckBox.isSelected() != data.isDecompileToMemory())
        {
            return true;
        }
        if (clearAndCloseConsoleCheckBox.isSelected() != data.isClearAndCloseConsoleOnSuccess())
        {
            return true;
        }
        if (markDecompiledFilesAsCheckBox.isSelected() != data.isReadOnly())
        {
            return true;
        }
        if (printDefaultInitializersForCheckBox.isSelected() != data.isDefaultInitializers())
        {
            return true;
        }
        if (generateFullyQualifiedNamesCheckBox.isSelected() != data.isFullyQualifiedNames())
        {
            return true;
        }
        if (clearAllPrefixesIncludingCheckBox.isSelected() != data.isClearPrefixes())
        {
            return true;
        }
        if (generateRedundantBracesCheckBox.isSelected() != data.isRedundantBraces())
        {
            return true;
        }
        if (suppressEmptyConstructorsCheckBox.isSelected() != data.isNoctor())
        {
            return true;
        }
        if (donTGenerateAuxiliaryCheckBox.isSelected() != data.isNocast())
        {
            return true;
        }
        if (donTDisambiguateFieldsCheckBox.isSelected() != data.isNofd())
        {
            return true;
        }
        if (useTabsInsteadOfCheckBox.isSelected() != data.isUseTabs())
        {
            return true;
        }
        if (spaceBetweenKeywordAndCheckBox.isSelected() != data.isSpaceAfterKeyword())
        {
            return true;
        }
        if (originalLineNumbersAsCheckBox.isSelected() != data.isLineNumbersAsComments())
        {
            return true;
        }
        if (outputFieldsBeforeMethodsCheckBox.isSelected() != data.isFieldsFirst())
        {
            return true;
        }
        if (insertANewlineBeforeCheckBox.isSelected() != data.isNonlb())
        {
            return true;
        }
        if (splitStringsOnNewlineCheckBox.isSelected() != data.isSplitStringsAtNewline())
        {
            return true;
        }
        if (isModified(classesWithNumericalNamesTextField.getText(), data.getPrefixNumericalClasses()))
        {
            return true;
        }
        if (isModified(fieldsWithNumericalNamesTextField.getText(), data.getPrefixNumericalFields()))
        {
            return true;
        }
        if (isModified(localsWithNumericalNamesTextField.getText(), data.getPrefixNumericalLocals()))
        {
            return true;
        }
        if (isModified(methodsWithNumericalNamesTextField.getText(), data.getPrefixNumericalMethods()))
        {
            return true;
        }
        if (isModified(parametersWithNumericalNamesTextField.getText(), data.getPrefixNumericalParameters()))
        {
            return true;
        }
        if (isModified(allPackagesTextField.getText(), data.getPrefixPackages()))
        {
            return true;
        }
        if (isModified(unusedExceptionNamesTextField.getText(), data.getPrefixUnusedExceptions()))
        {
            return true;
        }
        if (alwaysExcludePackagesRecursivelyCheckBox.isSelected() != data.isAlwaysExcludeRecursively())
        {
            return true;
        }
        if (cleanupSourceRootsCheckBox.isSelected() != data.isCleanupSourceRoots())
        {
            return true;
        }
        return false;
    }

    public void setData(Config data)
    {
        setUnboundData(data);
        jadTextField.setText(data.getJadPath());
        outputDirectoryTextField.setText(data.getOutputDirectory());
        createIfDirectoryDoesnCheckBox.setSelected(data.isCreateOutputDirectory());
        decompileToMemoryCheckBox.setSelected(data.isDecompileToMemory());
        markDecompiledFilesAsCheckBox.setSelected(data.isReadOnly());
        clearAndCloseConsoleCheckBox.setSelected(data.isClearAndCloseConsoleOnSuccess());
        printDefaultInitializersForCheckBox.setSelected(data.isDefaultInitializers());
        generateFullyQualifiedNamesCheckBox.setSelected(data.isFullyQualifiedNames());
        clearAllPrefixesIncludingCheckBox.setSelected(data.isClearPrefixes());
        generateRedundantBracesCheckBox.setSelected(data.isRedundantBraces());
        suppressEmptyConstructorsCheckBox.setSelected(data.isNoctor());
        donTGenerateAuxiliaryCheckBox.setSelected(data.isNocast());
        donTDisambiguateFieldsCheckBox.setSelected(data.isNofd());
        useTabsInsteadOfCheckBox.setSelected(data.isUseTabs());
        spaceBetweenKeywordAndCheckBox.setSelected(data.isSpaceAfterKeyword());
        originalLineNumbersAsCheckBox.setSelected(data.isLineNumbersAsComments());
        outputFieldsBeforeMethodsCheckBox.setSelected(data.isFieldsFirst());
        insertANewlineBeforeCheckBox.setSelected(data.isNonlb());
        splitStringsOnNewlineCheckBox.setSelected(data.isSplitStringsAtNewline());
        classesWithNumericalNamesTextField.setText(data.getPrefixNumericalClasses());
        fieldsWithNumericalNamesTextField.setText(data.getPrefixNumericalFields());
        localsWithNumericalNamesTextField.setText(data.getPrefixNumericalLocals());
        methodsWithNumericalNamesTextField.setText(data.getPrefixNumericalMethods());
        parametersWithNumericalNamesTextField.setText(data.getPrefixNumericalParameters());
        allPackagesTextField.setText(data.getPrefixPackages());
        unusedExceptionNamesTextField.setText(data.getPrefixUnusedExceptions());
        alwaysExcludePackagesRecursivelyCheckBox.setSelected(data.isAlwaysExcludeRecursively());
        cleanupSourceRootsCheckBox.setSelected(data.isCleanupSourceRoots());
        
        if (project != null)
        {
            setControlsEnabled(project,
                               data.isUseProjectSpecificSettings());
        }
    }

    public void getData(Config data)
    {
        getUnboundData(data);
        data.setJadPath(jadTextField.getText());
        data.setOutputDirectory(outputDirectoryTextField.getText());
        data.setCreateOutputDirectory(createIfDirectoryDoesnCheckBox.isSelected());
        data.setDecompileToMemory(decompileToMemoryCheckBox.isSelected());
        data.setReadOnly(markDecompiledFilesAsCheckBox.isSelected());
        data.setClearAndCloseConsoleOnSuccess(clearAndCloseConsoleCheckBox.isSelected());
        data.setDefaultInitializers(printDefaultInitializersForCheckBox.isSelected());
        data.setFullyQualifiedNames(generateFullyQualifiedNamesCheckBox.isSelected());
        data.setClearPrefixes(clearAllPrefixesIncludingCheckBox.isSelected());
        data.setRedundantBraces(generateRedundantBracesCheckBox.isSelected());
        data.setNoctor(suppressEmptyConstructorsCheckBox.isSelected());
        data.setNocast(donTGenerateAuxiliaryCheckBox.isSelected());
        data.setNofd(donTDisambiguateFieldsCheckBox.isSelected());
        data.setUseTabs(useTabsInsteadOfCheckBox.isSelected());
        data.setSpaceAfterKeyword(spaceBetweenKeywordAndCheckBox.isSelected());
        data.setLineNumbersAsComments(originalLineNumbersAsCheckBox.isSelected());
        data.setFieldsFirst(outputFieldsBeforeMethodsCheckBox.isSelected());
        data.setNonlb(insertANewlineBeforeCheckBox.isSelected());
        data.setSplitStringsAtNewline(splitStringsOnNewlineCheckBox.isSelected());
        data.setPrefixNumericalClasses(classesWithNumericalNamesTextField.getText());
        data.setPrefixNumericalFields(fieldsWithNumericalNamesTextField.getText());
        data.setPrefixNumericalLocals(localsWithNumericalNamesTextField.getText());
        data.setPrefixNumericalMethods(methodsWithNumericalNamesTextField.getText());
        data.setPrefixNumericalParameters(parametersWithNumericalNamesTextField.getText());
        data.setPrefixPackages(allPackagesTextField.getText());
        data.setPrefixUnusedExceptions(unusedExceptionNamesTextField.getText());
        data.setAlwaysExcludeRecursively(alwaysExcludePackagesRecursivelyCheckBox.isSelected());
        data.setCleanupSourceRoots(cleanupSourceRootsCheckBox.isSelected());

        if (project != null)
        {
            setControlsEnabled(project,
                               data.isUseProjectSpecificSettings());
        }
    }

    /**
     * Restricted, non-contiguous spinner model.
     */
    private final class SpinnerRadixModel extends SpinnerNumberModel
    {
        /**
         * Initialises a new instance of this class with a default value of 10.
         */
        SpinnerRadixModel()
        {
            this(10);
        }

        /**
         * Initialises a new instance of this class with the given value.
         *
         * @param value the initial value
         */
        SpinnerRadixModel(int value)
        {
            super(value,
                  8,
                  16,
                  2);
            // ensure it's legal
            setValue(value);
        }

        /** {@inheritDoc} */
        public void setValue(Object object)
        {
            int value = ((Number) object).intValue();
            if (value != 8 && value != 10 && value != 16)
            {
                object = 10;
            }
            super.setValue(object);
        }

        /** {@inheritDoc} */
        public Object getNextValue()
        {
            Number number = getNumber();
            Integer next;
            switch (number.intValue())
            {
                case 8:
                    next = 10;
                    break;
                case 10:
                    next = 16;
                    break;
                case 16:
                    next = 8;
                    break;
                default:
                    next = 8;
            }
            return next;
        }

        /** {@inheritDoc} */
        public Object getPreviousValue()
        {
            Number number = getNumber();
            Integer previous;
            switch (number.intValue())
            {
                case 8:
                    previous = 16;
                    break;
                case 10:
                    previous = 8;
                    break;
                case 16:
                    previous = 10;
                    break;
                default:
                    previous = 8;
            }
            return previous;
        }
    }

    /**
     * Marker annotation to indicate a {@link JComponent} is considered
     * part of the general control set, as defined by whatever class is using this.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Control
    {
    }
}
