package org.freeplane.core.ui.components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.HeadlessException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;

public class JFreeplaneCustomizableFileChooser extends JFileChooser{

    private static final long serialVersionUID = 1;

    private final List<JComponent> optionComponents = new ArrayList<>();

    public JFreeplaneCustomizableFileChooser() {
        super();
        putClientProperty("FileChooser.useShellFolder", Boolean.FALSE);
    }

    public JFreeplaneCustomizableFileChooser(File currentDirectory) {
        super(currentDirectory);
        putClientProperty("FileChooser.useShellFolder", Boolean.FALSE);
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
    protected JDialog createDialog(Component parent) throws HeadlessException {
        final JDialog dialog = super.createDialog(parent);
        customizer.accept(dialog);
        if(optionComponents.size() == 1) {
           dialog.getContentPane().add(optionComponents.get(0), BorderLayout.SOUTH);
        }
        else if(optionComponents.size() > 1) {
            Box optionBox = Box.createVerticalBox();
            optionComponents.forEach(c -> c.setAlignmentX(LEFT_ALIGNMENT));
            optionComponents.forEach(optionBox::add);
            dialog.getContentPane().add(optionBox, BorderLayout.SOUTH);
        }
        if(!dialog.getContentPane().isValid())
            dialog.pack();
        return dialog;
    }

    public void addOptionComponent(final JComponent component) {
        optionComponents.add(component);
    }


}
