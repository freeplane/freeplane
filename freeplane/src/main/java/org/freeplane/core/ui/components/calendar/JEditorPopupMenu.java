/*
 * Created on 4 Oct 2024
 *
 * author dimitry
 */
package org.freeplane.core.ui.components.calendar;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

public final class JEditorPopupMenu extends JPopupMenu {
    private static final long serialVersionUID = 1L;
    private AWTEventListener mouseListener;

    public JEditorPopupMenu() {
        // Initialize the mouse listener
        mouseListener = new AWTEventListener() {
            @Override
            public void eventDispatched(AWTEvent event) {
                if (event instanceof MouseEvent) {
                    MouseEvent mouseEvent = (MouseEvent) event;

                    if (isVisible()
                            && mouseEvent.getID() == MouseEvent.MOUSE_CLICKED
                            && getInvoker() != mouseEvent.getComponent()
                            && !SwingUtilities.isDescendingFrom(mouseEvent.getComponent(), JEditorPopupMenu.this)) {
                        setVisible(false);
                    }
                }
            }
        };
    }

    @Override
    public void setVisible(boolean b) {
        if (b) {
            Toolkit.getDefaultToolkit().addAWTEventListener(mouseListener, AWTEvent.MOUSE_EVENT_MASK);
        } else {
            Toolkit.getDefaultToolkit().removeAWTEventListener(mouseListener);
        }
        super.setVisible(b);
    }

    @Override
    public void menuSelectionChanged(boolean isIncluded) {
        if (!isIncluded) {
            AWTEvent currentEvent = EventQueue.getCurrentEvent();
            if(currentEvent != null) {
                final Object source = currentEvent.getSource();
                if (source instanceof Component) {
                    final Component c = (Component) source;
                    isIncluded = SwingUtilities.isDescendingFrom(c, this);
                }
            }
        }
        super.menuSelectionChanged(isIncluded);
    }
}