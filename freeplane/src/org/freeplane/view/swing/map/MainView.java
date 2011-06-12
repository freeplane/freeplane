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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.net.URI;
import java.util.Collection;
import java.util.Map.Entry;

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
import org.freeplane.features.edge.EdgeController;
import org.freeplane.features.icon.IconController;
import org.freeplane.features.icon.MindIcon;
import org.freeplane.features.icon.UIIcon;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.NodeLinks;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.text.IContentTransformer;
import org.freeplane.features.text.TextController;


/**
 * Base class for all node views.
 */
public abstract class MainView extends ZoomableLabel {
    private static final String USE_COMMON_OUT_POINT_FOR_ROOT_NODE_STRING = "use_common_out_point_for_root_node";
    public static boolean USE_COMMON_OUT_POINT_FOR_ROOT_NODE = ResourceController.getResourceController().getBooleanProperty(USE_COMMON_OUT_POINT_FOR_ROOT_NODE_STRING);

	static Dimension maximumSize = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
	static Dimension minimumSize = new Dimension(0, 0);
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected int isDraggedOver = NodeView.DRAGGED_OVER_NO;
	private boolean isShortened;
	private TextModificationState textModified = TextModificationState.NONE;

	boolean isShortened() {
    	return isShortened;
    }

	MainView() {
		setAlignmentX(Component.CENTER_ALIGNMENT);
		setHorizontalAlignment(SwingConstants.LEFT);
		setVerticalAlignment(SwingConstants.CENTER);
		setHorizontalTextPosition(SwingConstants.TRAILING);
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

	abstract String getStyle();

	int getZoomedFoldingSymbolHalfWidth() {
		return getNodeView().getZoomedFoldingSymbolHalfWidth();
	}

	public boolean isInFollowLinkRegion(final double xCoord) {
		final NodeView nodeView = getNodeView();
		final NodeModel model = nodeView.getModel();
		return NodeLinks.getValidLink(model) != null && isInVerticalRegion(xCoord, 1. / 4);
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
		if (isDraggedOver == NodeView.DRAGGED_OVER_SON) {
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
		if (isDraggedOver == NodeView.DRAGGED_OVER_SIBLING) {
			graphics.setPaint(new GradientPaint(0, getHeight() * 3 / 5, getMap().getBackground(), 0, getHeight() / 5,
			    NodeView.dragColor));
			graphics.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
		}
	}

	void paintFoldingMark(final NodeView nodeView, final Graphics2D g, final Point p, boolean itself) {
		final int zoomedFoldingSymbolHalfWidth = getZoomedFoldingSymbolHalfWidth();
		p.translate(-zoomedFoldingSymbolHalfWidth, -zoomedFoldingSymbolHalfWidth);
		final Color color = g.getColor();
		g.setColor(itself ? Color.WHITE : Color.GRAY);
		g.fillOval(p.x, p.y, zoomedFoldingSymbolHalfWidth * 2, zoomedFoldingSymbolHalfWidth * 2);
		final NodeModel model = nodeView.getModel();
		final Color edgeColor = EdgeController.getController(nodeView.getMap().getModeController()).getColor(model);
		g.setColor(edgeColor);
		g.drawOval(p.x, p.y, zoomedFoldingSymbolHalfWidth * 2, zoomedFoldingSymbolHalfWidth * 2);
		g.setColor(color);
	}
	
	static private enum FoldingMarkType {
		UNFOLDED, ITSELF_FOLDED, UNVISIBLE_CHILDREN_FOLDED
	};

	static private FoldingMarkType foldingMarkType(MapController mapController, NodeModel node) {
		if (mapController.isFolded(node) && (node.isVisible() || node.getFilterInfo().isAncestor())) {
			return FoldingMarkType.ITSELF_FOLDED;
		}
		for (final NodeModel child : mapController.childrenUnfolded(node)) {
			if (!child.isVisible() && !FoldingMarkType.UNFOLDED.equals(foldingMarkType(mapController, child))) {
				return FoldingMarkType.UNVISIBLE_CHILDREN_FOLDED;
			}
		}
		return FoldingMarkType.UNFOLDED;
	}

	void paintDecoration(final NodeView nodeView, final Graphics2D g) {
		drawModificationRect(g);
		FoldingMarkType markType = foldingMarkType(getMap().getModeController().getMapController(), nodeView.getModel());
		if (!markType.equals(FoldingMarkType.UNFOLDED)) {
			final Point out = nodeView.isLeft() ? getLeftPoint() : getRightPoint();
			paintFoldingMark(nodeView, g, out, markType.equals(FoldingMarkType.ITSELF_FOLDED));
		}
        if (isShortened()) {
            final Point in =  nodeView.isLeft() ? getRightPoint() : getLeftPoint();
            paintFoldingMark(nodeView, g, in, true);
        }
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

	public void paintBackgound(final Graphics2D graphics) {
		if (getNodeView().useSelectionColors()) {
			paintBackground(graphics, getNodeView().getSelectedColor());
		}
		else {
			paintBackground(graphics, getNodeView().getTextBackground());
		}
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

	/**
	 * @return true if a link is to be displayed and the curser is the hand now.
	 */
	public boolean updateCursor(final double xCoord) {
		final boolean followLink = isInFollowLinkRegion(xCoord);
		final int requiredCursor = followLink ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR;
		if (getCursor().getType() != requiredCursor) {
			setCursor(requiredCursor != Cursor.DEFAULT_CURSOR ? new Cursor(requiredCursor) : null);
		}
		return followLink;
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
		for (final Entry<String, UIIcon> iconEntry : model.getStateIcons().entrySet()) {
			iconImages.addImage(iconEntry.getValue().getIcon());
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
		final boolean textShortened = textController.getIsShortened(nodeModel);
		Object content = nodeModel.getUserObject();
		String text;
		try {
			if(textShortened && (content instanceof String))
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
		if(textShortened){
			text = shortenText(text);
		}
		else{
			isShortened = false;
		}
		updateText(text);
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
	    final int maxNodeWidth = ResourceController.getResourceController().getIntProperty("max_shortened_text_length");
		if(eolPosition == -1 || eolPosition >= length || eolPosition >= maxNodeWidth){
	    	if(length <= maxNodeWidth){
	    		final Container parent = getParent();
	    		if(parent instanceof NodeView || parent.getComponentCount() == 1){
	    			isShortened = false;
	    			return longText;
	    		}
	    		isShortened = true;
	    		return text;
	    	}
	    	length = maxNodeWidth;
	    }
	    else{
	    	length = eolPosition;
	    }
	    text = text.substring(0, length);
	    isShortened = true;
	    return text;
    }

	@Override
    public JToolTip createToolTip() {
        JToolTip tip = new NodeTooltip();
        tip.setComponent(this);
        return tip;
    }

	@Override
    public void setToolTipText(String text) {
        String oldText = getToolTipText();
        putClientProperty(TOOL_TIP_TEXT_KEY, text);
        NodeTooltipManager toolTipManager = NodeTooltipManager.getSharedInstance(getMap().getModeController());
        if (text != null) {
	    if (oldText == null) {
                toolTipManager.registerComponent(this);
	    }
        } else {
            toolTipManager.unregisterComponent(this);
        }
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
        if(relativeLocation.y > getHeight())
            return getBottomPoint();
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
}
