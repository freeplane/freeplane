package org.freeplane.features.mindmapmode.styles;

import java.util.LinkedList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.common.filter.AFilterComposerDialog;
import org.freeplane.features.common.filter.condition.ISelectableCondition;

@SuppressWarnings("serial")
class FilterComposerDialog extends AFilterComposerDialog{

	public FilterComposerDialog() {
        super(TextUtils.getText("filter_dialog"), true);
        conditions = new LinkedList<ISelectableCondition>();
    }

	protected DefaultComboBoxModel createModel() {
		conditions.clear();
		if(model == null){
			model = new DefaultComboBoxModel();
		}
		return model;
    }
	
	protected boolean applyModel(DefaultComboBoxModel model, int[] selectedIndices) {
		if(selectedIndices.length != 1 && ! acceptMultipleConditions){
			return false;
		}
		conditions.clear();
		this.model = model;
		for(int i : selectedIndices){
			conditions.add((ISelectableCondition) model.getElementAt(i));
		}
	    return ! conditions.isEmpty();
    }
			
	final private List<ISelectableCondition> conditions ;
	private DefaultComboBoxModel model;
	private boolean acceptMultipleConditions;

	public List<ISelectableCondition> getConditions() {
    	return conditions;
    }

	public void acceptMultipleConditions(boolean acceptMultipleConditions) {
	    this.acceptMultipleConditions = acceptMultipleConditions;
    }
}
