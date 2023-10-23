/*
 * Created on 2 Oct 2023
 *
 * author dimitry
 */
package org.freeplane.api;

import javax.swing.SwingConstants;

public enum HorizontalTextAlignment {
	DEFAULT(SwingConstants.LEFT), 
	LEFT(SwingConstants.LEFT), 
	RIGHT(SwingConstants.RIGHT), 
	CENTER(SwingConstants.CENTER);
	
	final public int swingConstant;

	HorizontalTextAlignment(int swingConstant){
		this.swingConstant = swingConstant;}
}