package org.freeplane.core.ui.components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.HeadlessException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.filechooser.FileSystemView;
import javax.swing.filechooser.FileView;

public class JFreeplaneCustomizableFileChooser extends JFileChooser{

    private static final long serialVersionUID = 1;

    private static final Icon directoryIcon = UIManager.getIcon("FileView.directoryIcon");
    private static final Icon computerIcon = UIManager.getIcon("FileView.computerIcon");
    private static final Icon hardDriveIcon = UIManager.getIcon("FileView.hardDriveIcon");
    private static final Icon floppyDriveIcon = UIManager.getIcon("FileView.floppyDriveIcon");
    
    private final List<JComponent> optionComponents = new ArrayList<>();
    
    public JFreeplaneCustomizableFileChooser() {
        super();
    }

    public JFreeplaneCustomizableFileChooser(File currentDirectory) {
        super(currentDirectory);
    }

    @FunctionalInterface
    public interface Customizer extends Consumer<JDialog>{
        Customizer DEFAULT = d -> {};
    }
    
    private Consumer<JDialog> customizer = Customizer.DEFAULT;
    
    public void addCustomizer(Customizer newCustomizer) {
        customizer = customizer.andThen(newCustomizer);
    }

    public Consumer<JDialog> getCustomizer() {
        return customizer;
    }
    
    @Override
    public Icon getIcon(File f) {
        Icon icon = null;
        if (f != null) {
            if(getFileView() != null) {
                icon = getFileView().getIcon(f);
            }

            final FileView uiFileView;
			if(icon == null && f.isDirectory()) {
				final FileSystemView fsv = getFileSystemView();

                if (fsv.isFloppyDrive(f)) {
                    icon = floppyDriveIcon;
                } else if (fsv.isDrive(f)) {
                    icon = hardDriveIcon;
                } else if (fsv.isComputerNode(f)) {
                    icon = computerIcon;
                } else {
                    icon = directoryIcon;
                }
			}
			else {
				uiFileView = getUI().getFileView(this);

				if(icon == null && uiFileView != null) {
					icon = uiFileView.getIcon(f);
				}
			}
        }
        return icon;
    }

    @Override
    protected JDialog createDialog(Component parent) throws HeadlessException {
        final JDialog dialog = super.createDialog(parent);
        customizer.accept(dialog);
        if(optionComponents.size() == 1) {
           dialog.getContentPane().add(optionComponents.get(0), BorderLayout.NORTH);
        }
        else if(optionComponents.size() > 1) {
            Box optionBox = Box.createVerticalBox();
            optionComponents.forEach(c -> c.setAlignmentX(LEFT_ALIGNMENT));
            optionComponents.forEach(optionBox::add);
            dialog.getContentPane().add(optionBox, BorderLayout.NORTH);
        }
        if(!dialog.getContentPane().isValid())
            dialog.pack();
        return dialog;
    }

    public void addOptionComponent(final JComponent component) {
        optionComponents.add(component);
    }


}
