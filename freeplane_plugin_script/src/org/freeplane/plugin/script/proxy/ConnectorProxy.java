/**
 * 
 */
package org.freeplane.plugin.script.proxy;

import java.awt.Color;

import org.freeplane.features.common.link.ArrowType;
import org.freeplane.features.common.link.ConnectorModel;
import org.freeplane.features.common.link.LinkController;
import org.freeplane.features.mindmapmode.MModeController;
import org.freeplane.features.mindmapmode.link.MLinkController;
import org.freeplane.plugin.script.proxy.Proxy.Node;

class ConnectorProxy extends AbstractProxy<ConnectorModel> implements Proxy.Connector {

	ConnectorProxy(final ConnectorModel connector,
			final MModeController modeController) {
		super(connector, modeController);
	}

	public Color getColor() {
		return getLinkController().getColor(getConnector());
	}

	public ConnectorModel getConnector() {
		return (ConnectorModel) getDelegate();
	}

	public ArrowType getEndArrow() {
		return getConnector().getEndArrow();
	}

	private MLinkController getLinkController() {
		return (MLinkController) LinkController
				.getController(getModeController());
	}

	public String getMiddleLabel() {
		return getConnector().getMiddleLabel();
	}

	public Node getSource() {
		return new NodeProxy(getConnector().getSource(), getModeController());
	}

	public String getSourceLabel() {
		return getConnector().getSourceLabel();
	}

	public ArrowType getStartArrow() {
		return getConnector().getStartArrow();
	}

	public Node getTarget() {
		return new NodeProxy(getConnector().getTarget(), getModeController());
	}

	public String getTargetLabel() {
		return getConnector().getTargetLabel();
	}

	public void setColor(final Color color) {
		getLinkController().setArrowLinkColor(getConnector(), color);
	}

	public void setEndArrow(final ArrowType arrowType) {
		final ConnectorModel connector = getConnector();
		getLinkController().changeArrowsOfArrowLink(connector,
				connector.getStartArrow(), arrowType);
	}

	public void setMiddleLabel(final String label) {
		getLinkController().setMiddleLabel(getConnector(), label);
	}

	public void setSimulatesEdge(final boolean simulatesEdge) {
		getLinkController().setEdgeLike(getConnector(), simulatesEdge);
	}

	public void setSourceLabel(final String label) {
		getLinkController().setSourceLabel(getConnector(), label);
	}

	public void setStartArrow(final ArrowType arrowType) {
		final ConnectorModel connector = getConnector();
		getLinkController().changeArrowsOfArrowLink(connector, arrowType,
				connector.getEndArrow());
	}

	public void setTargetLabel(final String label) {
		getLinkController().setTargetLabel(getConnector(), label);
	}

	public boolean simulatesEdge() {
		return getConnector().isEdgeLike();
	}
}