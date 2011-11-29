/**
 * author: Marcel Genzmehr
 * 07.11.2011
 */
package org.freeplane.main.application.docear;

import java.awt.Graphics;

import javax.swing.JSplitPane;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

public class FreeplaneOneTouchSplitPaneUI extends BasicSplitPaneUI {
	private int collapseDirection = FreeplaneOneTouchSplitDivider.COLLAPSE_LEFT;
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public FreeplaneOneTouchSplitPaneUI() {
	}

	public FreeplaneOneTouchSplitPaneUI(int collapseDirection) {
		this.collapseDirection = collapseDirection;
	}
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	
	protected int getCollapseDirection() {
		return this.collapseDirection ;
	}
	
	public BasicSplitPaneDivider createDefaultDivider() {
        return new FreeplaneOneTouchSplitDivider(this, getCollapseDirection());
    }
	
	/**
     * Messaged after the JSplitPane the receiver is providing the look
     * and feel for paints its children.
     */
    public void finishedPaintingChildren(JSplitPane jc, Graphics g) {
    	//FIXME: DOCEAR - for later improvements and better gui element painting
//    	for(Component c : jc.getComponents()) {
//    		if(c instanceof FreeplaneOneTouchSplitDivider) {
//    			if(((FreeplaneOneTouchSplitDivider) c).isMouseOver()) {
//    				((FreeplaneOneTouchSplitDivider) c).paintSpecial(g);
//    			}
//    		}
//    	}
        super.finishedPaintingChildren(jc, g);
    }
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/

	
}
