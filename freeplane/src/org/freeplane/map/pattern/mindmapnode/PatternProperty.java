package org.freeplane.map.pattern.mindmapnode;

public class PatternProperty implements Cloneable{
	  public PatternProperty() {
	  }
	  
	  public PatternProperty(String value) {
	    super();
	    this.value = value;
    }

@Override
    protected Object clone(){
	    return new PatternProperty(value);
    }

protected String value;


  public String getValue() {
    return this.value;
  }

  public void setValue(String value) {
    this.value = value;
  }

}
