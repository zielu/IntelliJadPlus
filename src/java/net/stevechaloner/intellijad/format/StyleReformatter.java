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

package net.stevechaloner.intellijad.format;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.util.IncorrectOperationException;

import net.stevechaloner.intellijad.console.ConsoleEntryType;
import net.stevechaloner.intellijad.decompilers.DecompilationContext;

import org.jetbrains.annotations.NotNull;

/**
 * Reformats the source code of a file to match the preferred source formatting.
 *
 * @author Steve Chaloner
 */
public class StyleReformatter
{
    /**
     * Reformats the content of the given file to match the IDE settings.
     *
     * @param context the context the decompilation is occurring in
     * @param file the file representing the source code
     * @return true if reformatted
     */
    public static boolean reformat(@NotNull final DecompilationContext context,
                                   @NotNull final VirtualFile file)
    {
        final boolean[] result = { false };
        Reformatter reformatter = new Reformatter()
        {
            public void run()
            {
                try
                {
                    JavaCodeStyleManager.getInstance(context.getProject()).optimizeImports(psiFile);
                    CodeStyleManager.getInstance(context.getProject()).reformat(psiFile);
                    fileDocManager.saveDocument(document);
                    context.getConsoleContext().addSectionMessage(ConsoleEntryType.INFO,
                                                                  "message.reformatting",
                                                                  file.getName());
                    result[0] = true;
                }
                catch (IncorrectOperationException e)
                {
                    Logger.getInstance(StyleReformatter.class.getName()).error(e);
                }
            }
        };
        reformatter.execute(context,
                            file);
        return result[0];
    }

    /**
     * Reindents the contents of the file.
     *
     * @param context the decompilation context
     * @param file the file to reindent
     * @return true if reindented
     */
    public static boolean reindent(@NotNull final DecompilationContext context,
                                   @NotNull VirtualFile file)
    {
        final boolean[] result = { false };
        Reformatter reformatter = new Reformatter()
        {
            public void run()
            {
                CodeStyleManager styleManager = CodeStyleManager.getInstance(context.getProject());
                try
                {
                    styleManager.adjustLineIndent(psiFile,
                                                  new TextRange(0,
                                                                document.getTextLength()));
                    fileDocManager.saveDocument(document);
                    result[0] = true;
                }
                catch (IncorrectOperationException e)
                {
                    Logger.getInstance(getClass().getName()).error(e);
                }
            }
        };
        reformatter.execute(context,
                            file);
        return result[0];
    }

    /**
     * Contains common functionality for reformatting operations.
     */
    private abstract static class Reformatter implements Runnable
    {
        /**
         * The PSI file.
         */
        PsiFile psiFile;

        /**
         * The file document manager.
         */
        FileDocumentManager fileDocManager;

        /**
         * The document containing the source.
         */
        Document document;

        /**
         * Executes the reformatting operation.
         * 
         * @param context the decompilation context
         * @param file the file representing the source
         */
        void execute(@NotNull DecompilationContext context,
                     @NotNull VirtualFile file)
        {
            fileDocManager = FileDocumentManager.getInstance();
            document = fileDocManager.getDocument(file);
            if (document != null)
            {
                Project project = context.getProject();
                psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);
                if (psiFile != null)
                {
                    ApplicationManager.getApplication().runWriteAction(this);
                }
            }
        }
    }
}
