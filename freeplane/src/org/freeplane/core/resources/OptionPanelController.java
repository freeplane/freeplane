package org.freeplane.core.resources;

import java.util.Properties;
import java.util.Vector;

import org.freeplane.core.resources.components.IPropertyControl;
import org.freeplane.core.resources.components.PropertyBean;

public class OptionPanelController {	
	private Vector<IPropertyControl> propertyControls = new Vector<IPropertyControl>();
	
	public void setCurrentPropertyControls(final Vector<IPropertyControl> props) {
		this.propertyControls = props;
	}
		
	public Properties getCurrentOptionProperties() {
		final Properties p = new Properties();
		Vector<IPropertyControl> controls = this.propertyControls; 
		for (final IPropertyControl control : controls) {
			if (control instanceof PropertyBean) {
				final PropertyBean bean = (PropertyBean) control;
				final String value = bean.getValue();				
				if (value != null) {
					p.setProperty(bean.getName(), value);
				}
			}
		}
		return p;
	}
}
