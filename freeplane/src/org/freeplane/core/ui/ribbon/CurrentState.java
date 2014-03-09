package org.freeplane.core.ui.ribbon;

import java.util.HashMap;
import java.util.Map;

import org.freeplane.features.mode.Controller;
import org.freeplane.features.ui.CloseAction;
import org.freeplane.features.url.mindmapmode.OpenAction;

public class CurrentState {
	private Map<Class<? extends Object>, Object> map = new HashMap<Class<? extends Object>, Object>();
 
	public void set(Class<? extends Object> key, Object value) {
		map.put(key, value);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Object> T get(Class<T> key) {
		return (T) map.get(key);
	}
	
	public boolean contains(Class<? extends Object> key) {
		return (get(key) != null);
	}
	
	public boolean isNodeChangeEvent() {
		return map.get(OpenAction.class) == null && map.get(CloseAction.class) == null && Controller.getCurrentController().getMapViewManager().getMaps().size() >= 1;
	}
	
	public boolean allMapsClosed() {
		return map.get(CloseAction.class) != null && Controller.getCurrentController().getMapViewManager().getMaps().size() <= 1; 
	}

}
