/* 
 * @(#) $Id:  $
 */
package net.stevechaloner.intellijad.editor;

import com.intellij.codeInsight.AttachSourcesProvider;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.psi.PsiFile;
import net.stevechaloner.intellijad.IntelliJad;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
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
        actions.add(new JadAttachSourcesAction(psiFile, intelliJad));
        return actions;
    }
}
