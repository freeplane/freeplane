package org.freeplane.features.presentations;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;

public class PresentationCollectionModel {

	public ComboBoxModel<PresentationModel> getPresentations() {
		return new DefaultComboBoxModel<>();
	}

}
