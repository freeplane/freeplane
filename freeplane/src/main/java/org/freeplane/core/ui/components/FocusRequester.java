package org.freeplane.core.ui.components;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import javax.swing.SwingUtilities;

public class FocusRequester implements HierarchyListener {
    @Override
    public void hierarchyChanged(HierarchyEvent e) {
    	Component component = e.getComponent();
    	if(0 != (e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) && component.isShowing()) {
    		component.removeHierarchyListener(this);
    		Window windowAncestor = SwingUtilities.getWindowAncestor(component);
    		if(windowAncestor.isFocused()) {
    		    component.requestFocusInWindow();
    		}
    		else {
    		    windowAncestor.addWindowFocusListener(new WindowFocusListener() {
                    
                    @Override
                    public void windowLostFocus(WindowEvent e) {/**/}
                    
                    @Override
                    public void windowGainedFocus(WindowEvent e) {
                        windowAncestor.removeWindowFocusListener(this);
                        component.requestFocusInWindow();
                    }
                });
    		}
    	}
    }
}