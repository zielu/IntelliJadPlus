/* 
 * @(#) $Id:  $
 */
package net.stevechaloner.intellijad.editor;

import com.intellij.codeInsight.AttachSourcesProvider.AttachSourcesAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.util.ActionCallback;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import net.stevechaloner.intellijad.IntelliJadResourceBundle;
import net.stevechaloner.intellijad.decompilers.DecompilationChoiceListener;
import net.stevechaloner.intellijad.decompilers.DecompilationDescriptor;
import net.stevechaloner.intellijad.decompilers.DecompilationDescriptorFactory;
import net.stevechaloner.intellijad.environment.EnvironmentContext;
import net.stevechaloner.intellijad.util.Exclusion;
import net.stevechaloner.intellijad.util.PluginUtil;

import java.util.List;

/**
 * <p></p>
 * <br/>
 * <p>Created on 15.05.12.</p>
 *
 * @author Lukasz Zielinski
 */
public class JadAttachSourcesAction implements AttachSourcesAction {
    private final PsiFile psiFile;
    private final DecompilationChoiceListener decompilationChoice;

    public JadAttachSourcesAction(PsiFile psiFile, DecompilationChoiceListener decompilationChoice) {
        this.psiFile = psiFile;
        this.decompilationChoice = decompilationChoice;
    }

    @Override
    public String getName() {
        return IntelliJadResourceBundle.message("attachSources.Decompile.text");
    }

    @Override
    public String getBusyText() {
        return IntelliJadResourceBundle.message("attachSources.Decompile.text.busyText");
    }

    @Override
    public ActionCallback perform(List<LibraryOrderEntry> orderEntriesContainingFile) {
        VirtualFile file = psiFile.getVirtualFile();
        DecompilationDescriptor descriptor = DecompilationDescriptorFactory.getFactoryForFile(file).create(file);
        boolean excluded = PluginUtil.getExclusion(psiFile.getProject()).isExcluded(descriptor);
        if (excluded) {
            return new ActionCallback.Rejected();
        } else {
            EnvironmentContext context = new EnvironmentContext(psiFile.getProject());
            decompilationChoice.decompile(context, descriptor);
            return new ActionCallback.Done();
        }
    }
}
