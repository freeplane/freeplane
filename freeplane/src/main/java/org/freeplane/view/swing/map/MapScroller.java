package org.freeplane.view.swing.map;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;
import java.util.function.Supplier;

import javax.swing.JComponent;
import javax.swing.JViewport;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.map.IMapSelection.NodePosition;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.NodeRelativePath;
import org.freeplane.view.swing.map.MapViewScrollPane.MapViewPort;

class MapScroller {

    private enum ScrollingDirective {
        SCROLL_NODE_TO_CENTER(NodePosition.CENTER),
        SCROLL_NODE_TO_LEFT_MARGIN(NodePosition.LEFT),
        SCROLL_NODE_TO_RIGHT_MARGIN(NodePosition.RIGHT),
        SCROLL_NODE_TO_TOP_MARGIN(NodePosition.TOP),
        SCROLL_NODE_TO_BOTTOM_MARGIN(NodePosition.BOTTOM),
        SCROLL_NODE_TO_TOP_LEFT_CORNER(NodePosition.TOP_LEFT),
        SCROLL_NODE_TO_TOP_RIGHT_CORNER(NodePosition.TOP_RIGHT),
        SCROLL_NODE_TO_BOTTOM_LEFT_CORNER(NodePosition.BOTTOM_LEFT),
        SCROLL_NODE_TO_BOTTOM_RIGHT_CORNER(NodePosition.BOTTOM_RIGHT),
        SCROLL_TO_BEST_ROOT_POSITION,
        MAKE_NODE_VISIBLE, DONE,
        ANCHOR;
        static private class CompanionObject{
            private static final ScrollingDirective positionDirectiveMapping[] = new ScrollingDirective[NodePosition.values().length];
        }
        public static ScrollingDirective of(NodePosition position) {
            return CompanionObject.positionDirectiveMapping[position.ordinal()];
        }
        private ScrollingDirective() {/**/}
        private ScrollingDirective(NodePosition position) {
            CompanionObject.positionDirectiveMapping[position.ordinal()] = this;
        }
    }


	private static final String KEEP_SELECTED_NODE_VISIBLE_PROPERTY = "keepSelectedNodeVisible";
    private NodeView anchor;
	private Point anchorContentLocation;
	private float anchorHorizontalPoint;
	private float anchorVerticalPoint;
	private NodeView scrolledNode = null;
	private ScrollingDirective scrollingDirective = ScrollingDirective.DONE;
	private boolean showsSelectedAfterScroll = false;
    private boolean slowScroll;
	private int extraWidth;
	final private MapView map;
	private boolean scrollsNodeTreeToVisible;



	MapScroller(MapView map) {
		this.map = map;
		this.anchorContentLocation = null;
		this.anchor = null;
	}

    <T> T withPreservedState(Supplier<T> operation) {
        NodeView savedAnchor = anchor;
        Point savedAnchorContentLocation = anchorContentLocation == null ? null : new Point(anchorContentLocation);
        float savedHorizontalPoint = anchorHorizontalPoint;
        float savedVerticalPoint = anchorVerticalPoint;
        NodeView savedScrolledNode = scrolledNode;
        ScrollingDirective savedDirective = scrollingDirective;
        boolean savedShowsSelected = showsSelectedAfterScroll;
        boolean savedSlowScroll = slowScroll;
        int savedExtraWidth = extraWidth;
        boolean savedScrollsTree = scrollsNodeTreeToVisible;
        try {
            return operation.get();
        } finally {
            anchor = savedAnchor;
            anchorContentLocation = savedAnchorContentLocation;
            anchorHorizontalPoint = savedHorizontalPoint;
            anchorVerticalPoint = savedVerticalPoint;
            scrolledNode = savedScrolledNode;
            scrollingDirective = savedDirective;
            showsSelectedAfterScroll = savedShowsSelected;
            slowScroll = savedSlowScroll;
            extraWidth = savedExtraWidth;
            scrollsNodeTreeToVisible = savedScrollsTree;
        }
    }

	void anchorToNode(final NodeView view, final float horizontalPoint, final float verticalPoint) {
		if (view != null && view.getMainView() != null) {
			if(scrollingDirective == ScrollingDirective.DONE || view.isRoot() || anchor == null) {
				anchor = view;
			}
			else if (! (anchor.isRoot() || anchor == view || anchor.isAncestorOf(view))){
				final NodeModel lastAnchor = anchor.getNode();
				final NodeModel newAnchor = view.getNode();
				final NodeRelativePath nodeRelativePath = new NodeRelativePath(lastAnchor, newAnchor);
				final NodeModel commonAncestor = nodeRelativePath.commonAncestor();
				final NodeView newAnchorView = view.getMap().getNodeView(commonAncestor);
				if(newAnchorView  != null)
					anchor = newAnchorView;
			}
			anchorHorizontalPoint = horizontalPoint;
			anchorVerticalPoint = verticalPoint;
			this.anchorContentLocation = getAnchorCenterPoint();
			if (scrolledNode == null) {
				scrolledNode = anchor;
				scrollingDirective = ScrollingDirective.ANCHOR;
				extraWidth = 0;
				keepShowingSelectedAfterScroll();
			}
		}
	}

	/**
	 * Scroll the viewport of the map to the south-west, i.e. scroll the map
	 * itself to the north-east.
	 */
	void scrollBy(final int x, final int y) {
		final JViewport mapViewport = (JViewport) map.getParent();
		if (mapViewport != null) {
			final Point currentPoint = mapViewport.getViewPosition();
			currentPoint.translate(x, y);
			if (currentPoint.getX() < 0) {
				currentPoint.setLocation(0, currentPoint.getY());
			}
			if (currentPoint.getY() < 0) {
				currentPoint.setLocation(currentPoint.getX(), 0);
			}
			final double maxX = map.getSize().getWidth() - mapViewport.getExtentSize().getWidth();
			final double maxY = map.getSize().getHeight() - mapViewport.getExtentSize().getHeight();
			if (currentPoint.getX() > maxX) {
				currentPoint.setLocation(maxX, currentPoint.getY());
			}
			if (currentPoint.getY() > maxY) {
				currentPoint.setLocation(currentPoint.getX(), maxY);
			}
			mapViewport.setViewPosition(currentPoint);
		}
	}


    void scrollNode(NodeView nodeView, NodePosition position, boolean slowScroll) {
        showsSelectedAfterScroll = false;
        scrollNode(nodeView, ScrollingDirective.of(position), slowScroll);
    }

    void scrollNodeToCenter(NodeView node, boolean slow) {
        scrollNode(node, ScrollingDirective.SCROLL_NODE_TO_CENTER, slow);
    }

	boolean shouldScrollSlowly() {
		return ResourceController.getResourceController().getBooleanProperty("slow_scroll_selected_node");
	}


	private void scrollNode(final NodeView node, ScrollingDirective scrollingDirective, boolean slowScroll) {
		if (node != null) {
			this.slowScroll = slowScroll;
			scrolledNode = node;
			this.scrollingDirective = scrollingDirective;
			if (map.isDisplayable() && map.isFrameLayoutCompleted() && map.isValid()) {
				if(slowScroll)
					startSlowScrolling();
				scrollNodeNow();
			}
		}
	}

	private Rectangle calculateOptimalVisibleRectangle() {
		final JViewport viewPort = (JViewport) map.getParent();
		final Dimension extentSize = viewPort.getExtentSize();
		final JComponent content = scrolledNode.getContent();
		Point contentLocation = new Point();
		UITools.convertPointToAncestor(content, contentLocation, map);
		final Rectangle rect = new Rectangle(contentLocation.x + content.getWidth() / 2 - extentSize.width / 2,
				contentLocation.y + content.getHeight() / 2 - extentSize.height
				/ 2, extentSize.width, extentSize.height);

		final int distanceToMargin = (extentSize.width - content.getWidth()) / 2 - 10;
		final int distanceToMarginY = (extentSize.height - content.getHeight()) / 2 - 10;

		switch (scrollingDirective) {
		case SCROLL_NODE_TO_TOP_LEFT_CORNER:
			rect.x += distanceToMargin;
			rect.y += distanceToMarginY;
			break;

		case SCROLL_NODE_TO_BOTTOM_LEFT_CORNER:
			rect.x += distanceToMargin;
			rect.y -= distanceToMarginY;
			break;

		case SCROLL_NODE_TO_TOP_RIGHT_CORNER:
			rect.y += distanceToMarginY;
			rect.x -= distanceToMargin;
			break;

		case SCROLL_NODE_TO_BOTTOM_RIGHT_CORNER:
			rect.x -= distanceToMargin;
			rect.y -= distanceToMarginY;
			break;

		case SCROLL_NODE_TO_LEFT_MARGIN:
			rect.x += distanceToMargin;
			break;

		case SCROLL_NODE_TO_TOP_MARGIN:
			rect.y += distanceToMarginY;
			break;

		case SCROLL_NODE_TO_RIGHT_MARGIN:
			rect.x -= distanceToMargin;
			break;

		case SCROLL_NODE_TO_BOTTOM_MARGIN:
			rect.y -= distanceToMarginY;
			break;

		case SCROLL_TO_BEST_ROOT_POSITION:
			final Rectangle innerBounds = map.getInnerBounds();
			if(innerBounds.width <= extentSize.width && map.getModeController().shouldCenterCompactMaps()){
				rect.x = innerBounds.x - (extentSize.width - innerBounds.width) / 2;
			}
			else {
				NodeView root = map.getRoot();
				final boolean outlineLayoutSet = map.isOutlineLayoutSet();
				if(!outlineLayoutSet && ! root.usesHorizontalLayout()) {
					boolean scrollToTheLeft = false;
					final List<NodeModel> children = root.getNode().getChildren();
					if(! children.isEmpty()){
						scrollToTheLeft = true;
						for(NodeModel node :children) {
							if(node.isTopOrLeft(root.getNode())){
								scrollToTheLeft = false;
								break;
							}
						}
					}
					if(scrollToTheLeft)
						rect.x += (extentSize.width - content.getWidth()) / 2 - 10;
				}
			}
			break;
		default:
			break;
		}
		return rect;
	}

	private void scrollNodeNow() {
		final Rectangle rect = calculateOptimalVisibleRectangle();
		map.scrollRectToVisible(rect);
		scrolledNode = null;
		scrollingDirective = ScrollingDirective.DONE;
		this.slowScroll = false;
		this.anchor = null;
		this.anchorContentLocation = getAnchorCenterPoint();
	}

	private void startSlowScrolling() {
		final MapViewPort viewPort = (MapViewPort) map.getParent();
		viewPort.startSlowScrolling(20);
	}

	private Point getAnchorCenterPoint() {
		if (! map.isDisplayable()) {
			return null;
		}
		final JComponent mainView = getAnchor().getMainView();
		final int referenceWidth = mainView.getWidth();
		final int referenceHeight = mainView.getHeight();
		final JViewport viewPort = (JViewport) map.getParent();
		final Point anchorCenterPoint = new Point((int) (referenceWidth * anchorHorizontalPoint) - viewPort.getWidth()/2,
				(int) (referenceHeight * anchorVerticalPoint) - viewPort.getHeight()/2);
		UITools.convertPointToAncestor(mainView, anchorCenterPoint, viewPort);
		return anchorCenterPoint;
	}

	private NodeView getAnchor() {
		return anchor != null ? anchor : map.getRoot();
	}

	void scrollNodeToVisible(final NodeView node) {
		scrollNodeToVisible(node, 0);
	}

	private void scrollNodeToVisible(final NodeView node, final int extraWidth) {
		if(node == null)
			return;
		if(scrollingDirective == ScrollingDirective.DONE
		        || scrollingDirective == ScrollingDirective.ANCHOR) {
            scrollingDirective = ScrollingDirective.MAKE_NODE_VISIBLE;
            showsSelectedAfterScroll = false;
        }
		if (scrolledNode != null && scrollingDirective != ScrollingDirective.MAKE_NODE_VISIBLE) {
			if (node != scrolledNode) {
				if (scrollingDirective == ScrollingDirective.SCROLL_TO_BEST_ROOT_POSITION && !node.isRoot()) {
					showsSelectedAfterScroll = true;
					return;
				}
				scrollNode(node, scrollingDirective, false);
			}
			return;
		}
		if (!map.isValid()) {
			scrolledNode = node;
			scrollingDirective = ScrollingDirective.MAKE_NODE_VISIBLE;
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
		scrollingDirective = ScrollingDirective.DONE;
		scrolledNode = null;
	}

	void scrollToRootNode() {
		scrollNode(map.getRoot(), ScrollingDirective.SCROLL_TO_BEST_ROOT_POSITION, false);
	}

	void scrollView() {
		final JViewport vp = (JViewport) map.getParent();
		final Point viewPosition = vp.getViewPosition();
		final Point oldAnchorContentLocation = anchorContentLocation;
		final Point newAnchorContentLocation = getAnchorCenterPoint();
		if(oldAnchorContentLocation != null && (slowScroll ||
				scrollingDirective == ScrollingDirective.DONE ||
				scrollingDirective == ScrollingDirective.MAKE_NODE_VISIBLE ||
				scrollingDirective == ScrollingDirective.ANCHOR)) {
			final int deltaX = newAnchorContentLocation.x - oldAnchorContentLocation.x;
			final int deltaY = newAnchorContentLocation.y - oldAnchorContentLocation.y;
			if (deltaX != 0 || deltaY != 0) {
				viewPosition.x += deltaX;
				viewPosition.y += deltaY;
				vp.setViewPosition(viewPosition);
			}
		}
		final NodeView scrolledNode = this.scrolledNode;
		if(scrolledNode != null && scrollingDirective != ScrollingDirective.MAKE_NODE_VISIBLE
				&& scrollingDirective != ScrollingDirective.ANCHOR){
			scrollNode(scrolledNode, scrollingDirective, slowScroll);
		}

		if(scrollsNodeTreeToVisible && scrolledNode != null) {
			scrollNodeTreeToVisible(scrolledNode, slowScroll);
		}
        showSelectedAfterScroll();
		if(scrolledNode != null &&
		        (scrollingDirective == ScrollingDirective.MAKE_NODE_VISIBLE))
			scrollNodeToVisible(scrolledNode, extraWidth);
		this.scrolledNode = null;
		scrollingDirective = ScrollingDirective.DONE;
		anchor = null;
		anchorHorizontalPoint = anchorVerticalPoint = 0.5f;
		this.anchorContentLocation = getAnchorCenterPoint();
	}

    private void showSelectedAfterScroll() {
        if(showsSelectedAfterScroll) {
            showsSelectedAfterScroll = false;
            scrollNodeToVisible(map.getSelectionEnd(), 0);
        }
    }

	void setAnchorContentLocation(){
		if(scrollingDirective == ScrollingDirective.SCROLL_TO_BEST_ROOT_POSITION)
			scrollView();
		else
			anchorContentLocation = getAnchorCenterPoint();
	}

	void scrollNodeTreeToVisible(NodeView node, boolean slow) {
		if(! map.isValid()) {
			scrolledNode = node;
			scrollsNodeTreeToVisible = true;
			slowScroll = slow;
			return;
		}
		MapViewPort viewport = (MapViewPort) map.getParent();
		final Rectangle visibleRect = viewport.getViewRect();
		Rectangle requiredRectangle = new Rectangle(node.getSize());
		int margin = 30;
		int spaceToCut = node.getSpaceAround() - margin;
		requiredRectangle.x += spaceToCut;
		requiredRectangle.y += spaceToCut;
		requiredRectangle.width -= 2*spaceToCut;
		requiredRectangle.height -= 2*spaceToCut;
		final Rectangle contentBounds = node.getContent().getBounds();
		int lackingWidth = requiredRectangle.width - visibleRect.width;
		if(lackingWidth > 0){
			int leftGap = contentBounds.x - requiredRectangle.x - margin;
			if(leftGap > 0) {
			    int rightGap = requiredRectangle.x + requiredRectangle. width  - contentBounds.x - contentBounds.width - margin;
			    requiredRectangle.width  = visibleRect.width;
			    requiredRectangle.x += lackingWidth * leftGap /  (leftGap + rightGap);
			}
		}
		int lackingHeight = requiredRectangle.height - visibleRect.height;
		if(lackingHeight > 0){
			int topGap = contentBounds.y - requiredRectangle.y - margin;
			if(topGap > 0) {
				int bottomGap = requiredRectangle.y + requiredRectangle. height  - contentBounds.y - contentBounds.height - margin;
				requiredRectangle.height  = visibleRect.height;
				requiredRectangle.y += lackingHeight * topGap /  (topGap + bottomGap);
			}
		}
		keepShowingSelectedAfterScroll();
		if(slow)
			startSlowScrolling();
		scrollsNodeTreeToVisible = false;
		node.scrollRectToVisible(requiredRectangle);
		showSelectedAfterScroll();
	}

	void anchorToRoot() {
		final NodeView root = map.getRoot();
		if(anchor != null && ! anchor.equals(root))
		    anchorToNode(root, 0, 0);
	}

    boolean isShowsSelectedAfterScroll() {
        return showsSelectedAfterScroll;
    }

    void setShowsSelectedAfterScroll(boolean showSelectedAfterScroll) {
        this.showsSelectedAfterScroll = showSelectedAfterScroll;
    }

	private void keepShowingSelectedAfterScroll() {
	    if (! showsSelectedAfterScroll
	            && ResourceController.getResourceController().getBooleanProperty(KEEP_SELECTED_NODE_VISIBLE_PROPERTY)) {
	        JComponent selectionEndContent = map.getSelectionEnd().getContent();
	        Rectangle selectionEndVisibleRectangle = selectionEndContent.getVisibleRect();
	        showsSelectedAfterScroll = selectionEndVisibleRectangle.width >0 && selectionEndVisibleRectangle.height >0;
	    }
	}

}

