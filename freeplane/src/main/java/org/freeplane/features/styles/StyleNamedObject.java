package org.freeplane.features.styles;

import org.freeplane.core.resources.NamedObject;


public class StyleNamedObject implements IStyle {
	final private NamedObject namedObject;

	public StyleNamedObject(NamedObject namedObject) {
	    super();
	    if(namedObject == null)
	    	throw new NullPointerException();
	    this.namedObject = namedObject;
    }

	public StyleNamedObject(String string) {
		if(string == null)
			throw new NullPointerException();
		namedObject = new NamedObject(string);
    }

	@Override
    public boolean equals(Object obj) {
		if(this == obj){
			return true;
		}
		if(obj == null){
			return false;
		}
		if(! this.getClass().equals(obj.getClass())){
			return false;
		}
		return namedObject.equals(((StyleNamedObject)obj).namedObject);
    }

	@Override
    public int hashCode() {
	    return namedObject.hashCode() + 37 * StyleNamedObject.class.hashCode();
    }

	@Override
    public String toString() {
	    return namedObject.toString();
    }

	public Object getObject() {
	    return namedObject.getObject();
    }

	public static String toKeyString(IStyle style) {
		if(style instanceof StyleNamedObject){
			return ((StyleNamedObject)style).getObject().toString();
		}
		return style.toString();
    }
}
