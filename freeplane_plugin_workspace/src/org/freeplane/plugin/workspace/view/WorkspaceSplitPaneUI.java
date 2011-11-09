/**
 * author: Marcel Genzmehr
 * 07.11.2011
 */
package org.freeplane.plugin.workspace.view;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.JSplitPane;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

/**
 * 
 */
public class WorkspaceSplitPaneUI extends BasicSplitPaneUI {
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public WorkspaceSplitPaneUI() {
	}

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	
	public BasicSplitPaneDivider createDefaultDivider() {
        return new WorkspaceSplitDivider(this);
    }
	
	/**
     * Messaged after the JSplitPane the receiver is providing the look
     * and feel for paints its children.
     */
    public void finishedPaintingChildren(JSplitPane jc, Graphics g) {
    	for(Component c : jc.getComponents()) {
    		if(c instanceof WorkspaceSplitDivider) {
    			if(((WorkspaceSplitDivider) c).isMouseOver()) {
    				((WorkspaceSplitDivider) c).paintSpecial(g);
    			}
    		}
    	}
        super.finishedPaintingChildren(jc, g);
    }
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/

	
}
