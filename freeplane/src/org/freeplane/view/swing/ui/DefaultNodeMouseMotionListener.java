package org.freeplane.view.swing.ui;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.FocusManager;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.ControllerPopupMenuListener;
import org.freeplane.core.ui.IMouseListener;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.SysUtils;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.NodeLinks;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.url.UrlManager;
import org.freeplane.view.swing.map.MainView;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.MouseArea;
import org.freeplane.view.swing.map.NodeView;
import org.freeplane.view.swing.map.NodeViewLayoutAdapter;

import com.thebuzzmedia.imgscalr.Scalr;
import com.thebuzzmedia.imgscalr.Scalr.Rotation;

/**
 * The MouseMotionListener which belongs to every NodeView
 */
public class DefaultNodeMouseMotionListener implements IMouseListener {
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
	                    Controller currentController = Controller.getCurrentController();
						if (!currentController.getModeController().isBlocked() && currentController.getSelection().size() <= 1) {
							extendSelection(e);
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
	private boolean wasFocused;

	public DefaultNodeMouseMotionListener() {
//		mc = modeController;
		popupListener = new ControllerPopupMenuListener();
	}

	private void createTimer(final MouseEvent e) {
		if(isInFoldingRegion(e))
			return;
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

	protected Rectangle getControlRegion(final Point2D p) {
		final int side = 8;
		return new Rectangle((int) (p.getX() - side / 2), (int) (p.getY() - side / 2), side, side);
	}

	public void mouseClicked(final MouseEvent e) {
		ModeController mc = Controller.getCurrentController().getModeController();
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

		if(e.getButton() == 1){
			if(Compat.isPlainEvent(e)){
				if (component.isInFollowLinkRegion(e.getX())) {
					LinkController.getController(mc).loadURL(nodeView.getModel(), e);
					return;
				}

				final String link = component.getLink(e.getPoint());
				if (link != null) {
					loadLink(link);
					return;
				}
			}
		}
		if (isInFoldingRegion(e)) {
			/* perform action only if one selected node. */
			final MapController mapController = mc.getMapController();
			mapController.toggleFolded(nodeView.getModel());
			e.consume();
			return;
		}
		extendSelection(e);
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
		if (!((MapView) Controller.getCurrentController().getViewController().getMapView()).isSelected(nodeV)) {
			extendSelection(e);
		}
	}

	public void mouseEntered(final MouseEvent e) {
		createTimer(e);
		mouseMoved(e);
	}

	public void mouseExited(final MouseEvent e) {
		stopTimerForDelayedSelection();
		final MainView v = (MainView) e.getSource();
		v.setMouseArea(MouseArea.DEFAULT);
	}

	public void mouseMoved(final MouseEvent e) {
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
        else if (node.isInRightFoldingRegion(e.getPoint())){
        	requiredCursor = rightFoldingCursor();
        	node.setMouseArea(MouseArea.FOLDING);
        }
        else if (node.isInLeftFoldingRegion(e.getPoint())){
        	requiredCursor = leftFoldingCursor();
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

	static private Cursor rightFoldingCursor = null;
	private Cursor rightFoldingCursor() {
		if(rightFoldingCursor == null){
			rightFoldingCursor = createFoldingCursor("right");
		}
		return rightFoldingCursor;
	}

	static private Cursor leftFoldingCursor = null;
	private Cursor leftFoldingCursor() {
		if(leftFoldingCursor == null){
			leftFoldingCursor = createFoldingCursor("left");
		}
		return leftFoldingCursor;
	}

	private Cursor createFoldingCursor(String name) {
		Cursor cursor;
		try {
			final URL resource = ResourceController.getResourceController()
					.getResource("/images/" + name + "-fold-unfold32.gif");
			final Toolkit toolkit = Toolkit.getDefaultToolkit();
			BufferedImage resourceImage = ImageIO.read(resource);
			final Dimension bestCursorSize = toolkit.getBestCursorSize(32, 32);
			cursor = toolkit.createCustomCursor(
					resourceImage,
					new Point(bestCursorSize.width / 2,
							bestCursorSize.height / 2), name + "FoldingCursor");
		} catch (Exception e) {
			LogUtils.severe(e);
			cursor = Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
			
		}
		return cursor;
	}

	public void mousePressed(final MouseEvent e) {
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
		if (isInside(e) 
				&& e.isPopupTrigger()) {
			stopTimerForDelayedSelection();
			final MainView component = (MainView) e.getComponent();
			final NodeView nodeView = component.getNodeView();
			if(! nodeView.isSelected()){
				Controller.getCurrentController().getSelection().selectAsTheOnlyOneSelected(nodeView.getModel());
			}
			ModeController mc = Controller.getCurrentController().getModeController();
			final JPopupMenu popupmenu = mc.getUserInputListenerFactory().getNodePopupMenu();
			if (popupmenu != null) {
				popupmenu.addHierarchyListener(popupListener);
				popupmenu.show(e.getComponent(), e.getX(), e.getY());
				e.consume();
			}
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

	public boolean extendSelection(final MouseEvent e) {
		final Controller controller = Controller.getCurrentController();
		final MainView mainView = (MainView) e.getComponent();
        final NodeModel newlySelectedNodeView = mainView.getNodeView().getModel();
		final boolean extend = Compat.isMacOsX() ? e.isMetaDown() : e.isControlDown();
		final boolean range = e.isShiftDown();
		/* windows alt, linux altgraph .... */
		boolean retValue = false;
		if (extend || range 
		        || !controller.getSelection().isSelected(newlySelectedNodeView) 
		        || ! (FocusManager.getCurrentManager().getFocusOwner() instanceof MainView)) {
			if (!range) {
				if (extend) {
					controller.getSelection().toggleSelected(newlySelectedNodeView);
				}
				else {
					controller.getSelection().selectAsTheOnlyOneSelected(newlySelectedNodeView);
				}
				retValue = true;
			}
			else {
				controller.getSelection().selectContinuous(newlySelectedNodeView);
				retValue = true;
			}
		}
		if (retValue) {
			e.consume();
		}
		return retValue;
	}
}
