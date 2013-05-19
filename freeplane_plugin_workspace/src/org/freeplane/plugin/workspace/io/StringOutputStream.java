/**
 * author: Marcel Genzmehr
 * 25.10.2011
 */
package org.freeplane.plugin.workspace.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 
 */
public class StringOutputStream extends OutputStream {
	private final StringBuffer buffer = new StringBuffer();
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public synchronized String getString() {
		return this.buffer.toString();
	}
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	public void write(int b) throws IOException {
		buffer.append(((char) b));		
	}
}
