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
package org.freeplane.view.swing.features.time.mindmapmode.nodelist;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EventListener;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.JTextComponent;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.WindowConfigurationStorage;
import org.freeplane.core.ui.components.JComboBoxWithBorder;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.DelayedRunner;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.IMapChangeListener;
import org.freeplane.features.map.IMapSelectionListener;
import org.freeplane.features.map.INodeChangeListener;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeDeletionEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.NodeMoveEvent;
import org.freeplane.features.map.clipboard.MapClipboardController;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.text.DetailTextModel;
import org.freeplane.features.text.TextController;
import org.freeplane.features.ui.IMapViewManager;
import org.freeplane.features.url.mindmapmode.MFileManager;
import org.freeplane.view.swing.features.time.mindmapmode.ReminderExtension;

/**
 * @author foltin
 */
class NodeList {
	private final class MapChangeListener implements IMapChangeListener, INodeChangeListener, IMapSelectionListener {
		public MapChangeListener() {
			super();
			this.runner = new DelayedRunner(new Runnable() {

				@Override
				public void run() {
					tableModel.fireTableDataChanged();
				}
			});
		}

		final private DelayedRunner runner;
	    @Override
		public void onPreNodeMoved(NodeMoveEvent nodeMoveEvent) {
	    	disposeDialog();
	    }

		@Override
		public void onPreNodeDelete(NodeDeletionEvent nodeDeletionEvent) {
	    	disposeDialog();
	    }

	    @Override
		public void onNodeMoved(NodeMoveEvent nodeMoveEvent) {
	    	disposeDialog();
	    }

	    @Override
		public void onNodeInserted(NodeModel parent, NodeModel child, int newIndex) {
	    	disposeDialog();
	    }

	    @Override
		public void onNodeDeleted(NodeDeletionEvent nodeDeletionEvent) {
	    	disposeDialog();
	    }

	    @Override
		public void mapChanged(MapChangeEvent event) {
	    	disposeDialog();
	    }

		@Override
		public void nodeChanged(NodeChangeEvent event) {
			if(hasTableFieldValueChanged(event.getProperty()))
				runner.runLater();
        }

		@Override
		public void afterMapChange(MapModel oldMap, MapModel newMap) {
       }

		@Override
		public void beforeMapChange(MapModel oldMap, MapModel newMap) {
			disposeDialog();
        }
    }

	final private class FilterTextDocumentListener implements DocumentListener, ActionListener {
		private Timer mTypeDelayTimer = null;
		private String selectedItem = "";
		private boolean shouldMatchCase = false;
		private boolean shouldUseRegex = false;

		private synchronized void delayedChange() {
			stopTimer();
			mTypeDelayTimer = new Timer(500, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					change();
				}
			});
			mTypeDelayTimer.start();
		}
		public void stopTimer() {
	        if (mTypeDelayTimer != null) {
				mTypeDelayTimer.stop();
				mTypeDelayTimer = null;
			}
        }
		@Override
		public void changedUpdate(final DocumentEvent event) {
			delayedChange();
		}

		@Override
		public void insertUpdate(final DocumentEvent event) {
			delayedChange();
		}

		@Override
		public void removeUpdate(final DocumentEvent event) {
			delayedChange();
		}

		private synchronized void change() {
			stopTimer();
			final String selectedItem = (String)mFilterTextSearchField.getEditor().getItem();
			final boolean shouldMatchCase = matchCase.isSelected();
			final boolean shouldUseRegex = useRegexInFind.isSelected();
			if(!this.selectedItem.equals(selectedItem) || this.shouldMatchCase != shouldMatchCase || this.shouldUseRegex != shouldUseRegex) {
				this.selectedItem = selectedItem;
				this.shouldMatchCase = shouldMatchCase;
				this.shouldUseRegex = shouldUseRegex;
				mFlatNodeTableFilterModel.setFilter( selectedItem, shouldMatchCase, shouldUseRegex);
			}
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			change();
        }
	}

	final private class FlatNodeTable extends JTable {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public TableCellRenderer getCellRenderer(final int row, final int column) {
			final Object object = getModel().getValueAt(row, column);
			if (object instanceof Date) {
				return dateRenderer;
			}
			if (object instanceof TextHolder) {
				return textRenderer;
			}
			if (object instanceof IconsHolder) {
				return iconsRenderer;
			}
			return super.getCellRenderer(row, column);
		}

		@Override
		public boolean isCellEditable(final int rowIndex, final int vColIndex) {
			return false;
		}

		@Override
		protected void processKeyEvent(final KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				final EventListener[] el = super.getListeners(KeyListener.class);
				if (e.getID() != KeyEvent.KEY_RELEASED) {
					return;
				}
				for (int i = 0; i < el.length; i++) {
					final KeyListener kl = (KeyListener) el[i];
					kl.keyReleased(e);
				}
				return;
			}
			super.processKeyEvent(e);
		}
	}

	final private class FlatNodeTableKeyListener implements KeyListener {
		@Override
		public void keyPressed(final KeyEvent arg0) {
		}

		@Override
		public void keyReleased(final KeyEvent arg0) {
			if (arg0.getKeyCode() == KeyEvent.VK_ESCAPE) {
				disposeDialog();
			}
			if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
				selectSelectedRows();
				disposeDialog();
			}
		}

		@Override
		public void keyTyped(final KeyEvent arg0) {
		}
	}

	final private class FlatNodeTableMouseAdapter extends MouseAdapter {
		@Override
		public void mouseClicked(final MouseEvent e) {
			if (e.getClickCount() == 2) {
				final Point p = e.getPoint();
				final int row = tableView.rowAtPoint(p);
				selectNodes(row, new int[] { row });
				if (!(e.isControlDown() || e.isMetaDown()))
					disposeDialog();
			}
		}
	}

	private static String COLUMN_CREATED = "Created";
	private static String COLUMN_REMINDER = "Reminder";
	private static String COLUMN_ICONS = "Icons";
	private static String COLUMN_MODIFIED = "Modified";
	private static String COLUMN_NOTES = "Notes";
	private static String COLUMN_TEXT = "Text";
	private static String COLUMN_DETAILS = "Details";
	private static String COLUMN_MAP = "Map";
	private final int nodeMapColumn;
	final int nodeTextColumn;
	private final int nodeIconColumn;
	final int nodeDetailsColumn;
	final int nodeNotesColumn;
	private final int nodeReminderColumn;
	private final int nodeCreatedColumn;
	private final int nodeModifiedColumn;
	private static final String PLUGINS_TIME_LIST_XML_CREATED = "plugins/TimeList.xml_Created";
	private static final String PLUGINS_TIME_LIST_XML_REMINDER = "plugins/TimeList.xml_Reminder";
	private static final String PLUGINS_TIME_LIST_XML_ICONS = "plugins/TimeList.xml_Icons";
	private static final String PLUGINS_TIME_LIST_XML_MODIFIED = "plugins/TimeList.xml_Modified";
	private static final String PLUGINS_TIME_LIST_XML_NOTES = "plugins/TimeList.xml_Notes";
	private static final String PLUGINS_TIME_LIST_XML_DETAILS = "plugins/TimeList.xml_Details";

	private static final String PLUGINS_TIME_LIST_XML_MAP = "plugins/TimeList.xml_Map";
	private static final String PLUGINS_TIME_LIST_XML_TEXT = "plugins/TimeList.xml_Text";
	private static final String PLUGINS_TIME_MANAGEMENT_XML_CLOSE = "plugins/TimeManagement.xml_closeButton";
	private static final String PLUGINS_TIME_MANAGEMENT_XML_FIND = "plugins/TimeManagement.xml_Find";
	public static final String PLUGINS_TIME_MANAGEMENT_XML_WINDOW_TITLE = "plugins/TimeManagement.xml_WindowTitle";
	public static final String PLUGINS_TIME_MANAGEMENT_XML_WINDOW_TITLE_ALL_NODES = "plugins/TimeManagement.xml_WindowTitle_All_Nodes";
	private final String windowPreferenceStorageProperty;

	private DateRenderer dateRenderer;
	private JDialog dialog;
	private IconsRenderer iconsRenderer;
	protected final JComboBox mFilterTextSearchField;
	protected FlatNodeTableFilterModel mFlatNodeTableFilterModel;
	private JTextField mNodePath;
	private TextRenderer textRenderer;

	private final String windowTitle;
	public interface NodeFilter {
		boolean showsNode(NodeModel node, ReminderExtension reminder) ;
	}
	private final NodeFilter nodeFilter;
	TableSorter sorter;
	protected JTable tableView;
	private DefaultTableModel tableModel;
	private final boolean searchInAllMaps;
	protected final JCheckBox useRegexInFind;
	protected final JCheckBox matchCase;
	final private boolean modal;
	private final MapChangeListener mapChangeListener;

	NodeList( final String windowTitle, final NodeFilter nodeFilter, final boolean searchInAllMaps, String windowPreferenceStorageProperty) {
	    this(false, windowTitle, nodeFilter, searchInAllMaps, windowPreferenceStorageProperty);
    }

	public NodeList( final boolean modal, String windowTitle, final NodeFilter nodeFilter, final boolean searchInAllMaps, String windowPreferenceStorageProperty) {
		this.windowTitle = windowTitle;
		nodeMapColumn = searchInAllMaps ? 0 : -1;
		nodeTextColumn = nodeMapColumn + 1;
		nodeIconColumn = nodeTextColumn + 1;
		nodeDetailsColumn = nodeIconColumn + 1;
		nodeNotesColumn = nodeDetailsColumn + 1;
		nodeReminderColumn = nodeNotesColumn + 1;
		nodeCreatedColumn = nodeReminderColumn + 1;
		nodeModifiedColumn = nodeCreatedColumn + 1;

//		this.modeController = modeController;
//		controller = modeController.getController();
		this.modal = modal;
		this.nodeFilter = nodeFilter;
		this.searchInAllMaps = searchInAllMaps;
		mFilterTextSearchField = new JComboBoxWithBorder();
		mFilterTextSearchField.setEditable(true);
		final FilterTextDocumentListener listener = new FilterTextDocumentListener();
		mFilterTextSearchField.addActionListener(listener);
		final JTextComponent editorComponent = (JTextComponent) mFilterTextSearchField.getEditor().getEditorComponent();
		editorComponent.getDocument().addDocumentListener(listener);
		useRegexInFind = new JCheckBox();
		useRegexInFind.addActionListener(listener);
		matchCase = new JCheckBox();
		matchCase.addActionListener(listener);
		mapChangeListener = new MapChangeListener();
		this.windowPreferenceStorageProperty = windowPreferenceStorageProperty;
	}

	/**
	 *
	 */
	private void disposeDialog() {
    	if(dialog == null || !dialog.isVisible()){
    		return;
    	}
		final TimeWindowConfigurationStorage storage = new TimeWindowConfigurationStorage();
		for (int i = 0; i < tableView.getColumnCount(); i++) {
			final TimeWindowColumnSetting setting = new TimeWindowColumnSetting();
			setting.setColumnWidth(tableView.getColumnModel().getColumn(i).getWidth());
			setting.setColumnSorting(sorter.getSortingStatus(i));
			storage.addTimeWindowColumnSetting(setting);
		}
		storage.storeDialogPositions(dialog, windowPreferenceStorageProperty);
		final boolean dialogWasFocused = dialog.isFocused();
		dialog.setVisible(false);
		dialog.dispose();
		dialog = null;
		final ModeController modeController = Controller.getCurrentModeController();
		final MapController mapController = modeController.getMapController();
		mapController.removeMapChangeListener(mapChangeListener);
		mapController.removeNodeChangeListener(mapChangeListener);
		final IMapViewManager mapViewManager = Controller.getCurrentController().getMapViewManager();
		mapViewManager.removeMapSelectionListener(mapChangeListener);
		if(dialogWasFocused) {
			final Component selectedComponent = mapViewManager.getSelectedComponent();
			if(selectedComponent != null)
				selectedComponent.requestFocus();
		}
	}

	protected void exportSelectedRowsAndClose() {
		final int[] selectedRows = tableView.getSelectedRows();
		final List<NodeModel> selectedNodes = new ArrayList<NodeModel>();
		for (int i = 0; i < selectedRows.length; i++) {
			final int row = selectedRows[i];
			selectedNodes.add(getMindMapNode(row));
		}
		final ModeController mindMapController = Controller.getCurrentModeController();
		MFileManager.getController(mindMapController).newMapFromDefaultTemplate();
		final MapModel newMap = Controller.getCurrentController().getMap();
		for (final NodeModel node : selectedNodes) {
			final NodeModel copy = MapClipboardController.getController().duplicate(node, false);
			if (copy != null) {
				mindMapController.getMapController().insertNodeIntoWithoutUndo(copy, newMap.getRootNode());
			}
		}
		disposeDialog();
	}

	/**
	 */
	private NodeModel getMindMapNode(final int focussedRow) {
		final NodeModel selectedNode = ((TextHolder) tableView.getModel().getValueAt(focussedRow,
		    nodeTextColumn)).getNode();
		return selectedNode;
	}


	private void selectNodes(final int focussedRow, final int[] selectedRows) {
		if (focussedRow >= 0) {
			final NodeModel focussedNode = getMindMapNode(focussedRow);
			final MapModel map = focussedNode.getMap();
			final List<NodeModel> selectedNodes = new ArrayList<NodeModel>();
			for (final int row : selectedRows) {
				final NodeModel node = getMindMapNode(row);
				if (!node.getMap().equals(map)) {
					continue;
				}
				selectedNodes.add(node);
			}
			selectMap(map);
			Controller.getCurrentModeController().getMapController().selectMultipleNodes(focussedNode, selectedNodes);
		}
	}

	private void selectMap(final MapModel map) {
		if (map.equals(Controller.getCurrentController().getMap())) {
			return;
		}
		final IMapViewManager mapViewManager = Controller.getCurrentController().getMapViewManager();
		final Map<String, MapModel> maps = mapViewManager.getMaps(MModeController.MODENAME);
		for (final Map.Entry<String, MapModel> entry : maps.entrySet()) {
			if (map.equals(entry.getValue())) {
				mapViewManager.tryToChangeToMapView(entry.getKey());
			}
		}
	}

	private void selectSelectedRows() {
		selectNodes(tableView.getSelectedRow(), tableView.getSelectedRows());
	}

	public void startup() {
		if(dialog != null){
			dialog.toFront();
			return;
		}
		COLUMN_MODIFIED = TextUtils.getText(PLUGINS_TIME_LIST_XML_MODIFIED);
		COLUMN_CREATED = TextUtils.getText(PLUGINS_TIME_LIST_XML_CREATED);
		COLUMN_ICONS = TextUtils.getText(PLUGINS_TIME_LIST_XML_ICONS);
		COLUMN_TEXT = TextUtils.getText(PLUGINS_TIME_LIST_XML_TEXT);
		COLUMN_MAP = TextUtils.getText(PLUGINS_TIME_LIST_XML_MAP);
		COLUMN_DETAILS= TextUtils.getText(PLUGINS_TIME_LIST_XML_DETAILS);
		COLUMN_REMINDER = TextUtils.getText(PLUGINS_TIME_LIST_XML_REMINDER);
		COLUMN_NOTES = TextUtils.getText(PLUGINS_TIME_LIST_XML_NOTES);
		dialog = new JDialog(UITools.getCurrentFrame(), modal /* modal */);
		dialog.setTitle(TextUtils.getText(windowTitle));
		dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		final WindowAdapter windowListener = new WindowAdapter() {

			@Override
            public void windowGainedFocus(WindowEvent e) {
				mFilterTextSearchField.getEditor().selectAll();
            }

			@Override
			public void windowClosing(final WindowEvent event) {
				disposeDialog();
			}
		};
		dialog.addWindowListener(windowListener);
		dialog.addWindowFocusListener(windowListener);
		UITools.addEscapeActionToDialog(dialog, new AbstractAction() {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent arg0) {
				disposeDialog();
			}
		});
		final Container contentPane = dialog.getContentPane();
		final GridBagLayout gbl = new GridBagLayout();
		contentPane.setLayout(gbl);
		final GridBagConstraints layoutConstraints = new GridBagConstraints();
		layoutConstraints.gridx = 0;
		layoutConstraints.gridy = 0;
		layoutConstraints.gridwidth = 1;
		layoutConstraints.gridheight = 1;
		layoutConstraints.weightx = 0.0;
		layoutConstraints.weighty = 0.0;
		layoutConstraints.anchor = GridBagConstraints.WEST;
		layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
		contentPane.add(new JLabel(TextUtils.getText(PLUGINS_TIME_MANAGEMENT_XML_FIND)), layoutConstraints);
		layoutConstraints.gridwidth = 1;
		layoutConstraints.gridx++;
		contentPane.add(Box.createHorizontalStrut(40), layoutConstraints);
		layoutConstraints.gridx++;
		contentPane.add(new JLabel(TextUtils.getText("filter_match_case")), layoutConstraints);
		layoutConstraints.gridx++;
		contentPane.add(matchCase, layoutConstraints);
		layoutConstraints.gridx++;
		contentPane.add(Box.createHorizontalStrut(40), layoutConstraints);
		layoutConstraints.gridx++;
		contentPane.add(new JLabel(TextUtils.getText("regular_expressions")), layoutConstraints);
		layoutConstraints.gridx++;
		contentPane.add(useRegexInFind, layoutConstraints);
		layoutConstraints.gridx = 0;
		layoutConstraints.weightx = 1.0;
		layoutConstraints.gridwidth = GridBagConstraints.REMAINDER;
		layoutConstraints.gridy++;
		contentPane.add(/* new JScrollPane */(mFilterTextSearchField), layoutConstraints);
		createReplacementUI(contentPane, layoutConstraints);
		dateRenderer = new DateRenderer();
		textRenderer = new TextRenderer();
		iconsRenderer = new IconsRenderer();
		tableView = new FlatNodeTable();
		tableView.addKeyListener(new FlatNodeTableKeyListener());
		tableView.addMouseListener(new FlatNodeTableMouseAdapter());
		tableView.getTableHeader().setReorderingAllowed(false);
		tableModel = updateModel();
		mFlatNodeTableFilterModel = new FlatNodeTableFilterModel(tableModel,
			new int[]{nodeTextColumn, nodeDetailsColumn, nodeNotesColumn}
		);
		sorter = new TableSorter(mFlatNodeTableFilterModel);
		tableView.setModel(sorter);
		sorter.setTableHeader(tableView.getTableHeader());
		sorter.setColumnComparator(Date.class, TableSorter.COMPARABLE_COMPARATOR);
		sorter.setColumnComparator(NodeModel.class, TableSorter.LEXICAL_COMPARATOR);
		sorter.setColumnComparator(IconsHolder.class, TableSorter.COMPARABLE_COMPARATOR);
		sorter.setSortingStatus(nodeReminderColumn, TableSorter.ASCENDING);
		final JScrollPane pane = new JScrollPane(tableView);
		UITools.setScrollbarIncrement(pane);
		layoutConstraints.gridy++;
		GridBagConstraints tableConstraints = (GridBagConstraints) layoutConstraints.clone();
		tableConstraints.weightx = 1;
		tableConstraints.weighty = 10;
		tableConstraints.fill = GridBagConstraints.BOTH;
		contentPane.add(pane, tableConstraints);
		mNodePath = new JTextField();
		mNodePath.setEditable(false);
		layoutConstraints.gridy++;
		GridBagConstraints treeConstraints = (GridBagConstraints) layoutConstraints.clone();
		treeConstraints.fill = GridBagConstraints.BOTH;
		@SuppressWarnings("serial")
		JScrollPane scrollPane = new JScrollPane(mNodePath, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		contentPane.add(scrollPane, treeConstraints);
		final AbstractAction exportAction = new AbstractAction(TextUtils.getText("plugins/TimeManagement.xml_Export")) {
			/**
			     *
			     */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent arg0) {
				exportSelectedRowsAndClose();
			}
		};
		final JButton exportButton = new JButton(exportAction);
		final AbstractAction gotoAction = new AbstractAction(TextUtils.getText("plugins/TimeManagement.xml_Goto")) {
			/**
			     *
			     */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent arg0) {
				selectSelectedRows();
			}
		};
		final JButton gotoButton = new JButton(gotoAction);
		final AbstractAction disposeAction = new AbstractAction(TextUtils.getText(PLUGINS_TIME_MANAGEMENT_XML_CLOSE)) {
			/**
			     *
			     */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent arg0) {
				disposeDialog();
			}
		};
		final JButton cancelButton = new JButton(disposeAction);
		/* Initial State */
		gotoAction.setEnabled(false);
		exportAction.setEnabled(false);
		final Box bar = Box.createHorizontalBox();
		bar.add(Box.createHorizontalGlue());
		bar.add(cancelButton);
		bar.add(exportButton);
		addReplacementButtons(bar);
		bar.add(gotoButton);
		bar.add(Box.createHorizontalGlue());
		layoutConstraints.gridy++;
		contentPane.add(/* new JScrollPane */(bar), layoutConstraints);
		final ListSelectionModel rowSM = tableView.getSelectionModel();
		rowSM.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(final ListSelectionEvent e) {
				if (e.getValueIsAdjusting()) {
					return;
				}
				final ListSelectionModel lsm = (ListSelectionModel) e.getSource();
				final boolean enable = !(lsm.isSelectionEmpty());
				gotoAction.setEnabled(enable);
				exportAction.setEnabled(enable);
			}
		});
		rowSM.addListSelectionListener(new ListSelectionListener() {
			String getNodeText(final NodeModel node) {
				final String nodeText = TextController.getController().getShortPlainText(node);
				if (node.isRoot())
					return nodeText;
				else
					return getNodeText(node.getParentNode()) + " -> " + nodeText;
			}

			@Override
			public void valueChanged(final ListSelectionEvent e) {
				if (e.getValueIsAdjusting()) {
					return;
				}
				final ListSelectionModel lsm = (ListSelectionModel) e.getSource();
				if (lsm.isSelectionEmpty()) {
					mNodePath.setText("");
					return;
				}
				final int selectedRow = lsm.getLeadSelectionIndex();
				final NodeModel mindMapNode = getMindMapNode(selectedRow);
				mNodePath.setText(getNodeText(mindMapNode));
			}
		});
		final String marshalled = ResourceController.getResourceController().getProperty(
				windowPreferenceStorageProperty);
		final WindowConfigurationStorage result = TimeWindowConfigurationStorage.decorateDialog(marshalled, dialog);
		final WindowConfigurationStorage storage = result;
		if (storage != null) {
			tableView.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			int column = 0;
			for (final TimeWindowColumnSetting setting : ((TimeWindowConfigurationStorage) storage)
			    .getListTimeWindowColumnSettingList()) {
				tableView.getColumnModel().getColumn(column).setPreferredWidth(setting.getColumnWidth());
				sorter.setSortingStatus(column, setting.getColumnSorting());
				column++;
			}
		}
		mFlatNodeTableFilterModel.setFilter((String)mFilterTextSearchField.getSelectedItem(),
			matchCase.isSelected(), useRegexInFind.isSelected());
		final ModeController modeController = Controller.getCurrentModeController();
		final MapController mapController = modeController.getMapController();
		mapController.addMapChangeListener(mapChangeListener);
		mapController.addNodeChangeListener(mapChangeListener);
		Controller.getCurrentController().getMapViewManager().addMapSelectionListener(mapChangeListener);
		dialog.setVisible(true);
	}

	protected void addReplacementButtons(final Box bar) {

	}


	protected void createReplacementUI(Container contentPane, GridBagConstraints layoutConstraints) {
	}

	/**
	 * Creates a table model for the new table and returns it.
	 */
	private DefaultTableModel updateModel() {
		final DefaultTableModel model = new DefaultTableModel() {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			/*
			 * (non-Javadoc)
			 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
			 */
			@Override
			public Class<?> getColumnClass(final int column) {
				if (column == nodeReminderColumn || column == nodeCreatedColumn || column == nodeModifiedColumn) {
					return Date.class;
				}
				else if (column == nodeTextColumn || column == nodeNotesColumn || column == nodeDetailsColumn) {
					return TextHolder.class;
				}
				else if (column == nodeMapColumn) {
					return String.class;
				}
				else if (column == nodeIconColumn) {
					return IconsHolder.class;
				}
				else {
					return Object.class;
				}
			}
		};
		if(nodeMapColumn >= 0)
			model.addColumn(COLUMN_MAP);
		model.addColumn(COLUMN_TEXT);
		model.addColumn(COLUMN_ICONS);
		model.addColumn(COLUMN_DETAILS);
		model.addColumn(COLUMN_NOTES);
		model.addColumn(COLUMN_REMINDER);
		model.addColumn(COLUMN_CREATED);
		model.addColumn(COLUMN_MODIFIED);
		if (searchInAllMaps == false) {
			final MapModel map = Controller.getCurrentController().getMap();
			if(map != null) {
				final NodeModel node = map.getRootNode();
				updateModel(model, node);
			}
		}
		else {
			final Map<String, MapModel> maps = Controller.getCurrentController().getMapViewManager().getMaps(MModeController.MODENAME);
			for (final MapModel map : maps.values()) {
				final NodeModel node = map.getRootNode();
				updateModel(model, node);
			}
		}
		return model;
	}

	private void updateModel(final DefaultTableModel model, final NodeModel node) {
		final ReminderExtension hook = ReminderExtension.getExtension(node);
		Date date = null;
		if (hook != null) {
			date = new Date(hook.getRemindUserAt());
		}
		if (nodeFilter.showsNode(node, hook)) {
			final Object[] row = searchInAllMaps ? new Object[] {
					node.getMap().getTitle(),
					new TextHolder(new CoreTextAccessor(node)),
					new IconsHolder(node),
					new TextHolder(new DetailTextAccessor(node)) ,
					new TextHolder(new NoteTextAccessor(node)),
					date,
					node.getHistoryInformation().getCreatedAt(),
					node.getHistoryInformation().getLastModifiedAt()} :
						new Object[] {
								new TextHolder(new CoreTextAccessor(node)),
								new IconsHolder(node),
								new TextHolder(new DetailTextAccessor(node)) ,
								new TextHolder(new NoteTextAccessor(node)),
					date,
					node.getHistoryInformation().getCreatedAt(),
					node.getHistoryInformation().getLastModifiedAt()};
			model.addRow(row);
		}
		for (final NodeModel child : node.getChildren()) {
			updateModel(model, child);
		}
	}
	static private HashSet<Object> changeableProperties = new HashSet<Object>(
			Arrays.asList(NodeModel.NODE_TEXT, NodeModel.NODE_ICON, DetailTextModel.class, NodeModel.NOTE_TEXT)
			);

	private boolean hasTableFieldValueChanged(Object property) {
		return changeableProperties.contains(property);
	}

}
