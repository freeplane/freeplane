package org.freeplane.view.swing.ui;

import java.awt.Cursor;
import java.awt.Frame;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.net.URI;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.FocusManager;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.ControllerPopupMenuListener;
import org.freeplane.core.ui.DoubleClickTimer;
import org.freeplane.core.ui.IMouseListener;
import org.freeplane.core.ui.components.AutoHide;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.SysUtils;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.map.FoldingController;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.url.UrlManager;
import org.freeplane.view.swing.map.FoldingMark;
import org.freeplane.view.swing.map.MainView;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.MouseArea;
import org.freeplane.view.swing.map.NodeView;

/**
 * The MouseMotionListener which belongs to every NodeView
 */
public class DefaultNodeMouseMotionListener implements IMouseListener {
	private static final String FOLD_ON_CLICK_INSIDE = "fold_on_click_inside";
	private static final String SELECTION_METHOD_DIRECT = "selection_method_direct";
	private static final String SELECTION_METHOD_BY_CLICK = "selection_method_by_click";
	private static final String TIME_FOR_DELAYED_SELECTION = "time_for_delayed_selection";
	private static final String SELECTION_METHOD = "selection_method";

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
					if (e.getModifiers() != 0){
						return;
					}
					try {
	                    Controller controller = Controller.getCurrentController();
						if (!controller.getModeController().isBlocked()&& controller.getSelection().size() <= 1) {
							final NodeView nodeV = ((MainView) e.getComponent()).getNodeView();
							if(nodeV.isDisplayable() && nodeV.getModel().isVisible()) {
								nodeV.getMap().select();
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

	/**
	 * The mouse has to stay in this region to enable the selection after a
	 * given time.
	 */
	private Rectangle controlRegionForDelayedSelection;
// 	final private ModeController mc;
	final private ControllerPopupMenuListener popupListener;
	private Timer timerForDelayedSelection;
	protected final DoubleClickTimer doubleClickTimer;
	private boolean wasFocused;
	private MovedMouseEventFilter windowMouseTracker;

	public DefaultNodeMouseMotionListener() {
//		mc = modeController;
		popupListener = new ControllerPopupMenuListener();
		doubleClickTimer = new DoubleClickTimer();
		windowMouseTracker = new MovedMouseEventFilter();
	}

	private void createTimer(final MouseEvent e) {
		if(! isInside(e))
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

	protected boolean isInFoldingRegion(MouseEvent e) {
		return ((MainView)e.getComponent()).isInFoldingRegion(e.getPoint());
	}

	protected boolean isInDragRegion(MouseEvent e) {
		return ((MainView)e.getComponent()).isInDragRegion(e.getPoint());
	}

	protected Rectangle getControlRegion(final Point2D p) {
		final int side = 8;
		return new Rectangle((int) (p.getX() - side / 2), (int) (p.getY() - side / 2), side, side);
	}

	public void mouseClicked(final MouseEvent e) {
		final ModeController mc = Controller.getCurrentController().getModeController();
		if(Compat.isMacOsX()){
			final JPopupMenu popupmenu = mc.getUserInputListenerFactory().getNodePopupMenu();
			if(popupmenu.isShowing()){
				return;
			}
		}
		final MainView component = (MainView) e.getComponent();
		NodeView nodeView = component.getNodeView();
		if (nodeView == null)
			return;

		final NodeModel node = nodeView.getModel();
		final boolean plainEvent = Compat.isPlainEvent(e);
		final boolean inside = isInside(e);
		final MapController mapController = mc.getMapController();
		if(e.getButton() == 1){
			if(plainEvent){
				if (component.isInFollowLinkRegion(e.getX())) {
					LinkController.getController(mc).loadURL(node, e);
					e.consume();
					return;
				}

				final String link = component.getLink(e.getPoint());
				if (link != null) {
					doubleClickTimer.start(new Runnable() {
						public void run() {
							loadLink(link);
						}
					});
					e.consume();
					return;
				}
				
				if(inside && e.getClickCount() == 1 && ResourceController.getResourceController().getBooleanProperty(FOLD_ON_CLICK_INSIDE)){
					final boolean fold = FoldingMark.UNFOLDED.equals(component.foldingMarkType(mapController, node)) && ! mapController.hasHiddenChildren(node);
					if(!shouldSelectOnClick(e)){
						doubleClickTimer.start(new Runnable() {
							public void run() {
								mapController.setFolded(node, fold);
							}
						});
					}
				}
			}
			else if(Compat.isShiftEvent(e)){
				if (isInFoldingRegion(e)) {
					if (! mapController.showNextChild(node))
						mapController.setFolded(node, true);
					e.consume();
				}
			}
		}
		final boolean inFoldingRegion = isInFoldingRegion(e);
		if ((plainEvent && inFoldingRegion 
				|| (inFoldingRegion || inside) && Compat.isCtrlShiftEvent(e)) 
				&& !shouldSelectOnClick(e)) {
			boolean fold = FoldingMark.UNFOLDED.equals(component.foldingMarkType(mapController, node)) && ! mapController.hasHiddenChildren(node);
			doubleClickTimer.cancel();
			mapController.setFolded(node, fold);
			e.consume();
			return;
		}
		if(inside && e.getButton() == 1 &&  ! e.isAltDown())
			extendSelection(e);
	}


	private boolean shouldSelectOnClick(MouseEvent e) {
		if(isInside(e)){
			final MainView component = (MainView) e.getComponent();
			NodeView nodeView = component.getNodeView();
			return !nodeView.isSelected() || Controller.getCurrentController().getSelection().size() != 1;
		}
	    return false;
    }

	protected void loadLink(final String link) {
		try {
			UrlManager.getController().loadURL(new URI(link));
		} catch (Exception ex) {
			LogUtils.warn(ex);
		}
	}

	/**
	 * Invoked when a mouse button is pressed on a component and then
	 * dragged.
	 */
	public void mouseDragged(final MouseEvent e) {
		if(! isInside(e))
			return;
		stopTimerForDelayedSelection();
		final NodeView nodeV = ((MainView) e.getComponent()).getNodeView();
		final Controller controller = Controller.getCurrentController();
		if (!((MapView) controller.getMapViewManager().getMapViewComponent()).isSelected(nodeV)) {
			controller.getSelection().selectAsTheOnlyOneSelected(nodeV.getModel());
		}
	}

	public void mouseEntered(final MouseEvent e) {
		if(windowMouseTracker.isRelevant(e)){
			createTimer(e);
			mouseMoved(e);
		}
	}

	public void mouseExited(final MouseEvent e) {
		stopTimerForDelayedSelection();
		final MainView v = (MainView) e.getSource();
		v.setMouseArea(MouseArea.OUT);
		windowMouseTracker.trackWindowForComponent(v);
	}

	public void mouseMoved(final MouseEvent e) {
		if(! windowMouseTracker.isRelevant(e))
			return;
		final MainView node = ((MainView) e.getComponent());
		String link = node.getLink(e.getPoint());
		boolean followLink = link != null;
		Controller currentController = Controller.getCurrentController();
        if(! followLink){
        	followLink = node.isInFollowLinkRegion(e.getX());
        	if(followLink){
				link = LinkController.getController(currentController.getModeController()).getLinkShortText(node.getNodeView().getModel());
        	}
        }
        final Cursor requiredCursor;
        if(followLink){
			currentController.getViewController().out(link);
			requiredCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
			node.setMouseArea(MouseArea.LINK);
        }
        else if (isInFoldingRegion(e)){
        	requiredCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
        	node.setMouseArea(MouseArea.FOLDING);
        }
        else{
        	requiredCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
        	node.setMouseArea(MouseArea.DEFAULT);
        }
        if (node.getCursor().getType() != requiredCursor.getType() || requiredCursor.getType() == Cursor.CUSTOM_CURSOR && node.getCursor() != requiredCursor) {
        	node.setCursor(requiredCursor);
        }
		if (controlRegionForDelayedSelection == null 
				|| !controlRegionForDelayedSelection.contains(e.getPoint())) {
				createTimer(e);
		}
	}

	public void mousePressed(final MouseEvent e) {
		final MapView mapView = MapView.getMapView(e.getComponent());
		mapView.select();
		doubleClickTimer.cancel();
		final MainView component = (MainView) e.getComponent();
		wasFocused = component.hasFocus();
		showPopupMenu(e);
	}

	public boolean wasFocused() {
    	return wasFocused;
    }

	public void mouseReleased(final MouseEvent e) {
		stopTimerForDelayedSelection();
		showPopupMenu(e);
	}

	public void showPopupMenu(final MouseEvent e) {
		if (! e.isPopupTrigger())
			return;
		final boolean inside = isInside(e);
		final boolean inFoldingRegion = ! inside && isInFoldingRegion(e);
		if (inside || inFoldingRegion) {
			if(inside){
				stopTimerForDelayedSelection();
				ModeController mc = Controller.getCurrentController().getModeController();
				final MainView component = (MainView) e.getComponent();
				final NodeView nodeView = component.getNodeView();
				if(! nodeView.isSelected()){
					Controller.getCurrentController().getSelection().selectAsTheOnlyOneSelected(nodeView.getModel());
				}
				final JPopupMenu popupmenu = mc.getUserInputListenerFactory().getNodePopupMenu();
				showMenuAndConsumeEvent(popupmenu, e);
			}
			else if(inFoldingRegion){
				showFoldingPopup(e);
			}
		}
	}

	private void showFoldingPopup(MouseEvent e) {
		ModeController mc = Controller.getCurrentController().getModeController();
		final FoldingController foldingController = mc.getExtension(FoldingController.class);
		if(foldingController == null)
			return;
		final MainView component = (MainView) e.getComponent();
		final NodeView nodeView = component.getNodeView();
		final JPopupMenu popupmenu = foldingController.createFoldingPopupMenu(nodeView.getModel());
		AutoHide.start(popupmenu);
		showMenuAndConsumeEvent(popupmenu, e);
    }

	private void showMenuAndConsumeEvent(final JPopupMenu popupmenu, final MouseEvent e) {
	    if (popupmenu != null) {
	    	popupmenu.addHierarchyListener(popupListener);
	    	popupmenu.show(e.getComponent(), e.getX(), e.getY());
	    	e.consume();
	    }
    }

	protected boolean isInside(final MouseEvent e) {
		return new Rectangle(0, 0, e.getComponent().getWidth(), e.getComponent().getHeight()).contains(e.getPoint());
	}

	protected void stopTimerForDelayedSelection() {
		if (timerForDelayedSelection != null) {
			timerForDelayedSelection.cancel();
		}
		timerForDelayedSelection = null;
		controlRegionForDelayedSelection = null;
	}

	private void extendSelection(final MouseEvent e) {
		final Controller controller = Controller.getCurrentController();
		final MainView mainView = (MainView) e.getComponent();
		final NodeModel newlySelectedNode = mainView.getNodeView().getModel();
		final boolean extend = Compat.isMacOsX() ? e.isMetaDown() : e.isControlDown();
		final boolean range = e.isShiftDown();
		final IMapSelection selection = controller.getSelection();
		if (range && !extend) {
			selection.selectContinuous(newlySelectedNode);
		}
		else if (extend && !range) {
			selection.toggleSelected(newlySelectedNode);
		}
		if(extend == range){
			if (selection.isSelected(newlySelectedNode) && selection.size() == 1 
			        && FocusManager.getCurrentManager().getFocusOwner() instanceof MainView)
				return;
			else {
				selection.selectAsTheOnlyOneSelected(newlySelectedNode);
			}
			e.consume();
		}
	}
}
