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

package net.stevechaloner.intellijad.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderEnumerator;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Extension to the openapi LibraryUtil to allow easy searching in module libraries.
 * @author Steve Chaloner
 */
public class LibraryUtil
{
    /**
     * Static use only.
     */
    private LibraryUtil()
    {
    }

    /**
     * Searches the project and application scopes for libraries containing
     * the class.  If none are found, the module libraries are searched.
     *
     * @param fqn the fully-qualified name of the class.
     * @param project the current project
     * @return a list containing any matching libraries
     */
    @NotNull
    public static List<Library> findLibrariesByClass(@Nullable String fqn,
                                                     @NotNull Project project)
    {
        List<Library> libraries = new ArrayList<Library>();
        if (!StringUtil.isEmptyOrSpaces(fqn))
        {
            Library lib = com.intellij.openapi.roots.libraries.LibraryUtil.findLibraryByClass(fqn,
                                                                                              project);
            if (lib != null)
            {
                libraries.add(lib);
            }
            else
            {
                ModuleManager moduleManager = ModuleManager.getInstance(project);
                List<Module> modules = new ArrayList<Module>(Arrays.asList(moduleManager.getSortedModules()));
                for (Module module : modules)
                {
                    ModuleRootManager mrm = ModuleRootManager.getInstance(module);
                    Library library = findInLibraries(mrm.orderEntries(), fqn);
                    if (library != null) {
                        libraries.add(library);
                    }
                }
            }
        }
        return libraries;
    }
    
    @Nullable
    private static Library findInLibraries(OrderEnumerator enumerator, final String fqn) {
        final AtomicReference<Library> libraryRef = new AtomicReference<Library>();
        enumerator.forEachLibrary(new Processor<Library>() {
            @Override
            public boolean process(Library library) {
                if (com.intellij.openapi.roots.libraries.LibraryUtil.isClassAvailableInLibrary(library, fqn)) {
                    libraryRef.set(library);
                }
                return true;
            }
        });        
        return libraryRef.get();
    }
}
