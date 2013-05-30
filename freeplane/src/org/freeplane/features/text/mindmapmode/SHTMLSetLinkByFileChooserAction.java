/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.features.text.mindmapmode;

import java.awt.event.ActionEvent;
import java.net.URI;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.url.UrlManager;
import org.freeplane.features.url.mindmapmode.MFileManager;

import com.lightdev.app.shtm.SHTMLAction;
import com.lightdev.app.shtm.SHTMLEditorPane;
import com.lightdev.app.shtm.SHTMLPanelImpl;

class SHTMLSetLinkByFileChooserAction extends AFreeplaneAction implements SHTMLAction {
    /**
    *
    */
   private final SHTMLPanelImpl panel;
	private static final long serialVersionUID = 1L;

	public SHTMLSetLinkByFileChooserAction(final SHTMLPanelImpl panel) {
	       super("SetLinkByFileChooserAction");
	       this.panel = panel;
	}

	public void actionPerformed(final ActionEvent e) {
		setLinkByFileChooser();
	}

	public void setLinkByFileChooser() {
		final URI relative = ((MFileManager) UrlManager.getController())
		    .getLinkByFileChooser(Controller.getCurrentController().getMap());
		if (relative != null) {
			SHTMLEditorPane editor = panel.getSHTMLEditorPane();
			editor.setLink(null, relative.toString(), null);
		}
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

	public void getProperties() {
		// TODO Auto-generated method stub
		
	}
}
