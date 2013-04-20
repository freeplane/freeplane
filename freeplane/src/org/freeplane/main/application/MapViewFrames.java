/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2013 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2013.
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
package org.freeplane.main.application;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.dnd.DropTarget;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import org.freeplane.features.mode.Controller;
import org.freeplane.features.ui.IMapViewChangeListener;
import org.freeplane.features.url.mindmapmode.FileOpener;
import org.freeplane.view.swing.map.MapViewScrollPane;
import org.freeplane.view.swing.ui.DefaultMapMouseListener;

class MapViewFrames implements IMapViewChangeListener {
// // 	final private Controller controller;
	private JDesktopPane desktopPane = null;
	final private Vector<Component> mPaneMapViews;
	private boolean mPaneSelectionUpdate = true;

	public MapViewFrames() {
		desktopPane = new JDesktopPane();
		desktopPane.setFocusable(false);
		mPaneMapViews = new Vector<Component>();
		final FileOpener fileOpener = new FileOpener();
		new DropTarget(desktopPane, fileOpener);
		desktopPane.addMouseListener(new DefaultMapMouseListener());

		final Controller controller = Controller.getCurrentController();
		controller.getMapViewManager().addMapViewChangeListener(this);
	}

	public void afterViewChange(final Component pOldMap, final Component pNewMap) {
		if (pNewMap == null) {
			return;
		}
		for (int i = 0; i < mPaneMapViews.size(); ++i) {
			if (mPaneMapViews.get(i) == pNewMap) {
				JInternalFrame frame = getContainingFrame(pNewMap);
				if (! frame.isSelected()) {
					try {
	                    frame.setSelected(true);
                    }
                    catch (PropertyVetoException e) {
                    }
				}
				return;
			}
		}
		mPaneMapViews.add(pNewMap);
		addInternalFrame(pNewMap);
	}

	private JInternalFrame getContainingFrame(final Component pNewMap) {
	    return (JInternalFrame) SwingUtilities.getAncestorOfClass(JInternalFrame.class, pNewMap);
    }

	private Component getContainedView(JInternalFrame internalFrame) {
        JScrollPane scrollPane = (JScrollPane) internalFrame.getContentPane().getComponent(0);
		Component view = scrollPane.getViewport().getView();
        return view;
    }
	
	private void addInternalFrame(final Component pNewMap) {
	    final String title = pNewMap.getName();
		final JInternalFrame viewFrame = new JInternalFrame(title,
	          true, //resizable
	          true, //closable
	          true, //maximizable
	          true);
		viewFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		MapViewScrollPane mapViewScrollPane = new MapViewScrollPane();
		mapViewScrollPane.getViewport().setView(pNewMap);
		viewFrame.getContentPane().add(mapViewScrollPane);
		viewFrame.setSize(400, 400);
		viewFrame.setVisible(true); 
		desktopPane.add(viewFrame);
		try {
	        viewFrame.setMaximum(true);
	        viewFrame.setSelected(true);
        }
        catch (PropertyVetoException e) {
        }
		
		viewFrame.addInternalFrameListener(new InternalFrameAdapter() {
			@Override
            public void internalFrameActivated(InternalFrameEvent e) {
				JInternalFrame internalFrame = e.getInternalFrame();
				Component view = getContainedView(internalFrame);
				viewSelectionChanged(view);
            }

			@Override
            public void internalFrameClosing(InternalFrameEvent e) {
				JInternalFrame internalFrame = e.getInternalFrame();
				Component view = getContainedView(internalFrame);
				Controller.getCurrentController().getMapViewManager().close(view, false);
            }
		});
		
		viewFrame.addHierarchyListener(new HierarchyListener() {
			public void hierarchyChanged(HierarchyEvent e) {
				if(viewFrame.isShowing()){
					viewFrame.removeHierarchyListener(this);
					Component selectedComponent = Controller.getCurrentController().getMapViewManager().getMapViewComponent();
					Component containedView = getContainedView(viewFrame);
					if(containedView == selectedComponent)
	                    try {
	                        viewFrame.setSelected(true);
                        }
                        catch (PropertyVetoException e1) {
                        }
				}
			}
		});

    }


	public void afterViewClose(final Component pOldMapView) {
		for (int i = 0; i < mPaneMapViews.size(); ++i) {
			if (mPaneMapViews.get(i) == pOldMapView) {
				mPaneSelectionUpdate = false;
				desktopPane.remove(getContainingFrame(pOldMapView));
				mPaneMapViews.remove(i);
				mPaneSelectionUpdate = true;
				return;
			}
		}
	}

	public void afterViewCreated(final Component mapView) {
		mapView.addPropertyChangeListener("name", new PropertyChangeListener() {
			public void propertyChange(final PropertyChangeEvent evt) {
				final Component pMapView = (Component) evt.getSource();
				for (int i = 0; i < mPaneMapViews.size(); ++i) {
					if (mPaneMapViews.get(i) == pMapView) {
						getContainingFrame(mapView).setTitle(pMapView.getName());
					}
				}
			}
		});
	}

	public void beforeViewChange(final Component pOldMapView, final Component pNewMapView) {
	}

	private void viewSelectionChanged(final Component mapView) {
		if (!mPaneSelectionUpdate) {
			return;
		}
		Controller controller = Controller.getCurrentController();
		if (mapView != controller.getMapViewManager().getMapViewComponent()) {
			controller.getMapViewManager().changeToMapView(mapView.getName());
		}
	}

	public JComponent getMapPane() {
	    return desktopPane;
    }
}
