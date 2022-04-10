package org.freeplane.core.ui.components;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JScrollPane;

@SuppressWarnings("serial")
public class JRestrictedSizeScrollPane extends JScrollPane {

	private static final int MAX_HEIGHT = 100000;

	public JRestrictedSizeScrollPane() {
	    super();
    }

	public JRestrictedSizeScrollPane(Component view) {
        super(view);
        // TODO Auto-generated constructor stub
    }

    @Override
    public Dimension getPreferredSize() {
		final Dimension preferredSize = super.getPreferredSize();
		if(isPreferredSizeSet() || ! (isMaximumSizeSet() || isMinimumSizeSet())){
			return preferredSize;
		}
		if(isMinimumSizeSet()){
			final Dimension minimumSize = getMinimumSize();
			preferredSize.width = Math.max(minimumSize.width, preferredSize.width);
			preferredSize.height = Math.max(minimumSize.height, preferredSize.height);
		}
		if(isMaximumSizeSet()){
			final Dimension maximumSize = getMaximumSize();
			preferredSize.width = Math.min(maximumSize.width, preferredSize.width);
			preferredSize.height = Math.min(maximumSize.height, preferredSize.height);
		}
		return preferredSize;
    }

    @Override
    public void doLayout() {
    	fixRowHeaderSize();
    	super.doLayout();
    }

	private void fixRowHeaderSize() {
   		if(rowHeader == null)
			return;
		Component rowHeaderView = rowHeader.getView();
		if(rowHeaderView == null)
			return;
		int rowHeaderViewHeight = rowHeaderView.getHeight();
		if(rowHeaderViewHeight > MAX_HEIGHT)
			rowHeaderView.setSize(rowHeaderView.getWidth(), MAX_HEIGHT);
		if(!rowHeaderView.isPreferredSizeSet())
			return;
		Dimension preferredSize = rowHeaderView.getPreferredSize();
		if(preferredSize.height > MAX_HEIGHT) {
			rowHeaderView.setPreferredSize(new Dimension(preferredSize.width, MAX_HEIGHT));
		}
	}
}
