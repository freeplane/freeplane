package org.freeplane.features.icon.mindmapmode;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.stream.Stream;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.FreeplaneToolBar;
import org.freeplane.core.ui.components.ToolbarLayout;
import org.freeplane.features.mode.ModeController;

class FastAccessableIcons {
     
    private static final String FAST_ACCESS_ICON_NUMBER_PROPERTY = "fast_access_icon_number";
    private final JPanel panel;
    private final ModeController modeController;

    public FastAccessableIcons(ModeController modeController) {
        super();
        this.modeController = modeController;
        panel = new JPanel(ToolbarLayout.vertical());
    }

    public void add(IconAction action) {
        int actionCount = panel.getComponentCount();
        for (int index = 0; index < actionCount; index++) {
            JButton button =  (JButton) panel.getComponent(index);
            IconAction buttonAction = (IconAction)button.getAction();
            if (buttonAction.getKey() == action.getKey()) {
                return;
            }
        }
        int maxCount = ResourceController.getResourceController().getIntProperty(FAST_ACCESS_ICON_NUMBER_PROPERTY);
        while(actionCount >= maxCount)
            panel.remove(actionCount - 1);
        panel.add(createButton(action), 0);
        panel.revalidate();
        panel.repaint();
    }
    
    private Component createButton(IconAction action) {
        JButton button = new JButton(action);
        FreeplaneToolBar.configureToolbarButton(button);
        return button;
    }

    public String getInitializer() {
        StringBuilder builder = new StringBuilder();
        int actionCount = panel.getComponentCount();
        for (int index = actionCount - 1; index >= 0; index--) {
            JButton button =  (JButton) panel.getComponent(index);
            IconAction buttonAction = (IconAction)button.getAction();
            builder.append(buttonAction.getKey());
            if(index > 0)
                builder.append(';');
        }
        return builder.toString();
    }
    
    public void load(String initializer) {
        Stream.of(initializer.split(";")).map(modeController::getAction)//
        .filter(IconAction.class::isInstance)
        .map(IconAction.class::cast).forEach(this::add);
    }
    
    public void addPanelTo(Container container) {
        container.add(panel);
        container.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                panel.revalidate();
                panel.repaint();
            }
        });
    }
}
