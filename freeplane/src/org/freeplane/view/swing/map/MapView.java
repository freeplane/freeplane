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
package org.freeplane.view.swing.map;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.dnd.Autoscroll;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.KeyEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import org.freeplane.core.controller.Controller;
import org.freeplane.core.io.xml.TreeXmlReader;
import org.freeplane.core.modecontroller.IMapChangeListener;
import org.freeplane.core.modecontroller.IMapSelection;
import org.freeplane.core.modecontroller.MapChangeEvent;
import org.freeplane.core.modecontroller.MapController;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.IUserInputListenerFactory;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.ColorUtils;
import org.freeplane.features.common.addins.mapstyle.MapStyle;
import org.freeplane.features.common.addins.mapstyle.MapStyleModel;
import org.freeplane.features.common.addins.mapstyle.MapViewLayout;
import org.freeplane.features.common.link.ConnectorModel;
import org.freeplane.features.common.link.LinkController;
import org.freeplane.features.common.link.LinkModel;
import org.freeplane.features.common.link.NodeLinks;
import org.freeplane.features.common.nodestyle.NodeStyleController;
import org.freeplane.features.controller.print.FitMap;
import org.freeplane.view.swing.map.link.ConnectorView;
import org.freeplane.view.swing.map.link.EdgeLinkView;
import org.freeplane.view.swing.map.link.ILinkView;

/**
 * This class represents the view of a whole MindMap (in analogy to class
 * JTree).
 */
public class MapView extends JPanel implements Printable, Autoscroll, IMapChangeListener {
	private final class ParentListener extends ComponentAdapter implements ContainerListener {
	    public void componentRemoved(ContainerEvent e) {
	    	if(e.getChild() == MapView.this){
	    		final Container container = e.getContainer();
				container.removeContainerListener(parentListener);
				SwingUtilities.getAncestorOfClass(JScrollPane.class, container).removeComponentListener(parentListener);
	    		parentListener = null;
	    	}
	    }

	    public void componentAdded(ContainerEvent e) {
	    }

		@Override
        public void componentResized(ComponentEvent e) {
			if(anchor == null){
				return;
			}
			if (nodeToBeVisible == null){
				nodeToBeVisible = getSelected();
				extraWidth = 0;
			}
			setViewPositionAfterValidate();
        }
	    
	    
    }

	enum PaintingMode{CLOUDS, NODES, ALL};
	private MapViewLayout layoutType;
	public MapViewLayout getLayoutType() {
    	return layoutType;
    }

	protected void setLayoutType(MapViewLayout layoutType) {
    	this.layoutType = layoutType;
    }

	private PaintingMode paintingMode = PaintingMode.ALL;
	private class MapSelection implements IMapSelection {
		public void centerNode(final NodeModel node) {
			final NodeView nodeView = getNodeView(node);
			if (nodeView != null) {
				MapView.this.centerNode(nodeView);
			}
		}

		public NodeModel getSelected() {
			final NodeView selected = MapView.this.getSelected();
			return selected == null ? null : selected.getModel();
		}

		public List<NodeModel> getSelection() {
			return MapView.this.getSelectedNodes();
		}

		public List<NodeModel> getSortedSelection(boolean differentSubtrees) {
			return MapView.this.getSelectedNodesSortedByY(differentSubtrees);
		}

		public boolean isSelected(final NodeModel node) {
			final NodeView nodeView = getNodeView(node);
			return nodeView != null && MapView.this.isSelected(nodeView);
		}

		public void keepNodePosition(final NodeModel node, float horizontalPoint, float verticalPoint) {
			anchorToSelected(node, horizontalPoint, verticalPoint);
		}

		public void makeTheSelected(final NodeModel node) {
			final NodeView nodeView = getNodeView(node);
			if (nodeView != null) {
				MapView.this.makeTheSelected(nodeView);
			}
		}

		public void scrollNodeToVisible(final NodeModel node) {
			MapView.this.scrollNodeToVisible(getNodeView(node));
		}

		public void selectAsTheOnlyOneSelected(final NodeModel node) {
			final NodeView nodeView = getNodeView(node);
			if (nodeView != null) {
				MapView.this.selectAsTheOnlyOneSelected(nodeView);
			}
		}

		public void selectBranch(final NodeModel node, final boolean extend) {
			MapView.this.selectBranch(getNodeView(node), extend);
		}

		public void selectContinuous(final NodeModel node) {
			MapView.this.selectContinuous(getNodeView(node));
		}

		public void selectRoot() {
			final NodeModel rootNode = getModel().getRootNode();
			selectAsTheOnlyOneSelected(rootNode);
			centerNode(rootNode);
		}

		public void setSiblingMaxLevel(final int nodeLevel) {
			MapView.this.setSiblingMaxLevel(nodeLevel);
		}

		public int size() {
			return getSelection().size();
		}

		public void toggleSelected(final NodeModel node) {
			MapView.this.toggleSelected(getNodeView(node), true);
		}
	}

	private class Selection {
		final private Vector mySelected = new Vector();

		public Selection() {
		};

		public void add(final NodeView node) {
			if (size() > 0) {
				removeSelectionForHooks(get(0));
			}
			mySelected.add(0, node);
			addSelectionForHooks(node);
		}

		private void addSelectionForHooks(final NodeView node) {
			final ModeController modeController = getModeController();
			final MapController mapController = modeController.getMapController();
			final NodeModel model = node.getModel();
			mapController.onSelect(model);
		}

		public void clear() {
			if (size() > 0) {
				removeSelectionForHooks(get(0));
			}
			mySelected.clear();
		}

		public boolean contains(final NodeView node) {
			return mySelected.contains(node);
		}

		public NodeView get(final int i) {
			return (NodeView) mySelected.get(i);
		}

		/**
		 * @return
		 */
		public List<NodeView> getSelection() {
			return Collections.unmodifiableList(mySelected);
		}

		/**
		 */
		public void moveToFirst(final NodeView newSelected) {
			if (contains(newSelected)) {
				final int pos = mySelected.indexOf(newSelected);
				if (pos > 0) {
					if (size() > 0) {
						removeSelectionForHooks(get(0));
					}
					mySelected.remove(newSelected);
					mySelected.add(0, newSelected);
				}
			}
			else {
				add(newSelected);
			}
			addSelectionForHooks(newSelected);
		}

		public void remove(final NodeView node) {
			if (mySelected.indexOf(node) == 0) {
				removeSelectionForHooks(node);
			}
			mySelected.remove(node);
		}

		private void removeSelectionForHooks(final NodeView node) {
			if (node.getModel() == null) {
				return;
			}
			getModeController().getMapController().onDeselect(node.getModel());
		}

		public int size() {
			return mySelected.size();
		}
	}

	private static final int margin = 20;
	static boolean printOnWhiteBackground;
	static private IFreeplanePropertyListener propertyChangeListener;
	public static final String RESOURCES_SELECTED_NODE_COLOR = "standardselectednodecolor";
	public static final String RESOURCES_SELECTED_NODE_RECTANGLE_COLOR = "standardselectednoderectanglecolor";
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static boolean standardDrawRectangleForSelection;
	static Color standardSelectColor;
	private static Stroke standardSelectionStroke;
	static Color standardSelectRectangleColor;
	private NodeView anchor;
	private Point anchorContentLocation;
	/** Used to identify a right click onto a link curve. */
	private Vector<ILinkView> arrowLinkViews;
	private Color background = null;
	private Rectangle boundingRectangle = null;
	private int centerNodeCounter;
	final private Controller controller;
	private boolean disableMoveCursor = true;
	private int extraWidth;
	private FitMap fitMap = FitMap.USER_DEFINED;
	private boolean isPreparedForPrinting = false;
	private boolean isPrinting = false;
	private int maxNodeWidth = 0;
	private final ModeController modeController;
	final private MapModel model;
	private NodeView nodeToBeVisible = null;
	private NodeView rootView = null;
	private boolean selectedsValid = true;
	final private Selection selection = new Selection();
	private NodeView shiftSelectionOrigin = null;
	private int siblingMaxLevel;
	private float zoom = 1F;
	private float anchorHorizontalPoint;
	private float anchorVerticalPoint;
	private ParentListener parentListener;

	public MapView(final MapModel model, final ModeController modeController) {
		super();
		this.model = model;
		this.modeController = modeController;
		controller = modeController.getController();
		final String name = getModel().getTitle();
		setName(name);
		if (MapView.standardSelectColor == null) {
			final String stdcolor = ResourceController.getResourceController().getProperty(
			    MapView.RESOURCES_SELECTED_NODE_COLOR);
			MapView.standardSelectColor = ColorUtils.stringToColor(stdcolor);
			final String stdtextcolor = ResourceController.getResourceController().getProperty(
			    MapView.RESOURCES_SELECTED_NODE_RECTANGLE_COLOR);
			MapView.standardSelectRectangleColor = ColorUtils.stringToColor(stdtextcolor);
			final String drawCircle = ResourceController.getResourceController().getProperty(
			    ResourceController.RESOURCE_DRAW_RECTANGLE_FOR_SELECTION);
			MapView.standardDrawRectangleForSelection = TreeXmlReader.xmlToBoolean(drawCircle);
			final String printOnWhite = ResourceController.getResourceController()
			    .getProperty("printonwhitebackground");
			MapView.printOnWhiteBackground = TreeXmlReader.xmlToBoolean(printOnWhite);
			createPropertyChangeListener();
		}
		this.setAutoscrolls(true);
		this.setLayout(new MindMapLayout());
		initRoot();
		setBackground(requiredBackground());
		MapStyleModel mapStyleModel = MapStyleModel.getExtension(model);
		zoom = mapStyleModel.getZoom();
		layoutType = mapStyleModel.getMapViewLayout();
		final IUserInputListenerFactory userInputListenerFactory = getModeController().getUserInputListenerFactory();
		addMouseListener(userInputListenerFactory.getMapMouseListener());
		addMouseMotionListener(userInputListenerFactory.getMapMouseListener());
		addMouseWheelListener(userInputListenerFactory.getMapMouseWheelListener());
		setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, Collections.EMPTY_SET);
		setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, Collections.EMPTY_SET);
		setFocusTraversalKeys(KeyboardFocusManager.UP_CYCLE_TRAVERSAL_KEYS, Collections.EMPTY_SET);
		disableMoveCursor = ResourceController.getResourceController().getBooleanProperty("disable_cursor_move_paper");
	}

	private void anchorToSelected(final NodeModel node, float horizontalPoint, float verticalPoint) {
		final NodeView view = getNodeView(node);
		anchorToSelected(view, horizontalPoint, verticalPoint);
	}

	void anchorToSelected(final NodeView view, float horizontalPoint, float verticalPoint) {
		if (view != null) {
			anchor = view;
			anchorHorizontalPoint = horizontalPoint;
			anchorVerticalPoint = verticalPoint;
			anchorContentLocation = getAnchorCenterPoint();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.dnd.Autoscroll#autoscroll(java.awt.Point)
	 */
	public void autoscroll(final Point cursorLocn) {
		final Rectangle r = new Rectangle((int) cursorLocn.getX() - MapView.margin, (int) cursorLocn.getY()
		        - MapView.margin, 1 + 2 * MapView.margin, 1 + 2 * MapView.margin);
		scrollRectToVisible(r);
	}

	/**
	 * Problem: Before scrollRectToVisible is called, the node has the location
	 * (0,0), ie. the location first gets calculated after the scrollpane is
	 * actually scrolled. Thus, as a workaround, I simply call
	 * scrollRectToVisible twice, the first time the location of the node is
	 * calculated, the second time the scrollPane is actually scrolled.
	 */
	public void centerNode(final NodeView node) {
		if (SwingUtilities.getRoot(this) == null) {
			addAncestorListener(new AncestorListener() {
				public void ancestorAdded(final AncestorEvent event) {
					removeAncestorListener(this);
					centerNode(node);
				}

				public void ancestorMoved(final AncestorEvent event) {
				}

				public void ancestorRemoved(final AncestorEvent event) {
				}
			});
			return;
		}
		nodeToBeVisible = null;
		if (!(isValid() && isShowing())) {
			centerNodeCounter = 5;
		}
		if (centerNodeCounter != 0) {
			centerNodeCounter--;
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					centerNode(node);
				}
			});
			return;
		}
		final JViewport viewPort = (JViewport) getParent();
		final Dimension d = viewPort.getExtentSize();
		final JComponent content = node.getContent();
		final Rectangle rect = new Rectangle(content.getWidth() / 2 - d.width / 2, content.getHeight() / 2 - d.height
		        / 2, d.width, d.height);
		final Point oldAnchorContentLocation = anchorContentLocation;
		anchorContentLocation = new Point();
		final Point oldViewPosition = viewPort.getViewPosition();
		content.scrollRectToVisible(rect);
		final Point newViewPosition = viewPort.getViewPosition();
		if (oldViewPosition.equals(newViewPosition)) {
			anchorContentLocation = oldAnchorContentLocation;
		}
	}

	/**
	 * @return
	 */
	public List<NodeView> cloneSelection() {
		final List<NodeView> copy = new ArrayList<NodeView>();
		copy.addAll(getSelection());
		return copy;
	}

	private void createPropertyChangeListener() {
		MapView.propertyChangeListener = new IFreeplanePropertyListener() {
			public void propertyChanged(final String propertyName, final String newValue, final String oldValue) {
				final Component mapView = controller.getViewController().getMapView();
				if (!(mapView instanceof MapView)) {
					return;
				}
				if (propertyName.equals(MapView.RESOURCES_SELECTED_NODE_COLOR)) {
					MapView.standardSelectColor = ColorUtils.stringToColor(newValue);
					((MapView) mapView).repaintSelecteds();
					return;
				}
				if (propertyName.equals(MapView.RESOURCES_SELECTED_NODE_RECTANGLE_COLOR)) {
					MapView.standardSelectRectangleColor = ColorUtils.stringToColor(newValue);
					((MapView) mapView).repaintSelecteds();
					return;
				}
				if (propertyName.equals(ResourceController.RESOURCE_DRAW_RECTANGLE_FOR_SELECTION)) {
					MapView.standardDrawRectangleForSelection = TreeXmlReader.xmlToBoolean(newValue);
					((MapView) mapView).repaintSelecteds();
					return;
				}
				if (propertyName.equals("printonwhitebackground")) {
					MapView.printOnWhiteBackground = TreeXmlReader.xmlToBoolean(newValue);
					return;
				}
				if (propertyName.equals(NodeView.RESOURCES_SHOW_NODE_TOOLTIPS)) {
					getRoot().updateToolTipsRecursive();
					return;
				}
			}
		};
		ResourceController.getResourceController().addPropertyChangeListener(MapView.propertyChangeListener);
	}

	public void deselect(final NodeView newSelected) {
		if (isSelected(newSelected)) {
			selection.remove(newSelected);
			newSelected.repaintSelected();
		}
	}

	public Object detectCollision(final Point p) {
		if (arrowLinkViews == null) {
			return null;
		}
		for (int i = 0; i < arrowLinkViews.size(); ++i) {
			final ILinkView arrowView = arrowLinkViews.get(i);
			if (arrowView.detectCollision(p, true)) {
				return arrowView.getModel();
			}
		}
		for (int i = 0; i < arrowLinkViews.size(); ++i) {
			final ILinkView arrowView = arrowLinkViews.get(i);
			if (arrowView.detectCollision(p, false)) {
				return arrowView.getModel();
			}
		}
		return null;
	}

	/**
	 * Call preparePrinting() before printing and endPrinting() after printing
	 * to minimize calculation efforts
	 */
	public void endPrinting() {
		if (isPreparedForPrinting == true) {
			isPrinting = false;
			if(zoom == 1f){
				getRoot().updateAll();
				validateTree();
			}
			if (MapView.printOnWhiteBackground) {
				setBackground(background);
			}
		}
		isPreparedForPrinting = false;
	}

	private void extendSelectionWithKeyMove(final NodeView newlySelectedNodeView, final KeyEvent e) {
		if (e.isShiftDown()) {
			if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_RIGHT) {
				shiftSelectionOrigin = null;
				final NodeView toBeNewSelected = newlySelectedNodeView.isParentOf(getSelected()) ? newlySelectedNodeView
				        : getSelected();
				selectBranch(toBeNewSelected, false);
				makeTheSelected(toBeNewSelected);
				return;
			}
			if (shiftSelectionOrigin == null) {
				shiftSelectionOrigin = getSelected();
			}
			final int newY = getMainViewY(newlySelectedNodeView);
			final int selectionOriginY = getMainViewY(shiftSelectionOrigin);
			final int deltaY = newY - selectionOriginY;
			NodeView currentSelected = getSelected();
			if (e.getKeyCode() == KeyEvent.VK_PAGE_UP) {
				for (;;) {
					final int currentSelectedY = getMainViewY(currentSelected);
					if (currentSelectedY > selectionOriginY) {
						deselect(currentSelected);
					}
					else {
						makeTheSelected(currentSelected);
					}
					if (currentSelectedY <= newY) {
						break;
					}
					currentSelected = currentSelected.getPreviousVisibleSibling();
				}
				return;
			}
			if (e.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
				for (;;) {
					final int currentSelectedY = getMainViewY(currentSelected);
					if (currentSelectedY < selectionOriginY) {
						deselect(currentSelected);
					}
					else {
						makeTheSelected(currentSelected);
					}
					if (currentSelectedY >= newY) {
						break;
					}
					currentSelected = currentSelected.getNextVisibleSibling();
				}
				return;
			}
			final boolean enlargingMove = (deltaY > 0) && (e.getKeyCode() == KeyEvent.VK_DOWN) || (deltaY < 0)
			        && (e.getKeyCode() == KeyEvent.VK_UP);
			if (enlargingMove) {
				toggleSelected(newlySelectedNodeView, true);
			}
			else {
				toggleSelected(getSelected(), true);
				makeTheSelected(newlySelectedNodeView);
			}
		}
		else {
			shiftSelectionOrigin = null;
			selectAsTheOnlyOneSelected(newlySelectedNodeView);
		}
	}

	private Point getAnchorCenterPoint() {
		final MainView mainView = anchor.getMainView();
		final Point anchorCenterPoint = new Point(
			(int) (mainView.getWidth() * anchorHorizontalPoint),
		    (int) (mainView.getHeight() * anchorVerticalPoint));
		final JViewport parent = (JViewport) getParent();
		if (parent == null) {
			return new Point();
		}
		try {
	        UITools.convertPointToAncestor(mainView, anchorCenterPoint, parent);
        }
        catch (NullPointerException e) {
			return new Point();
        }
		final Point viewPosition = parent.getViewPosition();
		anchorCenterPoint.x += viewPosition.x - parent.getWidth()/2;
		anchorCenterPoint.y += viewPosition.y - parent.getHeight()/2;
		return anchorCenterPoint;
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.dnd.Autoscroll#getAutoscrollInsets()
	 */
	public Insets getAutoscrollInsets() {
		final Container parent = getParent();
		if(parent == null){
			return new Insets(0, 0, 0, 0);
		}
		final Rectangle outer = getBounds();
		final Rectangle inner = parent.getBounds();
		return new Insets(inner.y - outer.y + MapView.margin, inner.x - outer.x + MapView.margin, outer.height
		        - inner.height - inner.y + outer.y + MapView.margin, outer.width - inner.width - inner.x + outer.x
		        + MapView.margin);
	}

	/**
	 * Return the bounding box of all the descendants of the source view, that
	 * without BORDER. Should that be implemented in LayoutManager as minimum
	 * size?
	 */
	public Rectangle getInnerBounds() {
		final Rectangle innerBounds = getRoot().getInnerBounds();
		innerBounds.x += getRoot().getX();
		innerBounds.y += getRoot().getY();
		final Rectangle maxBounds = new Rectangle(0, 0, getWidth(), getHeight());
		for (int i = 0; i < arrowLinkViews.size(); ++i) {
			final ILinkView arrowView = arrowLinkViews.get(i);
			arrowView.increaseBounds(innerBounds);
		}
		return innerBounds.intersection(maxBounds);
	}

	private int getMainViewY(final NodeView node) {
		final Point newSelectedLocation = new Point();
		UITools.convertPointToAncestor(node.getMainView(), newSelectedLocation, this);
		final int newY = newSelectedLocation.y;
		return newY;
	}

	public IMapSelection getMapSelection() {
		return new MapSelection();
	}

	public int getMaxNodeWidth() {
		return MapStyleModel.getExtension(getModel()).getMaxNodeWidth();
	}

	public ModeController getModeController() {
		return modeController;
	}

	public MapModel getModel() {
		return model;
	}

	public Point getNodeContentLocation(final NodeView nodeView) {
		final Point contentXY = new Point(0, 0);
		UITools.convertPointToAncestor(nodeView.getContent(), contentXY, this);
		return contentXY;
	}

	public NodeView getNodeView(final NodeModel node) {
		if (node == null) {
			return null;
		}
		final Collection viewers = node.getViewers();
		final Iterator iterator = viewers.iterator();
		while (iterator.hasNext()) {
			final NodeView candidateView = (NodeView) iterator.next();
			if (candidateView.getMap() == this) {
				return candidateView;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#getPreferredSize()
	 */
	@Override
	public Dimension getPreferredSize() {
		if (!getParent().isValid()) {
			final Dimension preferredLayoutSize = getLayout().preferredLayoutSize(this);
			return preferredLayoutSize;
		}
		return super.getPreferredSize();
	}

	public NodeView getRoot() {
		return rootView;
	}

	public NodeView getSelected() {
		if (selection.size() > 0) {
			return selection.get(0);
		}
		else {
			return null;
		}
	}

	private NodeView getSelected(final int i) {
		return selection.get(i);
	}

	public List<NodeModel> getSelectedNodes() {
		final int size = selection.size();
		final ArrayList<NodeModel> selectedNodes = new ArrayList(size);
		for (int i = 0; i < size; i++) {
			selectedNodes.add(getSelected(i).getModel());
		}
		return selectedNodes;
	}

	/**
	 * @param differentSubtrees TODO
	 * @return an ArrayList of MindMapNode objects. If both ancestor and
	 *         descandant node are selected, only the ancestor ist returned
	 */
	ArrayList<NodeModel> getSelectedNodesSortedByY(boolean differentSubtrees) {
		final HashSet selectedNodesSet = new HashSet();
		for (int i = 0; i < selection.size(); i++) {
			selectedNodesSet.add(getSelected(i).getModel());
		}
		final TreeMap<Integer, LinkedList<NodeModel>> sortedNodes = new TreeMap();
		final Point point = new Point();
		iteration: for (int i = 0; i < selection.size(); i++) {
			final NodeView view = getSelected(i);
			final NodeModel node = view.getModel();
			if(differentSubtrees){
				for (NodeModel parent = node.getParentNode(); parent != null; parent = parent.getParentNode()) {
					if (selectedNodesSet.contains(parent)) {
						continue iteration;
					}
				}
			}
			view.getContent().getLocation(point);
			UITools.convertPointToAncestor(view, point, this);
			final Integer pointY = new Integer(point.y);
			LinkedList<NodeModel> nodeList = sortedNodes.get(pointY);
			if (nodeList == null) {
				nodeList = new LinkedList<NodeModel>();
				sortedNodes.put(pointY, nodeList);
			}
			nodeList.add(node);
		}
		final ArrayList<NodeModel> selectedNodes = new ArrayList();
		for (final Iterator<LinkedList<NodeModel>> it = sortedNodes.values().iterator(); it.hasNext();) {
			final LinkedList<NodeModel> nodeList = it.next();
			for (final Iterator<NodeModel> itn = nodeList.iterator(); itn.hasNext();) {
				selectedNodes.add(itn.next());
			}
		}
		return selectedNodes;
	}

	/**
	 * @return
	 */
	public List<NodeView> getSelection() {
		return selection.getSelection();
	}

	public int getSiblingMaxLevel() {
		return siblingMaxLevel;
	}

	/**
	 * Returns the size of the visible part of the view in view coordinates.
	 */
	public Dimension getViewportSize() {
		final JViewport mapViewport = (JViewport) getParent();
		return mapViewport == null ? null : mapViewport.getSize();
	}

	private NodeView getVisibleLeft(final NodeView oldSelected) {
		NodeView newSelected = oldSelected;
		final NodeModel oldModel = oldSelected.getModel();
		if (oldModel.isRoot()) {
			newSelected = oldSelected.getPreferredVisibleChild(layoutType.equals(MapViewLayout.OUTLINE), true);
		}
		else if (!oldSelected.isLeft()) {
			newSelected = oldSelected.getVisibleParentView();
		}
		else {
			if (getModeController().getMapController().isFolded(oldModel)) {
				getModeController().getMapController().setFolded(oldModel, false);
				return oldSelected;
			}
			newSelected = oldSelected.getPreferredVisibleChild(layoutType.equals(MapViewLayout.OUTLINE), true);
			while (newSelected != null && !newSelected.isContentVisible()) {
				newSelected = newSelected.getPreferredVisibleChild(layoutType.equals(MapViewLayout.OUTLINE), true);
			}
		}
		return newSelected;
	}

	private NodeView getVisibleNeighbour(final int directionCode) {
		final NodeView oldSelected = getSelected();
		NodeView newSelected = null;
		switch (directionCode) {
			case KeyEvent.VK_LEFT:
				newSelected = getVisibleLeft(oldSelected);
				if (newSelected != null) {
					setSiblingMaxLevel(newSelected.getModel().getNodeLevel(false));
				}
				return newSelected;
			case KeyEvent.VK_RIGHT:
				newSelected = getVisibleRight(oldSelected);
				if (newSelected != null) {
					setSiblingMaxLevel(newSelected.getModel().getNodeLevel(false));
				}
				return newSelected;
			case KeyEvent.VK_UP:
				newSelected = oldSelected.getPreviousVisibleSibling();
				break;
			case KeyEvent.VK_DOWN:
				newSelected = oldSelected.getNextVisibleSibling();
				break;
			case KeyEvent.VK_PAGE_UP:
				newSelected = oldSelected.getPreviousPage();
				break;
			case KeyEvent.VK_PAGE_DOWN:
				newSelected = oldSelected.getNextPage();
				break;
		}
		return newSelected != oldSelected ? newSelected : null;
	}

	private NodeView getVisibleRight(final NodeView oldSelected) {
		NodeView newSelected = oldSelected;
		final NodeModel oldModel = oldSelected.getModel();
		if (oldModel.isRoot()) {
			newSelected = oldSelected.getPreferredVisibleChild(layoutType.equals(MapViewLayout.OUTLINE), false);
		}
		else if (oldSelected.isLeft()) {
			newSelected = oldSelected.getVisibleParentView();
		}
		else {
			if (getModeController().getMapController().isFolded(oldModel)) {
				getModeController().getMapController().setFolded(oldModel, false);
				return oldSelected;
			}
			newSelected = oldSelected.getPreferredVisibleChild(layoutType.equals(MapViewLayout.OUTLINE), false);
			while (newSelected != null && !newSelected.isContentVisible()) {
				newSelected = newSelected.getPreferredVisibleChild(layoutType.equals(MapViewLayout.OUTLINE), false);
			}
		}
		return newSelected;
	}

	public float getZoom() {
		return zoom;
	}

	public int getZoomed(final int number) {
		return (int) Math.ceil(number * zoom);
	}

	public void initRoot() {
		anchorContentLocation = new Point();
		rootView = NodeViewFactory.getInstance().newNodeView(getModel().getRootNode(), 0, this, this);
		rootView.insert();
		anchor = rootView;
		revalidate();
	}

	public boolean isPrinting() {
		return isPrinting;
	}

	public boolean isSelected(final NodeView n) {
		if (isPrinting) {
			return false;
		}
		return selection.contains(n);
	}

	/**
	 * Add the node to the selection if it is not yet there, making it the
	 * focused selected node.
	 */
	void makeTheSelected(final NodeView newSelected) {
		if (isSelected(newSelected)) {
			selection.moveToFirst(newSelected);
		}
		else {
			selection.add(newSelected);
		}
		getSelected().requestFocus();
		getSelected().repaintSelected();
	}

	public void mapChanged(final MapChangeEvent event) {
		Object property = event.getProperty();
		if (property.equals(MapStyle.RESOURCES_BACKGROUND_COLOR)) {
			setBackground(requiredBackground());
			return;
		}
		if (property.equals(NodeStyleController.RESOURCES_NODE_TEXT_COLOR)
				|| property.equals(MapStyle.MAX_NODE_WIDTH)) {
			getRoot().updateAll();
			return;
		}
	}

	public void move(final KeyEvent e) {
		final NodeView newSelected = getVisibleNeighbour(e.getKeyCode());
		if (newSelected != null) {
			if (!(newSelected == getSelected())) {
				extendSelectionWithKeyMove(newSelected, e);
				scrollNodeToVisible(newSelected);
			}
			e.consume();
		}
	}

	public void onNodeDeleted(final NodeModel parent, final NodeModel child, final int index) {
	}

	public void onNodeInserted(final NodeModel parent, final NodeModel child, final int newIndex) {
	}

	public void onNodeMoved(final NodeModel oldParent, final int oldIndex, final NodeModel newParent,
	                        final NodeModel child, final int newIndex) {
	}

	public void onPreNodeDelete(final NodeModel oldParent, final NodeModel selectedNode, final int index) {
	}

	/*****************************************************************
	 ** P A I N T I N G **
	 *****************************************************************/
	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	@Override
	public void paint(final Graphics g) {
		if (isValid()) {
			anchorContentLocation = getAnchorCenterPoint();
		}
		final Graphics2D g2 = (Graphics2D) g.create();
		try {
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			controller.getViewController().setTextRenderingHint(g2);
			super.paint(g2);
		}
		finally {
			g2.dispose();
		}
	};

	@Override
	public void paintChildren(final Graphics graphics) {
		final boolean paintLinksBehind = ResourceController.getResourceController().getBooleanProperty(
		    "paint_connectors_behind");
		if (paintLinksBehind) {
			paintingMode = PaintingMode.CLOUDS;
			super.paintChildren(graphics);
			paintLinks((Graphics2D) graphics);
			paintingMode = PaintingMode.NODES;
			super.paintChildren(graphics);
		}
		else {
			paintingMode = PaintingMode.ALL;
			super.paintChildren(graphics);
			paintLinks((Graphics2D) graphics);
		}
		paintSelecteds((Graphics2D) graphics);
	}

	protected PaintingMode getPaintingMode() {
    	return paintingMode;
    }

	private void paintLinks(final Collection<LinkModel> links, final Graphics2D graphics,
	                        final HashSet alreadyPaintedLinks) {
		final Font font = graphics.getFont();
		try {
			final String fontFamily = ResourceController.getResourceController().getProperty("label_font_family");
			final int fontSize = ResourceController.getResourceController().getIntProperty("label_font_size", 12);
			final Font linksFont = new Font(fontFamily, 0, getZoomed(fontSize));
			graphics.setFont(linksFont);
			final Iterator<LinkModel> linkIterator = links.iterator();
			while (linkIterator.hasNext()) {
				final LinkModel next = linkIterator.next();
				if (!(next instanceof ConnectorModel)) {
					continue;
				}
				final ConnectorModel ref = (ConnectorModel) next;
				if (alreadyPaintedLinks.add(ref)) {
					final NodeModel target = ref.getTarget();
					if (target == null) {
						continue;
					}
					final NodeModel source = ref.getSource();
					final NodeView sourceView = getNodeView(source);
					final NodeView targetView = getNodeView(target);
					final ILinkView arrowLink;
					if(sourceView != null && targetView != null 
							&& (ref.isEdgeLike() || sourceView.getMap().getLayoutType() == MapViewLayout.OUTLINE)
							&& source.isVisible() && target.isVisible()){
						arrowLink = new EdgeLinkView(ref, sourceView, targetView);
					}
					else{
						arrowLink = new ConnectorView(ref, sourceView, targetView);
					}
					arrowLink.paint(graphics);
					arrowLinkViews.add(arrowLink);
				}
			}
		}
		finally {
			graphics.setFont(font);
		}
	}

	private void paintLinks(final Graphics2D graphics) {
		arrowLinkViews = new Vector<ILinkView>();
		final Object renderingHint = getModeController().getController().getViewController().setEdgesRenderingHint(
		    graphics);
		paintLinks(rootView, graphics, new HashSet());
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, renderingHint);
	}

	private void paintLinks(final NodeView source, final Graphics2D graphics, final HashSet alreadyPaintedLinks) {
		final NodeModel node = source.getModel();
		final Collection<LinkModel> outLinks = NodeLinks.getLinks(node);
		paintLinks(outLinks, graphics, alreadyPaintedLinks);
		final Collection<LinkModel> inLinks = LinkController.getController(getModeController()).getLinksTo(node);
		paintLinks(inLinks, graphics, alreadyPaintedLinks);
		final int nodeViewCount = source.getComponentCount();
		for (int i = 0; i < nodeViewCount; i++) {
			final Component component = source.getComponent(i);
			if(! (component instanceof NodeView)){
				continue;
			}
			final NodeView child = (NodeView) component;
			if(! isPrinting){
				final Rectangle bounds = SwingUtilities.convertRectangle(source, child.getBounds(), this);
				final JViewport vp = (JViewport) getParent();
				final Rectangle viewRect = vp.getViewRect();
				viewRect.x -= viewRect.width;
				viewRect.y -= viewRect.height;
				viewRect.width *= 3;
				viewRect.height *= 3;
				if(! viewRect.intersects(bounds)){
					continue;
				}
			}
			paintLinks(child, graphics, alreadyPaintedLinks);
		}
	}

	private void paintSelected(final Graphics2D g, final NodeView selected) {
		if(selected.getMainView().isEdited()){
			return;
		}
		final int arcWidth = 4;
		final JComponent content = selected.getContent();
		final Point contentLocation = new Point();
		UITools.convertPointToAncestor(content, contentLocation, this);
		g.drawRoundRect(contentLocation.x - arcWidth, contentLocation.y - arcWidth, content.getWidth() + 2 * arcWidth,
		    content.getHeight() + 2 * arcWidth, 15, 15);
	}

	private void paintSelecteds(final Graphics2D g) {
		if (!MapView.standardDrawRectangleForSelection 
				|| isPrinting()) {
			return;
		}
		final Color c = g.getColor();
		final Stroke s = g.getStroke();
		g.setColor(MapView.standardSelectRectangleColor);
		if (MapView.standardSelectionStroke == null) {
			MapView.standardSelectionStroke = new BasicStroke(2.0f);
		}
		g.setStroke(MapView.standardSelectionStroke);
		final Object renderingHint = getModeController().getController().getViewController().setEdgesRenderingHint(g);
		final Iterator i = getSelection().iterator();
		while (i.hasNext()) {
			final NodeView selected = (NodeView) i.next();
			paintSelected(g, selected);
		}
		g.setColor(c);
		g.setStroke(s);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, renderingHint);
	}

	/**
	 * Call preparePrinting() before printing and endPrinting() after printing
	 * to minimize calculation efforts
	 */
	public void preparePrinting() {
		if (isPreparedForPrinting == false) {
			isPrinting = true;
			if(zoom == 1f){
				getRoot().updateAll();
				validateTree();
			}
			if (MapView.printOnWhiteBackground) {
				background = getBackground();
				setBackground(Color.WHITE);
			}
			boundingRectangle = getInnerBounds();
			fitMap = FitMap.valueOf();
			isPreparedForPrinting = true;
		}
	}

	@Override
	public void print(final Graphics g) {
		try {
			preparePrinting();
			super.print(g);
		}
		finally {
			endPrinting();
		}
	}

	public int print(final Graphics graphics, final PageFormat pageFormat, final int pageIndex) {
		double userZoomFactor = ResourceController.getResourceController().getDoubleProperty("user_zoom", 1);
		userZoomFactor = Math.max(0, userZoomFactor);
		userZoomFactor = Math.min(2, userZoomFactor);
		if (fitMap==FitMap.PAGE && pageIndex > 0) {
			return Printable.NO_SUCH_PAGE;
		}
		final Graphics2D graphics2D = (Graphics2D) graphics;
		try {
			preparePrinting();
			double zoomFactor = 1;
			if (fitMap == FitMap.PAGE) {
				final double zoomFactorX = pageFormat.getImageableWidth() / boundingRectangle.getWidth();
				final double zoomFactorY = pageFormat.getImageableHeight() / boundingRectangle.getHeight();
				zoomFactor = Math.min(zoomFactorX, zoomFactorY);
			}
			else 
			{
				if (fitMap == FitMap.WIDTH){
					zoomFactor = pageFormat.getImageableWidth() / boundingRectangle.getWidth();
				}
				else if (fitMap == FitMap.HEIGHT){
					zoomFactor = pageFormat.getImageableHeight() / boundingRectangle.getHeight();
				}
				else {
					zoomFactor = userZoomFactor;
				}
				final int nrPagesInWidth = (int) Math.ceil(zoomFactor * boundingRectangle.getWidth()
					/ pageFormat.getImageableWidth());
				final int nrPagesInHeight = (int) Math.ceil(zoomFactor * boundingRectangle.getHeight()
				        / pageFormat.getImageableHeight());
				if (pageIndex >= nrPagesInWidth * nrPagesInHeight) {
					return Printable.NO_SUCH_PAGE;
				}
				final int yPageCoord = (int) Math.floor(pageIndex / nrPagesInWidth);
				final int xPageCoord = pageIndex - yPageCoord * nrPagesInWidth;
				graphics2D.translate(-pageFormat.getImageableWidth() * xPageCoord, -pageFormat.getImageableHeight()
				        * yPageCoord);
			}
			graphics2D.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
			graphics2D.scale(zoomFactor, zoomFactor);
			graphics2D.translate(-boundingRectangle.getX(), -boundingRectangle.getY());
			print(graphics2D);
		}
		finally {
			endPrinting();
		}
		return Printable.PAGE_EXISTS;
	}

	private void repaintSelecteds() {
		final Iterator iterator = getSelection().iterator();
		while (iterator.hasNext()) {
			final NodeView next = (NodeView) iterator.next();
			next.repaintSelected();
		}
	}

	private Color requiredBackground() {
		final MapStyle mapStyle = (MapStyle) getModeController().getExtension(MapStyle.class);
		final Color mapBackground = mapStyle.getBackground(model);
		return mapBackground;
	}

	public void resetShiftSelectionOrigin() {
		shiftSelectionOrigin = null;
	}

	void revalidateSelecteds() {
		selectedsValid = false;
	}

	/**
	 * Scroll the viewport of the map to the south-west, i.e. scroll the map
	 * itself to the north-east.
	 */
	public void scrollBy(final int x, final int y) {
		final JViewport mapViewport = (JViewport) getParent();
		if (mapViewport != null) {
			final Point currentPoint = mapViewport.getViewPosition();
			currentPoint.translate(x, y);
			if (currentPoint.getX() < 0) {
				currentPoint.setLocation(0, currentPoint.getY());
			}
			if (currentPoint.getY() < 0) {
				currentPoint.setLocation(currentPoint.getX(), 0);
			}
			final double maxX = getSize().getWidth() - mapViewport.getExtentSize().getWidth();
			final double maxY = getSize().getHeight() - mapViewport.getExtentSize().getHeight();
			if (currentPoint.getX() > maxX) {
				currentPoint.setLocation(maxX, currentPoint.getY());
			}
			if (currentPoint.getY() > maxY) {
				currentPoint.setLocation(currentPoint.getX(), maxY);
			}
			mapViewport.setViewPosition(currentPoint);
		}
	}

	public void scrollNodeToVisible(final NodeView node) {
		scrollNodeToVisible(node, 0);
	}

	public void scrollNodeToVisible(final NodeView node, final int extraWidth) {
		if (!isValid()) {
			nodeToBeVisible = node;
			this.extraWidth = extraWidth;
			return;
		}
		final int HORIZ_SPACE = 10;
		final int HORIZ_SPACE2 = 20;
		final int VERT_SPACE = 5;
		final int VERT_SPACE2 = 10;
		final JComponent nodeContent = node.getContent();
		int width = nodeContent.getWidth();
		if (extraWidth < 0) {
			width -= extraWidth;
			nodeContent.scrollRectToVisible(new Rectangle(-HORIZ_SPACE + extraWidth, -VERT_SPACE, width + HORIZ_SPACE2,
			    nodeContent.getHeight() + VERT_SPACE2));
		}
		else {
			width += extraWidth;
			nodeContent.scrollRectToVisible(new Rectangle(-HORIZ_SPACE, -VERT_SPACE, width + HORIZ_SPACE2, nodeContent
			    .getHeight()
			        + VERT_SPACE2));
		}
	}

	/**
	 * Select the node, resulting in only that one being selected.
	 */
	public void selectAsTheOnlyOneSelected(final NodeView newSelected) {
		if (ResourceController.getResourceController().getBooleanProperty("center_selected_node")) {
			centerNode(newSelected);
		}
		else {
			scrollNodeToVisible(newSelected);
		}
		selectAsTheOnlyOneSelected(newSelected, true);
		setSiblingMaxLevel(newSelected.getModel().getNodeLevel(false));
	}

	public void selectAsTheOnlyOneSelected(final NodeView node, final boolean requestFocus) {
		final NodeView newSelected = node;
		final Collection oldSelecteds = cloneSelection();
		selection.clear();
		selection.add(newSelected);
		if (requestFocus) {
			newSelected.requestFocus();
		}
		if (newSelected.getModel().getParentNode() != null) {
			((NodeView) newSelected.getParent()).setPreferredChild(newSelected);
		}
		scrollNodeToVisible(newSelected);
		newSelected.repaintSelected();
		for (final Iterator e = oldSelecteds.iterator(); e.hasNext();) {
			final NodeView oldSelected = (NodeView) e.next();
			if (oldSelected != null) {
				oldSelected.repaintSelected();
			}
		}
	}

	/**
	 * Select the node and his descendants. On extend = false clear up the
	 * previous selection. if extend is false, the past selection will be empty.
	 * if yes, the selection will extended with this node and its children
	 */
	void selectBranch(final NodeView newlySelectedNodeView, final boolean extend) {
		if (!extend) {
			selectAsTheOnlyOneSelected(newlySelectedNodeView);
		}
		else if (!isSelected(newlySelectedNodeView) && newlySelectedNodeView.isContentVisible()) {
			toggleSelected(newlySelectedNodeView, false);
		}
		for (final ListIterator e = newlySelectedNodeView.getChildrenViews().listIterator(); e.hasNext();) {
			final NodeView target = (NodeView) e.next();
			selectBranch(target, true);
		}
	}

	boolean selectContinuous(final NodeView nodeView) {
		/* fc, 25.1.2004: corrected due to completely inconsistent behaviour. */
		NodeView oldSelected = null;
		final NodeView newSelected = nodeView;
		final Collection selList = cloneSelection();
		final Iterator j = selList.iterator(/* selList.size() */);
		while (j.hasNext()) {
			final NodeView selectedNode = (NodeView) j.next();
			if (selectedNode != newSelected && newSelected.isSiblingOf(selectedNode)) {
				oldSelected = selectedNode;
				break;
			}
		}
		if (oldSelected == null) {
			if (!isSelected(newSelected) && newSelected.isContentVisible()) {
				toggleSelected(newSelected, true);
				return true;
			}
			return false;
		}
		final boolean oldPositionLeft = oldSelected.isLeft();
		final boolean newPositionLeft = newSelected.isLeft();
		/* find old starting point. */
		ListIterator i = newSelected.getSiblingViews().listIterator();
		while (i.hasNext()) {
			final NodeView next = (NodeView) i.next();
			if (next == oldSelected) {
				break;
			}
		}
		/*
		 * Remove all selections for the siblings in the connected component
		 * between old and new.
		 */
		final ListIterator i_backup = i;
		while (i.hasNext()) {
			final NodeView next = (NodeView) i.next();
			if ((next.isLeft() == oldPositionLeft || next.isLeft() == newPositionLeft)) {
				if (isSelected(next)) {
					deselect(next);
				}
				else {
					break;
				}
			}
		}
		/* other direction. */
		i = i_backup;
		if (i.hasPrevious()) {
			i.previous(); /* this is old selected! */
			while (i.hasPrevious()) {
				final NodeView previous = (NodeView) i.previous();
				if (previous.isLeft() == oldPositionLeft || previous.isLeft() == newPositionLeft) {
					if (isSelected(previous)) {
						deselect(previous);
					}
					else {
						break;
					}
				}
			}
		}
		/* reset iterator */
		i = newSelected.getSiblingViews().listIterator();
		/* find starting point. */
		i = newSelected.getSiblingViews().listIterator();
		while (i.hasNext()) {
			final NodeView next = (NodeView) i.next();
			if (next == newSelected || next == oldSelected) {
				if (!isSelected(next) && next.isContentVisible()) {
					toggleSelected(next, true);
				}
				break;
			}
		}
		/* select all up to the end point. */
		while (i.hasNext()) {
			final NodeView next = (NodeView) i.next();
			if ((next.isLeft() == oldPositionLeft || next.isLeft() == newPositionLeft) && !isSelected(next)
			        && next.isContentVisible()) {
				toggleSelected(next, true);
			}
			if (next == newSelected || next == oldSelected) {
				break;
			}
		}
		toggleSelected(oldSelected, true);
		toggleSelected(oldSelected, true);
		return true;
	}

	public void setMoveCursor(final boolean isHand) {
		final int requiredCursor = (isHand && !disableMoveCursor) ? Cursor.MOVE_CURSOR : Cursor.DEFAULT_CURSOR;
		if (getCursor().getType() != requiredCursor) {
			setCursor(requiredCursor != Cursor.DEFAULT_CURSOR ? new Cursor(requiredCursor) : null);
		}
	}

	void setSiblingMaxLevel(final int level) {
		siblingMaxLevel = level;
	}

	private void setViewPositionAfterValidate() {
		if (anchorContentLocation.getX() == 0 && anchorContentLocation.getY() == 0) {
			return;
		}
		final JViewport vp = (JViewport) getParent();
		final Point viewPosition = vp.getViewPosition();
		final Point oldAnchorContentLocation = anchorContentLocation;
		final Point newAnchorContentLocation = getAnchorCenterPoint();
		if (anchor != getRoot()) {
			anchor = getRoot();
			anchorContentLocation = getAnchorCenterPoint();
		}
		else{
			anchorContentLocation = newAnchorContentLocation;
		}
		final int deltaX = newAnchorContentLocation.x - oldAnchorContentLocation.x;
		final int deltaY = newAnchorContentLocation.y - oldAnchorContentLocation.y;
		if (deltaX != 0 || deltaY != 0) {
			viewPosition.x += deltaX;
			viewPosition.y += deltaY;
			final int scrollMode = vp.getScrollMode();
			vp.setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
			vp.setViewPosition(viewPosition);
			vp.setScrollMode(scrollMode);
		}
		else {
			repaintVisible();
		}
		if (nodeToBeVisible != null) {
			final int scrollMode = vp.getScrollMode();
			vp.setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
			scrollNodeToVisible(nodeToBeVisible, extraWidth);
			vp.setScrollMode(scrollMode);
			nodeToBeVisible = null;
		}
	}

//	@Override
//    public void repaint(int x, int y, int width, int height) {
//		final JViewport vp = (JViewport) getParent();
//		final Rectangle viewRect = vp.getViewRect();
//		super.repaint(viewRect.x, viewRect.y, viewRect.width, viewRect.height);
////	    super.repaint(x, y, width, height);
//    }

	public void setZoom(final float zoom) {
		this.zoom = zoom;
		anchorToSelected(getSelected(), CENTER_ALIGNMENT, CENTER_ALIGNMENT);
		getRoot().updateAll();
		revalidate();
	}

	/**
	 * Add the node to the selection if it is not yet there, remove it
	 * otherwise.
	 * @param requestFocus TODO
	 */
	private void toggleSelected(final NodeView nodeView, boolean requestFocus) {
		final NodeView newSelected = nodeView;
		NodeView oldSelected = getSelected();
		if (isSelected(newSelected)) {
			if (selection.size() > 1) {
				selection.remove(newSelected);
				oldSelected = newSelected;
			}
		}
		else {
			selection.add(newSelected);
		}
		if(requestFocus){
			getSelected().requestFocus();
		}
		getSelected().repaintSelected();
		if (oldSelected != null) {
			oldSelected.repaintSelected();
		}
	}

	private void validateSelecteds() {
		if (selectedsValid) {
			return;
		}
		selectedsValid = true;
		final ArrayList selectedNodes = new ArrayList();
		for (final Iterator it = getSelection().iterator(); it.hasNext();) {
			final NodeView nodeView = (NodeView) it.next();
			if (nodeView != null) {
				selectedNodes.add(nodeView);
			}
		}
		selection.clear();
		for (final ListIterator it = selectedNodes.listIterator(); it.hasNext();) {
			final NodeView oldNodeView = ((NodeView) it.next());
			if (oldNodeView.isContentVisible()) {
				final NodeView newNodeView = getNodeView(oldNodeView.getModel());
				if (newNodeView != null) {
					selection.add(newNodeView);
				}
			}
		}
		NodeView focussedNodeView = getSelected();
		if (focussedNodeView == null) {
			focussedNodeView = getRoot();
		}
		focussedNodeView.requestFocus();
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.Container#validateTree()
	 */
	@Override
	protected void validateTree() {
		registerParentListener();
		validateSelecteds();
		super.validateTree();
		setViewPositionAfterValidate();
	}

	private void registerParentListener() {
	    if(parentListener != null){
	    	return;
	    }
	    parentListener = new ParentListener();
		final Container parent = getParent();
		parent.addContainerListener(parentListener);
		SwingUtilities.getAncestorOfClass(JScrollPane.class, parent).addComponentListener(parentListener);
	    
    }

	public void onPreNodeMoved(NodeModel oldParent, int oldIndex, NodeModel newParent, NodeModel child, int newIndex) {
    }
	
	public void repaintVisible(){
		final JViewport vp = (JViewport) getParent();
		repaint(vp.getViewRect());
	}
}
