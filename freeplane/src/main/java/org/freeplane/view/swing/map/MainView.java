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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.JComponent;
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
import org.freeplane.features.DashVariant;
import org.freeplane.features.edge.EdgeController;
import org.freeplane.features.edge.EdgeStyle;
import org.freeplane.features.icon.IconController;
import org.freeplane.features.icon.MindIcon;
import org.freeplane.features.icon.UIIcon;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.NodeLinks;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.nodelocation.LocationModel;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.nodestyle.NodeStyleModel.HorizontalTextAlignment;
import org.freeplane.features.nodestyle.ShapeConfigurationModel;
import org.freeplane.features.styles.MapViewLayout;
import org.freeplane.features.text.HighlightedTransformedObject;
import org.freeplane.features.text.TextController;


/**
 * Base class for all node views.
 */
public abstract class MainView extends ZoomableLabel {
	private static final int FOLDING_CIRCLE_WIDTH = 16;
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
	private MouseArea mouseArea = MouseArea.OUT;
	final static Stroke DEF_STROKE = new BasicStroke();
	private static final int DRAG_OVAL_WIDTH = 10;
	private float unzoomedBorderWidth = 1f;
	private DashVariant dash = DashVariant.DEFAULT;
	private Color borderColor = EdgeController.STANDARD_EDGE_COLOR;
	private Boolean borderColorMatchesEdgeColor = true;

	boolean isShortened() {
    	return isShortened;
    }

	MainView() {
		setAlignmentX(Component.CENTER_ALIGNMENT);
		setHorizontalAlignment(SwingConstants.LEFT);
		setVerticalAlignment(SwingConstants.CENTER);
		setHorizontalTextPosition(SwingConstants.TRAILING);
		setVerticalTextPosition(SwingConstants.TOP);
	}

	protected void convertPointFromMap(final Point p) {
		UITools.convertPointFromAncestor(getMap(), p, this);
	}

	protected void convertPointToMap(final Point p) {
		UITools.convertPointToAncestor(this, p, getMap());
	}

	public boolean dropAsSibling(final double xCoord) {
		if(getNodeView().isRoot())
			return false;
		if(dropLeft(xCoord))
		return ! isInVerticalRegion(xCoord, 2. / 3);
		else
			return isInVerticalRegion(xCoord, 1. / 3);
	}

	/** @return true if should be on the left, false otherwise. */
	public boolean dropLeft(final double xCoord) {
		/* here it is the same as me. */
		NodeView nodeView = getNodeView();
		if(getNodeView().isRoot())
			return xCoord < getSize().width * 1 / 2;
		else
			return nodeView.isLeft();
	}

	public int getDeltaX() {
		final NodeView nodeView = getNodeView();
		if (nodeView.isFolded() && nodeView.isLeft()) {
			return getZoomedFoldingSymbolHalfWidth() * 3;
		}
		else
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
		int width = getWidth();
		final NodeView nodeView = getNodeView();
		final NodeModel model = nodeView.getModel();
		if (nodeView.isFolded()) {
			width += getZoomedFoldingSymbolHalfWidth() * 3;
		}
		return width;
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

	
	abstract public ShapeConfigurationModel getShapeConfiguration();

	int getZoomedFoldingSymbolHalfWidth() {
		return getNodeView().getZoomedFoldingSymbolHalfWidth();
	}

	public boolean isClickableLink(final double xCoord) {
		final NodeView nodeView = getNodeView();
		final NodeModel model = nodeView.getModel();
		if (NodeLinks.getValidLink(model) == null)
			return false;
		return isInIconRegion(xCoord);
	}
	
	public boolean isInIconRegion(final double xCoord)
	{
		Rectangle iconR = getIconRectangle();
		return xCoord >= iconR.x && xCoord < iconR.x + iconR.width;
	}

	private Rectangle getIconRectangle() {
		ZoomableLabelUI zoomableLabelUI = (ZoomableLabelUI)getUI();
		Rectangle iconR = zoomableLabelUI.getIconR(this);
		return iconR;
	}

	/**
	 * Determines whether or not the xCoord is in the part p of the node: if
	 * node is on the left: part [1-p,1] if node is on the right: part[ 0,p] of
	 * the total width.
	 */
	public boolean isInVerticalRegion(final double xCoord, final double p) {
		return xCoord < getSize().width * p;
	}

	@Override
	final public void paint(Graphics g){
		final PaintingMode paintingMode = getMap().getPaintingMode();
		if(! (PaintingMode.SELECTED_NODES.equals(paintingMode)
				 || PaintingMode.NODES.equals(paintingMode)))
			return;
		final NodeView nodeView = getNodeView();
		final boolean selected = nodeView.isSelected();
		if(paintingMode.equals(PaintingMode.SELECTED_NODES) == selected)
			super.paint(g);
	}

	
	public void paintDragOver(final Graphics2D graphics) {
		if (isDraggedOver == NodeView.DRAGGED_OVER_SON || isDraggedOver == NodeView.DRAGGED_OVER_SON_LEFT) {
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
		if (getNodeView().isLeft() || isDraggedOver == NodeView.DRAGGED_OVER_SON_LEFT) {
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

	public FoldingMark foldingMarkType(MapController mapController, NodeView nodeView) {
		NodeModel node = nodeView.getModel();
		if (nodeView.isFolded()) {
			return FoldingMark.ITSELF_FOLDED;
		}
		for (final NodeModel child : mapController.childrenUnfolded(node)) {
			if (child.hasVisibleContent() && nodeView.isChildHidden(child)) {
				return FoldingMark.ITSELF_FOLDED;
			}
		}
		for (final NodeView childView : nodeView.getChildrenViews()) {
			if (!childView.getModel().hasVisibleContent() && !FoldingMark.UNFOLDED.equals(foldingMarkType(mapController, childView))) {
				return FoldingMark.UNVISIBLE_CHILDREN_FOLDED;
			}
		}
		return FoldingMark.UNFOLDED;
	}

	void paintDecoration(final NodeView nodeView, final Graphics2D g) {
		drawModificationRect(g);
		paintDragRectangle(g);
		paintFoldingMark(nodeView, g);
        if (isShortened()) {
        	FoldingMark.SHORTENED.draw(g, nodeView, decorationMarkBounds(nodeView, 7./3, 5./3));
        } else if (shouldPaintCloneMarker(nodeView)){
			if (nodeView.getModel().isCloneTreeRoot())
				FoldingMark.CLONE.draw(g, nodeView, decorationMarkBounds(nodeView, 2, 2.5));
			else if (nodeView.getModel().isCloneTreeNode())
				FoldingMark.CLONE.draw(g, nodeView, decorationMarkBounds(nodeView, 1.5, 2.5));
		}
	}

	private boolean shouldPaintCloneMarker(final NodeView nodeView) {
		final ResourceController resourceController = ResourceController.getResourceController();
		return resourceController.getBooleanProperty("markClones") || nodeView.isSelected() && resourceController.getBooleanProperty("markSelectedClones");
	}

	private Rectangle decorationMarkBounds(final NodeView nodeView, double widthFactor, double heightFactor) {
		final int size = nodeView.getZoomedStateSymbolHalfWidth();
		int width = (int) (size * widthFactor);
		int x = nodeView.isLeft() ? getWidth() : 0 - width;
		int height = (int) (size * heightFactor);
		int y = (getHeight() - height) / 2;
		Rectangle decorationMarkBounds = new Rectangle(x, y, width, height);
		return decorationMarkBounds;
	}

	protected void paintFoldingMark(final NodeView nodeView, final Graphics2D g) {
		if (! hasChildren())
			return;
		final MapView map = getMap();
		final MapController mapController = map.getModeController().getMapController();
		final FoldingMark markType = foldingMarkType(mapController, nodeView);
	    Point mousePosition = null;
	    try {
	        mousePosition = getMousePosition();
        }
        catch (Exception e) {
        }
		if(mousePosition != null && ! map.isPrinting()){
			final int width = Math.max(FOLDING_CIRCLE_WIDTH, getZoomedFoldingSymbolHalfWidth() * 2);
			final Point p = getNodeView().isLeft() ? getLeftPoint() : getRightPoint();
			if(p.y + width/2 > getHeight())
				p.y = getHeight() - width;
			else
				p.y -= width/2;
			if(nodeView.isLeft())
				p.x -= width;
			final FoldingMark foldingCircle;
			if(markType.equals(FoldingMark.UNFOLDED)) {
				final NodeModel node = nodeView.getModel();
				if(nodeView.hasHiddenChildren())
					foldingCircle = FoldingMark.FOLDING_CIRCLE_HIDDEN_CHILD;
				else
					foldingCircle = FoldingMark.FOLDING_CIRCLE_UNFOLDED;
            }
			else{
				foldingCircle = FoldingMark.FOLDING_CIRCLE_FOLDED;
			}
            foldingCircle.draw(g, nodeView, new Rectangle(p.x, p.y, width, width));
		}
		else{
			final int halfWidth = getZoomedFoldingSymbolHalfWidth();
			final Point p = getNodeView().isLeft() ? getLeftPoint() : getRightPoint();
			if (p.x <= 0) {
				p.x -= halfWidth;
			}
			else {
				p.x += halfWidth;
			}
			markType.draw(g, nodeView, new Rectangle(p.x - halfWidth, p.y-halfWidth, halfWidth*2, halfWidth*2));
		}
	}


	private void paintDragRectangle(final Graphics g) {
		if (! MouseArea.MOTION.equals(mouseArea))
			return;
		final Graphics2D g2 = (Graphics2D) g;
		final Object renderingHint = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
		final MapView parent = (MapView) SwingUtilities.getAncestorOfClass(MapView.class, this);
		parent.getModeController().getController().getMapViewManager().setEdgesRenderingHint(g2);
		final Color color = g2.getColor();
		NodeView movedView = getNodeView();
		Rectangle r = getDragRectangle();
		if (movedView .isFree()) {
			g2.setColor(Color.BLUE);
			g.fillOval(r.x, r.y, r.width - 1, r.height - 1);
		}
		else if (LocationModel.getModel(movedView.getModel()).getHGap().value <= 0) {
			g2.setColor(Color.RED);
			g.fillOval(r.x, r.y, r.width- 1, r.height- 1);
		}
		g2.setColor(Color.BLACK);
		g.drawOval(r.x, r.y, r.width- 1, r.height- 1);
		g2.setColor(color);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, renderingHint);
	}

	public Rectangle getDragRectangle() {
		final int size = getDraggingWidth();
		Rectangle r;
		if(getNodeView().isLeft())
			r = new Rectangle(getWidth(), -size, size, getHeight() + size * 2);
		else
			r = new Rectangle(-size, -size, size, getHeight() + size * 2);
		return r;
	}

    private void drawModificationRect(Graphics g) {
		final Color color = g.getColor();
		if(TextModificationState.HIGHLIGHT.equals(textModified)){
			final boolean markTransformedText = TextController.isMarkTransformedTextSet();
			if(! markTransformedText)
				return;
			g.setColor(Color.GREEN);
		}
		else if(TextModificationState.FAILURE.equals(textModified)){
			g.setColor(Color.RED);
		}
		else{
			return;
		}
		g.drawRect(-1, -1, getWidth() + 2, getHeight() + 2);
		g.setColor(color);
    }

	protected void paintBackgound(final Graphics2D g) {
		final Color color = getPaintedBackground();
		paintBackground(g, color);
	}

	public Color getPaintedBackground() {
		final Color color;
		if (getNodeView().useSelectionColors()) {
			color = getNodeView().getSelectedColor();
		}
		else {
			color = getNodeView().getTextBackground();
		}
		return color;
	}
	abstract protected void paintBackground(final Graphics2D graphics, final Color color);

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
		final int draggedOver;
		if(getNodeView().isRoot()) {
			if (dropLeft(p.getX()))
				draggedOver = NodeView.DRAGGED_OVER_SON_LEFT;
			else
				draggedOver = NodeView.DRAGGED_OVER_SON;
		} 
		else {
			if (dropAsSibling(p.getX()))
				draggedOver = NodeView.DRAGGED_OVER_SIBLING;
			else
				draggedOver = NodeView.DRAGGED_OVER_SON;
		}
		setDraggedOver(draggedOver);
	}

	public void updateFont(final NodeView node) {
		final Font font = NodeStyleController.getController(node.getMap().getModeController()).getFont(node.getModel());
		setFont(UITools.scale(font));
	}

	void updateIcons(final NodeView node) {
		if(! node.getMap().showsIcons()) {
			setIcon(null);
			return;
		}
//		setHorizontalTextPosition(node.isLeft() ? SwingConstants.LEADING : SwingConstants.TRAILING);
		final MultipleImage iconImages = new MultipleImage();
		/* fc, 06.10.2003: images? */
		final NodeModel model = node.getModel();
		for (final UIIcon icon : IconController.getController().getStateIcons(model)) {
			iconImages.addIcon(icon, model);
		}
		final ModeController modeController = getNodeView().getMap().getModeController();
		final Collection<MindIcon> icons = IconController.getController(modeController).getIcons(model);
		for (final MindIcon myIcon : icons) {
			iconImages.addIcon(myIcon, model);
		}
		addOwnIcons(iconImages, model);
		setIcon((iconImages.getImageCount() > 0 ? iconImages : null));
	}

	private void addOwnIcons(final MultipleImage iconImages, final NodeModel model) {
		final URI link = NodeLinks.getLink(model);
		final Icon icon = getNodeView().getMap().getModeController().getExtension(LinkController.class).getLinkIcon(link, model);
		if(icon != null)
			iconImages.addLinkIcon(icon, model);
	}

	void updateTextColor(final NodeView node) {
		final Color color = NodeStyleController.getController(node.getMap().getModeController()).getColor(
		    node.getModel());
		setForeground(color);
	}
	
	void updateHorizontalTextAlignment(NodeView node) {
		final HorizontalTextAlignment textAlignment = NodeStyleController.getController(node.getMap().getModeController()).getHorizontalTextAlignment(node.getModel());
		final boolean isCenteredByDefault = textAlignment == HorizontalTextAlignment.DEFAULT && node.isRoot();
		setHorizontalAlignment(isCenteredByDefault ? HorizontalTextAlignment.CENTER.swingConstant : textAlignment.swingConstant);
	}


	public boolean isEdited() {
		return getComponentCount() == 1 && getComponent(0) instanceof JTextComponent;
	}

	static enum TextModificationState{NONE, HIGHLIGHT, FAILURE};

	public void updateText(NodeModel nodeModel) {
		final NodeView nodeView = getNodeView();
		if(nodeView == null)
			return;
		final ModeController modeController = nodeView.getMap().getModeController();
		final TextController textController = TextController.getController(modeController);
		isShortened = textController.isMinimized(nodeModel);
		final Object userObject = nodeModel.getUserObject();
		String text;
		try {
			final Object transformedContent = textController.getTransformedObject(nodeModel);
			if(nodeView.isSelected()){
				nodeView.getMap().getModeController().getController().getViewController().addObjectTypeInfo(transformedContent);
			}
			Icon icon = textController.getIcon(transformedContent, nodeModel, userObject);
			putClientProperty(TEXT_RENDERING_ICON, icon);
			text = transformedContent.toString();
			textModified = transformedContent instanceof HighlightedTransformedObject ? TextModificationState.HIGHLIGHT : TextModificationState.NONE;
		}
		catch (Throwable e) {
			LogUtils.warn(e.getMessage(), e);
			text = TextUtils.format("MainView.errorUpdateText", String.valueOf(userObject), e.getLocalizedMessage());
			textModified = TextModificationState.FAILURE;
		}
		if(isShortened){
			text = textController.getShortText(text);
		}
		text = convertTextToHtmlLink(text,  nodeModel);
		updateText(text);
	}

	private String convertTextToHtmlLink(String text, NodeModel node) {
		URI link = NodeLinks.getLink(node);
		if(link == null || "menuitem".equals(link.getScheme()) || ! LinkController.getController().formatNodeAsHyperlink(node))
			return text;
		if (HtmlUtils.isHtmlNode(text))
			text = HtmlUtils.htmlToPlain(text);
		StringBuilder sb = new StringBuilder("<html><body><a href=\"");
		sb.append(link.toString());
		sb.append("\">");
		final String xmlEscapedText = HtmlUtils.toHTMLEscapedText(text);
		sb.append(xmlEscapedText);
		sb.append("</a></body></html>");
		return sb.toString();
	}

	@Override
    public JToolTip createToolTip() {
		NodeTooltip tip = new NodeTooltip(this.getGraphicsConfiguration());
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
		if (nodeView == null)
			return "";
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
		final Point p = new Point(x, y);
		return isInFoldingRegion(p) || isInDragRegion(p)|| super.contains(x, y);
	}

	public boolean isInDragRegion(Point p) {
		if (p.y >= 0 && p.y < getHeight()){
			final NodeView nodeView = getNodeView();
			if(nodeView.isRoot())
				return false;
			if (MapViewLayout.OUTLINE.equals(nodeView.getMap().getLayoutType()))
				return false;
			final int draggingWidth = getDraggingWidth();
			if(nodeView.isLeft()){
				final int width = getWidth();
				return p.x >= width && p.x < width + draggingWidth;
			}
			else
				return p.x >= -draggingWidth && p.x < 0;
		}
		return false;

	}

	public boolean isInFoldingRegion(Point p) {
		if (hasChildren() && p.y >= 0 && p.y < getHeight()) {
			final boolean isLeft = getNodeView().isLeft();
			final int width = Math.max(FOLDING_CIRCLE_WIDTH, getZoomedFoldingSymbolHalfWidth() * 2);
			if (isLeft) {
	            final int maxX = 0;
	            return p.x >= -width && p.x < maxX;
            }
            else {
	            final int minX = getWidth();
	            return p.x >= minX && p.x < (getWidth() + width);
            }
		}
        else
			return false;
	}

	private boolean hasChildren() {
	    final NodeView nodeView = getNodeView();
		final NodeModel node = nodeView.getModel();
		return node.hasChildren();
    }

	public MouseArea getMouseArea() {
		return mouseArea;
	}
	public MouseArea whichMouseArea(Point point) {
		final int x = point.x;
		if(isInDragRegion(point))
			return MouseArea.MOTION;
		if(isInFoldingRegion(point))
			return MouseArea.FOLDING;
		if(isClickableLink(x))
			return MouseArea.LINK;
		return MouseArea.DEFAULT;
	}


	public void setMouseArea(MouseArea mouseArea) {
		if(mouseArea.equals(this.mouseArea))
			return;
		final boolean repaintDraggingRectangle = isVisible()
				&& (mouseArea.equals(MouseArea.MOTION)
						|| this.mouseArea.equals(MouseArea.MOTION)
						);
		final boolean repaintFoldingRectangle = isVisible()
				&& (mouseArea.equals(MouseArea.OUT)
						|| mouseArea.equals(MouseArea.FOLDING)
						|| this.mouseArea.equals(MouseArea.OUT)
						|| this.mouseArea.equals(MouseArea.FOLDING));
		this.mouseArea = mouseArea;
		if(repaintDraggingRectangle)
			paintDraggingRectangleImmediately();
		if(repaintFoldingRectangle)
			paintFoldingRectangleImmediately();
	}

	private void paintFoldingRectangleImmediately() {
			final int zoomedFoldingSymbolHalfWidth = getZoomedFoldingSymbolHalfWidth();
			final int width = Math.max(FOLDING_CIRCLE_WIDTH, zoomedFoldingSymbolHalfWidth * 2);
			final NodeView nodeView = getNodeView();
			int height;
			final int x, y;
			if (nodeView.isLeft()){
				x = -width;
			}
			else{
				x = getWidth();
			}
			if(FOLDING_CIRCLE_WIDTH >= getHeight()){
				height = FOLDING_CIRCLE_WIDTH;
				y = getHeight() - FOLDING_CIRCLE_WIDTH;
			}
			else{
				height = getHeight();
				y = 0;
			}
			height += zoomedFoldingSymbolHalfWidth;
			final Rectangle foldingRectangle = new Rectangle(x-4, y-4, width+8, height+8);
			final MapView map = nodeView.getMap();
			UITools.convertRectangleToAncestor(this, foldingRectangle, map);
			map.paintImmediately(foldingRectangle);
	}

	private void paintDraggingRectangleImmediately() {
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

	private int getDraggingWidth() {
		return getNodeView().getZoomed(DRAG_OVAL_WIDTH);
	}

	public UIIcon getUIIconAt(Point coordinate){
		Icon icon = getIcon();
		if(icon instanceof MultipleImage){
			Rectangle iconRectangle = getIconRectangle();
			Point transformedToIconCoordinate = new Point(coordinate);
			transformedToIconCoordinate.translate(-iconRectangle.x, -iconRectangle.y);
			return ((MultipleImage)icon).getUIIconAt(transformedToIconCoordinate);
			
		}
		else
			return null;
	}
	
	public int getSingleChildShift() {
		return 0;
	}

	public float getUnzoomedEdgeWidth() {
		final NodeView nodeView = getNodeView();
		final int edgeWidth = nodeView.getEdgeWidth();
		final EdgeStyle style = nodeView.getEdgeStyle();
		final float nodeLineWidth = style.getNodeLineWidth(edgeWidth);
		return nodeLineWidth;
	}
	
	public float getPaintedBorderWidth() {
		final float zoomedLineWidth = getNodeView().getMap().getZoom() * unzoomedBorderWidth;
		return Math.max(zoomedLineWidth, 1);
	}

	public float getUnzoomedBorderWidth() {
		return Math.max(unzoomedBorderWidth, 1);
	}

	public DashVariant getDash() {
		return dash;
	}

	public Color getBorderColor() {
		return borderColorMatchesEdgeColor ? getNodeView().getEdgeColor() : borderColor;
	}

	public void updateBorder(NodeView nodeView) {
		final NodeStyleController controller = NodeStyleController.getController(nodeView.getMap().getModeController());
		final NodeModel node = nodeView.getModel();
		final Boolean borderWidthMatchesEdgeWidth = controller.getBorderWidthMatchesEdgeWidth(node);
		if(borderWidthMatchesEdgeWidth)
			unzoomedBorderWidth = getUnzoomedEdgeWidth();
		else
			unzoomedBorderWidth = (float) controller.getBorderWidth(node).toBaseUnits();
		
		final Boolean borderDashMatchesEdgeDash = controller.getBorderDashMatchesEdgeDash(node);
		if(borderDashMatchesEdgeDash)
			dash = nodeView.getEdgeDash();
		else
			dash = controller.getBorderDash(node);
		
		borderColorMatchesEdgeColor = controller.getBorderColorMatchesEdgeColor(node);
		if(borderColorMatchesEdgeColor)
			borderColor = nodeView.getEdgeColor();
		else
			borderColor = controller.getBorderColor(node);
	}
}
