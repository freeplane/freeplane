package org.docear.plugin.core.ui;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class HeaderPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JLabel lblHeadline;
	private JLabel lblSubHeadline;

	/**
	 * Create the panel.
	 */
	public HeaderPanel() {
		setBackground(new Color(-1643275));
		
		setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("5dlu"),
				ColumnSpec.decode("default:grow"),
				ColumnSpec.decode("5dlu"),},
			new RowSpec[] {
				RowSpec.decode("fill:5dlu"),
				RowSpec.decode("fill:default"),
				RowSpec.decode("fill:5dlu"),
				RowSpec.decode("fill:default"),
				RowSpec.decode("fill:20dlu"),}));
		
		lblHeadline = new JLabel("Headline");
		lblHeadline.setFont(new Font("Dialog", Font.BOLD, 14));
		add(lblHeadline, "2, 2");
		
		JPanel subTitlePanel = new JPanel();
		subTitlePanel.setBackground(new Color(-1643275));
		add(subTitlePanel, "2, 4, fill, fill");
		subTitlePanel.setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("15dlu"),
				ColumnSpec.decode("default:grow"),},
			new RowSpec[] {
				RowSpec.decode("fill:default:grow"),}));
		
		lblSubHeadline = new JLabel("Subheadline");
		lblSubHeadline.setFont(new Font("Dialog", Font.PLAIN, 13));
		subTitlePanel.add(lblSubHeadline, "2, 1");		

	}

	public String getHeadlineText() {
		return lblHeadline.getText();
	}
	public void setHeadlineText(String text) {
		lblHeadline.setText(text);
	}
	public String getSubHeadlineText() {
		return lblSubHeadline.getText();
	}
	public void setSubHeadlineText(String text_1) {
		lblSubHeadline.setText(text_1);
	}
}
