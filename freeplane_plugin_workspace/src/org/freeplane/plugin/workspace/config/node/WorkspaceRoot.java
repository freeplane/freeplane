package org.freeplane.plugin.workspace.config.node;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.config.IConfigurationInfo;
import org.freeplane.plugin.workspace.config.actions.AddNewGroupAction;
import org.freeplane.plugin.workspace.config.actions.WorkspaceCollapseAction;
import org.freeplane.plugin.workspace.config.actions.WorkspaceDeleteNodeAction;
import org.freeplane.plugin.workspace.config.actions.WorkspaceExpandAction;
import org.freeplane.plugin.workspace.config.actions.WorkspaceHideAction;
import org.freeplane.plugin.workspace.config.actions.WorkspaceRefreshAction;
import org.freeplane.plugin.workspace.config.actions.WorkspaceChangeLocationAction;
import org.freeplane.plugin.workspace.controller.IWorkspaceNodeEventListener;
import org.freeplane.plugin.workspace.controller.WorkspaceNodeEvent;
import org.freeplane.plugin.workspace.io.annotation.ExportAsAttribute;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.model.WorkspacePopupMenu;
import org.freeplane.plugin.workspace.model.WorkspacePopupMenuBuilder;

public class WorkspaceRoot extends AWorkspaceTreeNode implements IConfigurationInfo, IWorkspaceNodeEventListener {

	private static final long serialVersionUID = 1L;
	private static Icon DEFAULT_ICON = new ImageIcon(
			PhysicalFolderNode.class.getResource("/images/16x16/preferences-desktop-filetype-association.png"));
	private static WorkspacePopupMenu popupMenu;

	private String version = WorkspaceController.WORKSPACE_VERSION;
	private Object meta = null;

	public WorkspaceRoot() {
		super(null);
	}

	public String getTagName() {
		return "workspace";
	}

	public void handleEvent(WorkspaceNodeEvent event) {
		if (event.getType() == WorkspaceNodeEvent.MOUSE_RIGHT_CLICK) {
			showPopup((Component) event.getBaggage(), event.getX(), event.getY());
		}
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@ExportAsAttribute("version")
	public String getVersion() {
		return this.version;
	}

	public void setMeta(String meta) {
		this.meta = meta;
	}

	@ExportAsAttribute("meta")
	public Object getMeta() {
		return this.meta;
	}

	public boolean setIcons(DefaultTreeCellRenderer renderer) {
		renderer.setOpenIcon(DEFAULT_ICON);
		renderer.setClosedIcon(DEFAULT_ICON);
		renderer.setLeafIcon(DEFAULT_ICON);
		return true;
	}

	public void initializePopup() {
		if (popupMenu == null) {
			ModeController modeController = Controller.getCurrentModeController();
			modeController.addAction(new WorkspaceExpandAction());
			modeController.addAction(new WorkspaceCollapseAction());
			modeController.addAction(new WorkspaceChangeLocationAction());
			modeController.addAction(new WorkspaceRefreshAction());
			modeController.addAction(new WorkspaceHideAction());
			modeController.addAction(new WorkspaceDeleteNodeAction());
			modeController.addAction(new AddNewGroupAction());
			
			popupMenu = new WorkspacePopupMenu();
			WorkspacePopupMenuBuilder.addActions(popupMenu, new String[] {"workspace.action.location.change",
					"workspace.action.hide",
					WorkspacePopupMenuBuilder.SEPARATOR, 
					"workspace.action.all.expand",
					"workspace.action.all.collapse", 
					WorkspacePopupMenuBuilder.SEPARATOR,
					"workspace.action.node.group.new",					 
					WorkspacePopupMenuBuilder.SEPARATOR,
					"workspace.action.node.refresh"					
			});
		}
	}

	protected AWorkspaceTreeNode clone(WorkspaceRoot node) {
		node.setMeta((String) getMeta());
		node.setVersion(getVersion());
		return super.clone(node);
	}

	public AWorkspaceTreeNode clone() {
		WorkspaceRoot node = new WorkspaceRoot();
		return clone(node);
	}

	protected WorkspacePopupMenu getPopupMenu() {
		if (popupMenu == null) {
			initializePopup();
		}
		return popupMenu;
	}
}
