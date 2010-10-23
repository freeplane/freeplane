package org.freeplane.features.common.filter.condition;

import javax.swing.JComponent;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.freeplane.n3.nanoxml.XMLElement;


public abstract class ASelectableCondition  implements ICondition{
	transient private String description;
	transient private JComponent renderer;

	public ASelectableCondition() {
		super();
	}


	@Override
    public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
    }

	@Override
    public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
    }
	protected abstract String createDesctiption();
	
	final public JComponent getListCellRendererComponent() {
		if (renderer == null) {
			renderer = createRendererComponent();
		}
		return renderer;
	}

	protected JComponent createRendererComponent() {
	    return ConditionFactory.createCellRendererComponent(toString());
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
