package org.freeplane.features.mindmapmode.styles;

import javax.swing.DefaultComboBoxModel;

import org.freeplane.core.util.TextUtils;
import org.freeplane.features.common.filter.AFilterComposerDialog;
import org.freeplane.features.common.filter.condition.ISelectableCondition;

@SuppressWarnings("serial")
class FilterComposerDialog extends AFilterComposerDialog{

	public FilterComposerDialog() {
        super(TextUtils.getText("filter_dialog"), true);
    }

	protected DefaultComboBoxModel createModel() {
		condition = null;
		if(model == null){
			model = new DefaultComboBoxModel();
		}
		return model;
    }
	
	protected boolean applyModel(DefaultComboBoxModel model) {
		condition = (ISelectableCondition) model.getSelectedItem();
		this.model = model;
	    return condition != null;
    }
			
	private ISelectableCondition condition ;
	private DefaultComboBoxModel model;

	public ISelectableCondition getCondition() {
    	return condition;
    }
}
