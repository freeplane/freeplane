package org.freeplane.view.swing.map.cloud;

import java.awt.Graphics2D;
import java.awt.Polygon;

import org.freeplane.features.cloud.CloudModel;
import org.freeplane.view.swing.map.NodeView;

public class StarCloudView extends CloudView {

	StarCloudView(CloudModel cloudModel, NodeView source) {
	    super(cloudModel, source);
	}
	protected void paintDecoration(final Graphics2D g, final Graphics2D gstroke, final double x0, final double y0,
	                                 final double x1, final double y1, double dx, double dy, double dxn, double dyn) {
		final double xctrl, yctrl;
		final double middleDistanceToConvexHull = getDistanceToConvexHull();
		final double distanceToConvexHull = middleDistanceToConvexHull * random(0.5);
		final double k = random(0.3);
		xctrl = x0 + .5f * dx * k - distanceToConvexHull * dyn;
		yctrl = y0 + .5f * dy* k + distanceToConvexHull * dxn;
		final Polygon shape = new Polygon();
		shape.addPoint((int)x0, (int)y0);
		shape.addPoint((int)xctrl, (int)yctrl);
		shape.addPoint((int)x1, (int)y1);
		g.fill(shape);
		gstroke.drawLine((int)x0, (int)y0, (int)xctrl, (int)yctrl);
		gstroke.drawLine((int)xctrl, (int)yctrl, (int)x1, (int)y1);
	}
	@Override
    protected double getDistanceToConvexHull() {
	    return 3 * super.getDistanceToConvexHull();
    }
	
}
