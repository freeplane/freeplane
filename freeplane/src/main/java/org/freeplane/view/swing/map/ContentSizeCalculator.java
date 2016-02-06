package org.freeplane.view.swing.map;

import java.awt.Dimension;

import javax.swing.JComponent;

public class ContentSizeCalculator {
	public static final Dimension ZERO = new Dimension(0, 0);
	public static ContentSizeCalculator INSTANCE = new ContentSizeCalculator();
	public  Dimension calculateContentSize(final NodeView view) {
		if(! view.isContentVisible())
			return ZERO;
    	final JComponent content = view.getContent();
        Dimension contentSize=  content.getPreferredSize();
        return contentSize;
    }

}
