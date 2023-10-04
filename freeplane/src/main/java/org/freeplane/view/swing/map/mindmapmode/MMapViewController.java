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
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.util.HashSet;
import java.util.function.Supplier;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import org.apache.commons.lang.StringUtils;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.styles.LogicalStyleController.StyleOption;
import org.freeplane.features.text.mindmapmode.EditNodeBase;
import org.freeplane.features.text.mindmapmode.EditNodeBase.EditedComponent;
import org.freeplane.features.text.mindmapmode.EditNodeBase.IEditControl;
import org.freeplane.features.text.mindmapmode.EditNodeWYSIWYG;
import org.freeplane.features.text.mindmapmode.IEditBaseCreator;
import org.freeplane.features.url.UrlManager;
import org.freeplane.features.url.mindmapmode.MFileManager;
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
	public EditNodeBase createEditor(final NodeModel node, Object nodeProperty,
                             Object content, final EditNodeBase.IEditControl editControl, final boolean editLong) {
	    String text;
		if(content instanceof String)
			text = (String) content;
		else
			throw new IllegalArgumentException("Unknown content type " + content);
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
			editNodeWYSIWYG.setPreferredContentSize(preferredSize);
			final MainView mainView = (MainView) getComponent(node);
	        final NodeView nodeView = mainView.getNodeView();
	        final ComponentOrientation componentOrientation = mainView.getComponentOrientation();
			if(EditedComponent.TEXT.equals(editControl.getEditType())){
	            final Font font = getFont(node);
	            editNodeWYSIWYG.setTitle("edit_long_node");
	            editNodeWYSIWYG.setFont(font);
	            final Color nodeTextColor = nodeView.getTextColor(StyleOption.FOR_UNSELECTED_NODE);
	            editNodeWYSIWYG.setTextColor(nodeTextColor);
				editNodeWYSIWYG.setBackground (nodeView.getTextBackground(StyleOption.FOR_UNSELECTED_NODE));
				editNodeWYSIWYG.setTextAlignment(mainView.getHorizontalAlignment());
				editNodeWYSIWYG.setComponentOrientation(componentOrientation);
				editNodeWYSIWYG.setCustomStyleSheet(mainView.getStyleSheet());
			}
			else if(EditedComponent.DETAIL.equals(editControl.getEditType())){
			    final MapView map = nodeView.getMap();
	            editNodeWYSIWYG.setTitle("edit_details");
                editNodeWYSIWYG.setFont(map.getDetailFont());
                editNodeWYSIWYG.setTextColor(map.getDetailForeground());
    			final Color detailBackground = map.getDetailBackground();
                editNodeWYSIWYG.setBackground (detailBackground != null ? detailBackground : nodeView.getTextBackground(StyleOption.FOR_UNSELECTED_NODE));
                editNodeWYSIWYG.setTextAlignment(map.getDetailHorizontalAlignment());
                editNodeWYSIWYG.setComponentOrientation(componentOrientation);
                editNodeWYSIWYG.setCustomStyleSheet(map.getDetailCss().getStyleSheet());
			}
			else if(EditedComponent.NOTE.equals(editControl.getEditType())){
			    final MapView map = nodeView.getMap();
	            editNodeWYSIWYG.setTitle("edit_note");
                editNodeWYSIWYG.setFont(map.getNoteFont());
                editNodeWYSIWYG.setTextColor(map.getNoteForeground());
                final Color noteBackground = map.getNoteBackground();
				editNodeWYSIWYG.setBackground (noteBackground != null ? noteBackground : map.getBackground());
                editNodeWYSIWYG.setTextAlignment(map.getNoteHorizontalAlignment());
                editNodeWYSIWYG.setComponentOrientation(componentOrientation);
                editNodeWYSIWYG.setCustomStyleSheet(map.getNoteCss().getStyleSheet());
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
			else
				return createEditor(node, nodeProperty, text, editControl, true);
		}
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
			textField.setBackground (nodeView.getTextBackground(StyleOption.FOR_UNSELECTED_NODE));
		else if(EditedComponent.DETAIL.equals(parent)) {
			final Color detailBackground = nodeView.getMap().getDetailBackground();
			textField.setBackground (detailBackground != null ? detailBackground : nodeView.getTextBackground(StyleOption.FOR_UNSELECTED_NODE));
		}
		return textField;
	}

	public MMapViewController(Controller controller) {
		super(controller);
		new EditNodeTextField(null, null, null, null);
    }

    @Override
    public JEditorPane createTextEditorPane(Supplier<JScrollPane> scrollPaneSupplier, NodeModel node, Object nodeProperty, Object content, boolean editInline) {
        return null;
    }


    public boolean saveAllModifiedMaps(){
        return saveAllModifiedMapsExcept(null);
    }

    public boolean saveAllModifiedMapsExcept(MapModel mapToKeepOpen) {
        MapModel currentMap = getMap();
        if(currentMap != null && currentMap != mapToKeepOpen && ! saveModifiedIfNotCancelled(currentMap))
            return false;
        HashSet<MapModel> otherMaps = new HashSet(getMaps().values());
        otherMaps.remove(mapToKeepOpen);
        otherMaps.remove(getMap());
        for (MapModel map : otherMaps){
            if(! saveModifiedIfNotCancelled(map))
                return false;
        }

        return true;
    }

    @Override
    public boolean saveModifiedIfNotCancelled(final MapModel map) {
        if (!(map.isSaved() || map.isReadOnly())) {
            changeToMap(map);
            final MapView mapView = getMapView();
            if(mapView.getMap() != map)
                return true;
            final String text = TextUtils.getText("save_unsaved") + "\n" + map.getTitle();
            final String title = TextUtils.getText("SaveAction.text");
            Component dialogParent;
            final Frame viewFrame = UITools.getCurrentFrame();
            if(viewFrame != null && viewFrame.isShowing() && viewFrame.getExtendedState() != Frame.ICONIFIED)
                dialogParent = viewFrame;
            else
                dialogParent = UITools.getCurrentRootComponent();
            final int returnVal = JOptionPane.showOptionDialog(dialogParent, text, title,
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
            if (returnVal == JOptionPane.YES_OPTION) {
                final UrlManager fileManager = mapView.getModeController()
                        .getExtension(UrlManager.class);
                final boolean savingNotCancelled = (fileManager instanceof MFileManager)
                        && ((MFileManager) fileManager).save(map);
                if (!savingNotCancelled) {
                    return false;
                }
            }
            else if ((returnVal == JOptionPane.CANCEL_OPTION) || (returnVal == JOptionPane.CLOSED_OPTION)) {
                return false;
            }
        }
        return true;
    }


}
