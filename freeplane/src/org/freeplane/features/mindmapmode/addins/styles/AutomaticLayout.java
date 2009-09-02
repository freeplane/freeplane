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
package org.freeplane.features.mindmapmode.addins.styles;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.freeplane.core.addins.NodeHookDescriptor;
import org.freeplane.core.addins.PersistentNodeHook;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.IReadCompletionListener;
import org.freeplane.core.modecontroller.IMapChangeListener;
import org.freeplane.core.modecontroller.INodeChangeListener;
import org.freeplane.core.modecontroller.MapChangeEvent;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.modecontroller.NodeChangeEvent;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.FpStringUtils;
import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.ui.IPropertyControl;
import org.freeplane.core.resources.ui.IPropertyControlCreator;
import org.freeplane.core.resources.ui.OptionPanelBuilder;
import org.freeplane.core.resources.ui.PropertyBean;
import org.freeplane.core.ui.ActionLocationDescriptor;
import org.freeplane.core.ui.IndexedTree;
import org.freeplane.features.mindmapmode.MModeController;
import org.freeplane.features.mindmapnode.pattern.MPatternController;
import org.freeplane.features.mindmapnode.pattern.Pattern;
import org.freeplane.features.mindmapnode.pattern.Patterns;
import org.freeplane.features.mindmapnode.pattern.StylePatternFactory;
import org.freeplane.n3.nanoxml.XMLElement;

import com.jgoodies.forms.builder.DefaultFormBuilder;

@NodeHookDescriptor(hookName = "accessories/plugins/AutomaticLayout.properties")
@ActionLocationDescriptor(locations = "/menu_bar/format/nodes")
public class AutomaticLayout extends PersistentNodeHook implements IMapChangeListener, INodeChangeListener,
        IReadCompletionListener, IExtension {
	/**
	 * Registers the property pages.
	 *
	 * @author foltin
	 */
	static class MyFreeplanePropertyListener implements IFreeplanePropertyListener {
		public void propertyChanged(final String propertyName, final String newValue, final String oldValue) {
			if (propertyName.startsWith(AutomaticLayout.AUTOMATIC_FORMAT_LEVEL)) {
				AutomaticLayout.patterns = null;
			}
		}
	}

	public static class StylePatternListProperty extends PropertyBean implements IPropertyControl,
	        ListSelectionListener {
		final private DefaultListModel mDefaultListModel;
		boolean mDialogIsShown = false;
		final private ModeController mindMapController;
		JList mList;
		String patterns;

		public StylePatternListProperty(final String name, final ModeController pController) {
			super(name);
			mindMapController = pController;
			mList = new JList();
			mList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			mDefaultListModel = new DefaultListModel();
			mList.setModel(mDefaultListModel);
			mList.addListSelectionListener(this);
			patterns = null;
		}

		private Patterns getPatternsFromString() {
			return StylePatternFactory.getPatternsFromString(patterns);
		}

		@Override
		public String getValue() {
			return patterns;
		}

		public void layout(final DefaultFormBuilder builder) {
			final JLabel label = builder.append(FpStringUtils.getOptionalText(getLabel()));
			builder.append(new JLabel());
			label.setToolTipText(FpStringUtils.getOptionalText(getDescription()));
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
			for (final Iterator i = resultPatterns.getListChoiceList().iterator(); i.hasNext();) {
				final Pattern pattern = (Pattern) i.next();
				mDefaultListModel.addElement(FpStringUtils.getOptionalText("OptionPanel.level" + j) + ": "
				        + StylePatternFactory.toString(pattern));
				j++;
			}
		}

		public void valueChanged(final ListSelectionEvent e) {
			final Patterns pat = getPatternsFromString();
			final JList source = (JList) e.getSource();
			if (source.getSelectedIndex() < 0) {
				return;
			}
			final Pattern choice = pat.getChoice(source.getSelectedIndex());
			final ChooseFormatPopupDialog formatDialog = new ChooseFormatPopupDialog(JOptionPane
			    .getFrameForComponent(source), mindMapController,
			    "accessories/plugins/AutomaticLayout.properties_StyleDialogTitle", choice);
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
							patterns = pat.marshall();
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
	public static class StylePatternProperty extends PropertyBean implements IPropertyControl, ActionListener {
		JButton mButton;
		final private ModeController mindMapController;
		String pattern;

		public StylePatternProperty(final String name, final ModeController pController) {
			super(name);
			mindMapController = pController;
			mButton = new JButton();
			mButton.addActionListener(this);
			pattern = null;
		}

		public void actionPerformed(final ActionEvent e) {
			final Pattern pat = getPatternFromString();
			final ChooseFormatPopupDialog formatDialog = new ChooseFormatPopupDialog(mindMapController.getController()
			    .getViewController().getFrame(), mindMapController,
			    "accessories/plugins/AutomaticLayout.properties_StyleDialogTitle", pat);
			formatDialog.setModal(true);
			formatDialog.setVisible(true);
			if (formatDialog.getResult() == ChooseFormatPopupDialog.OK) {
				final Pattern resultPattern = formatDialog.getPattern();
				resultPattern.setName("dummy");
				pattern = resultPattern.marshall();
				setValue(pattern);
				firePropertyChangeEvent();
			}
		}

		private Pattern getPatternFromString() {
			return StylePatternFactory.getPatternFromString(pattern);
		}

		@Override
		public String getValue() {
			return pattern;
		}

		public void layout(final DefaultFormBuilder builder) {
			final JLabel label = builder.append(FpStringUtils.getOptionalText(getLabel()), mButton);
			label.setToolTipText(FpStringUtils.getOptionalText(getDescription()));
		}

		public void setEnabled(final boolean pEnabled) {
			mButton.setEnabled(pEnabled);
		}

		@Override
		public void setValue(final String value) {
			pattern = value;
			final Pattern resultPattern = getPatternFromString();
			final String patternString = StylePatternFactory.toString(resultPattern);
			mButton.setText(patternString);
			mButton.setToolTipText(patternString);
		}
	}

	/**
	 * Translates style pattern properties into strings.
	 */
	static class StylePropertyTranslator {
		StylePropertyTranslator() {
			super();
		}
	}

	private static final String AUTOMATIC_FORMAT_LEVEL = "automaticFormat_level";
	private static Patterns patterns = null;
	private static final String TAB = "OptionPanel.accessories/plugins/AutomaticLayout.properties_PatternTabName";
	private boolean setStyleActive = false;

	/**
	 *
	 */
	public AutomaticLayout(final ModeController modeController) {
		super(modeController);
		final MyFreeplanePropertyListener listener = new MyFreeplanePropertyListener();
		ResourceController.getResourceController().addPropertyChangeListener(listener);
		addPropertiesToOptionPanel();
		modeController.getMapController().getReadManager().addReadCompletionListener(this);
		getModeController().getMapController().addMapChangeListener(this);
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.extensions.NodeHook#invoke(freeplane.modes.MindMapNode)
	 */
	@Override
	protected void add(final NodeModel node, final IExtension extension) {
		super.add(node, extension);
		setStyleRecursive(node);
	}

	private void addPropertiesToOptionPanel() {
		final MModeController modeController = (MModeController) getModeController();
		final OptionPanelBuilder controls = modeController.getOptionPanelBuilder();
		controls.addTab(TAB);
		final String SEPARATOR = "OptionPanel.separator.accessories/plugins/AutomaticLayout.properties_PatternSeparatorName";
		controls.addSeparator(TAB, SEPARATOR, IndexedTree.AS_CHILD);
		controls.addCreator(TAB + "/" + SEPARATOR, new IPropertyControlCreator() {
			public IPropertyControl createControl() {
				return new StylePatternListProperty(AUTOMATIC_FORMAT_LEVEL, modeController);
			}
		}, AUTOMATIC_FORMAT_LEVEL, IndexedTree.AS_CHILD);
	}

	@Override
	protected IExtension createExtension(final NodeModel node, final XMLElement element) {
		return this;
	}

	private int depth(final NodeModel node) {
		final NodeModel parentNode = node.getParentNode();
		if (parentNode == null) {
			return 0;
		}
		return depth(parentNode) + 1;
	}

	public void mapChanged(final MapChangeEvent event) {
	}

	public void nodeChanged(final NodeChangeEvent event) {
		if(setStyleActive){
			return;
		}
		final NodeModel node = event.getNode();
		if (!isActive(node)) {
			return;
		}
		setStyle(node);
	}

	public void onNodeDeleted(final NodeModel parent, final NodeModel child, final int index) {
	}

	public void onNodeInserted(final NodeModel parent, final NodeModel child, final int newIndex) {
		if (setStyleActive || !isActive(parent)) {
			return;
		}
		setStyleRecursive(child);
	}

	public void onNodeMoved(final NodeModel oldParent, final int oldIndex, final NodeModel newParent,
	                        final NodeModel child, final int newIndex) {
		if (setStyleActive || !isActive(newParent)) {
			return;
		}
		setStyleRecursive(child);
	}

	public void onPreNodeDelete(final NodeModel parent, final NodeModel child, final int index) {
	}

	public void readingCompleted(final NodeModel topNode, final HashMap<String, String> newIds) {
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
			final String property = ResourceController.getResourceController().getProperty(
			    AutomaticLayout.AUTOMATIC_FORMAT_LEVEL);
			AutomaticLayout.patterns = StylePatternFactory.getPatternsFromString(property);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.extensions.NodeHook#invoke(freeplane.modes.MindMapNode)
	 */
	@Override
	protected void remove(final NodeModel node, final IExtension extension) {
		super.remove(node, extension);
	}

	/**
	 */
	private void setStyle(final NodeModel node) {
		if (((MModeController) getModeController()).isUndoAction()) {
			return;
		}
		setStyleActive = true;
		setStyleImpl(node);
		setStyleActive = false;
	}

	private void setStyleImpl(final NodeModel node) {
		final int depth = depth(node);
		reloadPatterns();
		int myIndex = AutomaticLayout.patterns.sizeChoiceList() - 1;
		if (depth < AutomaticLayout.patterns.sizeChoiceList()) {
			myIndex = depth;
		}
		final Pattern p = AutomaticLayout.patterns.getChoice(myIndex);
		MPatternController.getController((getModeController())).applyPattern(node, p);
	}

	/**
	 */
	private void setStyleRecursive(final NodeModel node) {
		setStyleActive = true;
		setStyleRecursiveImpl(node);
		setStyleActive = false;
	}

	private void setStyleRecursiveImpl(final NodeModel node) {
		if (((MModeController) getModeController()).isUndoAction()) {
			return;
		}
		setStyleImpl(node);
		for (final Iterator i = getModeController().getMapController().childrenUnfolded(node); i.hasNext();) {
			final NodeModel child = (NodeModel) i.next();
			setStyleRecursiveImpl(child);
		}
	}

	public void onPreNodeMoved(NodeModel oldParent, int oldIndex, NodeModel newParent, NodeModel child, int newIndex) {
	    // TODO Auto-generated method stub
	    
    }
}
