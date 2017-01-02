package org.freeplane.features.edge.mindmapmode;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.freeplane.core.ui.ColorTracker;
import org.freeplane.core.ui.components.JRestrictedSizeScrollPane;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.ColorUtils;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class ColorListEditorPanelBuilder {
	private final JPanel panel;
	private final CellConstraints cc;
	private final FormLayout formlayout;
	private final List<Color> colors;
	private final List<RowButtons> buttons;
	
	private final static Font BUTTON_FONT;
	private final static String MOVE_DOWN = "2";
	private final static String MOVE_UP = "1";
	private final static String DELETE = "3";
	private final static String ADD = "4";
	static {
		try (final InputStream fontResource = ColorListEditorPanelBuilder.class.getResourceAsStream("/fonts/listcontrols.ttf")){
			final float fontSize = Math.round(UITools.FONT_SCALE_FACTOR * 14);
			BUTTON_FONT = Font.createFont(Font.TRUETYPE_FONT, fontResource).deriveFont(fontSize);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private class ColorAdder implements ActionListener {
		private final int index;
		public ColorAdder(int index) {
			super();
			this.index = index;
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			final Color color;
			if (index > 0) {
				color = colors.get(index - 1);
			} else {
				color = Color.GRAY;
			}
			colors.add(index, color);
			addColorButtonRow();
			for(int i = index; i < colors.size() - 1; i++) {
				RowButtons r = buttons.get(i);
				r.updatePickColorButton();
				
			}
			buttons.get(colors.size() - 2).updateMoveColorDownButton();
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					buttons.get(index).scrollToVisible();
				}
			});
		}
	}

	private class RowButtons {
		private class ColorSwapper implements ActionListener {
			final private int direction;
			public ColorSwapper(int direction) {
				super();
				this.direction = direction;
			}
			@Override
			public void actionPerformed(ActionEvent e) {
				final Color movedColor = colors.get(index);
				final int swappedColorIndex = index + direction;
				final Color otherColorColor = colors.get(swappedColorIndex);
				colors.set(index, otherColorColor);
				colors.set(swappedColorIndex, movedColor);
				updatePickColorButton();
				updateMoveColorDownButton();
				RowButtons r = buttons.get(swappedColorIndex);
				r.updatePickColorButton();
				r.updateMoveColorDownButton();
			}
		}

		private final JLabel rowNumber;
		private final JButton pickColor;
		private final JButton moveColorUp;
		private final JButton addNextColor;
		private final JButton deleteColor;
		private final JButton moveColorDown;
		final private int index;
		public RowButtons(final int index) {
			super();
			rowNumber = new JLabel(Integer.toString(index + 1) + ":");
			rowNumber.setHorizontalAlignment(SwingConstants.RIGHT);
			pickColor = new JButton();
			moveColorUp = new JButton();
			moveColorDown = new JButton();
			addNextColor = new JButton();
			deleteColor = new JButton();
			this.index = index;
			initialize();
		}
		
		private void initialize() {
			rowNumber.setName("rowNumber");
			initializePickColorButton();
			initializeMoveColorUpButton();
			initializeMoveColorDownButton();
			initializeAddButton();
			initializeDeleteButton();
			appendRowsToPanel();
			addComponentsToPanel();
		}

		private void initializePickColorButton() {
			pickColor.setName("pickColor");
			pickColor.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					final Color newColor = ColorTracker.showCommonJColorChooserDialog(pickColor, "Pick color", colors.get(index), null);
					if(newColor != null){
						colors.set(index, newColor);
						updatePickColorButton();
						updateMoveColorDownButton();
					}
				}
			});
			updatePickColorButton();

		}

		private void initializeMoveColorDownButton() {
			moveColorDown.setName("moveColorDown");
			moveColorDown.setFont(BUTTON_FONT);
			moveColorDown.setText(MOVE_DOWN);
			moveColorDown.addActionListener(new ColorSwapper(+1));
			updateMoveColorDownButton();
		}

		private void initializeMoveColorUpButton() {
			moveColorUp.setName("moveColorUp");
			moveColorUp.setFont(BUTTON_FONT);
			moveColorUp.setText(MOVE_UP);
			moveColorUp.addActionListener(new ColorSwapper(-1));
			moveColorUp.setVisible(index > 0);
		}

		private void initializeAddButton() {
			addNextColor.setName("addNextColor");
			addNextColor.setFont(BUTTON_FONT);
			addNextColor.setText(ADD);
			addNextColor.addActionListener(new ColorAdder(index + 1));
		}

		private void initializeDeleteButton() {
			deleteColor.setName("deleteColor");
			deleteColor.setFont(BUTTON_FONT);
			deleteColor.setText(DELETE);
			deleteColor.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					colors.remove(index);
					for(int i = index; i < colors.size(); i++) {
						RowButtons r = buttons.get(i);
						r.updatePickColorButton();
					}
					if(colors.size() > 0) {
						RowButtons r = buttons.get(colors.size() - 1);
						r.updateMoveColorDownButton();
					}
					final int lastButtonRowIndex = buttons.size() - 1;
					buttons.get(lastButtonRowIndex).removeFromPanel();
					buttons.remove(lastButtonRowIndex);
					panel.repaint();
					SwingUtilities.invokeLater(new Runnable() {
						
						@Override
						public void run() {
						}
					});
				}
			});
		}

		private void addComponentsToPanel() {
			final int row = getFormRow();
			panel.add(rowNumber, cc.xy(1, row));
			panel.add(pickColor, cc.xy(3, row));
			panel.add(moveColorUp, cc.xy(5, row));
			panel.add(moveColorDown, cc.xy(7, row));
			panel.add(deleteColor, cc.xy(9, row));
			panel.add(addNextColor, cc.xy(11, row));
		}

		private void appendRowsToPanel() {
			formlayout.appendRow(RowSpec.decode("CENTER:4DLU:NONE"));
			formlayout.appendRow(RowSpec.decode("CENTER:DEFAULT:NONE"));
		}
		public void scrollToVisible() {
			pickColor.scrollRectToVisible(new Rectangle(0, 0, pickColor.getWidth(), pickColor.getHeight()));
		}
		private int getFormRow() {
			return index * 2 + 3;
		}
		
		private void updateMoveColorDownButton() {
			moveColorDown.setVisible(index < colors.size() -1);
		}

		private void updatePickColorButton() {
			Color color = colors.get(index);
			pickColor.setBackground(ColorUtils.alphaToColor(255, color));
			final Color textColor = UITools.getTextColorForBackground(color);
			pickColor.setForeground(textColor);
			pickColor.setText(ColorUtils.colorToString(color));
		}
		private void removeFromPanel() {
			panel.remove(rowNumber);
			panel.remove(pickColor);
			panel.remove(moveColorUp);
			panel.remove(moveColorDown);
			panel.remove(addNextColor);
			panel.remove(deleteColor);
			final int row = getFormRow();
			formlayout.removeRow(row);
			formlayout.removeRow(row - 1);
		}
	}

	/**
	 * Main method for panel
	 */
	public static void main(String[] args) {
		final List<Color> colorList = new ArrayList<>(100);
		for(int i = 0; i < 100; i++)
			colorList.add(Color.WHITE);
		final JComponent panel = new ColorListEditorPanelBuilder(colorList).getPanel();
		JScrollPane jscrollpane = new JRestrictedSizeScrollPane(panel);
		jscrollpane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		jscrollpane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		jscrollpane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 600));
		JOptionPane.showConfirmDialog(null, jscrollpane);
	}
	
	public ColorListEditorPanelBuilder(List<Color> colors) {
		super();
		this.colors = new ArrayList<>();
		panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		formlayout = new FormLayout(
				"FILL:DEFAULT:NONE,FILL:4DLU:NONE,FILL:108DLU:NONE,FILL:4DLU:NONE,FILL:DEFAULT:NONE,FILL:4DLU:NONE,FILL:DEFAULT:NONE,FILL:4DLU:NONE,FILL:DEFAULT:NONE,FILL:4DLU:NONE,FILL:DEFAULT:NONE",
				"");
		cc = new CellConstraints();

		formlayout.setColumnGroups(new int[][] { { 5, 7, 9, 11 } });
		panel.setLayout(formlayout);
		buttons = new ArrayList<>();
		this.colors.clear();
		this.colors.addAll(colors);
		addAddColorButton();
		addColorButtons();
	}



	public JComponent getPanel() {
		return panel;
	}

	private void addColorButtons() {
		for(@SuppressWarnings("unused") Color c : colors) {
			addColorButtonRow();
		}
	}

	private void addColorButtonRow() {
		final RowButtons rowButtons = new RowButtons(buttons.size());
		buttons.add(rowButtons);
	}

	private void addAddColorButton() {
		formlayout.appendRow(RowSpec.decode("CENTER:DEFAULT:NONE"));
		JButton addColor = new JButton();
		addColor.addActionListener(new ColorAdder(0));
		addColor.setName("addColor");
		addColor.setFont(BUTTON_FONT);
		addColor.setText(ADD);
		panel.add(addColor, cc.xy(11, 1));
	}


}
