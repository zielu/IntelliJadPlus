package net.stevechaloner.intellijad.gui;

import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupListener;
import com.intellij.util.Alarm;
import net.stevechaloner.intellijad.util.PluginUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.TimeUnit;

public class DelayedBalloonInfo implements Runnable {
    private final MessageType myMessageType;
    private final String myText;
    private final Alarm myAlarm;
    private final JComponent myComponent;
    private List<JBPopupListener> listeners = new LinkedList<JBPopupListener>();

    public DelayedBalloonInfo(@NotNull MessageType messageType, @NotNull JComponent component, @NotNull Alarm alarm, @NotNull String text) {
        myMessageType = messageType;
        myText = text;
        myAlarm = alarm;
        myComponent = component;
    }

    public void enqueueListener(JBPopupListener listener) {
        listeners.add(listener);
    }
    
    public void show() {
        run();    
    }
    
    @Override
    public void run() {
        if (myComponent == null || !myComponent.isShowing()) {
            myAlarm.cancelAllRequests();
            myAlarm.addRequest(this, (int) TimeUnit.MILLISECONDS.toMillis(200));
            return;
        }
        Balloon balloon = PluginUtil.showBalloon(myComponent, myMessageType, myText);
        ListIterator<JBPopupListener> iter = listeners.listIterator();
        while (iter.hasNext()) {
            balloon.addListener(iter.next());
            iter.remove();
        }
    }
    
    
}