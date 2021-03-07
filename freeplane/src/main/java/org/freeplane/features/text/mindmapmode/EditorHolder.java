package org.freeplane.features.text.mindmapmode;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.map.NodeModel;

abstract public class EditorHolder implements IExtension, HierarchyListener{
    private final NodeModel node;
    private final Window window;
    public EditorHolder(NodeModel node, Window window) {
        super();
        this.node = node;
        this.window = window;
        window.addHierarchyListener(this);
    }
    
    @Override
    public void hierarchyChanged(HierarchyEvent e) {
        if(0 != (e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED)) {
            if(! window.isShowing()) {
                node.removeExtension(this);
                window.removeHierarchyListener(this);
            }
        }
    }
    
    public void activate() {
        window.toFront();
        Component mostRecentFocusOwner = window.getMostRecentFocusOwner();
        if(mostRecentFocusOwner != null)
            mostRecentFocusOwner.requestFocus();
        else
            window.requestFocus();
    }
};
