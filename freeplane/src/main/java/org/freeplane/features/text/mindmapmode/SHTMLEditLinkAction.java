package org.freeplane.features.text.mindmapmode;

import java.awt.event.ActionEvent;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.html.HTML;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.mode.Controller;
import com.lightdev.app.shtm.SHTMLEditorPane;
import com.lightdev.app.shtm.SHTMLPanelImpl;
import com.lightdev.app.shtm.SHTMLAction;


public class SHTMLEditLinkAction extends AFreeplaneAction implements SHTMLAction {
    /**
    *
    */
   private final SHTMLPanelImpl panel;

   public SHTMLEditLinkAction(final SHTMLPanelImpl panel) {
       super("SetLinkByTextFieldAction");
       this.panel = panel;
       SHTMLPanelImpl.getActionProperties(this, "setLinkByTextField");
   }

   public void actionPerformed(final ActionEvent ae) {
        SHTMLEditorPane editorPane = panel.getSHTMLEditorPane();
		final Element linkElement = editorPane.getCurrentLinkElement();
        final boolean foundLink = (linkElement != null);
		final String linkAsString;
        if (foundLink) {
            final AttributeSet elemAttrs = linkElement.getAttributes();
            final Object linkAttr = elemAttrs.getAttribute(HTML.Tag.A);
            final Object href = ((AttributeSet) linkAttr).getAttribute(HTML.Attribute.HREF);
            if (href != null) {
            	linkAsString = href.toString();
            }
            else
            	linkAsString = "http://";
        }
        else {
            linkAsString = "http://";
        }
		final String inputValue = UITools.showInputDialog(
		    Controller.getCurrentController().getSelection().getSelected(), TextUtils.getText("edit_link_manually"), linkAsString);
		if (inputValue != null && ! inputValue.matches("\\w+://")) {
			SHTMLEditorPane editor = panel.getSHTMLEditorPane();
			if (inputValue.equals("")) {
				editor.setLink(null, null, null);
				return;
			}
			try {
				final URI link = LinkController.createURI(inputValue.trim());
				editor.setLink(null, link.toString(), null);
			}
			catch (final URISyntaxException e1) {
				LogUtils.warn(e1);
				UITools.errorMessage(TextUtils.format("invalid_uri", inputValue));
				return;
			}
		}
       panel.updateActions();
   }

   public void update() {
       if (panel.isHtmlEditorActive()) {
           this.setEnabled(false);
           return;
       }
       if (panel.getSHTMLEditorPane() != null) {
           if ((panel.getSHTMLEditorPane().getSelectionEnd() > panel.getSHTMLEditorPane().getSelectionStart())
                   || (panel.getSHTMLEditorPane().getCurrentLinkElement() != null)) {
               this.setEnabled(true);
           }
           else {
               this.setEnabled(false);
           }
       }
       else {
           this.setEnabled(false);
       }
   }
}
