/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2013 Dimitry
 *
 *  This file author is Dimitry
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
package org.freeplane.view.swing.ui;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.FocusManager;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.SysUtils;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.view.swing.map.MainView;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.NodeView;

/**
 * @author Dimitry Polivaev
 * 19.06.2013
 */
public class NodeSelector {
	private static final String SELECTION_METHOD_DIRECT = "selection_method_direct";
	private static final String SELECTION_METHOD_BY_CLICK = "selection_method_by_click";
	private static final String TIME_FOR_DELAYED_SELECTION = "time_for_delayed_selection";
	private static final String SELECTION_METHOD = "selection_method";
	private final MovedMouseEventFilter windowMouseTracker = new MovedMouseEventFilter();

	protected class TimeDelayedSelection extends TimerTask {
		final private MouseEvent e;

		TimeDelayedSelection(final MouseEvent e) {
			this.e = e;
		}

		/** TimerTask method to enable the selection after a given time. */
		@Override
		public void run() {
			/*
			 * formerly in ControllerAdapter. To guarantee, that
			 * point-to-select does not change selection if any meta key is
			 * pressed.
			 */
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					if (e.getModifiers() != 0) {
						return;
					}
					try {
						Controller controller = Controller.getCurrentController();
						if (!controller.getModeController().isBlocked() && controller.getSelection().size() <= 1) {
							final NodeView nodeV = (NodeView) SwingUtilities.getAncestorOfClass(NodeView.class,
							    e.getComponent());
							MapView map = nodeV.getMap();
							if (nodeV.isDisplayable() && nodeV.getModel().hasVisibleContent(map.getFilter())) {
                                map.select();
								controller.getSelection().selectAsTheOnlyOneSelected(nodeV.getModel());
							}
						}
					}
					catch (NullPointerException e) {
					}
				}
			});
		}
	}

	private Rectangle controlRegionForDelayedSelection;
	private Timer timerForDelayedSelection;

	public void createTimer(final MouseEvent e) {
		if (controlRegionForDelayedSelection != null && controlRegionForDelayedSelection.contains(e.getPoint())) {
			return;
		}
		if (!isInside(e))
			return;
		stopTimerForDelayedSelection();
		Window focusedWindow = FocusManager.getCurrentManager().getFocusedWindow();
		if (focusedWindow == null) {
			return;
		}
		if (KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner() instanceof JTextComponent) {
			return;
		}
		/* Region to check for in the sequel. */
		controlRegionForDelayedSelection = getControlRegion(e.getPoint());
		final String selectionMethod = ResourceController.getResourceController().getProperty(SELECTION_METHOD);
		if (selectionMethod.equals(SELECTION_METHOD_BY_CLICK)) {
			return;
		}
		if (selectionMethod.equals(SELECTION_METHOD_DIRECT)) {
			new TimeDelayedSelection(e).run();
			return;
		}
		final int timeForDelayedSelection = ResourceController.getResourceController().getIntProperty(
		    TIME_FOR_DELAYED_SELECTION, 0);
		timerForDelayedSelection = SysUtils.createTimer(getClass().getSimpleName());
		timerForDelayedSelection.schedule(new TimeDelayedSelection(e), timeForDelayedSelection);
	}

	protected boolean isInside(final MouseEvent e) {
		return new Rectangle(0, 0, e.getComponent().getWidth(), e.getComponent().getHeight())
		    .contains(e.getPoint());
	}

	public void stopTimerForDelayedSelection() {
		if (timerForDelayedSelection != null) {
			timerForDelayedSelection.cancel();
		}
		timerForDelayedSelection = null;
		controlRegionForDelayedSelection = null;
	}

	protected Rectangle getControlRegion(final Point2D p) {
		final int side = 8;
		return new Rectangle((int) (p.getX() - side / 2), (int) (p.getY() - side / 2), side, side);
	}

	public boolean shouldSelectOnClick(MouseEvent e) {
		if (isInside(e)) {
			final NodeView nodeView = getRelatedNodeView(e);
			return !nodeView.isSelected() || Controller.getCurrentController().getSelection().size() != 1;
		}
		return false;
	}

	public void extendSelection(final MouseEvent e) {
		final Controller controller = Controller.getCurrentController();
		final NodeView nodeView = getRelatedNodeView(e);
		final NodeModel newlySelectedNode = nodeView.getModel();
		final boolean extend = Compat.isMacOsX() ? e.isMetaDown() : e.isControlDown();
		final boolean range = e.isShiftDown();
		final IMapSelection selection = controller.getSelection();
		if (range && !extend) {
			selection.selectContinuous(newlySelectedNode);
		}
		else if (extend && !range) {
			selection.toggleSelected(newlySelectedNode);
		}
		if (extend == range) {
			if (selection.isSelected(newlySelectedNode) && selection.size() == 1
			        && FocusManager.getCurrentManager().getFocusOwner() instanceof MainView)
				return;
			else {
				selection.selectAsTheOnlyOneSelected(newlySelectedNode);
			}
			e.consume();
		}
	}

	public void selectSingleNode(MouseEvent e) {
		final NodeView nodeV = getRelatedNodeView(e);
		final Controller controller = Controller.getCurrentController();
		if (!((MapView) controller.getMapViewManager().getMapViewComponent()).isSelected(nodeV)) {
			controller.getSelection().selectAsTheOnlyOneSelected(nodeV.getModel());
		}
	}

	public NodeView getRelatedNodeView(MouseEvent e) {
		return (NodeView) SwingUtilities.getAncestorOfClass(NodeView.class, e.getComponent());
	}

	public boolean isRelevant(MouseEvent e) {
		return windowMouseTracker.isRelevant(e);
	}

	public void trackWindowForComponent(Component c) {
		windowMouseTracker.trackWindowForComponent(c);
	}
}