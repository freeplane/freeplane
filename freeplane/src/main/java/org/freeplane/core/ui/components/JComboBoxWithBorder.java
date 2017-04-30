package org.freeplane.core.ui.components;

import java.awt.Component;
import java.awt.Insets;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;

import org.freeplane.core.ui.LengthUnits;
import org.freeplane.core.ui.MenuSplitterConfiguration;
import org.freeplane.core.util.Quantity;

@SuppressWarnings("serial")
public class JComboBoxWithBorder<T> extends JComboBox<T>{
	static private final int MARGIN = new Quantity<LengthUnits>(2, LengthUnits.pt).toBaseUnitsRounded();
	static private final EmptyBorder STANDARD_BORDER = new EmptyBorder(0, MARGIN, 0, MARGIN);  
	private RendererWithBorder rendererWithBorder;
	private EmptyBorder border = STANDARD_BORDER;  
	class RendererWithBorder implements ListCellRenderer{
		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			final ListCellRenderer baseRenderer = getBaseRenderer();
			if(baseRenderer == null)
				return null;
			final Component listCellRendererComponent = baseRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if(listCellRendererComponent instanceof JComponent) {
				final JComponent borderOwner = (JComponent) listCellRendererComponent;
				borderOwner.setBorder(border);
			}
			return listCellRendererComponent;
		}
		
	}

	public JComboBoxWithBorder() {
		super();
		initialize();
	}

	public JComboBoxWithBorder(ComboBoxModel aModel) {
		super(aModel);
		initialize();
	}

	public JComboBoxWithBorder(T[] items) {
		super(items);
		initialize();
	}

	public JComboBoxWithBorder(Vector<T> items) {
		super(items);
		initialize();
	}

	private void initialize() {
		rendererWithBorder = new RendererWithBorder();
		MenuSplitterConfiguration.setMaximumRowCount(this);
		updateUI();
	}

	@Override
	public ListCellRenderer getRenderer() {
		if(rendererWithBorder == null)
			return super.getRenderer();
		else
			return rendererWithBorder;
	}
	
	

	private ListCellRenderer getBaseRenderer() {
		return super.getRenderer();
	}

	public void setVerticalMargin(int verticalMargin) {
		final Insets borderInsets = border.getBorderInsets();
		if(borderInsets.top != verticalMargin || borderInsets.bottom != verticalMargin)
			border= new EmptyBorder(verticalMargin, MARGIN, verticalMargin, MARGIN);  
	}
}
