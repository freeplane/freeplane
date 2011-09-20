package org.freeplane.view.swing.map;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JToolTip;
import javax.swing.SwingUtilities;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.StyleSheet;

import org.freeplane.core.ui.components.JRestrictedSizeScrollPane;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.url.UrlManager;

@SuppressWarnings("serial")
public class NodeTooltip extends JToolTip {
	final private JEditorPane tip; 
	public NodeTooltip(){
		tip  = new JEditorPane();
		tip.setContentType("text/html");
		tip.setEditable(false);
		tip.setMargin(new Insets(0, 0, 0, 0));
		tip.addMouseListener(new MouseAdapter() {
			public void mouseClicked(final MouseEvent ev) {
				if ((ev.getModifiers() & MouseEvent.CTRL_MASK) != 0) {
					final String linkURL = HtmlUtils.getURLOfExistingLink((HTMLDocument) tip.getDocument(), tip.viewToModel(ev.getPoint()));
					if (linkURL != null) {
						try {
							UrlManager.getController().loadURL(new URI(linkURL));
						} catch (Exception e) {
							LogUtils.warn(e);
						}
					}
				}
			}
		});
		final HTMLDocument document = (HTMLDocument) tip.getDocument();
		final StyleSheet styleSheet = document.getStyleSheet();
		styleSheet.removeStyle("p");
		styleSheet.removeStyle("body");
		styleSheet.addRule("p {margin-top:0;}\n");

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
        final HTMLDocument document = (HTMLDocument) tip.getDocument();
        document.getStyleSheet().addRule("body { width: " + maximumWidth  + "}");
        // bad hack: call "setEditable" only to update view
        tip.setEditable(true);
        tip.setEditable(false);
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
