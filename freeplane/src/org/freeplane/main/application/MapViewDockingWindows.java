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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Vector;

import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import net.infonode.docking.DockingWindow;
import net.infonode.docking.DockingWindowAdapter;
import net.infonode.docking.OperationAbortedException;
import net.infonode.docking.RootWindow;
import net.infonode.docking.TabWindow;
import net.infonode.docking.View;
import net.infonode.docking.properties.RootWindowProperties;
import net.infonode.docking.theme.BlueHighlightDockingTheme;
import net.infonode.docking.util.DockingUtil;
import net.infonode.util.Direction;

import org.apache.commons.codec.binary.Base64;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.ui.IMapViewChangeListener;
import org.freeplane.features.url.mindmapmode.FileOpener;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.ui.DefaultMapMouseListener;

class MapViewDockingWindows implements IMapViewChangeListener {

	// // 	final private Controller controller;
	private static final String OPENED_NOW = "openedNow_1.3.04";
	private RootWindow rootWindow = null;
	final private Vector<Component> mapViews;
	private boolean mPaneSelectionUpdate = true;
	private boolean loadingLayoutFromObjectInpusStream;
	private byte[] emptyConfigurations;
	private final MapViewSerializer viewSerializer;

	public MapViewDockingWindows() {
		viewSerializer = new MapViewSerializer();
		rootWindow = new RootWindow(viewSerializer);
		RootWindowProperties rootWindowProperties = rootWindow.getRootWindowProperties();
		rootWindowProperties.addSuperObject(new BlueHighlightDockingTheme().getRootWindowProperties());
		rootWindowProperties.getWindowAreaProperties().setBackgroundColor(UIManager.getColor("Panel.background"));
		rootWindow.getWindowBar(Direction.DOWN).setEnabled(true);
		try {
	        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			ObjectOutputStream wrapper = new ObjectOutputStream(byteStream);
			rootWindow.write(wrapper);
			wrapper.close();
			emptyConfigurations = byteStream.toByteArray();
        }
        catch (IOException e1) {
        }
		removeDesktopPaneAccelerators();
		mapViews = new Vector<Component>();
		final FileOpener fileOpener = new FileOpener();
		new DropTarget(rootWindow, fileOpener);
		rootWindow.addMouseListener(new DefaultMapMouseListener());

		final Controller controller = Controller.getCurrentController();
		controller.getMapViewManager().addMapViewChangeListener(this);
		rootWindow.addListener(new DockingWindowAdapter(){

			@Override
            public void viewFocusChanged(View previouslyFocusedView, View focusedView) {
	            if(previouslyFocusedView != null && focusedView != null){
	            	Component containedMapView = getContainedMapView(focusedView);
	            	viewSelectionChanged(containedMapView);
	            }
            }

			@Override
            public void windowClosing(DockingWindow window) throws OperationAbortedException {
				for(Component mapViewComponent : mapViews.toArray(new Component[]{}))
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
		if(! loadingLayoutFromObjectInpusStream) {
			for (int i = 0; i < mapViews.size(); ++i) {
				if (mapViews.get(i) == pNewMap) {
					View dockedView = getContainingDockedWindow(pNewMap);
					dockedView.restoreFocus();
					return;
				}
			}
	        addDockedWindow(pNewMap);
        }
		else if(mapViews.contains(pNewMap))
			return;
		mapViews.add(pNewMap);
	}

	static private View getContainingDockedWindow(final Component pNewMap) {
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

	static Component getContainedMapView(View dockedWindow) {
        JScrollPane scrollPane = (JScrollPane) dockedWindow.getComponent();
		Component view = scrollPane.getViewport().getView();
        return view;
    }

	private void addDockedWindow(final Component pNewMap) {
	    final View viewFrame = viewSerializer.newDockedView(pNewMap);
		addDockedView(viewFrame);
    }

	public void afterViewClose(final Component pOldMapView) {
		for (int i = 0; i < mapViews.size(); ++i) {
			if (mapViews.get(i) == pOldMapView) {
				mPaneSelectionUpdate = false;
				getContainingDockedWindow(pOldMapView).close();
				mapViews.remove(i);
				mPaneSelectionUpdate = true;
				rootWindow.repaint();
				return;
			}
		}
	}

	public void afterViewCreated(final Component mapView) {
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

	public void saveLayout(){
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		try {
	        ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
			rootWindow.write(objectStream);
			objectStream.close();
			String encodedBytes = Base64.encodeBase64String(byteStream.toByteArray());
			ResourceController.getResourceController().setProperty(OPENED_NOW, encodedBytes);
        }
        catch (IOException e) {
	        e.printStackTrace();
        }
	}

	public void loadLayout(){
		String encodedBytes = ResourceController.getResourceController().getProperty(OPENED_NOW, null);
		if(encodedBytes != null){
			byte[] bytes = Base64.decodeBase64(encodedBytes);
			ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
			try {
				loadingLayoutFromObjectInpusStream = true;
				rootWindow.read(new ObjectInputStream(byteStream));
				selectMapViewLater();
			}
			catch (Exception e) {
				LogUtils.severe(e);
				try {
	                rootWindow.read(new ObjectInputStream(new ByteArrayInputStream(emptyConfigurations)));
	                selectMapViewLater();
                }
                catch (IOException e1) {
                }
			}
			finally{
				viewSerializer.removeDummyViews();
				loadingLayoutFromObjectInpusStream = false;
			}
		}
	}

	private void selectMapViewLater() {
		Timer timer = new Timer(40, new ActionListener() {
			MapView mapView = null;
			int retryCount = 5;
		    public void actionPerformed(ActionEvent e) {
		    	if(mapView == null){
		    		for(Component mapView : mapViews){
		    			if(mapView.isShowing()){
		    				this.mapView = (MapView) mapView;
		    				break;
		    			}
		    		}
		    	}

		    	if(mapView != null){
		    		mapView.select();
		    		mapView.selectAsTheOnlyOneSelected(mapView.getRoot());
		    	}
				if(retryCount > 1){
					retryCount--;
					((Timer)e.getSource()).start();
				}
		    }
		  });
		timer.setRepeats(false);
		timer.start();
    }

	public void setTitle() {
		if(loadingLayoutFromObjectInpusStream)
			return;
		for (Component mapViewComponent: mapViews) {
			if (mapViewComponent instanceof MapView ) {
	            MapView mapView = (MapView)mapViewComponent;
	            String name = mapView.getName();
	            String title;
	            if(mapView.getModel().isSaved())
	            	title = name;
	            else
	            	title = name + " *";
	            View containingDockedWindow = getContainingDockedWindow(mapViewComponent);
				containingDockedWindow.getViewProperties().setTitle(title);
            }
		}
    }
}
