package org.freeplane.view.swing.map.cloud;

import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;

import org.freeplane.features.cloud.CloudModel;
import org.freeplane.view.swing.map.NodeView;

public class RectangleCloudView extends CloudView {
	
	private final boolean isRound;
	RectangleCloudView(CloudModel cloudModel, NodeView source, boolean isRound) {
	    super(cloudModel, source);
	    this.isRound = isRound;
	}

	@Override
    protected void fillPolygon(Polygon p, Graphics2D g) {
    }

	@Override
    protected void paintDecoration(Graphics2D g, Graphics2D gstroke) {
	    Polygon p = getCoordinates();
        final int distanceToConvexHull = (int) getDistanceToConvexHull();
	    final Rectangle bounds = source.getInnerBounds();
	    bounds.x -= distanceToConvexHull;
	    bounds.width += 2 * distanceToConvexHull;
	    if(isRound){
			g.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, distanceToConvexHull, distanceToConvexHull);
			gstroke.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, distanceToConvexHull, distanceToConvexHull);
		}
		else{
			g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
			gstroke.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
		}
    }

	@Override
    protected void paintDecoration(Graphics2D g, Graphics2D gstroke, double x0, double y0, double x1, double y1,
                                   double dx, double dy, double dxn, double dyn) {	    
    }

    @Override
    protected double getDistanceToConvexHull() {
        return 0.5 * super.getDistanceToConvexHull();
    }
}
