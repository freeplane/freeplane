/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2010 dimitry
 *
 *  This file author is dimitry
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.plugin.formula;

import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.MenuBar;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.JScrollBar;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.text.Document;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.ui.DelayedMouseListener;
import org.freeplane.core.ui.IMouseListener;
import org.freeplane.features.common.map.MapController;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.mindmapmode.text.EditNodeDialog;
import org.freeplane.features.mindmapmode.text.EditNodeBase.IEditControl;
import org.freeplane.view.swing.map.MainView;
import org.freeplane.view.swing.map.MapView;

/**
 * @author Dimitry Polivaev
 * Nov 20, 2010
 */
class FormulaEditor extends EditNodeDialog {
	
	class MouseListenerImpl extends MouseAdapter implements IMouseListener{

	    public void mouseMoved(MouseEvent e) {
	    }

	    public void mouseDragged(MouseEvent e) {
	        final Component component = findMapComponent(e);
	        if(canRedispatchEventFor(component)){
	        	redispatchMouseEvent(e, component);
	        }
	    }

	    public void mouseClicked(MouseEvent e) {
	    	if(e.getButton() != 1){
	    		return;
	    	}
	        final Component component = findMapComponent(e);
	        if(! (component instanceof MainView)){
	        	return;
	        }
	        MainView mainView = (MainView) component;
	        final NodeModel model = mainView.getNodeView().getModel();
	        switch(e.getClickCount()){
	        	case 1:
	        		final MapController mapController = Controller.getCurrentModeController().getMapController();
					mapController.setFolded(model, ! model.isFolded());
	        		break;
	        	case 2:
					final String id = model.getID();
	    	        textEditor.replaceSelection(id);
	        		break;
	        }
	    }

		public boolean canRedispatchEventFor(final Component component) {
	        if (component instanceof MapView)
	        	return true;
	        if (component instanceof JScrollBar || SwingUtilities.getAncestorOfClass(JScrollBar.class, component) != null)
	        	return true;
	        return false;
        }

	    public void mouseEntered(MouseEvent e) {
	    }

	    public void mouseExited(MouseEvent e) {
	    }

	    public void mousePressed(MouseEvent e) {
	    	if(e.getButton() != 1){
	    		return;
	    	}
	        final Component component = findMapComponent(e);
	        if(canRedispatchEventFor(component)){
	        	redispatchMouseEvent(e, component);
	        }
	    }

	    public void mouseReleased(MouseEvent e) {
	    	if(e.getButton() != 1){
	    		return;
	    	}
	        final Component component = findMapComponent(e);
	        if(canRedispatchEventFor(component)){
	        	redispatchMouseEvent(e, component);
	        }
	    }

	    //A basic implementation of redispatching events.
	    private Component findMapComponent(MouseEvent e) {
	    	final Component glassPane = e.getComponent();
	    	final Point glassPanePoint = e.getPoint();
	    	final Container container = jframe.getRootPane().getContentPane();
	    	Point containerPoint = SwingUtilities.convertPoint(
	    		glassPane,
	    		glassPanePoint,
	    		container);
	    	Component component = 
	    		SwingUtilities.getDeepestComponentAt(
	    			container,
	    			containerPoint.x,
	    			containerPoint.y);
	    	return component;
	    }
		    public void redispatchMouseEvent(MouseEvent e, final Component component) {
	        final Component glassPane = e.getComponent();
	        final Point glassPanePoint = e.getPoint();
	        Point componentPoint = SwingUtilities.convertPoint(
	        	glassPane,
	        	glassPanePoint,
	        	component);
	        component.dispatchEvent(new MouseEvent(component,
	        	e.getID(),
	        	e.getWhen(),
	        	e.getModifiers(),
	        	componentPoint.x,
	        	componentPoint.y,
	        	e.getClickCount(),
	        	e.isPopupTrigger()));
        }

	}

    private JFrame jframe;
	private JEditorPane textEditor;

	FormulaEditor(NodeModel nodeModel, String text, KeyEvent firstEvent, IEditControl editControl,
                          boolean enableSplit, JEditorPane textEditor) {
	    super(nodeModel, text, firstEvent, editControl, enableSplit, textEditor);
	    super.setModal(false);
	    this.textEditor = textEditor;
	    final IMouseListener mouseListener = new DelayedMouseListener(new MouseListenerImpl(), 2, 1);
	    textEditor.addAncestorListener(new AncestorListener() {

			public void ancestorRemoved(AncestorEvent event) {
				jframe.getJMenuBar().setEnabled(true);
				final Component glassPane = jframe.getRootPane().getGlassPane();
				glassPane.removeMouseListener(mouseListener);
				glassPane.removeMouseMotionListener(mouseListener);
				glassPane.setVisible(false);
			}
			
			public void ancestorMoved(AncestorEvent event) {
			}
			
			public void ancestorAdded(AncestorEvent event) {
				jframe.getJMenuBar().setEnabled(false);
				final Component glassPane = jframe.getRootPane().getGlassPane();
				glassPane.addMouseListener(mouseListener);
				glassPane.addMouseMotionListener(mouseListener);
				glassPane.setVisible(true);
			}
		});
	    
    }

	@Override
    public void show(Frame frame) {
		jframe = (JFrame) frame;
	    super.show(frame);
    }
}