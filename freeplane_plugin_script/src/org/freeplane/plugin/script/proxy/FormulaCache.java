package org.freeplane.plugin.script.proxy;

import java.util.HashMap;

public class FormulaCache {
	private HashMap<String, Object> idValueMap = new HashMap<String, Object>();

	public Object get(String id) {
		return idValueMap.get(id);
	}

	public void put(String id, Object value) {
		idValueMap.put(id, value);
    }
}
