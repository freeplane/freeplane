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

import java.awt.AWTKeyStroke;
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
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.util.AbstractList;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

import org.freeplane.core.io.xml.TreeXmlReader;
import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.IUserInputListenerFactory;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.ColorUtils;
import org.freeplane.features.attribute.AttributeController;
import org.freeplane.features.attribute.ModelessAttributeController;
import org.freeplane.features.filter.Filter;
import org.freeplane.features.link.ConnectorModel;
import org.freeplane.features.link.ConnectorModel.Shape;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.LinkModel;
import org.freeplane.features.link.NodeLinks;
import org.freeplane.features.map.IMapChangeListener;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.INodeChangeListener;
import org.freeplane.features.map.INodeView;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.SummaryNode;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.note.NoteController;
import org.freeplane.features.print.FitMap;
import org.freeplane.features.styles.MapStyle;
import org.freeplane.features.styles.MapStyleModel;
import org.freeplane.features.styles.MapViewLayout;
import org.freeplane.features.text.TextController;
import org.freeplane.features.ui.ViewController;
import org.freeplane.features.url.UrlManager;
import org.freeplane.view.swing.map.link.ConnectorView;
import org.freeplane.view.swing.map.link.EdgeLinkView;
import org.freeplane.view.swing.map.link.ILinkView;

/**
 * This class represents the view of a whole MindMap (in analogy to class
 * JTree).
 */
public class MapView extends JPanel implements Printable, Autoscroll, IMapChangeListener, IFreeplanePropertyListener {

	private MapViewLayout layoutType;

	public MapViewLayout getLayoutType() {
		return layoutType;
	}

	protected void setLayoutType(final MapViewLayout layoutType) {
		this.layoutType = layoutType;
	}

	private boolean showNotes;

	boolean showNotes() {
		return showNotes;
	}

	private void setShowNotes() {
		final boolean showNotes= NoteController.getController(getModeController()).showNotesInMap(getModel());
		if(this.showNotes == showNotes){
			return;
		}
		this.showNotes = showNotes;
		getRoot().updateAll();
	}

	private PaintingMode paintingMode = null;

	private class MapSelection implements IMapSelection {
		public void centerNode(final NodeModel node) {
			final NodeView nodeView = getNodeView(node);
			if (nodeView != null) {
				MapView.this.centerNode(nodeView, false);
			}
		}

		public NodeModel getSelected() {
			final NodeView selected = MapView.this.getSelected();
			return selected.getModel();
		}

		public Set<NodeModel> getSelection() {
			return MapView.this.getSelectedNodes();
		}


		public List<NodeModel> getOrderedSelection() {
			return MapView.this.getOrderedSelectedNodes();
        }
		public List<NodeModel> getSortedSelection(final boolean differentSubtrees) {
			return MapView.this.getSelectedNodesSortedByY(differentSubtrees);
		}

		public boolean isSelected(final NodeModel node) {
			final NodeView nodeView = getNodeView(node);
			return nodeView != null && MapView.this.isSelected(nodeView);
		}

		public void keepNodePosition(final NodeModel node, final float horizontalPoint, final float verticalPoint) {
			anchorToSelected(node, horizontalPoint, verticalPoint);
		}

		public void makeTheSelected(final NodeModel node) {
			final NodeView nodeView = getNodeView(node);
			if (nodeView != null) {
				MapView.this.addSelected(nodeView, false);
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
			if(! extend)
				selectAsTheOnlyOneSelected(node);
			MapView.this.addBranchToSelection(getNodeView(node));
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
			MapView.this.toggleSelected(getNodeView(node));
		}

        public void replaceSelection(NodeModel[] nodes) {
            if(nodes.length == 0)
                return;
            NodeView views[] = new NodeView[nodes.length];
            int i = 0;
            for(NodeModel node : nodes)
                views[i++] = getNodeView(node);
            MapView.this.replaceSelection(views);
        }

	}

	private class Selection {
		final private Set<NodeView> selectedSet = new LinkedHashSet<NodeView>();
		final private List<NodeView> selectedList = new ArrayList<NodeView>();
		private NodeView selectedNode = null;

		public Selection() {
		};

		private void select(final NodeView node) {
			clear();
			selectedSet.add(node);
			selectedList.add(node);
			selectedNode = node;
			addSelectionForHooks(node);
			node.repaintSelected();
		}

		private boolean add(final NodeView node) {
			if(selectedNode == null){
				select(node);
				return true;
			}
			else{
				if(selectedSet.add(node)){
					selectedList.add(node);
					node.repaintSelected();
					return true;
				}
				return false;
			}
		}

		private void addSelectionForHooks(final NodeView node) {
			if(! isSelected())
				return;
			final ModeController modeController = getModeController();
			final MapController mapController = modeController.getMapController();
			final NodeModel model = node.getModel();
			mapController.onSelect(model);
		}

		private void clear() {
			if (selectedNode != null) {
				removeSelectionForHooks(selectedNode);
				selectedNode = null;
				selectedSet.clear();
				selectedList.clear();
			}
		}

		private boolean contains(final NodeView node) {
			return selectedSet.contains(node);
		}

		/**
		 * @return
		 */
		public Set<NodeView> getSelection() {
			return Collections.unmodifiableSet(selectedSet);
		}


		private boolean deselect(final NodeView node) {
			final boolean selectedChanged = selectedNode != null && selectedNode.equals(node);
			if (selectedChanged) {
				removeSelectionForHooks(node);
			}
			if (selectedSet.remove(node)){
				final int last = selectedList.size() - 1;
				if(selectedList.get(last) .equals(node))
					selectedList.remove(last);
				else
					selectedList.remove(node);
				node.repaintSelected();
				if(selectedChanged) {
	                if (size() > 0) {
	                	selectedNode = selectedSet.iterator().next();
	                	addSelectionForHooks(selectedNode);
	                }
	                else{
	                	selectedNode = null;
	                }
                }
				return true;
			}
			return false;
		}

		private void removeSelectionForHooks(final NodeView node) {
			if (node.getModel() == null || ! isSelected()) {
				return;
			}
			getModeController().getMapController().onDeselect(node.getModel());
		}

		private int size() {
			return selectedSet.size();
		}

		private void replace(NodeView[] newSelection) {
            if(newSelection.length == 0)
                return;
            final boolean selectedChanges = ! newSelection[0].equals(selectedNode);
            if (selectedChanges) {
            	if(selectedNode != null)
            		removeSelectionForHooks(selectedNode);
            	selectedNode = newSelection[0];
            }
            for(NodeView view : newSelection)
                if (!selectedSet.contains(view))
                	view.repaintSelected();
            final NodeView[] oldSelection = selectedSet.toArray(new NodeView[selectedSet.size()]);
            selectedSet.clear();
            selectedList.clear();
            for(NodeView view : newSelection)
                if (selectedSet.add(view))
                	selectedList.add(view);
            if (selectedChanges) {
                addSelectionForHooks(selectedNode);
            }
            for(NodeView view : oldSelection)
                if (!selectedSet.contains(view))
                	view.repaintSelected();
        }

		public NodeView[] toArray() {
	        return selectedList.toArray(new NodeView[selectedList.size()]);
        }

		private List<NodeView> getSelectedList() {
	        return selectedList;
        }

		private Set<NodeView> getSelectedSet() {
	        return selectedSet;
        }
	}

	private static final int margin = 20;
	static boolean printOnWhiteBackground;
	static private IFreeplanePropertyListener propertyChangeListener;
	public static final String RESOURCES_SELECTED_NODE_COLOR = "standardselectednodecolor";
	public static final String RESOURCES_SELECTED_NODE_RECTANGLE_COLOR = "standardselectednoderectanglecolor";
	private static final String PRESENTATION_DIMMER_TRANSPARENCY = "presentation_dimmer_transparency";
	private static final String PRESENTATION_MODE_ENABLED = "presentation_mode";

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
	private boolean disableMoveCursor = true;
	private int extraWidth;
	private FitMap fitMap = FitMap.USER_DEFINED;
	private boolean isPreparedForPrinting = false;
	private boolean isPrinting = false;
	private final ModeController modeController;
	final private MapModel model;
	private NodeView nodeToBeVisible = null;
	private NodeView rootView = null;
	private boolean selectedsValid = true;
	final private Selection selection = new Selection();
	private int siblingMaxLevel;
	private float zoom = 1F;
	private float anchorHorizontalPoint;
	private float anchorVerticalPoint;
	private NodeView nodeToBeCentered;
    private Font noteFont;
    private Font detailFont;
    private Color detailForeground;
    private Color detailBackground;
	private boolean slowScroll;
	private static boolean presentationModeEnabled;
	private static int transparency;
	private INodeChangeListener connectorChangeListener;

	public MapView(final MapModel model, final ModeController modeController) {
		super();
		this.model = model;
		this.modeController = modeController;
		final String name = model.getTitle();
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
			MapView.transparency = 255 - ResourceController.getResourceController().getIntProperty(PRESENTATION_DIMMER_TRANSPARENCY, 0x70);
			MapView.presentationModeEnabled = ResourceController.getResourceController().getBooleanProperty(PRESENTATION_MODE_ENABLED);

			createPropertyChangeListener();
		}
		this.setAutoscrolls(true);
		this.setLayout(new MindMapLayout());
		final NoteController noteController = NoteController.getController(getModeController());
		showNotes= noteController != null && noteController.showNotesInMap(getModel());
        updateContentStyle();
        initRoot();
		setBackground(requiredBackground());
		final MapStyleModel mapStyleModel = MapStyleModel.getExtension(model);
		zoom = mapStyleModel.getZoom();
		layoutType = mapStyleModel.getMapViewLayout();
		final IUserInputListenerFactory userInputListenerFactory = getModeController().getUserInputListenerFactory();
		addMouseListener(userInputListenerFactory.getMapMouseListener());
		addMouseMotionListener(userInputListenerFactory.getMapMouseListener());
		addMouseWheelListener(userInputListenerFactory.getMapMouseWheelListener());
		setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, emptyNodeViewSet());
		setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, emptyNodeViewSet());
		setFocusTraversalKeys(KeyboardFocusManager.UP_CYCLE_TRAVERSAL_KEYS, emptyNodeViewSet());
		disableMoveCursor = ResourceController.getResourceController().getBooleanProperty("disable_cursor_move_paper");
		connectorChangeListener = new INodeChangeListener() {
			public void nodeChanged(NodeChangeEvent event) {
				if(NodeLinks.CONNECTOR.equals(event.getProperty()) &&
						event.getNode().getMap().equals(getModel()))
					repaint();
			}
		};
	}

	@Override
    public void addNotify() {
	    super.addNotify();
		modeController.getMapController().addNodeChangeListener(connectorChangeListener);
    }

	@Override
    public void removeNotify() {
		modeController.getMapController().removeNodeChangeListener(connectorChangeListener);
	    super.removeNotify();
    }

	public void replaceSelection(NodeView[] views) {
        selection.replace(views);
        if(views.length > 0)
        	views[0].requestFocusInWindow();
    }

    // generics trickery
	private Set<AWTKeyStroke> emptyNodeViewSet() {
	    return Collections.emptySet();
    }

	private void anchorToSelected(final NodeModel node, final float horizontalPoint, final float verticalPoint) {
		final NodeView view = getNodeView(node);
		anchorToSelected(view, horizontalPoint, verticalPoint);
	}

	void anchorToSelected(final NodeView view, final float horizontalPoint, final float verticalPoint) {
		if (view != null && view.getMainView() != null) {
			anchor = view;
			anchorHorizontalPoint = horizontalPoint;
			anchorVerticalPoint = verticalPoint;
			this.anchorContentLocation = getAnchorCenterPoint();
			if (nodeToBeVisible == null) {
				nodeToBeVisible = anchor;
				extraWidth = 0;
			}
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

	public void centerNode(final NodeView node, boolean slowScroll) {
		if (node != null) {
			this.slowScroll = slowScroll;
			nodeToBeVisible = null;
			nodeToBeCentered = node;
			if (isDisplayable())
				centerNodeNow(slowScroll);
		}
	}

	private void centerNodeNow(boolean slowScroll) {
	    final JViewport viewPort = (JViewport) getParent();
		if(slowScroll)
			viewPort.putClientProperty(ViewController.SLOW_SCROLLING, Boolean.TRUE);
		final Dimension d = viewPort.getExtentSize();
		final JComponent content = nodeToBeCentered.getContent();
		final Rectangle rect = new Rectangle(content.getWidth() / 2 - d.width / 2, content.getHeight() / 2 - d.height
		        / 2, d.width, d.height);
		content.scrollRectToVisible(rect);
		nodeToBeCentered = null;
		this.slowScroll = false;
		this.anchorContentLocation = getAnchorCenterPoint();
    }

	boolean isLayoutCompleted() {
	    final JViewport viewPort = (JViewport) getParent();
		final Dimension visibleDimension = viewPort.getExtentSize();
		return visibleDimension.width > 0;
    }

	static private void createPropertyChangeListener() {
		MapView.propertyChangeListener = new IFreeplanePropertyListener() {
			public void propertyChanged(final String propertyName, final String newValue, final String oldValue) {
				final Component mapView = Controller.getCurrentController().getMapViewManager().getMapViewComponent();
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
				if (propertyName.equals(PRESENTATION_DIMMER_TRANSPARENCY)) {
					MapView.transparency = 255 - ResourceController.getResourceController().getIntProperty(PRESENTATION_DIMMER_TRANSPARENCY, 0x70);
					((MapView) mapView).repaint();
					return;
				}
				if (propertyName.equals(PRESENTATION_MODE_ENABLED)) {
					MapView.presentationModeEnabled = ResourceController.getResourceController().getBooleanProperty(PRESENTATION_MODE_ENABLED);
					((MapView) mapView).repaint();
					return;
				}

			}
		};
		ResourceController.getResourceController().addPropertyChangeListener(MapView.propertyChangeListener);
	}

	public void deselect(final NodeView newSelected) {
		if (selection.contains(newSelected) && selection.deselect(newSelected)) {
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
		if (isPreparedForPrinting == false)
			return;
		isPreparedForPrinting = false;
		isPrinting = false;
		if (zoom == 1f) {
			getRoot().updateAll();
			synchronized (getTreeLock()) {
				validateTree();
			}
		}
		if (MapView.printOnWhiteBackground) {
			setBackground(background);
		}
	}

	private Point getAnchorCenterPoint() {
		if (! isDisplayable()) {
			return new Point();
		}
		final MainView mainView = anchor.getMainView();
		final int mainViewWidth = mainView.getWidth();
		final int mainViewHeight = mainView.getHeight();
		final Point anchorCenterPoint = new Point((int) (mainViewWidth * anchorHorizontalPoint), (int) (mainViewHeight * anchorVerticalPoint));
		final JViewport parent = (JViewport) getParent();
		UITools.convertPointToAncestor(mainView, anchorCenterPoint, this);
		anchorCenterPoint.x += - parent.getWidth() / 2;
		anchorCenterPoint.y += - parent.getHeight() / 2;
		return anchorCenterPoint;
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.dnd.Autoscroll#getAutoscrollInsets()
	 */
	public Insets getAutoscrollInsets() {
		final Container parent = getParent();
		if (parent == null) {
			return new Insets(0, 0, 0, 0);
		}
		final Rectangle outer = getBounds();
		final Rectangle inner = parent.getBounds();
		return new Insets(inner.y - outer.y + MapView.margin, inner.x - outer.x + MapView.margin, outer.height
		        - inner.height - inner.y + outer.y + MapView.margin, outer.width - inner.width - inner.x + outer.x
		        + MapView.margin);
	}

	public Rectangle getInnerBounds() {
		final Rectangle innerBounds = rootView.getBounds();
		final Rectangle maxBounds = new Rectangle(0, 0, getWidth(), getHeight());
		for (int i = 0; i < arrowLinkViews.size(); ++i) {
			final ILinkView arrowView = arrowLinkViews.get(i);
			arrowView.increaseBounds(innerBounds);
		}
		return innerBounds.intersection(maxBounds);
	}

	public IMapSelection getMapSelection() {
		return new MapSelection();
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

	private NodeView getNodeView(Object o) {
        if(! (o instanceof NodeModel))
			return null;
		final NodeView nodeView = getNodeView((NodeModel)o);
		return nodeView;
    }

	public NodeView getNodeView(final NodeModel node) {
		if (node == null) {
			return null;
		}
		for (INodeView iNodeView : node.getViewers()) {
			if(! (iNodeView instanceof NodeView)){
				continue;
			}
			final NodeView candidateView = (NodeView) iNodeView;
			if (candidateView.getMap() == this) {
				return candidateView;
			}
		}
		NodeView root = getRoot();
		if(root.getModel().equals(node))
			return root;
		else
			return null;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#getPreferredSize()
	 */
	@Override
	public Dimension getPreferredSize() {
		return getLayout().preferredLayoutSize(this);
	}

	public NodeView getRoot() {
		return rootView;
	}

	public NodeView getSelected() {
		if(! selectedsValid) {
			NodeView node = selection.selectedNode;
	        if (node == null || ! SwingUtilities.isDescendingFrom(node, this))
		        validateSelecteds();
            else {
	            final JComponent content = node.getContent();
	            if (content == null || ! content.isVisible())
	                validateSelecteds();
            }
        }
		return selection.selectedNode;
	}

	public Set<NodeModel> getSelectedNodes() {
		validateSelecteds();
		return new AbstractSet<NodeModel>() {

			@Override
			public int size() {
				return selection.size();
			}

			@Override
            public boolean contains(Object o) {
                final NodeView nodeView = getNodeView(o);
                if(nodeView == null)
                	return false;
                return selection.contains(nodeView);
            }

			@Override
            public boolean add(NodeModel o) {
				final NodeView nodeView = getNodeView(o);
				if(nodeView == null)
					return false;
				return selection.add(nodeView);
            }

			@Override
            public boolean remove(Object o) {
				final NodeView nodeView = getNodeView(o);
				if(nodeView == null)
					return false;
				return selection.deselect(nodeView);
            }

			@Override
            public Iterator<NodeModel> iterator() {
				return new Iterator<NodeModel>() {
					final Iterator<NodeView> i = selection.getSelectedSet().iterator();

					public boolean hasNext() {
	                    return i.hasNext();
                    }

					public NodeModel next() {
	                    return i.next().getModel();
                    }

					public void remove() {
	                    i.remove();
                    }

				};
            }
		};
	}

	public List<NodeModel> getOrderedSelectedNodes() {
		validateSelecteds();
		return new AbstractList<NodeModel>(){

			@Override
            public boolean add(NodeModel o) {
				final NodeView nodeView = getNodeView(o);
				if(nodeView == null)
					return false;
				return selection.add(nodeView);
            }



			@Override
            public boolean contains(Object o) {
                final NodeView nodeView = getNodeView(o);
                if(nodeView == null)
                	return false;
                return selection.contains(nodeView);
            }



			@Override
            public boolean remove(Object o) {
				final NodeView nodeView = getNodeView(o);
				if(nodeView == null)
					return false;
				return selection.deselect(nodeView);
            }

			@Override
            public NodeModel get(int index) {
	            return selection.getSelectedList().get(index).getModel();
            }

			@Override
            public int size() {
	            return selection.size();
            }
		};
    }

	/**
	 * @param differentSubtrees
	 * @return an ArrayList of MindMapNode objects. If both ancestor and
	 *         descandant node are selected, only the ancestor ist returned
	 */
	ArrayList<NodeModel> getSelectedNodesSortedByY(final boolean differentSubtrees) {
		validateSelecteds();
		final TreeMap<Integer, LinkedList<NodeModel>> sortedNodes = new TreeMap<Integer, LinkedList<NodeModel>>();
		iteration: for (final NodeView view : selection.getSelectedSet()) {
			if (differentSubtrees) {
				for (Component parent = view.getParent(); parent != null; parent = parent.getParent()) {
					if (selection.getSelectedSet().contains(parent)) {
						continue iteration;
					}
				}
			}
			final Point point = new Point();
			UITools.convertPointToAncestor(view.getParent(), point, this);
			final NodeModel node = view.getModel();
			if(node.getParentNode() != null){
			    point.y += node.getParentNode().getIndex(node);
			}
			LinkedList<NodeModel> nodeList = sortedNodes.get(point.y);
			if (nodeList == null) {
				nodeList = new LinkedList<NodeModel>();
				sortedNodes.put(point.y, nodeList);
			}
			nodeList.add(node);
		}
		final ArrayList<NodeModel> selectedNodes = new ArrayList<NodeModel>();
		for (final LinkedList<NodeModel> nodeList : sortedNodes.values()) {
			for (final NodeModel nodeModel : nodeList) {
				selectedNodes.add(nodeModel);
			}
		}
		return selectedNodes;
	}

	/**
	 * @return
	 */
	public Collection<NodeView> getSelection() {
		validateSelecteds();
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
			if(newSelected == null)
				newSelected = getVisibleSummaryView(oldSelected);
		}
		return newSelected;
	}

	private NodeView getVisibleSummaryView(NodeView node) {
	    if(node.isRoot())
	    	return null;
	    final int currentSummaryLevel = SummaryNode.getSummaryLevel(node.getModel());
		int level = currentSummaryLevel;
		final int requiredSummaryLevel = level + 1;
	    final NodeView parent = node.getParentView();
	    for (int i = 1 + getIndex(node);i < parent.getComponentCount();i++){
	    	final Component component = parent.getComponent(i);
	    	if(! (component instanceof NodeView))
	    		break;
	    	NodeView next = (NodeView) component;
	    	if(next.isLeft() != node.isLeft())
	    		continue;
	    	if(next.isSummary())
	    		level++;
	    	else
	    		level = 0;
	    	if(level == requiredSummaryLevel){
	    		if(next.getModel().isVisible())
	    			return next;
	    		break;
	    	}
	    	if(level == currentSummaryLevel && SummaryNode.isFirstGroupNode(next.getModel()))
	    		break;
	    }
	    return getVisibleSummaryView(parent);
    }

	int getIndex(NodeView node) {
	    final NodeView parent = node.getParentView();
	    for(int i = 0; i < parent.getComponentCount(); i++){
	    	if(parent.getComponent(i).equals(node))
	    		return i;
	    }
	    return -1;
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
			if(newSelected == null)
				newSelected = getVisibleSummaryView(oldSelected);
		}
		return newSelected;
	}

	public float getZoom() {
		return zoom;
	}

	public int getZoomed(final int number) {
		return (int) Math.ceil(number * zoom);
	}

	private void initRoot() {
		this.anchorContentLocation = new Point();
		rootView = NodeViewFactory.getInstance().newNodeView(getModel().getRootNode(), this, this, 0);
		anchor = rootView;
	}

	public boolean isPrinting() {
		return isPrinting;
	}

	public boolean isSelected(final NodeView n) {
		if(isPrinting || (! selectedsValid &&
				(selection.selectedNode == null || ! SwingUtilities.isDescendingFrom(selection.selectedNode, this)  || ! selection.selectedNode.getContent().isVisible())))
			return false;
		return selection.contains(n);
	}

	/**
	 * Add the node to the selection if it is not yet there, making it the
	 * focused selected node.
	 */
	void addSelected(final NodeView newSelected, boolean scroll) {
		selection.add(newSelected);
		if(scroll)
			scrollNodeToVisible(newSelected);
	}

	public void mapChanged(final MapChangeEvent event) {
		final Object property = event.getProperty();
		if (property.equals(MapStyle.RESOURCES_BACKGROUND_COLOR)) {
			setBackground(requiredBackground());
			return;
		}
		if (property.equals(MapStyle.MAP_STYLES)){
	        // set default font for notes:
	        updateContentStyle();
		}
		if (property.equals(MapStyle.MAP_STYLES) && event.getMap().equals(model)
		        || property.equals(ModelessAttributeController.ATTRIBUTE_VIEW_TYPE)
		        || property.equals(Filter.class)
		        || property.equals(UrlManager.MAP_URL)) {
			setBackground(requiredBackground());
			getRoot().updateAll();
			return;
		}
		if(property.equals(AttributeController.SHOW_ICON_FOR_ATTRIBUTES)
				||property.equals(NoteController.SHOW_NOTE_ICONS))
			updateStateIconsRecursively(getRoot());
		if(property.equals(NoteController.SHOW_NOTES_IN_MAP))
			setShowNotes();
	}

    private void updateStateIconsRecursively(NodeView node) {
    	final MainView mainView = node.getMainView();
    	if(mainView == null)
    		return;
		mainView.updateIcons(node);
    	for(int i = 0; i < node.getComponentCount(); i++){
    		final Component component = node.getComponent(i);
    		if(component instanceof NodeView)
    		updateStateIconsRecursively((NodeView) component);
    	}
    }

	private void updateContentStyle() {
        final NodeStyleController style = Controller.getCurrentModeController().getExtension(NodeStyleController.class);
        MapModel map = getModel();
        noteFont = UITools.scale(style.getDefaultFont(map, MapStyleModel.NOTE_STYLE));
        final MapStyleModel model = MapStyleModel.getExtension(map);
        final NodeModel detailStyleNode = model.getStyleNodeSafe(MapStyleModel.DETAILS_STYLE);
        detailFont = UITools.scale(style.getFont(detailStyleNode));
        detailBackground = style.getBackgroundColor(detailStyleNode);
        detailForeground = style.getColor(detailStyleNode);
    }

	public boolean selectLeft(boolean continious) {
		NodeView selected = getSelected();
		NodeView newSelected = getVisibleLeft(selected);
		return selectRightOrLeft(newSelected, continious);
    }

	private boolean selectRightOrLeft(NodeView newSelected, boolean continious) {
	    if (newSelected == null) {
	    	return false;
		}
		if(continious){
			if(newSelected.isParentOf(getSelected())){
				selectAsTheOnlyOneSelected(newSelected);
				addBranchToSelection(newSelected);
			}
			else{
				addBranchToSelection(getSelected());
			}
		}
		else
			selectAsTheOnlyOneSelected(newSelected);
		return true;
    }

	public boolean selectRight(boolean continious) {
		NodeView selected = getSelected();
		NodeView newSelected = getVisibleRight(selected);
		return selectRightOrLeft(newSelected, continious);
    }


	public boolean selectUp(boolean continious) {
		return selectSibling(continious, false, false);
	}

	private boolean selectSibling(boolean continious, boolean page, boolean down) {
		NodeView nextSelected = getSelected();
		do {
			final NodeView nextVisibleSibling = getNextVisibleSibling(nextSelected, down);
			if(nextSelected == null || nextSelected == nextVisibleSibling)
				return false;
			nextSelected = nextVisibleSibling;
		} while (nextSelected.isSelected());
		if(page){
			NodeView sibling = nextSelected;
			for(;;)  {
				sibling = getNextVisibleSibling(sibling, down);
				if(sibling == nextSelected || sibling.getParentView() != nextSelected.getParentView())
					break;
				nextSelected = sibling;
			}
		}
		if(continious){
			selectAsTheOnlyOneSelected(getSelected());
			NodeView node = getSelected();
			do{
				node = getNextVisibleSibling(node, down);
				addSelected(node, false);
			}while(node != nextSelected);
			scrollNodeToVisible(nextSelected);
		}
		else
			selectAsTheOnlyOneSelected(nextSelected);
		return true;
    }

	public NodeView getNextVisibleSibling(NodeView node, boolean down) {
	    return down ? node.getNextVisibleSibling() : node.getPreviousVisibleSibling();
    }

	public boolean selectDown(boolean continious) {
		return selectSibling(continious, false, true);
	}

	public boolean selectPageDown(boolean continious) {
		return selectSibling(continious, true, true);
    }

	public boolean selectPageUp(boolean continious) {
		return selectSibling(continious, true, false);
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
		if(isPrinting == false && isPreparedForPrinting == true){
			isPreparedForPrinting = false;
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					endPrinting();
					repaint();
				}
			});
			return;
		}

		final Graphics2D g2 = (Graphics2D) g.create();
		try {
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			Controller.getCurrentController().getMapViewManager().setTextRenderingHint(g2);
			super.paint(g2);
		}
		finally {
			this.paintingMode = null;
			g2.dispose();
		}
	}

	@Override
	protected void paintChildren(final Graphics g) {
	    final boolean paintLinksBehind = ResourceController.getResourceController().getBooleanProperty(
	    	    "paint_connectors_behind");
	    final PaintingMode paintModes[];
	    if(paintLinksBehind)
	    	paintModes = new PaintingMode[]{
	    		PaintingMode.CLOUDS,
	    		PaintingMode.LINKS, PaintingMode.NODES, PaintingMode.SELECTED_NODES
	    		};
	    else
	    	paintModes = new PaintingMode[]{
	    		PaintingMode.CLOUDS,
	    		PaintingMode.NODES,PaintingMode.SELECTED_NODES, PaintingMode.LINKS
	    		};
	    Graphics2D g2 = (Graphics2D) g;
	    paintChildren(g2, paintModes);
	    if(presentationModeEnabled)
	    	paintDimmer(g2, paintModes);
		paintSelecteds(g2);
		highlightEditor(g2);
    }

	private void paintChildren(Graphics2D g2, final PaintingMode[] paintModes) {
	    for(PaintingMode paintingMode : paintModes){
	    	this.paintingMode = paintingMode;
			switch(paintingMode){
	    		case LINKS:
	    			paintLinks(g2);
	    			break;
	    		default:
	    			super.paintChildren(g2);
	    	}
	    }
    };


	private void paintDimmer(Graphics2D g2, PaintingMode[] paintModes) {
		final Color color = g2.getColor();
		try{
			Color dimmer = new Color(0, 0, 0, transparency);
			g2.setColor(dimmer);
			g2.fillRect(0, 0, getWidth(), getHeight());
		}
		finally{
			g2.setColor(color);
		}
		for (final NodeView selected : getSelection()) {
			highlightSelected(g2, selected, paintModes);
		}
    }

	private void highlightEditor(Graphics2D g2) {
	    final Component editor = getComponent(0);
		if(editor instanceof NodeView)
	    	return;
	    final java.awt.Shape oldClip = g2.getClip();
	    try{
	    	g2.setClip(editor.getX(), editor.getY(), editor.getWidth(), editor.getHeight());
	    	super.paintChildren(g2);
	    }
	    finally{
	    	g2.setClip(oldClip);
	    }

    }

	protected PaintingMode getPaintingMode() {
		return paintingMode;
	}

	private void paintLinks(final Collection<LinkModel> links, final Graphics2D graphics,
	                        final HashSet<ConnectorModel> alreadyPaintedLinks) {
		final Font font = graphics.getFont();
		try {
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
					if (sourceView != null && targetView != null
					        && (Shape.EDGE_LIKE.equals(ref.getShape()) || sourceView.getMap().getLayoutType() == MapViewLayout.OUTLINE)
					        && source.isVisible() && target.isVisible()) {
						arrowLink = new EdgeLinkView(ref, getModeController(), sourceView, targetView);
					}
					else {
						arrowLink = new ConnectorView(ref, sourceView, targetView, getBackground());
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
		final Object renderingHint = getModeController().getController().getMapViewManager().setEdgesRenderingHint(
		    graphics);
		paintLinks(rootView, graphics, new HashSet<ConnectorModel>());
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, renderingHint);
	}

	private void paintLinks(final NodeView source, final Graphics2D graphics,
	                        final HashSet<ConnectorModel> alreadyPaintedLinks) {
		final NodeModel node = source.getModel();
		final Collection<LinkModel> outLinks = NodeLinks.getLinks(node);
		paintLinks(outLinks, graphics, alreadyPaintedLinks);
		final Collection<LinkModel> inLinks = LinkController.getController(getModeController()).getLinksTo(node);
		paintLinks(inLinks, graphics, alreadyPaintedLinks);
		final int nodeViewCount = source.getComponentCount();
		for (int i = 0; i < nodeViewCount; i++) {
			final Component component = source.getComponent(i);
			if (!(component instanceof NodeView)) {
				continue;
			}
			final NodeView child = (NodeView) component;
			if (!isPrinting) {
				final Rectangle bounds = SwingUtilities.convertRectangle(source, child.getBounds(), this);
				final JViewport vp = (JViewport) getParent();
				final Rectangle viewRect = vp.getViewRect();
				viewRect.x -= viewRect.width;
				viewRect.y -= viewRect.height;
				viewRect.width *= 3;
				viewRect.height *= 3;
				if (!viewRect.intersects(bounds)) {
					continue;
				}
			}
			paintLinks(child, graphics, alreadyPaintedLinks);
		}
	}

	private void paintSelecteds(final Graphics2D g) {
		if (!MapView.standardDrawRectangleForSelection || isPrinting()) {
			return;
		}
		final Color c = g.getColor();
		final Stroke s = g.getStroke();
		g.setColor(MapView.standardSelectRectangleColor);
		final Stroke standardSelectionStroke = getStandardSelectionStroke();
		g.setStroke(standardSelectionStroke);
		final Object renderingHint = getModeController().getController().getMapViewManager().setEdgesRenderingHint(g);
		for (final NodeView selected : getSelection()) {
			paintSelectionRectangle(g, selected);
		}
		g.setColor(c);
		g.setStroke(s);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, renderingHint);
	}

	private RoundRectangle2D.Float getRoundRectangleAround(NodeView selected, int gap, int arcw) {
		final JComponent content = selected.getContent();
		final Point contentLocation = new Point();
		UITools.convertPointToAncestor(content, contentLocation, this);
		gap -= 1;
		final RoundRectangle2D.Float roundRectClip = new RoundRectangle2D.Float(
			contentLocation.x - gap, contentLocation.y - gap,
			content.getWidth() + 2 * gap, content.getHeight() + 2 * gap, arcw, arcw);
		return roundRectClip;
	}

	private void paintSelectionRectangle(final Graphics2D g, final NodeView selected) {
		if (selected.getMainView().isEdited()) {
			return;
		}
		final RoundRectangle2D.Float roundRectClip = getRoundRectangleAround(selected, 4, 15);
		g.draw(roundRectClip);
	}

	private void highlightSelected(Graphics2D g, NodeView selected, PaintingMode[] paintedModes) {
		final java.awt.Shape highlightClip;
		if (MapView.standardDrawRectangleForSelection)
			highlightClip = getRoundRectangleAround(selected, 4, 15);
		else
			highlightClip = getRoundRectangleAround(selected, 4, 2);
		final java.awt.Shape oldClip = g.getClip();
		final Rectangle oldClipBounds = g.getClipBounds();
		try{
			g.setClip(highlightClip);
			if(oldClipBounds != null)
				g.clipRect(oldClipBounds.x, oldClipBounds.y, oldClipBounds.width, oldClipBounds.height);
			final Rectangle clipBounds = highlightClip.getBounds();
			final Color color = g.getColor();
			g.setColor(getBackground());
			g.fillRect(clipBounds.x, clipBounds.y, clipBounds.width, clipBounds.height);
			g.setColor(color);
			paintChildren(g, paintedModes);
		}
		finally{
			g.setClip(oldClip);
		}
    }

	Stroke getStandardSelectionStroke() {
	    if (MapView.standardSelectionStroke == null) {
			MapView.standardSelectionStroke = new BasicStroke(2.0f);
		}
		final Stroke standardSelectionStroke = MapView.standardSelectionStroke;
	    return standardSelectionStroke;
    }

	/**
	 * Call preparePrinting() before printing and endPrinting() after printing
	 * to minimize calculation efforts
	 */
	public void preparePrinting() {
		isPrinting = true;
		if (isPreparedForPrinting == false) {
			if (zoom == 1f) {
				getRoot().updateAll();
				synchronized (getTreeLock()) {
					validateTree();
				}
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
			isPrinting = false;
		}
	}

	public void render(Graphics g1, final Rectangle source, final Rectangle target) {
		Graphics2D g = (Graphics2D) g1;
		AffineTransform old = g.getTransform();
		final double scaleX = (0.0 + target.width) / source.width;
		final double scaleY = (0.0 + target.height) / source.height;
		final double zoom;
		if(scaleX < scaleY){
			zoom = scaleX;
		}
		else{
			zoom = scaleY;
		}
		AffineTransform tr2 = new AffineTransform(old);
		tr2.translate(target.getWidth() / 2, target.getHeight() / 2);
		tr2.scale(zoom, zoom);
		tr2.translate(-source.getX()- (source.getWidth() ) / 2, -source.getY()- (source.getHeight()) / 2);
		g.setTransform(tr2);
		final Rectangle clipBounds = g1.getClipBounds();
		g1.clipRect(source.x, source.y, source.width, source.height);
		print(g1);
		g.setTransform(old);
		g1.setClip(clipBounds.x, clipBounds.y, clipBounds.width, clipBounds.height);
	}

	public int print(final Graphics graphics, final PageFormat pageFormat, final int pageIndex) {
		double userZoomFactor = ResourceController.getResourceController().getDoubleProperty("user_zoom", 1);
		userZoomFactor = Math.max(0, userZoomFactor);
		userZoomFactor = Math.min(2, userZoomFactor);
		if (fitMap == FitMap.PAGE && pageIndex > 0) {
			return Printable.NO_SUCH_PAGE;
		}
		final Graphics2D g2 = (Graphics2D) graphics.create();
		preparePrinting();
		final double zoomFactor;
		final double imageableX = pageFormat.getImageableX();
		final double imageableY = pageFormat.getImageableY();
		final double imageableWidth = pageFormat.getImageableWidth();
		final double imageableHeight = pageFormat.getImageableHeight();
		g2.clipRect((int)imageableX, (int)imageableY, (int)imageableWidth, (int)imageableHeight);
		final double mapWidth = boundingRectangle.getWidth();
		final double mapHeight = boundingRectangle.getHeight();
		if (fitMap == FitMap.PAGE) {
			final double zoomFactorX = imageableWidth / mapWidth;
			final double zoomFactorY = imageableHeight / mapHeight;
			zoomFactor = Math.min(zoomFactorX, zoomFactorY) * 0.99;
		}
		else {
			if (fitMap == FitMap.WIDTH) {
				zoomFactor = imageableWidth / mapWidth * 0.99;
			}
			else if (fitMap == FitMap.HEIGHT) {
				zoomFactor = imageableHeight / mapHeight * 0.99;
			}
			else {
				zoomFactor = userZoomFactor / UITools.FONT_SCALE_FACTOR;
			}
			final int nrPagesInWidth = (int) Math.ceil(zoomFactor * mapWidth
				/ imageableWidth);
			final int nrPagesInHeight = (int) Math.ceil(zoomFactor * mapHeight
				/ imageableHeight);
			if (pageIndex >= nrPagesInWidth * nrPagesInHeight) {
				return Printable.NO_SUCH_PAGE;
			}
			final int yPageCoord = (int) Math.floor(pageIndex / nrPagesInWidth);
			final int xPageCoord = pageIndex - yPageCoord * nrPagesInWidth;
			g2.translate(-imageableWidth * xPageCoord, -imageableHeight * yPageCoord);
		}
		g2.translate(imageableX, imageableY);
		g2.scale(zoomFactor, zoomFactor);
		final double mapX = boundingRectangle.getX();
		final double mapY = boundingRectangle.getY();
		g2.translate(-mapX, -mapY);
		print(g2);
		g2.dispose();
		return Printable.PAGE_EXISTS;
	}

	private void repaintSelecteds() {
		for (final NodeView selected : getSelection()) {
			selected.repaintSelected();
		}
	}

	private Color requiredBackground() {
		final MapStyle mapStyle = getModeController().getExtension(MapStyle.class);
		final Color mapBackground = mapStyle.getBackground(model);
		return mapBackground;
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
		if (nodeToBeCentered != null) {
			if (node != nodeToBeCentered) {
				centerNode(node, false);
			}
			return;
		}
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
		if(! newSelected.getModel().isVisible())
			throw new AssertionError("select invisible node");
		if (ResourceController.getResourceController().getBooleanProperty("center_selected_node")) {
			centerNode(newSelected, ResourceController.getResourceController().getBooleanProperty("slow_scroll_selected_node"));
		}
		else {
			scrollNodeToVisible(newSelected);
		}
		selectAsTheOnlyOneSelected(newSelected, true);
		setSiblingMaxLevel(newSelected.getModel().getNodeLevel(false));
	}

	public void selectAsTheOnlyOneSelected(final NodeView newSelected, final boolean requestFocus) {
		if (requestFocus) {
			newSelected.requestFocusInWindow();
		}
		scrollNodeToVisible(newSelected);
		if(selection.size() == 1 && getSelected().equals(newSelected)){
			return;
		}
		final NodeView[] oldSelecteds = selection.toArray();
		selection.select(newSelected);
		if (newSelected.getModel().getParentNode() != null) {
			((NodeView) newSelected.getParent()).setPreferredChild(newSelected);
		}
		newSelected.repaintSelected();
		for (final NodeView oldSelected : oldSelecteds) {
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
	private void addBranchToSelection(final NodeView newlySelectedNodeView) {
		if (newlySelectedNodeView.isContentVisible()) {
			addSelected(newlySelectedNodeView, false);
		}
		for (final NodeView target : newlySelectedNodeView.getChildrenViews()) {
			addBranchToSelection(target);
		}
	}

	void selectContinuous(final NodeView newSelected) {
		if(newSelected.isRoot()){
			selection.add(newSelected);
			scrollNodeToVisible(newSelected);
			return;
		}
		final NodeView parentView = newSelected.getParentView();
		final boolean isLeft = newSelected.isLeft();
		final NodeModel parent = parentView.getModel();
		final int newIndex = parent.getIndex(newSelected.getModel());
		int indexGapAbove = Integer.MAX_VALUE;
		int indexGapBelow = Integer.MIN_VALUE;
		final LinkedList<NodeView> childrenViews = parentView.getChildrenViews();
		for(NodeView sibling : childrenViews){
			if(newSelected == sibling || sibling.isLeft() != isLeft || ! sibling.isSelected())
				continue;
			final int index2 = parent.getIndex(sibling.getModel());
			final int indexGap = newIndex - index2;
			if(indexGap > 0){
				if(indexGap < indexGapAbove){
					indexGapAbove = indexGap;
				}
			}
			else if(indexGapAbove == Integer.MAX_VALUE){
				if(indexGap > indexGapBelow){
					indexGapBelow = indexGap;
				}
			}
		}
		if(indexGapAbove == Integer.MAX_VALUE && indexGapBelow == Integer.MIN_VALUE){
			selection.add(newSelected);
			scrollNodeToVisible(newSelected);
			return;
		}
		NodeView lastSelected = newSelected;
		for(NodeView sibling : childrenViews){
			if(sibling.isLeft() != isLeft)
				continue;
			final int index2 = parent.getIndex(sibling.getModel());
			final int indexGap = newIndex - index2;
			if(indexGap >= 0 && indexGapAbove < Integer.MAX_VALUE && indexGap < indexGapAbove
					|| indexGap <= 0 && indexGapAbove == Integer.MAX_VALUE && indexGapBelow > Integer.MIN_VALUE  && indexGap > indexGapBelow){
				selection.add(sibling);
				lastSelected = sibling;
			}
		}
		scrollNodeToVisible(lastSelected);
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

	private void scrollView() {
		if(nodeToBeCentered != null){
			centerNode(nodeToBeCentered, slowScroll);
			return;
		}
		if (anchorContentLocation.getX() == 0 && anchorContentLocation.getY() == 0) {
			return;
		}
		final JViewport vp = (JViewport) getParent();
		final Point viewPosition = vp.getViewPosition();
		final Point oldAnchorContentLocation = anchorContentLocation;
		final Point newAnchorContentLocation = getAnchorCenterPoint();
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
		anchor = getRoot();
		anchorHorizontalPoint = anchorVerticalPoint = 0;
		this.anchorContentLocation = getAnchorCenterPoint();
	}

	public void setZoom(final float zoom) {
		this.zoom = zoom;
		anchorToSelected(getSelected(), CENTER_ALIGNMENT, CENTER_ALIGNMENT);
		getRoot().updateAll();
		revalidate();
	}

	/**
	 * Add the node to the selection if it is not yet there, remove it
	 * otherwise.
	 * @param requestFocus
	 */
	private void toggleSelected(final NodeView nodeView) {
		if (isSelected(nodeView)) {
			if(selection.size() > 1)
				selection.deselect(nodeView);
		}
		else {
			selection.add(nodeView);
			scrollNodeToVisible(nodeView);
		}
	}

	private void validateSelecteds() {
		if (selectedsValid) {
			return;
		}
		selectedsValid = true;
		final NodeView selectedView = getSelected();
		if(selectedView == null){
			final NodeView root = getRoot();
			selectAsTheOnlyOneSelected(root);
			centerNode(root, false);
			return;
		}
		final NodeModel selectedNode = selectedView.getModel();
		final ArrayList<NodeView> selectedNodes = new ArrayList<NodeView>(getSelection().size());
		for (final NodeView nodeView : getSelection()) {
			if (nodeView != null) {
				selectedNodes.add(nodeView);
			}
		}
		selection.clear();
		for (final NodeView nodeView : selectedNodes) {
			if (nodeView.isContentVisible()) {
				selection.add(nodeView);
			}
		}
		if (getSelected() != null) {
			return;
        }
		for(NodeModel node = selectedNode.getParentNode(); node != null; node = node.getParentNode()){
			final NodeView newNodeView = getNodeView(node);
			if(newNodeView != null && newNodeView.isContentVisible() ){
				selectAsTheOnlyOneSelected(newNodeView);
				return;
			}
		}
		selectAsTheOnlyOneSelected(getRoot());
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.Container#validateTree()
	 */
	@Override
	protected void validateTree() {
		validateSelecteds();
		getRoot().validateTree();
		super.validateTree();
	}

	public void onPreNodeMoved(final NodeModel oldParent, final int oldIndex, final NodeModel newParent,
	                           final NodeModel child, final int newIndex) {
	}

	public void repaintVisible() {
		final JViewport vp = (JViewport) getParent();
		repaint(vp.getViewRect());
	}

	public void propertyChanged(String propertyName, String newValue, String oldValue) {
		if(propertyName.equals(TextController.MARK_TRANSFORMED_TEXT))
			UITools.repaintAll(getRoot());
	}

	public void selectVisibleAncestorOrSelf(NodeView preferred) {
		while(! preferred.getModel().isVisible())
			preferred = preferred.getParentView();
		selectAsTheOnlyOneSelected(preferred);
    }

    public Font getDefaultNoteFont() {
        return noteFont;
    }

    public Font getDetailFont() {
        return detailFont;
    }

    public Color getDetailForeground() {
        return detailForeground;
    }

    public Color getDetailBackground() {
        return detailBackground;
    }

	private boolean isSelected() {
	    return Controller.getCurrentController().getMapViewManager().getMapViewComponent() == MapView.this;
    }

	void selectIfSelectionIsEmpty(NodeView nodeView) {
		if(selection.selectedNode == null)
			selectAsTheOnlyOneSelected(nodeView);
    }

	public static MapView getMapView(final Component component) {
    	if(component instanceof MapView)
    		return (MapView) component;
    	return (MapView) SwingUtilities.getAncestorOfClass(MapView.class, component);
    }

	public void select() {
		getModeController().getController().getMapViewManager().changeToMapView(this);
    }

	@Override
    public void setSize(int width, int height) {
		super.setSize(width, height);
	    validate();
		if(isDisplayable())
	    	scrollView();
    }

	@Override
    public void setLocation(int x, int y) {
	    if(isValid()){
	    	super.setLocation(x, y);
	    	this.anchorContentLocation = getAnchorCenterPoint();
	    }
    }
}
