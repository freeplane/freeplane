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
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JToolTip;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import org.freeplane.api.ChildNodesAlignment;
import org.freeplane.api.ChildrenSides;
import org.freeplane.api.Dash;
import org.freeplane.api.HorizontalTextAlignment;
import org.freeplane.api.LayoutOrientation;
import org.freeplane.api.LengthUnit;
import org.freeplane.api.Quantity;
import org.freeplane.api.TextWritingDirection;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.FreeplaneMenuBar;
import org.freeplane.core.ui.components.MultipleImageIcon;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.Hyperlink;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.edge.EdgeController;
import org.freeplane.features.filter.Filter;
import org.freeplane.features.filter.FilterController;
import org.freeplane.features.icon.IconController;
import org.freeplane.features.icon.NamedIcon;
import org.freeplane.features.icon.UIIcon;
import org.freeplane.features.icon.factory.IconStoreFactory;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.NodeLinks;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.NodeModel.Side;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.nodelocation.LocationModel;
import org.freeplane.features.nodestyle.NodeCss;
import org.freeplane.features.nodestyle.NodeGeometryModel;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.styles.LogicalStyleController.StyleOption;
import org.freeplane.features.styles.MapViewLayout;
import org.freeplane.features.text.HighlightedTransformedObject;
import org.freeplane.features.text.TextController;


/**
 * Base class for all node views.
 */
public class MainView extends ZoomableLabel {
    private static final String MOUSE_DRIVEN_NODE_SHIFTS_OPTION_NAME = "mouseDrivenNodeShiftsAreDisabled";
	private static final long serialVersionUID = 1L;
    private static MainView lastMouseEventTarget = null;

    public enum DragOverRelation {
        NOT_AVAILABLE, CHILD_BEFORE, CHILD_AFTER, SIBLING_BEFORE, SIBLING_AFTER;
        public boolean isChild() {
            return this == CHILD_BEFORE || this == CHILD_AFTER;
        }
    }

    public enum DragOverDirection {
        OFF(false) {
            @Override
            void paint(MainView view, final Graphics2D graphics) {/**/}

            @Override
            DragOverRelation relation(LayoutOrientation layoutOrientation, Side side) {
                return DragOverRelation.NOT_AVAILABLE;
            }
        },
        DROP_UP(false) {
            @Override
            void paint(MainView view, final Graphics2D graphics) {
                graphics.setPaint(new GradientPaint(0, view.getHeight() * 3 / 5, view.getMap().getBackground(), 0, view.getHeight() / 5,
                        NodeView.dragColor));
                graphics.fillRect(0, 0, view.getWidth() - 1, view.getHeight() - 1);
            }
            @Override
            DragOverRelation relation(LayoutOrientation layoutOrientation, Side side) {
                return layoutOrientation == LayoutOrientation.LEFT_TO_RIGHT
                        ? DragOverRelation.CHILD_BEFORE
                        : DragOverRelation.SIBLING_BEFORE;
            }
       },
        DROP_DOWN(false) {
            @Override
            void paint(MainView view, final Graphics2D graphics) {
                graphics.setPaint(new GradientPaint(0, view.getHeight() * 2 / 5, view.getMap().getBackground(), 0, view.getHeight() * 4 / 5,
                        NodeView.dragColor));
                graphics.fillRect(0, 0, view.getWidth() - 1, view.getHeight() - 1);
            }
            @Override
            DragOverRelation relation(LayoutOrientation layoutOrientation, Side side) {
                return layoutOrientation == LayoutOrientation.LEFT_TO_RIGHT
                        ? DragOverRelation.CHILD_AFTER
                        : DragOverRelation.SIBLING_AFTER;
            }
        },
        DROP_LEFT(true) {
            @Override
            void paint(MainView view, final Graphics2D graphics) {
                graphics.setPaint(new GradientPaint(view.getWidth() * 3 / 4, 0, view.getMap().getBackground(), view.getWidth() / 4, 0,
                        NodeView.dragColor));
                graphics.fillRect(0, 0, view.getWidth() * 3 / 4, view.getHeight() - 1);
            }
            @Override
            DragOverRelation relation(LayoutOrientation layoutOrientation, Side side) {
                return layoutOrientation == LayoutOrientation.LEFT_TO_RIGHT
                        ? side == Side.BOTTOM_OR_RIGHT
                            ? DragOverRelation.SIBLING_BEFORE
                            : DragOverRelation.SIBLING_AFTER
                        : DragOverRelation.CHILD_BEFORE;
            }
        },
        DROP_RIGHT(true) {
            @Override
            void paint(MainView view, final Graphics2D graphics) {
                graphics.setPaint(new GradientPaint(view.getWidth() / 4, 0, view.getMap().getBackground(), view.getWidth() * 3 / 4, 0,
                        NodeView.dragColor));
                graphics.fillRect(view.getWidth() / 4, 0, view.getWidth() - 1, view.getHeight() - 1);

            }
            @Override
            DragOverRelation relation(LayoutOrientation layoutOrientation, Side side) {
                return layoutOrientation == LayoutOrientation.LEFT_TO_RIGHT
                        ? side == Side.BOTTOM_OR_RIGHT
                            ? DragOverRelation.SIBLING_AFTER
                            : DragOverRelation.SIBLING_BEFORE
                        : DragOverRelation.CHILD_AFTER;
            }
        },
        ;

        public final boolean isHorizontal;
        private DragOverDirection(boolean isHorizontal) {
            this.isHorizontal = isHorizontal;
        }

        abstract void paint(MainView view, final Graphics2D graphics);

        abstract DragOverRelation relation(LayoutOrientation layoutOrientation, Side side);
    }
	static final String USE_COMMON_OUT_POINT_FOR_ROOT_NODE_STRING = "use_common_out_point_for_root_node";
    public static boolean USE_COMMON_OUT_POINT_FOR_ROOT_NODE = ResourceController.getResourceController().getBooleanProperty(USE_COMMON_OUT_POINT_FOR_ROOT_NODE_STRING);

	static Dimension maximumSize = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
	static Dimension minimumSize = new Dimension(0,0);
	/**
	 *
	 */
	final static Stroke DEF_STROKE = new BasicStroke(1f);
	private static final BasicStroke THIN_STROKE = new BasicStroke(1.5f);
	private static final int THICK_STROKE_WIDTH = 4;
    private final static Stroke THICK_STROKE =  new BasicStroke(3f);

	private DragOverDirection dragOverDirection = DragOverDirection.OFF;
	private boolean isShortened;
	private TextModificationState textModified = TextModificationState.NONE;
	private MouseArea mouseArea = MouseArea.OUT;
	private float unzoomedBorderWidth = 1f;
	private Dash dash = Dash.DEFAULT;
	private Color borderColor = EdgeController.STANDARD_EDGE_COLOR;
	private Boolean borderColorMatchesEdgeColor = true;

	private MainViewPainter painter;
	private Color unselectedForeground = null;

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

    private DragOverDirection dragOverDirection(final Point p) {
        final DragOverDirection dragOverDirection;
        if(p.getX() < getWidth() * 1 / 4)
            dragOverDirection = DragOverDirection.DROP_LEFT;
        else if (p.getX() >= getWidth() * 3 / 4)
            dragOverDirection = DragOverDirection.DROP_RIGHT;
        else if (p.getY() < getHeight() * 1 / 2)
            dragOverDirection = DragOverDirection.DROP_UP;
        else
            dragOverDirection = DragOverDirection.DROP_DOWN;

        NodeView nodeView = getNodeView();
        DragOverRelation relation = dragOverDirection.relation(nodeView.layoutOrientation(),
                nodeView.side());
        if(relation == DragOverRelation.SIBLING_AFTER)
            return DragOverDirection.OFF;
        boolean isRoot = nodeView.isRoot();
        if(isRoot && relation == DragOverRelation.SIBLING_BEFORE)
            return DragOverDirection.OFF;
        ChildrenSides childrenSides = nodeView.childrenSides();
        if(relation.isChild() && ! childrenSides.matches(relation == DragOverRelation.CHILD_BEFORE))
            return DragOverDirection.OFF;
        return dragOverDirection;
    }

	public DragOverRelation dragOverRelation(final Point p) {
	    final DragOverDirection dragOverDirection = dragOverDirection(p);
	    NodeView nodeView = getNodeView();
        return dragOverDirection.relation(nodeView.layoutOrientation(), nodeView.side());
	}

	@Override
	public Dimension getMaximumSize() {
		return MainView.maximumSize;
	}

	@Override
	public Dimension getMinimumSize() {
		return MainView.minimumSize;
	}

	int getZoomedFoldingMarkHalfWidth() {
		return getNodeView().getZoomedFoldingMarkHalfWidth();
	}


    public int getZoomedFoldingSwitchMinWidth() {
        return getNodeView().getZoomedFoldingSwitchMinWidth();
    }


	public boolean isClickableLink(final double xCoord) {
		final NodeView nodeView = getNodeView();
		final NodeModel model = nodeView.getNode();
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
	    dragOverDirection.paint(this, graphics);
	}

	public FoldingMark foldingMarkType(MapController mapController, NodeView nodeView) {
		if (nodeView.isFolded() || nodeView.hasHiddenChildren()) {
			return FoldingMark.FOLDING_CIRCLE_FOLDED;
		}
		Filter filter = nodeView.getMap().getFilter();
		for (final NodeView childView : nodeView.getChildrenViews()) {
			if (!childView.getNode().hasVisibleContent(filter)
					&& !FoldingMark.FOLDING_CIRCLE_UNFOLDED.equals(foldingMarkType(mapController, childView))) {
				return FoldingMark.FOLDING_CIRCLE_FOLDED;
			}
		}
		if(nodeView.getNode().isRoot())
			return FoldingMark.INVISIBLE;
		return FoldingMark.FOLDING_CIRCLE_UNFOLDED;
	}

	boolean shouldPaintCloneMarker(final NodeView nodeView) {
		final ResourceController resourceController = ResourceController.getResourceController();
		return resourceController.getBooleanProperty("markClones") || nodeView.isSelected() && resourceController.getBooleanProperty("markSelectedClones");
	}

	Rectangle decorationMarkBounds(final NodeView nodeView, double shiftYFactor, double widthFactor, double heightFactor) {
		final int size = nodeView.getZoomedStateSymbolHalfWidth();
		int width = (int) (size * widthFactor);
		int x = nodeView.paintsChildrenOnTheLeft() ? getWidth() : 0 - width;
		int height = (int) (size * heightFactor);
		int y = (getHeight() - height) / 2 + (int)(height * shiftYFactor);
		Rectangle decorationMarkBounds = new Rectangle(x, y, width, height);
		return decorationMarkBounds;
	}


	void paintDragRectangle(final Graphics g) {
		if (! MouseArea.MOTION.equals(mouseArea))
			return;
		final Graphics2D g2 = (Graphics2D) g;
		final Object renderingHint = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
		final MapView parent = (MapView) SwingUtilities.getAncestorOfClass(MapView.class, this);
		parent.getModeController().getController().getMapViewManager().setEdgesRenderingHint(g2);
		final Color color = g2.getColor();
		Stroke stroke = g2.getStroke();
        g2.setColor(Color.WHITE);
        Rectangle r = getDragRectangle();
        r.x += THICK_STROKE_WIDTH / 2;
        r.y += THICK_STROKE_WIDTH / 2;
        r.width -= THICK_STROKE_WIDTH;
        r.height -= THICK_STROKE_WIDTH;

        g2.setStroke(THICK_STROKE);
        g.drawOval(r.x, r.y, r.width - 1, r.height - 1);
        g2.setStroke(THIN_STROKE);
		NodeView movedView = getNodeView();
		if (movedView .isFree()) {
			g2.setColor(Color.BLUE);
			g.fillOval(r.x, r.y, r.width - 1, r.height - 1);
		}
		else if (LocationModel.getModel(movedView.getNode()).getHGap().value <= 0) {
			g2.setColor(Color.RED);
			g.fillOval(r.x, r.y, r.width- 1, r.height- 1);
		}
		g2.setColor(Color.BLACK);
		g.drawOval(r.x, r.y, r.width- 1, r.height- 1);
		g2.setStroke(stroke);
		g2.setColor(color);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, renderingHint);
	}

	public Rectangle getDragRectangle() {
		final int size = getDraggingAreaWidth();
		Rectangle r;
		if(getNodeView().paintsChildrenOnTheLeft())
			r = new Rectangle(getWidth(), -size/2, size, getHeight() + size);
		else
			r = new Rectangle(-size, -size/2, size, getHeight() + size);
		return r;
	}

    void drawModificationRect(Graphics g) {
		final Color color = g.getColor();
		if(TextModificationState.FAILURE.equals(textModified)) {
			g.setColor(HighlightedTransformedObject.FAILURE_COLOR);
		}
		else if (MapView.isElementHighlighted(this, getNodeView().getNode())){
			g.setColor(FilterController.HIGHLIGHT_COLOR);
		}
		else if(TextModificationState.HIGHLIGHT.equals(textModified)) {
			final boolean markTransformedText = TextController.isMarkTransformedTextSet();
			if(! markTransformedText)
				return;
			g.setColor(HighlightedTransformedObject.OK_COLOR);
		}
		else{
			return;
		}
		g.drawRect(-1, -1, getWidth() + 2, getHeight() + 2);
		g.setColor(color);
    }

    protected void paintBackgound(final Graphics2D g) {
    	final Color color = getPaintedBackground();
    	painter.paintBackground(g, color);
    }

    public Color getUnselectedForeground() {
    	return unselectedForeground;
    }

    public Color getPaintedBackground() {
    	return getNodeView().getTextBackground();
	}

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

    public void stopDragOver() {
        dragOverDirection = DragOverDirection.OFF;
    }

    private void setDraggedOver(final DragOverDirection draggedOver) {
        final boolean isDifferent = draggedOver != dragOverDirection;
        if (isDifferent) {
            dragOverDirection = draggedOver;
            repaint();
        }
    }

    public void setDragOverDirection(final Point p) {
        final DragOverDirection dragOverDirection = dragOverDirection(p);
        setDraggedOver(dragOverDirection);
    }


    public void updateFont(final NodeView node) {
        final Font font = NodeStyleController.getController(node.getMap().getModeController()).getFont(node.getNode(), node.getStyleOption());
        setFont(UITools.scale(font));
	}

	void updateIcons(final NodeView node) {
	    final MultipleImageIcon iconImages = new MultipleImageIcon();
	    final NodeModel model = node.getNode();
		if(node.getMap().showsIcons()) {
		    StyleOption styleOption = node.getStyleOption();
            //		setHorizontalTextPosition(node.isLeft() ? SwingConstants.LEADING : SwingConstants.TRAILING);
		    /* fc, 06.10.2003: images? */
		    final Quantity<LengthUnit> iconHeight = IconController.getController().getIconSize(model, styleOption);
		    if(node.isRoot() && ! model.isRoot()) {
		        iconImages.addIcon(IconStoreFactory.ICON_STORE.getUIIcon("currentRoot.svg"), iconHeight);
		    }
		    for (final UIIcon icon : IconController.getController().getStateIcons(model)) {
		        iconImages.addIcon(icon, iconHeight);
		    }
		    final ModeController modeController = getNodeView().getMap().getModeController();
		    final Collection<NamedIcon> icons = IconController.getController(modeController).getIcons(model, styleOption);
		    for (final NamedIcon myIcon : icons) {
		        iconImages.addIcon(myIcon, iconHeight);
		    }
		}
        addOwnIcons(iconImages, model, getNodeView().getStyleOption());
        setIcon((iconImages.getImageCount() > 0 ? iconImages : null));
	}

	private void addOwnIcons(final MultipleImageIcon iconImages, final NodeModel model, StyleOption option) {
		getNodeView().getMap()
		        .getModeController().getExtension(LinkController.class).addLinkDecorationIcons(iconImages, model, option);
	}

	void updateTextColor(final NodeView node) {
		NodeStyleController styleController = NodeStyleController.getController(node.getMap().getModeController());
		Color newForeground = styleController.getColor(node.getNode(), node.getStyleOption());
		unselectedForeground = node.isSelected() ? styleController.getColor(node.getNode(), StyleOption.FOR_UNSELECTED_NODE)
				: newForeground;
		if(! Objects.equals(getForeground(), newForeground)) {
			setForeground(newForeground);
			revalidate();
		}
	}

	void updateCss(NodeView node) {
		NodeStyleController styleController = NodeStyleController.getController(node.getMap().getModeController());
		NodeCss newCss = styleController.getStyleSheet(node.getNode(), node.getStyleOption());
		setStyleSheet(newCss.css, newCss.getStyleSheet());
	}



	void updateHorizontalTextAlignment(NodeView node) {
		final HorizontalTextAlignment textAlignment = NodeStyleController
		        .getController(node.getMap().getModeController())
		        .getHorizontalTextAlignment(node.getNode(), node.getStyleOption());
		final boolean isCenteredByDefault = textAlignment == HorizontalTextAlignment.DEFAULT && node.getNode().isRoot();
		setHorizontalAlignment(isCenteredByDefault ? HorizontalTextAlignment.CENTER.swingConstant : textAlignment.swingConstant);
	}

	void updateTextWritingDirection(NodeView node) {
		final TextWritingDirection textDirection = NodeStyleController
		        .getController(node.getMap().getModeController())
		        .getTextWritingDirection(node.getNode(), node.getStyleOption());
		setComponentOrientation(textDirection.componentOrientation);
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
			Icon icon = textController.getIcon(transformedContent);
			setTextRenderingIcon(icon);
			text = icon == null ? transformedContent.toString() : "";
			textModified = transformedContent instanceof HighlightedTransformedObject ? TextModificationState.HIGHLIGHT : TextModificationState.NONE;
		}
		catch (Throwable e) {
			LogUtils.warn(e.getMessage());
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
		Hyperlink link = NodeLinks.getLink(node);
		if(link == null || "menuitem".equals(link.getScheme()) || ! LinkController.getController().formatNodeAsHyperlink(node))
			return text;
		if (HtmlUtils.isHtml(text))
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
		FreeplaneTooltip tip = new FreeplaneTooltip(this.getGraphicsConfiguration(), FreeplaneTooltip.TEXT_HTML);
        tip.setComponent(this);
        tip.setComponentOrientation(getComponentOrientation());
		final URL url = getMap().getMap().getURL();
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

    @SuppressWarnings("hiding")
    static public enum ConnectorLocation{
        LEFT(MainViewPainter::getLeftPoint),
        RIGHT(MainViewPainter::getRightPoint),
        TOP(MainViewPainter::getTopPoint),
        BOTTOM(MainViewPainter::getBottomPoint),
        CENTER(MainViewPainter::getCenterPoint);

        public final Function<MainViewPainter, Point> pointSupplier;

        private ConnectorLocation(Function<MainViewPainter, Point> pointSupplier) {
            this.pointSupplier = pointSupplier;
        }

    }

    public ConnectorLocation getConnectorLocation(Point relativeLocation,
            LayoutOrientation layoutOrientation,
            ChildNodesAlignment alignment) {
    	if(layoutOrientation == LayoutOrientation.LEFT_TO_RIGHT || alignment.isStacked()) {
            if(relativeLocation.y > getHeight())
                return ConnectorLocation.BOTTOM;
            if(relativeLocation.y <0)
                return ConnectorLocation.TOP;
    	}
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
		final NodeModel node = nodeView.getNode();
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
	    if(super.contains(x, y))
	        return true;
	    if(lastMouseEventTarget != null && lastMouseEventTarget != this)
	        return false;
		final Point p = new Point(x, y);
		return isInFoldingRegion(p) || isInDragRegion(p);
	}



	@Override
    protected void processMouseEvent(MouseEvent e) {
	    if(e.getID() == MouseEvent.MOUSE_ENTERED || e.getID() == MouseEvent.MOUSE_MOVED) {
	        if(e.getClickCount() == 0)
	            lastMouseEventTarget = this;
        } else if (lastMouseEventTarget == this && e.getID() == MouseEvent.MOUSE_EXITED)
	        lastMouseEventTarget = null;
	    super.processMouseEvent(e);
    }

    public boolean isInDragRegion(Point p) {
    	if(ResourceController.getResourceController().getBooleanProperty(MOUSE_DRIVEN_NODE_SHIFTS_OPTION_NAME))
    		return false;
		if (p.y >= 0 && p.y < getHeight()){
			final NodeView nodeView = getNodeView();
			if(nodeView.isRoot())
				return false;
			final NodeModel node = nodeView.getNode();
			if(node.getParentNode() == null ) {
				return false;
			}
			if (MapViewLayout.OUTLINE.equals(nodeView.getMap().getLayoutType()))
				return false;
			final int draggingWidth = getDraggingAreaWidth();
			if(nodeView.paintsChildrenOnTheLeft()){
				final int width = getWidth();
				return p.x >= width && p.x < width + draggingWidth;
			}
			else
				return p.x >= -draggingWidth && p.x < 0;
		}
		return false;

	}

	boolean hasChildren() {
	    final NodeView nodeView = getNodeView();
		final NodeModel node = nodeView.getNode();
		return node.hasChildren();
	}

	public boolean isInFoldingRegion(Point p) {
	    NodeView nodeView = getNodeView();
	    if (!nodeView.getNode().hasChildren())
	        return false;
	    Rectangle foldingRectangleBounds = painter.getFoldingRectangleBounds(nodeView, true);
	    if(nodeView.usesHorizontalLayout()) {
	        if(nodeView.isTopOrLeft())
	            return p.y >= foldingRectangleBounds.y && p.y < 0
	            && p.x >= 0 && p.x < getWidth();
	        else
	            return p.y >= foldingRectangleBounds.y && p.y < foldingRectangleBounds.y + foldingRectangleBounds.height
	                && p.x >= 0 && p.x < getWidth();
	    }
	    else {
	        if(nodeView.paintsChildrenOnTheLeft())
	            return p.x >= foldingRectangleBounds.x && p.x < 0
	                && p.y >= 0 && p.y < Math.max(foldingRectangleBounds.y + foldingRectangleBounds.height, getHeight());
	        else
	            return p.x >= foldingRectangleBounds.x && p.x < foldingRectangleBounds.x + foldingRectangleBounds.width
	                && p.y >= 0 && p.y < Math.max(foldingRectangleBounds.y + foldingRectangleBounds.height, getHeight());
	    }
	}

	public MouseArea getMouseArea() {
		return mouseArea;
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

	Rectangle getFoldingRectangleBounds(final NodeView nodeView, boolean drawsControls) {
	    return painter.getFoldingRectangleBounds(nodeView, drawsControls);
	}

	private void paintFoldingRectangleImmediately() {
			NodeView nodeView = getNodeView();
            final Rectangle foldingRectangle = painter.getFoldingRectangleBounds(nodeView, true);
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
			setMouseArea(MouseArea.OUT);
	}

	int getDraggingAreaWidth() {
		return getNodeView().getMap().getDraggingAreaWidth() + THICK_STROKE_WIDTH;
	}

	public NamedIcon getUIIconAt(Point coordinate){
		Icon icon = getIcon();
		if(icon instanceof MultipleImageIcon){
			Rectangle iconRectangle = getIconRectangle();
			Point transformedToIconCoordinate = new Point(coordinate);
			transformedToIconCoordinate.translate(-iconRectangle.x, -iconRectangle.y);
			return ((MultipleImageIcon)icon).getUIIconAt(transformedToIconCoordinate);

		}
		else
			return null;
	}

	public float getUnzoomedEdgeWidth() {
		final NodeView nodeView = getNodeView();
		final int edgeWidth = nodeView.getEdgeWidth();
		return edgeWidth;
	}

	public float getPaintedBorderWidth() {
		final float zoomedLineWidth = getNodeView().getMap().getZoom() * unzoomedBorderWidth;
		return Math.max(zoomedLineWidth, 1);
	}

	public float getUnzoomedBorderWidth() {
		return Math.max(unzoomedBorderWidth, 1);
	}

	public Dash getDash() {
		return dash;
	}

	public Color getBorderColor() {
		return borderColorMatchesEdgeColor ? getNodeView().getEdgeColor() : borderColor;
	}

	public Color getFoldingMarkBorderColor() {
		Color borderColor = getBorderColor();
		if(borderColor.getAlpha() == 255)
			return borderColor;
		else
			return new Color(borderColor.getRed(), borderColor.getGreen(), borderColor.getGreen(), 255);
	}

	public Color getFoldingMarkFillColor() {
		Color fillColor = getNodeView().getTextBackground();
		Color foldingCircleBorderColor = getFoldingMarkBorderColor();
		if(foldingCircleBorderColor.equals(fillColor)) {
			final Color color = fillColor;
			if(UITools.isLighter(color, 0x60))
				fillColor = fillColor.darker();
			else
				fillColor = fillColor.brighter().brighter();
		}

		return fillColor;
	}

	public void updateBorder(NodeView nodeView) {
		final NodeStyleController controller = NodeStyleController.getController(nodeView.getMap().getModeController());
		final NodeModel node = nodeView.getNode();
		StyleOption styleOption = nodeView.getStyleOption();
        final Boolean borderWidthMatchesEdgeWidth = controller.getBorderWidthMatchesEdgeWidth(node, styleOption);
		if(borderWidthMatchesEdgeWidth)
			unzoomedBorderWidth = getUnzoomedEdgeWidth();
		else
			unzoomedBorderWidth = (float) controller.getBorderWidth(node, styleOption).toBaseUnits();

		final Boolean borderDashMatchesEdgeDash = controller.getBorderDashMatchesEdgeDash(node, styleOption);
		if(borderDashMatchesEdgeDash)
			dash = nodeView.getEdgeDash();
		else
			dash = controller.getBorderDash(node, styleOption);

		borderColorMatchesEdgeColor = controller.getBorderColorMatchesEdgeColor(node, styleOption);
		if(borderColorMatchesEdgeColor)
			borderColor = null;
		else
			borderColor = controller.getBorderColor(node, styleOption);
	}

	void paintComponentDefault(final Graphics graphics) {
	    super.paintComponent(graphics);
	}

    public Insets getDefaultZoomedInsets() {
		return super.getZoomedInsets();
	}

	public Insets getDefaultInsets() {
		return super.getInsets();
	}

	public Insets getDefaultInsets(Insets insets) {
		return super.getInsets(insets);
	}

	public Dimension getDefaultPreferredSize() {
		return super.getPreferredSize();
	}

	public void setBoundsDefault(int x, int y, int width, int height) {
	    if(x != getX() || y != getY() || width != getWidth() || height != getHeight()) {
	        getNodeView().repaintEdge();
	    }
		super.setBounds(x, y, width, height);
	}

    public Point getConnectorPoint(Point relativeLocation, LayoutOrientation layoutOrientation, ChildNodesAlignment alignment) {
        ConnectorLocation location = getConnectorLocation(relativeLocation, layoutOrientation, alignment);
        return painter.getConnectorPoint(relativeLocation, location);
    }

    public Point getConnectorPoint(Point relativeLocation, ConnectorLocation connectorLocation) {
		return painter.getConnectorPoint(relativeLocation, connectorLocation);
	}

	public Point getLeftPoint() {
		return painter.getLeftPoint();
	}

	public Point getRightPoint() {
		return painter.getRightPoint();
	}

	public NodeGeometryModel getShapeConfiguration() {
		return painter.getShapeConfiguration();
	}

	@Override
	public void paintComponent(Graphics graphics) {
		painter.paintComponent(graphics);
	}

	@Override
	public Insets getZoomedInsets() {
		return painter.getZoomedInsets();
	}

	@Override
	public Insets getInsets() {
		return painter.getInsets();
	}

	@Override
	public Insets getInsets(Insets insets) {
		return painter.getInsets(insets);
	}

	@Override
	public Dimension getPreferredSize() {
		return painter.getPreferredSize();
	}

	@Override
	public void setBounds(int x, int y, int width, int height) {
		painter.setBounds(x, y, width, height);
	}

	public int getSingleChildShift() {
		return painter.getSingleChildShift();
	}

	public void paintDecoration(NodeView nodeView, Graphics2D g) {
		painter.paintDecoration(nodeView, g);
	}

	public void setPainter(MainViewPainter shape) {
		if(this.painter != shape) {
			this.painter = shape;
			revalidate();
			repaint();
		}
	}

}
