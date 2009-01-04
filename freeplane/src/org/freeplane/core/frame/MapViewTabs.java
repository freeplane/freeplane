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
package org.freeplane.core.frame;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.model.MapModel;
import org.freeplane.view.swing.map.MapView;

class MapViewTabs implements IMapViewChangeListener {
	private Component mContentComponent;
	private JTabbedPane mTabbedPane = null;
	final private Vector mTabbedPaneMapViews;
	private boolean mTabbedPaneSelectionUpdate = true;

	public MapViewTabs(final ApplicationViewController fm, final JComponent contentComponent) {
		mContentComponent = contentComponent;
		InputMap map;
		map = (InputMap) UIManager.get("TabbedPane.ancestorInputMap");
		final KeyStroke keyStrokeCtrlUp = KeyStroke.getKeyStroke(KeyEvent.VK_UP,
		    InputEvent.CTRL_DOWN_MASK);
		map.remove(keyStrokeCtrlUp);
		mTabbedPane = new JTabbedPane();
		mTabbedPane.setFocusable(false);
		mTabbedPaneMapViews = new Vector();
		mTabbedPane.addChangeListener(new ChangeListener() {
			public synchronized void stateChanged(final ChangeEvent pE) {
				tabSelectionChanged();
			}
		});
		Controller.getController().getMapViewManager().addIMapViewChangeListener(this);
		Controller.getController().getViewController().addMapTitleChangeListener(
		    new IMapTitleChangeListener() {
			    public void setMapTitle(final String pNewMapTitle, final MapView pMapView,
			                            final MapModel pModel) {
				    for (int i = 0; i < mTabbedPaneMapViews.size(); ++i) {
					    if (mTabbedPaneMapViews.get(i) == pMapView) {
						    mTabbedPane.setTitleAt(i, pNewMapTitle
						            + ((pModel.isSaved()) ? "" : "*"));
					    }
				    }
			    }
		    });
		fm.getContentPane().add(mTabbedPane, BorderLayout.CENTER);
	}

	public void afterMapClose(final MapView pOldMapView) {
		for (int i = 0; i < mTabbedPaneMapViews.size(); ++i) {
			if (mTabbedPaneMapViews.get(i) == pOldMapView) {
				mTabbedPaneSelectionUpdate = false;
				mTabbedPane.removeTabAt(i);
				mTabbedPaneMapViews.remove(i);
				mTabbedPaneSelectionUpdate = true;
				tabSelectionChanged();
				return;
			}
		}
	}

	public void afterMapViewChange(final MapView pOldMapView, final MapView pNewMapView) {
		final int selectedIndex = mTabbedPane.getSelectedIndex();
		if (pNewMapView == null) {
			return;
		}
		for (int i = 0; i < mTabbedPaneMapViews.size(); ++i) {
			if (mTabbedPaneMapViews.get(i) == pNewMapView) {
				if (selectedIndex != i) {
					mTabbedPane.setSelectedIndex(i);
				}
				return;
			}
		}
		mTabbedPaneMapViews.add(pNewMapView);
		final String title1 = pNewMapView.getModel().getTitle();
		final String title = title1;
		mTabbedPane.addTab(title, new JPanel());
		mTabbedPane.setSelectedIndex(mTabbedPane.getTabCount() - 1);
	}

	public void beforeMapViewChange(final MapView pOldMapView, final MapView pNewMapView) {
	}

	public boolean isMapViewChangeAllowed(final MapView pOldMapView, final MapView pNewMapView) {
		return true;
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
		final MapView mapView = (MapView) mTabbedPaneMapViews.get(selectedIndex);
		if (mapView != Controller.getController().getMapView()) {
			Controller.getController().getMapViewManager().changeToMapView(mapView.getName());
		}
		if (mContentComponent != null) {
			mContentComponent.setVisible(true);
			mTabbedPane.setComponentAt(selectedIndex, mContentComponent);
		}
	}
}
