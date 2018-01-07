package org.freeplane.plugin.collaboration.client.event.content.links;

import java.awt.Point;
import java.util.List;

import org.freeplane.features.link.ArrowType;
import org.freeplane.features.link.ConnectorModel.Shape;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableConnectorData.class)
@JsonDeserialize(as = ImmutableConnectorData.class)
public interface ConnectorData{
	String targetId();
	
	int getAlpha();
	String getColor();
	
	int getWidth();
	List<Integer> getDash();

	Shape getShape();
	boolean getShowControlPointsFlag();
	
	String getSourceLabel();
	String getMiddleLabel();
	String getTargetLabel();
	
	String getLabelFontFamily();
	int getLabelFontSize();

	ArrowType getStartArrow();
	ArrowType getEndArrow();
	Point getStartInclination();
	Point getEndInclination();
}
