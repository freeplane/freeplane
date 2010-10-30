package org.freeplane.core.ui.components;

import java.awt.Component;
import java.awt.Dimension;

@SuppressWarnings("serial")
public class JRestrictedSizeScrollPane extends JAutoScrollBarPane {

	public JRestrictedSizeScrollPane(Component view) {
	    super(view);
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
		if(getVerticalScrollBar().isVisible()){
			preferredSize.width += getVerticalScrollBar().getPreferredSize().width;
		}
		if(getHorizontalScrollBar().isVisible()){
			preferredSize.height += getHorizontalScrollBar().getPreferredSize().height;
		}
		return preferredSize;
    }
	
}
