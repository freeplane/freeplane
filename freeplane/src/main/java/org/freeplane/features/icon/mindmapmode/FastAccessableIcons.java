package org.freeplane.features.icon.mindmapmode;

import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.stream.Stream;

import javax.swing.AbstractButton;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.FreeplaneToolBar;
import org.freeplane.core.ui.components.JAutoToggleButton;
import org.freeplane.core.ui.components.ToolbarLayout;
import org.freeplane.features.mode.ModeController;

class FastAccessableIcons {
     
    private static final String FAST_ACCESS_ICON_NUMBER_PROPERTY = "fast_access_icon_number";
    private final DefaultListModel<AFreeplaneAction> actions;
    private final ModeController modeController;

    public FastAccessableIcons(ModeController modeController) {
        super();
        this.modeController = modeController;
        this.actions = new DefaultListModel<>();
    }

    public void add(IconAction action) {
        int actionCount = actions.size();
        if (actions.indexOf(action) >= 0)
        	return;
        int maxActionCount = Math.max(0, 
                ResourceController.getResourceController().getIntProperty(FAST_ACCESS_ICON_NUMBER_PROPERTY));
        if(maxActionCount < actionCount)
        	actions.removeRange(maxActionCount > 1 ? maxActionCount - 1 : 0, actionCount - 1);
        if(maxActionCount >= 1) {
            actions.add(0, action);
        }
    }
    
    public String getInitializer() {
        StringBuilder builder = new StringBuilder();
        int actionCount = actions.size();
        for (int index = actionCount - 1; index >= 0; index--) {
        	AFreeplaneAction buttonAction = actions.elementAt(index);
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
    
    public JPanel createActionPanel(AFreeplaneAction... controlActions) {
		JPanel panel = new JPanel(ToolbarLayout.vertical()) {
	    	private static final long serialVersionUID = 1L;
			ListDataListener l = new ListDataListener() {
				
				@Override
				public void intervalRemoved(ListDataEvent e) {
					int index0 = e.getIndex0();
					for(int i = index0; i <= e.getIndex1(); i++)
						remove(controlActions.length + index0);
					revalidate();
					repaint();
				}
				
				@Override
				public void intervalAdded(ListDataEvent e) {
					for(int i = e.getIndex0(); i <= e.getIndex1(); i++)
						add(createButton(actions.elementAt(i)), controlActions.length + i);
					revalidate();
					repaint();
				}
				
				@Override
				public void contentsChanged(ListDataEvent e) {
					intervalRemoved(e);
					intervalAdded(e);
				}
	    	};
	    	ComponentListener parentListener = new ComponentAdapter() {
	    		@Override
	    		public void componentResized(ComponentEvent e) {
	    			revalidate();
	    			repaint();
	    		}
	    	};
  			@Override
			public void addNotify() {
            	for(int i = 0; i < controlActions.length; i++) {
            		Component button = createButton(controlActions[i]);
					add(button);
            	}
            	for(int i = 0; i < actions.size(); i++) {
            		add(createButton(actions.elementAt(i)));
            	}
				actions.addListDataListener(l);
				getParent().addComponentListener(parentListener);
				super.addNotify();
			}

			@Override
			public void removeNotify() {
				super.removeNotify();
				actions.removeListDataListener(l);
				getParent().removeComponentListener(parentListener);
				removeAll();
			}
			
		};

		return panel;
	}
    
    private Component createButton(AFreeplaneAction action) {
    	AbstractButton button = new JButton(action);
		if (action.isSelectable()) {
			button = new JAutoToggleButton(action);
		}
		else {
			button = new JButton(action);
		}
        FreeplaneToolBar.configureToolbarButton(button);
        button.setFocusable(true);
        return button;
    }

}
