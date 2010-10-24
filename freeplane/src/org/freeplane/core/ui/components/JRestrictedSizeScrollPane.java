package org.freeplane.core.ui.components;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JScrollPane;

@SuppressWarnings("serial")
public class JRestrictedSizeScrollPane extends JScrollPane {

	public JRestrictedSizeScrollPane() {
	    super();
    }

	public JRestrictedSizeScrollPane(Component view, int vsbPolicy, int hsbPolicy) {
	    super(view, vsbPolicy, hsbPolicy);
    }

	public JRestrictedSizeScrollPane(Component view) {
	    super(view);
    }

	public JRestrictedSizeScrollPane(int vsbPolicy, int hsbPolicy) {
	    super(vsbPolicy, hsbPolicy);
    }

	@Override
    public Dimension getPreferredSize() {
		if(isPreferredSizeSet() || ! (isMaximumSizeSet() || isMinimumSizeSet())){
			return super.getPreferredSize();
		}
		final Dimension viewPreferredSize = getViewport().getView().getPreferredSize();
		final Dimension preferredSize = new Dimension(viewPreferredSize);
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
