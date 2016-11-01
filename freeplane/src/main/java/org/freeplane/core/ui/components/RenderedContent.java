package org.freeplane.core.ui.components;

import java.awt.Component;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JList;

public class RenderedContent<T> {
	public final T value;
	private final String text;
	private final Icon icon;
	public RenderedContent(T value, String text, Icon icon) {
		super();
		this.value = value;
		this.text = text;
		this.icon = icon;
	}

	@SuppressWarnings("serial")
	public static DefaultListCellRenderer createRenderer() {
		
		return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?>list, Object value, int index, boolean isSelected,
                                                          boolean cellHasFocus) {
            	RenderedContent<?> content = (RenderedContent<?>) value;
                Object renderedValue = content.text == null ? content.icon : content.text;
                DefaultListCellRenderer renderer = (DefaultListCellRenderer) super.getListCellRendererComponent(list, renderedValue, index, isSelected, cellHasFocus);
                if(content.icon != null) {
                	if(content.text != null)
                		renderer.setIcon(content.icon);
                	else
                		renderer.setHorizontalAlignment(CENTER);
                }
                return renderer;
            }
        };
	}
	
	public static <T extends Action> RenderedContent<T> of(T action){
		return new RenderedContent<T>(action, (String) action.getValue(Action.NAME), (Icon) action.getValue(Action.SMALL_ICON));
	}
	
	public static <T extends Action> Vector<RenderedContent<T>> of(T[] actions){
		Vector<RenderedContent<T>> vector = new Vector<>(actions.length);
		for(T action : actions)
			vector.add(RenderedContent.of(action));
		return vector;
	}
	
}
