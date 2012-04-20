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
package org.freeplane.view.swing.features.time.mindmapmode;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EventListener;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.JTextComponent;

import org.apache.commons.lang.StringUtils;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.WindowConfigurationStorage;
import org.freeplane.core.ui.UIBuilder;
import org.freeplane.core.ui.components.BlindIcon;
import org.freeplane.core.ui.components.MultipleImage;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.clipboard.ClipboardController;
import org.freeplane.features.icon.IconController;
import org.freeplane.features.icon.MindIcon;
import org.freeplane.features.map.IMapChangeListener;
import org.freeplane.features.map.IMapSelectionListener;
import org.freeplane.features.map.INodeChangeListener;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.note.NoteModel;
import org.freeplane.features.text.TextController;
import org.freeplane.features.text.mindmapmode.MTextController;
import org.freeplane.features.ui.IMapViewManager;
import org.freeplane.features.url.mindmapmode.MFileManager;

/**
 * @author foltin
 */
class NodeList {
	private final class MapChangeListener implements IMapChangeListener, INodeChangeListener, IMapSelectionListener {
	    public void onPreNodeMoved(NodeModel oldParent, int oldIndex, NodeModel newParent, NodeModel child, int newIndex) {
	    	disposeDialog();
	    }

		public void onPreNodeDelete(NodeModel oldParent, NodeModel selectedNode, int index) {
	    	disposeDialog();
	    }

	    public void onNodeMoved(NodeModel oldParent, int oldIndex, NodeModel newParent, NodeModel child, int newIndex) {
	    	disposeDialog();
	    }

	    public void onNodeInserted(NodeModel parent, NodeModel child, int newIndex) {
	    	disposeDialog();
	    }

	    public void onNodeDeleted(NodeModel parent, NodeModel child, int index) {
	    	disposeDialog();
	    }

	    public void mapChanged(MapChangeEvent event) {
	    	disposeDialog();
	    }

		public void nodeChanged(NodeChangeEvent event) {
			if(event.getProperty().equals(NodeModel.NODE_TEXT)){
				disposeDialog();
			}
        }

		public void afterMapChange(MapModel oldMap, MapModel newMap) {
       }

		public void beforeMapChange(MapModel oldMap, MapModel newMap) {
			disposeDialog();
        }
    }

	static class DateRenderer extends DefaultTableCellRenderer {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		DateFormat formatter;

		public DateRenderer() {
			super();
		}

		@Override
		public void setValue(final Object value) {
			if (formatter == null) {
				formatter = DateFormat.getDateTimeInstance();
			}
			setText((value == null) ? "" : formatter.format(value));
		}
	}

	final private class FilterTextDocumentListener implements DocumentListener,  ChangeListener, ActionListener {
		private Timer mTypeDelayTimer = null;

		private synchronized void delayedChange() {
			stopTimer();
			mTypeDelayTimer = new Timer(500, new ActionListener() {
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
		public void changedUpdate(final DocumentEvent event) {
			delayedChange();
		}

		public void insertUpdate(final DocumentEvent event) {
			delayedChange();
		}

		public void removeUpdate(final DocumentEvent event) {
			delayedChange();
		}

		private synchronized void change() {
			stopTimer();
			final Object selectedItem = mFilterTextSearchField.getEditor().getItem();
			mFlatNodeTableFilterModel.setFilter((String) selectedItem, matchCase.isSelected(),
			    useRegexInFind.isSelected());
		}

		public void stateChanged(ChangeEvent e) {
			change();
		}

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
			if (object instanceof NodeHolder) {
				return nodeRenderer;
			}
			if (object instanceof NotesHolder) {
				return notesRenderer;
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
		public void keyPressed(final KeyEvent arg0) {
		}

		public void keyReleased(final KeyEvent arg0) {
			if (arg0.getKeyCode() == KeyEvent.VK_ESCAPE) {
				disposeDialog();
			}
			if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
				selectSelectedRows();
				disposeDialog();
			}
		}

		public void keyTyped(final KeyEvent arg0) {
		}
	}

	final private class FlatNodeTableMouseAdapter extends MouseAdapter {
		@Override
		public void mouseClicked(final MouseEvent e) {
			if (e.getClickCount() == 2) {
				final Point p = e.getPoint();
				final int row = timeTable.rowAtPoint(p);
				gotoNodesAndClose(row, new int[] { row });
			}
		}
	}

	static class IconsHolder implements Comparable<IconsHolder> {
		final private String iconNames;
		List<MindIcon> icons = new ArrayList<MindIcon>();

		public IconsHolder(final NodeModel node) {
			icons.addAll(IconController.getController().getIcons(node));
			if (icons.size() > 0) {
				final List<MindIcon> toSort = new ArrayList<MindIcon>(icons);
				Collections.sort(toSort);
				final StringBuilder builder = new StringBuilder();
				for (final MindIcon icon : toSort) {
					builder.append(icon.getName()).append(" ");
				}
				iconNames = builder.toString();
			}
			else {
				iconNames = "";
			}
		}

		public int compareTo(final IconsHolder compareToObject) {
			return toString().compareTo(compareToObject.toString());
		}

		public List<MindIcon> getIcons() {
			return icons;
		}

		/** Returns a sorted list of icon names. */
		@Override
		public String toString() {
			return iconNames;
		}
	}

	static class IconsRenderer extends DefaultTableCellRenderer {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public IconsRenderer() {
			super();
		}

		@Override
		public void setValue(final Object value) {
			if (value instanceof IconsHolder) {
				final IconsHolder iconsHolder = (IconsHolder) value;
				final MultipleImage iconImages = new MultipleImage();
				for (final MindIcon icon : iconsHolder.getIcons()) {
					iconImages.addImage(icon.getIcon());
				}
				if (iconImages.getImageCount() > 0) {
					setIcon(iconImages);
				}
				else {
					setIcon(null);
				}
			}
		}
	}

	public interface IReplaceInputInformation {
		void changeString(NodeHolder holder, String newText);

		int getLength();

		NodeHolder getNodeHolderAt(int i);
	}

	/** removes html in nodes before comparison. */
	public static class NodeHolder implements Comparable<NodeHolder> {
		final private NodeModel node;
		private String originalNodeText = null;
		private String untaggedNodeText = null;

		/**
		 *
		 */
		public NodeHolder(final NodeModel node) {
			this.node = node;
		}

		public int compareTo(final NodeHolder compareToObject) {
			return toString().compareTo(compareToObject.toString());
		}

		public String getUntaggedNodeText() {
			final String nodeText = node.getText();
			if (untaggedNodeText == null || (originalNodeText != null && !originalNodeText.equals(nodeText))) {
				originalNodeText = nodeText;
				untaggedNodeText = HtmlUtils.removeHtmlTagsFromString(nodeText).replaceAll("\\s+", " ");
			}
			return untaggedNodeText;
		}

		@Override
		public String toString() {
			return getUntaggedNodeText();
		}
	}

	static class NodeRenderer extends DefaultTableCellRenderer {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public NodeRenderer() {
			super();
		}

		@Override
		public void setValue(final Object value) {
			setText((value == null) ? "" : ((NodeHolder) value).getUntaggedNodeText());
		}
	}

	/** removes html in notes before comparison. */
	public static class NotesHolder implements Comparable<NotesHolder> {
		final private NodeModel node;
		private String originalNotesText = null;
		private String untaggedNotesText = null;

		/**
		 *
		 */
		public NotesHolder(final NodeModel node) {
			this.node = node;
		}

		public int compareTo(final NotesHolder compareToObject) {
			return toString().compareTo(compareToObject.toString());
		}

		public String getUntaggedNotesText() {
			final String notesText = NoteModel.getNoteText(node);
			if (notesText == null) {
				return "";
			}
			if (untaggedNotesText == null || (originalNotesText != null && !originalNotesText.equals(notesText))) {
				originalNotesText = notesText;
				untaggedNotesText = HtmlUtils.removeHtmlTagsFromString(notesText).replaceAll("\\s+", " ");
			}
			return untaggedNotesText;
		}

		@Override
		public String toString() {
			return getUntaggedNotesText();
		}
	}

	static class NotesRenderer extends DefaultTableCellRenderer {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public NotesRenderer() {
			super();
		}

		@Override
		public void setValue(final Object value) {
			setText((value == null) ? "" : ((NotesHolder) value).getUntaggedNotesText());
		}
	}

	private class ReplaceAllInfo implements IReplaceInputInformation {
		public void changeString(final NodeHolder nodeHolder, final String newText) {
			((MTextController) TextController.getController()).setNodeText(nodeHolder.node, newText);
		}

		public int getLength() {
			return mFlatNodeTableFilterModel.getRowCount();
		}

		public NodeHolder getNodeHolderAt(final int i) {
			return (NodeHolder) mFlatNodeTableFilterModel.getValueAt(i, NodeList.NODE_TEXT_COLUMN);
		}
	}

	private class ReplaceSelectedInfo implements IReplaceInputInformation {
		public void changeString(final NodeHolder nodeHolder, final String newText) {
			((MTextController) TextController.getController()).setNodeText(nodeHolder.node, newText);
		}

		public int getLength() {
			return timeTable.getSelectedRowCount();
		}

		public NodeHolder getNodeHolderAt(final int i) {
			return (NodeHolder) sorter.getValueAt(timeTable.getSelectedRows()[i], NodeList.NODE_TEXT_COLUMN);
		}
	}

	private static String COLUMN_CREATED = "Created";
	private static String COLUMN_DATE = "Date";
	private static String COLUMN_ICONS = "Icons";
	private static String COLUMN_MODIFIED = "Modified";
	private static String COLUMN_NOTES = "Notes";
	private static String COLUMN_TEXT = "Text";
	private static final int DATE_COLUMN = 0;
	protected static final int NODE_CREATED_COLUMN = 3;
	protected static final int NODE_ICON_COLUMN = 2;
	protected static final int NODE_MODIFIED_COLUMN = 4;
	protected static final int NODE_NOTES_COLUMN = 5;
	public static final int NODE_TEXT_COLUMN = 1;
	private static final String PLUGINS_TIME_LIST_XML_CREATED = "plugins/TimeList.xml_Created";
	private static final String PLUGINS_TIME_LIST_XML_DATE = "plugins/TimeList.xml_Date";
	private static final String PLUGINS_TIME_LIST_XML_ICONS = "plugins/TimeList.xml_Icons";
	private static final String PLUGINS_TIME_LIST_XML_MODIFIED = "plugins/TimeList.xml_Modified";
	private static final String PLUGINS_TIME_LIST_XML_NOTES = "plugins/TimeList.xml_Notes";
	private static final String PLUGINS_TIME_LIST_XML_TEXT = "plugins/TimeList.xml_Text";
	private static final String PLUGINS_TIME_MANAGEMENT_XML_CLOSE = "plugins/TimeManagement.xml_closeButton";
	private static final String PLUGINS_TIME_MANAGEMENT_XML_FIND = "plugins/TimeManagement.xml_Find";
	private static final String PLUGINS_TIME_MANAGEMENT_XML_REPLACE = "plugins/TimeManagement.xml_Replace";
//	private static final String PLUGINS_TIME_MANAGEMENT_XML_SELECT = "plugins/TimeManagement.xml_Select";
	private static final String PLUGINS_TIME_MANAGEMENT_XML_WINDOW_TITLE = "plugins/TimeManagement.xml_WindowTitle";
	private static final String PLUGINS_TIME_MANAGEMENT_XML_WINDOW_TITLE_ALL_NODES = "plugins/TimeManagement.xml_WindowTitle_All_Nodes";
	private static final String WINDOW_PREFERENCE_STORAGE_PROPERTY = NodeList.class.getName() + "_properties";

	private static String replace(final Pattern p, String input, final String replacement) {
		final String result = HtmlUtils.getReplaceResult(p, input, replacement);
		return result;
	}

// // 	final private Controller controller;
	private DateRenderer dateRenderer;
	private JDialog dialog;
	private IconsRenderer iconsRenderer;
	final private JComboBox mFilterTextReplaceField;
	final private JComboBox mFilterTextSearchField;
	private FlatNodeTableFilterModel mFlatNodeTableFilterModel;
// 	final private ModeController modeController;
	private JLabel mTreeLabel;
	private NodeRenderer nodeRenderer;
	private NotesRenderer notesRenderer;
	private boolean showAllNodes = false;
	private org.freeplane.view.swing.features.time.mindmapmode.TableSorter sorter;
	private JTable timeTable;
	private DefaultTableModel timeTableModel;
	private final boolean searchInAllMaps;
	private final JCheckBox useRegexInReplace;
	private final JCheckBox useRegexInFind;
	private final JCheckBox matchCase;
	final private boolean modal;

	public NodeList(  final boolean showAllNodes, final boolean searchInAllMaps) {
	    this(false, showAllNodes, searchInAllMaps);
    }

	public NodeList( final boolean modal, final boolean showAllNodes, final boolean searchInAllMaps) {		
//		this.modeController = modeController;
//		controller = modeController.getController();
		this.modal = modal;
		this.showAllNodes = showAllNodes;
		this.searchInAllMaps = searchInAllMaps;
		mFilterTextSearchField = new JComboBox();
		mFilterTextSearchField.setEditable(true);
		final FilterTextDocumentListener listener = new FilterTextDocumentListener();
		mFilterTextSearchField.addActionListener(listener);
		final JTextComponent editorComponent = (JTextComponent) mFilterTextSearchField.getEditor().getEditorComponent();
		editorComponent.getDocument().addDocumentListener(listener);
		mFilterTextSearchField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(final KeyEvent pEvent) {
				if (pEvent.getKeyCode() == KeyEvent.VK_DOWN) {
					mFilterTextReplaceField.requestFocusInWindow();
				}
			}
		});
		mFilterTextReplaceField = new JComboBox();
		mFilterTextReplaceField.setEditable(true);
		mFilterTextReplaceField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(final KeyEvent pEvent) {
				if (pEvent.getKeyCode() == KeyEvent.VK_DOWN) {
					timeTable.requestFocusInWindow();
				}
				else if (pEvent.getKeyCode() == KeyEvent.VK_UP) {
					mFilterTextSearchField.requestFocusInWindow();
				}
			}
		});
		useRegexInReplace = new JCheckBox();
		useRegexInFind = new JCheckBox();
		useRegexInFind.addChangeListener(listener);
		matchCase = new JCheckBox();
		matchCase.addChangeListener(listener);
		final MapChangeListener mapChangeListener = new MapChangeListener();
		final ModeController modeController = Controller.getCurrentModeController();
		final MapController mapController = modeController.getMapController();
		mapController.addMapChangeListener(mapChangeListener);
		mapController.addNodeChangeListener(mapChangeListener);
		Controller.getCurrentController().getMapViewManager().addMapSelectionListener(mapChangeListener);

	}

	/**
	 *
	 */
	private void disposeDialog() {
    	if(dialog == null || !dialog.isVisible()){
    		return;
    	}
		final TimeWindowConfigurationStorage storage = new TimeWindowConfigurationStorage();
		for (int i = 0; i < timeTable.getColumnCount(); i++) {
			final TimeWindowColumnSetting setting = new TimeWindowColumnSetting();
			setting.setColumnWidth(timeTable.getColumnModel().getColumn(i).getWidth());
			setting.setColumnSorting(sorter.getSortingStatus(i));
			storage.addTimeWindowColumnSetting(setting);
		}
		storage.storeDialogPositions(dialog, NodeList.WINDOW_PREFERENCE_STORAGE_PROPERTY);
		dialog.setVisible(false);
		dialog.dispose();
		dialog = null;
	}

	protected void exportSelectedRowsAndClose() {
		final int[] selectedRows = timeTable.getSelectedRows();
		final List<NodeModel> selectedNodes = new ArrayList<NodeModel>();
		for (int i = 0; i < selectedRows.length; i++) {
			final int row = selectedRows[i];
			selectedNodes.add(getMindMapNode(row));
		}
		final ModeController mindMapController = Controller.getCurrentModeController();
		MFileManager.getController(mindMapController).newMapFromDefaultTemplate();
		final MapModel newMap = Controller.getCurrentController().getMap();
		for (final NodeModel node : selectedNodes) {
			final NodeModel copy = ClipboardController.getController().duplicate(node, false);
			if (copy != null) {
				mindMapController.getMapController().insertNodeIntoWithoutUndo(copy, newMap.getRootNode());
			}
		}
		disposeDialog();
	}

	/**
	 */
	private NodeModel getMindMapNode(final int focussedRow) {
		final NodeModel selectedNode = ((NodeHolder) timeTable.getModel().getValueAt(focussedRow,
		    NodeList.NODE_TEXT_COLUMN)).node;
		return selectedNode;
	}

	private void gotoNodesAndClose(final int focussedRow, final int[] selectedRows) {
		selectNodes(focussedRow, selectedRows);
		disposeDialog();
	}

	private void replace(final IReplaceInputInformation info) {
		final String searchString = (String) mFilterTextSearchField.getSelectedItem();
		if(searchString == null)
			return;
		final String replaceString = (String) mFilterTextReplaceField.getSelectedItem();
		Pattern p;
		try {
			p = Pattern.compile(useRegexInFind.isSelected() ? searchString : Pattern.quote(searchString),
					matchCase.isSelected() ? 0 : Pattern.CASE_INSENSITIVE);
		}
		catch (final PatternSyntaxException e) {
			UITools.errorMessage(TextUtils.format("wrong_regexp", searchString, e.getMessage()));
			return;
		}
		final String replacement = replaceString == null ? "" : replaceString;
		final int length = info.getLength();
		for (int i = 0; i < length; i++) {
			final NodeHolder nodeHolder = info.getNodeHolderAt(i);
			final String text = nodeHolder.node.getText();
			final String replaceResult;
			final String literalReplacement = useRegexInReplace.isSelected() ? replacement : Matcher.quoteReplacement(replacement);
			try {
	            if (HtmlUtils.isHtmlNode(text)) {
	            	replaceResult = NodeList.replace(p, text,literalReplacement);
	            }
	            else {
	            	replaceResult = p.matcher(text).replaceAll(literalReplacement);
	            }
            }
            catch (Exception e) {
    			UITools.errorMessage(TextUtils.format("wrong_regexp", replacement, e.getMessage()));
            	return;
            }
			if (!StringUtils.equals(text, replaceResult)) {
				info.changeString(nodeHolder, replaceResult);
			}
		}
		timeTableModel.fireTableDataChanged();
		mFlatNodeTableFilterModel.resetFilter();
		mFilterTextSearchField.insertItemAt(mFilterTextSearchField.getSelectedItem(), 0);
		mFilterTextReplaceField.insertItemAt(mFilterTextReplaceField.getSelectedItem(), 0);
		mFilterTextSearchField.setSelectedItem("");
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
		selectNodes(timeTable.getSelectedRow(), timeTable.getSelectedRows());
	}

	public void startup() {
		if(dialog != null){
			dialog.toFront();
			return;
		}
		NodeList.COLUMN_MODIFIED = TextUtils.getText(PLUGINS_TIME_LIST_XML_MODIFIED);
		NodeList.COLUMN_CREATED = TextUtils.getText(PLUGINS_TIME_LIST_XML_CREATED);
		NodeList.COLUMN_ICONS = TextUtils.getText(PLUGINS_TIME_LIST_XML_ICONS);
		NodeList.COLUMN_TEXT = TextUtils.getText(PLUGINS_TIME_LIST_XML_TEXT);
		NodeList.COLUMN_DATE = TextUtils.getText(PLUGINS_TIME_LIST_XML_DATE);
		NodeList.COLUMN_NOTES = TextUtils.getText(PLUGINS_TIME_LIST_XML_NOTES);
		dialog = new JDialog(Controller.getCurrentController().getViewController().getFrame(), modal /* modal */);
		String windowTitle;
		if (showAllNodes) {
			windowTitle = PLUGINS_TIME_MANAGEMENT_XML_WINDOW_TITLE_ALL_NODES;
		}
		else {
			windowTitle = PLUGINS_TIME_MANAGEMENT_XML_WINDOW_TITLE;
		}
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
		layoutConstraints.gridy++;
		layoutConstraints.weightx = 0.0;
		layoutConstraints.gridwidth = 1;
		contentPane.add(new JLabel(TextUtils.getText(PLUGINS_TIME_MANAGEMENT_XML_REPLACE)), layoutConstraints);
		layoutConstraints.gridx = 5;
		contentPane.add(new JLabel(TextUtils.getText("regular_expressions")), layoutConstraints);
		layoutConstraints.gridx++;
		contentPane.add(useRegexInReplace, layoutConstraints);
		layoutConstraints.gridx = 0;
		layoutConstraints.weightx = 1.0;
		layoutConstraints.gridwidth = GridBagConstraints.REMAINDER;
		layoutConstraints.gridy++;
		contentPane.add(/* new JScrollPane */(mFilterTextReplaceField), layoutConstraints);
		dateRenderer = new DateRenderer();
		nodeRenderer = new NodeRenderer();
		notesRenderer = new NotesRenderer();
		iconsRenderer = new IconsRenderer();
		timeTable = new FlatNodeTable();
		timeTable.addKeyListener(new FlatNodeTableKeyListener());
		timeTable.addMouseListener(new FlatNodeTableMouseAdapter());
		timeTable.getTableHeader().setReorderingAllowed(false);
		timeTableModel = updateModel();
		mFlatNodeTableFilterModel = new FlatNodeTableFilterModel(timeTableModel, NodeList.NODE_TEXT_COLUMN);
		sorter = new TableSorter(mFlatNodeTableFilterModel);
		timeTable.setModel(sorter);
		sorter.setTableHeader(timeTable.getTableHeader());
		sorter.setColumnComparator(Date.class, TableSorter.COMPARABLE_COMPARATOR);
		sorter.setColumnComparator(NodeModel.class, TableSorter.LEXICAL_COMPARATOR);
		sorter.setColumnComparator(IconsHolder.class, TableSorter.COMPARABLE_COMPARATOR);
		sorter.setSortingStatus(NodeList.DATE_COLUMN, TableSorter.ASCENDING);
		final JScrollPane pane = new JScrollPane(timeTable);
		UITools.setScrollbarIncrement(pane);
		layoutConstraints.gridy++;
		GridBagConstraints tableConstraints = (GridBagConstraints) layoutConstraints.clone();
		tableConstraints.weightx = 1;
		tableConstraints.weighty = 10;
		tableConstraints.fill = GridBagConstraints.BOTH;
		contentPane.add(pane, tableConstraints);
		mTreeLabel = new JLabel();
		layoutConstraints.gridy++;
		GridBagConstraints treeConstraints = (GridBagConstraints) layoutConstraints.clone();
		treeConstraints.fill = GridBagConstraints.BOTH;
		@SuppressWarnings("serial")
		JScrollPane scrollPane = new JScrollPane(mTreeLabel){
			@Override
			public boolean isValidateRoot() {
				return false;
			}
		};
		contentPane.add(scrollPane, treeConstraints);
		final AbstractAction exportAction = new AbstractAction(TextUtils.getText("plugins/TimeManagement.xml_Export")) {
			/**
			     * 
			     */
			private static final long serialVersionUID = 1L;

			public void actionPerformed(final ActionEvent arg0) {
				exportSelectedRowsAndClose();
			}
		};
		final JButton exportButton = new JButton(exportAction);
		final AbstractAction replaceAllAction = new AbstractAction(TextUtils
		    .getText("plugins/TimeManagement.xml_Replace_All")) {
			/**
			     * 
			     */
			private static final long serialVersionUID = 1L;

			public void actionPerformed(final ActionEvent arg0) {
				replace(new ReplaceAllInfo());
			}
		};
		final JButton replaceAllButton = new JButton(replaceAllAction);
		final AbstractAction replaceSelectedAction = new AbstractAction(TextUtils
		    .getText("plugins/TimeManagement.xml_Replace_Selected")) {
			/**
			     * 
			     */
			private static final long serialVersionUID = 1L;

			public void actionPerformed(final ActionEvent arg0) {
				replace(new ReplaceSelectedInfo());
			}
		};
		final JButton replaceSelectedButton = new JButton(replaceSelectedAction);
		final AbstractAction gotoAction = new AbstractAction(TextUtils.getText("plugins/TimeManagement.xml_Goto")) {
			/**
			     * 
			     */
			private static final long serialVersionUID = 1L;

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

			public void actionPerformed(final ActionEvent arg0) {
				disposeDialog();
			}
		};
		final JButton cancelButton = new JButton(disposeAction);
		/* Initial State */
		gotoAction.setEnabled(false);
		exportAction.setEnabled(false);
		replaceSelectedAction.setEnabled(false);
		final Box bar = Box.createHorizontalBox();
		bar.add(Box.createHorizontalGlue());
		bar.add(cancelButton);
		bar.add(exportButton);
		bar.add(replaceAllButton);
		bar.add(replaceSelectedButton);
		bar.add(gotoButton);
		bar.add(Box.createHorizontalGlue());
		layoutConstraints.gridy++;
		contentPane.add(/* new JScrollPane */(bar), layoutConstraints);
		final JMenuBar menuBar = new JMenuBar();
		final JMenu menu = new JMenu(TextUtils.getText("plugins/TimeManagement.xml_menu_actions"));
		final AbstractAction[] actionList = new AbstractAction[] { gotoAction,  replaceSelectedAction,
		        replaceAllAction, exportAction, disposeAction };
		for (int i = 0; i < actionList.length; i++) {
			final AbstractAction action = actionList[i];
			final JMenuItem item = menu.add(action);
			item.setIcon(new BlindIcon(UIBuilder.ICON_SIZE));
		}
		menuBar.add(menu);
		dialog.setJMenuBar(menuBar);
		final ListSelectionModel rowSM = timeTable.getSelectionModel();
		rowSM.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(final ListSelectionEvent e) {
				if (e.getValueIsAdjusting()) {
					return;
				}
				final ListSelectionModel lsm = (ListSelectionModel) e.getSource();
				final boolean enable = !(lsm.isSelectionEmpty());
				replaceSelectedAction.setEnabled(enable);
				gotoAction.setEnabled(enable);
				exportAction.setEnabled(enable);
			}
		});
		rowSM.addListSelectionListener(new ListSelectionListener() {
			String getNodeText(final NodeModel node) {
				return TextController.getController().getShortText(node) + ((node.isRoot()) ? "" : (" <- " + getNodeText(node.getParentNode())));
			}

			public void valueChanged(final ListSelectionEvent e) {
				if (e.getValueIsAdjusting()) {
					return;
				}
				final ListSelectionModel lsm = (ListSelectionModel) e.getSource();
				if (lsm.isSelectionEmpty()) {
					mTreeLabel.setText("");
					return;
				}
				final int selectedRow = lsm.getLeadSelectionIndex();
				final NodeModel mindMapNode = getMindMapNode(selectedRow);
				mTreeLabel.setText(getNodeText(mindMapNode));
			}
		});
		final String marshalled = ResourceController.getResourceController().getProperty(
		    NodeList.WINDOW_PREFERENCE_STORAGE_PROPERTY);
		final WindowConfigurationStorage result = TimeWindowConfigurationStorage.decorateDialog(marshalled, dialog);
		final WindowConfigurationStorage storage = result;
		if (storage != null) {
			timeTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			int column = 0;
			for (final TimeWindowColumnSetting setting : ((TimeWindowConfigurationStorage) storage)
			    .getListTimeWindowColumnSettingList()) {
				timeTable.getColumnModel().getColumn(column).setPreferredWidth(setting.getColumnWidth());
				sorter.setSortingStatus(column, setting.getColumnSorting());
				column++;
			}
		}
		mFlatNodeTableFilterModel.setFilter((String)mFilterTextSearchField.getSelectedItem(), 
			matchCase.isSelected(), useRegexInFind.isSelected());
		dialog.setVisible(true);
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
			public Class<?> getColumnClass(final int arg0) {
				switch (arg0) {
					case DATE_COLUMN:
					case NODE_CREATED_COLUMN:
					case NODE_MODIFIED_COLUMN:
						return Date.class;
					case NODE_TEXT_COLUMN:
						return NodeHolder.class;
					case NODE_ICON_COLUMN:
						return IconsHolder.class;
					case NODE_NOTES_COLUMN:
						return NotesHolder.class;
					default:
						return Object.class;
				}
			}
		};
		model.addColumn(NodeList.COLUMN_DATE);
		model.addColumn(NodeList.COLUMN_TEXT);
		model.addColumn(NodeList.COLUMN_ICONS);
		model.addColumn(NodeList.COLUMN_CREATED);
		model.addColumn(NodeList.COLUMN_MODIFIED);
		model.addColumn(NodeList.COLUMN_NOTES);
		if (searchInAllMaps == false) {
			final NodeModel node = Controller.getCurrentController().getMap().getRootNode();
			updateModel(model, node);
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
		if (showAllNodes && node.isVisible() || hook != null) {
			model.addRow(new Object[] { date, new NodeHolder(node), new IconsHolder(node),
			        node.getHistoryInformation().getCreatedAt(), node.getHistoryInformation().getLastModifiedAt(),
			        new NotesHolder(node) });
		}
		for (final NodeModel child : Controller.getCurrentModeController().getMapController().childrenUnfolded(node)) {
			updateModel(model, child);
		}
	}
}
