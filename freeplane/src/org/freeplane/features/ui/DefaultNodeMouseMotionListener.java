package org.freeplane.features.ui;

import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.ui.ControllerPopupMenuListener;
import org.freeplane.core.ui.INodeMouseMotionListener;
import org.freeplane.core.util.Tools;
import org.freeplane.features.common.link.LinkController;
import org.freeplane.view.swing.map.MainView;
import org.freeplane.view.swing.map.NodeView;

/**
 * The MouseMotionListener which belongs to every NodeView
 */
public class DefaultNodeMouseMotionListener implements INodeMouseMotionListener {
	protected class TimeDelayedSelection extends TimerTask {
		final private ModeController c;
		final private MouseEvent e;

		TimeDelayedSelection(final ModeController c, final MouseEvent e) {
			this.c = c;
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
					if (e.getModifiers() == 0 && !c.isBlocked()
					        && Controller.getController().getMapView().getSelection().size() <= 1) {
						c.getUserInputListenerFactory().extendSelection(e);
					}
				}
			});
		}
	}

	/** overwritten by property delayed_selection_enabled */
	private static Tools.BooleanHolder delayedSelectionEnabled;
	/** time in ms, overwritten by property time_for_delayed_selection */
	private static Tools.IntHolder timeForDelayedSelection;
	/**
	 * The mouse has to stay in this region to enable the selection after a
	 * given time.
	 */
	private Rectangle controlRegionForDelayedSelection;
	final private ControllerPopupMenuListener popupListener = new ControllerPopupMenuListener(
	    Controller.getModeController());
	private Timer timerForDelayedSelection;

	public DefaultNodeMouseMotionListener() {
		if (DefaultNodeMouseMotionListener.delayedSelectionEnabled == null) {
			updateSelectionMethod();
		}
	}

	public void createTimer(final MouseEvent e) {
		stopTimerForDelayedSelection();
		/* Region to check for in the sequel. */
		controlRegionForDelayedSelection = getControlRegion(e.getPoint());
		timerForDelayedSelection = new Timer();
		timerForDelayedSelection
		    .schedule(
		        new TimeDelayedSelection(Controller.getModeController(), e),
		        /*
		         * if the new selection method is not enabled we put 0 to
		         * get direct selection.
		         */
		        (DefaultNodeMouseMotionListener.delayedSelectionEnabled.getValue()) ? DefaultNodeMouseMotionListener.timeForDelayedSelection
		            .getValue()
		                : 0);
	}

	protected Rectangle getControlRegion(final Point2D p) {
		final int side = 8;
		return new Rectangle((int) (p.getX() - side / 2), (int) (p.getY() - side / 2), side, side);
	}

	public void mouseClicked(final MouseEvent e) {
	}

	/**
	 * Invoked when a mouse button is pressed on a component and then
	 * dragged.
	 */
	public void mouseDragged(final MouseEvent e) {
		stopTimerForDelayedSelection();
		final NodeView nodeV = ((MainView) e.getComponent()).getNodeView();
		if (!Controller.getController().getMapView().isSelected(nodeV)) {
			Controller.getModeController().getUserInputListenerFactory().extendSelection(e);
		}
	}

	public void mouseEntered(final MouseEvent e) {
		if (!JOptionPane.getFrameForComponent(e.getComponent()).isFocused()) {
			return;
		}
		createTimer(e);
	}

	public void mouseExited(final MouseEvent e) {
		stopTimerForDelayedSelection();
	}

	public void mouseMoved(final MouseEvent e) {
		final MainView node = ((MainView) e.getComponent());
		final boolean isLink = (node).updateCursor(e.getX());
		if (isLink) {
			Controller.getController().getViewController().out(
			    LinkController.getController(Controller.getModeController()).getLinkShortText(
			        node.getNodeView().getModel()));
		}
		if (controlRegionForDelayedSelection != null
		        && DefaultNodeMouseMotionListener.delayedSelectionEnabled.getValue()) {
			if (!controlRegionForDelayedSelection.contains(e.getPoint())) {
				createTimer(e);
			}
		}
	}

	public void mousePressed(final MouseEvent e) {
		showPopupMenu(e);
	}

	public void mouseReleased(final MouseEvent e) {
		stopTimerForDelayedSelection();
		Controller.getModeController().getUserInputListenerFactory().extendSelection(e);
		showPopupMenu(e);
		if (e.isConsumed()) {
			return;
		}
		if (e.getModifiers() == InputEvent.BUTTON1_MASK) {
			Controller.getModeController().plainClick(e);
			e.consume();
		}
	}

	public void showPopupMenu(final MouseEvent e) {
		if (e.isPopupTrigger()) {
			final JPopupMenu popupmenu = Controller.getModeController()
			    .getUserInputListenerFactory().getNodePopupMenu();
			if (popupmenu != null) {
				popupmenu.addPopupMenuListener(popupListener);
				popupmenu.show(e.getComponent(), e.getX(), e.getY());
				e.consume();
			}
		}
	}

	protected void stopTimerForDelayedSelection() {
		if (timerForDelayedSelection != null) {
			timerForDelayedSelection.cancel();
		}
		timerForDelayedSelection = null;
		controlRegionForDelayedSelection = null;
	}

	/**
	 * And a static method to reread this holder. This is used when the
	 * selection method is changed via the option menu.
	 */
	public void updateSelectionMethod() {
		if (DefaultNodeMouseMotionListener.timeForDelayedSelection == null) {
			DefaultNodeMouseMotionListener.timeForDelayedSelection = new Tools.IntHolder();
		}
		DefaultNodeMouseMotionListener.delayedSelectionEnabled = new Tools.BooleanHolder();
		DefaultNodeMouseMotionListener.delayedSelectionEnabled.setValue(Controller
		    .getResourceController().getProperty("selection_method").equals(
		        "selection_method_direct") ? false : true);
		/*
		 * set time for delay to infinity, if selection_method equals
		 * selection_method_by_click.
		 */
		if (Controller.getResourceController().getProperty("selection_method").equals(
		    "selection_method_by_click")) {
			DefaultNodeMouseMotionListener.timeForDelayedSelection.setValue(Integer.MAX_VALUE);
		}
		else {
			DefaultNodeMouseMotionListener.timeForDelayedSelection.setValue(Integer
			    .parseInt(Controller.getResourceController().getProperty(
			        "time_for_delayed_selection")));
		}
	}
}
