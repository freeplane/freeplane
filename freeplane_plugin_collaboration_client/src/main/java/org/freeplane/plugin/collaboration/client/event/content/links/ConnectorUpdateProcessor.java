package org.freeplane.plugin.collaboration.client.event.content.links;

import static org.freeplane.collaboration.event.utils.EnumMapper.map;

import java.awt.Point;

import org.apache.commons.lang.ArrayUtils;
import org.freeplane.collaboration.event.content.links.ConnectorData;
import org.freeplane.collaboration.event.content.links.ConnectorUpdated;
import org.freeplane.core.util.ColorUtils;
import org.freeplane.features.link.ArrowType;
import org.freeplane.features.link.ConnectorModel;
import org.freeplane.features.link.ConnectorModel.Shape;
import org.freeplane.features.link.mindmapmode.MLinkController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.collaboration.client.event.UpdateProcessor;

public class ConnectorUpdateProcessor implements UpdateProcessor<ConnectorUpdated> {
	private final MLinkController linkController;

	public ConnectorUpdateProcessor(MLinkController linkController) {
		this.linkController = linkController;
	}

	@Override
	public void onUpdate(MapModel map, ConnectorUpdated event) {
		NodeModel node = map.getNodeForID(event.nodeId());
		ConnectorModel connector = connectorFrom(node, event.getConnectorData());
		linkController.swapProperties(connector);
	}

	@Override
	public Class<ConnectorUpdated> eventClass() {
		return ConnectorUpdated.class;
	}

	private ConnectorModel connectorFrom(NodeModel node, ConnectorData data) {
		final ConnectorModel connectorModel = new ConnectorModel(node,
		    data.targetId(),
		    map(data.getStartArrow(), ArrowType.class),
		    map(data.getEndArrow(), ArrowType.class),
		    data.getDash().map(ints -> ArrayUtils.toPrimitive(ints.toArray(new Integer[0]))).orElse(null),
		    ColorUtils.rgbStringToColor(data.getColor()),
		    data.getAlpha(),
		    map(data.getShape(), Shape.class),
		    data.getWidth(),
		    data.getLabelFontFamily(),
		    data.getLabelFontSize());
		data.getSourceLabel().ifPresent(connectorModel::setSourceLabel);
		data.getMiddleLabel().ifPresent(connectorModel::setMiddleLabel);
		data.getTargetLabel().ifPresent(connectorModel::setTargetLabel);
		final ConnectorData.Point startInclination = data.getStartInclination();
		connectorModel.setStartInclination(new Point(startInclination.x(), startInclination.y()));
		final ConnectorData.Point endInclination = data.getEndInclination();
		connectorModel.setEndInclination(new Point(endInclination.x(), endInclination.y()));
		return connectorModel;
	}
}
