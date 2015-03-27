package org.freeplane.view.swing.map.cloud;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.QuadCurve2D;

import org.freeplane.features.cloud.CloudModel;
import org.freeplane.view.swing.map.NodeView;

public class ArcCloudView extends CloudView {

	@Override
    protected double calcDistanceBetweenPoints() {
	    final double distanceBetweenPoints;
		if (getIterativeLevel() > 4) {
			distanceBetweenPoints = 140 * getZoom(); /* flat */
		}
		else{
			 distanceBetweenPoints = 6 * getDistanceToConvexHull();
		}
		return distanceBetweenPoints;
    }

	ArcCloudView(CloudModel cloudModel, NodeView source) {
	    super(cloudModel, source);
    }

	protected void paintDecoration(final Graphics2D g, final Graphics2D gstroke, final double x0, final double y0,
                                 final double x1, final double y1, double dx, double dy, double dxn, double dyn) {
	    double xctrl;
	    double yctrl;
		final double middleDistanceToConvexHull = getDistanceToConvexHull();
	    final double distanceToConvexHull = middleDistanceToConvexHull * 2.2 * random(0.7);
		xctrl = x0 + .5f * dx - distanceToConvexHull * dyn;
		yctrl = y0 + .5f * dy + distanceToConvexHull * dxn;
		final Shape shape = new QuadCurve2D.Double(x0, y0, xctrl, yctrl, x1, y1);
		g.fill(shape);
		gstroke.draw(shape);
    }
	
	
}
