/* 
 * @(#) $Id:  $
 */
package net.stevechaloner.intellijad.editor;

import com.intellij.codeInsight.AttachSourcesProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import net.stevechaloner.intellijad.IntelliJad;
import net.stevechaloner.intellijad.config.Config;
import net.stevechaloner.intellijad.config.NavigationTriggeredDecompile;
import net.stevechaloner.intellijad.decompilers.DecompilationDescriptor;
import net.stevechaloner.intellijad.decompilers.DecompilationDescriptorFactory;
import net.stevechaloner.intellijad.util.PluginUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * <p></p>
 * <br/>
 * <p>Created on 15.05.12.</p>
 *
 * @author Lukasz Zielinski
 */
public class JadAttachSourcesProvider implements AttachSourcesProvider {
    private final IntelliJad intelliJad;

    public JadAttachSourcesProvider(IntelliJad intelliJad) {
        this.intelliJad = intelliJad;
    }

    @NotNull
    @Override
    public Collection<AttachSourcesAction> getActions(List<LibraryOrderEntry> orderEntries, PsiFile psiFile) {
        List<AttachSourcesAction> actions = new ArrayList<AttachSourcesAction>();
        VirtualFile vFile = psiFile.getVirtualFile();
        if (vFile != null && "class".equals(vFile.getExtension())) {
            Config config = PluginUtil.getConfig(psiFile.getProject());
            NavigationTriggeredDecompile decompile = NavigationTriggeredDecompile.getByName(config.getDecompileOnNavigation());
            if (decompile == NavigationTriggeredDecompile.ON_DEMAND) {
                DecompilationDescriptor descriptor = DecompilationDescriptorFactory.getFactoryForFile(vFile).create(vFile);
                boolean excluded = PluginUtil.getExclusion(psiFile.getProject()).isExcluded(descriptor);
                if (!excluded) {
                    actions.add(new JadAttachSourcesAction(psiFile, intelliJad));
                }

            }
        }
        return actions;
    }
}
