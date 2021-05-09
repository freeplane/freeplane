package org.freeplane.core.ui.components;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class FocusRequestor implements HierarchyListener {
    public static void requestFocus(Component component) {
        if(component.isShowing())
            ComponentFocusRequestor.requestFocus(component);
        else
            component.addHierarchyListener(new FocusRequestor());
    }

    private static class ComponentFocusRequestor implements WindowFocusListener {
        static void requestFocus(Component component) {
            Window windowAncestor = SwingUtilities.getWindowAncestor(component);
            if(windowAncestor.isFocused()) {
                component.requestFocusInWindow();
            }
            else {
                windowAncestor.addWindowFocusListener(new ComponentFocusRequestor(component, windowAncestor));
            }
        }

        private final Component component;

        private final Window windowAncestor;

        private ComponentFocusRequestor(Component component, Window windowAncestor) {
            this.component = component;
            this.windowAncestor = windowAncestor;
        }

        @Override
        public void windowLostFocus(WindowEvent e) {/**/}

        @Override
        public void windowGainedFocus(WindowEvent e) {
            windowAncestor.removeWindowFocusListener(this);
            component.requestFocusInWindow();
        }
    }

    private FocusRequestor() {/**/}

    @Override
    public void hierarchyChanged(HierarchyEvent e) {
        Component component = e.getComponent();
        if(0 != (e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) && component.isShowing()) {
            component.removeHierarchyListener(this);
            requestFocus(component);
        }
    }


}