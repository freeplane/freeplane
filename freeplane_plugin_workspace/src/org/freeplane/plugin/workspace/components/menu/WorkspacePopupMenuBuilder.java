package org.freeplane.plugin.workspace.components.menu;

import java.util.Stack;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.tree.TreePath;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.SelectableAction;
import org.freeplane.core.ui.components.JAutoCheckBoxMenuItem;
import org.freeplane.core.ui.components.JFreeplaneMenuItem;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.actions.AWorkspaceAction;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;

public class WorkspacePopupMenuBuilder {

	public static final String SEPARATOR = "-----";
	
	public WorkspacePopupMenuBuilder() {
	}

	public static void addAction(final JComponent popupMenu, AFreeplaneAction action) {
		assert action != null;
		assert popupMenu != null;
		
		final JMenuItem item;
		if (action.getClass().getAnnotation(SelectableAction.class) != null) {
			item = new JAutoCheckBoxMenuItem(action);
		}
		else {
			item = new JFreeplaneMenuItem(action);
		}
		popupMenu.add(item);
		return;
	}
	
	public static void insertAction(final WorkspacePopupMenu popupMenu, AFreeplaneAction action, int index) {
		assert action != null;
		assert popupMenu != null;
		
		final JMenuItem item;
		if (action.getClass().getAnnotation(SelectableAction.class) != null) {
			item = new JAutoCheckBoxMenuItem(action);
		}
		else {
			item = new JFreeplaneMenuItem(action);
		}
		popupMenu.add(item, index);
		addListeners(popupMenu, action);
		return;
	}
	
	public static void insertAction(final WorkspacePopupMenu popupMenu, String actionKey, int index) {
		assert actionKey != null;
		assert popupMenu != null;
		if(actionKey.equals(SEPARATOR)) {
			popupMenu.add(new JPopupMenu.Separator(), index);		
		} 
		else {
			AFreeplaneAction action = Controller.getCurrentController().getAction(actionKey);
			
			if(action == null) {
				return;
			}
			
			final JMenuItem item;
			if (action.getClass().getAnnotation(SelectableAction.class) != null) {
				item = new JAutoCheckBoxMenuItem(action);
			}
			else {
				item = new JFreeplaneMenuItem(action);
			}
			popupMenu.add(item, index);
			addListeners(popupMenu, action);
		}
		return;
	}
	
	public static void addActions(final WorkspacePopupMenu popupMenu, final String[] keys) {
		assert popupMenu != null;
		assert keys != null;
		
		Stack<JMenu> subMenuStack = new Stack<JMenu>();
		
		for(String key : keys) {
			if(key == null) {
				continue;
			}
			else
			if(key.equals(SEPARATOR)) {
				if(subMenuStack.size() == 0) {
					popupMenu.addSeparator();					
				} 
				else {
					subMenuStack.peek().addSeparator();
				}
			} 
			else if(key.startsWith("beginSubMenu")) {
				String popupName = key.substring("beginSubMenu".length());
				JMenu subMenu = new JMenu(popupName);
				(subMenuStack.size() == 0 ? popupMenu : subMenuStack.peek()).add(subMenu);
				subMenuStack.push(subMenu);				
			}
			else if(key.equals("endSubMenu")) {
				subMenuStack.pop();
			}
			else {
				AFreeplaneAction action = Controller.getCurrentController().getAction(key);
				if(action == null) {
					continue;
				}
				addAction(popupMenu, subMenuStack.size() == 0 ? popupMenu : subMenuStack.peek(), action);
			}
			
		}
	}
	
	private static void addAction(WorkspacePopupMenu popupMenu, JComponent jComponent, AFreeplaneAction action) {
		addAction(jComponent, action);
		addListeners(popupMenu, action);		
	}

	public static String createSubMenu(String name) {
		return "beginSubMenu"+name;
	}
	
	public static String endSubMenu() {
		return "endSubMenu";
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
						TreePath[] selectedNodes = ((JTree)menu.getInvoker()).getSelectionPaths();
						AWorkspaceTreeNode node = (AWorkspaceTreeNode) ((JTree)menu.getInvoker()).getClosestPathForLocation(menu.getInvokerLocation().x, menu.getInvokerLocation().y).getLastPathComponent();
						((AWorkspaceAction) action).setSelectedFor(node, selectedNodes);
					} 
					else {
						action.setSelected();
					}
				}
			});
		}
		if (AWorkspaceAction.checkEnabledOnPopup(action)) {
			popupMenu.addPopupMenuListener(new PopupMenuListener() {
				public void popupMenuCanceled(final PopupMenuEvent e) {
				}

				public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
				}

				public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
					if(action instanceof AWorkspaceAction && e.getSource() instanceof WorkspacePopupMenu) {
						WorkspacePopupMenu menu = ((WorkspacePopupMenu)e.getSource());
						TreePath[] selectedNodes = ((JTree)menu.getInvoker()).getSelectionPaths();
						TreePath path = ((JTree)menu.getInvoker()).getClosestPathForLocation( menu.getInvokerLocation().x , menu.getInvokerLocation().y);
						if(path != null) {
							AWorkspaceTreeNode node = (AWorkspaceTreeNode) path.getLastPathComponent();
							((AWorkspaceAction) action).setEnabledFor(node, selectedNodes);
						} 
						else {
							((AWorkspaceAction) action).setEnabledFor((AWorkspaceTreeNode) WorkspaceController.getCurrentModel().getRoot(), selectedNodes);
						}
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
