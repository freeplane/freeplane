package org.freeplane.view.swing.addins.filepreview;

import java.io.File;


import javax.swing.JComponent;
import javax.swing.filechooser.FileFilter;

public interface IViewerFactory {
	FileFilter getFileFilter();
	JComponent createViewer(File file);
}
