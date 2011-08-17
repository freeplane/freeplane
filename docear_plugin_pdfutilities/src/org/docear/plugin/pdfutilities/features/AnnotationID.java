package org.docear.plugin.pdfutilities.features;

import java.net.URI;
import java.net.URISyntaxException;

public class AnnotationID implements Comparable<AnnotationID>{
	
	private String id;
	
	public AnnotationID(URI absoluteUri, Integer objectNumber) throws IllegalArgumentException{
		this.setId(absoluteUri, objectNumber);
	}

	public String getId() {
		return id;
	}

	public void setId(URI absoluteUri, Integer objectNumber) throws IllegalArgumentException{
		if(absoluteUri == null){
			throw new IllegalArgumentException(this.getClass().getName() + ": Uri can not be null.");
		}
		if(objectNumber == null){
			throw new IllegalArgumentException(this.getClass().getName() + ": Object number can not be null.");
		}
		String uri = absoluteUri.toString();
		uri = uri.trim();
		this.id = uri + " " + objectNumber;
	}
	
	public URI getUri(){		
		String uri = this.id.split(" ")[0];
		try {
			return new URI(uri);
		} catch (URISyntaxException e) {
			return null;
		}
	}
	
	public Integer getObjectNumber(){		
		String objectNumber = this.id.split(" ")[1];
		return Integer.valueOf(objectNumber);
	}
	
	public boolean equals(Object object){
		if(object instanceof AnnotationID){
			return this.getUri().equals(((AnnotationID) object).getUri()) && this.getObjectNumber().equals(((AnnotationID) object).getObjectNumber());
			//return this.id.equals(((AnnotationID) object).getId());
		}
		else{
			return super.equals(object);
		}
	}

	public int compareTo(AnnotationID id) {
		if (id.getId() == null && this.getId() == null) {
	      return 0;
	    }
	    if (this.getId() == null) {
	      return 1;
	    }
	    if (id.getId() == null) {
	      return -1;
	    }
	    return this.getId().compareTo(id.getId());
	}
	
	public int hashCode(){		
		return this.id.hashCode();
		
	}
	
	

}
