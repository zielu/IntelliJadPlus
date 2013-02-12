/* 
 * @(#) $Id:  $
 */
package net.stevechaloner.intellijad.config;

import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.JBPopupAdapter;
import com.intellij.openapi.ui.popup.LightweightWindowEvent;
import com.intellij.util.Alarm;
import net.stevechaloner.intellijad.IntelliJadResourceBundle;
import net.stevechaloner.intellijad.gui.DelayedBalloonInfo;

/**
 * <p></p>
 * <br/>
 * <p>Created on 20.01.12.</p>
 *
 * @author Lukasz Zielinski
 */
public abstract class UiConfigComponent extends ConfigComponent {
    private final Alarm myAlarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD);
    private volatile boolean closedAfterReset;

    protected Alarm getAlarm() {
        return myAlarm;
    }

    @Override
    protected void afterReset(Config config) {
        ConfigForm form = getForm();
        if (config.isReportOutputDirectoryNotSet()) {
            DelayedBalloonInfo delayedBalloonInfo = new DelayedBalloonInfo(MessageType.ERROR,
                    form.getOutputDirectoryInputComponent(),
                    getAlarm(), IntelliJadResourceBundle.message("error.output-directory-not-set"));
            delayedBalloonInfo.enqueueListener(new JBPopupAdapter() {
                @Override
                public void onClosed(LightweightWindowEvent event) {
                    closedAfterReset = true;
                }
            });
            delayedBalloonInfo.show();

        }
    }

    @Override
    protected void afterIsModified(boolean modified) {
        ConfigForm form = getForm();
        if (closedAfterReset && form.isReportOutputDirectoryNotSet()) {
            new DelayedBalloonInfo(MessageType.ERROR, form.getOutputDirectoryInputComponent(),
                getAlarm(), IntelliJadResourceBundle.message("error.output-directory-not-set")).run();
        }
    }
}
