package org.docear.plugin.core.features;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.freeplane.core.extension.IExtension;

public class DocearNodeModelExtension implements IExtension{
	
	public enum DocearExtensionKey{
		MONITOR_PATH
	};
	
	Map<String, Object> map = new HashMap<String, Object>();
	String xmlBuilderKey;	
	
	public String getXmlBuilderKey() {
		return xmlBuilderKey;
	}

	public void setXmlBuilderKey(String xmlBuilderKey) {
		this.xmlBuilderKey = xmlBuilderKey;
	}	

	public void putEntry(String key, Object value){
		this.map.put(key, value);
	}
	
	public void putEntry(Entry<String, Object> entry){
		this.map.put(entry.getKey(), entry.getValue());
	}
	
	public void putAllEntries(Map<String, Object> map){
		this.map.putAll(map);
	}
	
	public Object getValue(String key){
		return this.map.get(key);
	}
	
	public boolean containsKey(String key){
		return this.map.containsKey(key);
	}
	
	public Map<String, Object> getMap(){
		return this.map;
	}
	
	public Set<Entry<String, Object>> getAllEntries(){
		return this.map.entrySet();
	}
	
	

}
