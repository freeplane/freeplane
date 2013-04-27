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
import java.awt.Container;
import java.awt.dnd.DropTarget;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.Vector;

import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import net.infonode.docking.DockingWindow;
import net.infonode.docking.DockingWindowAdapter;
import net.infonode.docking.OperationAbortedException;
import net.infonode.docking.RootWindow;
import net.infonode.docking.TabWindow;
import net.infonode.docking.View;
import net.infonode.docking.ViewSerializer;
import net.infonode.docking.util.DockingUtil;
import net.infonode.util.Direction;

import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.ui.IMapViewChangeListener;
import org.freeplane.features.url.mindmapmode.FileOpener;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.MapViewScrollPane;
import org.freeplane.view.swing.ui.DefaultMapMouseListener;

class MapViewDockingWindows implements IMapViewChangeListener {
// // 	final private Controller controller;
	private RootWindow rootWindow = null;
	final private Vector<Component> mPaneMapViews;
	private boolean mPaneSelectionUpdate = true;

	public MapViewDockingWindows() {
		rootWindow = new RootWindow(new ViewSerializer() {
			public void writeView(View view, ObjectOutputStream out) throws IOException {
				Component component = view.getComponent();
				if(component instanceof MapView){
					MapView mapView = (MapView) component;
					out.writeUTF(mapView.getModeController().getModeName());
					out.writeObject(mapView.getModel().getURL());
				}
			}
			
			public View readView(ObjectInputStream in) throws IOException {
				try {
	                String modeName = in.readUTF();
	                Controller controller = Controller.getCurrentController();
					controller.selectMode(modeName);
	                ModeController modeController = Controller.getCurrentModeController();
	                URL mapUrl = (URL) in.readObject();
	                MapController mapController = modeController.getMapController();
					if (!mapController.newMap(mapUrl)){
						MapModel map = controller.getMap();
						if(map.getURL().equals(mapUrl)){
							mapController.newMapView(map);
							Component mapViewComponent = controller.getMapViewManager().getMapViewComponent();
							return newDockedView(mapViewComponent);
						}
	                }
	                return null;
                }
                catch (Exception e) {
                	return null;
                }
			}
		});
		rootWindow.getWindowBar(Direction.DOWN).setEnabled(true);
		removeDesktopPaneAccelerators();
		mPaneMapViews = new Vector<Component>();
		final FileOpener fileOpener = new FileOpener();
		new DropTarget(rootWindow, fileOpener);
		rootWindow.addMouseListener(new DefaultMapMouseListener());

		final Controller controller = Controller.getCurrentController();
		controller.getMapViewManager().addMapViewChangeListener(this);
		rootWindow.addListener(new DockingWindowAdapter(){

			@Override
            public void viewFocusChanged(View previouslyFocusedView, View focusedView) {
	            if(focusedView != null){
	            	Component containedMapView = getContainedMapView(focusedView);
	            	viewSelectionChanged(containedMapView);
	            }
            }

			@Override
            public void windowClosing(DockingWindow window) throws OperationAbortedException {
				for(Component mapViewComponent : mPaneMapViews.toArray(new Component[]{}))
					if(SwingUtilities.isDescendingFrom(mapViewComponent, window))
					if (!Controller.getCurrentController().getMapViewManager().close(mapViewComponent, false))
						throw new OperationAbortedException("can not close view");
	            super.windowClosing(window);
            }
			
		});
		
		new InternalFrameAdapter() {
			@Override
            public void internalFrameClosing(InternalFrameEvent e) {
            }
		};
		

	}
	
	private void removeDesktopPaneAccelerators() {
		 final InputMap map = new InputMap();
		 rootWindow.setInputMap(JDesktopPane.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, map);
	}


	private DockingWindow getLastFocusedChildWindow(DockingWindow parentWindow) {
  	  DockingWindow lastFocusedChildWindow = parentWindow.getLastFocusedChildWindow();
  	  if(lastFocusedChildWindow == null)
  		  return parentWindow;
  	  else
  		  return getLastFocusedChildWindow(lastFocusedChildWindow);
    }
    
	public void afterViewChange(final Component pOldMap, final Component pNewMap) {
		if (pNewMap == null) {
			return;
		}
		for (int i = 0; i < mPaneMapViews.size(); ++i) {
			if (mPaneMapViews.get(i) == pNewMap) {
				View dockedView = getContainingDockedWindow(pNewMap);
				if (dockedView == getLastFocusedChildWindow(rootWindow)) {
	                    dockedView.restoreFocus();
				}
				return;
			}
		}
		mPaneMapViews.add(pNewMap);
		addDockedWindow(pNewMap);
	}

	private View getContainingDockedWindow(final Component pNewMap) {
	    return (View) SwingUtilities.getAncestorOfClass(View.class, pNewMap);
    }

	protected void addDockedView(View dynamicView) {
		DockingWindow lastFocusedChildWindow = getLastFocusedChildWindow(rootWindow);
	    if(lastFocusedChildWindow == null) {
	        DockingUtil.addWindow(dynamicView, rootWindow);
       }
       else{
			Container parent = SwingUtilities.getAncestorOfClass(DockingWindow.class, lastFocusedChildWindow);
			if(parent instanceof TabWindow)
				((TabWindow)parent).addTab(dynamicView);
			else
				DockingUtil.addWindow(dynamicView, lastFocusedChildWindow.getRootWindow());
		}
    }
	private Component getContainedMapView(View dockedWindow) {
        JScrollPane scrollPane = (JScrollPane) dockedWindow.getComponent();
		Component view = scrollPane.getViewport().getView();
        return view;
    }
	
	private void addDockedWindow(final Component pNewMap) {
	    final View viewFrame = newDockedView(pNewMap);

		addDockedView(viewFrame);
    }

	protected View newDockedView(final Component pNewMap) {
	    final String title = pNewMap.getName();
		MapViewScrollPane mapViewScrollPane = new MapViewScrollPane();
		mapViewScrollPane.getViewport().setView(pNewMap);
		@SuppressWarnings("serial")
        final View viewFrame = new ConnectedToMenuView(title, null, mapViewScrollPane);
		
		viewFrame.addHierarchyListener(new HierarchyListener() {
			public void hierarchyChanged(HierarchyEvent e) {
				if(viewFrame.isShowing()){
					viewFrame.removeHierarchyListener(this);
					Component selectedComponent = Controller.getCurrentController().getMapViewManager().getMapViewComponent();
					Component containedView = getContainedMapView(viewFrame);
					if(containedView == selectedComponent)
						viewFrame.restoreFocus();
				}
			}
		});
	    return viewFrame;
    }


	public void afterViewClose(final Component pOldMapView) {
		for (int i = 0; i < mPaneMapViews.size(); ++i) {
			if (mPaneMapViews.get(i) == pOldMapView) {
				mPaneSelectionUpdate = false;
				rootWindow.remove(getContainingDockedWindow(pOldMapView));
				mPaneMapViews.remove(i);
				mPaneSelectionUpdate = true;
				rootWindow.repaint();
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
						getContainingDockedWindow(mapView).getViewProperties().setTitle(pMapView.getName());
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
	    return rootWindow;
    }
}
