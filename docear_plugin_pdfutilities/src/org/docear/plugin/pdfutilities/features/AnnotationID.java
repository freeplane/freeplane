package org.docear.plugin.pdfutilities.features;

import java.net.URI;

public class AnnotationID implements Comparable<AnnotationID>{
	
	private String id;
	private URI uri;
	private Integer objectNumber;
	
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
		
		String uri = absoluteUri.getPath();
		uri = uri.trim();
		this.id = uri + " " + objectNumber;
		this.objectNumber = objectNumber;
		this.uri = absoluteUri;
	}
	
	public URI getUri(){		
		return this.uri;
	}
	
	public Integer getObjectNumber(){		
		return this.objectNumber;
	}
	
	public boolean equals(Object object){
		if(object instanceof AnnotationID){
			return this.getUri().equals(((AnnotationID) object).getUri()) && this.getObjectNumber().equals(((AnnotationID) object).getObjectNumber());
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
