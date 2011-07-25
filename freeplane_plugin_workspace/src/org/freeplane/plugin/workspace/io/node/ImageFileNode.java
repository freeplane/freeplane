package org.freeplane.plugin.workspace.io.node;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.net.MalformedURLException;

import org.freeplane.plugin.workspace.controller.WorkspaceNodeEvent;
import org.freeplane.plugin.workspace.imageviewer.ImageViewer;

public class ImageFileNode extends PhysicalNode {
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	public ImageFileNode(String name, File file) {
		super(name, file);
	}
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/



	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	@Override
	public void handleEvent(WorkspaceNodeEvent event) {
		System.out.println("ImageFileNode: "+ event);
		
		if(event.getType() == WorkspaceNodeEvent.MOUSE_LEFT_DBLCLICK) {
			Image image;
			try {
				image = Toolkit.getDefaultToolkit().getImage(getFile().toURI().toURL());				
				new ImageViewer(image, true, "PICTURE");
			}
			catch (MalformedURLException e1) {
				e1.printStackTrace();
			}
		}
		else {
			super.handleEvent(event);
		}
		
	}
}
