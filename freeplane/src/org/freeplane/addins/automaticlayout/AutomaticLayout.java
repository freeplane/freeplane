/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file author is Christian Foltin
 *  It is modified by Dimitry Polivaev in 2008.
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
package org.freeplane.addins.automaticlayout;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.freeplane.addins.NodeHookDescriptor;
import org.freeplane.addins.PersistentNodeHook;
import org.freeplane.controller.ActionDescriptor;
import org.freeplane.controller.Controller;
import org.freeplane.extension.IExtension;
import org.freeplane.io.IReadCompletionListener;
import org.freeplane.map.pattern.mindmapnode.StylePatternFactory;
import org.freeplane.map.tree.NodeModel;
import org.freeplane.map.tree.mindmapmode.IMapChangeListener;
import org.freeplane.modes.INodeChangeListener;
import org.freeplane.modes.NodeChangeEvent;
import org.freeplane.modes.mindmapmode.MModeController;

import accessories.plugins.dialogs.ChooseFormatPopupDialog;

import com.jgoodies.forms.builder.DefaultFormBuilder;

import deprecated.freemind.common.IPropertyControl;
import deprecated.freemind.common.ITextTranslator;
import deprecated.freemind.common.PropertyBean;
import deprecated.freemind.common.SeparatorProperty;
import deprecated.freemind.common.XmlBindingTools;
import deprecated.freemind.preferences.IFreemindPropertyContributor;
import deprecated.freemind.preferences.IFreemindPropertyListener;
import deprecated.freemind.preferences.layout.OptionPanel;
import deprecated.freemind.preferences.layout.OptionString;
import freemind.controller.actions.generated.instance.Pattern;
import freemind.controller.actions.generated.instance.Patterns;

@NodeHookDescriptor(hookName = "accessories/plugins/AutomaticLayout.properties")
@ActionDescriptor(locations = "/menu_bar/extras/first/nodes/change", //
name = "accessories/plugins/AutomaticLayout.properties_name", //
tooltip = "accessories/plugins/AutomaticLayout.properties_documentation")
public class AutomaticLayout extends PersistentNodeHook implements
        IMapChangeListener, INodeChangeListener, IReadCompletionListener {
	private static final class AutomaticLayoutPropertyContributor implements
	        IFreemindPropertyContributor {
		final private MModeController modeController;

		public AutomaticLayoutPropertyContributor(
		                                          final MModeController modeController) {
			this.modeController = modeController;
		}

		public List getControls() {
			final Vector controls = new Vector();
			controls
			    .add(new OptionPanel.NewTabProperty(
			        "accessories/plugins/AutomaticLayout.properties_PatternTabName"));
			controls
			    .add(new SeparatorProperty(
			        "accessories/plugins/AutomaticLayout.properties_PatternSeparatorName"));
			controls.add(new StylePatternListProperty("level",
			    AutomaticLayout.AUTOMATIC_FORMAT_LEVEL, modeController));
			return controls;
		}
	}

	/**
	 * Registers the property pages.
	 *
	 * @author foltin
	 */
	static class MyFreemindPropertyListener implements
	        IFreemindPropertyListener {
		public void propertyChanged(final String propertyName,
		                            final String newValue, final String oldValue) {
			if (propertyName.startsWith(AutomaticLayout.AUTOMATIC_FORMAT_LEVEL)) {
				AutomaticLayout.patterns = null;
			}
		}
	}

	public static class StylePatternListProperty extends PropertyBean implements
	        IPropertyControl, ListSelectionListener {
		String description;
		String label;
		final private DefaultListModel mDefaultListModel;
		boolean mDialogIsShown = false;
		final private MModeController mindMapController;
		JList mList;
		String patterns;

		public StylePatternListProperty(final String description,
		                                final String label,
		                                final MModeController pController) {
			super();
			this.description = description;
			this.label = label;
			mindMapController = pController;
			mList = new JList();
			mList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			mDefaultListModel = new DefaultListModel();
			mList.setModel(mDefaultListModel);
			mList.addListSelectionListener(this);
			patterns = null;
		}

		public String getDescription() {
			return description;
		}

		@Override
		public String getLabel() {
			return label;
		}

		private Patterns getPatternsFromString() {
			return StylePatternFactory.getPatternsFromString(patterns);
		}

		@Override
		public String getValue() {
			return patterns;
		}

		public void layout(final DefaultFormBuilder builder) {
			final JLabel label = builder.append(OptionString
			    .getText(getLabel()));
			builder.append(new JLabel());
			label.setToolTipText(OptionString.getText(getDescription()));
			builder.appendSeparator();
			builder.append(new JScrollPane(mList), 3);
		}

		public void setEnabled(final boolean pEnabled) {
			mList.setEnabled(pEnabled);
		}

		@Override
		public void setValue(final String value) {
			patterns = value;
			final Patterns resultPatterns = getPatternsFromString();
			mDefaultListModel.clear();
			int j = 1;
			final StylePropertyTranslator stylePropertyTranslator = new StylePropertyTranslator(
			    mindMapController);
			for (final Iterator i = resultPatterns.getListChoiceList()
			    .iterator(); i.hasNext();) {
				final Pattern pattern = (Pattern) i.next();
				mDefaultListModel.addElement(OptionString.getText("level" + j)
				        + ": "
				        + StylePatternFactory.toString(pattern,
				            stylePropertyTranslator));
				j++;
			}
		}

		public void valueChanged(final ListSelectionEvent e) {
			final Patterns pat = getPatternsFromString();
			final JList source = (JList) e.getSource();
			if (source.getSelectedIndex() < 0) {
				return;
			}
			final Pattern choice = (Pattern) pat.getChoice(source
			    .getSelectedIndex());
			final ChooseFormatPopupDialog formatDialog = new ChooseFormatPopupDialog(
			    Controller.getController().getViewController().getJFrame(),
			    mindMapController,
			    "accessories/plugins/AutomaticLayout.properties_StyleDialogTitle",
			    choice);
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					if (mDialogIsShown) {
						return;
					}
					mDialogIsShown = true;
					try {
						formatDialog.setModal(true);
						formatDialog.setVisible(true);
						if (formatDialog.getResult() == ChooseFormatPopupDialog.OK) {
							formatDialog.getPattern(choice);
							patterns = XmlBindingTools.getInstance().marshall(
							    pat);
							setValue(patterns);
							firePropertyChangeEvent();
						}
					}
					finally {
						mDialogIsShown = false;
					}
				}
			});
		}
	}

	/**
	 * Currently not used. Is useful if you want to make single patterns
	 * changeable.
	 */
	public static class StylePatternProperty extends PropertyBean implements
	        IPropertyControl, ActionListener {
		String description;
		String label;
		JButton mButton;
		final private MModeController mindMapController;
		String pattern;

		public StylePatternProperty(final String description,
		                            final String label,
		                            final ITextTranslator pTranslator,
		                            final MModeController pController) {
			super();
			this.description = description;
			this.label = label;
			mindMapController = pController;
			mButton = new JButton();
			mButton.addActionListener(this);
			pattern = null;
		}

		public void actionPerformed(final ActionEvent arg0) {
			final Pattern pat = getPatternFromString();
			final ChooseFormatPopupDialog formatDialog = new ChooseFormatPopupDialog(
			    Controller.getController().getViewController().getJFrame(),
			    mindMapController,
			    "accessories/plugins/AutomaticLayout.properties_StyleDialogTitle",
			    pat);
			formatDialog.setModal(true);
			formatDialog.setVisible(true);
			if (formatDialog.getResult() == ChooseFormatPopupDialog.OK) {
				final Pattern resultPattern = formatDialog.getPattern();
				resultPattern.setName("dummy");
				pattern = XmlBindingTools.getInstance().marshall(resultPattern);
				setValue(pattern);
				firePropertyChangeEvent();
			}
		}

		public String getDescription() {
			return description;
		}

		@Override
		public String getLabel() {
			return label;
		}

		private Pattern getPatternFromString() {
			return StylePatternFactory.getPatternFromString(pattern);
		}

		@Override
		public String getValue() {
			return pattern;
		}

		public void layout(final DefaultFormBuilder builder) {
			final JLabel label = builder.append(OptionString
			    .getText(getLabel()), mButton);
			label.setToolTipText(OptionString.getText(getDescription()));
		}

		public void setEnabled(final boolean pEnabled) {
			mButton.setEnabled(pEnabled);
		}

		@Override
		public void setValue(final String value) {
			pattern = value;
			final Pattern resultPattern = getPatternFromString();
			final String patternString = StylePatternFactory.toString(
			    resultPattern, new StylePropertyTranslator(mindMapController));
			mButton.setText(patternString);
			mButton.setToolTipText(patternString);
		}
	}

	/**
	 * Translates style pattern properties into strings.
	 */
	static class StylePropertyTranslator implements ITextTranslator {
		final private MModeController controller;

		StylePropertyTranslator(final MModeController controller) {
			super();
			this.controller = controller;
		}

		public String getText(final String pKey) {
			return controller.getText(pKey);
		}
	}

	private static final String AUTOMATIC_FORMAT_LEVEL = "automaticFormat_level";
	private static Patterns patterns = null;
	private final AutomaticLayoutPropertyContributor mAutomaticLayoutPropertyContributor;

	/**
	 *
	 */
	public AutomaticLayout(final MModeController modeController) {
		super(modeController);
		final MyFreemindPropertyListener listener = new MyFreemindPropertyListener();
		Controller.getResourceController().addPropertyChangeListener(listener);
		mAutomaticLayoutPropertyContributor = new AutomaticLayoutPropertyContributor(
		    ((MModeController) getModeController()));
		OptionPanel.addContributor(mAutomaticLayoutPropertyContributor);
		modeController.getMapController().getReadManager()
		    .addReadCompletionListener(this);
	}

	/*
	 * (non-Javadoc)
	 * @see freemind.extensions.NodeHook#invoke(freemind.modes.MindMapNode)
	 */
	@Override
	protected void add(final NodeModel node, final IExtension extension) {
		super.add(node, extension);
		getModeController().getMapController().addMapChangeListener(this);
		setStyleRecursive(node);
	}

	private int depth(final NodeModel node) {
		if (node.isRoot()) {
			return 0;
		}
		return depth(node.getParentNode()) + 1;
	}

	public void nodeChanged(final NodeChangeEvent event) {
		setStyle(event.getNode());
	}

	public void onNodeDeleted(final NodeModel parent, final NodeModel child) {
	}

	public void onNodeInserted(final NodeModel parent, final NodeModel child,
	                           final int newIndex) {
		setStyleRecursive(child);
	}

	public void onNodeMoved(final NodeModel oldParent,
	                        final NodeModel newParent, final NodeModel child,
	                        final int newIndex) {
		setStyleRecursive(child);
	}

	public void onPreNodeDelete(final NodeModel model) {
	}

	public void readingCompleted(final NodeModel topNode,
	                             final HashMap<String, String> newIds) {
		if (!topNode.containsExtension(getClass())) {
			return;
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				setStyleRecursive(topNode);
			}
		});
	}

	/** get styles from preferences: */
	private void reloadPatterns() {
		if (AutomaticLayout.patterns == null) {
			final String property = Controller.getResourceController()
			    .getProperty(AutomaticLayout.AUTOMATIC_FORMAT_LEVEL);
			AutomaticLayout.patterns = StylePatternFactory
			    .getPatternsFromString(property);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see freemind.extensions.NodeHook#invoke(freemind.modes.MindMapNode)
	 */
	@Override
	protected void remove(final NodeModel node, final IExtension extension) {
		getModeController().getMapController().removeMapChangeListener(this);
		super.remove(node, extension);
	}

	/**
	 */
	private void setStyle(final NodeModel node) {
		if (((MModeController) getModeController()).isUndoAction()) {
			return;
		}
		getModeController().getMapController().removeMapChangeListener(this);
		setStyleImpl(node);
		getModeController().getMapController().addMapChangeListener(this);
	}

	private void setStyleImpl(final NodeModel node) {
		final int depth = depth(node);
		reloadPatterns();
		int myIndex = AutomaticLayout.patterns.sizeChoiceList() - 1;
		if (depth < AutomaticLayout.patterns.sizeChoiceList()) {
			myIndex = depth;
		}
		final Pattern p = (Pattern) AutomaticLayout.patterns.getChoice(myIndex);
		((MModeController) getModeController()).getPatternController()
		    .applyPattern(node, p);
	}

	/**
	 */
	private void setStyleRecursive(final NodeModel node) {
		getModeController().getMapController().removeMapChangeListener(this);
		setStyleRecursiveImpl(node);
		getModeController().getMapController().addMapChangeListener(this);
	}

	private void setStyleRecursiveImpl(final NodeModel node) {
		if (((MModeController) getModeController()).isUndoAction()) {
			return;
		}
		setStyleImpl(node);
		for (final Iterator i = node.getModeController().getMapController()
		    .childrenUnfolded(node); i.hasNext();) {
			final NodeModel child = (NodeModel) i.next();
			setStyleRecursiveImpl(child);
		}
	}
}
