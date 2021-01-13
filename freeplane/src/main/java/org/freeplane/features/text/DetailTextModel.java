/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
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
package org.freeplane.features.text;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.map.NodeModel;

/**
 * @author Dimitry Polivaev
 */
public class DetailTextModel extends RichTextModel implements IExtension {
	public static final String EDITING_PURPOSE = "DetailText";
    public static DetailTextModel createDetailText(final NodeModel node) {
        DetailTextModel details = DetailTextModel.getDetailText(node);
        if (details == null) {
            details = new DetailTextModel(false);
            node.addExtension(details);
        }
        return details;
    }

    public static DetailTextModel getDetailText(final NodeModel node) {
        final DetailTextModel extension = (DetailTextModel) node.getExtension(DetailTextModel.class);
        return extension;
    }

    public static String getDetailTextText(final NodeModel node) {
        final DetailTextModel extension = DetailTextModel.getDetailText(node);
        return extension != null ? extension.getText() : null;
    }

    public static String getDetailContentType(final NodeModel node) {
        final DetailTextModel extension = DetailTextModel.getDetailText(node);
        return extension != null ? extension.getContentType() : null;
    }

    public static String getXmlDetailTextText(final NodeModel node) {
        final DetailTextModel extension = DetailTextModel.getDetailText(node);
        return extension != null ? extension.getText() : null;
    }

	private boolean hidden = false;
	private String localizedHtmlPropertyName;
	public DetailTextModel(boolean hidden) {
	    this.hidden = hidden;
    }
	
	public DetailTextModel(String contentType, String text, String xml, boolean hidden, String localizedHtmlPropertyName) {
        super(contentType, text, xml);
        this.hidden = hidden;
        this.localizedHtmlPropertyName = localizedHtmlPropertyName;
    }
	
	public DetailTextModel copy() {
	    return new DetailTextModel(getContentType(), getText(), getXml(), hidden, localizedHtmlPropertyName);
	}


    public boolean isHidden() {
    	return hidden;
    }

	public void setHidden(boolean hidden) {
    	this.hidden = hidden;
    }
	
	public void setLocalizedHtmlPropertyName(String localizedHtmlPropertyName) {
		this.localizedHtmlPropertyName = localizedHtmlPropertyName;
	}
	
	public String getLocalizedHtmlPropertyName() {
		return localizedHtmlPropertyName;
	}

    public boolean isEmpty() {
        return ! hidden  && localizedHtmlPropertyName == null && super.isEmpty();
    }


}
