package org.freeplane.plugin.workspace.io.node;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.MalformedURLException;

import javax.swing.JFrame;

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
	public String toString() {
		return getName();
	}

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
				
				final JFrame f = new ImageViewer(image, true, "PICTURE");
				
				f.addWindowListener(new WindowAdapter() {
					public void windowClosing(WindowEvent e) {
						f.dispose();
					}
				});
				f.pack();
				f.setVisible(true);
			}
			catch (MalformedURLException e1) {
				e1.printStackTrace();
			}
		}
		
	}
}
