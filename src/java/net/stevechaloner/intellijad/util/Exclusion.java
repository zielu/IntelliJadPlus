/* 
 * @(#) $Id:  $
 */
package net.stevechaloner.intellijad.util;

import net.stevechaloner.intellijad.config.Config;
import net.stevechaloner.intellijad.config.ExclusionTableModel;
import net.stevechaloner.intellijad.decompilers.DecompilationDescriptor;
import org.jetbrains.annotations.NotNull;

/**
 * <p></p>
 * <br/>
 * <p>Created on 16.05.12.</p>
 *
 * @author Lukasz Zielinski
 */
public class Exclusion {
    private final Config config;

    public Exclusion(@NotNull Config config) {
        this.config = config;
    }

    /**
     * Checks the exclusion settings to see if the class is eligible for decompilation.
     *
     * @param decompilationDescriptor the descriptor of the target class
     * @return true if the class should not be decompiled
     */
    public boolean isExcluded(@NotNull DecompilationDescriptor decompilationDescriptor) {
        ExclusionTableModel exclusionModel = config.getExclusionTableModel();
        String packageName = decompilationDescriptor.getPackageName();
        boolean exclude = false;
        if (packageName != null) {
            if (ExclusionTableModel.ExclusionType.NOT_EXCLUDED == exclusionModel.getExclusionType(packageName)) {
                for (int i = 0; !exclude && i < exclusionModel.getRowCount(); i++) {
                    String pn = (String) exclusionModel.getValueAt(i, 0);
                    if (pn != null) {
                        exclude = packageName.startsWith(pn) &&
                                (Boolean) exclusionModel.getValueAt(i, 1) &&
                                (Boolean) exclusionModel.getValueAt(i, 2);
                    }
                }
            }
        }
        return exclude;
    }
}
