package org.freeplane.plugin.workspace.mindmapmode;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.tree.TreePath;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.util.LogUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.actions.NodeCopyAction;
import org.freeplane.plugin.workspace.actions.NodeCutAction;
import org.freeplane.plugin.workspace.actions.NodePasteAction;
import org.freeplane.plugin.workspace.actions.NodeRefreshAction;
import org.freeplane.plugin.workspace.actions.NodeRemoveAction;
import org.freeplane.plugin.workspace.actions.NodeRenameAction;
import org.freeplane.plugin.workspace.components.TreeView;
import org.freeplane.plugin.workspace.components.WorkspaceNodeRenderer;
import org.freeplane.plugin.workspace.dnd.DnDController;
import org.freeplane.plugin.workspace.event.IWorkspaceNodeActionListener;
import org.freeplane.plugin.workspace.event.WorkspaceActionEvent;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;

public class InputController implements KeyListener, MouseListener, MouseMotionListener {
	// WORKSPACE - ToDo: implement gui for hot-key handling
	private Map<HotKeyIdentifier, String> actionKeyMap = new LinkedHashMap<InputController.HotKeyIdentifier, String>();
	
	
	private TreePath lastSelection = null;

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public InputController() {
		initActionKeyMap();
	}
	
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public final TreePath getLastSelectionPath() {
		return lastSelection;
	}

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	public void mouseClicked(MouseEvent e) {
		if (e.isConsumed()) {
			return;
		}
		TreePath path = ((JTree) e.getSource()).getClosestPathForLocation(e.getX(), e.getY());

		((TreeView) WorkspaceController.getCurrentModeExtension().getView()).addSelectionPath(path);
		if (path != null) {
			AWorkspaceTreeNode node = (AWorkspaceTreeNode) path.getLastPathComponent();
			// encode buttons
			int eventType = 0;
			if (e.getButton() == MouseEvent.BUTTON1) {
				eventType += WorkspaceActionEvent.MOUSE_LEFT;
			}
			if (e.getButton() == MouseEvent.BUTTON3) {
				eventType += WorkspaceActionEvent.MOUSE_RIGHT;
			}
			if (e.getClickCount() % 2 == 0) {
				eventType += WorkspaceActionEvent.MOUSE_DBLCLICK;
			} else {
				eventType += WorkspaceActionEvent.MOUSE_CLICK;
			}

			if (e.isPopupTrigger()) {
				eventType += WorkspaceActionEvent.POPUP_TRIGGER;
			}

			WorkspaceActionEvent event = new WorkspaceActionEvent(node, eventType, e.getX(), e.getY(), e.getComponent());

			List<IWorkspaceNodeActionListener> nodeEventListeners = WorkspaceController.getCurrentModeExtension().getIOController()
					.getNodeActionListeners(node.getClass(), eventType);
			if (nodeEventListeners != null) {
				for (IWorkspaceNodeActionListener listener : nodeEventListeners) {
					if (event.isConsumed()) {
						break;
					}
					listener.handleAction(event);
				}
			}

			if (!event.isConsumed() && node instanceof IWorkspaceNodeActionListener) {
				((IWorkspaceNodeActionListener) node).handleAction(event);
			}

		} else {
			if (e.getButton() == MouseEvent.BUTTON3) {
				// WorkspaceController.getController().getPopups().showWorkspacePopup(e.getComponent(), e.getX(), e.getY());
				((AWorkspaceTreeNode) WorkspaceController.getCurrentModel().getRoot()).showPopup(e.getComponent(), e.getX(), e.getY());
			}
		}
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseDragged(MouseEvent e) {
	}

	public void mouseMoved(MouseEvent e) {
		JTree tree = ((JTree) e.getSource());
		TreePath path = tree.getPathForLocation(e.getX(), e.getY());
		if (path == getLastSelectionPath()) {
			return;
		}
		WorkspaceNodeRenderer renderer = (WorkspaceNodeRenderer) tree.getCellRenderer();
		if (path != null && path != getLastSelectionPath()) {
			lastSelection = path;
			renderer.highlightRow(tree.getRowForLocation(e.getX(), e.getY()));
			tree.repaint();
		} else if (getLastSelectionPath() != null) {
			lastSelection = null;
			renderer.highlightRow(-1);
			tree.repaint();
		}
	}
	
	/*********
	 * Keyboard events
	 */
	
	private void initActionKeyMap() {
		actionKeyMap.put(new HotKeyIdentifier("copy", KeyStroke.getKeyStroke(KeyEvent.VK_C, /*KeyEvent.CTRL_DOWN_MASK*/Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())), NodeCopyAction.KEY);
		actionKeyMap.put(new HotKeyIdentifier("cut", KeyStroke.getKeyStroke(KeyEvent.VK_X, /*KeyEvent.CTRL_DOWN_MASK*/Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())),NodeCutAction.KEY);
		actionKeyMap.put(new HotKeyIdentifier("paste", KeyStroke.getKeyStroke(KeyEvent.VK_V, /*KeyEvent.CTRL_DOWN_MASK*/Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),new KeyEventAcceptor() {
			
			public boolean accept(KeyEvent e) {
				TreePath path = ((JTree) e.getSource()).getSelectionPath();
				if (path == null) {
					return false;
				}
				AWorkspaceTreeNode node = (AWorkspaceTreeNode) path.getLastPathComponent(); 
				if(DnDController.isDropAllowed(node)) {
					return true;
				}
				return false;
			}
		}), NodePasteAction.KEY);
		actionKeyMap.put(new HotKeyIdentifier("delete", KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), new KeyEventAcceptor() {
			
			public boolean accept(KeyEvent e) {
				TreePath path = ((JTree) e.getSource()).getSelectionPath();
				if (path == null) {
					return false;
				}
				AWorkspaceTreeNode node = (AWorkspaceTreeNode) path.getLastPathComponent();
				if(!node.isSystem() && node.isTransferable()) {
					return true;
				}
				return false;
			}
		}), NodeRemoveAction.KEY);
		actionKeyMap.put(new HotKeyIdentifier("rename", KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), new KeyEventAcceptor() {
			public boolean accept(KeyEvent event) {
				TreePath path = ((JTree) event.getSource()).getSelectionPath();
				if (path == null) {
					return false;
				}
				AWorkspaceTreeNode node = (AWorkspaceTreeNode) path.getLastPathComponent();
				if(!node.isSystem()) {
					return true;
				}
				return false;
			}
		}), NodeRenameAction.KEY);
		actionKeyMap.put(new HotKeyIdentifier("refresh", KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0)), NodeRefreshAction.KEY);
		
	}

	public void keyTyped(KeyEvent e) {
	}

	public void keyPressed(KeyEvent e) {
		KeyStroke currentStroke = KeyStroke.getKeyStrokeForEvent(e);
		
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			TreePath path = ((JTree) e.getSource()).getSelectionPath();
			if (path == null) {
				return;
			}
			AWorkspaceTreeNode node = (AWorkspaceTreeNode) path.getLastPathComponent();

			if (node instanceof IWorkspaceNodeActionListener) {
				((IWorkspaceNodeActionListener) node).handleAction(new WorkspaceActionEvent(node, WorkspaceActionEvent.WSNODE_OPEN_DOCUMENT, 0, 0, e
						.getComponent()));
				e.consume();
			}
		}
		else {
			for(HotKeyIdentifier id : actionKeyMap.keySet()) {
				if(currentStroke.equals(id.getKeyStroke())) {
					if(id.accept(e)) {
						AFreeplaneAction action = WorkspaceController.getAction(actionKeyMap.get(id));
						if(action != null) {
							action.actionPerformed(new ActionEvent(e.getSource(), 0, null));
						}
						else {
							LogUtils.info("No action set for: "+ id.getKeyStroke());
						}
					}
					e.consume();
					break;				
				}
			}
		}	
	}

	public void keyReleased(KeyEvent e) {
	}
	
	
	class HotKeyIdentifier {
		private final KeyStroke stroke;
		private final String identifier;
		private final KeyEventAcceptor acceptor;
				
		public HotKeyIdentifier(String id, KeyStroke stroke) {
			this(id, stroke, null);
		}
		public HotKeyIdentifier(String id, KeyStroke stroke, KeyEventAcceptor acceptor) {
			this.identifier = id;
			this.stroke = stroke;
			this.acceptor = acceptor;
		}
		
		public boolean accept(KeyEvent e) {
			if(this.acceptor == null) {
				return true;
			}
			return this.acceptor.accept(e);
		}

		public KeyStroke getKeyStroke() {
			return this.stroke;
		}
		
		public String getIdentifier() {
			return this.identifier;
		}
		
		//WORKSPACE - ToDo: localization
//		public String getLocalizedIdentifier() {
//			return TextUtils.getText("workspace.key.action."+getIdentifier().toLowerCase(Locale.ENGLISH));
//		}
		
	}
	
	interface KeyEventAcceptor {
		public boolean accept(KeyEvent event);
	}
}
