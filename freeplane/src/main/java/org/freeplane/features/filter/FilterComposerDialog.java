package org.freeplane.features.filter;

import java.util.LinkedList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.filter.condition.ASelectableCondition;

@SuppressWarnings("serial")
public class FilterComposerDialog extends AFilterComposerDialog{

	public FilterComposerDialog() {
        super(TextUtils.getText("filter_dialog"), true);
        conditions = new LinkedList<ASelectableCondition>();
    }

	protected DefaultComboBoxModel createModel() {
		conditions.clear();
		initializeModel();
		return model;
    }

	protected void initializeModel() {
	    if(model == null){
			model = new DefaultComboBoxModel();
		}
    }
	
	protected boolean applyModel(DefaultComboBoxModel model, int[] selectedIndices) {
		if(selectedIndices.length != 1 && ! acceptMultipleConditions){
			return false;
		}
		conditions.clear();
		this.model = model;
		for(int i : selectedIndices){
			conditions.add((ASelectableCondition) model.getElementAt(i));
		}
	    return true;
    }
			
	final private List<ASelectableCondition> conditions ;
	private DefaultComboBoxModel model;
	private boolean acceptMultipleConditions;

	public List<ASelectableCondition> getConditions() {
    	return conditions;
    }

	public void acceptMultipleConditions(boolean acceptMultipleConditions) {
	    this.acceptMultipleConditions = acceptMultipleConditions;
    }

	public void addCondition(ASelectableCondition value) {
		initializeModel();
		if (model.getIndexOf(value) == -1){
			model.addElement(value);
		}
		setSelectedItem(value);
    }
}
