package org.freeplane.core.actions;

import javax.swing.Action;

import org.freeplane.core.controller.Named;

/**
 * Specifics for freeplane actions to be put here.
 * 
 * @author robert.ladstaetter
 */
// time will tell which methods should be placed here
public interface IFreeplaneAction extends Named, Action {

	// TODO rladstaetter 15.02.2009 getName() for this interface should be deprecated. use getClass().getName() as key. 
	// since the actions are connected to static html which isn't generated, this interface extends from Named. 
	// once the handling of actions is refactored, this dependency can be removed
}
