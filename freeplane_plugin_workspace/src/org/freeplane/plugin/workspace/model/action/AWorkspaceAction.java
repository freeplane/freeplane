package org.freeplane.plugin.workspace.model.action;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.model.WorkspacePopupMenu;
import org.freeplane.plugin.workspace.model.node.AWorkspaceTreeNode;

public abstract class AWorkspaceAction extends AFreeplaneAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AWorkspaceAction(String key, String title, Icon icon) {
		super(key, title, icon);
	}

	public AWorkspaceAction(String key) {
		super(key, TextUtils.getRawText(key + ".label"), null);
		setIcon();
	}
	
	public void setEnabledFor(AWorkspaceTreeNode node) {
		setEnabled();
	}

	public void setSelectedFor(AWorkspaceTreeNode node) {
		super.setSelected();
	}
	
	private void setIcon() {
		final String iconResource = ResourceController.getResourceController().getProperty(getIconKey(), null);
		if (iconResource != null) {
			// look in this package
			URL url = this.getClass().getResource(iconResource);
			if (url != null) {
				final ImageIcon icon = new ImageIcon(url);
				putValue(SMALL_ICON, icon);
				return;
			}
			//  look in workspace package
			url = WorkspaceController.class.getResource(iconResource);
			if (url != null) {
				final ImageIcon icon = new ImageIcon(url);
				putValue(SMALL_ICON, icon);
				return;
			}
			// look in freeplane package
			url = ResourceController.class.getResource(iconResource);
			if (url != null) {
				final ImageIcon icon = new ImageIcon(url);
				putValue(SMALL_ICON, icon);
				return;
			}
			
			LogUtils.severe("can not load icon '" + iconResource + "'");
		}
	}
	
	protected AWorkspaceTreeNode getNodeFromActionEvent(ActionEvent e) {
		WorkspacePopupMenu pop = getRootPopupMenu((Component) e.getSource());
		if(pop == null) {
			return null;
		}
		int x = pop.getInvokerLocation().x;
		int y = pop.getInvokerLocation().y;
		JTree tree = (JTree)pop.getInvoker();
		TreePath path = tree.getPathForLocation(x, y);
		if(path == null) {
			return null;
		}
		return (AWorkspaceTreeNode) path.getLastPathComponent();
	}
	
	private WorkspacePopupMenu getRootPopupMenu(Component component) {
		Component parent = component;
		while(!(parent instanceof WorkspacePopupMenu) && parent != null) {
			if(parent.getParent() == null && parent instanceof JPopupMenu) {
				parent = getRootPopupMenu(((JPopupMenu) parent).getInvoker());
				break;
			} 
			else  {
				parent = parent.getParent();
			}
		}
		return (WorkspacePopupMenu) parent;
	}
	
	protected Component getComponentFromActionEvent(ActionEvent e) {
		WorkspacePopupMenu pop = (WorkspacePopupMenu)((Component) e.getSource()).getParent();		
		JTree tree = (JTree)pop.getInvoker();
		return tree.getComponentAt(pop.getInvokerLocation());
	}
}
