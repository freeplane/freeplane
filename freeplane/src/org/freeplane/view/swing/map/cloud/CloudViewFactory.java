package org.freeplane.view.swing.map.cloud;

import java.util.NoSuchElementException;

import org.freeplane.features.cloud.CloudModel;
import org.freeplane.features.cloud.CloudModel.Shape;
import org.freeplane.view.swing.map.NodeView;

public class CloudViewFactory {
	public CloudView createCloudView(CloudModel cloudModel, NodeView nodeView){
		if(null == cloudModel.getShape())
			return new ArcCloudView(cloudModel, nodeView);
		if(Shape.ARC.equals(cloudModel.getShape()))
			return new ArcCloudView(cloudModel, nodeView);
		if(Shape.STAR.equals(cloudModel.getShape()))
			return new StarCloudView(cloudModel, nodeView);
		if(Shape.ROUND_RECT.equals(cloudModel.getShape()))
			return new RectangleCloudView(cloudModel, nodeView, true);
		if(Shape.RECT.equals(cloudModel.getShape()))
			return new RectangleCloudView(cloudModel, nodeView, false);
		throw new NoSuchElementException();
	}
}
