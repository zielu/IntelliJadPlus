/* 
 * @(#) $Id:  $
 */
package net.stevechaloner.intellijad.gui;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * <p></p>
 * <br/>
 * <p>Created on 06.09.12.</p>
 *
 * @author Lukasz Zielinski
 */
public class IntelliJadIconTest {


    @Test
    public void testIcons() {

        for (IntelliJadIcon icon : IntelliJadIcon.values()) {
            assertNotNull("Failed to load icon: "+icon.getPath(), icon.get());
        }
    }
}
