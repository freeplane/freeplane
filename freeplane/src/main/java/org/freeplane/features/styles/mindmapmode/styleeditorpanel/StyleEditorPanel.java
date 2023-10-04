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
package org.freeplane.features.styles.mindmapmode.styleeditorpanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

import javax.swing.Box;
import javax.swing.JPanel;

import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.components.SeparatorProperty;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.map.IMapSelectionListener;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.styles.mindmapmode.MUIFactory;
import org.freeplane.features.styles.mindmapmode.SelectedNodeChangeListener;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Paddings;
import com.jgoodies.forms.layout.ConstantSize;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;
import com.jgoodies.forms.util.LayoutStyle;

public class StyleEditorPanel extends JPanel {
	private static final String STYLE_EDITOR_PANEL_SIZE_PROPERTY = "styleEditorPanelSize";

	public enum StyleEditorPanelSize{SMALL(0.6f), MEDIUM(0.8f), BIG(1f);
		final float fontSize;
		final ConstantSize paragraphGapSize;
		private StyleEditorPanelSize(float scalingFactor) {
			this.fontSize = UITools.getUIFontSize(scalingFactor);
			this.paragraphGapSize = Sizes.pixel((int) (2.5 * scalingFactor * fontSize));
		}

	}

    private final class PanelEnabler implements IFreeplanePropertyListener, IMapSelectionListener {
		private final Controller controller;
		private final ModeController modeController;
		boolean canEdit = true;

		private PanelEnabler(Controller controller, ModeController modeController) {
			this.controller = controller;
			this.modeController = modeController;
		}

		@Override
		public void propertyChanged(String propertyName, String newValue, String oldValue) {
			if(propertyName.equals(ModeController.VIEW_MODE_PROPERTY))
				updatePanel();
		}

		@Override
		public void afterMapChange(final MapModel oldMap, final MapModel newMap) {
			if (modeController.equals(Controller.getCurrentModeController())) {
				updatePanel();
			}
		}

		private void updatePanel() {
			boolean canEditNow = modeController.canEdit(controller.getMap());
			if(canEdit != canEditNow) {
				canEdit = canEditNow;
				setComponentsEnabled(canEdit);
			}
		}
	}

	/**
	*
	*/
	private static final long serialVersionUID = 1L;

	private boolean internalChange;
	ControlGroup [] controlGroups;

	private PanelEnabler panelEnabler;

	private final StyleEditorPanelSize panelConfiguration;

	/**
	 * @throws HeadlessException
	 */
	public StyleEditorPanel(final ModeController modeController, final MUIFactory uiFactory,
	                        final boolean addStyleBox) throws HeadlessException {
		super();
		setLayout(new BorderLayout());
		ResourceController resourceController = ResourceController.getResourceController();
		if("MIDDLE".equals(resourceController.getProperty(STYLE_EDITOR_PANEL_SIZE_PROPERTY, null)))
			resourceController.setProperty(STYLE_EDITOR_PANEL_SIZE_PROPERTY, StyleEditorPanelSize.MEDIUM.name());
		panelConfiguration = resourceController.getEnumProperty(STYLE_EDITOR_PANEL_SIZE_PROPERTY, StyleEditorPanelSize.MEDIUM);
		controlGroups = createControlGroups(modeController, uiFactory, addStyleBox);
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

	private ControlGroup[] createControlGroups(ModeController modeController, MUIFactory uiFactory, boolean addStyleBox) {
		return new ControlGroup[]{
				new StyleControlGroup(addStyleBox, uiFactory, modeController, panelConfiguration.fontSize),

				new GroupSeparator("OptionPanel.separator.NodeColors"),
				new NodeColorControlGroup(),
				new NodeBackgroundColorControlGroup(),

                new GroupSeparator("OptionPanel.separator.NodeFont"),
                new FontNameControlGroup(),
                new FontSizeControlGroup(),
                new FontBoldControlGroup(),
                new FontStrikeThroughControlGroup(),
                new FontItalicControlGroup(),
                new NodeHorizontalTextAlignmentControlGroup(),
                new NodeTextWritingDirectionControlGroup(),
                new NodeFontHyperLinkControlGroup(),
                new NextLineControlGroup(),
                new CssControlGroup(modeController),

                new GroupSeparator("OptionPanel.separator.IconControls"),
                new IconSizeControlGroup(),

                new GroupSeparator("OptionPanel.separator.NodeText"),
                new FormatControlGroup(),
                new NodeNumberingControlGroup(),

				new GroupSeparator("OptionPanel.separator.ContentTypes"),
                new DetailContentTypeControlGroup(),
                new NoteContentTypeControlGroup(),

                new GroupSeparator("OptionPanel.separator.NodeShape"),
				new NodeShapeControlGroup(),
				new MinNodeWidthControlGroup(),
				new MaxNodeWidthControlGroup(),

				new GroupSeparator("OptionPanel.separator.NodeLayout"),
				new ChildNodesLayoutControlGroup(),
                new ChildHorizontalGapControlGroup(),
                new ChildVerticalGapControlGroup(),

				new GroupSeparator("OptionPanel.separator.NodeBorder"),
				new BorderWidthAndBorderWidthMatchesEdgeControlGroup(),
				new BorderDashAndDashMatchesEdgeControlGroup(),
				new BorderColorAndColorMatchesEdgeControlGroup(),
				new NextLineControlGroup(),

				new GroupSeparator("OptionPanel.separator.EdgeControls"),
				new EdgeWidthControlGroup(),
				new EdgeDashControlGroup(),
				new EdgeStyleControlGroup(),
				new EdgeColorControlGroup(),
				new NextLineControlGroup(),
				new GroupSeparator("OptionPanel.separator.CloudControls"),
				new CloudColorShapeControlGroup()
		};	}

	/**
	 * Creates all controls and adds them to the frame.
	 * @param modeController
	 */
	private void init() {
        final String colSpec = "right:max(20dlu;p), 4dlu, 40dlu:grow, 4dlu, max(10dlu;p)";
        final FormLayout rightLayout = new FormLayout(colSpec, "");
		final DefaultFormBuilder formBuilder = new DefaultFormBuilder(rightLayout);
		formBuilder.border(Paddings.DLU2);
		formBuilder.lineGapSize(LayoutStyle.getCurrent().getNarrowLinePad());
		formBuilder.paragraphGapSize(panelConfiguration.paragraphGapSize);
		new SeparatorProperty("OptionPanel.separator.NodeStyle").appendToForm(formBuilder);

		for (ControlGroup controlGroup :controlGroups) {
			controlGroup.addControlGroup(formBuilder);
		}
		formBuilder.getLayout().setHonorsVisibility(false);
		add(formBuilder.getPanel(), BorderLayout.CENTER);
		addListeners();
		setFont(this, panelConfiguration.fontSize);
	}



	@Override
	public Dimension getPreferredSize() {
		Dimension preferredSize = super.getPreferredSize();
		return new Dimension((int) (100*UITools.FONT_SCALE_FACTOR), preferredSize.height);

	}

	private void setFont(Container c, float size) {
		c.setFont(c.getFont().deriveFont(size));
		for(int i = 0; i < c.getComponentCount(); i++){
			setFont((Container) c.getComponent(i), size);
		}
    }

	private void setStyle( final NodeModel node) {
		if (internalChange) {
			return;
		}
		internalChange = true;
		try {
			for (int i=0; i<controlGroups.length; i++) {
				controlGroups[i].setStyle(node, panelEnabler != null && panelEnabler.canEdit);
			}

		}
		finally {
			internalChange = false;
		}
	}

	private void updatePanel(NodeModel selected) {
	    if (selected == null) {
	        setComponentsEnabled(false);
	    }
	    else {
	        if(Controller.getCurrentModeController().canEdit(selected.getMap()))
	            setComponentsEnabled(true);
	        setStyle(selected);
	    }
	}

	private void setComponentsEnabled(boolean enabled) {
		final Container panel = (Container) getComponent(0);
		if(panel.isEnabled() != enabled)
		    setComponentsEnabled(panel, enabled);
	}

	private void setComponentsEnabled(final Container container, boolean enabled) {
	    container.setEnabled(enabled);
	    if(container instanceof Box || container instanceof JPanel)
	        for (int i = 0; i < container.getComponentCount(); i++) {
	            Component component = container.getComponent(i);
	            if(container instanceof Container)
	                setComponentsEnabled((Container) component, enabled);
	        }
	}

	private void addListeners() {
	    SelectedNodeChangeListener.onSelectedNodeChange(this::updatePanel);

		final ModeController modeController = Controller.getCurrentModeController();
		Controller controller = modeController.getController();

        panelEnabler = new PanelEnabler(controller, modeController);
        controller.getMapViewManager().addMapSelectionListener(panelEnabler);
        ResourceController.getResourceController().addPropertyChangeListener(panelEnabler);
	}

}