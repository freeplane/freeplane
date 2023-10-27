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

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.Window;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.freeplane.api.ChildNodesAlignment;
import org.freeplane.api.ChildNodesLayout;
import org.freeplane.api.ChildrenSides;
import org.freeplane.api.Dash;
import org.freeplane.api.LayoutOrientation;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.IUserInputListenerFactory;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.ObjectRule;
import org.freeplane.features.attribute.AttributeController;
import org.freeplane.features.attribute.NodeAttributeTableModel;
import org.freeplane.features.cloud.CloudController;
import org.freeplane.features.cloud.CloudModel;
import org.freeplane.features.edge.EdgeController;
import org.freeplane.features.edge.EdgeController.Rules;
import org.freeplane.features.edge.EdgeStyle;
import org.freeplane.features.filter.Filter;
import org.freeplane.features.filter.FilterController;
import org.freeplane.features.filter.hidden.NodeVisibility;
import org.freeplane.features.filter.hidden.NodeVisibilityConfiguration;
import org.freeplane.features.highlight.HighlightController;
import org.freeplane.features.highlight.NodeHighlighter;
import org.freeplane.features.icon.hierarchicalicons.HierarchicalIcons;
import org.freeplane.features.layout.LayoutController;
import org.freeplane.features.map.EncryptionModel;
import org.freeplane.features.map.FreeNode;
import org.freeplane.features.map.HistoryInformationModel;
import org.freeplane.features.map.INodeView;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeDeletionEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.NodeModel.NodeChangeType;
import org.freeplane.features.map.NodeModel.Side;
import org.freeplane.features.map.SummaryNode;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.nodelocation.LocationController;
import org.freeplane.features.nodelocation.LocationModel;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.nodestyle.NodeStyleShape;
import org.freeplane.features.styles.LogicalStyleController.StyleOption;
import org.freeplane.features.styles.MapViewLayout;
import org.freeplane.features.text.TextController;
import org.freeplane.view.swing.map.attribute.AttributeView;
import org.freeplane.view.swing.map.cloud.CloudView;
import org.freeplane.view.swing.map.cloud.CloudViewFactory;
import org.freeplane.view.swing.map.edge.AutomaticEdgeStyle;
import org.freeplane.view.swing.map.edge.EdgeView;
import org.freeplane.view.swing.map.edge.EdgeViewFactory;

/**
 * This class represents a single Node of a MindMap (in analogy to
 * TreeCellRenderer).
 */
public class NodeView extends JComponent implements INodeView {
	static final String DEBUG_INFO_PROPERTY = "debugInfo";
	private static final int HIGHLIGHTED_NODE_ARC_MARGIN = 4;
	final static int ALIGN_BOTTOM = -1;
	final static int ALIGN_CENTER = 0;
	final static int ALIGN_TOP = 1;
	protected final static Color dragColor = Color.lightGray;
	private static final long serialVersionUID = 1L;
	static final int SPACE_AROUND = 50;
	public static final int MAIN_VIEWER_POSITION = 1;
	public static final int NOTE_VIEWER_POSITION = 10;
	final static boolean PAINT_DEBUG_INFO;
    public static int ADDITIONAL_MOUSE_SENSITIVE_AREA = 50;
    public static final int DETAIL_VIEWER_POSITION = 2;

	static {
		boolean paintDebugInfo = false;
		try{
			paintDebugInfo = Boolean.getBoolean("org.freeplane.view.swing.map.NodeView.PAINT_DEBUG_INFO");
		}
		catch(Exception e){/**/}
		PAINT_DEBUG_INFO = paintDebugInfo;
	}
	static private int maxToolTipWidth;
	private AttributeView attributeView;
	private JComponent contentPane;
	private MainView mainView;
	private final MapView map;
	private NodeModel viewedNode;
	private NodeView lastSelectedChild;
	private EdgeStyle edgeStyle = EdgeStyle.EDGESTYLE_HIDDEN;
	private Integer edgeWidth = 1;
	private ObjectRule<Color, Rules> edgeColor = null;
	private Color modelBackgroundColor;

	private boolean isFolded;
	private Dash edgeDash = Dash.DEFAULT;
	private final NodeViewLayoutHelper layoutHelper;
    private Side side;
    private ChildNodesAlignment childNodesAlignment;
    private ChildNodesLayout childNodesLayout;
    private LayoutOrientation layoutOrientation;
    private ChildrenSides childrenSides;

	protected NodeView(final NodeModel viewedNode, final MapView map) {
		setFocusCycleRoot(true);
		this.viewedNode = viewedNode;
		this.map = map;
		this.isFolded = map.getModeController().getMapController().isFolded(viewedNode);
		this.layoutHelper = new NodeViewLayoutHelper(this);
	}



	public boolean isFolded(){
		return isFolded && ! isRoot();
	}

	void addDragListener(final DragGestureListener dgl) {
		if (dgl == null) {
			return;
		}
		final DragSource dragSource = DragSource.getDefaultDragSource();
		dragSource.createDefaultDragGestureRecognizer(getMainView(), DnDConstants.ACTION_COPY
		        | DnDConstants.ACTION_MOVE | DnDConstants.ACTION_LINK, dgl);
	}

	void addDropListener(final DropTargetListener dtl) {
		if (dtl == null) {
			return;
		}
		final DropTarget dropTarget = new DropTarget(getMainView(), dtl);
		dropTarget.setActive(true);
	}

	private int calcShiftY(final LocationModel locationModel) {
		try {
			final NodeModel parent = viewedNode.getParentNode();
			Filter filter = map.getFilter();
			int singleChildShift = ! getParentView().isSummary() && getModeController().hasOneVisibleChild(parent, filter)
			        ? getMainView().getSingleChildShift() : 0;
            return locationModel.getShiftY().toBaseUnitsRounded() + singleChildShift;
		}
		catch (final NullPointerException e) {
			return 0;
		}
	}

	@Override
	public boolean contains(final int x, final int y) {
		final int space = map.getZoomed(NodeView.SPACE_AROUND);
		final int reducedSpace = space - ADDITIONAL_MOUSE_SENSITIVE_AREA;
		if (x >= reducedSpace && x < getWidth() - reducedSpace && y >= reducedSpace && y < getHeight() - reducedSpace){
			for(int i = getComponentCount()-1; i >= 0; i--){
				final Component comp = getComponent(i);
				if(comp.isVisible() && comp.contains(x-comp.getX(), y-comp.getY()))
					return true;
			}
		}
		return false;
	}

	protected void convertPointToMap(final Point p) {
		UITools.convertPointToAncestor(this, p, map);
	}

	public void createAttributeView() {
		if (attributeView == null && NodeAttributeTableModel.getModel(viewedNode) != NodeAttributeTableModel.EMTPY_ATTRIBUTES) {
			attributeView = new AttributeView(this, true);
		}
		syncronizeAttributeView();
	}

	public boolean focused() {
		return mainView.hasFocus();
	}

	public AttributeView getAttributeView() {
		if (attributeView == null) {
			AttributeController.getController(getModeController()).createAttributeTableModel(viewedNode);
			attributeView = new AttributeView(this, true);
		}
		return attributeView;
	}

	public Color getBackgroundColor() {
		final Color cloudColor = getCloudColor();
		if (cloudColor != null) {
			return cloudColor;
		}
		final NodeView parentView = getParentView();
		if (parentView == null) {
			return map.getBackground();
		}
		return parentView.getBackgroundColor();
	}

	public Color getCloudColor() {
	    final CloudModel cloudModel = getCloudModel();
		if(cloudModel != null){
			final Color cloudColor = cloudModel.getColor();
			return cloudColor;
		}
		return null;
    }

	/**
	 * This method returns the NodeViews that are children of this node.
	 */
	public LinkedList<NodeView> getChildrenViews() {
		final LinkedList<NodeView> childrenViews = new LinkedList<NodeView>();
		final Component[] components = getComponents();
		for (int i = 0; i < components.length; i++) {
			if (!(components[i] instanceof NodeView)) {
				continue;
			}
			final NodeView view = (NodeView) components[i];
			childrenViews.add(view);
		}
		return childrenViews;
	}

	public JComponent getContent() {
		final JComponent c = contentPane == null ? mainView : contentPane;
		assert (c == null || c.getParent() == this);
		return c;
	}

	private JComponent getContentPane() {
		if (contentPane == null) {
			Window windowAncestor = SwingUtilities.getWindowAncestor(mainView);
			boolean hasFocus = windowAncestor != null && windowAncestor.getMostRecentFocusOwner() == mainView;
			contentPane = NodeViewFactory.getInstance().newContentPane(this);
			final int index = getComponentCount() - 1;
			remove(index);
			contentPane.add(mainView);
			mainView.putClientProperty("NODE_VIEW_CONTENT_POSITION", MAIN_VIEWER_POSITION);
			if(! mainView.isVisible())
				mainView.setVisible(true);
			add(contentPane, index);
			if(hasFocus)
				restoreFocusToMainView();
		}
		return contentPane;
	}

	private void restoreFocusToMainView() {
		final Window windowAncestor = SwingUtilities.getWindowAncestor(mainView);
		if(windowAncestor.isFocused())
			mainView.requestFocusInWindow();
		else
			windowAncestor.addWindowFocusListener(new WindowFocusListener() {
				@Override
				public void windowLostFocus(WindowEvent e) {/**/}

				@Override
				public void windowGainedFocus(WindowEvent e) {
					mainView.requestFocusInWindow();
					windowAncestor.removeWindowFocusListener(this);
				}
			});
	}

	/**
	 * Returns the coordinates occupied by the node and its children as a vector
	 * of four point per node.
	 */
	public void getCoordinates(final LinkedList<Point> inList) {
		getCoordinates(inList, 0, false, 0, 0);
	}

	private void getCoordinates(final LinkedList<Point> inList, int additionalDistanceForConvexHull,
	                            final boolean byChildren, final int transX, final int transY) {
		if (!isVisible()) {
			return;
		}
		if (isContentVisible()) {
			if (byChildren) {
				final ModeController modeController = getModeController();
				final CloudController cloudController = CloudController.getController(modeController);
				final CloudModel cloud = cloudController.getCloud(getNode(), getStyleOption());
				if (cloud != null) {
					additionalDistanceForConvexHull += CloudView.getAdditionalHeigth(cloud, this) / 5;
				}
			}

			Rectangle foldingRectangleBounds = getMainView().getFoldingRectangleBounds(this, false);
			JComponent content = getContent();
            int extraFoldingRectangleLeftWidth = - Math.min(0, foldingRectangleBounds.x);
            final int x = transX + content.getX() - extraFoldingRectangleLeftWidth;
			int extraFoldingRectangleTopHeight = - Math.min(0, foldingRectangleBounds.y);
            final int y = transY + content.getY() - extraFoldingRectangleTopHeight;
			final int width = extraFoldingRectangleLeftWidth + Math.max(content.getWidth(), foldingRectangleBounds.x + foldingRectangleBounds.width);
			final int height = extraFoldingRectangleTopHeight + Math.max(content.getHeight(), foldingRectangleBounds.y + foldingRectangleBounds.height);
			inList.addLast(new Point(-additionalDistanceForConvexHull + x, -additionalDistanceForConvexHull + y));
			inList
			    .addLast(new Point(-additionalDistanceForConvexHull + x, additionalDistanceForConvexHull + y + height));
			inList.addLast(new Point(additionalDistanceForConvexHull + x + width, additionalDistanceForConvexHull + y
			        + height));
			inList
			    .addLast(new Point(additionalDistanceForConvexHull + x + width, -additionalDistanceForConvexHull + y));
		}
		for (final NodeView child : getChildrenViews()) {
			child.getCoordinates(inList, additionalDistanceForConvexHull, true, transX + child.getX(),
			    transY + child.getY());
		}
	}

	private NodeView getFirst(Component startAfter, final boolean leftOnly, final boolean rightOnly) {
		final Component[] components = getComponents();
		for (int i = 0; i < components.length; i++) {
			if (startAfter != null) {
				if (components[i] == startAfter) {
					startAfter = null;
				}
				continue;
			}
			if (!(components[i] instanceof NodeView)) {
				continue;
			}
			final NodeView view = (NodeView) components[i];
			if (leftOnly && !view.isTopOrLeft() || rightOnly && view.isTopOrLeft()) {
				continue;
			}
			if (view.isContentVisible()) {
				return view;
			}
			if(! view.isSummary()) {
				final NodeView child = view.getFirst(null, leftOnly, rightOnly);
				if (child != null) {
					return child;
				}
			}
		}
		return null;
	}

	int getHGap() {
	    final double modelGap = LocationModel.getModel(viewedNode).getHGap().toBaseUnits();
	    NodeView parentView = getParentView();
	    final double unscaledHGap;
	    if(parentView == null) {
	        unscaledHGap = modelGap;
	    }
	    else {
	        ChildNodesAlignment childNodesAlignment = parentView.getChildNodesAlignment();
	        ChildrenSides childrenSides = parentView.childrenSides();
	        boolean reduce = childNodesAlignment.isStacked() && (childrenSides == ChildrenSides.BOTH_SIDES || parentView.usesHorizontalLayout());
	        if (reduce) {
	            if(parentView.usesHorizontalLayout()) {
	                if(childrenSides != ChildrenSides.BOTH_SIDES)
	                    unscaledHGap = modelGap - LocationModel.DEFAULT_HGAP_PX + LocationModel.DEFAULT_VGAP_PX;
	                else
	                    unscaledHGap = modelGap - LocationModel.DEFAULT_HGAP_PX + 2 * LocationModel.DEFAULT_VGAP_PX;
                } else
                    unscaledHGap = modelGap - LocationModel.DEFAULT_HGAP_PX * (1. / 2.);
            } else
                unscaledHGap = modelGap;
	    }
	    return map.getZoomed(unscaledHGap);
	}

	private NodeView getLast(Component startBefore, final boolean leftOnly, final boolean rightOnly) {
		final Component[] components = getComponents();
		for (int i = components.length - 1; i >= 0; i--) {
			if (startBefore != null) {
				if (components[i] == startBefore) {
					startBefore = null;
				}
				continue;
			}
			if (!(components[i] instanceof NodeView)) {
				continue;
			}
			final NodeView view = (NodeView) components[i];
			if (leftOnly && !view.isTopOrLeft() || rightOnly && view.isTopOrLeft()) {
				continue;
			}
			if (view.isContentVisible()) {
				return view;
			}
			if(! view.isSummary()) {
				final NodeView child = view.getLast(null, leftOnly, rightOnly);
				if (child != null) {
					return child;
				}
			}
		}
		return null;
	}

	LinkedList<NodeView> getLeft() {
		final LinkedList<NodeView> left = new LinkedList<NodeView>();
		for (final NodeView node : getChildrenViews()) {
			if (node == null) {
				continue;
			}
			if (node.isTopOrLeft()) {
				left.add(node);
			}
		}
		return left;
	}

	/**
	 * Returns the Point where the Links should arrive the Node.
	 */
	public Point getLinkPoint(final Point declination) {
		int x, y;
		Point linkPoint;
		if (declination != null) {
			x = map.getZoomed(declination.x);
			y = map.getZoomed(declination.y);
		}
		else {
			x = 1;
			y = 0;
		}
		if (isTopOrLeft()) {
			x = -x;
		}
		if (y != 0 && getContent().getHeight() > 0) {
			final double ctgRect = Math.abs((double) getContent().getWidth() / getContent().getHeight());
			final double ctgLine = Math.abs((double) x / y);
			int absLinkX, absLinkY;
			if (ctgRect >= ctgLine || x == 0) {
				absLinkX = Math.abs(x * getContent().getHeight() / (2 * y));
				absLinkY = getContent().getHeight() / 2;
			}
			else {
				absLinkX = getContent().getWidth() / 2;
				absLinkY = Math.abs(y * getContent().getWidth() / (2 * x));
			}
			linkPoint = new Point(getContent().getWidth() / 2 + (x > 0 ? absLinkX : -absLinkX), getContent()
			    .getHeight() / 2 + (y > 0 ? absLinkY : -absLinkY));
		}
		else {
			linkPoint = new Point((x > 0 ? getContent().getWidth() : 0), (getContent().getHeight() / 2));
		}
		linkPoint.translate(getContent().getX(), getContent().getY());
		convertPointToMap(linkPoint);
		return linkPoint;
	}

	public MainView getMainView() {
		return mainView;
	}

    public Point getRelativeLocation(NodeView target) {
        Component component;
        int targetX = 0;
        int targetY = 0;
        for(component = target.getMainView();
            !(this.equals(component) || component.getClass().equals(MapView.class));
            component = component.getParent()){
            targetX += component.getX();
            targetY += component.getY();
        }
        Point relativeLocation = new Point();
        UITools.convertPointToAncestor(mainView, relativeLocation, component);
        relativeLocation.x = targetX - relativeLocation.x;
        relativeLocation.y = targetY - relativeLocation.y;
        return relativeLocation;
    }

	public MapView getMap() {
		return map;
	}

	public int getMaxToolTipWidth() {
		if (maxToolTipWidth == 0) {
			try {
				maxToolTipWidth = ResourceController.getResourceController().getIntProperty(
				    "toolTipManager.max_tooltip_width", 600);
			}
			catch (final NumberFormatException e) {
				maxToolTipWidth = 600;
			}
		}
		return maxToolTipWidth;
	}

	public NodeModel getNode() {
		return viewedNode;
	}

	private NodeView getNextSiblingSameParent() {
		LinkedList<NodeView> v = getSiblingViews();
		final int index = v.indexOf(this);
		boolean isOutlineLayoutSet = map.isOutlineLayoutSet();
        boolean skipUntilSummaryEnd = ! isOutlineLayoutSet && isSummary();
		for (int i = index + 1; i < v.size(); i++) {
			final NodeView nextView = v.get(i);
			if(this.isTopOrLeft() != nextView.isTopOrLeft())
			    continue;
			if(skipUntilSummaryEnd && nextView.isSummary())
				break;
			skipUntilSummaryEnd = false;
			final NodeModel node = nextView.getNode();
			if (node.hasVisibleContent(map.getFilter())) {
				return nextView;
			}
			else if (isOutlineLayoutSet || ! node.isHiddenSummary()){
				final NodeView first = nextView.getFirst(null, this.isTopOrLeft(),
		                !this.isTopOrLeft());
				if (first != null) {
					return first;
				}
			}
		}
		return this;
	}

	NodeView getNextVisibleSibling(LayoutOrientation requiredLayoutOrientation) {
	    NodeView sibling = this;
	    NodeView lastSibling = this;
	    NodeView parentView = getParentView();
	    while (sibling != map.getRoot()) {
	        lastSibling = sibling;
	        LayoutOrientation parentLayoutOrientation = parentView.layoutOrientation();
            if (requiredLayoutOrientation == parentLayoutOrientation) {
	            sibling = sibling.getNextSiblingSameParent();
	            if (sibling != lastSibling) {
	                break;
	            }
	        }
			sibling = parentView;
			parentView = parentView.getParentView();
		}
	    if(sibling.getChildNodesAlignment().isStacked() && ! sibling.usesHorizontalLayout())
	        return sibling;
		while (sibling.getNode().getNodeLevel(map.getFilter()) < map.getSiblingMaxLevel()
		        && sibling.layoutOrientation() == requiredLayoutOrientation) {
			final NodeView first = sibling.getFirst(sibling.isRoot() ? lastSibling : null,
			        this.isTopOrLeft(), !this.isTopOrLeft());
			if (first == null) {
				break;
			}
			sibling = first;
		}
		if (sibling.isRoot() ) {
			return this;
		}
		return sibling;
	}

	public NodeView getParentView() {
		final Container parent = getParent();
		if (parent instanceof NodeView) {
			return (NodeView) parent;
		}
		return null;
	}

    public NodeView getParentNodeView() {
        NodeView parent = getParentView();
        if (parent != null)
            return parent;
        NodeModel parentNode = viewedNode.getParentNode();
        if(parentNode == null)
            return null;
        return getMap().getNodeView(parentNode);
    }

    enum PreferredChild {
        LAST_SELECTED, FIRST, LAST, NEAREST_SIBLING
    }

    NodeView getPreferredVisibleChild(final PreferredChild preferredChild, final ChildrenSides sides) {
        return getPreferredVisibleChild(preferredChild, sides, this);
    }

    private NodeView getPreferredVisibleChild(final PreferredChild preferredChild, final ChildrenSides sides, NodeView nearest) {
		if (getNode().isLeaf()) {
			return null;
		}
		if (preferredChild == PreferredChild.LAST_SELECTED
		        && lastSelectedChild != null
		        && lastSelectedChild.getParent() == this && (sides.matches(lastSelectedChild.isTopOrLeft()))) {
			if (lastSelectedChild.isContentVisible()) {
				return lastSelectedChild;
			}
			else {
				final NodeView newSelected = lastSelectedChild.getPreferredVisibleChild(preferredChild, ChildrenSides.BOTH_SIDES, nearest);
				if (newSelected != null) {
					return newSelected;
				}
			}
		}
		return selectNearest(preferredChild, sides, nearest);
	}



    NodeView selectNearest(final PreferredChild preferredChild, final ChildrenSides sides,
            NodeView ancor) {
        int distance = Integer.MAX_VALUE;
		final NodeView baseComponent = (isContentVisible() || isSummary()) && preferredChild != PreferredChild.NEAREST_SIBLING || getParentView() == null ? this : getAncestorWithVisibleContent();
		NodeView newSelected = null;
		JComponent ancorContent = ancor.getContent();
        final Point ownPoint = baseComponent.calculateCentralPoint(ancorContent);
		for (int i = 0; i < getComponentCount(); i++) {
			final Component c = getComponent(i);
			if (!(c instanceof NodeView) || c == ancor) {
			    continue;
			}
			NodeView childView = (NodeView) c;
			boolean isChildTopOrLeft = childView.isTopOrLeft();
			if (! sides.matches(isChildTopOrLeft)
			        || sides == ChildrenSides.BOTH_SIDES
			        && lastSelectedChild != null && lastSelectedChild.getParent() == this && (lastSelectedChild.isTopOrLeft() != isChildTopOrLeft)) {
			    continue;
			}
			if (!childView.isContentVisible()) {
			    if(childView.isSummary() && newSelected !=  null && ! map.isOutlineLayoutSet())
			        continue;
			    childView = childView.getPreferredVisibleChild(preferredChild, ChildrenSides.BOTH_SIDES, ancor);
			    if (childView == null) {
			        continue;
			    }
			}
			if (preferredChild == PreferredChild.FIRST) {
			    return childView;
			}
			else if(preferredChild == PreferredChild.LAST) {
			    newSelected = childView;
			    continue;
			}
			final JComponent childContent = childView.getContent();
			if(childContent == null)
			    continue;
			final Point childPoint = baseComponent.calculateCentralPoint(childContent);
			if(baseComponent.usesHorizontalLayout()) {
			    int distanceToBorder = (ancorContent.getHeight() + childContent.getHeight())/2;
                if(childView.isTopOrLeft())
			        childPoint.y += distanceToBorder;
			    else
			        childPoint.y -= distanceToBorder;
			}
			else {
			    int distanceToBorder = (ancorContent.getWidth() + childContent.getWidth())/2;
                if(childView.isTopOrLeft())
			        childPoint.x += distanceToBorder;
			    else
			        childPoint.x -= distanceToBorder;

			}
			final int dx = childPoint.x - ownPoint.x;
			final int dy = childPoint.y - ownPoint.y;
			final int gapToChild = baseComponent.usesHorizontalLayout()
			        ? dy*dy + 2 * dx*dx
			        : 2 * dy*dy + dx*dx;
			if (gapToChild < distance) {
			    newSelected = childView;
			    distance = gapToChild;
			}
			else {
			    break;
			}
		}
		return newSelected;
    }



    private Point calculateCentralPoint(final JComponent c) {
        final Point childPoint = new Point(c.getWidth()/2, c.getHeight() / 2);
        UITools.convertPointToAncestor(c, childPoint, this);
        return childPoint;
    }

	private NodeView getPreviousSiblingSameParent() {
		LinkedList<NodeView> v = getSiblingViews();
		final int index = v.indexOf(this);
		boolean skipUntilFirstGroupNode = ! map.isOutlineLayoutSet() && isSummary();
		for (int i = index - 1; i >= 0; i--) {
			final NodeView nextView = v.get(i);
 			if(skipUntilFirstGroupNode) {
 				skipUntilFirstGroupNode = !nextView.isFirstGroupNode();
 				continue;
 			}
            if(this.isTopOrLeft() != nextView.isTopOrLeft())
                continue;
			final NodeModel node = nextView.getNode();
			if (node.hasVisibleContent(map.getFilter())) {
				return nextView;
			}
			else if (! node.isHiddenSummary()){
				final NodeView last = nextView.getLast(null, this.isTopOrLeft(),
		                !this.isTopOrLeft());
				if (last != null) {
					return last;
				}
			}
		}
		return this;
	}

	protected LinkedList<NodeView> getSiblingViews() {
		LinkedList<NodeView> v = null;
		final NodeView parentView = getParentView();
		if (parentView == null){
			UITools.errorMessage("unexpected error: node " + getMainView().getText() + " has lost its parent ");
			return null;
		}
		if (parentView.isRoot()) {
			if (this.isTopOrLeft()) {
				v = parentView.getLeft();
			}
			else {
				v = parentView.getRight();
			}
		}
		else {
			v = parentView.getChildrenViews();
		}
		return v;
	}

	NodeView getPreviousVisibleSibling(LayoutOrientation requiredLayoutOrientation) {
	    NodeView sibling = this;
	    NodeView previousSibling = this;
	    NodeView parentView = getParentView();
	    boolean parentUsesHorizontalLayout = parentView.usesHorizontalLayout();
	    while(parentView != null) {
	        previousSibling = sibling;
	        if (requiredLayoutOrientation == parentView.layoutOrientation()) {
	            sibling = sibling.getPreviousSiblingSameParent();
	            if (sibling != previousSibling) {
	                break;
	            }
	        }
	        sibling = parentView;
	        parentView = parentView.getParentView();
		}
	    if((parentView != null ? parentView : sibling).layoutOrientation() != requiredLayoutOrientation)
	        return this;
        if(sibling.getChildNodesAlignment().isStacked() && ! sibling.usesHorizontalLayout())
            return sibling;
        while (sibling.getNode().getNodeLevel(map.getFilter()) < map.getSiblingMaxLevel()
                && sibling.usesHorizontalLayout() == parentUsesHorizontalLayout) {
			final NodeView last = sibling.getLast(sibling.isRoot() ? previousSibling : null, this.isTopOrLeft(),
			    !this.isTopOrLeft());
			if (last == null) {
				break;
			}
			sibling = last;
		}
		if (sibling.isRoot()) {
			return this;
		}
		return sibling;
	}

	LinkedList<NodeView> getRight() {
		final LinkedList<NodeView> right = new LinkedList<NodeView>();
		for (final NodeView node : getChildrenViews()) {
			if (node == null) {
				continue;
			}
			if (!node.isTopOrLeft()) {
				right.add(node);
			}
		}
		return right;
	}

	/**
		 * @return Returns the sHIFT.s
		 */
	public int getShift() {
		final LocationModel locationModel = LocationModel.getModel(viewedNode);
		return map.getZoomed(calcShiftY(locationModel));
	}

    public Color getTextBackground() {
        if (modelBackgroundColor != null) {
            return modelBackgroundColor;
        }
        return getBackgroundColor();
    }

    public void setTextBackground(Color textBackground) {
        modelBackgroundColor = textBackground;
    }

    public Color getTextBackground(StyleOption styleOption) {
        Color modelBackgroundColor = styleController().getBackgroundColor(viewedNode, styleOption);
        if (modelBackgroundColor != null) {
            return modelBackgroundColor;
        }
        return getBackgroundColor();
    }

    private NodeStyleController styleController() {
        return NodeStyleController.getController(getModeController());
    }

	public Color getTextColor(StyleOption styleOption) {
		final Color color = styleController().getColor(viewedNode, styleOption);
		return color;
	}

    public int getMinimalDistanceBetweenChildren() {
        final double minimalDistanceBetweenChildren = getModeController().getExtension(LocationController.class).getCommonVGapBetweenChildren(viewedNode).toBaseUnits();
        return map.getZoomed(minimalDistanceBetweenChildren);
    }

    public int getBaseDistanceToChildren() {
        final double distance = getModeController().getExtension(LocationController.class).getBaseHGapToChildren(viewedNode).toBaseUnits();
        return map.getZoomed(distance - LocationModel.DEFAULT_HGAP_PX);
    }

	public ChildNodesAlignment getChildNodesAlignment() {
	    updateLayoutProperties();
        return childNodesAlignment;
	}

	private void updateChildNodesAlignment() {
	    if(map.isOutlineLayoutSet())
	        this.childNodesAlignment =  ChildNodesAlignment.AFTER_PARENT;
	    else {
	        ChildNodesAlignment childNodesAlignment = childNodesLayout.childNodesAlignment();
	        switch (childNodesAlignment) {
	        case STACKED_AUTO:
	        case NOT_SET:
	        case AUTO:
	            this.childNodesAlignment = getDefaultChildNodesAlignment();
	            break;
	        default:
	            this.childNodesAlignment =  childNodesAlignment;
	        }
	    }
	}

	private boolean isEffectivelyTopOrLeft() {
	    NodeView parentView = getParentNodeView();
	    if(parentView == null || parentView.layoutOrientation() != layoutOrientation())
	        return isTopOrLeft();
	    else
	        return parentView.isEffectivelyTopOrLeft();
	}

	private ChildNodesAlignment getDefaultChildNodesAlignment() {
		NodeView parentView = getParentNodeView();

		if (parentView == null)
		    return ChildNodesAlignment.FLOW;
		else if(parentView.isSummary())
		    return parentView.getDefaultChildNodesAlignment();
		else if(childNodesLayout.childNodesAlignment() == ChildNodesAlignment.STACKED_AUTO) {
            if(parentView.getChildNodesAlignment().isStacked())
                return parentView.getChildNodesAlignment();
            else {
		        if(isEffectivelyTopOrLeft())
		            return ChildNodesAlignment.BEFORE_PARENT;
		        else
		            return   ChildNodesAlignment.AFTER_PARENT;
		    }
        } else if(parentView.usesHorizontalLayout() == usesHorizontalLayout())
			return parentView.getChildNodesAlignment();
		else if(isTopOrLeft())
		    return ChildNodesAlignment.BEFORE_PARENT;
		else
		    return ChildNodesAlignment.AFTER_PARENT;
	}

	public NodeView getAncestorWithVisibleContent() {
		final NodeView parentView = getParentView();
		if (parentView == null || parentView.isContentVisible()) {
			return parentView;
		}
		return parentView.getAncestorWithVisibleContent();
	}

    NodeView getDescendant(PreferredChild preferredChild) {
        NodeView newSelected = this;
        while(layoutOrientation() == LayoutOrientation.TOP_TO_BOTTOM
                 && getChildNodesAlignment() == getParentView().getChildNodesAlignment()
                 && isSubtreeVisible()) {
            NodeView preferredVisibleChild = newSelected.getPreferredVisibleChild(preferredChild, ChildrenSides.BOTH_SIDES);
            if(preferredVisibleChild != null)
                newSelected = preferredVisibleChild;
            else
                break;
        }
        return newSelected;
    }
	NodeView getVisibleSummarizedOrParentView(LayoutOrientation requiredLayoutOrientation, boolean isChildTopOrLeft) {
		final Container parent = getParent();
		if (!(parent instanceof NodeView)) {
			return null;
		}
		final NodeView parentView = (NodeView) parent;
		if(parentView.layoutOrientation() == requiredLayoutOrientation && isChildTopOrLeft == isTopOrLeft()) {
		    if(isSummary()){
		        boolean startFromSummary = true;
		        LinkedList<NodeView> v = getSiblingViews();
		        final int index = v.indexOf(this);
		        for (int i = index - 1; i >= 0; i--) {
		            final NodeView nextView = v.get(i);
		            if (nextView.isContentVisible() || nextView.isSubtreeVisible()) {
		                return nextView.getDescendant(PreferredChild.FIRST);
		            }
		            if(! nextView.isSummary())
		                startFromSummary = false;
		            else if(! startFromSummary)
		                break;

		        }
		    }
		    if (parentView.isContentVisible()) {
		        return parentView;
		    }
		}
		return parentView.getVisibleSummarizedOrParentView(requiredLayoutOrientation, isChildTopOrLeft);
	}

	public int getZoomedFoldingMarkHalfWidth() {
	    final int preferredFoldingSymbolHalfWidth = (int) ((ResourceController.getResourceController().getLengthQuantityProperty("foldingsymbolwidth").toBaseUnits() * map.getZoom()) / 2);
	    return preferredFoldingSymbolHalfWidth;
	}


    public int getZoomedFoldingSwitchMinWidth() {
        final int preferredFoldingSwitchMinWidth = (int) ((ResourceController.getResourceController().getLengthQuantityProperty("foldingSwitchMinWidth").toBaseUnits() * map.getZoom()));
        return preferredFoldingSwitchMinWidth;
    }


	int getMinimumDistanceConsideringHandles() {
	    int draggingAreaWidth = mainView.getDraggingAreaWidth();
	    if(!usesHorizontalLayout()) {
	        final int preferredFoldingSymbolWidth = Math.max(getZoomedFoldingMarkHalfWidth(), getZoomedFoldingSwitchMinWidth());
	        return draggingAreaWidth + preferredFoldingSymbolWidth;
	    }
	    else
	        return draggingAreaWidth;
	}

	public int getZoomedStateSymbolHalfWidth() {
		final int preferredFoldingSymbolHalfWidth = (int) ((ResourceController.getResourceController().getLengthQuantityProperty("statesymbolwidth").toBaseUnits() * map.getZoom()) / 2);
		return preferredFoldingSymbolHalfWidth;
	}

	void addChildViews() {
		if(isFolded())
			return;
		int index = 0;
		for (NodeModel child : getNode().getChildren()) {
			if(isChildHidden(child))
				return;
			if(getComponentCount() <= index
					|| ! (getComponent(index) instanceof NodeView))
				addChildView(child, index++);
		}
	}

	NodeView addChildView(final NodeModel newNode, int index) {
			return NodeViewFactory.getInstance().newNodeView(newNode, map, this, index);
	}

	/* fc, 25.1.2004: Refactoring necessary: should call the model. */
	public boolean isChildOf(final NodeView myNodeView) {
		return getParentView() == myNodeView;
	}

	/**
	 */
	public boolean isContentVisible() {
		if(isValid())
			return getContent().isVisible();
		else
			return getNode().hasVisibleContent(map.getFilter()) || isRoot();
	}

    public Side side() {
        updateLayoutProperties();
         return side;
     }

    @Override
    public boolean isTopOrLeft() {
         return side() == Side.TOP_OR_LEFT;
    }

	public boolean isRight() {
		return ! isTopOrLeft() && getNode() != map.getRoot().getNode();
	}

	public boolean isParentHidden() {
		final Container parent = getParent();
		if (!(parent instanceof NodeView)) {
			return false;
		}
		final NodeView parentView = (NodeView) parent;
		return !parentView.isContentVisible() && ! parentView.getNode().isHiddenSummary();
	}

	/* fc, 25.1.2004: Refactoring necessary: should call the model. */
	public boolean isParentOf(final NodeView myNodeView) {
		return (this == myNodeView.getParentView());
	}

	public boolean isRoot() {
		return map.isRoot(this);
	}

	public boolean isSelected() {
		return (map.isSelected(this));
	}

	/* fc, 25.1.2004: Refactoring necessary: should call the model. */
	public boolean isSiblingOf(final NodeView myNodeView) {
		return getParentView() == myNodeView.getParentView();
	}

	@Override
	public void nodeChanged(final NodeChangeEvent event) {
		final NodeModel node = event.getNode();
		// is node is deleted, skip the rest.
		if (!node.isRoot() && node.getParentNode() == null) {
			return;
		}
		final Object property = event.getProperty();
		if (property == NodeChangeType.FOLDING || property == Properties.HIDDEN_CHILDREN || property == EncryptionModel.class) {
			if(map.isSelected() || property == EncryptionModel.class && ! isFolded()){
				boolean folded = getModeController().getMapController().isFolded(viewedNode);
				boolean force = property ==Properties.HIDDEN_CHILDREN || property == EncryptionModel.class;
				setFolded(folded, force);
			}
			if(property != EncryptionModel.class)
				return;
		}
        if(property == ChildNodesAlignment.class
                || property == LayoutOrientation.class
                || property == ChildNodesLayout.class) {
            resetLayoutPropertiesRecursively();
            revalidate();
            repaint();
            return;
        }
		if(property == NodeVisibilityConfiguration.class) {
			updateAll();
			if(event.getNewValue() != NodeVisibilityConfiguration.SHOW_HIDDEN_NODES)
			    FilterController.getCurrentFilterController().selectVisibleNodes(map.getMapSelection());
			return;
		}

		if(property == NodeVisibility.class
				&& node.getMap().getRootNode().getExtension(NodeVisibilityConfiguration.class) != NodeVisibilityConfiguration.SHOW_HIDDEN_NODES) {
		    if(! isRoot()) {
		        final NodeView parentView = getParentView();
		        parentView.setFolded(parentView.isFolded(), true);
		        if(event.getNewValue() == NodeVisibility.HIDDEN && isSelected())
		            FilterController.getCurrentFilterController().selectVisibleNodes(map.getMapSelection());
		    }
		    else
		        updateIcons();
			return;
		}

		if(property == Side.class) {
			resetLayoutPropertiesRecursively();
			revalidate();
		}

		// is node is not fully initialized, skip the rest.
		if (mainView == null) {
			return;
		}
		if (property.equals(NodeModel.NODE_ICON) || property.equals(HierarchicalIcons.ICONS)) {
			updateIcons();
			revalidate();
			return;
		}
		if (property.equals(NodeModel.NODE_ICON_SIZE))
		{
			updateIcons();
			revalidate();
			return;
		}

		if (property.equals(HistoryInformationModel.class)) {
			return;
		}
		update();
		NodeView parentView = getParentView();
		NodeModel parentNode = node.getParentNode();
		if (parentNode != null && parentView != null) {
			parentView.numberingChanged(parentNode.getIndex(node) + 1);
		}
	}

	public void setFolded(boolean folded) {
		setFolded(folded, false);
		revalidate();
	}

    void keepUnfolded() {
        isFolded = false;
    }

	private void setFolded(boolean fold, boolean force) {
		boolean wasFolded = isFolded;
		this.isFolded = fold;
		if(wasFolded != fold || force) {
	        map.preserveRootNodeLocationOnScreen();
			fireFoldingChanged();
		}
		if(lastSelectedChild != null && isFolded())
			lastSelectedChild = null;
	}

    void fireFoldingChanged() {
        treeStructureChanged();
        map.selectIfSelectionIsEmpty(this.isContentVisible() ? this : getAncestorWithVisibleContent());
        NodeStyleShape shape = styleController().getShape(viewedNode, getStyleOption());
        if (shape.equals(NodeStyleShape.combined))
        	update();
    }

	@Override
	public void onPreNodeDeleted(NodeDeletionEvent nodeDeletionEvent) {
	    adjustLastSelectedChild(nodeDeletionEvent);
	}


	private void adjustLastSelectedChild(NodeDeletionEvent nodeDeletionEvent) {
		if(lastSelectedChild == null || isFolded())
			return;
		final NodeView mapRootNodeView = map.getRoot();
        NodeModel mapRootNode = mapRootNodeView.getNode();
	    final NodeView node;
	    final int childModeViewIndex;
	    if(mapRootNode == nodeDeletionEvent.node) {
            childModeViewIndex = nodeDeletionEvent.index;
            node = mapRootNodeView;
	    } else {
	        childModeViewIndex = map.calculateComponentIndex(this, nodeDeletionEvent.index);
	        node = (NodeView) getComponent(childModeViewIndex);
	    }
	    if (node == lastSelectedChild) {
	        lastSelectedChild = null;
	        for (int j = mapRootNode == nodeDeletionEvent.node ? childModeViewIndex : childModeViewIndex + 1; j < getComponentCount(); j++) {
	            final Component c = getComponent(j);
				if (!(c instanceof NodeView)) {
					break;
				}
				final NodeView candidate = (NodeView) c;
				if (candidate.isVisible() && node.isTopOrLeft() == candidate.isTopOrLeft()) {
					lastSelectedChild = candidate;
					break;
				}
			}
			if (lastSelectedChild == null) {
				for (int j = childModeViewIndex - 1; j >= 0; j--) {
					final Component c = getComponent(j);
					if (!(c instanceof NodeView)) {
						break;
					}
					final NodeView candidate = (NodeView) c;
					if (candidate.isVisible() && node.isTopOrLeft() == candidate.isTopOrLeft()) {
						lastSelectedChild = candidate;
						break;
					}
				}
			}
		}
	}

    @Override
	public void onNodeDeleted(NodeDeletionEvent nodeDeletionEvent) {
        NodeModel mapRootNode = map.getRoot().getNode();
        if(mapRootNode == nodeDeletionEvent.node)
            map.restoreRootNode(nodeDeletionEvent.index);
        else if (mapRootNode.isDescendantOf(nodeDeletionEvent.node))
            map.restoreRootNode();
        if (getComponentCount() <= 1) {
            return;
        }

        final int childNodeViewIndex;
        if (viewedNode == map.getRoot().getNode().getParentNode()) {
            if(nodeDeletionEvent.index == getComponentCount() - 1)
                childNodeViewIndex = nodeDeletionEvent.index - 1;
            else if(nodeDeletionEvent.index == 0)
                childNodeViewIndex = nodeDeletionEvent.index;
            else {
                final NodeView node = (NodeView) getComponent(nodeDeletionEvent.index);
                if(node.getNode() == nodeDeletionEvent.node)
                    childNodeViewIndex = nodeDeletionEvent.index;
                else
                    childNodeViewIndex = nodeDeletionEvent.index - 1;
            }
        }
        else
            childNodeViewIndex = nodeDeletionEvent.index;
        final NodeView node = (NodeView) getComponent(childNodeViewIndex);
        if(node.viewedNode != nodeDeletionEvent.node) {
            throw new IllegalStateException("Inconsistent child node view after deletion");
        }
		numberingChanged(childNodeViewIndex+1);
		map.preserveRootNodeLocationOnScreen();
		node.remove();
		map.updateSelectedNode();
		NodeView preferred = getPreferredVisibleChild(PreferredChild.LAST_SELECTED, childrenSides());
		if (preferred == null) {
			preferred = this;
		}
		revalidate();
		if(map.getSelected() ==  null)
			map.selectVisibleAncestorOrSelf(preferred);
	}

	public NodeView getPreferredVisibleChild(PreferredChild preferredChild, boolean isTopOrLeft) {
	    return getPreferredVisibleChild(preferredChild, ChildrenSides.ofTopOrLeft(isTopOrLeft));
    }



    @Override
	public void onNodeInserted(final NodeModel parent, final NodeModel child, final int index) {
		assert parent == viewedNode;
		if (isFolded()) {
		    return;
		}
		if(child == map.getRoot().getNode())
		    return;
		NodeView newChild = addChildView(child, index);
		if(map.getRoot().getNode().getParentNode() != parent)
		    numberingChanged(index + 1);
		if(! SummaryNode.isSummaryNode(child))
		    lastSelectedChild = newChild;
		revalidate();
    }

	// updates children, starting from firstChangedIndex, if necessary.
	private void numberingChanged(int firstChangedIndex) {
		final TextController textController = TextController.getController(getModeController());
		if (firstChangedIndex > 0 || textController.getNodeNumbering(getNode())) {
			final Component[] components = getComponents();
			for (int i = firstChangedIndex; i < components.length; i++) {
				if (components[i] instanceof NodeView) {
					final NodeView view = (NodeView) components[i];
					final MainView childMainView = view.getMainView();
					if(childMainView != null){
						childMainView.updateText(view.getNode());
						view.numberingChanged(0);
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	@Override
	public void paintComponent(final Graphics g) {
		if(getMainView() == null)
			return;
		final PaintingMode paintingMode = map.getPaintingMode();
		if(paintingMode == null){
			LogUtils.severe("paintingMode = null");
			LogUtils.severe("own map ="  + map);
			final MapView ancestorMap = (MapView) SwingUtilities.getAncestorOfClass(MapView.class, this);
			LogUtils.severe("parent component map ="  + ancestorMap);
			if(ancestorMap != null)
				LogUtils.severe("ancestor map paintingMode = " + ancestorMap.getPaintingMode());
			throw new NullPointerException();
		}
		final Graphics2D g2 = (Graphics2D) g;
		final ModeController modeController = getModeController();
		final Object renderingHint = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
		switch (paintingMode) {
		case CLOUDS:
		    if (isSubtreeVisible()) {
		        modeController.getController().getMapViewManager().setEdgesRenderingHint(g2);
		        final boolean isRoot = isRoot();
		        if (isRoot) {
		            paintCloud(g);
		        }
		        paintClouds(g2);
		        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, renderingHint);
		    }
		    break;
		case NODES:
		    if (isContentVisible()) {
		        g2.setStroke(MainView.DEF_STROKE);
		        modeController.getController().getMapViewManager().setEdgesRenderingHint(g2);
		        paintEdges(g2, this);
		        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, renderingHint);
		    }
		    break;
		default:
		    break;
		}
		if (PAINT_DEBUG_INFO && isSelected() && paintingMode.equals(PaintingMode.SELECTED_NODES)){
			final int spaceAround = getZoomed(SPACE_AROUND);
			g.setColor(UITools.getTextColorForBackground(getBackgroundColor()));
			g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
			g.drawRect(spaceAround - 1, spaceAround - 1, getWidth() - 2 * spaceAround, getHeight() - 2 * spaceAround);
			Object debugInfo = getClientProperty(DEBUG_INFO_PROPERTY);
			if(debugInfo != null)
				g.drawString(debugInfo.toString(), 0, spaceAround);
		}
	}

	@Override
    public void paint(Graphics g) {
		if(isSubtreeVisible()) {
			super.paint(g);
			paintDecoration((Graphics2D) g);
		}
    }

	private void paintCloud(final Graphics g) {
		if (!isSubtreeVisible()) {
			return;
		}
		final CloudModel cloudModel = getCloudModel();
		if (cloudModel == null) {
			return;
		}
		final CloudView cloud = new CloudViewFactory().createCloudView(cloudModel, this);
		cloud.paint(g);
	}

    private void paintClouds(final Graphics2D g) {
        for (int i = getComponentCount() - 1; i >= 0; i--) {
            final Component component = getComponent(i);
            if (!(component instanceof NodeView)) {
                continue;
            }
            final NodeView nodeView = (NodeView) component;
            final Point p = new Point();
            UITools.convertPointToAncestor(nodeView, p, this);
            g.translate(p.x, p.y);
            if (nodeView.isSubtreeVisible()) {
                nodeView.paintCloud(g);
            }
            else {
                nodeView.paintClouds(g);
            }
            g.translate(-p.x, -p.y);
        }
    }

    private void paintEdges(final Graphics2D g, NodeView source) {
        ChildrenSides childrenSides = childrenSides();
    	boolean paintsChildrenOnBothSides  = childrenSides == ChildrenSides.BOTH_SIDES || isRoot();
		boolean paintsOnTheLeftSide = paintsChildrenOnBothSides ? true
		        : childrenSides == ChildrenSides.BOTTOM_OR_RIGHT ? false
		        : childrenSides == ChildrenSides.TOP_OR_LEFT ? true
		        : isTopOrLeft();
        SummaryEdgePainter summaryEdgePainter = new SummaryEdgePainter(this, paintsOnTheLeftSide);
    	SummaryEdgePainter rightSummaryEdgePainter =  paintsChildrenOnBothSides ? new SummaryEdgePainter(this, false) : null;
        final int start;
        final int end;
        final int step;
        if (map.getLayoutType() == MapViewLayout.OUTLINE){
        	start = getComponentCount() - 1;
        	end = -1;
        	step = -1;
        }
        else{
        	start = 0;
        	end = getComponentCount();
        	step = 1;
        }
		for (int i = start; i != end; i+=step) {
            final Component component = getComponent(i);
            if (!(component instanceof NodeView)) {
                continue;
            }
            final NodeView nodeView = (NodeView) component;
        	if (map.getLayoutType() != MapViewLayout.OUTLINE) {
        		SummaryEdgePainter activePainter = nodeView.isTopOrLeft() || !paintsChildrenOnBothSides ? summaryEdgePainter : rightSummaryEdgePainter;
        		activePainter.addChild(nodeView);
        		if(nodeView.isSubtreeVisible() && activePainter.paintSummaryEdge(g, source, nodeView)){
        			if(! nodeView.isContentVisible()){
        				final Rectangle bounds =  SwingUtilities.convertRectangle(this, nodeView.getBounds(), source);
        				final Graphics cg = g.create(bounds.x, bounds.y, bounds.width, bounds.height);
        				try{
        					nodeView.paintEdges((Graphics2D) cg, nodeView);
        				}
        				finally{
        					cg.dispose();
        				}

        			}
        			continue;
        		}
            }
        	if (nodeView.isContentVisible()) {
        		final EdgeView edge = EdgeViewFactory.getInstance().getEdge(source, nodeView, source);
        		edge.paint(g);
        	}
        	else {
        		nodeView.paintEdges(g, source);
        	}
        }
    }


    public ChildNodesLayout recalculateChildNodesLayout() {
        childNodesLayout = null;
        return getChildNodesLayout();
    }


    public ChildNodesLayout getChildNodesLayout() {
        updateLayoutProperties();
        return childNodesLayout;
    }


    public ChildrenSides childrenSides() {
        updateLayoutProperties();
        return childrenSides;
    }



    int getSpaceAround() {
		return getZoomed(NodeView.SPACE_AROUND);
	}

	public int getZoomed(int x) {
		return map.getZoomed(x);
	}

	private void paintDecoration(final Graphics2D g) {
		final PaintingMode paintingMode = map.getPaintingMode();
		if(! (getMainView() != null &&
				( paintingMode.equals(PaintingMode.NODES) && !isSelected() || paintingMode.equals(PaintingMode.SELECTED_NODES) && isSelected())
				&& isContentVisible()))
			return;
		final Graphics2D g2 = g;
		final ModeController modeController = getModeController();
		final Object renderingHint = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
		g2.setStroke(MainView.DEF_STROKE);
		modeController.getController().getMapViewManager().setEdgesRenderingHint(g2);
		final Point origin = new Point();
		UITools.convertPointToAncestor(mainView, origin, this);
		g.translate(origin.x, origin.y);
		mainView.paintDecoration(this, g);
		g.translate(-origin.x, -origin.y);
		if (map.isSelected()) {
			final HighlightController highlightController = getModeController().getController().getExtension(HighlightController.class);
			final List<NodeHighlighter> highlighters = highlightController.getHighlighters(viewedNode, map.isPrinting());
			int margin = HIGHLIGHTED_NODE_ARC_MARGIN;
			for(NodeHighlighter highlighter : highlighters){
				margin += HIGHLIGHTED_NODE_ARC_MARGIN;
				highlightNode(g, highlighter, margin);
			}
		}
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, renderingHint);
	}

	public void highlightNode(final Graphics2D g, NodeHighlighter highlighter, final int arcMargin) {
		final Color oldColor = g.getColor();
		final Stroke oldStroke = g.getStroke();
		g.setStroke(NodeHighlighter.DEFAULT_STROKE);
		highlighter.configure(getNode(), g, map.isPrinting());
		final JComponent content = getContent();
		Point contentLocation = content.getLocation();
		final int arcWidth = 15;
		g.drawRoundRect(contentLocation.x - arcMargin, contentLocation.y - arcMargin, content.getWidth() + 2 * arcMargin,
		    content.getHeight() + 2 * arcMargin, arcWidth, arcWidth);
		g.setColor(oldColor);
		g.setStroke(oldStroke);
	}

	/**
	 * This is a bit problematic, because getChildrenViews() only works if model
	 * is not yet removed. (So do not _really_ delete the model before the view
	 * removed (it needs to stay in memory)
	 */
	void remove() {
		for (final ListIterator<NodeView> e = getChildrenViews().listIterator(); e.hasNext();) {
			NodeView child = e.next();
			child.remove();
		}
		getModeController().onViewRemoved(this);
		removeFromMap();
		if (attributeView != null) {
			attributeView.viewRemoved();
		}
		getNode().removeViewer(this);
		map.deselect(this);
	}

	protected void removeFromMap() {
		setFocusCycleRoot(false);
		Container parent = getParent();
		parent.remove(this);
	}

	private void repaintEdge(final NodeView target) {
        final MainView targetMainView = target.getMainView();
        int targetEdgeWidth = target.getEdgeWidth();

        Point mainViewLocation = new Point(0, 0);
        UITools.convertPointToAncestor(mainView, mainViewLocation, this);
        Point targetMainViewLocation = new Point(0, 0);
        UITools.convertPointToAncestor(targetMainView, targetMainViewLocation, this);

        final int x = Math.min(mainViewLocation.x, targetMainViewLocation.x);
		final int y = Math.min(mainViewLocation.y, targetMainViewLocation.y);
		final int w = Math.max(mainViewLocation.x + mainView.getWidth(),
		        targetMainViewLocation.x + targetMainView.getWidth()) - x;
		final int h =Math.max(mainViewLocation.y + mainView.getHeight(),
                targetMainViewLocation.y + targetMainView.getHeight()) - y;
		final int EXTRA = 1 + targetEdgeWidth;
		repaint(x - EXTRA, y - EXTRA, w + EXTRA * 2, h + EXTRA * 2);
	}

	void repaintSelected() {
		// return if main view was not set
		if (mainView == null) {
			return;
		}
		// do not repaint removed nodes
		if (viewedNode.getParentNode() == null && !viewedNode.isRoot()) {
			return;
		}
		if (getEdgeStyle().equals(EdgeStyle.EDGESTYLE_HIDDEN)) {
			repaintEdge();
		}
		final JComponent content = getContent();
		final int EXTRA = 20;
		final int x = content.getX() - EXTRA;
		final int y = content.getY() - EXTRA;
		repaint(x, y, content.getWidth() + EXTRA * 2, content.getHeight() + EXTRA * 2);
	}



    void repaintEdge() {
        final NodeView visibleParentView = getAncestorWithVisibleContent();
        if (visibleParentView != null) {
        	visibleParentView.repaintEdge(this);
        }
    }

	@Override
	public boolean requestFocusInWindow() {
		if (mainView == null) {
			return false;
		}
		if (mainView.requestFocusInWindow()) {
			map.scrollNodeToVisible(this);
			Controller.getCurrentController().getViewController().addObjectTypeInfo(getNode().getUserObject());
			return true;
		}
		return false;
	}

	@Override
	public void requestFocus() {
		if (mainView == null) {
			return;
		}
		map.scrollNodeToVisible(this);
		Controller.getCurrentController().getViewController().addObjectTypeInfo(getNode().getUserObject());
		mainView.requestFocus();
	}

	void setMainView(final MainView newMainView) {
		if (contentPane != null) {
			assert (contentPane.getParent() == this);
			if (mainView != null)
				removeContent(MAIN_VIEWER_POSITION);
			addContent(newMainView, MAIN_VIEWER_POSITION);
			assert (contentPane.getParent() == this);
		}
		else if (mainView != null) {
			final Container c = mainView.getParent();
			int i;
			for (i = c.getComponentCount() - 1; i >= 0 && mainView != c.getComponent(i); i--) {/**/}
			c.remove(i);
			c.add(newMainView, i);
		}
		else {
			add(newMainView);
		}
		mainView = newMainView;
		ModeController modeController = getModeController();
		if(modeController.canEdit(getNode())) {
			final IUserInputListenerFactory userInputListenerFactory = modeController
					.getUserInputListenerFactory();
			mainView.addMouseListener(userInputListenerFactory.getNodeMouseMotionListener());
			mainView.addMouseMotionListener(userInputListenerFactory.getNodeMouseMotionListener());
			mainView.addMouseWheelListener(userInputListenerFactory.getNodeMouseWheelListener());
			mainView.addKeyListener(userInputListenerFactory.getNodeKeyListener());
			addDragListener(userInputListenerFactory.getNodeDragListener());
			addDropListener(userInputListenerFactory.getNodeDropTargetListener());
		}
	}

	protected void setViewedNode(final NodeModel model) {
		this.viewedNode = model;
	}

	public void setLastSelectedChild(final NodeView view) {
	    if (view == null) {
	        return;
	    }
		if(! SummaryNode.isSummaryNode(view.getNode()))
			lastSelectedChild = view;
		final Container parent = this.getParent();
		if (parent instanceof NodeView) {
			((NodeView) parent).setLastSelectedChild(this);
		}
	}

	/**
	 */
	public void setText(final String string) {
		mainView.setText(string);
	}


	void syncronizeAttributeView() {
		if (attributeView != null) {
			attributeView.syncronizeAttributeView();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.Component#toString()
	 */
	@Override
	public String toString() {
		return getNode().toString() + ", " + super.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * javax.swing.event.TreeModelListener#treeStructureChanged(javax.swing.
	 * event.TreeModelEvent)
	 */
	private void treeStructureChanged() {
		for (NodeView child : getChildrenViews()) {
			child.remove();
		}
		map.updateSelectedNode();
		addChildViews();
		map.revalidateSelecteds();
		revalidate();
	}

	public void update() {
		if(! map.isDisplayable())
			return;
		invalidate();
		updateShape();
		updateEdge();
		updateCloud();
		if (!isContentVisible()) {
			mainView.setVisible(false);
			return;
		}
		mainView.setVisible(true);
		mainView.updateTextColor(this);
		mainView.updateCss(this);
		mainView.updateFont(this);
		mainView.updateHorizontalTextAlignment(this);
		mainView.updateTextWritingDirection(this);
		mainView.updateBorder(this);
		final ModeController modeController = getModeController();
        final NodeStyleController nsc = NodeStyleController.getController(modeController);
        StyleOption styleOption = getStyleOption();
        final int minNodeWidth = map.getZoomed(nsc.getMinWidth(getNode(), styleOption).toBaseUnits());
        final int maxNodeWidth = Math.max(map.getLayoutSpecificMaxNodeWidth(), map.getZoomed(nsc.getMaxWidth(getNode(), styleOption).toBaseUnits()));
        mainView.setMinimumWidth(minNodeWidth);
        mainView.setMaximumWidth(maxNodeWidth);

		createAttributeView();
		if (attributeView != null) {
			attributeView.update();
		}
		final boolean textShortened = isShortened();

		if(! textShortened){
			final NodeViewFactory nodeViewFactory = NodeViewFactory.getInstance();
			nodeViewFactory.updateDetails(this, minNodeWidth, maxNodeWidth);
			nodeViewFactory.updateNoteViewer(this, minNodeWidth, maxNodeWidth);
			if (contentPane != null) {
				final int componentCount = contentPane.getComponentCount();
				for (int i = 1; i < componentCount; i++) {
					final Component component = contentPane.getComponent(i);
					if (component instanceof JComponent) {
						((JComponent) component).revalidate();
					}
				}
			}
		}
		updateShortener(textShortened);
		updateIcons();
		mainView.updateText(getNode());
		modelBackgroundColor = styleController().getBackgroundColor(viewedNode, getStyleOption());
		revalidate();
		repaint();
	}



    void updateIcons() {
        mainView.updateIcons(this);
    }

    private ModeController getModeController() {
        return map.getModeController();
    }

	public boolean isShortened() {
	    final ModeController modeController = getModeController();
		final TextController textController = TextController.getController(modeController);
		final boolean textShortened = textController.isMinimized(getNode());
	    return textShortened;
    }

	private void updateEdge() {
        final EdgeController edgeController = EdgeController.getController(getModeController());
        EdgeStyle newEdgeStyle = edgeController.getStyle(viewedNode, getStyleOption(), false);
        final NodeModel realNode = SummaryNode.getRealNode(viewedNode);
        Integer newEdgeWidth = edgeController.getWidth(realNode, getStyleOption(), false);
        Dash newEdgeDash = edgeController.getDash(realNode, getStyleOption(), false);
        final ObjectRule<Color, Rules> newColor = edgeController.getColorRule(realNode, getStyleOption());
        if(newEdgeStyle != edgeStyle
                || ! Objects.equals(newEdgeWidth, edgeWidth)
                || newEdgeDash != edgeDash
                || ! Objects.equals(newColor, edgeColor)) {
            this.edgeStyle = newEdgeStyle;
            this.edgeWidth = newEdgeWidth;
            this.edgeDash = newEdgeDash;
            this.edgeColor = newColor;
            if(! isRoot()) {
                NodeView ancestorWithVisibleContent = getAncestorWithVisibleContent();
                if(ancestorWithVisibleContent != null)
                    ancestorWithVisibleContent.repaint();
            }
        }
        final NodeModel parentNode = viewedNode.getParentNode();
        if(!isRoot() && parentNode != null && SummaryNode.isSummaryNode(parentNode))
			getParentView().updateEdge();
    }

	public EdgeStyle getEdgeStyle() {
		if(edgeStyle != null)
			return edgeStyle;
		final NodeView parentView = getParentNodeView();
		if(parentView != null)
			return parentView.getEdgeStyle();
		return EdgeStyle.values()[0];
    }

	public Dash getEdgeDash() {
		if(edgeDash != null)
		    return edgeDash;
		final NodeView parentView = getParentNodeView();
		if(parentView != null)
			return parentView.getEdgeDash();
		return Dash.DEFAULT;
    }

	public int getEdgeWidth() {
		if(edgeWidth != null)
		    return edgeWidth;
		final NodeView parentView = getParentNodeView();
		if(parentView != null)
			return parentView.getEdgeWidth();
		return 1;
    }

	public Color getEdgeColor() {
		if(edgeColor.hasValue())
			return edgeColor.getValue();
		Rules rule = edgeColor.getRule();
		if(rule == EdgeController.Rules.BY_COLUMN){
			final Color color = new AutomaticEdgeStyle(this).getColor();
			edgeColor.setCache(color);
			return color;
		}
		final NodeModel parentNode = viewedNode.getParentNode();
		if(rule == EdgeController.Rules.BY_BRANCH && parentNode.isRoot()
				|| rule == EdgeController.Rules.BY_LEVEL){
			final int index;
			if (rule == EdgeController.Rules.BY_BRANCH)
				index = parentNode.getIndex(viewedNode) + 1;
			else
				index = viewedNode.getNodeLevel(map.getFilter()) + (viewedNode.isHiddenSummary() ? 1 : 0);
			final MapModel mapModel = map.getMap();
			ModeController modeController = getModeController();
			EdgeController edgeController = modeController.getExtension(EdgeController.class);
			if(edgeController.areEdgeColorsAvailable(mapModel)){
				Color color = edgeController.getEdgeColor(mapModel, index);
				edgeColor.setCache(color);
				return color;
			}
		}
		else
			if(rule == EdgeController.Rules.BY_PARENT) {
			final NodeView parentView = getParentNodeView();
			if (parentView != null) {
				final Color color = parentView.getEdgeColor();
				return color;
			}
		}
		return Color.GRAY;
    }

	private void updateCloud() {
		final CloudModel cloudModel = CloudController.getController(getModeController()).getCloud(viewedNode, getStyleOption());
		putClientProperty(CloudModel.class, cloudModel);
    }

	public CloudModel getCloudModel() {
		return (CloudModel) getClientProperty(CloudModel.class);
    }

	private void updateShortener(boolean textShortened) {
		final boolean componentsVisible = !textShortened;
		setContentComponentVisible(componentsVisible);
	}

	private void setContentComponentVisible(final boolean componentsVisible) {
		if(contentPane == null)
			return;
		final Component[] components = getContentPane().getComponents();
		int index;
		for (index = 0; index < components.length; index++) {
			final Component component = components[index];
			if (component == getMainView()) {
				continue;
			}
			if (component.isVisible() != componentsVisible) {
				component.setVisible(componentsVisible);
			}
		}
	}

	public void updateAll() {
		update();
		invalidate();
		for (final NodeView child : getChildrenViews()) {
			child.updateAll();
		}
	}
	void resetLayoutPropertiesRecursively() {
	    childNodesAlignment = null;
	    childNodesLayout = null;
		LinkedList<NodeView> childrenViews = getChildrenViews();
		if(childrenViews.isEmpty())
			invalidate();
		for (final NodeView child : childrenViews) {
			child.resetLayoutPropertiesRecursively();
		}
	}

	private void updateLayoutProperties() {
	    if(childNodesLayout == null) {
            updateSide();
	        LayoutController layoutController = getModeController().getExtension(LayoutController.class);
	        childNodesLayout = layoutController.getEffectiveChildNodesLayout(viewedNode);
	        updateLayoutOrientation();
	        updateChildNodesAlignment();
	        updateChildrenSides();
	        childNodesLayout = ChildNodesLayout.using(layoutOrientation, childrenSides, childNodesAlignment)
	                .orElse(childNodesLayout);
	    }
    }

    @Override
    public boolean hasStandardLayoutWithRootNode(NodeModel root) {
        return map.getLayoutType() == MapViewLayout.MAP
                && map.getRoot().getNode().equals(root);
    }


    private void updateChildrenSides() {
        final ChildrenSides childrenSides;
        if (map.getLayoutType() == MapViewLayout.OUTLINE)
            childrenSides = ChildrenSides.BOTTOM_OR_RIGHT;
        else if(viewedNode.isRoot() && childNodesLayout.childNodesAlignment() == ChildNodesAlignment.STACKED_AUTO)
            childrenSides = ChildrenSides.BOTH_SIDES;
        else {
            ChildrenSides childrenSidesByLayout = childNodesLayout.childrenSides();
            if(childrenSidesByLayout == ChildrenSides.TOP_OR_LEFT
                    || childrenSidesByLayout == ChildrenSides.BOTTOM_OR_RIGHT
                    || childrenSidesByLayout == ChildrenSides.BOTH_SIDES) {
                childrenSides = childrenSidesByLayout;
            } else if (isRoot()) {
                childrenSides = ChildrenSides.BOTH_SIDES;
            } else {
                if(childNodesAlignment.isStacked()
                        && (childrenSidesByLayout == ChildrenSides.DIAGONAL_ASCENDING || childrenSidesByLayout == ChildrenSides.DIAGONAL_DESCENDING))
                        childrenSides  = (childrenSidesByLayout == ChildrenSides.DIAGONAL_ASCENDING)
                            == (childNodesAlignment == ChildNodesAlignment.AFTER_PARENT)
                                ? ChildrenSides.TOP_OR_LEFT
                                        :  ChildrenSides.BOTTOM_OR_RIGHT;
                else {
                    childrenSides  = side == Side.TOP_OR_LEFT
                            ? ChildrenSides.TOP_OR_LEFT
                                    :  ChildrenSides.BOTTOM_OR_RIGHT;
                }
            }
        }
        this.childrenSides = childrenSides;
    }

    private void updateSide() {
        final boolean isTopOrLeft;
        if (map.getLayoutType() == MapViewLayout.OUTLINE || viewedNode.isRoot()) {
            isTopOrLeft = false;
        }
        else {
            NodeView parent = getParentNodeView();
            ChildrenSides childrenSides = parent.childrenSides();
            if(childrenSides == ChildrenSides.TOP_OR_LEFT)
                isTopOrLeft = true;
            else if(childrenSides == ChildrenSides.BOTTOM_OR_RIGHT)
                isTopOrLeft = false;
            else if (parent.isRoot() || childrenSides == ChildrenSides.BOTH_SIDES) {
                Side side = viewedNode.getSide();
                if (side != Side.DEFAULT)
                    isTopOrLeft = side == Side.TOP_OR_LEFT;
                else
                    isTopOrLeft = parent.getNode().isTopOrLeft(viewedNode.getMap().getRootNode());
            } else
                isTopOrLeft = parent.isTopOrLeft();
        }
        this.side = isTopOrLeft ? Side.TOP_OR_LEFT :  Side.BOTTOM_OR_RIGHT;
    }



    private void updateShape() {
		if(mainView != null) {
			NodeViewFactory.getInstance().updateViewPainter(this);
		}
		else {
			final MainView newMainView = NodeViewFactory.getInstance().newMainView(this);
			setMainView(newMainView);
			if (map.getSelected() == this) {
				requestFocusInWindow();
			}
		}
	}

	boolean useSelectionColors() {
		return isSelected() && !MapView.drawsRectangleForSelection() && !map.isPrinting();
	}

	@Override
	protected void validateTree() {
		super.validateTree();
	}

	public void addContent(JComponent component, int pos) {
		component.putClientProperty("NODE_VIEW_CONTENT_POSITION", pos);
		final JComponent contentPane = getContentPane();
		for (int i = 0; i < contentPane.getComponentCount(); i++) {
			JComponent content = (JComponent) contentPane.getComponent(i);
			if (content == null)
				throw new RuntimeException("component " + i + "is null");
			final Object clientProperty = content.getClientProperty("NODE_VIEW_CONTENT_POSITION");
			if (clientProperty == null)
				throw new RuntimeException("NODE_VIEW_CONTENT_POSITION not set on component " + content.toString() + i
				        + "/" + contentPane.getComponentCount());
			if (pos < (Integer) clientProperty) {
				contentPane.add(component, i);
				return;
			}
		}
		contentPane.add(component);
	}

	public JComponent removeContent(int pos) {
		return removeContent(pos, true);
	}

	private JComponent removeContent(int pos, boolean remove) {
		if (contentPane == null)
			return null;
		for (int i = 0; i < contentPane.getComponentCount(); i++) {
			JComponent component = (JComponent) contentPane.getComponent(i);
			Integer contentPos = (Integer) component.getClientProperty("NODE_VIEW_CONTENT_POSITION");
			if (contentPos == null) {
				continue;
			}
			if (contentPos == pos) {
				if (remove) {
					component.putClientProperty("NODE_VIEW_CONTENT_POSITION", null);
					contentPane.remove(i);
				}
				return component;
			}
			if (contentPos > pos) {
				return null;
			}
		}
		return null;
	}

	public JComponent getContent(int pos) {
		return removeContent(pos, false);
	}

	public boolean isSummary() {
		return SummaryNode.isSummaryNode(getNode());
	}

	public boolean isFirstGroupNode() {
		return SummaryNode.isFirstGroupNode(getNode());
	}

	public boolean isFree() {
		return FreeNode.isFreeNode(getNode());
	}

	@Override
	public void setBounds(int x, int y, int width, int height) {
	    if(x != getX() || y != getY() || width != getWidth() || height != getHeight()) {
	        Rules rule = edgeColor.getRule();
	        if(EdgeController.Rules.BY_PARENT != rule)
	            edgeColor.resetCache();
	        repaintEdge();
	    }
		super.setBounds(x, y, width, height);
	}

	boolean isSubtreeVisible() {
	    if(isValid())
	        return getHeight() > 2 * getSpaceAround();
	    if(isContentVisible())
	        return true;
	    final Component[] components = getComponents();
	    for (int i = 0; i < components.length; i++) {
	        if (!(components[i] instanceof NodeView)) {
	            continue;
	        }
	        final NodeView view = (NodeView) components[i];
	        if(view.isSubtreeVisible())
	            return true;
	    }
	    return false;
	}



	public enum Properties{HIDDEN_CHILDREN}

	boolean isChildHidden(NodeModel node) {
		@SuppressWarnings("unchecked")
		final Set<NodeModel> hiddenChildren = (Set<NodeModel>) getClientProperty(Properties.HIDDEN_CHILDREN);
		return hiddenChildren != null && hiddenChildren.contains(node);
	}

	public int getHiddenChildCount() {
		@SuppressWarnings("unchecked")
		final Set<NodeModel> hiddenChildren = (Set<NodeModel>) getClientProperty(Properties.HIDDEN_CHILDREN);
		return hiddenChildren != null ? hiddenChildren.size() : 0;
	}

	boolean hasHiddenChildren() {
		@SuppressWarnings("unchecked")
		final Set<NodeModel> hiddenChildren = (Set<NodeModel>) getClientProperty(Properties.HIDDEN_CHILDREN);
		return hiddenChildren != null && ! hiddenChildren.isEmpty();
	}

	boolean unfoldHiddenChildren() {
		final boolean hasHiddenChildren = hasHiddenChildren();
		putClientProperty(Properties.HIDDEN_CHILDREN, null);
		return hasHiddenChildren;
	}

	public void hideChildren(NodeModel node) {
		final HashSet<NodeModel> set = new HashSet<>();
		set.addAll(node.getChildren());
		putClientProperty(Properties.HIDDEN_CHILDREN, set);
	}

	public boolean showHiddenNode(NodeModel node) {
		@SuppressWarnings("unchecked")
		final Set<NodeModel> hiddenChildren = (Set<NodeModel>) getClientProperty(Properties.HIDDEN_CHILDREN);
		return hiddenChildren != null && hiddenChildren.remove(node);
	}

    public Rectangle getInnerBounds() {
        int spaceAround = getSpaceAround();
        if (isContentVisible())
            return new Rectangle(spaceAround, spaceAround, getWidth() - 2 * spaceAround, getHeight() - 2 * spaceAround);
        else {
            Rectangle innerBounds = new Rectangle(spaceAround, spaceAround, -1, -1);
            getChildrenViews().stream()
            .map(v -> {
                Rectangle r = v.getInnerBounds();
                r.x += v.getX();
                r.y += v.getY();
                return r;
            })
            .forEach(innerBounds::add);
            innerBounds.y = spaceAround;
            innerBounds.width = Math.max(0, innerBounds.width);
            innerBounds.height = getHeight() - 2 * spaceAround;
            return innerBounds;
        }
    }

    public StyleOption getStyleOption() {
        return useSelectionColors() ? StyleOption.FOR_SELECTED_NODE : StyleOption.FOR_UNSELECTED_NODE;
    }

	NodeViewLayoutHelper getLayoutHelper() {
	    return layoutHelper;
	}

    public boolean usesHorizontalLayout() {
        return layoutOrientation() == LayoutOrientation.LEFT_TO_RIGHT;
    }

    public LayoutOrientation layoutOrientation() {
        updateLayoutProperties();
	    return layoutOrientation;
    }

    private void updateLayoutOrientation() {
        if(map.isOutlineLayoutSet())
            this.layoutOrientation = LayoutOrientation.TOP_TO_BOTTOM;
        else {
            LayoutOrientation layoutOrientation = childNodesLayout.layoutOrientation();
            switch(layoutOrientation) {
            case TOP_TO_BOTTOM:
            case LEFT_TO_RIGHT:
                    this.layoutOrientation = layoutOrientation;
                break;
            default:
                NodeView parent = getParentNodeView();
                if(parent != null)
                    this.layoutOrientation = parent.layoutOrientation();
                else
                    this.layoutOrientation = LayoutOrientation.TOP_TO_BOTTOM;
            }
        }
	}

	boolean paintsChildrenOnTheLeft() {
	    if(usesHorizontalLayout())
	        return false;
        else {
            return paintsChildrenOnTopOrLeft();
        }
     }



    boolean paintsChildrenOnTopOrLeft() {
        ChildrenSides childrenSides = childrenSides();
        boolean paintsChildrenOnBothSides  = childrenSides == ChildrenSides.BOTH_SIDES || isRoot();
        return paintsChildrenOnBothSides ? false
                : childrenSides == ChildrenSides.BOTTOM_OR_RIGHT ? false
                        : childrenSides == ChildrenSides.TOP_OR_LEFT ? true
                                : isTopOrLeft();
    }
}
