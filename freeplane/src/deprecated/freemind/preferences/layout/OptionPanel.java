/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is to be reworked.
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
package deprecated.freemind.preferences.layout;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.freeplane.controller.Controller;
import org.freeplane.controller.resources.ResourceController;
import org.freeplane.controller.views.ViewController;
import org.freeplane.main.FreemindStarter;
import org.freeplane.map.icon.IIconInformation;
import org.freeplane.map.icon.MindIcon;
import org.freeplane.map.icon.mindmapnode.MIconController;
import org.freeplane.map.nodestyle.NodeStyleModel;
import org.freeplane.modes.ModeController;
import org.freeplane.modes.mindmapmode.MModeController;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import deprecated.freemind.common.BooleanProperty;
import deprecated.freemind.common.ColorProperty;
import deprecated.freemind.common.ComboProperty;
import deprecated.freemind.common.DontShowNotificationProperty;
import deprecated.freemind.common.IPropertyControl;
import deprecated.freemind.common.NextLineProperty;
import deprecated.freemind.common.NumberProperty;
import deprecated.freemind.common.PropertyBean;
import deprecated.freemind.common.RemindValueProperty;
import deprecated.freemind.common.SeparatorProperty;
import deprecated.freemind.common.StringProperty;

import deprecated.freemind.preferences.IFreemindPropertyContributor;

/**
 * refactoring of class OptionPanel from package freemind.preferences.layout .
 * This class builds tabs of the preferences dialog responsible for comfortable
 * editing of FreeMind (Freeplane) options. The bad thing about it is that
 * currently every control is programmed there. So for adding new options to the
 * program one have to extend implementation of this class. The good thing is
 * that this class is quite loosely coupled to the others and can be re-factored
 * independent from the rest. For me it was the absolutely last step in the
 * whole refactoring, but it would be a great help if you had enough time to do
 * this job. Currently I have some ideas about how to change it. Basically I
 * think it should build the dialog interpreting known Properties or other
 * Hashtable. I have already implemented some classes which could be helpful for
 * it. Use properties files Use many files for different purposes Use UIBuilder
 * like construct Also see MenuBuilder Position of new entry could be somehow
 * established not sure how yet Iterate over "sets" of properties at abstract
 * level MenuBuilder. public void processMenuCategory(final MenuStructure menu)
 * { this could be used many times to iterate over it
 */
// candidates for singleton: MModeController
// last 500 or so lines is just assigning values by hard coding
public class OptionPanel {
	final private class ChangeTabAction implements ActionListener {
		final private CardLayout cardLayout;
		final private JPanel centralPanel;
		final private String tabName;

		private ChangeTabAction(final CardLayout cardLayout,
		                        final JPanel centralPanel, final String tabName) {
			super();
			this.cardLayout = cardLayout;
			this.centralPanel = centralPanel;
			this.tabName = tabName;
		}

		public void actionPerformed(final ActionEvent arg0) {
			cardLayout.show(centralPanel, tabName);
			final Collection c = getAllButtons();
			for (final Iterator i = c.iterator(); i.hasNext();) {
				final JButton button = (JButton) i.next();
				button.setForeground(null);
			}
			getTabButton(tabName)
			    .setForeground(OptionPanel.MARKED_BUTTON_COLOR);
			selectedPanel = tabName;
		}
	}

	public interface IOptionPanelFeedback {
		void writeProperties(Properties props);
	}

	private static class KeyProperty extends PropertyBean implements
	        IPropertyControl {
		private static RowSpec rowSpec;
		String description;
		private ImageIcon icon;
		String label;
		private String labelText;
		JButton mButton = new JButton();
		private int modifierMask = 0;

		/**
		 */
		public KeyProperty(final JDialog dialog, final String description,
		                   final String label) {
			super();
			this.description = description;
			this.label = label;
			mButton.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent arg0) {
					final Vector allKeybindings = new Vector();
					final GrabKeyDialog keyDialog = new GrabKeyDialog(dialog,
					    new GrabKeyDialog.KeyBinding(getLabel(), getLabel(),
					        getValue(), false), allKeybindings, null,
					    modifierMask);
					if (keyDialog.isOK()) {
						setValue(keyDialog.getShortcut());
						firePropertyChangeEvent();
					}
				}
			});
		}

		public void disableModifiers() {
			modifierMask = KeyEvent.ALT_MASK | KeyEvent.CTRL_MASK;
		}

		public String getDescription() {
			return description;
		}

		public String getLabel() {
			return label;
		}

		public String getValue() {
			return mButton.getText();
		}

		public void layout(final DefaultFormBuilder builder) {
			if (labelText == null) {
				labelText = OptionString.getText(getLabel());
			}
			final JLabel label = new JLabel(labelText, icon, JLabel.RIGHT);
			label.setToolTipText(OptionString.getText(getDescription()));
			if (KeyProperty.rowSpec == null) {
				KeyProperty.rowSpec = new RowSpec("fill:20dlu");
			}
			if (3 < builder.getColumn()) {
				builder.appendRelatedComponentsGapRow();
				builder.appendRow(KeyProperty.rowSpec);
				builder.nextLine(2);
			}
			else {
				builder.nextColumn(2);
			}
			builder.add(label);
			builder.nextColumn(2);
			builder.add(mButton);
		}

		public void setEnabled(final boolean pEnabled) {
			mButton.setEnabled(pEnabled);
		}

		public void setImageIcon(final ImageIcon icon) {
			this.icon = icon;
		}

		public void setLabelText(final String labelText) {
			this.labelText = labelText;
		}

		public void setValue(final String value) {
			mButton.setText(value);
			mButton.setToolTipText(mButton.getText());
		}
	}

	public static class NewTabProperty implements IPropertyControl {
		final private String label;
		final private String layoutFormat;

		public NewTabProperty(final String label) {
			this(label, OptionPanel.DEFAULT_LAYOUT_FORMAT);
		}

		public NewTabProperty(final String label, final String layoutFormat) {
			super();
			this.label = label;
			this.layoutFormat = layoutFormat;
		}

		public String getDescription() {
			return layoutFormat;
		}

		public String getLabel() {
			return label;
		}

		public void layout(final DefaultFormBuilder builder) {
		}

		public void setEnabled(final boolean pEnabled) {
		}
	}

	public static Vector changeListeners = new Vector();
	private static final String DEFAULT_LAYOUT_FORMAT = "right:max(40dlu;p), 4dlu, 120dlu, 7dlu";
	private static final Color MARKED_BUTTON_COLOR = Color.BLUE;
	private static final String PREFERENCE_STORAGE_PROPERTY = "OptionPanel_Window_Properties";
	private static Set sContributors = new HashSet();

	public static void addContributor(
	                                  final IFreemindPropertyContributor contributor) {
		OptionPanel.sContributors.add(contributor);
	}

	public static void removeContributor(
	                                     final IFreemindPropertyContributor contributor) {
		OptionPanel.sContributors.remove(contributor);
	}

	private Vector controls;
	final private IOptionPanelFeedback feedback;
	private String selectedPanel = null;
	final private HashMap tabActionMap = new HashMap();
	final private HashMap tabButtonMap = new HashMap();
	final private JDialog topDialog;

	/**
	 * @throws IOException
	 */
	public OptionPanel(final JDialog d, final IOptionPanelFeedback feedback) {
		super();
		topDialog = d;
		this.feedback = feedback;
		final String marshalled = Controller.getResourceController().getProperty(OptionPanel.PREFERENCE_STORAGE_PROPERTY);
        final OptionPanelWindowConfigurationStorage storage = OptionPanelWindowConfigurationStorage.decorateDialog(marshalled,d);
		if (storage != null) {
			final OptionPanelWindowConfigurationStorage oWindowSettings = (OptionPanelWindowConfigurationStorage) storage;
			selectedPanel = oWindowSettings.getPanel();
		}
	}

	public void buildPanel() {
		final FormLayout leftLayout = new FormLayout("80dlu", "");
		final DefaultFormBuilder leftBuilder = new DefaultFormBuilder(
		    leftLayout);
		final CardLayout cardLayout = new VariableSizeCardLayout();
		final JPanel rightStack = new JPanel(cardLayout);
		FormLayout rightLayout = null;
		DefaultFormBuilder rightBuilder = null;
		String lastTabName = null;
		controls = getControls();
		for (final Iterator i = controls.iterator(); i.hasNext();) {
			final IPropertyControl control = (IPropertyControl) i.next();
			if (control instanceof NewTabProperty) {
				final NewTabProperty newTab = (NewTabProperty) control;
				if (rightBuilder != null) {
					rightStack.add(rightBuilder.getPanel(), lastTabName);
				}
				rightLayout = new FormLayout(newTab.getDescription(), "");
				rightBuilder = new DefaultFormBuilder(rightLayout);
				rightBuilder.setDefaultDialogBorder();
				lastTabName = newTab.getLabel();
				final JButton tabButton = new JButton(OptionString
				    .getText(lastTabName));
				final ChangeTabAction changeTabAction = new ChangeTabAction(
				    cardLayout, rightStack, lastTabName);
				tabButton.addActionListener(changeTabAction);
				registerTabButton(tabButton, lastTabName, changeTabAction);
				leftBuilder.append(tabButton);
			}
			else {
				control.layout(rightBuilder);
			}
		}
		rightStack.add(rightBuilder.getPanel(), lastTabName);
		if (selectedPanel != null && tabActionMap.containsKey(selectedPanel)) {
			((ChangeTabAction) tabActionMap.get(selectedPanel))
			    .actionPerformed(null);
		}
		final JSplitPane centralPanel = new JSplitPane(
		    JSplitPane.HORIZONTAL_SPLIT, leftBuilder.getPanel(),
		    new JScrollPane(rightStack));
		topDialog.getContentPane().add(centralPanel, BorderLayout.CENTER);
		final JButton cancelButton = new JButton(OptionString.getText("Cancel"));
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				closeWindow();
			}
		});
		final JButton okButton = new JButton(OptionString.getText("OK"));
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				feedback.writeProperties(getOptionProperties());
				closeWindow();
			}
		});
		topDialog.getRootPane().setDefaultButton(okButton);
		topDialog.getContentPane().add(
		    ButtonBarFactory.buildOKCancelBar(cancelButton, okButton),
		    BorderLayout.SOUTH);
	}

	public void closeWindow() {
		final OptionPanelWindowConfigurationStorage storage = new OptionPanelWindowConfigurationStorage();
		storage.setPanel(selectedPanel);
		storage.storeDialogPositions(topDialog,OptionPanel.PREFERENCE_STORAGE_PROPERTY);
		topDialog.setVisible(false);
		topDialog.dispose();
	}

	private Collection getAllButtons() {
		return tabButtonMap.values();
	}

	private Vector getControls() {
		final Vector controls = new Vector();
		/***********************************************************************
		 * Language
		 * ****************************************************************
		 */
		controls.add(new NewTabProperty("Environment"));
		controls.add(new SeparatorProperty("language"));
		controls.add(new ComboProperty(
		/**
		 * For the codes see
		 * http://www.loc.gov/standards/iso639-2/php/English_list.php
		 */
		"language.tooltip", ResourceController.RESOURCE_LANGUAGE, new String[] {
		        "automatic", "ar", "cs", "de", "dk", "en", "el", "es", "et",
		        "fr", "gl", "hr", "hu", "id", "it", "ja", "kr", "lt", "nl",
		        "nn", "nb", "pl", "pt_BR", "pt_PT", "ru", "sk", "se", "sl",
		        "tr", "uk_UA", "vi", "zh_TW", "zh_CN" }));
		controls.add(new NextLineProperty());
		controls.add(new SeparatorProperty("files"));
		controls.add(new StringProperty(null, "last_opened_list_length"));
		controls.add(new BooleanProperty("load_last_map" + ".tooltip",
		    FreemindStarter.LOAD_LAST_MAP));
		controls.add(new BooleanProperty(
		    "experimental_file_locking_on.tooltip",
		    "experimental_file_locking_on"));
		controls.add(new NextLineProperty());
		controls.add(new StringProperty(null, "userproperties"));
		controls.add(new StringProperty(null, "patternsfile"));
		controls.add(new StringProperty("browsemode_initial_map.tooltip",
		    "browsemode_initial_map"));
		controls.add(new NextLineProperty());
		controls.add(new SeparatorProperty("automatic_save"));
		controls.add(new StringProperty("time_for_automatic_save.tooltip",
		    "time_for_automatic_save"));
		controls.add(new BooleanProperty(
		    "delete_automatic_saves_at_exit.tooltip",
		    "delete_automatic_saves_at_exit"));
		controls.add(new StringProperty(
		    "number_of_different_files_for_automatic_save.tooltip",
		    "number_of_different_files_for_automatic_save"));
		controls.add(new StringProperty("path_to_automatic_saves.tooltip",
		    "path_to_automatic_saves"));
		controls.add(new NextLineProperty());
		controls.add(new SeparatorProperty("save"));
		controls.add(new BooleanProperty(
		    "resources_save_folding_state.tooltip",
		    ResourceController.RESOURCES_SAVE_FOLDING_STATE));
		controls.add(new BooleanProperty(
		    "save_only_intrisically_needed_ids.tooltip",
		    "save_only_intrisically_needed_ids"));
		/***********************************************************************
		 * Defaults
		 * ****************************************************************
		 */
		controls.add(new NewTabProperty("Defaults"));
		controls.add(new SeparatorProperty("default_styles"));
		controls
		    .add(new ComboProperty("standardnodeshape.tooltip",
		        ResourceController.RESOURCES_NODE_SHAPE,
		        NodeStyleModel.NODE_STYLES));
		controls.add(new ComboProperty("standardrootnodeshape.tooltip",
		    ResourceController.RESOURCES_ROOT_NODE_SHAPE, new String[] {
		            NodeStyleModel.STYLE_FORK, NodeStyleModel.STYLE_BUBBLE,
		            NodeStyleModel.SHAPE_COMBINED }));
		controls.add(new NextLineProperty());
		controls.add(new SeparatorProperty("default_colors"));
		controls.add(new ColorProperty("standardnodetextcolor.tooltip",
		    ResourceController.RESOURCES_NODE_TEXT_COLOR,
		    getDefaultProperty(ResourceController.RESOURCES_NODE_TEXT_COLOR)));
		controls.add(new ColorProperty("standardedgecolor.tooltip",
		    ResourceController.RESOURCES_EDGE_COLOR,
		    getDefaultProperty(ResourceController.RESOURCES_EDGE_COLOR)));
		controls.add(new ColorProperty("standardlinkcolor.tooltip",
		    ResourceController.RESOURCES_LINK_COLOR,
		    getDefaultProperty(ResourceController.RESOURCES_LINK_COLOR)));
		controls.add(new ColorProperty("standardbackgroundcolor.tooltip",
		    ResourceController.RESOURCES_BACKGROUND_COLOR,
		    getDefaultProperty(ResourceController.RESOURCES_BACKGROUND_COLOR)));
		controls.add(new BooleanProperty("printonwhitebackground" + ".tooltip",
		    "printonwhitebackground"));
		controls.add(new ColorProperty("standardcloudcolor.tooltip",
		    ResourceController.RESOURCES_CLOUD_COLOR,
		    getDefaultProperty(ResourceController.RESOURCES_CLOUD_COLOR)));
		controls.add(new NextLineProperty());
		controls.add(new SeparatorProperty("default_fonts"));
		controls.add(new StringProperty("defaultfont.tooltip", "defaultfont"));
		controls.add(new StringProperty(null, "defaultfontstyle"));
		controls.add(new StringProperty(null, "defaultfontsize"));
		controls.add(new NumberProperty("max_node_width.tooltip",
		    "max_node_width", 1, Integer.MAX_VALUE, 1));
		controls.add(new NumberProperty("max_tooltip_width.tooltip",
		    "max_tooltip_width", 1, Integer.MAX_VALUE, 1));
		controls.add(new NextLineProperty());
		controls.add(new SeparatorProperty("other_defaults"));
		controls.add(new ComboProperty("standardedgestyle.tooltip",
		    ResourceController.RESOURCES_EDGE_STYLE, new String[] { "bezier",
		            "linear" }));
		/***********************************************************************
		 * Appearance
		 * ****************************************************************
		 */
		controls.add(new NewTabProperty("Appearance"));
		controls.add(new SeparatorProperty("look_and_feel"));
		final LookAndFeelInfo[] lafInfo = UIManager.getInstalledLookAndFeels();
		final String[] lafNames = new String[lafInfo.length + 5];
		final Vector translatedLafNames = new Vector();
		lafNames[0] = "default";
		translatedLafNames.add(OptionString.getText("default"));
		lafNames[1] = "metal";
		translatedLafNames.add(OptionString.getText("metal"));
		lafNames[2] = "windows";
		translatedLafNames.add(OptionString.getText("windows"));
		lafNames[3] = "motif";
		translatedLafNames.add(OptionString.getText("motif"));
		lafNames[4] = "gtk";
		translatedLafNames.add(OptionString.getText("gtk"));
		lafNames[5] = "nothing";
		translatedLafNames.add(OptionString.getText("nothing"));
		for (int i = 0; i < lafInfo.length; i++) {
			final LookAndFeelInfo info = lafInfo[i];
			final String className = info.getClassName();
			lafNames[i + 5] = className;
			translatedLafNames.add(info.getName());
		}
		controls.add(new ComboProperty("lookandfeel.tooltip", "lookandfeel",
		    lafNames, translatedLafNames));
		controls.add(new BooleanProperty("use_tabbed_pane.tooltip",
		    ResourceController.RESOURCES_USE_TABBED_PANE));
		/* ***************************************************************** */
		controls.add(new NextLineProperty());
		controls.add(new SeparatorProperty("selection_colors"));
		controls.add(new BooleanProperty(
		    ResourceController.RESOURCE_DRAW_RECTANGLE_FOR_SELECTION
		            + ".tooltip",
		    ResourceController.RESOURCE_DRAW_RECTANGLE_FOR_SELECTION));
		controls.add(new ColorProperty(
		    "standardselectednoderectanglecolor.tooltip",
		    ResourceController.RESOURCES_SELECTED_NODE_RECTANGLE_COLOR,
		    Controller.getResourceController().getDefaultProperty(
		        ResourceController.RESOURCES_SELECTED_NODE_RECTANGLE_COLOR)));
		controls
		    .add(new ColorProperty(
		        "standardselectednodecolor.tooltip",
		        ResourceController.RESOURCES_SELECTED_NODE_COLOR,
		        getDefaultProperty(ResourceController.RESOURCES_SELECTED_NODE_COLOR)));
		/* ***************************************************************** */
		controls.add(new NextLineProperty());
		final String RESOURCE_ROOT_NODE = "root_node_appearance";
		final String RESOURCE_USE_COMMON_OUT_POINT_FOR_ROOT_NODE = "use_common_out_point_for_root_node";
		controls.add(new SeparatorProperty(RESOURCE_ROOT_NODE));
		controls.add(new BooleanProperty(
		    RESOURCE_USE_COMMON_OUT_POINT_FOR_ROOT_NODE + ".tooltip",
		    RESOURCE_USE_COMMON_OUT_POINT_FOR_ROOT_NODE));
		/* ***************************************************************** */
		controls.add(new NextLineProperty());
		controls.add(new SeparatorProperty("anti_alias"));
		controls.add(new ComboProperty("antialias.tooltip",
		    ViewController.RESOURCE_ANTIALIAS, new String[] {
		            "antialias_edges", "antialias_all", "antialias_none" }));
		/* ***************************************************************** */
		controls.add(new NextLineProperty());
		controls.add(new SeparatorProperty("initial_map_size"));
		controls.add(new StringProperty("mapxsize.tooltip", "mapxsize"));
		controls.add(new StringProperty(null, "mapysize"));
		/* ***************************************************************** */
		controls.add(new NextLineProperty());
		controls.add(new SeparatorProperty("hyperlink_types"));
		controls.add(new ComboProperty("links.tooltip", "links", new String[] {
		        "relative", "absolute" }));
		/* ***************************************************************** */
		controls.add(new NextLineProperty());
		controls.add(new SeparatorProperty("edit_long_node_window"));
		controls.add(new StringProperty("el__buttons_position.tooltip",
		    "el__buttons_position"));
		controls
		    .add(new BooleanProperty(null, "el__position_window_below_node"));
		controls.add(new StringProperty(null, "el__min_default_window_height"));
		controls.add(new StringProperty(null, "el__max_default_window_height"));
		controls.add(new StringProperty(null, "el__min_default_window_width"));
		controls.add(new StringProperty(null, "el__max_default_window_width"));
		controls
		    .add(new BooleanProperty(null, "el__enter_confirms_by_default"));
		controls.add(new BooleanProperty(null, "el__show_icon_for_attributes"));
		controls.add(new SeparatorProperty("icon_properties"));
		controls.add(new BooleanProperty(null,
		    ResourceController.RESOURCES_DON_T_SHOW_NOTE_ICONS));
		controls.add(new StringProperty("icon_order_description",
		    MindIcon.PROPERTY_STRING_ICONS_LIST));
		/***********************************************************************
		 * Keystrokes
		 * ****************************************************************
		 */
		final String form = "right:max(40dlu;p), 4dlu, 80dlu, 7dlu";
		controls.add(new NewTabProperty("Keystrokes", form + "," + form));
		controls.add(new SeparatorProperty("commands_for_the_program"));
		controls.add(new KeyProperty(topDialog, null, "keystroke_newMap"));
		controls.add(new KeyProperty(topDialog, null, "keystroke_open"));
		controls.add(new KeyProperty(topDialog, null, "keystroke_save"));
		controls.add(new KeyProperty(topDialog, null, "keystroke_saveAs"));
		controls.add(new KeyProperty(topDialog, null, "keystroke_print"));
		controls.add(new KeyProperty(topDialog, null, "keystroke_close"));
		controls.add(new KeyProperty(topDialog, null, "keystroke_quit"));
		controls
		    .add(new KeyProperty(topDialog, null, "keystroke_option_dialog"));
		controls.add(new KeyProperty(topDialog, null,
		    "keystroke_export_to_html"));
		controls.add(new KeyProperty(topDialog, null,
		    "keystroke_export_branch_to_html"));
		controls.add(new KeyProperty(topDialog, null,
		    "keystroke_open_first_in_history"));
		controls.add(new KeyProperty(topDialog, null, "keystroke_previousMap"));
		controls.add(new KeyProperty(topDialog, null, "keystroke_nextMap"));
		controls
		    .add(new KeyProperty(topDialog, null, "keystroke_mode_MindMap"));
		controls.add(new KeyProperty(topDialog, null, "keystroke_mode_Browse"));
		controls.add(new KeyProperty(topDialog, null, "keystroke_mode_File"));
		controls.add(new KeyProperty(topDialog, null,
		    "keystroke_node_toggle_italic"));
		controls.add(new KeyProperty(topDialog, null,
		    "keystroke_node_toggle_boldface"));
		controls.add(new KeyProperty(topDialog, null,
		    "keystroke_node_toggle_underlined"));
		controls.add(new KeyProperty(topDialog, null,
		    "keystroke_node_toggle_cloud"));
		controls.add(new KeyProperty(topDialog, null, "keystroke_undo"));
		controls.add(new KeyProperty(topDialog, null, "keystroke_redo"));
		controls
		    .add(new KeyProperty(topDialog, null, "keystroke_delete_child"));
		controls.add(new KeyProperty(topDialog, null, "keystroke_select_all"));
		controls
		    .add(new KeyProperty(topDialog, null, "keystroke_select_branch"));
		controls.add(new KeyProperty(topDialog, null, "keystroke_zoom_out"));
		controls.add(new KeyProperty(topDialog, null, "keystroke_zoom_in"));
		controls.add(new NextLineProperty());
		controls.add(new SeparatorProperty("node_editing_commands"));
		controls.add(new KeyProperty(topDialog, null, "keystroke_cut"));
		controls.add(new KeyProperty(topDialog, null, "keystroke_copy"));
		controls.add(new KeyProperty(topDialog, null, "keystroke_copy_single"));
		controls.add(new KeyProperty(topDialog, null, "keystroke_paste"));
		controls.add(new KeyProperty(topDialog, null, "keystroke_remove"));
		controls.add(new KeyProperty(topDialog, null,
		    "keystroke_add_arrow_link_action"));
		controls.add(new KeyProperty(topDialog, null,
		    "keystroke_add_local_link_action"));
		controls.add(new NextLineProperty());
		controls.add(new SeparatorProperty("node_navigation_commands"));
		controls.add(new KeyProperty(topDialog, null, "keystroke_moveToRoot"));
		controls.add(new KeyProperty(topDialog, null, "keystroke_move_up"));
		controls.add(new KeyProperty(topDialog, null, "keystroke_move_down"));
		controls.add(new KeyProperty(topDialog, null, "keystroke_move_left"));
		controls.add(new KeyProperty(topDialog, null, "keystroke_move_right"));
		controls.add(new KeyProperty(topDialog, null, "keystroke_follow_link"));
		controls.add(new NextLineProperty());
		controls.add(new SeparatorProperty("new_node_commands"));
		controls.add(new KeyProperty(topDialog, null, "keystroke_add"));
		controls.add(new KeyProperty(topDialog, null, "keystroke_add_child"));
		controls
		    .add(new KeyProperty(topDialog, null, "keystroke_add_child_mac"));
		controls.add(new KeyProperty(topDialog, null,
		    "keystroke_add_sibling_before"));
		controls.add(new NextLineProperty());
		controls.add(new SeparatorProperty("node_editing_commands"));
		controls.add(new KeyProperty(topDialog, null, "keystroke_edit"));
		controls.add(new KeyProperty(topDialog, null,
		    "keystroke_edit_long_node"));
		controls.add(new KeyProperty(topDialog, null, "keystroke_join_nodes"));
		controls
		    .add(new KeyProperty(topDialog, null, "keystroke_toggle_folded"));
		controls.add(new KeyProperty(topDialog, null,
		    "keystroke_toggle_children_folded"));
		controls.add(new KeyProperty(topDialog, null,
		    "keystroke_set_link_by_filechooser"));
		controls.add(new KeyProperty(topDialog, null,
		    "keystroke_set_link_by_textfield"));
		controls.add(new KeyProperty(topDialog, null,
		    "keystroke_set_image_by_filechooser"));
		controls.add(new KeyProperty(topDialog, null, "keystroke_node_up"));
		controls.add(new KeyProperty(topDialog, null, "keystroke_node_down"));
		controls.add(new KeyProperty(topDialog, null,
		    "keystroke_node_increase_font_size"));
		controls.add(new KeyProperty(topDialog, null,
		    "keystroke_node_decrease_font_size"));
		controls
		    .add(new KeyProperty(topDialog, null, "keystroke_export_branch"));
		controls.add(new KeyProperty(topDialog, null, "keystroke_node_color"));
		controls.add(new KeyProperty(topDialog, null,
		    "keystroke_node_color_blend"));
		controls.add(new KeyProperty(topDialog, null, "keystroke_edge_color"));
		controls.add(new KeyProperty(topDialog, null, "keystroke_find"));
		controls.add(new KeyProperty(topDialog, null, "keystroke_find_next"));
		controls.add(new NextLineProperty());
		controls.add(new SeparatorProperty("patterns"));
		controls
		    .add(new KeyProperty(topDialog, null,
		        "keystroke_accessories/plugins/ManagePatterns_manage_patterns_dialog"));
		controls.add(new KeyProperty(topDialog, null,
		    "keystroke_apply_pattern_1"));
		controls.add(new KeyProperty(topDialog, null,
		    "keystroke_apply_pattern_2"));
		controls.add(new KeyProperty(topDialog, null,
		    "keystroke_apply_pattern_3"));
		controls.add(new KeyProperty(topDialog, null,
		    "keystroke_apply_pattern_4"));
		controls.add(new KeyProperty(topDialog, null,
		    "keystroke_apply_pattern_5"));
		controls.add(new KeyProperty(topDialog, null,
		    "keystroke_apply_pattern_6"));
		controls.add(new KeyProperty(topDialog, null,
		    "keystroke_apply_pattern_7"));
		controls.add(new KeyProperty(topDialog, null,
		    "keystroke_apply_pattern_8"));
		controls.add(new KeyProperty(topDialog, null,
		    "keystroke_apply_pattern_9"));
		controls.add(new KeyProperty(topDialog, null,
		    "keystroke_apply_pattern_10"));
		controls.add(new KeyProperty(topDialog, null,
		    "keystroke_apply_pattern_11"));
		controls.add(new KeyProperty(topDialog, null,
		    "keystroke_apply_pattern_12"));
		controls.add(new KeyProperty(topDialog, null,
		    "keystroke_apply_pattern_13"));
		controls.add(new KeyProperty(topDialog, null,
		    "keystroke_apply_pattern_14"));
		controls.add(new KeyProperty(topDialog, null,
		    "keystroke_apply_pattern_15"));
		controls.add(new KeyProperty(topDialog, null,
		    "keystroke_apply_pattern_16"));
		controls.add(new KeyProperty(topDialog, null,
		    "keystroke_apply_pattern_17"));
		controls.add(new KeyProperty(topDialog, null,
		    "keystroke_apply_pattern_18"));
		controls.add(new NextLineProperty());
		controls.add(new SeparatorProperty("others"));
		controls
		    .add(new KeyProperty(topDialog, null,
		        "keystroke_accessories/plugins/ChangeNodeLevelAction_left.properties_key"));
		controls
		    .add(new KeyProperty(topDialog, null,
		        "keystroke_accessories/plugins/ChangeNodeLevelAction_right.properties_key"));
		controls
		    .add(new KeyProperty(topDialog, null,
		        "keystroke_accessories/plugins/FormatCopy.properties.properties_key"));
		controls
		    .add(new KeyProperty(topDialog, null,
		        "keystroke_accessories/plugins/FormatPaste.properties.properties_key"));
		controls
		    .add(new KeyProperty(topDialog, null,
		        "keystroke_accessories/plugins/IconSelectionPlugin.properties.properties_key"));
		controls.add(new KeyProperty(topDialog, null,
		    "keystroke_accessories/plugins/NewParentNode.properties_key"));
		controls.add(new KeyProperty(topDialog, null,
		    "keystroke_accessories/plugins/NodeNote_jumpto.keystroke.alt_N"));
		controls
		    .add(new KeyProperty(topDialog, null,
		        "keystroke_accessories/plugins/NodeNote_hide_show.keystroke.control_shift_less"));
		controls
		    .add(new KeyProperty(topDialog, null,
		        "keystroke_accessories/plugins/RemoveNote.properties.properties_key"));
		controls.add(new KeyProperty(topDialog, null,
		    "keystroke_accessories/plugins/UnfoldAll.keystroke.alt_PAGE_UP"));
		controls.add(new KeyProperty(topDialog, null,
		    "keystroke_accessories/plugins/UnfoldAll.keystroke.alt_PAGE_DOWN"));
		controls.add(new KeyProperty(topDialog, null,
		    "keystroke_accessories/plugins/UnfoldAll.keystroke.alt_HOME"));
		controls.add(new KeyProperty(topDialog, null,
		    "keystroke_accessories/plugins/UnfoldAll.keystroke.alt_END"));
		controls.add(new NextLineProperty());
		controls.add(new SeparatorProperty("attributes"));
		controls.add(new KeyProperty(topDialog, null,
		    "keystroke_edit_attributes"));
		controls.add(new KeyProperty(topDialog, null,
		    "keystroke_show_all_attributes"));
		controls.add(new KeyProperty(topDialog, null,
		    "keystroke_show_selected_attributes"));
		controls.add(new KeyProperty(topDialog, null,
		    "keystroke_hide_all_attributes"));
		controls.add(new KeyProperty(topDialog, null,
		    "keystroke_show_attribute_manager"));
		controls.add(new KeyProperty(topDialog, null,
		    "keystroke_assign_attributes"));
		controls.add(new KeyProperty(topDialog, null,
		    "keystroke_plugins/ScriptingEngine.keystroke.evaluate"));
		Controller.getController();
		final ModeController modeController = Controller
		    .getModeController();
		if (modeController instanceof MModeController) {
			final MModeController controller = (MModeController) modeController;
			final Collection<Action> iconActions = ((MIconController) controller
			    .getIconController()).getIconActions();
			final Vector actions = new Vector();
			actions.addAll(iconActions);
			actions.add(modeController.getAction("removeLastIconAction"));
			actions.add(modeController.getAction("removeAllIconsAction"));
			controls.add(new NextLineProperty());
			controls.add(new SeparatorProperty("icons"));
			final Iterator iterator = actions.iterator();
			while (iterator.hasNext()) {
				final IIconInformation info = (IIconInformation) iterator
				    .next();
				final KeyProperty keyProperty = new KeyProperty(topDialog,
				    null, info.getKeystrokeResourceName());
				keyProperty.setLabelText(info.getDescription());
				keyProperty.setImageIcon(info.getIcon());
				keyProperty.disableModifiers();
				controls.add(keyProperty);
			}
		}
		/***********************************************************************
		 * Misc ****************************************************************
		 */
		controls.add(new NewTabProperty("Behaviour"));
		controls.add(new SeparatorProperty("behaviour"));
		controls.add(new ComboProperty("placenewbranches.tooltip",
		    "placenewbranches", new String[] { "first", "last" }));
		controls.add(new BooleanProperty("draganddrop.tooltip", "draganddrop"));
		controls.add(new BooleanProperty("unfold_on_paste.tooltip",
		    "unfold_on_paste"));
		controls.add(new BooleanProperty("disable_cursor_move_paper.tooltip",
		    "disable_cursor_move_paper"));
		controls.add(new BooleanProperty("enable_leaves_folding.tooltip",
		    "enable_leaves_folding"));
		controls.add(new StringProperty("foldingsymbolwidth.tooltip",
		    "foldingsymbolwidth"));
		controls.add(new NextLineProperty());
		controls.add(new SeparatorProperty("key_typing"));
		controls.add(new BooleanProperty("disable_key_type.tooltip",
		    "disable_key_type"));
		controls.add(new BooleanProperty("key_type_adds_new.tooltip",
		    "key_type_adds_new"));
		controls.add(new NextLineProperty());
		controls.add(new SeparatorProperty("resources_notifications"));
		controls
		    .add(new RemindValueProperty(
		        "remind_type_of_new_nodes.tooltip",
		        ResourceController.RESOURCES_REMIND_USE_RICH_TEXT_IN_NEW_LONG_NODES));
		controls.add(new DontShowNotificationProperty(
		    "resources_convert_to_current_version.tooltip",
		    ResourceController.RESOURCES_CONVERT_TO_CURRENT_VERSION));
		controls.add(new DontShowNotificationProperty(
		    "delete_nodes_without_question.tooltip",
		    ResourceController.RESOURCES_DELETE_NODES_WITHOUT_QUESTION));
		controls.add(new DontShowNotificationProperty(
		    "cut_nodes_without_question.tooltip",
		    ResourceController.RESOURCES_CUT_NODES_WITHOUT_QUESTION));
		controls.add(new DontShowNotificationProperty(
		    "remove_notes_without_question.tooltip",
		    ResourceController.RESOURCES_REMOVE_NOTES_WITHOUT_QUESTION));
		controls.add(new DontShowNotificationProperty(
		    "execute_scripts_without_asking.tooltip",
		    ResourceController.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_ASKING));
		controls.add(new NextLineProperty());
		controls.add(new SeparatorProperty(
		    ResourceController.RESOURCES_SELECTION_METHOD));
		controls.add(new ComboProperty("selection_method.tooltip",
		    ResourceController.RESOURCES_SELECTION_METHOD, new String[] {
		            "selection_method_direct", "selection_method_delayed",
		            "selection_method_by_click" }));
		controls.add(new NumberProperty("time_for_delayed_selection.tooltip",
		    "time_for_delayed_selection", 1, Integer.MAX_VALUE, 1));
		controls.add(new NextLineProperty());
		controls.add(new SeparatorProperty("mouse_wheel"));
		controls.add(new NumberProperty("wheel_velocity.tooltip",
		    ResourceController.RESOURCES_WHEEL_VELOCITY, 1, 250, 1));
		controls.add(new NextLineProperty());
		controls.add(new SeparatorProperty("undo"));
		controls.add(new NumberProperty("undo_levels.tooltip", "undo_levels",
		    2, 1000, 1));
		/***********************************************************************
		 * Browser/external apps
		 * ****************************************************************
		 */
		controls.add(new NewTabProperty("HTML"));
		controls.add(new SeparatorProperty("browser"));
		controls.add(new StringProperty(
		    "default_browser_command_windows_nt.tooltip",
		    "default_browser_command_windows_nt"));
		controls.add(new StringProperty(
		    "default_browser_command_windows_9x.tooltip",
		    "default_browser_command_windows_9x"));
		controls.add(new StringProperty(
		    "default_browser_command_other_os.tooltip",
		    "default_browser_command_other_os"));
		controls.add(new StringProperty("default_browser_command_mac.tooltip",
		    "default_browser_command_mac"));
		controls.add(new SeparatorProperty("html_export"));
		controls.add(new ComboProperty(null, "html_export_folding",
		    new String[] { "html_export_no_folding",
		            "html_export_fold_currently_folded",
		            "html_export_fold_all", "html_export_based_on_headings" }));
		controls.add(new NextLineProperty());
		controls.add(new BooleanProperty("export_icons_in_html.tooltip",
		    "export_icons_in_html"));
		for (final Iterator iter = OptionPanel.sContributors.iterator(); iter
		    .hasNext();) {
			final IFreemindPropertyContributor contributor = (IFreemindPropertyContributor) iter
			    .next();
			controls.addAll(contributor.getControls());
		}
		return controls;
	}

	/**
	 * @param key
	 * @return
	 */
	private String getDefaultProperty(final String key) {
		return Controller.getResourceController().getDefaultProperty(key);
	}

	private Properties getOptionProperties() {
		final Properties p = new Properties();
		for (final Iterator i = controls.iterator(); i.hasNext();) {
			final IPropertyControl control = (IPropertyControl) i.next();
			if (control instanceof PropertyBean) {
				final PropertyBean bean = (PropertyBean) control;
				final String value = bean.getValue();
				if (value != null) {
					p.setProperty(bean.getLabel(), value);
				}
			}
		}
		return p;
	}

	private JButton getTabButton(final String name) {
		return (JButton) tabButtonMap.get(name);
	}

	private void registerTabButton(final JButton tabButton, final String name,
	                               final ChangeTabAction changeTabAction) {
		tabButtonMap.put(name, tabButton);
		tabActionMap.put(name, changeTabAction);
		if (selectedPanel == null) {
			selectedPanel = name;
		}
	}

	public void setProperties() {
		for (final Iterator i = controls.iterator(); i.hasNext();) {
			final IPropertyControl control = (IPropertyControl) i.next();
			if (control instanceof PropertyBean) {
				final PropertyBean bean = (PropertyBean) control;
				final String label = bean.getLabel();
				final String value = Controller.getResourceController()
				    .getAdjustableProperty(label);
				bean.setValue(value);
			}
		}
	}
}
