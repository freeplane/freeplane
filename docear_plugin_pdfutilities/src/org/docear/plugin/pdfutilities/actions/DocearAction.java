package org.docear.plugin.pdfutilities.actions;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.IndexedTree.Node;

public abstract class DocearAction extends AFreeplaneAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<Component> views = new ArrayList<Component>();


	public DocearAction(String key) {
		super(key);		
	}
	
	public List<Component> getViews() {
		return views;
	}
	
	public void setVisible(boolean visible){
		for(Component view : views){
			view.setVisible(visible);
		}
	}
	
	public static List<Component> getMenuKey(final MenuBuilder builder, final AFreeplaneAction action) {
		List<Component> views = new ArrayList<Component>();
        final String actionKey = "$" + action.getKey() + '$';
		for(int i = 0; i < 1000; i++){
			final String key = actionKey + i;
			if (builder.get(key) != null){
				Node node = (Node) builder.get(key);
				if(node.getUserObject() instanceof Component){
					views.add((Component)node.getUserObject());
				}			    
			}
		}
		return views;
    }

	

}
