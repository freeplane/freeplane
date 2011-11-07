/**
 * author: Marcel Genzmehr
 * 07.11.2011
 */
package org.freeplane.plugin.workspace.view;

import javax.swing.plaf.SplitPaneUI;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

/**
 * 
 */
public class WorkspaceSplitPaneUI extends BasicSplitPaneUI {
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	private final SplitPaneUI wrappedUI;

	/**
	 * @param ui
	 */
	public WorkspaceSplitPaneUI(SplitPaneUI ui) {
		wrappedUI = ui;
	}

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	
	public BasicSplitPaneDivider createDefaultDivider() {
        return new WorkspaceSplitDivider(this);
    }
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/

	public SplitPaneUI getWrappedUI() {
		return wrappedUI;
	}
}
