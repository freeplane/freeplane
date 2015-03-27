package org.freeplane.features.styles;

public class StyleString implements IStyle {
	final private String string;

	public StyleString(String string) {
	    super();
	    if(string == null)
	    	throw new IllegalArgumentException("null");
	    this.string = string;
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
		return string.equals(((StyleString)obj).string);
    }

	@Override
    public int hashCode() {
	    return string.hashCode() + 37 * StyleString.class.hashCode();
    }

	@Override
    public String toString() {
	    return string;
    }
	
	
}
