/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Insets;
import java.awt.dnd.DropTarget;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;

import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.TabbedPaneUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

import org.freeplane.features.mode.Controller;
import org.freeplane.features.ui.IMapViewChangeListener;
import org.freeplane.features.ui.ViewController;
import org.freeplane.features.url.mindmapmode.FileOpener;
import org.freeplane.view.swing.ui.DefaultMapMouseListener;

class MapViewTabs implements IMapViewChangeListener {
// // 	final private Controller controller;
	private Component mContentComponent;
	private JTabbedPane mTabbedPane = null;
	final private Vector<Component> mTabbedPaneMapViews;
	private boolean mTabbedPaneSelectionUpdate = true;
	private TabbedPaneUI tabbedPaneUI;

	public MapViewTabs( final ViewController fm, final JComponent contentComponent) {
//		this.controller = controller;
		mContentComponent = contentComponent;
		mTabbedPane = new JTabbedPane();
		removeTabbedPaneAccelerators();

		mTabbedPane.setFocusable(false);
		mTabbedPane.setTabPlacement(JTabbedPane.BOTTOM);
		mTabbedPaneMapViews = new Vector<Component>();
		mTabbedPane.addChangeListener(new ChangeListener() {
			public synchronized void stateChanged(final ChangeEvent pE) {
				tabSelectionChanged();
			}
		});
		final FileOpener fileOpener = new FileOpener();
		new DropTarget(mTabbedPane, fileOpener);
		mTabbedPane.addMouseListener(new DefaultMapMouseListener());

		final Controller controller = Controller.getCurrentController();
		controller.getMapViewManager().addMapViewChangeListener(this);
		fm.getContentPane().add(mTabbedPane, BorderLayout.CENTER);
	}

	void removeTabbedPaneAccelerators() {
	    final InputMap map = new InputMap();
		mTabbedPane.setInputMap(JTabbedPane.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, map);
    }

	public void afterViewChange(final Component pOldMap, final Component pNewMap) {
		final int selectedIndex = mTabbedPane.getSelectedIndex();
		if (pNewMap == null) {
			return;
		}
		for (int i = 0; i < mTabbedPaneMapViews.size(); ++i) {
			if (mTabbedPaneMapViews.get(i) == pNewMap) {
				if (selectedIndex != i) {
					mTabbedPane.setSelectedIndex(i);
				}
				return;
			}
		}
		mTabbedPaneMapViews.add(pNewMap);
		final String title1 = pNewMap.getName();
		final String title = title1;
		mTabbedPane.addTab(title, new JPanel());
		mTabbedPane.setSelectedIndex(mTabbedPane.getTabCount() - 1);
		setTabsVisible();
	}

	public void afterViewClose(final Component pOldMapView) {
		for (int i = 0; i < mTabbedPaneMapViews.size(); ++i) {
			if (mTabbedPaneMapViews.get(i) == pOldMapView) {
				mTabbedPaneSelectionUpdate = false;
				mTabbedPane.removeTabAt(i);
				mTabbedPaneMapViews.remove(i);
				mTabbedPaneSelectionUpdate = true;
				tabSelectionChanged();
				setTabsVisible();
				return;
			}
		}
	}

	public void afterViewCreated(final Component mapView) {
		mapView.addPropertyChangeListener("name", new PropertyChangeListener() {
			public void propertyChange(final PropertyChangeEvent evt) {
				final Component pMapView = (Component) evt.getSource();
				for (int i = 0; i < mTabbedPaneMapViews.size(); ++i) {
					if (mTabbedPaneMapViews.get(i) == pMapView) {
						mTabbedPane.setTitleAt(i, pMapView.getName());
					}
				}
			}
		});
	}

	public void beforeViewChange(final Component pOldMapView, final Component pNewMapView) {
	}

	public void removeContentComponent() {
		mContentComponent = null;
		if (mTabbedPane.getSelectedIndex() >= 0) {
			mTabbedPane.setComponentAt(mTabbedPane.getSelectedIndex(), new JPanel());
		}
	}

	public void setContentComponent(final Component mContentComponent) {
		this.mContentComponent = mContentComponent;
		if (mTabbedPane.getSelectedIndex() >= 0) {
			mTabbedPane.setComponentAt(mTabbedPane.getSelectedIndex(), mContentComponent);
		}
	}

	private void tabSelectionChanged() {
		if (!mTabbedPaneSelectionUpdate) {
			return;
		}
		final int selectedIndex = mTabbedPane.getSelectedIndex();
		for (int j = 0; j < mTabbedPane.getTabCount(); j++) {
			if (j != selectedIndex) {
				mTabbedPane.setComponentAt(j, new JPanel());
			}
		}
		if (selectedIndex < 0) {
			return;
		}
		final Component mapView = mTabbedPaneMapViews.get(selectedIndex);
		Controller controller = Controller.getCurrentController();
		if (mapView != controller.getViewController().getMapView()) {
			controller.getMapViewManager().changeToMapView(mapView.getName());
		}
		if (mContentComponent != null) {
			mContentComponent.setVisible(true);
			mTabbedPane.setComponentAt(selectedIndex, mContentComponent);
		}
	}

	private void setTabsVisible() {
		final boolean visible = mTabbedPane.getTabCount() > 1;
		if (visible == areTabsVisible()) {
			return;
		}
		if (tabbedPaneUI == null) {
			tabbedPaneUI = mTabbedPane.getUI();
		}
		if (visible) {
			mTabbedPane.setUI(tabbedPaneUI);
		}
		else {
			mTabbedPane.setUI(new BasicTabbedPaneUI() {
				@Override
				protected int calculateTabAreaHeight(final int tabPlacement, final int horizRunCount,
				                                     final int maxTabHeight) {
					return 0;
				}

				@Override
				protected Insets getContentBorderInsets(final int tabPlacement) {
					return new Insets(0, 0, 0, 0);
				}

				@Override
				protected MouseListener createMouseListener() {
					return null;
				}
			});
		}
		removeTabbedPaneAccelerators();
		mTabbedPane.revalidate();
	}

	private boolean areTabsVisible() {
		return tabbedPaneUI == null || tabbedPaneUI == mTabbedPane.getUI();
	}
}
