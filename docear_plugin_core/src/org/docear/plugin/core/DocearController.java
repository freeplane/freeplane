/**
 * author: Marcel Genzmehr
 * 19.08.2011
 */
package org.docear.plugin.core;

/**
 * 
 */
public class DocearController {
	
	private final static DocearController docearController = new DocearController();
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	
	protected DocearController() {
	}
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public static DocearController getController() {
		return docearController;
	}
	
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
