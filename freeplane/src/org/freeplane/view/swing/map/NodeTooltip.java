package org.freeplane.view.swing.map;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JToolTip;
import javax.swing.SwingUtilities;

import org.freeplane.core.ui.components.JRestrictedSizeScrollPane;
import org.freeplane.core.ui.components.UITools;

@SuppressWarnings("serial")
public class NodeTooltip extends JToolTip {
	final private JEditorPane tip; 
	public NodeTooltip(){
		tip  = new JEditorPane();
		tip.setContentType("text/html");
		tip.setEditable(false);
		final JRestrictedSizeScrollPane scrollPane = new JRestrictedSizeScrollPane(tip);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, maximumWidth / 2));
		scrollPane.addComponentListener(new ComponentAdapter() {
			@Override
            public void componentResized(ComponentEvent e) {
	            revalidate();
            }
		});
		UITools.setScrollbarIncrement(scrollPane);
		add(scrollPane);
		tip.setOpaque(false);
//		scrollPane.setOpaque(false);
//		scrollPane.getViewport().setOpaque(false);
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
		tip.setText(tipText);
		Dimension preferredSize = tip.getPreferredSize();
		if (preferredSize.width < maximumWidth) {
			return ;
		}
		final String TABLE_START = "<html><table>";
		if (!tipText.startsWith(TABLE_START)) {
			return ;
		}
		tipText = "<html><table width=\"" + maximumWidth + "\">" + tipText.substring(TABLE_START.length());
		tip.setText(tipText);
    }

	@Override
    public Dimension getPreferredSize() {
	    return getComponent(0).getPreferredSize();
    }

	@Override
    public void layout() {
		Window window = SwingUtilities.windowForComponent(this);
		if(! window.getFocusableWindowState()){
			window.setFocusableWindowState(true);
		}
		getComponent(0).setSize(getPreferredSize());
	    super.layout();
    }

	void scrollUp() {
		tip.scrollRectToVisible(new Rectangle(1, 1));
    }

}
