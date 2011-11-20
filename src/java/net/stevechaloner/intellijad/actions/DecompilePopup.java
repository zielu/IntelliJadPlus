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

package net.stevechaloner.intellijad.actions;

import com.intellij.openapi.project.Project;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.stevechaloner.intellijad.IntelliJadResourceBundle;
import net.stevechaloner.intellijad.config.Config;
import net.stevechaloner.intellijad.config.ExclusionTableModel;
import net.stevechaloner.intellijad.config.NavigationTriggeredDecompile;
import net.stevechaloner.intellijad.decompilers.DecompilationDescriptor;
import net.stevechaloner.intellijad.environment.EnvironmentContext;
import net.stevechaloner.intellijad.util.PluginUtil;

import org.jetbrains.annotations.NotNull;

public class DecompilePopup {
    private JPanel contentPane;
    private JComboBox comboBox1;
    private JTabbedPane tabbedPane1;
    private JLabel confirmDecompileLabel;
    private JCheckBox excludePackageCheckBox;
    private JCheckBox excludeRecursivelyCheckBox;

    /**
     * The descriptor giving details of the class to decompile.
     */
    @NotNull
    private final DecompilationDescriptor decompilationDescriptor;

    /**
     *
     */
    @NotNull
    private final EnvironmentContext environmentContext;

    public DecompilePopup(@NotNull DecompilationDescriptor decompilationDescriptor,
                          @NotNull Project project)
    {
        this.decompilationDescriptor = decompilationDescriptor;
        this.environmentContext = new EnvironmentContext(project);

        confirmDecompileLabel.setText(IntelliJadResourceBundle.message("message.confirm-decompile",
                                                                       decompilationDescriptor.getClassName()));
        excludePackageCheckBox.setText(IntelliJadResourceBundle.message("config.exclude-package",
                                                                        decompilationDescriptor.getPackageName()));
        excludeRecursivelyCheckBox.setText(IntelliJadResourceBundle.message("config.exclude-recursively"));
        excludePackageCheckBox.addChangeListener(new ChangeListener()
        {

            public void stateChanged(ChangeEvent e)
            {
                excludeRecursivelyCheckBox.setEnabled(excludePackageCheckBox.isSelected());
            }
        });
        excludeRecursivelyCheckBox.setEnabled(false);

        Config config = PluginUtil.getConfig(project);
        excludeRecursivelyCheckBox.setSelected(config.isAlwaysExcludeRecursively());

        comboBox1.addItem(NavigationTriggeredDecompile.ALWAYS);
        comboBox1.addItem(NavigationTriggeredDecompile.ASK);
        comboBox1.addItem(NavigationTriggeredDecompile.NEVER);
        comboBox1.setSelectedItem(NavigationTriggeredDecompile.getByName(config.getDecompileOnNavigation()));
    }

    void persistConfig()
    {
        Config config = PluginUtil.getConfig(environmentContext.getProject());
        if (config != null)
        {
            NavigationTriggeredDecompile option = (NavigationTriggeredDecompile) comboBox1.getSelectedItem();
            config.setDecompileOnNavigation(option.getName());

            String packageName = decompilationDescriptor.getPackageName();
            if (packageName != null && excludePackageCheckBox.isSelected())
            {
                ExclusionTableModel tableModel = config.getExclusionTableModel();
                tableModel.addExclusion(packageName,
                                        excludeRecursivelyCheckBox.isSelected(),
                                        true);
            }
        }
    }

    /**
     * Checks if any of the settings have changed.
     *
     * @param data the control data
     * @return true if the current data differs from the control
     */
    public boolean isModified(Config data)
    {
        return !((NavigationTriggeredDecompile) comboBox1.getSelectedItem()).getName().equals(data.getDecompileOnNavigation());
    }

    /**
     *
     * @return
     */
    JComponent getContentPane()
    {
        return contentPane;
    }
}
