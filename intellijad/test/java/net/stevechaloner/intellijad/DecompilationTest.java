/* 
 * @(#) $Id:  $
 */
package net.stevechaloner.intellijad;

import java.io.File;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.LanguageLevelModuleExtension;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library.ModifiableModel;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import net.stevechaloner.intellijad.config.Config;
import net.stevechaloner.intellijad.decompilers.DecompilationChoiceListener;
import net.stevechaloner.intellijad.decompilers.DecompilationDescriptor;
import net.stevechaloner.intellijad.decompilers.DecompilationDescriptorFactory;
import net.stevechaloner.intellijad.decompilers.DecompilationResult;
import net.stevechaloner.intellijad.environment.EnvironmentContext;
import net.stevechaloner.intellijad.util.PluginUtil;
import org.jetbrains.annotations.NotNull;

/**
 * <p></p>
 * <br/>
 * <p>Created on 21.01.14</p>
 *
 * @author Lukasz Zielinski
 */
public class DecompilationTest extends LightCodeInsightFixtureTestCase {
    private final Logger LOG = Logger.getInstance(getClass());
    
    private String jarLibPath;
    private VirtualFile libJar;
    
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
                jarLibPath = IntelliJadTest.getJarLibPath("junit.jar");
                libJar = JarFileSystem.getInstance().refreshAndFindFileByPath(jarLibPath);
                assertNotNull("VF not found for '"+jarLibPath+"'", libJar);
                libModel.addRoot(libJar, OrderRootType.CLASSES);
                libModel.commit();
            }
        };
    }

    public void testDecompilation() throws Exception {
        VirtualFileManager virtualFileManager = VirtualFileManager.getInstance();
        assertNotNull(virtualFileManager);
        String fileUrl = libJar.getUrl() + "org/junit/Assert.class";

        VirtualFile testFile = virtualFileManager.findFileByUrl(fileUrl);

        Application application = ApplicationManager.getApplication();
        assertTrue(application.isUnitTestMode());
        IntelliJad intelliJad = application.getComponent(IntelliJad.class);
        DecompilationChoiceListener listener = intelliJad;
        assertNotNull(listener);
        Config config = PluginUtil.getConfig(getProject());
        assertNotNull(config);
        intelliJad.forceDecompilationToDirectory(config);
        File jad = new File("c:\\Programowanie\\jad158g.win\\jad.exe");
        assertTrue(jad.exists());
        assertTrue(jad.isFile());
        config.setJadPath(jad.getAbsolutePath());
        intelliJad.primeProject(getProject());       
        
        DecompilationDescriptor dd = DecompilationDescriptorFactory.getFactoryForFile(testFile).create(testFile);
        assertNotNull(dd);
        DecompilationResult result = listener.decompile(new EnvironmentContext(getProject()), dd);
        LOG.info("Decompilation finished");
        assertTrue(result.isSuccessful());
        assertTrue(result.getResultFile().exists());
    }
}
