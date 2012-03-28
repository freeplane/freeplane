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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JToolTip;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.FreeplaneMenuBar;
import org.freeplane.core.ui.components.MultipleImage;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.icon.IconController;
import org.freeplane.features.icon.MindIcon;
import org.freeplane.features.icon.UIIcon;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.NodeLinks;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.nodelocation.LocationModel;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.styles.MapViewLayout;
import org.freeplane.features.text.IContentTransformer;
import org.freeplane.features.text.TextController;


/**
 * Base class for all node views.
 */
public abstract class MainView extends ZoomableLabel {
    private static final int ADDITIONAL_VERTICAL_FOLDING_AREA = 9;
	private static final int ADDITIONAL_HORIZONTAL_FOLDING_AREA = 32;
	private static final String USE_COMMON_OUT_POINT_FOR_ROOT_NODE_STRING = "use_common_out_point_for_root_node";
    public static boolean USE_COMMON_OUT_POINT_FOR_ROOT_NODE = ResourceController.getResourceController().getBooleanProperty(USE_COMMON_OUT_POINT_FOR_ROOT_NODE_STRING);

	static Dimension maximumSize = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
	static Dimension minimumSize = new Dimension(0,0);
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected int isDraggedOver = NodeView.DRAGGED_OVER_NO;
	private boolean isShortened;
	private TextModificationState textModified = TextModificationState.NONE;
	private MouseArea mouseArea = MouseArea.DEFAULT;
	private static final int LISTENER_VIEW_WIDTH = 10;

	boolean isShortened() {
    	return isShortened;
    }

	MainView() {
		setAlignmentX(Component.CENTER_ALIGNMENT);
		setHorizontalAlignment(SwingConstants.LEFT);
		setVerticalAlignment(SwingConstants.CENTER);
		setHorizontalTextPosition(SwingConstants.TRAILING);
		setVerticalTextPosition(JLabel.TOP);
	}

	protected void convertPointFromMap(final Point p) {
		UITools.convertPointFromAncestor(getMap(), p, this);
	}

	protected void convertPointToMap(final Point p) {
		UITools.convertPointToAncestor(this, p, getMap());
	}

	public boolean dropAsSibling(final double xCoord) {
		if(dropLeft(xCoord))
		return ! isInVerticalRegion(xCoord, 2. / 3);
		else
			return isInVerticalRegion(xCoord, 1. / 3);
	}

	/** @return true if should be on the left, false otherwise. */
	public boolean dropLeft(final double xCoord) {
		/* here it is the same as me. */
		return getNodeView().isLeft();
	}

	/** get x coordinate including folding symbol */
	public int getDeltaX() {
		return 0;
	}

	/** get y coordinate including folding symbol */
	public int getDeltaY() {
		return 0;
	}

	public int getDraggedOver() {
		return isDraggedOver;
	}

	public abstract Point getLeftPoint();

	/** get height including folding symbol */
	protected int getMainViewHeightWithFoldingMark() {
		return getHeight();
	}

	/** get width including folding symbol */
	protected int getMainViewWidthWithFoldingMark() {
		return getWidth();
	}

	@Override
	public Dimension getMaximumSize() {
		return MainView.maximumSize;
	}

	@Override
	public Dimension getMinimumSize() {
		return MainView.minimumSize;
	}

	public abstract Point getRightPoint();

	public abstract String getShape();

	int getZoomedFoldingSymbolHalfWidth() {
		return getNodeView().getZoomedFoldingSymbolHalfWidth();
	}

	public boolean isInFollowLinkRegion(final double xCoord) {
		final NodeView nodeView = getNodeView();
		final NodeModel model = nodeView.getModel();
		if (NodeLinks.getValidLink(model) == null)
			return false;
		Rectangle iconR = ((ZoomableLabelUI)getUI()).getIconR(this);
		return xCoord >= iconR.x && xCoord < iconR.x + iconR.width;
	}

	private boolean isRightOrOutline() {
		final NodeView nodeView = getNodeView();
		if (!nodeView.isLeft() || MapViewLayout.OUTLINE.equals(nodeView.getMap().getLayoutType()))
			return true;
		else
			return false;
	}

	public boolean isInLeftFoldingRegion(Point p) {
		return canBeFolded()
				&& p.x < getWidth()
				&& isNearContent(p)	
				&& !isRightOrOutline();
	}

	public boolean isInRightFoldingRegion(Point p) {
		return  canBeFolded() 
				&& p.x >= 0
				&& isNearContent(p)	
				&& isRightOrOutline();
	}

	private boolean isNearContent(Point p) {
		final JComponent content = getNodeView().getContent();
		if(content != this){
			p = new Point(p.x + getX(), p.y + getY());
			for(int i = 0; i < content.getComponentCount(); i++){
				final Component component = content.getComponent(i);
				if(component.isVisible() && component.getBounds().contains(p))
					return false;
			}
		}
		else if(isInside(p))
			return false;
		final Rectangle contentBounds = new Rectangle(0, 0, content.getWidth(), content.getHeight());
		contentBounds.x -= ADDITIONAL_HORIZONTAL_FOLDING_AREA;
		contentBounds.width += 2 * ADDITIONAL_HORIZONTAL_FOLDING_AREA;
		contentBounds.height += ADDITIONAL_VERTICAL_FOLDING_AREA;
		return contentBounds.contains(p);
	}

	private boolean isInside(Point p) {
		final Rectangle bounds = new Rectangle(0, 0, getWidth(), getHeight());
		return bounds.contains(p);
	}

	private boolean canBeFolded() {
		final NodeModel node = getNodeView().getModel();
		return !node.isRoot() && node.hasChildren();
	}

	/**
	 * Determines whether or not the xCoord is in the part p of the node: if
	 * node is on the left: part [1-p,1] if node is on the right: part[ 0,p] of
	 * the total width.
	 */
	public boolean isInVerticalRegion(final double xCoord, final double p) {
		return xCoord < getSize().width * p;
	}

	protected void paintBackground(final Graphics2D graphics, final Color color) {
		graphics.setColor(color);
		graphics.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
	}

	public void paintDragOver(final Graphics2D graphics) {
		if (isDraggedOver == NodeView.DRAGGED_OVER_SON || getMouseArea().equals(MouseArea.FOLDING)) {
			paintDragOverSon(graphics);
		}
		if (isDraggedOver == NodeView.DRAGGED_OVER_SIBLING) {
			paintDragOverSibling(graphics);
		}
	}

	private void paintDragOverSibling(final Graphics2D graphics) {
		graphics.setPaint(new GradientPaint(0, getHeight() * 3 / 5, getMap().getBackground(), 0, getHeight() / 5,
		    NodeView.dragColor));
		graphics.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
	}

	private void paintDragOverSon(final Graphics2D graphics) {
		if (getNodeView().isLeft()) {
			graphics.setPaint(new GradientPaint(getWidth() * 3 / 4, 0, getMap().getBackground(), getWidth() / 4, 0,
			    NodeView.dragColor));
			graphics.fillRect(0, 0, getWidth() * 3 / 4, getHeight() - 1);
		}
		else {
			graphics.setPaint(new GradientPaint(getWidth() / 4, 0, getMap().getBackground(), getWidth() * 3 / 4, 0,
			    NodeView.dragColor));
			graphics.fillRect(getWidth() / 4, 0, getWidth() - 1, getHeight() - 1);
		}
	}

	void paintFoldingMark(final NodeView nodeView, final Graphics2D g, final Point p, FoldingMark foldingMarkType) {
		foldingMarkType.draw(nodeView, g, p);
	}
	
	static private FoldingMark foldingMarkType(MapController mapController, NodeModel node) {
		if (mapController.isFolded(node) && (node.isVisible() || node.getFilterInfo().isAncestor())) {
			return FoldingMark.ITSELF_FOLDED;
		}
		for (final NodeModel child : mapController.childrenUnfolded(node)) {
			if (!child.isVisible() && !FoldingMark.UNFOLDED.equals(foldingMarkType(mapController, child))) {
				return FoldingMark.UNVISIBLE_CHILDREN_FOLDED;
			}
		}
		return FoldingMark.UNFOLDED;
	}

	void paintDecoration(final NodeView nodeView, final Graphics2D g) {
		drawModificationRect(g);
		paintDragRectangle(g);
		final FoldingMark markType = foldingMarkType(getMap().getModeController().getMapController(), nodeView.getModel());
		if (!markType.equals(FoldingMark.UNFOLDED)) {
			final Point out = nodeView.isLeft() ? getLeftPoint() : getRightPoint();
			paintFoldingMark(nodeView, g, out, markType);
		}
        if (isShortened()) {
            final Point in =  nodeView.isLeft() ? getRightPoint() : getLeftPoint();
            in.y = getHeight() / 2;
            FoldingMark.SHORTENED.draw(nodeView, g, in);
        }
	}

	private void paintDragRectangle(final Graphics g) {
		if (! MouseArea.MOTION.equals(mouseArea)) 
			return;
		final Graphics2D g2 = (Graphics2D) g;
		final Object renderingHint = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
		final MapView parent = (MapView) SwingUtilities.getAncestorOfClass(MapView.class, this);
		parent.getModeController().getController().getViewController().setEdgesRenderingHint(g2);
		final Color color = g2.getColor();
		NodeView movedView = getNodeView();
		Rectangle r = getDragRectangle();
		if (movedView .isFree()) {
			g2.setColor(Color.BLUE);
			g.fillOval(r.x, r.y, r.width - 1, r.height - 1);
		}
		else if (LocationModel.getModel(movedView.getModel()).getHGap() <= 0) {
			g2.setColor(Color.RED);
			g.fillOval(r.x, r.y, r.width- 1, r.height- 1);
		}
		g2.setColor(Color.BLACK);
		g.drawOval(r.x, r.y, r.width- 1, r.height- 1);
		g2.setColor(color);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, renderingHint);
	}

	public Rectangle getDragRectangle() {
		final int size = MainView.LISTENER_VIEW_WIDTH;
		Rectangle r;
		if(getNodeView().isLeft())
			r = new Rectangle(getWidth(), -size, size, getHeight() + size * 2);
		else
			r = new Rectangle(-size, -size, size, getHeight() + size * 2);
		return r;
	}

    private void drawModificationRect(Graphics g) {
		final Color color = g.getColor();
		if(TextModificationState.SUCCESS.equals(textModified)){
			final boolean dontMarkTransformedText = Controller.getCurrentController().getResourceController()
		    .getBooleanProperty(IContentTransformer.DONT_MARK_TRANSFORMED_TEXT);
			if(dontMarkTransformedText)
				return;
			g.setColor(Color.GREEN);
		}
		else if(TextModificationState.FAILURE.equals(textModified)){
			g.setColor(Color.RED);
		}
		else{
			return;
		}
		g.drawRect(0, 0, getWidth(), getHeight());
		g.setColor(color);
    }

	public void paintBackgound(final Graphics2D g) {
		final Color color;
		if (getNodeView().useSelectionColors()) {
			color = getNodeView().getSelectedColor();
			paintBackground(g, color);
		}
		else {
			color = getNodeView().getTextBackground();
		}
		paintBackground(g, color);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#processKeyBinding(javax.swing.KeyStroke,
	 * java.awt.event.KeyEvent, int, boolean)
	 */
	@Override
	protected boolean processKeyBinding(final KeyStroke ks, final KeyEvent e, final int condition, final boolean pressed) {
		if (super.processKeyBinding(ks, e, condition, pressed)) {
			return true;
		}
		final MapView mapView = (MapView) SwingUtilities.getAncestorOfClass(MapView.class, this);
		final FreeplaneMenuBar freeplaneMenuBar = mapView.getModeController().getController().getViewController()
		    .getFreeplaneMenuBar();
		return !freeplaneMenuBar.isVisible()
		        && freeplaneMenuBar.processKeyBinding(ks, e, JComponent.WHEN_IN_FOCUSED_WINDOW, pressed);
	}

	public void setDraggedOver(final int draggedOver) {
		isDraggedOver = draggedOver;
	}

	public void setDraggedOver(final Point p) {
		setDraggedOver((dropAsSibling(p.getX())) ? NodeView.DRAGGED_OVER_SIBLING : NodeView.DRAGGED_OVER_SON);
	}

	public void updateFont(final NodeView node) {
		final Font font = NodeStyleController.getController(node.getMap().getModeController()).getFont(node.getModel());
		setFont(font);
	}

	void updateIcons(final NodeView node) {
//		setHorizontalTextPosition(node.isLeft() ? SwingConstants.LEADING : SwingConstants.TRAILING);
		final MultipleImage iconImages = new MultipleImage();
		/* fc, 06.10.2003: images? */
		final NodeModel model = node.getModel();
		for (final UIIcon icon : IconController.getController().getStateIcons(model)) {
			iconImages.addImage(icon.getIcon());
		}
		final ModeController modeController = getNodeView().getMap().getModeController();
		final Collection<MindIcon> icons = IconController.getController(modeController).getIcons(model);
		for (final MindIcon myIcon : icons) {
			iconImages.addImage(myIcon.getIcon());
		}
		addOwnIcons(iconImages, model);
		setIcon((iconImages.getImageCount() > 0 ? iconImages : null));
	}

	private void addOwnIcons(final MultipleImage iconImages, final NodeModel model) {
		final URI link = NodeLinks.getLink(model);
			final Icon icon = LinkController.getLinkIcon(link, model);
			if(icon != null)
				iconImages.addImage(icon);
	}

	void updateTextColor(final NodeView node) {
		final Color color = NodeStyleController.getController(node.getMap().getModeController()).getColor(
		    node.getModel());
		setForeground(color);
	}

	public boolean isEdited() {
		return getComponentCount() == 1 && getComponent(0) instanceof JTextComponent;
	}
	
	static enum TextModificationState{NONE, SUCCESS, FAILURE};

	public void updateText(NodeModel nodeModel) {
		final NodeView nodeView = getNodeView();
		if(nodeView == null)
			return;
		final ModeController modeController = nodeView.getMap().getModeController();
		final TextController textController = TextController.getController(modeController);
		isShortened = textController.isMinimized(nodeModel);
		Object content = nodeModel.getUserObject();
		String text;
		try {
			if(isShortened && (content instanceof String))
				content = HtmlUtils.htmlToPlain((String) content);
			final Object obj = textController.getTransformedObject(content, nodeModel, content);
			if(nodeView.isSelected()){
				nodeView.getMap().getModeController().getController().getViewController().addObjectTypeInfo(obj);
			}
			text = obj.toString();
			textModified = text.equals(content.toString()) ? TextModificationState.NONE : TextModificationState.SUCCESS;
		}
		catch (Throwable e) {
			LogUtils.warn(e.getMessage(), e);
			text = TextUtils.format("MainView.errorUpdateText", String.valueOf(content), e.getLocalizedMessage());
			textModified = TextModificationState.FAILURE;
		}
		if(isShortened){
			text = shortenText(text);
		}
		if(! HtmlUtils.isHtmlNode(text) && ! text.contains("\n"))
			text = convertTextToHtmlLink(text,  NodeLinks.getLink(nodeModel));
		updateText(text);
	}

	private String convertTextToHtmlLink(String text, URI link) {
		if(! LinkController.LinkType.DEFAULT.equals(LinkController.getLinkType(link, null)))
			return text;
		StringBuilder sb = new StringBuilder("<html><body><a href=\"");
		sb.append(link.toString());
		sb.append("\">");
		final String xmlEscapedText = HtmlUtils.toXMLEscapedText(text);
		sb.append(xmlEscapedText);
		sb.append("</a></body></html>");
		return sb.toString();
	}

	private String shortenText(String longText) {
		String text;
	    if(HtmlUtils.isHtmlNode(longText)){
	    	text = HtmlUtils.htmlToPlain(longText).trim();
	    }
	    else{
	    	text = longText;
	    }
	    int length = text.length();
	    final int eolPosition = text.indexOf('\n');
	    final int maxShortenedNodeWidth = ResourceController.getResourceController().getIntProperty("max_shortened_text_length");
		if(eolPosition == -1 || eolPosition >= length || eolPosition >= maxShortenedNodeWidth){
	    	if(length <= maxShortenedNodeWidth){
	    		return text;
	    	}
	    	length = maxShortenedNodeWidth;
	    }
	    else{
	    	length = eolPosition;
	    }
	    text = text.substring(0, length);
	    return text;
    }

	@Override
    public JToolTip createToolTip() {
		NodeTooltip tip = new NodeTooltip();
        tip.setComponent(this);
		final URL url = getMap().getModel().getURL();
		if (url != null) {
			tip.setBase(url);
		}
		else {
			try {
	            tip.setBase(new URL("file: "));
            }
            catch (MalformedURLException e) {
            }
		}
        return tip;
    }

    @Override
    public void setBorder(Border border) {
    }

    static public enum ConnectorLocation{LEFT, RIGHT, TOP, BOTTOM, CENTER};
    
    public ConnectorLocation getConnectorLocation(Point relativeLocation) {
        if(relativeLocation.x > getWidth())
            return ConnectorLocation.RIGHT;
        if(relativeLocation.x < 0)
            return ConnectorLocation.LEFT;
        if(relativeLocation.y > getHeight())
            return ConnectorLocation.BOTTOM;
        if(relativeLocation.y <0)
            return ConnectorLocation.TOP;
        return ConnectorLocation.CENTER;
    }
    public Point getConnectorPoint(Point relativeLocation) {
        if(relativeLocation.x > getWidth())
            return getRightPoint();
        if(relativeLocation.x < 0)
            return getLeftPoint();
        if(relativeLocation.y > getHeight()){
            final Point bottomPoint = getBottomPoint();
            bottomPoint.y = getNodeView().getContent().getHeight();
			return bottomPoint;
        }
        if(relativeLocation.y <0)
            return getTopPoint();
        return getCenterPoint();
    }

    private Point getCenterPoint() {
        return new Point(getWidth()/2, getHeight()/2);
    }

    public Point getTopPoint() {
        return new Point(getWidth()/2, 0);
    }

    public Point getBottomPoint() {
        return new Point(getWidth()/2, getHeight());
    }

	@Override
    public String getToolTipText() {
	    final String toolTipText = super.getToolTipText();
	    if(toolTipText != null)
	    	return toolTipText;
	    return createToolTipText();
    }

	private String createToolTipText() {
		final NodeView nodeView = getNodeView();
		final ModeController modeController = nodeView.getMap().getModeController();
		final NodeModel node = nodeView.getModel();
		return modeController.createToolTip(node, this);
    }

	@Override
    public String getToolTipText(MouseEvent event) {
	    final String toolTipText = super.getToolTipText(event);
	    if(toolTipText != null)
	    	return toolTipText;
	    return createToolTipText();
    }
	
	@Override
	public boolean contains(int x, int y) {
		return isInFoldingRegion(new Point(x, y)) 
				|| x >= -LISTENER_VIEW_WIDTH && x < getWidth() + LISTENER_VIEW_WIDTH 
				&& y >= 0 && y < getHeight();
	}

	public boolean isInFoldingRegion(Point p) {
		return isInRightFoldingRegion(p) || isInLeftFoldingRegion(p);
	}

	public MouseArea getMouseArea() {
		return mouseArea;
	}
	public MouseArea whichMouseArea(Point point) {
		final int x = point.x;
		if(! getNodeView().isRoot() && getDragRectangle().contains(point.x, point.y))
			return MouseArea.MOTION;
		if(isInFoldingRegion(point))
			return MouseArea.FOLDING;
		if(isInFollowLinkRegion(x))
			return MouseArea.LINK;
		return MouseArea.DEFAULT;
	}


	public void setMouseArea(MouseArea mouseArea) {
		if(mouseArea.equals(this.mouseArea))
			return;
		final boolean repaintDraggingRectangle = isVisible() && mouseArea.equals(MouseArea.MOTION) || this.mouseArea.equals(MouseArea.MOTION);
		final boolean repaintFoldingRectangle = isVisible() && mouseArea.equals(MouseArea.FOLDING) || this.mouseArea.equals(MouseArea.FOLDING);
		this.mouseArea = mouseArea;
		if(repaintDraggingRectangle)
			paintDraggingRectanleImmediately();
		if(repaintFoldingRectangle)
			paintFoldingRectanleImmediately();
	}

	private void paintFoldingRectanleImmediately() {
		paintImmediately(0, 0, getWidth(), getHeight());
	}

	private void paintDraggingRectanleImmediately() {
		final Rectangle dragRectangle = getDragRectangle();
		paintDecorationImmediately(dragRectangle);
	}

	private void paintDecorationImmediately(final Rectangle rectangle) {
		final MapView map = getMap();
		UITools.convertRectangleToAncestor(this, rectangle, map);
		map.paintImmediately(rectangle);
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if(! visible)
			setMouseArea(MouseArea.DEFAULT);
	}

}
