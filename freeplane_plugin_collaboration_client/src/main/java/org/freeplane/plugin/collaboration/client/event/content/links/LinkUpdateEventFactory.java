package org.freeplane.plugin.collaboration.client.event.content.links;

import static org.freeplane.collaboration.event.utils.EnumMapper.map;

import java.awt.Point;
import java.net.URI;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import org.freeplane.collaboration.event.MapUpdated;
import org.freeplane.collaboration.event.content.links.ConnectorData;
import org.freeplane.collaboration.event.content.links.ImmutableConnectorAdded;
import org.freeplane.collaboration.event.content.links.ImmutableConnectorData;
import org.freeplane.collaboration.event.content.links.ImmutableConnectorRemoved;
import org.freeplane.collaboration.event.content.links.ImmutableConnectorUpdated;
import org.freeplane.collaboration.event.content.links.ImmutableHyperlinkUpdated;
import org.freeplane.collaboration.event.content.links.ImmutablePoint;
import org.freeplane.core.util.ColorUtils;
import org.freeplane.features.link.ConnectorModel;
import org.freeplane.features.map.NodeModel;

class LinkUpdateEventFactory {
	public LinkUpdateEventFactory() {
		super();
	}

	MapUpdated createConnectorAddedEvent(NodeModel source, ConnectorModel connector) {
		return ImmutableConnectorAdded.builder().nodeId(source.getID()).targetId(connector.getTargetID()).build();
	}

	MapUpdated createConnectorUpdatedEvent(NodeModel source, ConnectorModel connector) {
		ConnectorData data = LinkUpdateEventFactory.of(connector);
		return ImmutableConnectorUpdated.builder().nodeId(source.getID()).connectorData(data).build();
	}

	MapUpdated createConnectorRemovedEvent(NodeModel source, ConnectorModel connector) {
		return ImmutableConnectorRemoved.builder().nodeId(source.getID()).targetId(connector.getTargetID()).build();
	}

	public MapUpdated createHyperlinkChangedEvent(NodeModel node, Optional<URI> uri) {
		return ImmutableHyperlinkUpdated.builder().nodeId(node.getID()).uri(uri.map(URI::toString)).build();
	}

	public static ConnectorData of(ConnectorModel connector) {
		final Point startInclination = connector.getStartInclination();
		final Point endInclination = connector.getEndInclination();
		return ImmutableConnectorData.builder()
		    .targetId(connector.getTargetID())
		    .alpha(connector.getAlpha())
		    .color(ColorUtils.colorToString(connector.getColor()))
		    .width(connector.getWidth())
		    .dash(Optional.ofNullable(connector.getDash())
		        .map(ints -> Arrays.stream(ints).boxed().collect(Collectors.toList())))
		    .shape(map(connector.getShape(), ConnectorData.Shape.class))
		    .showControlPointsFlag(connector.getShowControlPointsFlag())
		    .sourceLabel(Optional.ofNullable(connector.getSourceLabel()))
		    .middleLabel(Optional.ofNullable(connector.getMiddleLabel()))
		    .targetLabel(Optional.ofNullable(connector.getTargetLabel()))
		    .labelFontFamily(connector.getLabelFontFamily())
		    .labelFontSize(connector.getLabelFontSize())
		    .startArrow(map(connector.getStartArrow(), ConnectorData.ArrowType.class))
		    .endArrow(map(connector.getEndArrow(), ConnectorData.ArrowType.class))
		    .startInclination(ImmutablePoint.builder().x(startInclination.x).y(startInclination.y).build())
		    .endInclination(ImmutablePoint.builder().x(endInclination.x).y(endInclination.y).build())
		    .build();
	}
}
