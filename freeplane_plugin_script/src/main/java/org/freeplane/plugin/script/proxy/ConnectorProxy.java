/**
 * 
 */
package org.freeplane.plugin.script.proxy;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.freeplane.core.util.ColorUtils;
import org.freeplane.features.link.ArrowType;
import org.freeplane.features.link.ConnectorModel;
import org.freeplane.features.link.ConnectorModel.Shape;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.mindmapmode.MLinkController;
import org.freeplane.plugin.script.ScriptContext;
import org.freeplane.plugin.script.proxy.Proxy.Node;

class ConnectorProxy extends AbstractProxy<ConnectorModel> implements Proxy.Connector {
	ConnectorProxy(final ConnectorModel connector, final ScriptContext scriptContext) {
		super(connector, scriptContext);
	}

    public String getShape() {
		return getLinkController().getShape(getConnector()).name();
	}
	
	public void setShape(String shapeName) {
	    Shape shape = Shape.valueOf(shapeName);
        setShape(shape);
	}

    public void setShape(Shape shape) {
        getLinkController().setShape(getConnector(), Optional.of(shape));
    }

    public String getLabelFontFamily() {
		return getLinkController().getLabelFontFamily(getConnector());
	}
	
	public void setLabelFontFamily(String font) {
	    getLinkController().setLabelFontFamily(getConnector(), Optional.of(font));
	}

    public int getLabelFontSize() {
		return getLinkController().getLabelFontSize(getConnector());
	}
	
	public void setLabelFontSize(int size) {
	    getLinkController().setLabelFontSize(getConnector(), Optional.of(size));
	}
	
	public Color getColor() {
	    return getLinkController().getColor(getConnector());
	}
	
	public void setColor(final Color color) {
		getLinkController().setConnectorColor(getConnector(), Optional.of(color));
	}

	public int getOpacity() {
	    return getLinkController().getOpacity(getConnector());
	}
	
	public void setOpacity(final int opacity) {
		getLinkController().setOpacity(getConnector(), Optional.of(opacity));
	}

	public int getWidth() {
	    return getLinkController().getWidth(getConnector());
	}
	
	public void setWidth(final int width) {
		getLinkController().setWidth(getConnector(), Optional.of(width));
	}

	public int[] getDashArray() {
	    return getLinkController().getDashArray(getConnector());
	}
	
	public void setDashArray(final int[] dashArray) {
		getLinkController().setConnectorDashArray(getConnector(), Optional.of(dashArray != null ? dashArray : new int[] {}));
	}

	public String getColorCode() {
		return ColorUtils.colorToString(getColor());
	}

	public void setColorCode(final String rgbString) {
		setColor(ColorUtils.stringToColor(rgbString));
	}

	ConnectorModel getConnector() {
		return getDelegate();
	}

    public boolean hasEndArrow() {
        return getLinkController().getEndArrow(getConnector()) == ArrowType.DEFAULT;
    }

    @Deprecated
	public ArrowType getEndArrow() {
        return getLinkController().getEndArrow(getConnector());
	}

    private MLinkController getLinkController() {
		return (MLinkController) LinkController.getController();
	}

	public String getMiddleLabel() {
		return getConnector().getMiddleLabel().orElse(null);
	}

	public Node getSource() {
		return new NodeProxy(getConnector().getSource(), getScriptContext());
	}

	public String getSourceLabel() {
		return getConnector().getSourceLabel().orElse(null);
	}

    public boolean hasStartArrow() {
        return getLinkController().getStartArrow(getConnector()) == ArrowType.DEFAULT;
    }

    @Deprecated
	public ArrowType getStartArrow() {
		return getLinkController().getStartArrow(getConnector());
	}

	public Node getTarget() {
		return new NodeProxy(getConnector().getTarget(), getScriptContext());
	}

	public String getTargetLabel() {
		return getConnector().getTargetLabel().orElse(null);
	}

    private void setEndArrowImpl(final ArrowType arrowType) {
        final ConnectorModel connector = getConnector();
        getLinkController().changeArrowsOfArrowLink(connector, connector.getStartArrow(), Optional.of(arrowType));
    }

    public void setEndArrow(boolean showArrow) {
        setEndArrowImpl(showArrow ? ArrowType.DEFAULT : ArrowType.NONE);
    }

	@Deprecated
	public void setEndArrow(final ArrowType arrowType) {
		setEndArrowImpl(arrowType);
	}

	public void setMiddleLabel(final String label) {
		getLinkController().setMiddleLabel(getConnector(), label);
	}

	@Deprecated
	public void setSimulatesEdge(final boolean simulatesEdge) {
			setShape(simulatesEdge ? Shape.EDGE_LIKE : Shape.CUBIC_CURVE);
	}

	public void setSourceLabel(final String label) {
		getLinkController().setSourceLabel(getConnector(), label);
	}

    public void setStartArrow(boolean showArrow) {
        setStartArrowImpl(showArrow ? ArrowType.DEFAULT : ArrowType.NONE);
    }

    private void setStartArrowImpl(final ArrowType arrowType) {
        final ConnectorModel connector = getConnector();
        MLinkController linkController = getLinkController();
        linkController.changeArrowsOfArrowLink(connector, Optional.of(arrowType), connector.getEndArrow());
    }

	@Deprecated
	public void setStartArrow(final ArrowType arrowType) {
		setStartArrowImpl(arrowType);
	}

	public void setTargetLabel(final String label) {
		getLinkController().setTargetLabel(getConnector(), label);
	}

	public boolean simulatesEdge() {
		return Shape.EDGE_LIKE.equals(getConnector().getShape());
	}

    public List<Integer> getStartInclination() {
        return pointToList(getConnector().getStartInclination());
    }

    public void setInclination(final List<Integer> startPoint, final List<Integer> endPoint) {
        if (startPoint == null || startPoint.size() != 2 || endPoint == null || endPoint.size() != 2)
            throw new IllegalArgumentException("start and end points must have 2 elements");
        getLinkController().setArrowLinkEndPoints(getConnector(), listToPoint(startPoint), listToPoint(endPoint));
    }

    public List<Integer> getEndInclination() {
        return pointToList(getConnector().getEndInclination());
    }
    
    private Point listToPoint(List<Integer> point) {
        return new Point(point.get(0), point.get(1));
    }

    private static List<Integer> pointToList(Point point) {
        ArrayList<Integer> result = new ArrayList<Integer>(2);
        result.add(point.x);
        result.add(point.y);
        return result;
    }
}
