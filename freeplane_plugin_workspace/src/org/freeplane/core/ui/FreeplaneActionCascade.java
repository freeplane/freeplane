/**
 * author: Marcel Genzmehr
 * 02.12.2011
 */
package org.freeplane.core.ui;

import java.util.ArrayList;
import java.util.List;

import org.freeplane.features.mode.Controller;

/**
 * 
 */
public class FreeplaneActionCascade {
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public static void addAction(AFreeplaneAction action) {
		AFreeplaneAction previousAction = Controller.getCurrentController().getAction(action.getKey());
		if(previousAction != null) {
			Controller.getCurrentController().removeAction(action.getKey());
		}
		Controller.getCurrentController().addAction(new FreeplaneActionMultiCaster(previousAction, action));
	}
	
	public static void insertActionBefore(AFreeplaneAction action) {
		AFreeplaneAction previousAction = Controller.getCurrentController().getAction(action.getKey());
		if(previousAction != null) {
			Controller.getCurrentController().removeAction(action.getKey());
		}
		Controller.getCurrentController().addAction(new FreeplaneActionMultiCaster(action, previousAction));
	}
	
	public static boolean hasAction(String key) {
		return (Controller.getCurrentController().getAction(key) != null);
	}
	
	public static AFreeplaneAction[] getActionsForKey(String key) {
		ArrayList<AFreeplaneAction> list = new ArrayList<AFreeplaneAction>();
		AFreeplaneAction action = Controller.getCurrentController().getAction(key);
		if(action == null) {
			return null;
		}
		if(action instanceof FreeplaneActionMultiCaster) {
			traverseCaster(list, (FreeplaneActionMultiCaster) action);
		} 
		else {
			list.add(action);
		}
		return list.toArray(new AFreeplaneAction[]{});
	}
	
	private static void traverseCaster(List<AFreeplaneAction> list, FreeplaneActionMultiCaster caster) {
		if(caster.getB() != null) { 
			if(caster.getB() instanceof FreeplaneActionMultiCaster) {
				traverseCaster(list, (FreeplaneActionMultiCaster) caster.getB());
			}
			list.add(caster.getB());
		}
		if(caster.getA() != null) {
			if(caster.getA() instanceof FreeplaneActionMultiCaster) {
				traverseCaster(list, (FreeplaneActionMultiCaster) caster.getA());
			}
			list.add(caster.getA());
		}
		
		
	}
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
