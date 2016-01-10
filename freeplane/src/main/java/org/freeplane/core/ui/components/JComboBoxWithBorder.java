package org.freeplane.core.ui.components;

import java.awt.Component;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.freeplane.core.ui.LengthUnits;
import org.freeplane.core.util.Quantity;

@SuppressWarnings("serial")
public class JComboBoxWithBorder extends JComboBox{
	private RendererWithBorder rendererWithBorder;
	static private final int MARGIN = new Quantity<LengthUnits>(2, LengthUnits.pt).toBaseUnitsRounded();
	static private final Border STANDARD_BORDER = new EmptyBorder(0, MARGIN, 0, MARGIN);  
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
				borderOwner.setBorder(STANDARD_BORDER);
			}
			return listCellRendererComponent;
		}
		
	}

	public JComboBoxWithBorder() {
		super();
		initializeRenderer();
	}

	public JComboBoxWithBorder(ComboBoxModel aModel) {
		super(aModel);
		initializeRenderer();
	}

	public JComboBoxWithBorder(Object[] items) {
		super(items);
		initializeRenderer();
	}

	public JComboBoxWithBorder(Vector<?> items) {
		super(items);
		initializeRenderer();
	}

	private void initializeRenderer() {
		rendererWithBorder = new RendererWithBorder();
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

}
