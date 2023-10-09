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
import java.awt.Font;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Window;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import net.infonode.docking.SplitWindow;
import org.apache.commons.codec.binary.Base64;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.FileOpener;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.ui.IMapViewChangeListener;
import org.freeplane.features.url.mindmapmode.DroppedMindMapOpener;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.NodeView;
import org.freeplane.view.swing.ui.DefaultMapMouseListener;

import net.infonode.docking.AbstractTabWindow;
import net.infonode.docking.DockingWindow;
import net.infonode.docking.DockingWindowAdapter;
import net.infonode.docking.FloatingWindow;
import net.infonode.docking.OperationAbortedException;
import net.infonode.docking.RootWindow;
import net.infonode.docking.TabWindow;
import net.infonode.docking.View;
import net.infonode.docking.WindowPopupMenuFactory;
import net.infonode.docking.properties.DockingWindowProperties;
import net.infonode.docking.properties.RootWindowProperties;
import net.infonode.docking.theme.ClassicDockingTheme;
import net.infonode.docking.theme.DockingWindowsTheme;
import net.infonode.docking.theme.LookAndFeelDockingTheme;
import net.infonode.docking.util.DockingUtil;
import net.infonode.gui.DynamicUIManager;
import net.infonode.gui.DynamicUIManagerListener;
import net.infonode.gui.icon.button.DropDownIcon;
import net.infonode.properties.gui.util.ComponentProperties;
import net.infonode.tabbedpanel.TabAreaComponentsProperties;
import net.infonode.tabbedpanel.TabAreaProperties;
import net.infonode.tabbedpanel.TabAreaVisiblePolicy;
import net.infonode.tabbedpanel.TabDropDownListVisiblePolicy;
import net.infonode.tabbedpanel.TabLayoutPolicy;
import net.infonode.tabbedpanel.TabbedPanelProperties;
import net.infonode.tabbedpanel.TabbedUIDefaults;
import net.infonode.tabbedpanel.titledtab.TitledTabProperties;
import net.infonode.tabbedpanel.titledtab.TitledTabSizePolicy;
import net.infonode.util.Direction;

class MapViewDockingWindows implements IMapViewChangeListener {

	protected static final String CUSTOMIZED_TAB_NAME_PROPERTY = "customizedTabName";
    // // 	final private Controller controller;
	private static final String OPENED_NOW = "openedNow_1.3.04";
	private RootWindow rootWindow = null;
	final private Vector<Component> mapViews;
	private boolean mPaneSelectionUpdate = true;
	private boolean loadingLayoutFromObjectInputStream;
	private boolean initialTabNameLoadingWasDone;
	private byte[] emptyConfigurations;
	private final MapViewSerializer viewSerializer;
	private DockingWindowsTheme theme;

	public MapViewDockingWindows() {
		viewSerializer = new MapViewSerializer();
		rootWindow = new RootWindow(viewSerializer);
		configureDefaultDockingWindowProperties();

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
		final FileOpener fileOpener = new FileOpener("mm", new DroppedMindMapOpener());
		new DropTarget(rootWindow, fileOpener);
		rootWindow.addMouseListener(new DefaultMapMouseListener());

		final Controller controller = Controller.getCurrentController();
		controller.getMapViewManager().addMapViewChangeListener(this);
		rootWindow.addListener(new DockingWindowAdapter(){

			private IconColorReplacer iconColorReplacer;

			@Override
            public void viewFocusChanged(View previouslyFocusedView, View focusedView) {
				if (focusedView != null) {
	            	Component containedMapView = getContainedMapView(focusedView);
					final Component mapViewComponent = Controller.getCurrentController().getMapViewManager()
					    .getMapViewComponent();
					if (containedMapView != mapViewComponent)
						viewSelectionChanged(containedMapView);
	            }
            }

			@Override
            public void windowClosing(DockingWindow window) throws OperationAbortedException {
				for(Component mapViewComponent : mapViews.toArray(new Component[]{}))
					if(SwingUtilities.isDescendingFrom(mapViewComponent, window))
					if (!Controller.getCurrentController().getMapViewManager().close(mapViewComponent))
						throw new OperationAbortedException("can not close view");
            }



			@Override
            public void windowAdded(final DockingWindow addedToWindow, final DockingWindow addedWindow) {
				if(addedWindow instanceof TabWindow) {
					final DockingWindowProperties windowProperties = addedWindow.getWindowProperties();
					windowProperties.setDockEnabled(false);
					windowProperties.setUndockEnabled(false);
					if(UITools.getCurrentFrame().isResizable())
						setTabAreaVisiblePolicy((TabWindow) addedWindow);
					else
						setTabAreaPolicy((TabWindow) addedWindow, TabAreaVisiblePolicy.NEVER);
                }
				else if(addedWindow instanceof FloatingWindow) {
					final Container topLevelAncestor = addedWindow.getTopLevelAncestor();
					if(topLevelAncestor instanceof Window){
						if(iconColorReplacer == null)
							iconColorReplacer = new IconColorReplacer(((Window) UITools.getMenuComponent()).getIconImages());
						final List<Image> iconImages = iconColorReplacer.getNextIconImages();
						((Window)topLevelAncestor).setIconImages(iconImages);
					}
				}
				setTabPolicies(addedWindow);
            }

			private void setTabPolicies(final DockingWindow window) {
				if(window instanceof TabWindow){
					TabbedPanelProperties tabbedPanelProperties = ((TabWindow)window).getTabWindowProperties().getTabbedPanelProperties();
					if(! tabbedPanelProperties.getTabLayoutPolicy().equals(TabLayoutPolicy.COMPRESSION))
						tabbedPanelProperties.setTabLayoutPolicy(TabLayoutPolicy.COMPRESSION);
					if(! tabbedPanelProperties.getTabDropDownListVisiblePolicy().equals(TabDropDownListVisiblePolicy.MORE_THAN_ONE_TAB))
						tabbedPanelProperties.setTabDropDownListVisiblePolicy(TabDropDownListVisiblePolicy.MORE_THAN_ONE_TAB);
				}
				for(int i = 0; i < window.getChildWindowCount(); i++){
					setTabPolicies(window.getChildWindow(i));
				}
			}



			@Override
            public void windowRemoved(DockingWindow removedFromWindow, DockingWindow removedWindow) {
				if(removedWindow instanceof TabWindow) {
	                if (removedFromWindow == rootWindow) {
	                	final TabAreaProperties tabAreaProperties = ((TabWindow)removedWindow).getTabWindowProperties().getTabbedPanelProperties().getTabAreaProperties();
	                    tabAreaProperties.setTabAreaVisiblePolicy(TabAreaVisiblePolicy.ALWAYS);
                    }
                }
            }
		});

		new InternalFrameAdapter() {
			@Override
            public void internalFrameClosing(InternalFrameEvent e) {
            }
		};

		addTabsPopupMenu(rootWindow);

	}

	private void addTabsPopupMenu(DockingWindow dockingWindow){
		dockingWindow.setPopupMenuFactory(new WindowPopupMenuFactory() {
			public JPopupMenu createPopupMenu(DockingWindow window) {
				if( window.getWindowParent() instanceof RootWindow || window.getWindowParent() instanceof SplitWindow) {
				   return null;
				}
				JPopupMenu menu = new JPopupMenu(window.getTitle());
				JMenuItem menuItem = new JMenuItem(TextUtils.getText("TabPopUpMenu.rename.text","Rename"));
				menuItem.setToolTipText(TextUtils.getText("TabPopUpMenu.rename.tooltip","Windows layout changes may reset the tab title."));
				menuItem.addActionListener(new ActionListener() {
				    public void actionPerformed(ActionEvent e) {
						JComponent mapView = (JComponent) getContainedMapView(window);
						String customizedTabName = (String) mapView.getClientProperty(CUSTOMIZED_TAB_NAME_PROPERTY);
						customizedTabName = customizedTabName!=null ? customizedTabName : mapView.getName();
						String newName = JOptionPane.showInputDialog(TextUtils.getText("TabPopUpMenu.rename.inputDialog","Input new temporary name: "), customizedTabName);
				        if(Objects.equals(newName, "") || newName==null ){
				            mapView.putClientProperty(CUSTOMIZED_TAB_NAME_PROPERTY, null);
				        } else {
				            mapView.putClientProperty(CUSTOMIZED_TAB_NAME_PROPERTY, newName);
				        }
						addTitleProvider(window); //TODO: revisar
				        setTitle();
				    }
 				});
				menu.add(menuItem);
				return menu;
			}
		});
	}

	private void configureDefaultDockingWindowProperties() {

		RootWindowProperties rootWindowProperties = rootWindow.getRootWindowProperties();
		String lf = UIManager.getLookAndFeel().getID();
		if(lf.endsWith("Aqua")) {
			theme = createClassicTheme();
		} else {
			theme = new LookAndFeelDockingTheme();
		}
		DynamicUIManager.getInstance().addListener(new DynamicUIManagerListener() {

			@Override
			public void propertiesChanging() {
			}

			@Override
			public void propertiesChanged() {
			}

			@Override
			public void lookAndFeelChanging() {
				String lf = UIManager.getLookAndFeel().getID();
				DockingWindowsTheme newTheme;
				boolean existingDockingThemeMatchesLookAndFeel = lf.endsWith("Aqua") != (theme instanceof LookAndFeelDockingTheme);
				if(existingDockingThemeMatchesLookAndFeel)
					return;
				if(theme instanceof LookAndFeelDockingTheme) {
					((LookAndFeelDockingTheme)theme).dispose();
					newTheme = createClassicTheme();
				} else {
					newTheme = new LookAndFeelDockingTheme();
				}
				rootWindowProperties.replaceSuperObject(theme.getRootWindowProperties(), newTheme.getRootWindowProperties());
				theme = newTheme;
			}

			@Override
			public void lookAndFeelChanged() {
			}
		});
		rootWindowProperties.addSuperObject(theme.getRootWindowProperties());

		RootWindowProperties overwrittenProperties = new RootWindowProperties();

		overwrittenProperties.getWindowAreaProperties().setInsets(new Insets(0, 0, 0, 0)).setBorder(BorderFactory.createEmptyBorder());

		overwrittenProperties.getFloatingWindowProperties().setUseFrame(true);

		overwrittenProperties.getTabWindowProperties().getTabbedPanelProperties().getContentPanelProperties()
		    .getComponentProperties().setInsets(new Insets(0, 0, 0, 0)).setBorder(BorderFactory.createEmptyBorder());

		TabbedPanelProperties tabbedPanelProperties = overwrittenProperties.getTabWindowProperties().getTabbedPanelProperties();
		tabbedPanelProperties.setTabLayoutPolicy(TabLayoutPolicy.COMPRESSION);
		tabbedPanelProperties.setTabDropDownListVisiblePolicy(TabDropDownListVisiblePolicy.MORE_THAN_ONE_TAB);
		tabbedPanelProperties.setShadowEnabled(false);
		tabbedPanelProperties.getButtonProperties().getTabDropDownListButtonProperties()
			.setIcon(new DropDownIcon(TabbedUIDefaults.getButtonIconSize(), Direction.DOWN));

		Font tabFont = UITools.getUIFont();
		TitledTabProperties titledTabProperties = overwrittenProperties.getTabWindowProperties().getTabProperties().getTitledTabProperties();
		titledTabProperties.setSizePolicy(TitledTabSizePolicy.INDIVIDUAL_SIZE);
		ComponentProperties highlightedProperties = titledTabProperties.getHighlightedProperties().getComponentProperties();
		highlightedProperties.setFont(tabFont);
		ComponentProperties normalProperties = titledTabProperties.getNormalProperties().getComponentProperties();
		normalProperties.setFont(tabFont);
		rootWindowProperties.addSuperObject(overwrittenProperties);
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

	@Override
	public void afterViewChange(final Component pOldMap, final Component pNewMap) {
		if (pNewMap == null) {
			return;
		}
		if(loadingLayoutFromObjectInputStream) {
            if(mapViews.contains(pNewMap))
                return;
            else
                updateTitle(pNewMap);
        } else {
			for (int i = 0; i < mapViews.size(); ++i) {
				if (mapViews.get(i) == pNewMap) {
					View dockedView = getContainingDockedWindow(pNewMap);
					Frame window = JOptionPane.getFrameForComponent(dockedView);
					int frameState = window.getExtendedState();
					if((frameState & Frame.ICONIFIED) != 0)
						window.setExtendedState(frameState & ~Frame.ICONIFIED);
					if(dockedView.isMinimized())
						dockedView.restore();
					else
						dockedView.restoreFocus();
					focusMapViewLater((MapView) pNewMap);
					return;
				}
			}
	        addDockedWindow(pOldMap, pNewMap);
		}
		mapViews.add(pNewMap);
	}

	private void addTitleProvider(DockingWindow window){
		window.getWindowProperties().setTitleProvider(new CustomWindowTitleProvider());
	}

	static private View getContainingDockedWindow(final Component pNewMap) {
	    return (View) SwingUtilities.getAncestorOfClass(View.class, pNewMap);
    }

	private void addDockedView(View oldSelected, View newView) {
		DockingWindow lastFocusedChildWindow = oldSelected != null ? oldSelected : getLastFocusedChildWindow(rootWindow);
	    if(lastFocusedChildWindow == null) {
	        DockingUtil.addWindow(newView, rootWindow);
       }
       else{
			Container parent = SwingUtilities.getAncestorOfClass(DockingWindow.class, lastFocusedChildWindow);
			if(parent instanceof TabWindow) {
				final TabWindow tabWindow = (TabWindow)parent;
				tabWindow.addTab(newView, tabWindow.getChildWindowIndex(lastFocusedChildWindow) + 1);
			} else
			    DockingUtil.addWindow(newView, lastFocusedChildWindow.getRootWindow());
       }
	}

	static private Component getContainedMapView(DockingWindow window) {
	    if (window == null)
	        return null;
	    else if (window instanceof View) {
	        return getContainedMapView((View) window);
	    }
	    else {
			return window;
		}
	}

	static Component getContainedMapView(View dockedWindow) {
	    JScrollPane scrollPane = (JScrollPane) ((Container) dockedWindow.getComponent()).getComponent(1);
	    Component view = scrollPane.getViewport().getView();
        return view;
    }

	private void addDockedWindow(final Component pOldMap, final Component pNewMap) {
	    final View viewFrame = viewSerializer.newDockedView(pNewMap, createTitle(pNewMap));
		addDockedView(pOldMap != null ? getContainingDockedWindow(pOldMap) : null, viewFrame);
    }

	@Override
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
				loadingLayoutFromObjectInputStream = true;
				rootWindow.read(new ObjectInputStream(byteStream));
			}
			catch (Exception e) {
				LogUtils.severe(e);
				try {
	                rootWindow.read(new ObjectInputStream(new ByteArrayInputStream(emptyConfigurations)));
                }
                catch (IOException e1) {
                }
			}
			finally{
				viewSerializer.removeDummyViews();
				loadingLayoutFromObjectInputStream = false;
			}
			rootWindow.getWindowBar(Direction.DOWN).setEnabled(false);
			initialTabNameLoadingWasDone = false;
			setTitle();
		}
	}

	private void loadInitialCustomTabNames(){
		for (Component mapViewComponent: mapViews) {
			if (mapViewComponent instanceof MapView ) {
				View containingDockedWindow = getContainingDockedWindow(mapViewComponent);
				if(containingDockedWindow != null) {
					String oldTitle = containingDockedWindow.getViewProperties().getTitle();
					MapView mapView = (MapView)mapViewComponent;
					if(oldTitle != null && !oldTitle.equals("") && !oldTitle.equals(mapView.getName())){
						if(oldTitle.endsWith(" *")) {
							oldTitle = oldTitle.substring(0,oldTitle.length()-2);
						}
						mapView.putClientProperty(CUSTOMIZED_TAB_NAME_PROPERTY, oldTitle);
					}
				}
			}
		}
		initialTabNameLoadingWasDone = true;
	}

	public void focusMapViewLater(final MapView mapView) {
		Timer timer = new Timer(40, new ActionListener() {
			int retryCount = 5;
		    @Override
			public void actionPerformed(final ActionEvent event) {
		    	final Timer eventTimer = (Timer)event.getSource();
		    	focusMapLater(mapView, eventTimer);
		    }
			private void focusMapLater(final MapView mapView, final Timer eventTimer) {
	            if(mapView.isShowing() && Controller.getCurrentController().getMapViewManager().getMapViewComponent() == mapView){
		    		final NodeView selected = mapView.getSelected();
		    		if(selected != null){
		    			final Frame frame = JOptionPane.getFrameForComponent(mapView);
						if (frame.isFocused())
		    				selected.requestFocusInWindow();
						else
							frame.addWindowFocusListener(new WindowAdapter() {
								@Override
                                public void windowGainedFocus(WindowEvent e) {
									frame.removeWindowFocusListener(this);
									selected.requestFocusInWindow();
									retryCount = 2;
									eventTimer.start();
                                }
							});
		    		}
		    	}
				if(retryCount > 1){
					retryCount--;
					eventTimer.start();
				}
            }
		  });
		timer.setRepeats(false);
		timer.start();
	}

	public void setTitle() {
		if(loadingLayoutFromObjectInputStream)
			return;
		if(!initialTabNameLoadingWasDone){
			loadInitialCustomTabNames();
		}
		for (Component mapViewComponent: mapViews) {
			if (mapViewComponent instanceof MapView ) {
	            updateTitle(mapViewComponent);
            }
		}
    }

    private void updateTitle(Component mapViewComponent) {
		View containingDockedWindow = getContainingDockedWindow(mapViewComponent);
		if(containingDockedWindow != null) {
			String title = createTitle(mapViewComponent);
			containingDockedWindow.getViewProperties().setTitle(title);
		}
    }

	private String createTitle(Component mapViewComponent) {
		MapView mapView = (MapView)mapViewComponent;
		String customizedTabName = (String) mapView.getClientProperty(CUSTOMIZED_TAB_NAME_PROPERTY);
		String name = customizedTabName != null ? customizedTabName : mapView.getName();
		String title;
		if(mapView.getMap().isSaved() || mapView.getMap().isReadOnly())
			title = name;
		else
			title = name + " *";
		return title;
	}

	public void selectNextMapView() {
		selectMap(1);
	}

	public void selectPreviousMapView() {
		selectMap(-1);
	}

	private void selectMap(final int tabIndexChange) {
		final Controller controller = Controller.getCurrentController();
		MapView mapView = (MapView)controller.getMapViewManager().getMapViewComponent();
		if(mapView != null){
			AbstractTabWindow tabWindow = (AbstractTabWindow) SwingUtilities.getAncestorOfClass(AbstractTabWindow.class, mapView);
			if(tabWindow != null){
				final DockingWindow selectedWindow = tabWindow.getSelectedWindow();
				final int childWindowIndex = tabWindow.getChildWindowIndex(selectedWindow);
				final int childWindowCount = tabWindow.getChildWindowCount();
				final int nextWindowIndex = (childWindowIndex + childWindowCount + tabIndexChange) % childWindowCount;
				final View nextWindow = (View) tabWindow.getChildWindow(nextWindowIndex);
				final Component nextMapView = getContainedMapView(nextWindow);
				Controller.getCurrentController().getMapViewManager().changeToMapView(nextMapView);
			}
		}
	}

	private void setTabAreaVisiblePolicy(final TabWindow window) {
		setTabAreaPolicy(window,
				window.getWindowParent() == rootWindow ?
						TabAreaVisiblePolicy.MORE_THAN_ONE_TAB :
							TabAreaVisiblePolicy.ALWAYS);
	}

	private void setTabAreaPolicy(TabWindow window, TabAreaVisiblePolicy tabAreaVisiblePolicy) {
		final TabAreaProperties tabAreaProperties = window.getTabWindowProperties().getTabbedPanelProperties().getTabAreaProperties();
		tabAreaProperties.setTabAreaVisiblePolicy(tabAreaVisiblePolicy);
	}

	public void setTabAreaVisiblePolicy(JFrame frame){
		DockingWindow window = (DockingWindow) (JOptionPane.getFrameForComponent(rootWindow) == frame ? rootWindow : frame.getContentPane().getComponent(0));
		setTabAreaVisiblePolicies(window);
	}

	private void setTabAreaVisiblePolicies(DockingWindow parentWindow) {
		for(int i = 0; i < parentWindow.getChildWindowCount(); i++){
			final DockingWindow window = parentWindow.getChildWindow(i);
			if(window instanceof TabWindow)
				setTabAreaVisiblePolicy((TabWindow) window);
			if (!(window instanceof FloatingWindow))
				setTabAreaVisiblePolicies(window);
		}
	}

	public void setTabAreaInvisiblePolicy(JFrame frame){
		DockingWindow window = (DockingWindow) (JOptionPane.getFrameForComponent(rootWindow) == frame ? rootWindow : frame.getContentPane().getComponent(0));
		setTabAreaInvisiblePolicies(window);
	}

	private void setTabAreaInvisiblePolicies(DockingWindow parentWindow) {
		for(int i = 0; i < parentWindow.getChildWindowCount(); i++){
			final DockingWindow window = parentWindow.getChildWindow(i);
			if(window instanceof TabWindow)
				setTabAreaPolicy((TabWindow) window, TabAreaVisiblePolicy.NEVER);
			if (!(window instanceof FloatingWindow))
				setTabAreaInvisiblePolicies(window);
		}
	}

	public List<? extends Component> getMapViewVector() {
		final ArrayList<Component> orderedMapViews = new ArrayList<Component>(mapViews.size());
		addMapViews(orderedMapViews, rootWindow);
		return orderedMapViews;
	}

	private void addMapViews(ArrayList<Component> orderedMapViews, DockingWindow window) {
		if(window instanceof View)
			orderedMapViews.add(getContainedMapView((View) window));
		else
			for (int windowIndex = 0; windowIndex < window.getChildWindowCount(); windowIndex++)
				addMapViews(orderedMapViews, window.getChildWindow(windowIndex));
	}

	private ClassicDockingTheme createClassicTheme() {
		ClassicDockingTheme classicDockingTheme = new ClassicDockingTheme();
		RootWindowProperties rootWindowProperties = classicDockingTheme.getRootWindowProperties();
		final ComponentProperties windowAreaProperties = rootWindowProperties.getWindowAreaProperties();
		TabbedPanelProperties tabbedPanelProperties = rootWindowProperties.getTabWindowProperties().getTabbedPanelProperties();
		windowAreaProperties.setBackgroundColor(null);
		windowAreaProperties.setForegroundColor(null);
		TabAreaComponentsProperties tabAreaComponentsProperties = tabbedPanelProperties.getTabAreaComponentsProperties();
		tabAreaComponentsProperties.getComponentProperties().setBackgroundColor(null);
		tabAreaComponentsProperties.getComponentProperties().setForegroundColor(null);
		return classicDockingTheme;
	}
}
