package org.freeplane.features.common.filter.condition;

import javax.swing.JComponent;

import org.freeplane.n3.nanoxml.XMLElement;


public abstract class ASelectableCondition  implements ISelectableCondition{
	private String description;
	private JComponent renderer;

	public ASelectableCondition() {
		super();
	}

	protected abstract String createDesctiption();

	public JComponent getListCellRendererComponent() {
    	if (renderer == null) {
    		renderer = ConditionFactory.createCellRendererComponent(toString());
    	}
    	return renderer;
    }

	@Override
    public String toString() {
    	if (description == null) {
    		description = createDesctiption();
    	}
    	return description;
    }
	
	public void toXml(final XMLElement element) {
		final XMLElement child = new XMLElement();
		child.setName(getName());
		fillXML(child);
		element.addChild(child);
	}

	protected void fillXML(XMLElement element){}

	abstract protected String getName();

}
