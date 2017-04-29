package org.freeplane.features.filter;

import java.util.LinkedList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.filter.condition.ASelectableCondition;

@SuppressWarnings("serial")
public class FilterComposerDialog extends AFilterComposerDialog{

	final private List<ASelectableCondition> conditions ;
	private DefaultComboBoxModel model;
	
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
		if(this.model != model)
			throw new IllegalArgumentException();
		if(selectedIndices.length > 1){
			return false;
		}
		conditions.clear();
		for(int i : selectedIndices){
			conditions.add((ASelectableCondition) model.getElementAt(i));
		}
	    return true;
    }
			
	public List<ASelectableCondition> getConditions() {
    	return conditions;
    }

	public void addCondition(ASelectableCondition value) {
		initializeModel();
		if (model.getIndexOf(value) == -1){
			model.addElement(value);
		}
    }


	public ASelectableCondition editCondition(ASelectableCondition value) {
		initializeModel();
		if(value != null)
			setSelectedItem(value);
	    show();
	    List<ASelectableCondition> conditions = getConditions();
	    if(isSuccess())
	    	return conditions.isEmpty() ? null : conditions.get(0);
	    return value;
	}
}
