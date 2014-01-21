/* 
 * @(#) $Id:  $
 */
package net.stevechaloner.intellijad;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.LanguageLevelModuleExtension;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library.ModifiableModel;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import org.jetbrains.annotations.NotNull;

/**
 * <p></p>
 * <br/>
 * <p>Created on 21.01.14</p>
 *
 * @author Lukasz Zielinski
 */
public class DecompilationTest extends LightCodeInsightFixtureTestCase {

    @Override
    protected String getTestDataPath() {
        return IntelliJadTest.getTestDataDir().getAbsolutePath();
    }

    @NotNull
    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        return new DefaultLightProjectDescriptor() {
            @Override
            public void configureModule(Module module, ModifiableRootModel model, ContentEntry contentEntry) {
                model.getModuleExtension(LanguageLevelModuleExtension.class).setLanguageLevel(LanguageLevel.JDK_1_6);
                ModifiableModel libModel = model.getModuleLibraryTable().createLibrary("junit4test").getModifiableModel();
                String libPath = IntelliJadTest.getJarLibPath("junit.jar");
                final VirtualFile libJar = JarFileSystem.getInstance().refreshAndFindFileByPath(libPath);
                assertNotNull("VF not found for '"+libPath+"'", libJar);
                libModel.addRoot(libJar, OrderRootType.CLASSES);
                libModel.commit();
            }
        };
    }

    public void testDecompilation() throws Exception {
        
    }
}
