package org.freeplane.view.swing.map.link;

import java.awt.Point;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.link.ConnectorModel;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.styles.MapStyleModel;
import org.freeplane.view.swing.map.NodeView;

public class InclinationRecommender {
    private static final int LOOP_INCLINE_OFFSET = 45;
    private final LinkController linkController;
    private ConnectorModel connector;
    private final int recommendedLength;
    private final int recommendedHeight;
    private boolean selfLink;


    public InclinationRecommender(LinkController linkController, ConnectorView connectorView) {
        this.linkController = linkController;
        connector = connectorView.getModel();
        NodeView source = connectorView.getSource();
        NodeView target = connectorView.getTarget();
        if (!connectorView.isSourceVisible() || !connectorView.isTargetVisible() || source.equals(target)) {
            recommendedLength = (int) (UITools.FONT_SCALE_FACTOR * 100);
        } else
            recommendedLength = Math.max(40, (int)(source.getLinkPoint(null).distance(target.getLinkPoint(null)) / connectorView.getZoom()));
        recommendedHeight = (source.isContentVisible() ? source : target).getMainView().getHeight();
        selfLink = source == target;
    }

    /**
     */
    public Point calcStartInclination() {
        if(MapStyleModel.isStyleNode(connector.getSource())) {
            boolean hasLineShape = linkController.getShape(connector) == ConnectorModel.Shape.LINE;
            int y = hasLineShape ? 0 : -recommendedHeight / 2;
            return new Point(recommendedLength, y);
        } else {
            return new Point(recommendedLength, 0);
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