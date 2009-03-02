package org.freeplane.core.actions;

import javax.swing.Action;

import org.freeplane.core.model.INamed;

/**
 * Specifics for freeplane actions to be put here.
 * 
 * @author robert.ladstaetter
 */
// time will tell which methods should be placed here
public interface IFreeplaneAction extends INamed, Action {

	// rladstaetter 15.02.2009 getName() for this interface should be deprecated. use getClass().getName() as key. 
	// since the actions are connected to static xml config files which aren't generated, this interface extends from Named. 
	// once the handling of actions is refactored, this dependency can be removed
	
	// every freeplaneaction should extend from an abstract freeplane action which provides the getName() functionality.
}
