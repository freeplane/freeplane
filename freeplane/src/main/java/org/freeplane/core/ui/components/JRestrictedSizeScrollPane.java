package org.freeplane.core.ui.components;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JScrollPane;

@SuppressWarnings("serial")
public class JRestrictedSizeScrollPane extends JScrollPane {

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
	
}
