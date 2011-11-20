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

import net.stevechaloner.intellijad.IntelliJadResourceBundle;

import org.jetbrains.annotations.NotNull;

import javax.swing.table.DefaultTableModel;

/**
 * Table model for managing automatic decompilation exclusions.
 *
 * @author Steve Chaloner
 */
public class ExclusionTableModel extends DefaultTableModel
{
    /**
     * The combinations possible for exclusion.
     */
    public enum ExclusionType { NOT_EXCLUDED, EXCLUDED, EXCLUSION_DISABLED }

    public ExclusionTableModel()
    {
        setColumnCount(3);
        setColumnIdentifiers(new String[]
                {
                        IntelliJadResourceBundle.message("config.path"),
                        IntelliJadResourceBundle.message("config.recursive"),
                        IntelliJadResourceBundle.message("config.enabled")
                });
    }


    public Class<?> getColumnClass(int i)
    {
        return i == 0 ? String.class : Boolean.class;
    }

    /**
     * Add an exclusion to the table model.
     *
     * @param packageName the name of the package to exclude
     * @param recursive   true iff all subpackages should also be excluded
     * @param enabled   true iff tthe exclusion is active
     */
    public void addExclusion(@NotNull String packageName,
                             boolean recursive,
                             boolean enabled)
    {

        if (getExclusionType(packageName) == ExclusionType.NOT_EXCLUDED)
        {
            this.addRow(new Object[]{packageName,
                                     recursive,
                                     enabled});
        }
    }

    /**
     * Checks if the package is already specified in the model.
     *
     * @param packageName the name of the package
     * @return the exclusion status of the package
     */
    public ExclusionType getExclusionType(@NotNull String packageName)
    {
        ExclusionType exclusionType = ExclusionType.NOT_EXCLUDED;
        int row = getPackageRow(packageName);
        if (row != -1)
        {
            exclusionType = (Boolean)getValueAt(row, 2) ? ExclusionType.EXCLUSION_DISABLED : ExclusionType.EXCLUDED;
        }
        return exclusionType;
    }

    /**
     * Checks if the package is already in the model.  This works
     * for exact package matches, and doesn't take recursive entries
     * into account.
     *
     * @param packageName the name of the package
     * @return true iff the package is in the model
     */
    boolean containsPackage(@NotNull String packageName)
    {
        return getPackageRow(packageName) != -1;
    }

    /**
     * Gets the row, if any, of the package in the model.
     *
     * @param packageName the name of the package
     * @return the row of the package, or -1 if it isn't in the table
     */
    int getPackageRow(@NotNull String packageName)
    {
        int row = -1;
        for (int i = 0; row == -1 && i < getRowCount(); i++)
        {
            if (packageName.equals(getValueAt(i, 0)))
            {
                row = i;
            }
        }
        return row;
    }
}
