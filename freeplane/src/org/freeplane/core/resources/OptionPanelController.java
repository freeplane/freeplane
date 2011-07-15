package org.freeplane.core.resources;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.freeplane.core.resources.components.IPropertyControl;
import org.freeplane.core.resources.components.PropertyBean;

public class OptionPanelController {	
	private Vector<IPropertyControl> propertyControls = new Vector<IPropertyControl>();
	private List<ActionListener> list = new ArrayList<ActionListener>();
	private List<PropertyLoadListener> loadListener = new ArrayList<PropertyLoadListener>();
	
	public void addPropertyLoadListener(PropertyLoadListener listener){
		loadListener.add(listener);
	}
	
	public void addButtonListener(ActionListener listener) {
		list.add(listener);
	}

	public void actionPerformed(ActionEvent e) {
		for(ActionListener listener : list) {
			listener.actionPerformed(e);
		}

	}
	
	public void setCurrentPropertyControls(final Vector<IPropertyControl> props) {
		this.propertyControls = props;
		for(PropertyLoadListener listener : loadListener){
			listener.propertiesLoaded(props);
		}
	}
	
	public IPropertyControl getPropertyControl(String name){
		for(IPropertyControl control : this.propertyControls){
			if(control.getName() != null && control.getName().equals(name)){
				return control;
			}
		}
		return null;
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
	
	public interface PropertyLoadListener {
		
		public void propertiesLoaded(Collection<IPropertyControl> properties);
		
	}
}
