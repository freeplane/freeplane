/**
 * 
 */
package org.freeplane.plugin.script.proxy;

import java.util.Vector;

import org.freeplane.core.model.NodeModel;
import org.freeplane.features.common.attribute.Attribute;
import org.freeplane.features.common.attribute.AttributeController;
import org.freeplane.features.common.attribute.NodeAttributeTableModel;
import org.freeplane.features.mindmapmode.MModeController;
import org.freeplane.features.mindmapmode.attribute.MAttributeController;

class AttributesProxy extends AbstractProxy implements Proxy.Attributes {
	AttributesProxy(NodeModel delegate, MModeController modeController) {
		super(delegate, modeController);
	}

	public void set(String key, String value) {
		int attributeNumber = findAttribute(key);
		Attribute attribute = new Attribute(key, value);
		if(attributeNumber == -1){
			getAttributeController().addAttribute(getNode(), attribute);
			return;
		}
		getAttributeController().setAttribute(getNode(), attributeNumber, attribute);
	}

	public int findAttribute(String key) {
		NodeAttributeTableModel nodeAttributeTableModel = getNodeAttributeTableModel();
		if(nodeAttributeTableModel == null){
			return -1;
		}
		Vector<Attribute> attributes = nodeAttributeTableModel.getAttributes();
		int i = 0;
		for(Attribute a : attributes){
			if(a.getName().equals(key)){
				return i;
			}
			i++;
		}
		return -1;
	}

	public String get(String key) {
		int attributeNumber = findAttribute(key);
		if(attributeNumber == -1){
			return null;
		}
		NodeAttributeTableModel nodeAttributeTableModel = getNodeAttributeTableModel();
		return nodeAttributeTableModel.getAttribute(attributeNumber).getValue();
	}
	
	public boolean remove(String key) {
		int attributeNumber = findAttribute(key);
		if(attributeNumber == -1){
			return false;
		}
		getAttributeController().removeAttribute(getNode(), attributeNumber);
		return true;
	}
	
	NodeAttributeTableModel getNodeAttributeTableModel(){
		return NodeAttributeTableModel.getModel(getNode());
	}
	
	MAttributeController getAttributeController(){
		return (MAttributeController) AttributeController.getController(getModeController());
	}

}