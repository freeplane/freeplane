package org.freeplane.view.swing.map;

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JToolTip;

import org.freeplane.core.ui.components.JRestrictedSizeScrollPane;
import org.freeplane.core.ui.components.UITools;

@SuppressWarnings("serial")
public class NodeTooltip extends JToolTip {
	final private JToolTip tip; 
	public NodeTooltip(){
		tip  = new JToolTip();
		final JRestrictedSizeScrollPane scrollPane = new JRestrictedSizeScrollPane(tip);
		scrollPane.setMaximumSize(new Dimension(maximumWidth, maximumWidth));
		UITools.setScrollbarIncrement(scrollPane);
		add(scrollPane);
		
	}
	private static int maximumWidth = Integer.MAX_VALUE;
	/**
	 *  set maximum width
	 *  0 = no maximum width
	 */
	public static void setMaximumWidth(final int width) {
		maximumWidth = width;
	}

	@Override
    public void setTipText(String tipText) {
		tip.setTipText(tipText);
		Dimension preferredSize = tip.getPreferredSize();
		if (preferredSize.width < maximumWidth) {
			return ;
		}
		final String TABLE_START = "<html><table>";
		if (!tipText.startsWith(TABLE_START)) {
			return ;
		}
		tipText = "<html><table width=\"" + maximumWidth + "\">" + tipText.substring(TABLE_START.length());
		tip.setTipText(tipText);
    }

	@Override
    public Dimension getPreferredSize() {
	    return getComponent(0).getPreferredSize();
    }
	
	

	@Override
    public void layout() {
		getComponent(0).setSize(getPreferredSize());
	    super.layout();
    }

	@Override
    public void paintChildren(Graphics g) {
	    // TODO Auto-generated method stub
	    super.paintChildren(g);
    }	
	
	
}
