package org.freeplane.view.swing.ui;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.net.URI;

import javax.swing.JPopupMenu;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.DoubleClickTimer;
import org.freeplane.core.ui.IMouseListener;
import org.freeplane.core.ui.components.AutoHide;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.icon.IconController;
import org.freeplane.features.icon.UIIcon;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.map.FoldingController;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.url.UrlManager;
import org.freeplane.view.swing.map.MainView;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.MouseArea;
import org.freeplane.view.swing.map.NodeView;

/**
 * The MouseMotionListener which belongs to every NodeView
 */
public class DefaultNodeMouseMotionListener implements IMouseListener {
	protected final NodeSelector nodeSelector;
	private static final String FOLD_ON_CLICK_INSIDE = "fold_on_click_inside";
	/**
	 * The mouse has to stay in this region to enable the selection after a
	 * given time.
	 */
	protected final DoubleClickTimer doubleClickTimer;

	public DefaultNodeMouseMotionListener() {
//		mc = modeController;
		doubleClickTimer = new DoubleClickTimer();
		nodeSelector = new NodeSelector();
	}


	protected boolean isInFoldingRegion(MouseEvent e) {
		return ((MainView)e.getComponent()).isInFoldingRegion(e.getPoint());
	}

	protected boolean isInDragRegion(MouseEvent e) {
		return ((MainView)e.getComponent()).isInDragRegion(e.getPoint());
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
		final boolean inside = nodeSelector.isInside(e);
		final MapController mapController = mc.getMapController();
		if(e.getButton() == 1){
			if(plainEvent){
				UIIcon uiIcon = component.getUIIconAt(e.getPoint());
				if(uiIcon != null){
					final IconController iconController = mc.getExtension(IconController.class);
					if(iconController.onIconClicked(node, uiIcon))
						return;
				}
				else if (component.isClickableLink(e.getX())) {
					LinkController.getController(mc).loadURL(node, e);
					e.consume();
					return;
				}
				
				
				final String link = component.getLink(e.getPoint());
				if (link != null) {
					doubleClickTimer.start(new Runnable() {
						public void run() {
							loadLink(node, link);
						}
					});
					e.consume();
					return;
				}

				if(inside && e.getClickCount() == 1 && ResourceController.getResourceController().getBooleanProperty(FOLD_ON_CLICK_INSIDE)){
					final boolean fold = !isFoldedOnCurrentView(node);
					if (!nodeSelector.shouldSelectOnClick(e)) {
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
		        && !nodeSelector.shouldSelectOnClick(e)) {
			final boolean fold = ! isFoldedOnCurrentView(node);
			doubleClickTimer.cancel();
			mapController.setFolded(node, fold);
			e.consume();
			return;
		}
		if(inside && e.getButton() == 1 &&  ! e.isAltDown())
			nodeSelector.extendSelection(e);
	}
	private boolean isFoldedOnCurrentView(final NodeModel node) {
		return Controller.getCurrentController().getMapViewManager().isFoldedOnCurrentView(node);
	}


	private void loadLink(NodeModel node, final String link) {
		try {
			LinkController.getController().loadURI(node, new URI(link));
		} catch (Exception ex) {
			LogUtils.warn(ex);
		}
	}

	/**
	 * Invoked when a mouse button is pressed on a component and then
	 * dragged.
	 */
	public void mouseDragged(final MouseEvent e) {
		if (!nodeSelector.isInside(e))
			return;
		nodeSelector.stopTimerForDelayedSelection();
		nodeSelector.selectSingleNode(e);
	}

	public void mouseEntered(final MouseEvent e) {
		if (nodeSelector.isRelevant(e)) {
			nodeSelector.createTimer(e);
			mouseMoved(e);
		}
	}

	public void mouseExited(final MouseEvent e) {
		nodeSelector.stopTimerForDelayedSelection();
		final MainView v = (MainView) e.getSource();
		v.setMouseArea(MouseArea.OUT);
		nodeSelector.trackWindowForComponent(v);
	}

	public void mouseMoved(final MouseEvent e) {
		if (!nodeSelector.isRelevant(e))
			return;
		final MainView node = ((MainView) e.getComponent());
		String link = node.getLink(e.getPoint());
		boolean followLink = link != null;
		Controller currentController = Controller.getCurrentController();
        if(! followLink){
        	followLink = node.isClickableLink(e.getX());
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
		nodeSelector.createTimer(e);
	}

	public void mousePressed(final MouseEvent e) {
		final MapView mapView = MapView.getMapView(e.getComponent());
		mapView.select();
		doubleClickTimer.cancel();
		showPopupMenu(e);
	}

	public void mouseReleased(final MouseEvent e) {
		nodeSelector.stopTimerForDelayedSelection();
		showPopupMenu(e);
	}

	public void showPopupMenu(final MouseEvent e) {
		if (! e.isPopupTrigger())
			return;
		final boolean inside = nodeSelector.isInside(e);
		final boolean inFoldingRegion = ! inside && isInFoldingRegion(e);
		if (inside || inFoldingRegion) {
			if(inside){
				nodeSelector.stopTimerForDelayedSelection();
				new NodePopupMenuDisplayer().showNodePopupMenu(e);
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
		final NodeView nodeView = nodeSelector.getRelatedNodeView(e);
		final JPopupMenu popupmenu = foldingController.createFoldingPopupMenu(nodeView.getModel());
		AutoHide.start(popupmenu);
		new NodePopupMenuDisplayer().showMenuAndConsumeEvent(popupmenu, e);
    }

}
