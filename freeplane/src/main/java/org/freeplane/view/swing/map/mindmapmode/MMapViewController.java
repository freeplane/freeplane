/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2009 Dimitry Polivaev
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
package org.freeplane.view.swing.map.mindmapmode;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JComponent;

import org.apache.commons.lang.StringUtils;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.text.mindmapmode.EditNodeBase;
import org.freeplane.features.text.mindmapmode.EditNodeBase.EditedComponent;
import org.freeplane.features.text.mindmapmode.EditNodeBase.IEditControl;
import org.freeplane.features.text.mindmapmode.EditNodeWYSIWYG;
import org.freeplane.features.text.mindmapmode.IEditBaseCreator;
import org.freeplane.view.swing.map.MainView;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.MapViewController;
import org.freeplane.view.swing.map.NodeView;
import org.freeplane.view.swing.map.ZoomableLabel;

/**
 * @author Dimitry Polivaev
 * Jan 31, 2009
 */
public class MMapViewController extends MapViewController implements IEditBaseCreator {
	@Override
	public EditNodeBase createEditor(final NodeModel node, final EditNodeBase.IEditControl editControl,
                             Object content, final boolean editLong) {
	    String text = (String) content;
	    final String htmlEditingOption = ResourceController.getResourceController().getProperty("html_editing_option");
		final boolean editInternalWysiwyg = editLong && StringUtils.equals(htmlEditingOption, "internal-wysiwyg");
		final boolean editExternal = editLong && StringUtils.equals(htmlEditingOption, "external");
		if(! HtmlUtils.isHtml(text)){
			text = HtmlUtils.plainToHTML(text);
		}
		if (editInternalWysiwyg) {
			final EditNodeWYSIWYG editNodeWYSIWYG = new EditNodeWYSIWYG(node, text, editControl, true);
			int preferredHeight = (int) (getComponent(node).getHeight() * 1.2);
			preferredHeight = Math.max(preferredHeight, Integer.parseInt(ResourceController.getResourceController()
					.getProperty("el__min_default_window_height")));
			preferredHeight = Math.min(preferredHeight, Integer.parseInt(ResourceController.getResourceController()
					.getProperty("el__max_default_window_height")));
			int preferredWidth = (int) (getComponent(node).getWidth() * 1.2);
			preferredWidth = Math.max(preferredWidth, Integer.parseInt(ResourceController.getResourceController()
					.getProperty("el__min_default_window_width")));
			preferredWidth = Math.min(preferredWidth, Integer.parseInt(ResourceController.getResourceController()
					.getProperty("el__max_default_window_width")));
			final Dimension preferredSize = new Dimension(preferredWidth, preferredHeight);
			editNodeWYSIWYG.setPreferredSize(preferredSize);
			final MainView mainView = (MainView) getComponent(node);
	        final NodeView nodeView = mainView.getNodeView();
			if(EditedComponent.TEXT.equals(editControl.getEditType())){
	            final Font font = getFont(node);
	            editNodeWYSIWYG.setTitle("edit_long_node");
	            editNodeWYSIWYG.setFont(font);
	            final Color nodeTextColor = getTextColor(node);
	            editNodeWYSIWYG.setTextColor(nodeTextColor);
				editNodeWYSIWYG.setBackground (nodeView.getTextBackground());
				editNodeWYSIWYG.setTextAlignment(mainView.getHorizontalAlignment());
			}
			else if(EditedComponent.DETAIL.equals(editControl.getEditType())){
			    final MapView map = nodeView.getMap();
	            editNodeWYSIWYG.setTitle("edit_details");
                editNodeWYSIWYG.setFont(map.getDetailFont());
                editNodeWYSIWYG.setTextColor(map.getDetailForeground());
    			final Color detailBackground = map.getDetailBackground();
                editNodeWYSIWYG.setBackground (detailBackground != null ? detailBackground : nodeView.getTextBackground());
                editNodeWYSIWYG.setTextAlignment(map.getDetailHorizontalAlignment());
			}
			else if(EditedComponent.NOTE.equals(editControl.getEditType())){
			    final MapView map = nodeView.getMap();
	            editNodeWYSIWYG.setTitle("edit_note");
                editNodeWYSIWYG.setFont(map.getNoteFont());
                editNodeWYSIWYG.setTextColor(map.getNoteForeground());
                final Color noteBackground = map.getNoteBackground();
				editNodeWYSIWYG.setBackground (noteBackground != null ? noteBackground : map.getBackground());
                editNodeWYSIWYG.setTextAlignment(map.getNoteHorizontalAlignment());
			}
			return editNodeWYSIWYG;
		}
		else if (editExternal) {
			return new EditNodeExternalApplication(node, text, editControl);
		}
		else {
			final EditNodeBase textfield = createEditor(node, editControl.getEditType(), text, editControl);
			if(textfield != null)
				return textfield;
		}
		return createEditor(node, editControl, text, true);
    }

	private EditNodeBase createEditor(final NodeModel node, final EditedComponent parent, final String text,
	                                                     final IEditControl editControl) {
		final ZoomableLabel parentComponent;
		final MainView mainView = (MainView) getComponent(node);
        final NodeView nodeView = mainView.getNodeView();
		if(EditedComponent.TEXT.equals(parent))
			parentComponent = mainView;
		else if(EditedComponent.DETAIL.equals(parent)) {
			final JComponent component = nodeView.getContent(NodeView.DETAIL_VIEWER_POSITION);
	        if(component instanceof ZoomableLabel)
	        	parentComponent = (ZoomableLabel) component;
	        else
	        	parentComponent = null;
        }
        else
			parentComponent = null;
		if(parentComponent == null || ! parentComponent.isVisible()){
			return null;
		}
		final EditNodeTextField textField = new EditNodeTextField(node, parentComponent, text, editControl);
		if(EditedComponent.TEXT.equals(parent))
			textField.setBackground (nodeView.getTextBackground());
		else if(EditedComponent.DETAIL.equals(parent)) {
			final Color detailBackground = nodeView.getMap().getDetailBackground();
			textField.setBackground (detailBackground != null ? detailBackground : nodeView.getTextBackground());
		}
		return textField;
	}

	public MMapViewController(Controller controller) {
		super(controller);
		new EditNodeTextField(null, null, null, null);
    }

}
