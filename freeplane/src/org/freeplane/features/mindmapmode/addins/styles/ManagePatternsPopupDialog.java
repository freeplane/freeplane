/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
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

import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogTool;
import org.freeplane.features.mindmapnode.pattern.MPatternController;
import org.freeplane.features.mindmapnode.pattern.Pattern;
import org.freeplane.features.mindmapnode.pattern.StylePatternFactory;
import org.freeplane.features.mindmapnode.pattern.StylePatternPanel;
import org.freeplane.features.mindmapnode.pattern.StylePatternPanel.StylePatternPanelType;

import com.jgoodies.forms.factories.ButtonBarFactory;

/** */
class ManagePatternsPopupDialog extends JDialog implements KeyListener {
	protected final class PatternListModel extends AbstractListModel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		final private List mPatternList;

		public PatternListModel(final List patternList) {
			mPatternList = new Vector(patternList);
		}

		public void add(final int i, final Object object) {
			if (object instanceof String) {
				final String patternName = (String) object;
				final Pattern correspondingPattern = getPatternByName(patternName);
				if (correspondingPattern != null) {
					addPattern(correspondingPattern, i);
				}
			}
		}

		public void addPattern(final Pattern newPattern, final int selectedIndex) {
			mPatternList.add(selectedIndex, newPattern);
			fireIntervalAdded(mList, selectedIndex, selectedIndex);
		}

		/**
		 * @return the name of the pattern belonging to index.
		 */
		public Object getElementAt(final int index) {
			return getPatternAt(index).getName();
		}

		/**
		 * @return the pattern belonging to index.
		 */
		public Pattern getPatternAt(final int index) {
			return ((Pattern) mPatternList.get(index));
		}

		public Pattern getPatternByName(final String name) {
			for (final Iterator iter = mPatternList.iterator(); iter.hasNext();) {
				final Pattern pattern = (Pattern) iter.next();
				if (pattern.getName().equals(name)) {
					return pattern;
				}
			}
			return null;
		}

		public List getPatternList() {
			return Collections.unmodifiableList(mPatternList);
		}

		public int getSize() {
			return mPatternList.size();
		}

		public void remove(final int i) {
			removePattern(i);
		}

		public void removePattern(final int index) {
			if (index < 0 || index >= mPatternList.size()) {
				throw new IllegalArgumentException("try to delete in pattern list with an index out of range: " + index);
			}
			mPatternList.remove(index);
			fireIntervalRemoved(mList, index, index);
		}
	}

	final private class PatternListSelectionListener implements ListSelectionListener {
		public void valueChanged(final ListSelectionEvent e) {
			if (e.getValueIsAdjusting() || mIsDragging) {
				return;
			}
			writePatternBackToModel();
			final JList theList = (JList) e.getSource();
			if (theList.isSelectionEmpty()) {
				mCardLayout.show(mRightStack, ManagePatternsPopupDialog.EMPTY_FRAME);
			}
			else {
				final int index = theList.getSelectedIndex();
				final Pattern p = mPatternListModel.getPatternAt(index);
				setLastSelectedPattern(p);
				mStylePatternFrame.setPatternList(mPatternListModel.getPatternList());
				mStylePatternFrame.setPattern(p);
				mCardLayout.show(mRightStack, ManagePatternsPopupDialog.STACK_PATTERN_FRAME);
			}
		}
	}

	public static final int CANCEL = -1;
	private static final String EMPTY_FRAME = "EMPTY_FRAME";
	public static final int OK = 1;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static Pattern sLastSelectedPattern = null;
	private static final String STACK_PATTERN_FRAME = "PATTERN";
	private static final String WINDOW_PREFERENCE_STORAGE_PROPERTY = "accessories.plugins.dialogs.ManagePatternsPopupDialog/window_positions";
	private JButton jCancelButton;
	private javax.swing.JPanel jContentPane = null;
	private JButton jOKButton;
	private org.freeplane.features.mindmapmode.addins.styles.ArrayListTransferHandler mArrayListHandler;
	private CardLayout mCardLayout;
	final private ModeController mController;
	private boolean mIsDragging = false;
	private Pattern mLastSelectedPattern = null;
	private JList mList;
	private PatternListModel mPatternListModel;
	private JPanel mRightStack;
	private JSplitPane mSplitPane;
	private StylePatternPanel mStylePatternFrame;
	private JPopupMenu popupMenu;
	private int result = ManagePatternsPopupDialog.CANCEL;

	/**
	 * This is the default constructor
	 */
	public ManagePatternsPopupDialog(final ModeController controller) {
		super(controller.getController().getViewController().getFrame());
		mController = controller;
		List patternList = new Vector();
		try {
			patternList = StylePatternFactory.loadPatterns(MPatternController.getController(controller)
			    .getPatternReader());
		}
		catch (final Exception e) {
			LogTool.warn(e);
			JOptionPane.showMessageDialog(this, getDialogTitle(), ResourceBundles
			    .getText("accessories/plugins/ManagePatterns.not_found"), JOptionPane.ERROR_MESSAGE);
		}
		initialize(patternList);
	}

	private void addPattern(final ActionEvent actionEvent) {
		writePatternBackToModel();
		setLastSelectedPattern(null);
		final Pattern newPattern = new Pattern();
		newPattern.setName(searchForNameForNewPattern());
		int selectedIndex = mList.getSelectedIndex();
		if (selectedIndex < 0) {
			selectedIndex = mList.getModel().getSize();
		}
		mPatternListModel.addPattern(newPattern, selectedIndex);
		mList.setSelectedIndex(selectedIndex);
	}

	private void applyToNode(final ActionEvent actionEvent) {
		final int selectedIndex = mList.getSelectedIndex();
		if (selectedIndex < 0) {
			return;
		}
		writePatternBackToModel();
		setLastSelectedPattern(null);
		final Pattern pattern = mPatternListModel.getPatternAt(selectedIndex);
		for (final Iterator iterator = mController.getMapController().getSelectedNodes().iterator(); iterator.hasNext();) {
			final NodeModel node = (NodeModel) iterator.next();
			MPatternController.getController(mController).applyPattern(node, pattern);
		}
	}

	private void cancelPressed() {
		result = ManagePatternsPopupDialog.CANCEL;
		close();
	}

	private void close() {
		final StyleEditorWindowCfgStorage storage = new StyleEditorWindowCfgStorage();
		storage.setDividerPosition(mSplitPane.getDividerLocation());
		storage.storeDialogPositions(this, ManagePatternsPopupDialog.WINDOW_PREFERENCE_STORAGE_PROPERTY);
		this.dispose();
	}

	private void duplicatePattern(final ActionEvent actionEvent) {
		try {
			final int selectedIndex = mList.getSelectedIndex();
			writePatternBackToModel();
			setLastSelectedPattern(null);
			final Pattern oldPattern = mPatternListModel.getPatternAt(selectedIndex);
			final Pattern newPattern = (Pattern) oldPattern.clone();
			newPattern.setName(searchForNameForNewPattern());
			mPatternListModel.addPattern(newPattern, selectedIndex);
			mList.setSelectedIndex(selectedIndex);
		}
		catch (final CloneNotSupportedException e) {
			LogTool.severe(e);
		}
	}

	/**
	 */
	private String getDialogTitle() {
		return ResourceBundles.getText("accessories/plugins/ManagePatterns.dialog.title");
	}

	/**
	 * This method initializes jButton1
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJCancelButton() {
		if (jCancelButton == null) {
			jCancelButton = new JButton();
			jCancelButton.setAction(new AbstractAction() {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				public void actionPerformed(final ActionEvent e) {
					cancelPressed();
				}
			});
			MenuBuilder.setLabelAndMnemonic(jCancelButton, ResourceBundles.getText("cancel"));
		}
		return jCancelButton;
	}

	/**
	 * This method initializes jContentPane
	 *
	 * @return javax.swing.JPanel
	 */
	private javax.swing.JPanel getJContentPane(final List patternList) {
		if (jContentPane == null) {
			jContentPane = new javax.swing.JPanel();
			jContentPane.setLayout(new GridBagLayout());
			mList = new JList();
			mArrayListHandler = new ArrayListTransferHandler();
			mList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			mPatternListModel = new PatternListModel(patternList);
			mList.setModel(mPatternListModel);
			mList.setTransferHandler(mArrayListHandler);
			mList.setDragEnabled(true);
			mList.addListSelectionListener(new PatternListSelectionListener());
			mList.addMouseMotionListener(new MouseMotionListener() {
				public void mouseDragged(final MouseEvent pE) {
					mIsDragging = true;
				}

				public void mouseMoved(final MouseEvent pE) {
					mIsDragging = false;
				}
			});
			/* Some common action listeners */
			final ActionListener addPatternActionListener = new ActionListener() {
				public void actionPerformed(final ActionEvent actionEvent) {
					addPattern(actionEvent);
				}
			};
			final ActionListener fromNodesActionListener = new ActionListener() {
				public void actionPerformed(final ActionEvent actionEvent) {
					insertPatternFromNode(actionEvent);
				}
			};
			final ActionListener applyActionListener = new ActionListener() {
				public void actionPerformed(final ActionEvent actionEvent) {
					applyToNode(actionEvent);
				}
			};
			/** Menu **/
			{
				final JMenuBar menu = new JMenuBar();
				final JMenu mainItem = MenuBuilder.createMenu("ManagePatternsPopupDialog.Actions");
				menu.add(mainItem);
				final JMenuItem menuItemApplyPattern = MenuBuilder.createMenuItem("ManagePatternsPopupDialog.apply");
				menuItemApplyPattern.addActionListener(applyActionListener);
				mainItem.add(menuItemApplyPattern);
				final JMenuItem menuItemAddPattern = MenuBuilder.createMenuItem("ManagePatternsPopupDialog.add");
				menuItemAddPattern.addActionListener(addPatternActionListener);
				mainItem.add(menuItemAddPattern);
				final JMenuItem menuItemPatternFromNodes = MenuBuilder
				    .createMenuItem("ManagePatternsPopupDialog.from_nodes");
				menuItemPatternFromNodes.addActionListener(fromNodesActionListener);
				mainItem.add(menuItemPatternFromNodes);
				this.setJMenuBar(menu);
			}
			/* Popup menu */
			popupMenu = new JPopupMenu();
			final JMenuItem menuItemApply = MenuBuilder.createMenuItem("ManagePatternsPopupDialog.apply");
			popupMenu.add(menuItemApply);
			menuItemApply.addActionListener(applyActionListener);
			final JMenuItem menuItemAdd = MenuBuilder.createMenuItem("ManagePatternsPopupDialog.add");
			popupMenu.add(menuItemAdd);
			menuItemAdd.addActionListener(addPatternActionListener);
			final JMenuItem menuItemDuplicate = MenuBuilder.createMenuItem("ManagePatternsPopupDialog.duplicate");
			popupMenu.add(menuItemDuplicate);
			menuItemDuplicate.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent actionEvent) {
					duplicatePattern(actionEvent);
				}
			});
			final JMenuItem menuItemFromNodes = MenuBuilder.createMenuItem("ManagePatternsPopupDialog.from_nodes");
			popupMenu.add(menuItemFromNodes);
			menuItemFromNodes.addActionListener(fromNodesActionListener);
			popupMenu.addSeparator();
			final JMenuItem menuItemRemove = MenuBuilder.createMenuItem("ManagePatternsPopupDialog.remove");
			menuItemRemove.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent actionEvent) {
					removePattern(actionEvent);
				}
			});
			popupMenu.add(menuItemRemove);
			mList.addMouseListener(new MouseAdapter() {
				/** For Linux */
				@Override
				public void mousePressed(final MouseEvent me) {
					showPopup(mList, me);
				}

				@Override
				public void mouseReleased(final MouseEvent me) {
					showPopup(mList, me);
				}

				private void showPopup(final JList mList, final MouseEvent me) {
					if (me.isPopupTrigger() && !mList.isSelectionEmpty()
					        && mList.locationToIndex(me.getPoint()) == mList.getSelectedIndex()) {
						popupMenu.show(mList, me.getX(), me.getY());
					}
				}
			});
			mCardLayout = new CardLayout();
			mRightStack = new JPanel(mCardLayout);
			mRightStack.add(new JPanel(), ManagePatternsPopupDialog.EMPTY_FRAME);
			mStylePatternFrame = new StylePatternPanel(mController, StylePatternPanelType.WITH_NAME_AND_CHILDS);
			mStylePatternFrame.init();
			mStylePatternFrame.addListeners();
			JScrollPane scrollPane = new JScrollPane(mStylePatternFrame);
			mRightStack.add(scrollPane, ManagePatternsPopupDialog.STACK_PATTERN_FRAME);
			UITools.setScrollbarIncrement(scrollPane);

			final JScrollPane leftPane = new JScrollPane(mList);
			mSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, leftPane, mRightStack);
			jContentPane.add(mSplitPane, new GridBagConstraints(0, 0, 2, 1, 1.0, 8.0, GridBagConstraints.WEST,
			    GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			jContentPane.add(ButtonBarFactory.buildOKCancelBar(getJCancelButton(), getJOKButton()),
			    new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
			        new Insets(0, 0, 0, 0), 0, 0));
			getRootPane().setDefaultButton(getJOKButton());
		}
		return jContentPane;
	}

	/**
	 * This method initializes jButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJOKButton() {
		if (jOKButton == null) {
			jOKButton = new JButton();
			jOKButton.setAction(new AbstractAction() {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				public void actionPerformed(final ActionEvent e) {
					okPressed();
				}
			});
			jOKButton.setText(ResourceBundles.getText("ManagePatternsPopupDialog.Save"));
		}
		return jOKButton;
	}

	public Pattern getLastSelectedPattern() {
		return mLastSelectedPattern;
	}

	public List getPatternList() {
		return mPatternListModel.getPatternList();
	}

	/**
	 * @return Returns the result.
	 */
	public int getResult() {
		return result;
	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize(final List patternList) {
		this.setTitle(getDialogTitle());
		final JPanel contentPane = getJContentPane(patternList);
		this.setContentPane(contentPane);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent we) {
				cancelPressed();
			}
		});
		final Action cancelAction = new AbstractAction() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void actionPerformed(final ActionEvent arg0) {
				cancelPressed();
			}
		};
		UITools.addEscapeActionToDialog(this, cancelAction);
		//
		int i = 0;
		if (ManagePatternsPopupDialog.sLastSelectedPattern != null) {
			for (final Iterator iterator = mPatternListModel.getPatternList().iterator(); iterator.hasNext();) {
				final Pattern pattern = (Pattern) iterator.next();
				if (pattern.getName().equals(ManagePatternsPopupDialog.sLastSelectedPattern.getName())) {
					mList.setSelectedIndex(i);
					break;
				}
				++i;
			}
		}
		this.pack();
		final String marshalled = ResourceController.getResourceController().getProperty(
		    ManagePatternsPopupDialog.WINDOW_PREFERENCE_STORAGE_PROPERTY);
		final StyleEditorWindowCfgStorage decorateDialog = StyleEditorWindowCfgStorage.decorateDialog(marshalled, this);
		if (decorateDialog != null) {
			mSplitPane.setDividerLocation(decorateDialog.getDividerPosition());
		}
	}

	private void insertPatternFromNode(final ActionEvent actionEvent) {
		writePatternBackToModel();
		setLastSelectedPattern(null);
		final Pattern newPattern = StylePatternFactory.createPatternFromSelected(mController.getMapController()
		    .getSelectedNode(), mController.getMapController().getSelectedNodes());
		newPattern.setName(searchForNameForNewPattern());
		int selectedIndex = mList.getSelectedIndex();
		if (selectedIndex < 0) {
			selectedIndex = mList.getModel().getSize();
		}
		mPatternListModel.addPattern(newPattern, selectedIndex);
		mList.setSelectedIndex(selectedIndex);
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
	 */
	public void keyPressed(final KeyEvent keyEvent) {
		switch (keyEvent.getKeyCode()) {
			case KeyEvent.VK_ESCAPE:
				keyEvent.consume();
				cancelPressed();
				break;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
	 */
	public void keyReleased(final KeyEvent keyEvent) {
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
	 */
	public void keyTyped(final KeyEvent keyEvent) {
	}

	private void okPressed() {
		if (writePatternBackToModel()){
			result = ManagePatternsPopupDialog.OK;
			close();
		}
	}

	private void removePattern(final ActionEvent actionEvent) {
		final int selectedIndex = mList.getSelectedIndex();
		setLastSelectedPattern(null);
		mPatternListModel.removePattern(selectedIndex);
		if (mPatternListModel.getSize() > selectedIndex) {
			mList.setSelectedIndex(selectedIndex);
		}
		else if (mPatternListModel.getSize() > 0 && selectedIndex >= 0) {
			mList.setSelectedIndex(selectedIndex - 1);
		}
		else {
			mList.clearSelection();
		}
	}

	private String searchForNameForNewPattern() {
		final String newName = ResourceBundles.getText("PatternNewNameProperty");
		final Vector allNames = new Vector();
		for (final Iterator iter = mPatternListModel.getPatternList().iterator(); iter.hasNext();) {
			final Pattern p = (Pattern) iter.next();
			allNames.add(p.getName());
		}
		String toGiveName = newName;
		int i = 1;
		while (allNames.contains(toGiveName)) {
			toGiveName = newName + i;
			++i;
		}
		return toGiveName;
	}

	public void setLastSelectedPattern(final Pattern pLastSelectedPattern) {
		mLastSelectedPattern = pLastSelectedPattern;
		ManagePatternsPopupDialog.sLastSelectedPattern = pLastSelectedPattern;
	}

	private boolean writePatternBackToModel() {
		final Pattern pattern = getLastSelectedPattern();
		if (pattern == null) {
			return true;
		}
		final Pattern resultPatternCopy = mStylePatternFrame.getResultPattern();
		final String oldPatternName = pattern.getName();
		final String newPatternName = resultPatternCopy.getName();
		if (!(oldPatternName.equals(newPatternName))) {
			for (final Iterator iter = mPatternListModel.getPatternList().iterator(); iter.hasNext();) {
				final Pattern otherPattern = (Pattern) iter.next();
				if (otherPattern == pattern) {
					continue;
				}
				if (otherPattern.getName().equals(newPatternName)) {
					JOptionPane.showMessageDialog(this, ResourceBundles
							.getText("ManagePatternsPopupDialog.DuplicateNameMessage"));
					return false;
				}
			}
		}
		for (final Iterator iter = mPatternListModel.getPatternList().iterator(); iter.hasNext();) {
			final Pattern otherPattern = (Pattern) iter.next();
			if (otherPattern.getPatternChild() != null
					&& oldPatternName.equals(otherPattern.getPatternChild().getValue())) {
				otherPattern.getPatternChild().setValue(newPatternName);
			}
		}
		mStylePatternFrame.getResultPattern(pattern);
		if (pattern.getPatternChild() != null && oldPatternName.equals(pattern.getPatternChild().getValue())) {
			pattern.getPatternChild().setValue(newPatternName);
		}
		return true;
	}
	
}
