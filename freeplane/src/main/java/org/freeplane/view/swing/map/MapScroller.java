package org.freeplane.view.swing.map;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JViewport;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.IMapSelection.NodePosition;
import org.freeplane.features.ui.ViewController;

class MapScroller {

	private NodeView anchor;
	private Point anchorContentLocation;
	private float anchorHorizontalPoint;
	private float anchorVerticalPoint;
	private NodeView scrolledNode = null;
	private ScrollingDirective scrollingDirective = ScrollingDirective.DONE;
	private boolean slowScroll;
	private int extraWidth;
	final private MapView map;
	
	

	public MapScroller(MapView map) {
		this.map = map;
		this.anchorContentLocation = new Point();
	}

	void anchorToNode(final NodeView view, final float horizontalPoint, final float verticalPoint) {
		if (view != null && view.getMainView() != null) {
			setAnchorView(view);
			anchorHorizontalPoint = horizontalPoint;
			anchorVerticalPoint = verticalPoint;
			this.anchorContentLocation = getAnchorCenterPoint();
			if (scrolledNode == null) {
				scrolledNode = anchor;
				scrollingDirective = ScrollingDirective.ANCHOR;
				extraWidth = 0;
			}
		}
	}

	/**
	 * Scroll the viewport of the map to the south-west, i.e. scroll the map
	 * itself to the north-east.
	 */
	public void scrollBy(final int x, final int y) {
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

	public void scrollNode(final NodeView node, ScrollingDirective scrollingDirective, boolean slowScroll) {
		if (node != null) {
			this.slowScroll = slowScroll;
			scrolledNode = node;
			this.scrollingDirective = scrollingDirective;
			if (map.isDisplayable() && map.frameLayoutCompleted() && map.isValid())
				scrollNodeNow(slowScroll);
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
		if(scrollingDirective == ScrollingDirective.SCROLL_NODE_TO_LEFT_MARGIN){
			rect.x += distanceToMargin;
		}
		if(scrollingDirective == ScrollingDirective.SCROLL_NODE_TO_RIGHT_MARGIN){
			rect.x -= distanceToMargin;
		}
		if(scrollingDirective == ScrollingDirective.SCROLL_TO_BEST_ROOT_POSITION){
			final Rectangle innerBounds = map.getInnerBounds();
			if(innerBounds.width <= extentSize.width && map.getModeController().shouldCenterCompactMaps()){
				rect.x = innerBounds.x - (extentSize.width - innerBounds.width) / 2;
			}
			else {
				NodeView root = map.getRoot();
				final boolean outlineLayoutSet = map.isOutlineLayoutSet();
				if(!outlineLayoutSet) {
					boolean scrollToTheLeft = false;
					final List<NodeModel> children = root.getModel().getChildren();
					if(! children.isEmpty()){
						scrollToTheLeft = true;
						for(NodeModel node :children) {
							if(node.isLeft()){
								scrollToTheLeft = false;
								break;
							}
						}
					}
					if(scrollToTheLeft)
						rect.x += (extentSize.width - content.getWidth()) / 2 - 10;
				}
			}
		}
		return rect;
	}
	
	private void scrollNodeNow(boolean slowScroll) {
		final JViewport viewPort = (JViewport) map.getParent();
		if(slowScroll)
			viewPort.putClientProperty(ViewController.SLOW_SCROLLING, 20);
		final Rectangle rect = calculateOptimalVisibleRectangle();
		map.scrollRectToVisible(rect);
		scrolledNode = null;
		scrollingDirective = ScrollingDirective.DONE;
		this.slowScroll = false;
		if(! anchor.equals(map.getRoot()))
			this.anchor = map.getRoot();
		this.anchorContentLocation = getAnchorCenterPoint();
	}

	void setAnchorView(final NodeView view) {
		anchor = view;
	}

	private Point getAnchorCenterPoint() {
		if (! map.isDisplayable()) {
			return new Point();
		}
		final MainView mainView = anchor.getMainView();
		final int mainViewWidth = mainView.getWidth();
		final int mainViewHeight = mainView.getHeight();
		final Point anchorCenterPoint = new Point((int) (mainViewWidth * anchorHorizontalPoint), (int) (mainViewHeight * anchorVerticalPoint));
		final JViewport viewPort = (JViewport) map.getParent();
		UITools.convertPointToAncestor(mainView, anchorCenterPoint, map);
		final Dimension extentSize = viewPort.getExtentSize();
		anchorCenterPoint.x += (extentSize.width - viewPort.getWidth()) / 2;
		anchorCenterPoint.y += (extentSize.height - viewPort.getHeight()) / 2;
		return anchorCenterPoint;
	}

	public void scrollNodeToVisible(final NodeView node) {
		scrollNodeToVisible(node, 0);
	}

	private void scrollNodeToVisible(final NodeView node, final int extraWidth) {
		if(node == null)
			return;
		if(scrollingDirective == ScrollingDirective.DONE || scrollingDirective == ScrollingDirective.ANCHOR)
			scrollingDirective = ScrollingDirective.MAKE_NODE_VISIBLE;
		if (scrolledNode != null && scrollingDirective != ScrollingDirective.MAKE_NODE_VISIBLE) {
			if (node != scrolledNode) {
				if (scrollingDirective == ScrollingDirective.SCROLL_TO_BEST_ROOT_POSITION && !node.isRoot())
					scrollingDirective = ScrollingDirective.SCROLL_NODE_TO_CENTER;
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
	}

	void scrollToRootNode() {
		scrollNode(map.getRoot(), ScrollingDirective.SCROLL_TO_BEST_ROOT_POSITION, false);
	}

	void scrollView() {
		if(scrolledNode != null && scrollingDirective != ScrollingDirective.MAKE_NODE_VISIBLE
				&& scrollingDirective != ScrollingDirective.ANCHOR){
			scrollNode(scrolledNode, scrollingDirective, slowScroll);
			return;
		}
		if (anchorContentLocation.getX() == 0 && anchorContentLocation.getY() == 0) {
			return;
		}
		final JViewport vp = (JViewport) map.getParent();
		final int scrollMode = vp.getScrollMode();
		vp.setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
		final Point viewPosition = vp.getViewPosition();
		final Point oldAnchorContentLocation = anchorContentLocation;
		final Point newAnchorContentLocation = getAnchorCenterPoint();
		final int deltaX = newAnchorContentLocation.x - oldAnchorContentLocation.x;
		final int deltaY = newAnchorContentLocation.y - oldAnchorContentLocation.y;
		if (deltaX != 0 || deltaY != 0) {
			viewPosition.x += deltaX;
			viewPosition.y += deltaY;
			vp.setViewPosition(viewPosition);
		}
		else {
			map.repaintVisible();
		}
//		if (scrolledNode == null){
//			scrolledNode = map.getSelected();
//			scrollingDirective = ScrollingDirective.MAKE_NODE_VISIBLE;
//		}
		if(scrolledNode != null)
			scrollNodeToVisible(scrolledNode, extraWidth);
		vp.setScrollMode(scrollMode);
		scrolledNode = null;
		scrollingDirective = ScrollingDirective.DONE;
		setAnchorView(map.getRoot());
		anchorHorizontalPoint = anchorVerticalPoint = 0;
		this.anchorContentLocation = getAnchorCenterPoint();
	}
	
	void setAnchorContentLocation(){
		anchorContentLocation = getAnchorCenterPoint();
	}

	public void scrollNodeTreeToVisible(NodeView node) {
		final Rectangle visibleRect = map.getVisibleRect();
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
			int rightGap = requiredRectangle.x + requiredRectangle. width  - contentBounds.x - contentBounds.width - margin;
			requiredRectangle.width  = visibleRect.width;
			requiredRectangle.x += lackingWidth * leftGap /  (leftGap + rightGap);
		}
		int lackingHeight = requiredRectangle.height - visibleRect.height;
		if(lackingHeight > 0){
			int topGap = contentBounds.y - requiredRectangle.y - margin;
			int bottomGap = requiredRectangle.y + requiredRectangle. height  - contentBounds.y - contentBounds.height - margin;
			requiredRectangle.height  = visibleRect.height;
			requiredRectangle.y += lackingHeight * topGap /  (topGap + bottomGap);
		}
		if(! node.getVisibleRect().contains(requiredRectangle)){
			node.scrollRectToVisible(requiredRectangle);
		}
	}

	void anchorToRoot() {
		final NodeView root = map.getRoot();
		if(! root.equals(anchor))
			anchorToNode(root, 0, 0);
	}

}

enum ScrollingDirective {
	SCROLL_NODE_TO_CENTER, SCROLL_NODE_TO_LEFT_MARGIN, SCROLL_NODE_TO_RIGHT_MARGIN, SCROLL_TO_BEST_ROOT_POSITION, MAKE_NODE_VISIBLE, DONE, ANCHOR;
	private static ScrollingDirective positionDirective[] = {SCROLL_NODE_TO_LEFT_MARGIN, SCROLL_NODE_TO_CENTER, SCROLL_NODE_TO_RIGHT_MARGIN};
	public static ScrollingDirective of(NodePosition position) {
		return positionDirective[position.ordinal()];
	}
}
