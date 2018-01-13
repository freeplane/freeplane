package org.freeplane.plugin.collaboration.client.event.content.links;

import java.awt.Point;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang.ArrayUtils;
import org.freeplane.core.util.ColorUtils;
import org.freeplane.features.link.ArrowType;
import org.freeplane.features.link.ConnectorModel;
import org.freeplane.features.link.ConnectorModel.Shape;
import org.freeplane.features.map.NodeModel;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableConnectorData.class)
@JsonDeserialize(as = ImmutableConnectorData.class)
public interface ConnectorData {
	String targetId();

	int getAlpha();

	String getColor();

	int getWidth();

	Optional<List<Integer>> getDash();

	Shape getShape();

	boolean getShowControlPointsFlag();

	Optional<String> getSourceLabel();

	Optional<String> getMiddleLabel();

	Optional<String> getTargetLabel();

	String getLabelFontFamily();

	int getLabelFontSize();

	ArrowType getStartArrow();

	ArrowType getEndArrow();

	Point getStartInclination();

	Point getEndInclination();

	static ConnectorData of(ConnectorModel connector) {
		return ImmutableConnectorData.builder()
		    .targetId(connector.getTargetID())
		    .alpha(connector.getAlpha())
		    .color(ColorUtils.colorToString(connector.getColor()))
		    .width(connector.getWidth())
		    .dash(Optional.ofNullable(connector.getDash())
		        .map(ints -> Arrays.stream(ints).boxed().collect(Collectors.toList())))
		    .shape(connector.getShape())
		    .showControlPointsFlag(connector.getShowControlPointsFlag())
		    .sourceLabel(Optional.ofNullable(connector.getSourceLabel()))
		    .middleLabel(Optional.ofNullable(connector.getMiddleLabel()))
		    .targetLabel(Optional.ofNullable(connector.getTargetLabel()))
		    .labelFontFamily(connector.getLabelFontFamily())
		    .labelFontSize(connector.getLabelFontSize())
		    .startArrow(connector.getStartArrow())
		    .endArrow(connector.getEndArrow())
		    .startInclination(connector.getStartInclination())
		    .endInclination(connector.getEndInclination()).build();
	}

	default ConnectorModel connectorFrom(NodeModel node) {
		final ConnectorModel connectorModel = new ConnectorModel(node,
		    targetId(),
		    getStartArrow(), getEndArrow(),
		    getDash().map(ints -> ArrayUtils.toPrimitive(ints.toArray(new Integer[0]))).orElse(null),
		    ColorUtils.rgbStringToColor(getColor()),
		    getAlpha(),
		    getShape(),
		    getWidth(),
		    getLabelFontFamily(),
		    getLabelFontSize());
		getSourceLabel().ifPresent(connectorModel::setSourceLabel);
		getMiddleLabel().ifPresent(connectorModel::setMiddleLabel);
		getTargetLabel().ifPresent(connectorModel::setTargetLabel);
		return connectorModel;
	}
}
