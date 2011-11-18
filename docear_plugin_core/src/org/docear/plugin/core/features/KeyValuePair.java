package org.docear.plugin.core.features;



public class KeyValuePair {
	
	private String key;
	private Object value;
	
	public KeyValuePair(){}
	
	public KeyValuePair(String key, Object value) {
		super();
		this.key = key;
		this.value = value;
	}
	
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	
	public boolean equals(Object object){
		if(object instanceof KeyValuePair){
			return this.key.equals(((KeyValuePair) object).getKey()) && this.value.equals(((KeyValuePair) object).getValue());
		}
		else{
			return super.equals(object);
		}
	}

	public int compareTo(KeyValuePair keyValuePair) {
		if (keyValuePair.getKey() == null && this.getKey() == null) {
	      return 0;
	    }
	    if (this.getKey() == null) {
	      return 1;
	    }
	    if (keyValuePair.getKey() == null) {
	      return -1;
	    }
	    return this.getKey().compareTo(keyValuePair.getKey());
	}
	
	public int hashCode(){		
		return this.key.hashCode();
		
	}
}
