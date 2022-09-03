package org.freeplane.core.ui.components;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JScrollPane;
import javax.swing.JViewport;

@SuppressWarnings("serial")
public class JRestrictedSizeScrollPane extends JScrollPane {

	private static class SizeChanger extends ComponentAdapter {
        @Override
        public void componentResized(ComponentEvent e) {
            JViewport viewport = (JViewport) e.getComponent();
            Dimension extentSize = viewport.getExtentSize();
            Component view = viewport.getView();
            if(view != null && ! view.getSize().equals(extentSize)) {
                view.setSize(extentSize);
                viewport.revalidate();
            }
        }
    }

    public JRestrictedSizeScrollPane() {
	    super();
	    getViewport().addComponentListener(new SizeChanger());
    }

	public JRestrictedSizeScrollPane(Component view) {
	    super(view);
	    getViewport().addComponentListener(new SizeChanger());
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
