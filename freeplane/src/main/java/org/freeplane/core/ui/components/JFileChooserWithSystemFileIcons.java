package org.freeplane.core.ui.components;

import java.io.File;

import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;
import javax.swing.filechooser.FileView;
import javax.swing.plaf.FileChooserUI;
import javax.swing.plaf.basic.BasicFileChooserUI;

public class JFileChooserWithSystemFileIcons extends JFileChooser{

    private static final long serialVersionUID = 1;

    public JFileChooserWithSystemFileIcons() {
        super();
    }

    public JFileChooserWithSystemFileIcons(File currentDirectory, FileSystemView fsv) {
        super(currentDirectory, fsv);
    }

    public JFileChooserWithSystemFileIcons(File currentDirectory) {
        super(currentDirectory);
    }

    public JFileChooserWithSystemFileIcons(FileSystemView fsv) {
        super(fsv);
    }

    public JFileChooserWithSystemFileIcons(String currentDirectoryPath, FileSystemView fsv) {
        super(currentDirectoryPath, fsv);
    }

    public JFileChooserWithSystemFileIcons(String currentDirectoryPath) {
        super(currentDirectoryPath);
    }

    @Override
    public Icon getIcon(File f) {
        Icon icon = null;
        if (f != null) {
            if(getFileView() != null) {
                icon = getFileView().getIcon(f);
            }
            FileView uiFileView = getUI().getFileView(this);
            if(uiFileView.getClass().getName().equals("com.sun.java.swing.plaf.windows.WindowsFileChooserUI.WindowsFileView"))
                uiFileView = new BasicFileChooserUI(this).getFileView(this);

            if(icon == null && uiFileView != null) {
                icon = uiFileView.getIcon(f);
            }
        }
        return icon;
    }
    
    
}
