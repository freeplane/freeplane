package org.freeplane.plugin.workspace.model;

import javax.swing.JMenuItem;
import javax.swing.JTree;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.SelectableAction;
import org.freeplane.core.ui.components.JAutoCheckBoxMenuItem;
import org.freeplane.core.ui.components.JFreeplaneMenuItem;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.workspace.config.actions.AWorkspaceAction;

public class WorkspacePopupMenuBuilder {

	public static final String SEPARATOR = "-----";
	
	public WorkspacePopupMenuBuilder() {

	}

//	private void registerWorkspaceActions() {
//		ModeController modeController = Controller.getCurrentModeController();		
//		
//		modeController.addAction(new FileNodeAddNewMindmapAction());
//		modeController.addAction(new FileNodeCutAction());
//		modeController.addAction(new FileNodeRenameAction());
//		modeController.addAction(new FileNodeDeleteAction());
//		modeController.addAction(new FileNodeCopyAction());
//		modeController.addAction(new FileNodePasteAction());
//		
//		modeController.addAction(new AddNewFilesystemFolderAction());
//		modeController.addAction(new AddExistingFilesystemFolderAction());
//		modeController.addAction(new RemoveNodeFromWorkspaceAction());
//	}

	public static void addAction(final WorkspacePopupMenu popupMenu, AFreeplaneAction action) {
		assert action != null;
		assert popupMenu != null;
		
		final JMenuItem item;
		if (action.getClass().getAnnotation(SelectableAction.class) != null) {
			item = new JAutoCheckBoxMenuItem(action);
		}
		else {
			item = new JFreeplaneMenuItem(action);
		}
		//addMenuItem(category, item, key, position);
		popupMenu.add(item);
		addListeners(popupMenu, action);
		return;
	}
	
	public static void addActions(final WorkspacePopupMenu popupMenu, final String[] keys) {
		assert popupMenu != null;
		assert keys != null;
		
		for(String key : keys) {
			if(key == null) {
				continue;
			}
			else
			if(key.equals(SEPARATOR)) {
				popupMenu.addSeparator();
			} 
			else {
				AFreeplaneAction action = Controller.getCurrentModeController().getAction(key);
				if(action == null) {
					continue;
				}
				addAction(popupMenu, action);
			}
			
		}
	}
	
	private static void addListeners(final WorkspacePopupMenu popupMenu, final AFreeplaneAction action) {
		if (action instanceof PopupMenuListener) {
			popupMenu.addPopupMenuListener(new DelegatingPopupMenuListener((PopupMenuListener) action, popupMenu));
		}
		if (AFreeplaneAction.checkSelectionOnPopup(action)) {
			popupMenu.addPopupMenuListener(new PopupMenuListener() {
				public void popupMenuCanceled(final PopupMenuEvent e) {
				}

				public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
				}

				public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
					if(action instanceof AWorkspaceAction && e.getSource() instanceof WorkspacePopupMenu) {
						WorkspacePopupMenu menu = ((WorkspacePopupMenu)e.getSource());
						AWorkspaceTreeNode node = (AWorkspaceTreeNode) ((JTree)menu.getInvoker()).getPathForLocation(menu.getInvokerLocation().x, menu.getInvokerLocation().y).getLastPathComponent();
						((AWorkspaceAction) action).setSelectedFor(node);
					} 
					else {
						action.setSelected();
					}
				}
			});
		}
		if (AFreeplaneAction.checkEnabledOnPopup(action)) {
			popupMenu.addPopupMenuListener(new PopupMenuListener() {
				public void popupMenuCanceled(final PopupMenuEvent e) {
				}

				public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
				}

				public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
					if(action instanceof AWorkspaceAction && e.getSource() instanceof WorkspacePopupMenu) {
						WorkspacePopupMenu menu = ((WorkspacePopupMenu)e.getSource());
						AWorkspaceTreeNode node = (AWorkspaceTreeNode) ((JTree)menu.getInvoker()).getPathForLocation(menu.getInvokerLocation().x, menu.getInvokerLocation().y).getLastPathComponent();
						((AWorkspaceAction) action).setEnabledFor(node);
					}
					else {
						action.setEnabled();
					}
				}
			});
		}
	}
	
	static private class DelegatingPopupMenuListener implements PopupMenuListener {
		final private PopupMenuListener listener;
		final private Object source;

		public DelegatingPopupMenuListener(final PopupMenuListener listener, final Object source) {
			super();
			this.listener = listener;
			this.source = source;
		}

		public Object getSource() {
			return source;
		}

		private PopupMenuEvent newEvent() {
			return new PopupMenuEvent(getSource());
		}

		public void popupMenuCanceled(final PopupMenuEvent e) {
			listener.popupMenuCanceled(newEvent());
		}

		public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
			listener.popupMenuWillBecomeInvisible(newEvent());
		}

		public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
			listener.popupMenuWillBecomeVisible(newEvent());
		}
	}
}
