package org.freeplane.features.styles;

import org.freeplane.core.resources.TranslatedObject;


public class StyleTranslatedObject implements IStyle {
	final private TranslatedObject translatedObject;

	public StyleTranslatedObject(TranslatedObject namedObject) {
	    super();
	    if(namedObject == null)
	    	throw new NullPointerException();
	    this.translatedObject = namedObject;
    }

	public StyleTranslatedObject(String string) {
		if(string == null)
			throw new NullPointerException();
		translatedObject = new TranslatedObject(string);
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
		return translatedObject.equals(((StyleTranslatedObject)obj).translatedObject);
    }

	@Override
    public int hashCode() {
	    return translatedObject.hashCode() + 37 * StyleTranslatedObject.class.hashCode();
    }

	@Override
    public String toString() {
	    return translatedObject.toString();
    }

	public Object getObject() {
	    return translatedObject.getObject();
    }

	public static String toKeyString(IStyle style) {
		if(style instanceof StyleTranslatedObject){
			return ((StyleTranslatedObject)style).getObject().toString();
		}
		return style.toString();
    }
}
