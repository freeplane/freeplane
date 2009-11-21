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

class ConnectorProxy extends AbstractProxy implements Proxy.Connector {
	
	public ConnectorModel getConnector() {
		return (ConnectorModel) getDelegate();
	}

	ConnectorProxy(ConnectorModel connector, MModeController modeController){
		super(connector, modeController);
	}
	
	private MLinkController getLinkController(){
		return (MLinkController) LinkController.getController(getModeController());
	}
	
	public Node getSource() {
		return new NodeProxy(getConnector().getSource(), getModeController());
	}

	public Node getTarget() {
		return new NodeProxy(getConnector().getTarget(), getModeController());
	}

	public Color getColor() {
		return getLinkController().getColor(getConnector());
	}

	public String getMiddleLabel() {
		return getConnector().getMiddleLabel();
	}

	public String getSourceLabel() {
		return getConnector().getSourceLabel();
	}

	public String getTargetLabel() {
		return getConnector().getTargetLabel();
	}

	public ArrowType getEndArrow() {
		return getConnector().getEndArrow();
	}

	public ArrowType getStartArrow() {
		return getConnector().getStartArrow();
	}

	public void setEndArrow(ArrowType arrowType) {
		ConnectorModel connector = getConnector();
		getLinkController().changeArrowsOfArrowLink(connector, connector.getStartArrow(), arrowType);
	}

	public void setColor(Color color) {
		getLinkController().setArrowLinkColor(getConnector(), color);
	}

	public void setStartArrow(ArrowType arrowType) {
		ConnectorModel connector = getConnector();
		getLinkController().changeArrowsOfArrowLink(connector, arrowType, connector.getEndArrow());
	}

	public void setMiddleLabel(String label) {
		getLinkController().setMiddleLabel(getConnector(), label);
	}

	public void setSimulatesEdge(boolean simulatesEdge) {
		getLinkController().setEdgeLike(getConnector(), simulatesEdge);
	}

	public void setSourceLabel(String label) {
		getLinkController().setSourceLabel(getConnector(), label);
	}

	public void setTargetLabel(String label) {
		getLinkController().setTargetLabel(getConnector(), label);
	}

	public boolean simulatesEdge() {
		return getConnector().isEdgeLike();
	}
}