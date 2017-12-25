/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
 *
 *  This file author is Stefan Ott
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
package org.freeplane.view.swing.features.filepreview;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URI;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.url.UrlManager;
import org.freeplane.view.swing.features.progress.mindmapmode.ProgressUtilities;
import org.freeplane.view.swing.map.MainView;

/**
 * @author Stefan Ott
 *
 * This class shows a popup menu for the external image.
 */
class ExternalImagePopupMenu extends JPopupMenu implements MouseListener {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private NodeModel node = null;
	private ViewerController viewer = null;
	private JMenuItem remove = null;
	private JMenuItem change = null;
	private JMenuItem open = null;
	private JMenuItem resetZoom = null;

	@Override
	protected void firePopupMenuWillBecomeInvisible() {
		super.firePopupMenuWillBecomeInvisible();
		//	removeAll();
	}

	@Override
	protected void firePopupMenuWillBecomeVisible() {
		super.firePopupMenuWillBecomeVisible();
	}

	/**
	 * @return Returns the delete menu item.
	 */
	private JMenuItem getRemove() {
		final ProgressUtilities progUtil = new ProgressUtilities();
		if (remove == null) {
			remove = new JMenuItem(TextUtils.getText("ExternalImage_popupMenu_Remove"));
			remove.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					if (progUtil.hasExternalResource(node) && !progUtil.hasExtendedProgressIcon(node)) {
						viewer.undoableDeactivateHook(node);
						Controller.getCurrentModeController().getMapController().nodeChanged(node,
						    NodeModel.UNKNOWN_PROPERTY, null, null);
					}
				}
			});
		}
		return remove;
	}

	/**
	 * @return Returns the open menu item.
	 */
	private JMenuItem getOpen() {
		if (open == null) {
			open = new JMenuItem(TextUtils.getText("ExternalImage_popupMenu_Open"));
			open.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					final ExternalResource extRes = node.getExtension(ExternalResource.class);
					if(extRes == null)
						return;
					final UrlManager urlManager = Controller.getCurrentModeController().getExtension(
					    UrlManager.class);
					urlManager.loadURL(extRes.getUri());
				}
			});
		}
		return open;
	}

	/**
	 * @return Returns the change menu item.
	 */
	private JMenuItem getChange() {
		final ProgressUtilities progUtil = new ProgressUtilities();
		if (change == null) {
			change = new JMenuItem(TextUtils.getText("ExternalImage_popupMenu_Change"));
			change.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					final ExternalResource extRes = (ExternalResource) viewer.createExtension(node);
					if (extRes != null) {
						URI uri = extRes.getAbsoluteUri(node.getMap());
						if (progUtil.hasExternalResource(node) && !progUtil.hasExtendedProgressIcon(node)) {
							viewer.undoableDeactivateHook(node);
							viewer.paste(uri, node, node.isLeft());
						}
					}
				}
			});
		}
		return change;
	}

	/**
	 * @return Returns the reset zoom menu item
	 */
	private JMenuItem getResetZoom() {
		resetZoom = new JMenuItem(TextUtils.getText("ExternalImage_popupMenu_ResetZoom"));
		final ExternalResource extRes = node.getExtension(ExternalResource.class);
		if ((extRes != null) && (extRes.getZoom() != 1.0f)) {
			resetZoom.setEnabled(true);
		}
		else {
			resetZoom.setEnabled(false);
		}
		resetZoom.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				viewer.setZoom(Controller.getCurrentController().getModeController(), node.getMap(), extRes, 1f);
			}
		});
		return resetZoom;
	}

	/**
	 * Builds the menu.
	 */
	private void make() {
		final ProgressUtilities progUtil = new ProgressUtilities();
		if (progUtil.hasExtendedProgressIcon(node)) {
			removeAll();
			add(getOpen());
			add(getResetZoom());
		}
		else {
			removeAll();
			add(getRemove());
			add(getChange());
			add(getOpen());
			add(getResetZoom());
		}
	}

	protected void maybeShowPopup(final MouseEvent e) {
		MainView mv = null;
		if (e.isPopupTrigger()) {
			for (final Component cmp : e.getComponent().getParent().getComponents()) {
				if (cmp instanceof MainView) {
					mv = (MainView) cmp;
					node = mv.getNodeView().getModel();
					viewer = (Controller.getCurrentController().getModeController().getExtension(
					    ViewerController.class));
					break;
				}
			}
			make();
			show(e.getComponent(), e.getX(), e.getY());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(final MouseEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(final MouseEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(final MouseEvent e) {
	}

	public void mousePressed(final MouseEvent e) {
		maybeShowPopup(e);
	}

	public void mouseReleased(final MouseEvent e) {
		maybeShowPopup(e);
	}

	@Override
	public void setVisible(final boolean visible) {
		super.setVisible(visible);
	}
}
