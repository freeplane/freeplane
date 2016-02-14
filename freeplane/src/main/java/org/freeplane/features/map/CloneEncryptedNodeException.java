package org.freeplane.features.map;

@SuppressWarnings("serial") 
public class CloneEncryptedNodeException extends IllegalArgumentException{

	public CloneEncryptedNodeException() {
		super("Can not clone encrypted nodes");
	}
	
}