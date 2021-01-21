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
package org.freeplane.features.link.mindmapmode.editor;

import java.awt.BorderLayout;
import java.awt.HeadlessException;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.link.ConnectorModel;
import org.freeplane.features.link.NodeLinks;
import org.freeplane.features.link.mindmapmode.MLinkController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.styles.IStyle;
import org.freeplane.features.styles.MapStyleModel;
import org.freeplane.features.styles.StyleString;
import org.freeplane.features.styles.mindmapmode.ComboBoxRendererWithTooltip;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Paddings;
import com.jgoodies.forms.layout.FormLayout;

public class ConnectorEditorPanel extends JPanel {

	static final float FONT_SIZE = UITools.getUIFontSize(0.8);

	/**
	*
	*/
	private static final long serialVersionUID = 1L;

	ControlGroup [] controlGroups;

    private ConnectorModel connector;

    private MLinkController linkController;

	/**
	 * @throws HeadlessException
	 */
	public ConnectorEditorPanel(MLinkController linkController, ConnectorModel connector) throws HeadlessException {
		super();
        this.linkController = linkController;
        this.connector = connector;
		controlGroups = createControlGroups();
		addHierarchyListener(new HierarchyListener() {

			@Override
			public void hierarchyChanged(HierarchyEvent e) {
				if(isDisplayable()){
					removeHierarchyListener(this);
					init();
				}
			}
		});
	}

	private ControlGroup[] createControlGroups() {
		return new ControlGroup[]{
		        new ConnectorColorControlGroup(),
		        new OpacityControlGroup(),
		        new ConnectorArrowsControlGroup(),
		        new ConnectorShapeControlGroup(),
		        new ConnectorDashControlGroup(),
		        new ConnectorWidthControlGroup(),
				new FontNameControlGroup(),
				new FontSizeControlGroup(),
		};	}

	/**
	 * Creates all controls and adds them to the frame.
	 * @param modeController
	 */
	private void init() {
		final String form = "right:max(20dlu;p), 2dlu, p, 1dlu,right:max(20dlu;p), 4dlu, 80dlu, 7dlu";
		final FormLayout rightLayout = new FormLayout(form, "");
		final DefaultFormBuilder formBuilder = new DefaultFormBuilder(rightLayout);
		formBuilder.border(Paddings.DLU2);
        if(! MapStyleModel.isStyleNode(connector.getSource())) {
            final JComboBox<IStyle> stylesBox = createStyleSelector(connector);
            formBuilder.append(formBuilder.getComponentFactory().createLabel(TextUtils.getText("style")), 3);
            formBuilder.append(stylesBox, 3);
            formBuilder.nextLine();
        }

		for (ControlGroup controlGroup :controlGroups) {
			controlGroup.addControlGroup(formBuilder);
		}
		add(formBuilder.getPanel(), BorderLayout.CENTER);
        updateValues();
	}

    public void updateValues() {
        for (ControlGroup controlGroup :controlGroups) {
            controlGroup.updateValue(connector);
        }
    }
    
    private JComboBox<IStyle> createStyleSelector(ConnectorModel link) {
        MapModel map = link.getSource().getMap();
        MapStyleModel styleMap = MapStyleModel.getExtension(map);
        IStyle[] styles = styleMap.getStyles().stream().filter(key -> 
         NodeLinks.getSelfConnector(styleMap.getStyleNode(key)).isPresent())
        .toArray(IStyle[]::new);
        final JComboBox<IStyle> stylesBox = new JComboBox(styles);
        stylesBox.setSelectedItem(link.getStyle());
        stylesBox.setPrototypeDisplayValue(new StyleString("XXXXXXXXXXXXXXXXXXXXXXXX"));
        stylesBox.setRenderer(new ComboBoxRendererWithTooltip(stylesBox));
        stylesBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        stylesBox.addItemListener(item -> {
            linkController.setConnectorStyle(link, (IStyle)stylesBox.getSelectedItem());
            updateValues();
        });
        return stylesBox;
    }

}