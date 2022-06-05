package org.freeplane.features.icon.mindmapmode;

import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.swing.AbstractButton;
import javax.swing.DefaultListModel;
import javax.swing.JToolBar;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.FreeplaneToolBar;
import org.freeplane.features.mode.ModeController;

public class FastAccessableIcons {
     
	public class ActionPanel extends FreeplaneToolBar {
		private final AFreeplaneAction[] controlActions;

		private static final long serialVersionUID = 1L;

		private ListDataListener l;
		
		private Consumer<AbstractButton> buttonConfigurer;

		public void setButtonConfigurer(Consumer<AbstractButton> buttonConfigurer) {
			this.buttonConfigurer = buttonConfigurer;
		}

		private ActionPanel(AFreeplaneAction[] controlActions) {
			super(VERTICAL);
			this.controlActions = controlActions;
			l = new ListDataListener() {
				
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
					for(int i = e.getIndex0(); i <= e.getIndex1(); i++) {
						AbstractButton button = createAndConfigureButton(actions.elementAt(i));
						add(button, controlActions.length + i);
					}
					revalidate();
					repaint();
				}
				
				@Override
				public void contentsChanged(ListDataEvent e) {
					intervalRemoved(e);
					intervalAdded(e);
				}
	    	};
		}

		@Override
		public void addNotify() {
			for(int i = 0; i < controlActions.length; i++) {
				AbstractButton button = createAndConfigureButton(controlActions[i]);
				add(button);
			}
			for(int i = 0; i < actions.size(); i++) {
				AbstractButton button = createAndConfigureButton(actions.elementAt(i));
				add(button);
			}
			actions.addListDataListener(l);
			super.addNotify();
		}

		private AbstractButton createAndConfigureButton(AFreeplaneAction action) {
			AbstractButton button = FreeplaneToolBar.createButton(action);
			if(buttonConfigurer !=  null)
				buttonConfigurer.accept(button);
			return button;
		}

		@Override
		public void removeNotify() {
			super.removeNotify();
			actions.removeListDataListener(l);
			removeAll();
		}
	}
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
    
    public ActionPanel createActionPanel(AFreeplaneAction... controlActions) {
    	ActionPanel panel = new ActionPanel(controlActions);
		return panel;
	}

}
