package org.docear.plugin.pdfutilities.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;


public class GroupRadioButtonAction extends DocearAction{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private List<GroupRadioButtonAction> group = new ArrayList<GroupRadioButtonAction>();

	public GroupRadioButtonAction(String key) {
		super(key);			
	}

	public void actionPerformed(ActionEvent e) {
		this.setSelected(true);
		for(GroupRadioButtonAction item : this.group){
			item.setSelected(false);
		}
	}
	
	public void addGroupItem(GroupRadioButtonAction item){
		this.group.add(item);
	}

}
