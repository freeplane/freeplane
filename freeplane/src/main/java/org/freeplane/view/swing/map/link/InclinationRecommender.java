package org.freeplane.view.swing.map.link;

import java.awt.Point;

import org.freeplane.features.link.ConnectorModel;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.styles.MapStyleModel;
import org.freeplane.view.swing.map.NodeView;

public class InclinationRecommender {
    private static final int LOOP_INCLINE_OFFSET = 45;
    private final LinkController linkController;
    private ConnectorModel connector;
    private int dellength;
    private boolean selfLink;


    public InclinationRecommender(LinkController linkController, ConnectorView connectorView) {
        this.linkController = linkController;
        connector = connectorView.getModel();
        NodeView source = connectorView.getSource();
        NodeView target = connectorView.getTarget();
        dellength = connectorView.isSourceVisible() && connectorView.isTargetVisible()  && ! source.equals(target) ? 
                Math.max(40, (int)(source.getLinkPoint(connector.getStartInclination()).distance(target.getLinkPoint(connector.getEndInclination())) / connectorView.getZoom())) 
                : 40;
        selfLink = source == target;
    }

    /**
     */
    public Point calcStartInclination() {
        if(connector.getSource().getUserObject().equals(MapStyleModel.DEFAULT_STYLE)) {
            boolean hasLineShape = linkController.getShape(connector) == ConnectorModel.Shape.LINE;
            int x = hasLineShape ? 0 : -LOOP_INCLINE_OFFSET*3;
            int y = hasLineShape ? -2 * LOOP_INCLINE_OFFSET : -LOOP_INCLINE_OFFSET;
            return new Point(x, y);
        } else {
            int x = dellength;
            int y = 0;
            return new Point(x, y);
        }
    }

    void fixInclineIfLoopNode(Point endIncline) {
        if(connector.getSource().getUserObject().equals(MapStyleModel.DEFAULT_STYLE)) {
            endIncline.x = LOOP_INCLINE_OFFSET*3;
            endIncline.y = -LOOP_INCLINE_OFFSET;
        } else {
            endIncline.y += LOOP_INCLINE_OFFSET;
        }


    }

    public Point calcEndInclination() {
        Point endInclination = calcStartInclination();
        endInclination.y = -endInclination.y;
        if (selfLink) {
            fixInclineIfLoopNode(endInclination);
        }
        return endInclination;
    }

}