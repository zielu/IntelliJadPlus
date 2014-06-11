package net.stevechaloner.intellijad;

import net.stevechaloner.intellijad.decompilers.JarExtractorTest;
import net.stevechaloner.intellijad.gui.IntelliJadIconTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Created by Lukasz on 2014-06-11.
 */
@Suite.SuiteClasses({
        IntelliJadIconTest.class
        //,JarExtractorTest.class
})
@RunWith(Suite.class)
public class UnitTests {
}
