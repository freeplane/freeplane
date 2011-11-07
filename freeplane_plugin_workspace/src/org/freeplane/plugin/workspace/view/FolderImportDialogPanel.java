/**
 * author: Marcel Genzmehr
 * 04.11.2011
 */
package org.freeplane.plugin.workspace.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * 
 */
public class FolderImportDialogPanel extends JPanel implements ActionListener, ItemListener {
	
	private static final long serialVersionUID = 1L;
	
	private JRadioButton rbtnContentOnly;	
	private JCheckBox chckbxIncludeSubdirectories;
	
	private JRadioButton rbtnCompleteStructure;
	private JCheckBox chckbxEnableDirectoryMonitoring;
	private JPanel panel;
	private JPanel panel_1;
	private JScrollPane scrollPane;
	private JList list;
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	
	public FolderImportDialogPanel() {
		FormLayout formLayout = new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.PREF_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("pref:grow"),
				FormFactory.RELATED_GAP_ROWSPEC,});
		setLayout(formLayout);
		
		panel = new JPanel();
		panel.setBorder(null);
		add(panel, "2, 6, fill, fill");
		panel.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("20dlu"),
				ColumnSpec.decode("pref:grow"),
				FormFactory.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				RowSpec.decode("1dlu"),
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.MIN_ROWSPEC,
				RowSpec.decode("fill:default:grow"),
				FormFactory.RELATED_GAP_ROWSPEC,}));
		
		rbtnContentOnly = new JRadioButton("content only", false);
		panel.add(rbtnContentOnly, "2, 2, 2, 1");
		
		chckbxIncludeSubdirectories = new JCheckBox("include subdirectories");
		chckbxIncludeSubdirectories.setEnabled(false);
		panel.add(chckbxIncludeSubdirectories, "3, 3");
		
		scrollPane = new JScrollPane();
		scrollPane.setEnabled(false);
		panel.add(scrollPane, "3, 4, fill, default");
		
		list = new JList();
		list.setVisibleRowCount(3);
		list.setEnabled(false);
		list.setCellRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 1L;

			public Component getListCellRendererComponent( JList list,
				Object value,
			        int index,
			        boolean isSelected,
			        boolean cellHasFocus)
			    {
					if(value instanceof JLabel) {
						JLabel label = ((JLabel)value);
						if(isSelected) {
							label.setBackground(list.getSelectionBackground());
							label.setForeground(list.getSelectionForeground());
						} 
						else {
							label.setBackground(list.getBackground());
							label.setForeground(list.getForeground());
						}						
						
						
						label.setEnabled(list.isEnabled());
						label.setFont(list.getFont());

				        Border border = null;
				        if (cellHasFocus) {
				            if (isSelected) {
				                border = UIManager.getBorder("List.focusSelectedCellHighlightBorder");
				            }
				            if (border == null) {
				                border = UIManager.getBorder("List.focusCellHighlightBorder");
				            }
				        } else {
				            border = new BevelBorder(BevelBorder.LOWERED, Color.DARK_GRAY,  list.getBackground(),  Color.DARK_GRAY,  list.getBackground());
				        }
						label.setBorder(border);
						label.getInsets().set(2, 2, 2, 2);
						return label;
					}
					return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			    }
		});
		list.setModel(new AbstractListModel() {
			private static final long serialVersionUID = 1L;
			JLabel[] values = new JLabel[] {new JLabel("test1"),new JLabel("test1"),new JLabel("test1"),new JLabel("test1"),new JLabel("test end")};
			public int getSize() {
				return values.length;
			}
			public Object getElementAt(int index) {
				return values[index];
			}
		});
		scrollPane.setViewportView(list);
		scrollPane.setBorder(new BevelBorder(BevelBorder.LOWERED,Color.WHITE,Color.LIGHT_GRAY,null,null));
		rbtnContentOnly.addActionListener(this);
		rbtnContentOnly.addItemListener(this);
		
		JSeparator separator = new JSeparator();
		add(separator, "1, 4, 3, 1");
		
		panel_1 = new JPanel();
		add(panel_1, "2, 2, fill, fill");
		panel_1.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
				ColumnSpec.decode("20dlu"),
				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
				ColumnSpec.decode("min:grow"),},
			new RowSpec[] {
				FormFactory.NARROW_LINE_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.NARROW_LINE_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		
		rbtnCompleteStructure = new JRadioButton("complete structure", true);
		panel_1.add(rbtnCompleteStructure, "2, 2, 3, 1, left, top");
		
		chckbxEnableDirectoryMonitoring = new JCheckBox("enable directory monitoring");
		panel_1.add(chckbxEnableDirectoryMonitoring, "4, 4, left, top");
		chckbxEnableDirectoryMonitoring.setEnabled(true);
		rbtnCompleteStructure.addActionListener(this);
		rbtnCompleteStructure.addItemListener(this);
		
		
	}

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == this.rbtnContentOnly) {
			if(this.rbtnContentOnly.isSelected()) {
				this.rbtnCompleteStructure.setSelected(false);
			}
		} 
		else 
		if(e.getSource() == this.rbtnCompleteStructure){
			if(this.rbtnCompleteStructure.isSelected()) {
				this.rbtnContentOnly.setSelected(false);
			}
		}
	}

	public void itemStateChanged(ItemEvent e) {
		if(e.getSource() == rbtnContentOnly) {
			chckbxIncludeSubdirectories.setEnabled(rbtnContentOnly.isSelected());
			scrollPane.setEnabled(rbtnContentOnly.isSelected());
			list.setEnabled(rbtnContentOnly.isSelected());
		}
		if(e.getSource() == rbtnCompleteStructure) {
			chckbxEnableDirectoryMonitoring.setEnabled(rbtnCompleteStructure.isSelected());
		}
		
	}
}
