package org.docear.plugin.core.ui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.freeplane.core.util.TextUtils;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class MapIdsConflictsPanel extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JRadioButton radioThisMap;
	private JRadioButton radioConflictingMap;
	private JLabel pathThisMap;
	private JLabel pathConflictingMap;
	private JRadioButton radioKeepIds;

	/**
	 * Create the panel.
	 */
	public MapIdsConflictsPanel(File thisMap, File conflictingMap) {
		setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("max(16dlu;default)"),
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("max(134dlu;default)"),},
			new RowSpec[] {
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		
		JLabel lblNewLabel = new JLabel(TextUtils.getText("docear.conflicting_map_ids"));
		add(lblNewLabel, "1, 1, 3, 1");
		
		radioThisMap = new JRadioButton(TextUtils.getText("docear.conflicting_map_ids.this_map"));
		radioThisMap.setSelected(true);
		radioThisMap.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {				
				radioConflictingMap.setSelected(false);
				radioKeepIds.setSelected(false);
			}
		});
		add(radioThisMap, "1, 3, 3, 1");
		
		pathThisMap = new JLabel(thisMap.getAbsolutePath());
		pathThisMap.setForeground(Color.blue);
		add(pathThisMap, "3, 4");
		
		radioConflictingMap = new JRadioButton(TextUtils.getText("docear.conflicting_map_ids.conflicting_map"));
		radioConflictingMap.addActionListener(new ActionListener() {			
			public void actionPerformed(ActionEvent e) {
				radioThisMap.setSelected(false);
				radioKeepIds.setSelected(false);
			}
		});
		add(radioConflictingMap, "1, 6, 3, 1");		
		
		pathConflictingMap = new JLabel(conflictingMap.getAbsolutePath());
		pathConflictingMap.setForeground(Color.red);
		add(pathConflictingMap, "3, 7");
		
		radioKeepIds = new JRadioButton(TextUtils.getText("docear.conflicting_map_ids.keep_ids"));
		radioKeepIds.addActionListener(new ActionListener() {			
			public void actionPerformed(ActionEvent e) {
				radioThisMap.setSelected(false);
				radioConflictingMap.setSelected(false);
			}
		});
		add(radioKeepIds, "1, 9, 3, 1, fill, default");
	}
	
	public boolean isThisMapNew() {
		return radioThisMap.isSelected();
	}
	
	public boolean isOtherMapNew() {
		return radioConflictingMap.isSelected();
	}
}
