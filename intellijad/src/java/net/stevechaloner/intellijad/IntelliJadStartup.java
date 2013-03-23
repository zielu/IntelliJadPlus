/* 
 * @(#) $Id:  $
 */
package net.stevechaloner.intellijad;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;

/**
 * <p></p>
 * <br/>
 * <p>Created on 23.03.13</p>
 *
 * @author Lukasz Zielinski
 */
public class IntelliJadStartup implements StartupActivity {
    private final IntelliJad intelliJad;

    public IntelliJadStartup(IntelliJad intelliJad) {
        this.intelliJad = intelliJad;
    }

    @Override
    public void runActivity(Project project) {
        intelliJad.onStartup();
        intelliJad.onStartup(project);
    }
}
