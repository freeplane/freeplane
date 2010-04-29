package org.freeplane.view.swing.ui;

import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import org.freeplane.core.modecontroller.MapController;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.ControllerPopupMenuListener;
import org.freeplane.core.ui.INodeMouseMotionListener;
import org.freeplane.core.util.SysUtil;
import org.freeplane.features.common.link.LinkController;
import org.freeplane.features.common.link.NodeLinks;
import org.freeplane.view.swing.map.MainView;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.NodeView;

/**
 * The MouseMotionListener which belongs to every NodeView
 */
public class DefaultNodeMouseMotionListener implements INodeMouseMotionListener {
	private static final String SELECTION_METHOD_DIRECT = "selection_method_direct";
	private static final String SELECTION_METHOD_BY_CLICK = "selection_method_by_click";
	private static final String TIME_FOR_DELAYED_SELECTION = "time_for_delayed_selection";
	private static final String SELECTION_METHOD = "selection_method";

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
					if (e.getModifiers() == 0 && !c.isBlocked() && c.getController().getSelection().size() <= 1) {
						c.getUserInputListenerFactory().extendSelection(e);
					}
				}
			});
		}
	}

	/**
	 * The mouse has to stay in this region to enable the selection after a
	 * given time.
	 */
	private Rectangle controlRegionForDelayedSelection;
	final private ModeController mc;
	final private ControllerPopupMenuListener popupListener;
	private Timer timerForDelayedSelection;

	public DefaultNodeMouseMotionListener(final ModeController modeController) {
		mc = modeController;
		popupListener = new ControllerPopupMenuListener(modeController);
	}

	private void createTimer(final MouseEvent e) {
		stopTimerForDelayedSelection();
		if (!JOptionPane.getFrameForComponent(e.getComponent()).isFocused()) {
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
			new TimeDelayedSelection(mc, e).run();
			return;
		}
		final int timeForDelayedSelection = ResourceController.getResourceController().getIntProperty(
		    TIME_FOR_DELAYED_SELECTION, 0);
		timerForDelayedSelection = SysUtil.createTimer(getClass().getSimpleName());
		timerForDelayedSelection.schedule(new TimeDelayedSelection(mc, e), timeForDelayedSelection);
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
		if (!((MapView) mc.getController().getViewController().getMapView()).isSelected(nodeV)) {
			mc.getUserInputListenerFactory().extendSelection(e);
		}
	}

	public void mouseEntered(final MouseEvent e) {
		createTimer(e);
	}

	public void mouseExited(final MouseEvent e) {
		stopTimerForDelayedSelection();
	}

	public void mouseMoved(final MouseEvent e) {
		final MainView node = ((MainView) e.getComponent());
		final boolean isLink = (node).updateCursor(e.getX());
		if (isLink) {
			mc.getController().getViewController().out(
			    LinkController.getController(mc).getLinkShortText(node.getNodeView().getModel()));
		}
		if (controlRegionForDelayedSelection != null) {
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
		mc.getUserInputListenerFactory().extendSelection(e);
		showPopupMenu(e);
		if (e.isConsumed()) {
			return;
		}
		if (e.getModifiers() == InputEvent.BUTTON1_MASK) {
			/* perform action only if one selected node. */
			final MapController mapController = mc.getMapController();
			if (mapController.getSelectedNodes().size() != 1) {
				return;
			}
			final MainView component = (MainView) e.getComponent();
			if (component.isInFollowLinkRegion(e.getX())) {
				LinkController.getController(mc).loadURL();
			}
			else {
				final NodeModel node = (component).getNodeView().getModel();
				if (!mapController.hasChildren(node)) {
					/* If the link exists, follow the link; toggle folded otherwise */
					if (NodeLinks.getValidLink(mapController.getSelectedNode()) == null) {
						mapController.toggleFolded();
					}
					else {
						LinkController.getController(mc).loadURL();
					}
					return;
				}
				mapController.toggleFolded(mapController.getSelectedNodes().listIterator());
			}
			e.consume();
		}
	}

	public void showPopupMenu(final MouseEvent e) {
		if (e.isPopupTrigger()) {
			final JPopupMenu popupmenu = mc.getUserInputListenerFactory().getNodePopupMenu();
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
}
