package org.freeplane.plugin.workspace.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.components.dialog.ImportProjectDialogPanel;
import org.freeplane.plugin.workspace.io.IProjectSettingsIOHandler.LOAD_RETURN_TYPE;
import org.freeplane.plugin.workspace.model.WorkspaceModelException;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;

public class WorkspaceImportProjectAction extends AWorkspaceAction {
	
	private static final long serialVersionUID = 1L;
	public static final String KEY = "workspace.action.project.import";
	
	public WorkspaceImportProjectAction() {
		super(KEY);
	}

	public void actionPerformed(ActionEvent event) {
		final ImportProjectDialogPanel dialog = new ImportProjectDialogPanel();
		dialog.setConfirmButton(new Component() {
			private static final long serialVersionUID = 1L;
			private Component confirmButton;

			@Override
			public void setEnabled(boolean b) {
				if(confirmButton == null) {
					findButton(dialog);
				}
				if(confirmButton != null) {
					confirmButton.setEnabled(b);
				}
			}

			private void findButton(Component dialog) {
				Component parent = dialog.getParent();
				while(parent != null) {
					if(parent instanceof JOptionPane) {
						//WORKSPACE - test: os other than windows7
						for(Component comp : ((JOptionPane) parent).getComponents()) {
							if(comp instanceof JPanel && ((JPanel) comp).getComponentCount() > 0 && ((JPanel) comp).getComponent(0) instanceof JButton) {
								confirmButton = ((JPanel) comp).getComponent(0);
							}
						}
					}						
					parent = parent.getParent();
				}
			}
			
		});
		
		int response = JOptionPane.showConfirmDialog(UITools.getFrame(), dialog, TextUtils.getText("workspace.action.node.import.project.dialog.title"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		
		if(response == JOptionPane.OK_OPTION) {					
			AWorkspaceProject project = dialog.getProject();
			if(project == null) {
				return;
			}
			WorkspaceController.getCurrentModel().addProject(project);
			try {
				LOAD_RETURN_TYPE return_type = WorkspaceController.getCurrentModeExtension().getProjectLoader().loadProject(project);
				if(return_type == LOAD_RETURN_TYPE.NEW_PROJECT && dialog.getProjectName() != null && dialog.getProjectName().length() > 0) {
					project.getModel().changeNodeName(project.getModel().getRoot(), dialog.getProjectName());
				}
			} catch (IOException e) {
				LogUtils.severe(e);
			} catch (WorkspaceModelException e) {
				LogUtils.severe(e);
			}
		}
	}

}
