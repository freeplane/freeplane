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
@EnabledAction(checkOnNodeChange=true)
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
	
	public boolean isEnabled() {
		return b.isEnabled();
	}
	
	public void setEnabled() {
		if(a != null && checkEnabledAction(a)) this.a.setEnabled();
		if(b != null && checkEnabledAction(b)) this.b.setEnabled();
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
	
	private boolean checkEnabledAction(final AFreeplaneAction action) {
		final EnabledAction annotation = action.getClass().getAnnotation(EnabledAction.class);
		if (annotation == null) {
			return false;
		}
		return annotation.checkOnNodeChange();
	}

}
