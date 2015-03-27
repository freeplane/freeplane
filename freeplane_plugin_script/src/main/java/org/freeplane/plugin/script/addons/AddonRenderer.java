/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2011 dimitry
 *
 *  This file author is dimitry
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
package org.freeplane.plugin.script.addons;

import java.awt.Component;
import java.text.BreakIterator;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.freeplane.core.util.HtmlUtils;
import org.freeplane.main.addons.AddOnProperties;

/**
 * @author Dimitry Polivaev
 * Nov 17, 2011
 */
@SuppressWarnings("serial")
public class AddonRenderer extends DefaultTableCellRenderer {

	@Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
		if(value instanceof AddOnProperties){
			AddOnProperties addOn = (AddOnProperties)value;
			final String description = addOn.getDescription();
			final String shortDescription = HtmlUtils.toXMLEscapedText(shorten(HtmlUtils.htmlToPlain(description), 120));
			String text = "<html><body><b><font size='+1'>" + addOn.getTranslatedName() + " "
			        + addOn.getVersion().replaceAll("^v", "") + createAuthorText(addOn.getAuthor())  + "</font></b><br>"
			        + shortDescription + "</body></html>";
			value = text;
			setToolTipText(description);
		}
	    final Component tableCellRendererComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		return tableCellRendererComponent;
    }
	
	private String createAuthorText(String author) {
		if (author == null || author.length() == 0)
			return "";
	    return " " + ManageAddOnsDialog.getText("authored.by", author);
    }

    private String shorten(String string, int maxLength) {
        if (string.length() <= 3 || string.length() <= maxLength)
            return string;
        final BreakIterator bi = BreakIterator.getSentenceInstance();
        bi.setText(string);
        string = string.substring(0, bi.next());
        if (string.length() <= 3 || string.length() <= maxLength)
            return string + "...";
        return string.substring(0, maxLength - 3) + "...";
    }

}
