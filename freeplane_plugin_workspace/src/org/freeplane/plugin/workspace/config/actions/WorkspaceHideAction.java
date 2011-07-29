package org.freeplane.plugin.workspace.config.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.components.PropertyBean;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.WorkspacePreferences;

import com.jgoodies.forms.builder.DefaultFormBuilder;

public class WorkspaceHideAction extends AFreeplaneAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public WorkspaceHideAction() {
		super("WorkspaceHideAction");
	}

	public void actionPerformed(final ActionEvent e) {
		System.out.println("WorkspaceHideAction: " + e.getActionCommand() + " : " + e.getID());
		ResourceController.getResourceController().setProperty(WorkspacePreferences.SHOW_WORKSPACE_PROPERTY_KEY, false);
		WorkspaceController.getCurrentWorkspaceController().refreshWorkspace();

//		MyPropertyBean bean = new MyPropertyBean(WorkspacePreferences.SHOW_WORKSPACE_PROPERTY_KEY);
//		bean.firePropertyChanged();
	}
	
//	private class MyPropertyBean extends PropertyBean {
//
//		public MyPropertyBean(String name) {
//			super(name);			
//			// TODO Auto-generated constructor stub
//		}
//		
//		private void firePropertyChanged() {
//			firePropertyChange(WorkspacePreferences.SHOW_WORKSPACE_PROPERTY_KEY, true, false);
//			firePropertyChangeEvent();
//		}
//
//		public void layout(DefaultFormBuilder builder) {
//			// TODO Auto-generated method stub
//			
//		}
//
//		public void setEnabled(boolean pEnabled) {
//			// TODO Auto-generated method stub
//			
//		}
//
//		@Override
//		public String getValue() {
//			// TODO Auto-generated method stub
//			return null;
//		}
//
//		@Override
//		public void setValue(String value) {
//			// TODO Auto-generated method stub
//			
//		}
//
//		@Override
//		protected Component[] getComponents() {
//			// TODO Auto-generated method stub
//			return null;
//		}
//		
//	}
}
