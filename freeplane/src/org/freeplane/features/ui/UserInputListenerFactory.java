/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
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
package org.freeplane.features.ui;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Rectangle;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ButtonGroup;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.frame.MapViewManager;
import org.freeplane.core.frame.ViewController;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.ui.IFreeplanePropertyListener;
import org.freeplane.core.ui.ControllerPopupMenuListener;
import org.freeplane.core.ui.IEditHandler;
import org.freeplane.core.ui.IMapMouseReceiver;
import org.freeplane.core.ui.IMouseListener;
import org.freeplane.core.ui.IMouseWheelEventHandler;
import org.freeplane.core.ui.INodeMouseMotionListener;
import org.freeplane.core.ui.IUserInputListenerFactory;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.UIBuilder;
import org.freeplane.core.ui.components.FreeplaneMenuBar;
import org.freeplane.core.util.Tools;
import org.freeplane.features.common.clipboard.ClipboardController;
import org.freeplane.features.common.clipboard.MindMapNodesSelection;
import org.freeplane.features.common.link.LinkController;
import org.freeplane.view.swing.map.MainView;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.NodeView;

public class UserInputListenerFactory implements IUserInputListenerFactory {
	/**
	 * @author foltin
	 */
	public static class DefaultMouseWheelListener implements MouseWheelListener {
		private static final int HORIZONTAL_SCROLL_MASK = InputEvent.SHIFT_MASK
		        | InputEvent.BUTTON1_MASK | InputEvent.BUTTON2_MASK | InputEvent.BUTTON3_MASK;
		private static int SCROLL_SKIPS = 8;
		private static final int ZOOM_MASK = InputEvent.CTRL_MASK;

		/**
		 *
		 */
		public  DefaultMouseWheelListener() {
			super();
			Controller.getResourceController().addPropertyChangeListener(
			    new IFreeplanePropertyListener() {
				    public void propertyChanged(final String propertyName, final String newValue,
				                                final String oldValue) {
					    if (propertyName.equals(ResourceController.RESOURCES_WHEEL_VELOCITY)) {
						    DefaultMouseWheelListener.SCROLL_SKIPS = Integer.parseInt(newValue);
					    }
				    }
			    });
			DefaultMouseWheelListener.SCROLL_SKIPS = Controller.getResourceController()
			    .getIntProperty(ResourceController.RESOURCES_WHEEL_VELOCITY, 8);
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * freeplane.modes.ModeController.MouseWheelEventHandler#handleMouseWheelEvent
		 * (java.awt.event.MouseWheelEvent)
		 */
		public void mouseWheelMoved(final MouseWheelEvent e) {
			final MapView mapView = (MapView) e.getSource();
			final ModeController mController = mapView.getModel().getModeController();
			if (mController.isBlocked()) {
				return;
			}
			final Set registeredMouseWheelEventHandler = mController.getUserInputListenerFactory()
			    .getMouseWheelEventHandlers();
			for (final Iterator i = registeredMouseWheelEventHandler.iterator(); i.hasNext();) {
				final IMouseWheelEventHandler handler = (IMouseWheelEventHandler) i.next();
				final boolean result = handler.handleMouseWheelEvent(e);
				if (result) {
					return;
				}
			}
			if ((e.getModifiers() & DefaultMouseWheelListener.ZOOM_MASK) != 0) {
				float newZoomFactor = 1f + Math.abs((float) e.getWheelRotation()) / 10f;
				if (e.getWheelRotation() < 0) {
					newZoomFactor = 1 / newZoomFactor;
				}
				final float oldZoom = ((MapView) e.getComponent()).getZoom();
				float newZoom = oldZoom / newZoomFactor;
				newZoom = (float) Math.rint(newZoom * 1000f) / 1000f;
				newZoom = Math.max(1f / 32f, newZoom);
				newZoom = Math.min(32f, newZoom);
				if (newZoom != oldZoom) {
					Controller.getController().getViewController().setZoom(newZoom);
				}
			}
			else if ((e.getModifiers() & DefaultMouseWheelListener.HORIZONTAL_SCROLL_MASK) != 0) {
				((MapView) e.getComponent()).scrollBy(DefaultMouseWheelListener.SCROLL_SKIPS
				        * e.getWheelRotation(), 0);
			}
			else {
				((MapView) e.getComponent()).scrollBy(0, DefaultMouseWheelListener.SCROLL_SKIPS
				        * e.getWheelRotation());
			}
		}
	}
	/**
	 * The MouseListener which belongs to MapView
	 */
	public static class DefaultMapMouseListener implements IMouseListener {
		private final IMapMouseReceiver mReceiver;

		public DefaultMapMouseListener(final IMapMouseReceiver mReceiver) {
			this.mReceiver = mReceiver;
		}

		private void handlePopup(final MouseEvent e) {
			if (e.isPopupTrigger()) {
				JPopupMenu popup = null;
				final java.lang.Object obj = Controller.getController().getMapView()
				    .detectCollision(e.getPoint());
				final ModeController modeController = Controller.getModeController();
				final JPopupMenu popupForModel = LinkController.getController(modeController)
				    .getPopupForModel(obj);
				if (popupForModel != null) {
					final ControllerPopupMenuListener popupListener = new ControllerPopupMenuListener(
					    modeController);
					popupForModel.addPopupMenuListener(popupListener);
					popup = popupForModel;
				}
				else {
					popup = modeController.getUserInputListenerFactory().getMapPopup();
				}
				popup.show(e.getComponent(), e.getX(), e.getY());
				popup.setVisible(true);
			}
		}

		public void mouseClicked(final MouseEvent e) {
			Controller.getController().getMapView().selectAsTheOnlyOneSelected(
			    Controller.getController().getMapView().getSelected());
		}

		public void mouseDragged(final MouseEvent e) {
			if (mReceiver != null) {
				mReceiver.mouseDragged(e);
			}
		}

		public void mouseEntered(final MouseEvent e) {
		}

		public void mouseExited(final MouseEvent e) {
		}

		public void mouseMoved(final MouseEvent e) {
		}

		public void mousePressed(final MouseEvent e) {
			if (e.isPopupTrigger()) {
				handlePopup(e);
			}
			else if (mReceiver != null) {
				mReceiver.mousePressed(e);
			}
			e.consume();
		}

		public void mouseReleased(final MouseEvent e) {
			if (mReceiver != null) {
				mReceiver.mouseReleased(e);
			}
			handlePopup(e);
			e.consume();
			Controller.getController().getMapView().setMoveCursor(false);
		}
	}

	static public class DefaultMapMouseReceiver implements IMapMouseReceiver {
		int originX = -1;
		int originY = -1;

		/**
		 *
		 */
		public DefaultMapMouseReceiver() {
			super();
		}

		public void mouseDragged(final MouseEvent e) {
			final Rectangle r = new Rectangle(e.getX(), e.getY(), 1, 1);
			final MapView mapView = (MapView) e.getComponent();
			final boolean isEventPointVisible = mapView.getVisibleRect().contains(r);
			if (!isEventPointVisible) {
				mapView.scrollRectToVisible(r);
			}
			if (originX >= 0 && isEventPointVisible) {
				((MapView) e.getComponent()).scrollBy(originX - e.getX(), originY - e.getY());
			}
		}

		public void mousePressed(final MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				Controller.getController().getMapView().setMoveCursor(true);
				originX = e.getX();
				originY = e.getY();
			}
		}

		public void mouseReleased(final MouseEvent e) {
			originX = -1;
			originY = -1;
		}
	}


	/**
	 * The NodeDragListener which belongs to every NodeView
	 */
	static class DefaultNodeDragListener implements DragGestureListener {
		public DefaultNodeDragListener() {
		}

		public void dragGestureRecognized(final DragGestureEvent e) {
			if (!Controller.getResourceController().getBoolProperty("draganddrop")) {
				return;
			}
			final NodeModel node = ((MainView) e.getComponent()).getNodeView().getModel();
			if (node.isRoot()) {
				return;
			}
			String dragAction = "MOVE";
			Cursor cursor = getCursorByAction(e.getDragAction());
			if ((e.getTriggerEvent().getModifiersEx() & InputEvent.BUTTON3_DOWN_MASK) != 0) {
				cursor = DragSource.DefaultLinkDrop;
				dragAction = "LINK";
			}
			if ((e.getTriggerEvent().getModifiersEx() & InputEvent.BUTTON2_DOWN_MASK) != 0) {
				cursor = DragSource.DefaultCopyDrop;
				dragAction = "COPY";
			}
			final ModeController modeController = Controller.getModeController();
			final Transferable t = ClipboardController.getController(modeController).copy(
			    Controller.getController().getSelection());
			((MindMapNodesSelection) t).setDropAction(dragAction);
			e.startDrag(cursor, t, new DragSourceListener() {
				public void dragDropEnd(final DragSourceDropEvent dsde) {
				}

				public void dragEnter(final DragSourceDragEvent e) {
				}

				public void dragExit(final DragSourceEvent dse) {
				}

				public void dragOver(final DragSourceDragEvent dsde) {
				}

				public void dropActionChanged(final DragSourceDragEvent dsde) {
					dsde.getDragSourceContext().setCursor(getCursorByAction(dsde.getUserAction()));
				}
			});
		}

		public Cursor getCursorByAction(final int dragAction) {
			switch (dragAction) {
				case DnDConstants.ACTION_COPY:
					return DragSource.DefaultCopyDrop;
				case DnDConstants.ACTION_LINK:
					return DragSource.DefaultLinkDrop;
				default:
					return DragSource.DefaultMoveDrop;
			}
		}
	}

	static public class DefaultNodeDropListener implements DropTargetListener {
		public DefaultNodeDropListener() {
		}

		public void dragEnter(final DropTargetDragEvent dtde) {
		}

		public void dragExit(final DropTargetEvent dte) {
		}

		public void dragOver(final DropTargetDragEvent dtde) {
		}

		public void drop(final DropTargetDropEvent dtde) {
		}

		public void dropActionChanged(final DropTargetDragEvent dtde) {
		}
	}

	/**
	 * The KeyListener which belongs to the node and cares for Events like C-D
	 * (Delete Node). It forwards the requests to NodeController.
	 */
	static public class DefaultNodeKeyListener implements KeyListener {
		private boolean disabledKeyType = true;
		final private IEditHandler editHandler;
		final private KeyStroke keyStrokeDown;
		final private KeyStroke keyStrokeLeft;
		final private KeyStroke keyStrokeRight;
		final private KeyStroke keyStrokeUp;
		private boolean keyTypeAddsNew = false;
		final private String up, down, left, right;

		public DefaultNodeKeyListener(final IEditHandler editHandler) {
			this.editHandler = editHandler;
			up = Controller.getResourceController().getAdjustableProperty("keystroke_move_up");
			down = Controller.getResourceController().getAdjustableProperty("keystroke_move_down");
			left = Controller.getResourceController().getAdjustableProperty("keystroke_move_left");
			right = Controller.getResourceController()
			    .getAdjustableProperty("keystroke_move_right");
			disabledKeyType = Controller.getResourceController()
			    .getBoolProperty("disable_key_type");
			keyTypeAddsNew = Controller.getResourceController()
			    .getBoolProperty("key_type_adds_new");
			keyStrokeUp = KeyStroke.getKeyStroke(up);
			keyStrokeDown = KeyStroke.getKeyStroke(down);
			keyStrokeLeft = KeyStroke.getKeyStroke(left);
			keyStrokeRight = KeyStroke.getKeyStroke(right);
		}

		public void keyPressed(final KeyEvent e) {
			if (e.isAltDown() || e.isControlDown() || e.isMetaDown()) {
				return;
			}
			switch (e.getKeyCode()) {
				case KeyEvent.VK_ENTER:
				case KeyEvent.VK_ESCAPE:
				case KeyEvent.VK_SHIFT:
				case KeyEvent.VK_DELETE:
				case KeyEvent.VK_SPACE:
				case KeyEvent.VK_INSERT:
				case KeyEvent.VK_TAB:
					return;
				case KeyEvent.VK_UP:
				case KeyEvent.VK_DOWN:
				case KeyEvent.VK_LEFT:
				case KeyEvent.VK_RIGHT:
				case KeyEvent.VK_PAGE_UP:
				case KeyEvent.VK_PAGE_DOWN:
					Controller.getController().getMapView().move(e);
					return;
				case KeyEvent.VK_HOME:
				case KeyEvent.VK_END:
				case KeyEvent.VK_BACK_SPACE:
					if (editHandler != null) {
						editHandler.edit(e, false, false);
					}
					return;
			}
			if (!disabledKeyType) {
				if (!e.isActionKey() && e.getKeyChar() != KeyEvent.CHAR_UNDEFINED) {
					if (editHandler != null) {
						editHandler.edit(e, keyTypeAddsNew, false);
					}
					return;
				}
			}
			boolean doMove = false;
			if (keyStrokeUp != null && e.getKeyCode() == keyStrokeUp.getKeyCode()) {
				e.setKeyCode(KeyEvent.VK_UP);
				doMove = true;
			}
			else if (keyStrokeDown != null && e.getKeyCode() == keyStrokeDown.getKeyCode()) {
				e.setKeyCode(KeyEvent.VK_DOWN);
				doMove = true;
			}
			else if (keyStrokeLeft != null && e.getKeyCode() == keyStrokeLeft.getKeyCode()) {
				e.setKeyCode(KeyEvent.VK_LEFT);
				doMove = true;
			}
			else if (keyStrokeRight != null && e.getKeyCode() == keyStrokeRight.getKeyCode()) {
				e.setKeyCode(KeyEvent.VK_RIGHT);
				doMove = true;
			}
			if (doMove) {
				Controller.getController().getMapView().move(e);
				e.consume();
				return;
			}
		}

		public void keyReleased(final KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
				Controller.getController().getMapView().resetShiftSelectionOrigin();
			}
		}

		public void keyTyped(final KeyEvent e) {
		}
	}

	/**
	 * The MouseMotionListener which belongs to every NodeView
	 */
	static public class DefaultNodeMotionListener extends MouseAdapter implements IMouseListener {
		public DefaultNodeMotionListener() {
		}

		public void mouseDragged(final MouseEvent e) {
		}

		public void mouseMoved(final MouseEvent e) {
		}
	}

	public boolean extendSelection(final MouseEvent e) {
		final NodeModel newlySelectedNodeView = ((MainView) e.getComponent()).getNodeView().getModel();
		final boolean extend = e.isControlDown();
		final boolean range = e.isShiftDown();
		final boolean branch = e.isAltGraphDown() || e.isAltDown();
		/* windows alt, linux altgraph .... */
		boolean retValue = false;
		if (extend || range || branch
		        || !Controller.getController().getSelection().isSelected(newlySelectedNodeView)) {
			if (!range) {
				if (extend) {
					Controller.getController().getSelection().toggleSelected(newlySelectedNodeView);
				}
				else {
					Controller.getController().getSelection().selectAsTheOnlyOneSelected(newlySelectedNodeView);
				}
				retValue = true;
			}
			else {
				Controller.getController().getSelection().selectContinuous(
				    newlySelectedNodeView);
				retValue = true;
			}
			if (branch) {
				Controller.getController().getSelection().selectBranch(newlySelectedNodeView, extend);
				retValue = true;
			}
		}
		if (retValue) {
			e.consume();
		}
		return retValue;
	}

	/**
	 * The MouseMotionListener which belongs to every NodeView
	 */
	static public class DefaultNodeMouseMotionListener implements INodeMouseMotionListener {
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
						if (e.getModifiers() == 0
						        && !c.isBlocked()
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
			return new Rectangle((int) (p.getX() - side / 2), (int) (p.getY() - side / 2), side,
			    side);
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
				    LinkController.getController(Controller.getModeController())
				        .getLinkShortText(node.getNodeView().getModel()));
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

	public static final String NODE_POPUP = "/node_popup";

	private Component leftToolBar;
	private JToolBar mainToolBar;
	private IMouseListener mapMouseListener;
	private MouseWheelListener mapMouseWheelListener;
	final private ActionListener mapsMenuActionListener;
	private JPopupMenu mapsPopupMenu;
	private FreeplaneMenuBar menuBar;
	private final MenuBuilder menuBuilder;
	private URL menuStructure;
	final private HashSet mRegisteredMouseWheelEventHandler = new HashSet();
	private DragGestureListener nodeDragListener;
	private DropTargetListener nodeDropTargetListener;
	private KeyListener nodeKeyListener;
	private IMouseListener nodeMotionListener;
	private INodeMouseMotionListener nodeMouseMotionListener;
	private JPopupMenu nodePopupMenu;

	public UserInputListenerFactory(final ModeController modeController) {
		mapsMenuActionListener = new MapsMenuActionListener();
		menuBuilder = new MenuBuilder(modeController);
	}

	public void addMouseWheelEventHandler(final IMouseWheelEventHandler handler) {
		mRegisteredMouseWheelEventHandler.add(handler);
	}

	public Component getLeftToolBar() {
		return leftToolBar;
	}

	public JToolBar getMainToolBar() {
		return mainToolBar;
	}

	public IMouseListener getMapMouseListener() {
		if (mapMouseListener == null) {
			mapMouseListener = new DefaultMapMouseListener(new DefaultMapMouseReceiver());
		}
		return mapMouseListener;
	}

	public MouseWheelListener getMapMouseWheelListener() {
		if (mapMouseWheelListener == null) {
			mapMouseWheelListener = new DefaultMouseWheelListener();
		}
		return mapMouseWheelListener;
	}

	public JPopupMenu getMapPopup() {
		return mapsPopupMenu;
	}

	public FreeplaneMenuBar getMenuBar() {
		if (menuBar == null) {
			menuBar = new FreeplaneMenuBar();
		}
		return menuBar;
	}

	public MenuBuilder getMenuBuilder() {
		return menuBuilder;
	}

	public URL getMenuStructure() {
		return menuStructure;
	}

	public Set getMouseWheelEventHandlers() {
		return Collections.unmodifiableSet(mRegisteredMouseWheelEventHandler);
	}

	public DragGestureListener getNodeDragListener() {
		if (nodeDragListener == null) {
			nodeDragListener = new DefaultNodeDragListener();
		}
		return nodeDragListener;
	}

	public DropTargetListener getNodeDropTargetListener() {
		if (nodeDropTargetListener == null) {
			nodeDropTargetListener = new DefaultNodeDropListener();
		}
		return nodeDropTargetListener;
	}

	public KeyListener getNodeKeyListener() {
		if (nodeKeyListener == null) {
			nodeKeyListener = new DefaultNodeKeyListener(null);
		}
		return nodeKeyListener;
	}

	public IMouseListener getNodeMotionListener() {
		if (nodeMotionListener == null) {
			nodeMotionListener = new DefaultNodeMotionListener();
		}
		return nodeMotionListener;
	}

	public INodeMouseMotionListener getNodeMouseMotionListener() {
		if (nodeMouseMotionListener == null) {
			nodeMouseMotionListener = new DefaultNodeMouseMotionListener();
		}
		return nodeMouseMotionListener;
	}

	public JPopupMenu getNodePopupMenu() {
		return nodePopupMenu;
	}

	public void removeMouseWheelEventHandler(final IMouseWheelEventHandler handler) {
		mRegisteredMouseWheelEventHandler.remove(handler);
	}

	public void setLeftToolBar(final Component leftToolBar) {
		if (this.leftToolBar != null) {
			throw new RuntimeException("already set");
		}
		this.leftToolBar = leftToolBar;
	}

	public void setMainToolBar(final JToolBar mainToolBar) {
		if (this.mainToolBar != null) {
			throw new RuntimeException("already set");
		}
		this.mainToolBar = mainToolBar;
	}

	public void setMapMouseListener(final IMouseListener mapMouseMotionListener) {
		if (mapMouseListener != null) {
			throw new RuntimeException("already set");
		}
		mapMouseListener = mapMouseMotionListener;
	}

	public void setMapMouseWheelListener(final MouseWheelListener mouseWheelListener) {
		if (mapMouseWheelListener != null) {
			throw new RuntimeException("already set");
		}
		mapMouseWheelListener = mouseWheelListener;
	}

	public void setMenuBar(final FreeplaneMenuBar menuBar) {
		if (mapMouseWheelListener != null) {
			throw new RuntimeException("already set");
		}
		this.menuBar = menuBar;
	}

	public void setMenuStructure(final String menuStructureResource) {
		final URL menuStructure = Controller.getResourceController().getResource(
		    menuStructureResource);
		setMenuStructure(menuStructure);
	}

	private void setMenuStructure(final URL menuStructure) {
		if (this.menuStructure != null) {
			throw new RuntimeException("already set");
		}
		this.menuStructure = menuStructure;
	}

	public void setNodeDropTargetListener(final DropTargetListener nodeDropTargetListener) {
		if (this.nodeDropTargetListener != null) {
			throw new RuntimeException("already set");
		}
		this.nodeDropTargetListener = nodeDropTargetListener;
	}

	public void setNodeKeyListener(final KeyListener nodeKeyListener) {
		if (this.nodeKeyListener != null) {
			throw new RuntimeException("already set");
		}
		this.nodeKeyListener = nodeKeyListener;
	}

	public void setNodeMotionListener(final IMouseListener nodeMotionListener) {
		if (this.nodeMotionListener != null) {
			throw new RuntimeException("already set");
		}
		this.nodeMotionListener = nodeMotionListener;
	}

	public void setNodeMouseMotionListener(final INodeMouseMotionListener nodeMouseMotionListener) {
		if (this.nodeMouseMotionListener != null) {
			throw new RuntimeException("already set");
		}
		this.nodeMouseMotionListener = nodeMouseMotionListener;
	}

	public void setNodePopupMenu(final JPopupMenu nodePopupMenu) {
		if (this.nodePopupMenu != null) {
			throw new RuntimeException("already set");
		}
		this.nodePopupMenu = nodePopupMenu;
	}

	public void updateMapList() {
		updateModeMenu();
		menuBuilder.removeChildElements(FreeplaneMenuBar.MAP_POPUP_MENU + "/maps");
		final MapViewManager mapViewManager = Controller.getController().getMapViewManager();
		final List mapViewVector = mapViewManager.getMapViewVector();
		if (mapViewVector == null) {
			return;
		}
		final ButtonGroup group = new ButtonGroup();
		for (final Iterator iterator = mapViewVector.iterator(); iterator.hasNext();) {
			final MapView mapView = (MapView) iterator.next();
			final String displayName = mapView.getName();
			final JRadioButtonMenuItem newItem = new JRadioButtonMenuItem(displayName);
			newItem.setSelected(false);
			group.add(newItem);
			newItem.addActionListener(mapsMenuActionListener);
			newItem.setMnemonic(displayName.charAt(0));
			final MapView currentMapView = mapViewManager.getMapView();
			if (currentMapView != null) {
				if (mapView == currentMapView) {
					newItem.setSelected(true);
				}
			}
			menuBuilder.addMenuItem(FreeplaneMenuBar.MAP_POPUP_MENU + "/maps", newItem,
			    UIBuilder.AS_CHILD);
		}
	}

	public void updateMenus(final ModeController modeController) {
		final FreeplaneMenuBar menuBar = getMenuBar();
		menuBuilder.addMenuBar(menuBar, FreeplaneMenuBar.MENU_BAR_PREFIX);
		mapsPopupMenu = new JPopupMenu();
		menuBuilder.addPopupMenu(mapsPopupMenu, FreeplaneMenuBar.MAP_POPUP_MENU);
		menuBuilder.addPopupMenu(getNodePopupMenu(), UserInputListenerFactory.NODE_POPUP);
		menuBuilder.addToolbar(getMainToolBar(), "/main_toolbar");
		mapsPopupMenu.setName(Controller.getText("mindmaps"));
		if (menuStructure != null) {
			menuBuilder.processMenuCategory(menuStructure);
		}
		final ViewController viewController = Controller.getController().getViewController();
		viewController.updateMenus(menuBuilder);
		updateMapList();
	}

	private void updateModeMenu() {
		menuBuilder.removeChildElements(FreeplaneMenuBar.MODES_MENU);
		final ButtonGroup group = new ButtonGroup();
		final ActionListener modesMenuActionListener = new ModesMenuActionListener();
		final List keys = new LinkedList(Controller.getController().getModes());
		for (final ListIterator i = keys.listIterator(); i.hasNext();) {
			final String key = (String) i.next();
			final JRadioButtonMenuItem newItem = new JRadioButtonMenuItem(key);
			menuBuilder.addMenuItem(FreeplaneMenuBar.MODES_MENU, newItem, MenuBuilder.AS_CHILD);
			group.add(newItem);
			final ModeController modeController = Controller.getModeController();
			if (modeController != null) {
				newItem.setSelected(modeController.getModeName().equals(key));
			}
			else {
				newItem.setSelected(false);
			}
			final String keystroke = Controller.getResourceController().getAdjustableProperty(
			    "keystroke_mode_" + key);
			if (keystroke != null) {
				newItem.setAccelerator(KeyStroke.getKeyStroke(keystroke));
			}
			newItem.addActionListener(modesMenuActionListener);
		}
	}
}
