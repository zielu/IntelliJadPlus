/* 
 * $Id$
 */
package net.stevechaloner.intellijad.util;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;

/**
 * <p></p>
 * <br/>
 * <p>Created on 06.02.14</p>
 *
 * @author Lukasz Zielinski
 */
public class AppInvoker {
    
    private final Application application;
    
    public AppInvoker(Application application) {
        this.application = application;
    }
    
    public static AppInvoker get() {
        return new AppInvoker(ApplicationManager.getApplication());
    }
    
    public void runWriteActionAndWait(Runnable action) {
        runWriteActionAndWait(action, application.getCurrentModalityState());
    }
    
    public void runWriteActionAndWait(Runnable action, ModalityState modality) {
        application.invokeAndWait(new WriteAction(application, action), modality);        
    }
    
    public void saveSettings() {
        runWriteActionAndWait(new Runnable() {
            @Override
            public void run() {
                application.saveSettings();
            }
        });        
    }
    
    private class WriteAction implements Runnable {
        private final Application application;
        private final Runnable action;

        private WriteAction(Application application, Runnable action) {
            this.application = application;
            this.action = action;
        }

        @Override
        public void run() {
            application.runWriteAction(action);
        }
    }
}