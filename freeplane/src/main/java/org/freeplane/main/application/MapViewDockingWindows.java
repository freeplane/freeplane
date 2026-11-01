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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;

import org.apache.commons.codec.binary.Base64;
import org.freeplane.api.TextWritingDirection;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.FileOpener;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.ui.textchanger.TranslatedElementFactory;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.ui.IMapViewChangeListener;
import org.freeplane.features.ui.IMapViewManager;
import org.freeplane.features.url.mindmapmode.DroppedMindMapOpener;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.MapViewController;
import org.freeplane.view.swing.map.NodeView;
import org.freeplane.view.swing.map.overview.MapViewPane;
import org.freeplane.view.swing.ui.DefaultMapMouseListener;
import org.freeplane.view.swing.ui.MapDisplayNameResolver;

import net.infonode.docking.AbstractTabWindow;
import net.infonode.docking.DockingWindow;
import net.infonode.docking.DockingWindowAdapter;
import net.infonode.docking.FloatingWindow;
import net.infonode.docking.OperationAbortedException;
import net.infonode.docking.RootWindow;
import net.infonode.docking.SplitWindow;
import net.infonode.docking.TabWindow;
import net.infonode.docking.View;
import net.infonode.docking.WindowPopupMenuFactory;
import net.infonode.docking.properties.DockingWindowProperties;
import net.infonode.docking.properties.RootWindowProperties;
import net.infonode.docking.properties.TabWindowProperties;
import net.infonode.docking.properties.WindowTabProperties;
import net.infonode.docking.properties.WindowTabStateProperties;
import net.infonode.docking.theme.ClassicDockingTheme;
import net.infonode.docking.theme.DockingWindowsTheme;
import net.infonode.docking.theme.LookAndFeelDockingTheme;
import net.infonode.docking.title.SimpleDockingWindowTitleProvider;
import net.infonode.docking.util.DockingUtil;
import net.infonode.gui.DynamicUIManager;
import net.infonode.gui.DynamicUIManagerListener;
import net.infonode.gui.icon.button.CloseIcon;
import net.infonode.gui.icon.button.DockIcon;
import net.infonode.gui.icon.button.DropDownIcon;
import net.infonode.gui.icon.button.MaximizeIcon;
import net.infonode.gui.icon.button.MinimizeIcon;
import net.infonode.gui.icon.button.RestoreIcon;
import net.infonode.gui.icon.button.UndockIcon;
import net.infonode.properties.gui.util.ComponentProperties;
import net.infonode.tabbedpanel.TabAreaComponentsProperties;
import net.infonode.tabbedpanel.TabAreaProperties;
import net.infonode.tabbedpanel.TabAreaVisiblePolicy;
import net.infonode.tabbedpanel.TabDropDownListVisiblePolicy;
import net.infonode.tabbedpanel.TabLayoutPolicy;
import net.infonode.tabbedpanel.TabbedPanelProperties;
import net.infonode.tabbedpanel.titledtab.TitledTabProperties;
import net.infonode.tabbedpanel.titledtab.TitledTabSizePolicy;
import net.infonode.tabbedpanel.titledtab.TitledTabStateProperties;
import net.infonode.util.Direction;

class MapViewDockingWindows implements IMapViewChangeListener {

	private static final String FREEPLANE_FLOATING_WINDOW = "freeplaneFloatingWindow";
	private static final String CUSTOMIZED_TAB_NAME_PROPERTY = "customizedDockingTabName";
	private static final String TAB_TITLE_METADATA_PROPERTY = "dockingTabTitleMetadata_1.0";

	private static final String OPENED_NOW = "openedNow_1.3.04";
	private RootWindow rootWindow = null;
	final private Vector<Component> mapViews;
	private final Map<String, Deque<PendingTabTitle>> pendingTabTitles =
			new HashMap<String, Deque<PendingTabTitle>>();
	private boolean mPaneSelectionUpdate = true;
	private boolean loadingLayoutFromObjectInputStream;
	private byte[] emptyConfigurations;
	private final MapViewSerializer viewSerializer;
	private DockingWindowsTheme theme;
	private Dimension capturedWindowSize = null;
	private final ApplicationViewController applicationViewController;

	private static final class PendingTabTitle {
		private final String windowSignature;
		private final String title;

		private PendingTabTitle(String windowSignature, String title) {
			this.windowSignature = windowSignature;
			this.title = title;
		}
	}

    public MapViewDockingWindows(ApplicationViewController applicationViewController) {
		this.applicationViewController = applicationViewController;
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

		KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener("focusedWindow", evt -> {
			Window newWindow = (Window) evt.getNewValue();
			if (newWindow == null)
				return;
			IMapViewManager mgr = Controller.getCurrentController().getMapViewManager();
			JComponent candidate = null;
            try {
                JComponent selected = mgr.getMapViewComponent();
                if (selected != null && SwingUtilities.isDescendingFrom(selected, newWindow))
                    candidate = selected;
                else
                    candidate = mgr.getLastSelectedMapViewContainedIn(newWindow);
            } catch (Exception ignore) { }
            if (candidate == null) {
                candidate = findAnyVisibleMapViewInWindow(newWindow);
            }
			if (newWindow instanceof RootPaneContainer) {
				JRootPane root = ((RootPaneContainer) newWindow).getRootPane();
				root.putClientProperty(IMapViewManager.LAST_SELECTED_MAP_VIEW_PROPERTY, candidate);
			}
			if (mgr instanceof MapViewController) {
				((MapViewController) mgr).updateWindowLastSelectedMapView(newWindow,
						candidate instanceof MapView ? (MapView) candidate : null);
			}
        });
		rootWindow.addListener(new DockingWindowAdapter(){

			private IconColorReplacer iconColorReplacer;

			@Override
            public void viewFocusChanged(View previouslyFocusedView, View focusedView) {
                if (focusedView != null) {
                    Component containedMapView = getContainedMapView(focusedView);

                    Window top = (Window) focusedView.getTopLevelAncestor();
                    if (top instanceof RootPaneContainer && containedMapView instanceof MapView) {
                        JRootPane root = ((RootPaneContainer) top).getRootPane();
                        root.putClientProperty(IMapViewManager.LAST_SELECTED_MAP_VIEW_PROPERTY, containedMapView);
                        IMapViewManager mgr = Controller.getCurrentController().getMapViewManager();
                        if (mgr instanceof MapViewController) {
                            ((MapViewController) mgr).updateWindowLastSelectedMapView(top, (MapView) containedMapView);
                        }
                    }
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
                    if(topLevelAncestor instanceof JFrame){
						if(iconColorReplacer == null)
							iconColorReplacer = new IconColorReplacer((UITools.getFrame()).getIconImages());
						final List<Image> iconImages = iconColorReplacer.getNextIconImages();
						final JFrame frame = (JFrame)topLevelAncestor;
						frame.getRootPane().putClientProperty(FREEPLANE_FLOATING_WINDOW, addedWindow);
						frame.setIconImages(iconImages);

						applicationViewController.createAuxillaryPaneForFloatingWindow((Window) topLevelAncestor, addedWindow);

						applyCapturedSize(topLevelAncestor);

						Compat.registerFullScreenListener(frame);

                        addedWindow.addListener(new DockingWindowAdapter() {
                            @Override
                            public void viewFocusChanged(View previouslyFocusedView, View focusedView) {
                                if (focusedView != null) {
                                    Component containedMapView = getContainedMapView(focusedView);
                                    Window top = (Window) focusedView.getTopLevelAncestor();
                                    if (top instanceof RootPaneContainer && containedMapView instanceof MapView) {
                                        JRootPane root = ((RootPaneContainer) top).getRootPane();
                                        root.putClientProperty(IMapViewManager.LAST_SELECTED_MAP_VIEW_PROPERTY, containedMapView);
                                        IMapViewManager mgr = Controller.getCurrentController().getMapViewManager();
                                        if (mgr instanceof MapViewController) {
                                            ((MapViewController) mgr)
                                                    .updateWindowLastSelectedMapView(top, (MapView) containedMapView);
                                        }
                                    }
                                }
                            }

                            @Override
                            public void windowRemoved(DockingWindow removedFromWindow, DockingWindow removedWindow) {
                                if (removedWindow instanceof View && ((View)removedWindow).getComponent() instanceof MapViewPane) {
                                    Component contained = getContainedMapView((View) removedWindow);
                                    Window sourceTop = (Window) removedFromWindow.getTopLevelAncestor();
                                    if (sourceTop instanceof RootPaneContainer) {
                                        JRootPane root = ((RootPaneContainer) sourceTop).getRootPane();
                                        Object value = root.getClientProperty(IMapViewManager.LAST_SELECTED_MAP_VIEW_PROPERTY);
                                        if (value == contained) {
                                            root.putClientProperty(IMapViewManager.LAST_SELECTED_MAP_VIEW_PROPERTY, null);
                                            IMapViewManager mgr = Controller.getCurrentController().getMapViewManager();
                                            if (mgr instanceof MapViewController) {
                                                ((MapViewController) mgr)
                                                        .updateWindowLastSelectedMapView((Window) removedFromWindow.getTopLevelAncestor(), null);
                                            }
                                        }
                                    }
                                }
                            }
                        });
                    }
                }
                else
                	if (addedWindow instanceof View && ((View)addedWindow).getComponent() instanceof MapViewPane) {
                		Component contained = getContainedMapView((View) addedWindow);
                		Window sourceTop = (Window) addedToWindow.getTopLevelAncestor();
                		IMapViewManager mgr = Controller.getCurrentController().getMapViewManager();
                		if (sourceTop instanceof RootPaneContainer && mgr.getMapViewComponent() == contained) {
                			JRootPane root = ((RootPaneContainer) sourceTop).getRootPane();
                			Object value = root.getClientProperty(IMapViewManager.LAST_SELECTED_MAP_VIEW_PROPERTY);
                			if (value != contained) {
                				root.putClientProperty(IMapViewManager.LAST_SELECTED_MAP_VIEW_PROPERTY, contained);
                				if (mgr instanceof MapViewController) {
                					((MapViewController) mgr).updateWindowLastSelectedMapView(sourceTop, (JComponent) contained);
                				}
                			}
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

				captureCenterComponentSize(removedFromWindow);

				if(removedWindow instanceof TabWindow) {
                    if (removedFromWindow == rootWindow) {
                    	final TabAreaProperties tabAreaProperties = ((TabWindow)removedWindow).getTabWindowProperties().getTabbedPanelProperties().getTabAreaProperties();
                        tabAreaProperties.setTabAreaVisiblePolicy(TabAreaVisiblePolicy.ALWAYS);
                    }
                }
                else if(removedWindow instanceof FloatingWindow) {
                    final Container topLevelAncestor = removedFromWindow.getTopLevelAncestor();
                    if(topLevelAncestor instanceof Window) {
                        applicationViewController.removeAuxillaryPaneForFloatingWindow((Window) topLevelAncestor);

                        if (topLevelAncestor instanceof RootPaneContainer) {
                            JRootPane root = ((RootPaneContainer) topLevelAncestor).getRootPane();
                            Object old = root.getClientProperty(IMapViewManager.LAST_SELECTED_MAP_VIEW_PROPERTY);
                            if (old != null) {
                                root.putClientProperty(IMapViewManager.LAST_SELECTED_MAP_VIEW_PROPERTY, null);
                                IMapViewManager mgr = Controller.getCurrentController().getMapViewManager();
                                if (mgr instanceof MapViewController) {
                                    ((MapViewController) mgr).updateWindowLastSelectedMapView((Window) topLevelAncestor, null);
                                }
                            }
                        }
                    }
                }
                else if (removedWindow instanceof View && ((View)removedWindow).getComponent() instanceof MapViewPane) {
                    Component contained = getContainedMapView((View) removedWindow);
                    Window sourceTop = (Window) removedFromWindow.getTopLevelAncestor();
                    if (sourceTop instanceof RootPaneContainer) {
                        JRootPane root = ((RootPaneContainer) sourceTop).getRootPane();
                        Object value = root.getClientProperty(IMapViewManager.LAST_SELECTED_MAP_VIEW_PROPERTY);
                        if (value == contained) {

                            IMapViewManager mgr = Controller.getCurrentController().getMapViewManager();
                            JComponent replacement = findAnyVisibleMapViewInWindow(sourceTop);
                            root.putClientProperty(IMapViewManager.LAST_SELECTED_MAP_VIEW_PROPERTY, replacement);
                            if (mgr instanceof MapViewController) {
                                ((MapViewController) mgr).updateWindowLastSelectedMapView(sourceTop,
                                        replacement instanceof MapView ? (MapView) replacement : null);
                            }
                        }
                    }
                }
            }
		});

		addTabsPopupMenu(rootWindow);

	}

	private void captureCenterComponentSize(DockingWindow removedFromWindow) {
		Container topLevelAncestor = removedFromWindow.getTopLevelAncestor();
		if (topLevelAncestor instanceof RootPaneContainer) {
			JRootPane rootPane = ((RootPaneContainer) topLevelAncestor).getRootPane();
			capturedWindowSize = rootPane.getSize();
		}
	}

	private void applyCapturedSize(Container topLevelAncestor) {
		if (capturedWindowSize != null && topLevelAncestor instanceof RootPaneContainer) {

			((RootPaneContainer) topLevelAncestor).getRootPane().setPreferredSize(capturedWindowSize);
			((Window) topLevelAncestor).pack();
			((RootPaneContainer) topLevelAncestor).getRootPane().setPreferredSize(null);


			capturedWindowSize = null;
		}
	}

	private void addTabsPopupMenu(DockingWindow dockingWindow){
		dockingWindow.setPopupMenuFactory(new WindowPopupMenuFactory() {
			public JPopupMenu createPopupMenu(DockingWindow window) {
				if( window.getWindowParent() instanceof RootWindow || window.getWindowParent() instanceof SplitWindow) {
				   return null;
				}
				JPopupMenu menu = new JPopupMenu(window.getTitle());
                JMenuItem closeItem = TranslatedElementFactory.createMenuItem("close_map");
                closeItem.setIcon(new CloseIcon(tabIconSize()));
                closeItem.addActionListener(e -> {
                    Controller.getCurrentController().getMapViewManager().close(getContainedMapView(window));
                });
                menu.add(closeItem);
                JMenuItem renameItem = new JMenuItem(TextUtils.getText("TabPopUpMenu.rename.text","Rename"));
				renameItem.setToolTipText(TextUtils.getText("TabPopUpMenu.rename.tooltip",
						"The tab name is saved with the window layout."));
				renameItem.addActionListener(e -> {
					String customizedTabName = (String) window.getClientProperty(CUSTOMIZED_TAB_NAME_PROPERTY);
					if (customizedTabName == null) {
						Component containedMapView = getContainedMapView(window);
						customizedTabName = containedMapView instanceof MapView
								? containedMapView.getName()
								: window.getTitle();
					}
					String newName = JOptionPane.showInputDialog(
							TextUtils.getText("TabPopUpMenu.rename.inputDialog","Input new tab name: "),
							customizedTabName);
					if (newName == null) {
						return;
					}
					window.putClientProperty(CUSTOMIZED_TAB_NAME_PROPERTY,
							newName.isEmpty() ? null : newName);
                	addTitleProvider(window);
					persistTabTitleMetadata();
                    Controller.getCurrentController().getMapViewManager().setMapTitles();
                });
				menu.add(renameItem);
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

		TabWindowProperties tabWindowProperties = overwrittenProperties.getTabWindowProperties();
		TabbedPanelProperties tabbedPanelProperties = tabWindowProperties.getTabbedPanelProperties();
		tabbedPanelProperties.getContentPanelProperties()
		    .getComponentProperties().setInsets(new Insets(0, 0, 0, 0)).setBorder(BorderFactory.createEmptyBorder());

		int buttonSize = tabIconSize();
		tabWindowProperties.getMaximizeButtonProperties().setIcon(new MaximizeIcon(buttonSize));
		tabWindowProperties.getMinimizeButtonProperties().setIcon(new MinimizeIcon(buttonSize));
		tabWindowProperties.getCloseButtonProperties().setIcon(new CloseIcon(buttonSize));
		tabWindowProperties.getRestoreButtonProperties().setIcon(new RestoreIcon(buttonSize));
        tabWindowProperties.getDockButtonProperties().setIcon(new DockIcon(buttonSize));
        tabWindowProperties.getUndockButtonProperties().setIcon(new UndockIcon(buttonSize));

		tabbedPanelProperties.setTabLayoutPolicy(TabLayoutPolicy.COMPRESSION);
		tabbedPanelProperties.setTabDropDownListVisiblePolicy(TabDropDownListVisiblePolicy.MORE_THAN_ONE_TAB);
		tabbedPanelProperties.setShadowEnabled(false);
        tabbedPanelProperties.getButtonProperties().getTabDropDownListButtonProperties().setIcon(new DropDownIcon(buttonSize, Direction.DOWN));

		Font tabFont = UITools.getUIFont();
		WindowTabProperties tabProperties = tabWindowProperties.getTabProperties();
        TitledTabProperties titledTabProperties = tabProperties.getTitledTabProperties();
		titledTabProperties.setSizePolicy(TitledTabSizePolicy.INDIVIDUAL_SIZE);
		ComponentProperties highlightedProperties = titledTabProperties.getHighlightedProperties().getComponentProperties();
		highlightedProperties.setFont(tabFont);
		ComponentProperties normalProperties = titledTabProperties.getNormalProperties().getComponentProperties();
		normalProperties.setFont(tabFont);
		rootWindowProperties.addSuperObject(overwrittenProperties);

        WindowTabStateProperties normalButtonProperties = tabProperties.getNormalButtonProperties();
        normalButtonProperties.getDockButtonProperties().setIcon(new DockIcon(buttonSize));
        normalButtonProperties.getUndockButtonProperties().setIcon(new UndockIcon(buttonSize));
        normalButtonProperties.getCloseButtonProperties().setIcon(new CloseIcon(buttonSize));
        normalButtonProperties.getMinimizeButtonProperties().setIcon(new MinimizeIcon(buttonSize));
        normalButtonProperties.getRestoreButtonProperties().setIcon(new RestoreIcon(buttonSize));
	}

    private static int tabIconSize() {
        return UITools.getUIFontSize(0.8);
    }

    private JComponent findAnyVisibleMapViewInWindow(Window window) {
        for (Component c : mapViews.toArray(new Component[0])) {
            if (c instanceof MapView && c.isShowing() && SwingUtilities.isDescendingFrom(c, window)) {
                return (JComponent) c;
            }
        }
        return null;
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
					focusMapViewLater((MapView) pNewMap, () -> {/**/});
					return;
				}
			}
	        addDockedWindow(pOldMap, pNewMap);
		}
		mapViews.add(pNewMap);
	}

    private String getCustomTitle(DockingWindow window){
        String windowName = (String) window.getClientProperty(CUSTOMIZED_TAB_NAME_PROPERTY);
        if (windowName == null) {
            return SimpleDockingWindowTitleProvider.INSTANCE.getTitle(window);
        } else {
            boolean dirty = isAnyChildWindowDirty(window);
            int childWindowsCount = getChildWindowsCount(window);
            return windowName + (childWindowsCount>1?" (" + childWindowsCount + ")":"") +(dirty?" *":"");
        }
    }

	private boolean isAnyChildWindowDirty(DockingWindow window){
		if (window == null)
			return false;
		else if (window instanceof View) {
			Component mapViewComponent = getContainedMapView((View) window);
			if (mapViewComponent instanceof MapView) {
				MapModel map = ((MapView) mapViewComponent).getMap();
				return !map.isSaved() && !map.isReadOnly();
			}
			return false;
		}
		else {
			for (int i = 0; i < window.getChildWindowCount(); i++) {
				if (isAnyChildWindowDirty(window.getChildWindow(i)) )
					return true;
			}
		}
		return false;
	}

	private int getChildWindowsCount(DockingWindow window){
		int windowCount = 0;
		if (window == null)
			return 0;
		else if (window instanceof View) {
			return 1;
		}
		else {
			for (int i = 0; i < window.getChildWindowCount(); i++) {
				windowCount += getChildWindowsCount(window.getChildWindow(i));
			}
		}
		return windowCount;
	}

	private void addTitleProvider(DockingWindow window){
		window.getWindowProperties().setTitleProvider(this::getCustomTitle);
	}

	private void preserveCustomizedTitleForReopenedMap(Component mapViewComponent) {
		if (loadingLayoutFromObjectInputStream || !(mapViewComponent instanceof MapView)) {
			return;
		}
		String mapUrl = getMapUrlKey((MapView) mapViewComponent);
		if (mapUrl == null) {
			return;
		}
		View view = getContainingDockedWindow(mapViewComponent);
		DockingWindow titleOwner = findCustomizedTitleOwner(view);
		if (titleOwner == null) {
			return;
		}
		String title = (String) titleOwner.getClientProperty(CUSTOMIZED_TAB_NAME_PROPERTY);
		Deque<PendingTabTitle> titles = pendingTabTitles.get(mapUrl);
		if (titles == null) {
			titles = new ArrayDeque<PendingTabTitle>();
			pendingTabTitles.put(mapUrl, titles);
		}
		titles.addLast(new PendingTabTitle(getWindowSignature(titleOwner), title));
	}

	private void restorePendingCustomizedTitle(Component mapViewComponent, View newView) {
		if (loadingLayoutFromObjectInputStream || !(mapViewComponent instanceof MapView)) {
			return;
		}
		String mapUrl = getMapUrlKey((MapView) mapViewComponent);
		Deque<PendingTabTitle> titles = mapUrl != null ? pendingTabTitles.get(mapUrl) : null;
		if (titles == null || titles.isEmpty()) {
			return;
		}
		PendingTabTitle pendingTitle = titles.removeFirst();
		if (titles.isEmpty()) {
			pendingTabTitles.remove(mapUrl);
		}
		DockingWindow titleOwner = findAncestorWithSignature(newView, pendingTitle.windowSignature);
		if (titleOwner == null) {
			titleOwner = newView;
		}
		titleOwner.putClientProperty(CUSTOMIZED_TAB_NAME_PROPERTY, pendingTitle.title);
		addTitleProvider(titleOwner);
	}

	private DockingWindow findCustomizedTitleOwner(DockingWindow window) {
		DockingWindow current = window;
		while (current != null) {
			if (current.getClientProperty(CUSTOMIZED_TAB_NAME_PROPERTY) instanceof String) {
				return current;
			}
			current = current.getWindowParent();
		}
		return null;
	}

	private DockingWindow findAncestorWithSignature(DockingWindow window, String signature) {
		DockingWindow current = window;
		while (current != null) {
			if (signature.equals(getWindowSignature(current))) {
				return current;
			}
			current = current.getWindowParent();
		}
		return null;
	}

	private String getMapUrlKey(MapView mapView) {
		return mapView.getMap().getURL() != null ? mapView.getMap().getURL().toExternalForm() : null;
	}

	private String getWindowSignature(DockingWindow window) {
		StringBuilder signature = new StringBuilder();
		appendSignaturePart(signature, window.getClass().getName());
		if (window instanceof View) {
			Component mapView = getContainedMapView((View) window);
			String identity = mapView instanceof MapView
					? getMapUrlKey((MapView) mapView)
					: window.getTitle();
			appendSignaturePart(signature, identity != null ? identity : "");
		}
		else {
			List<String> childSignatures = new ArrayList<String>(window.getChildWindowCount());
			for (int i = 0; i < window.getChildWindowCount(); i++) {
				childSignatures.add(getWindowSignature(window.getChildWindow(i)));
			}
			Collections.sort(childSignatures);
			for (String childSignature : childSignatures) {
				appendSignaturePart(signature, childSignature);
			}
		}
		return signature.toString();
	}

	private void appendSignaturePart(StringBuilder signature, String value) {
		signature.append(value.length()).append(':').append(value);
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
	    Component pane = dockedWindow.getComponent();
	    if(pane instanceof MapViewPane) {
	    	JScrollPane scrollPane = ((MapViewPane) pane).getMapViewScrollPane();
	    	Component view = scrollPane.getViewport().getView();
	    	return view;
	    }
	    else
	    	return null;
    }

	private void addDockedWindow(final Component pOldMap, final Component pNewMap) {
	    final View viewFrame = viewSerializer.newDockedView(pNewMap, createTitle(pNewMap));
		addDockedView(pOldMap != null ? getContainingDockedWindow(pOldMap) : null, viewFrame);
		restorePendingCustomizedTitle(pNewMap, viewFrame);
        updateTabTooltip(pNewMap);
    }

    private void updateTabTooltip(final Component pNewMap) {
        File file = ((MapView)pNewMap).getMap().getFile();
        TitledTabStateProperties tabProperties = getContainingDockedWindow(pNewMap)
                .getWindowProperties().getTabProperties().getTitledTabProperties().getNormalProperties();
        tabProperties.setToolTipText(file != null ? TextWritingDirection.LEFT_TO_RIGHT.isolatePathSeparators(file.getAbsolutePath()) :  null);
    }

    @Override
	public void afterViewClose(final Component pOldMapView) {
        for (int i = 0; i < mapViews.size(); ++i) {
            if (mapViews.get(i) == pOldMapView) {
				preserveCustomizedTitleForReopenedMap(pOldMapView);
                mPaneSelectionUpdate = false;
                getContainingDockedWindow(pOldMapView).close();
                mapViews.remove(i);
                mPaneSelectionUpdate = true;
                rootWindow.repaint();

                Window w = SwingUtilities.getWindowAncestor(pOldMapView);
                if (w instanceof RootPaneContainer) {
                    Object value = ((RootPaneContainer) w).getRootPane()
                            .getClientProperty(IMapViewManager.LAST_SELECTED_MAP_VIEW_PROPERTY);
                    if (value == pOldMapView) {
                        ((RootPaneContainer) w).getRootPane().putClientProperty(
                                IMapViewManager.LAST_SELECTED_MAP_VIEW_PROPERTY, null);
                    }
                }
                return;
            }
        }
    }
	private void viewSelectionChanged(final Component mapView) {
		if (!mPaneSelectionUpdate ||
				! SwingUtilities.isDescendingFrom(mapView, applicationViewController.getMenuComponent())
				&& ! FrameComponentMover.selectedMapFollowsActiveWindow()) {
			return;
		}
		Controller controller = Controller.getCurrentController();
		if (mapView != controller.getMapViewManager().getMapViewComponent()) {
			controller.getMapViewManager().changeToMapView(mapView.getName());
		}
	}

	public RootWindow getRootWindow() {
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
			persistTabTitleMetadata();
        }
        catch (IOException e) {
	        e.printStackTrace();
        }
	}

	public void loadLayout(){
		ResourceController resourceController = ResourceController.getResourceController();
		String encodedBytes = resourceController.getProperty(OPENED_NOW, null);
		if(encodedBytes != null){
			String encodedTabTitles = resourceController.getProperty(TAB_TITLE_METADATA_PROPERTY, null);
			List<DockingWindowTabTitleMetadata.Entry> tabTitles = null;
			if (encodedTabTitles != null) {
				try {
					tabTitles = DockingWindowTabTitleMetadata.decode(encodedTabTitles);
				}
				catch (IOException e) {
					LogUtils.severe(e);
				}
			}
			byte[] bytes = Base64.decodeBase64(encodedBytes);
			ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
			boolean layoutWasRead = false;
			try {
				loadingLayoutFromObjectInputStream = true;
				rootWindow.read(new ObjectInputStream(byteStream));
				layoutWasRead = true;
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
			if (layoutWasRead) {
				if (tabTitles != null) {
					restoreTabTitleMetadata(tabTitles);
				}
				else {
					migrateLegacyTabTitles();
					persistTabTitleMetadata();
				}
			}
			setTitle();
		}
	}

	private void migrateLegacyTabTitles(){
		for (Component mapViewComponent: mapViews) {
			if (mapViewComponent instanceof MapView ) {
				View containingDockedWindow = getContainingDockedWindow(mapViewComponent);
				if(containingDockedWindow != null) {
					String oldTitle = containingDockedWindow.getViewProperties().getTitle();
					MapView mapView = (MapView)mapViewComponent;
					if(oldTitle != null && !oldTitle.equals("")){
						String titleWithoutDirtyMarker = MapDisplayNameResolver.removeDirtyMarker(oldTitle);
						if(!MapDisplayNameResolver.matchesAnyAutomaticLabel(mapView, titleWithoutDirtyMarker)) {
							containingDockedWindow.putClientProperty(CUSTOMIZED_TAB_NAME_PROPERTY, titleWithoutDirtyMarker);
							addTitleProvider(containingDockedWindow);
						}
					}
				}
			}
		}
	}

	private void persistTabTitleMetadata() {
		try {
			List<DockingWindowTabTitleMetadata.Entry> entries = new ArrayList<DockingWindowTabTitleMetadata.Entry>();
			collectTabTitleMetadata(rootWindow, "", entries);
			ResourceController.getResourceController().setProperty(TAB_TITLE_METADATA_PROPERTY,
					DockingWindowTabTitleMetadata.encode(entries));
		}
		catch (IOException e) {
			LogUtils.severe(e);
		}
	}

	private void collectTabTitleMetadata(DockingWindow window, String path,
			List<DockingWindowTabTitleMetadata.Entry> entries) {
		Object title = window.getClientProperty(CUSTOMIZED_TAB_NAME_PROPERTY);
		if (title instanceof String && !((String) title).isEmpty()) {
			entries.add(new DockingWindowTabTitleMetadata.Entry(path, getWindowSignature(window), (String) title));
		}
		for (int i = 0; i < window.getChildWindowCount(); i++) {
			collectTabTitleMetadata(window.getChildWindow(i), childPath(path, i), entries);
		}
	}

	private void restoreTabTitleMetadata(List<DockingWindowTabTitleMetadata.Entry> entries) {
		for (DockingWindowTabTitleMetadata.Entry entry : entries) {
			DockingWindow window = findWindowAtPath(entry.path);
			if (window != null && entry.windowSignature.equals(getWindowSignature(window))) {
				window.putClientProperty(CUSTOMIZED_TAB_NAME_PROPERTY, entry.title);
				addTitleProvider(window);
			}
		}
	}

	private DockingWindow findWindowAtPath(String path) {
		DockingWindow window = rootWindow;
		if (path.isEmpty()) {
			return window;
		}
		for (String childIndex : path.split("/")) {
			try {
				int index = Integer.parseInt(childIndex);
				if (index < 0 || index >= window.getChildWindowCount()) {
					return null;
				}
				window = window.getChildWindow(index);
			}
			catch (NumberFormatException e) {
				return null;
			}
		}
		return window;
	}

	private String childPath(String parentPath, int childIndex) {
		return parentPath.isEmpty() ? Integer.toString(childIndex) : parentPath + "/" + childIndex;
	}

	public void focusMapViewLater(final MapView mapView, Runnable onFocus) {
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
				else
					onFocus.run();
            }
		  });
		timer.setRepeats(false);
		timer.start();
	}

	public void setTitle() {
		if(loadingLayoutFromObjectInputStream)
			return;
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
			updateTabTooltip(mapViewComponent);
		}
    }

	private String createTitle(Component mapViewComponent) {
		MapView mapView = (MapView)mapViewComponent;
		String name = MapDisplayNameResolver.resolveBaseLabel(mapView);
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

	public void setTabAreaVisiblePolicy(JFrame frame, boolean visible){
		DockingWindow window = (DockingWindow) (JOptionPane.getFrameForComponent(rootWindow) == frame ? rootWindow
				: frame.getRootPane().getClientProperty(FREEPLANE_FLOATING_WINDOW));
		if(visible)
			setTabAreaVisiblePolicies(window);
		else
			setTabAreaInvisiblePolicies(window);
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


	void visitAllFloatingWindows(Consumer<FloatingWindow> visitor) {
		visitAllFloatingWindowsRecursive(rootWindow, visitor);
	}

	private void visitAllFloatingWindowsRecursive(DockingWindow window, Consumer<FloatingWindow> visitor) {
		if (window instanceof FloatingWindow) {
			visitor.accept((FloatingWindow) window);
		}
		for (int i = 0; i < window.getChildWindowCount(); i++) {
			visitAllFloatingWindowsRecursive(window.getChildWindow(i), visitor);
		}
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

	public void selectMapNextView(JComponent current) {
	    selectMapViewRelative(current,  1);
	}

	public void selectMapPreviousView(JComponent current) {
	    selectMapViewRelative(current, -1);
	}

	private void selectMapViewRelative(JComponent current, int step) {
	    if (!(current instanceof MapView)) return;
	    int currentIndex = mapViews.indexOf(current);
	    if (currentIndex < 0) return;
	    MapModel map = ((MapView) current).getMap();
	    int n = mapViews.size();
	    int idx = Math.floorMod(currentIndex + step, n);
	    while (idx != currentIndex) {
	        Component c = mapViews.get(idx);
	        if (c instanceof MapView && ((MapView) c).getMap() == map) {
	            Controller.getCurrentController()
	                      .getMapViewManager()
	                      .changeToMapView(c);
	            return;
	        }
	        idx = Math.floorMod(idx + step, n);
	    }
	}
}
