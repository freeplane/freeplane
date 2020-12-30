package org.freeplane.view.swing.map.link;

import java.awt.Point;

import org.freeplane.features.link.ConnectorModel;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.styles.MapStyleModel;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.NodeView;

public class InclinationRecommender {
    private static final int RECOMMENDED_LENGTH_FACTOR = 2;
    private static final int LOOP_INCLINE_OFFSET = 45;
    private final LinkController linkController;
    private ConnectorModel connector;
    private final int recommendedLength;
    private boolean selfLink;


    public InclinationRecommender(LinkController linkController, ConnectorView connectorView) {
        this.linkController = linkController;
        connector = connectorView.getModel();
        NodeView source = connectorView.getSource();
        NodeView target = connectorView.getTarget();
        if (connectorView.isSourceVisible() && connectorView.isTargetVisible()  && ! source.equals(target))
            recommendedLength = Math.max(40, (int)(source.getLinkPoint(null).distance(target.getLinkPoint(null)) / connectorView.getZoom()));
        else
            recommendedLength = (int) (source.getMainView().getHeight() * RECOMMENDED_LENGTH_FACTOR / connectorView.getZoom());
        selfLink = source == target;
    }

    /**
     */
    public Point calcStartInclination() {
        int x = recommendedLength;
        if(MapStyleModel.isStyleNode(connector.getSource())) {
            boolean hasLineShape = linkController.getShape(connector) == ConnectorModel.Shape.LINE;
            int y = hasLineShape ? 0 : -recommendedLength/(RECOMMENDED_LENGTH_FACTOR * 2);
            return new Point(x, y);
        } else {
            int y = 0;
            return new Point(x, y);
        }
    }

    private void fixInclineIfLoopNode(Point endIncline) {
        if(!MapStyleModel.isStyleNode(connector.getSource())) {
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