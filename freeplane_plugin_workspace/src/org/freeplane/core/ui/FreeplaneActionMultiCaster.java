/**
 * author: Marcel Genzmehr
 * 02.12.2011
 */
package org.freeplane.core.ui;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;

/**
 * 
 */
@EnabledAction(checkOnPopup=true)
public class FreeplaneActionMultiCaster extends AFreeplaneAction {

	private static final long serialVersionUID = 1L;
	
	private final AFreeplaneAction a;
	private final AFreeplaneAction b;
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	
	public FreeplaneActionMultiCaster(AFreeplaneAction a, AFreeplaneAction b) {
		super(b.getKey());
		
		if(a != null) {
			assert(b.getKey().equals(a.getKey()));
		}
		
		this.a = a;
		this.b = b;
	}
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	
	public void setEnabled() {
		//FIXME: DOCEAR - check annotation settings
		this.a.setEnabled();
		this.b.setEnabled();
	}
	
	public AFreeplaneAction getA() {
		return a;
	}

	public AFreeplaneAction getB() {
		return b;
	}
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public void actionPerformed(ActionEvent e) {		
		if(b != null) b.actionPerformed(e);
		if(a != null) a.actionPerformed(e);
	}
}
