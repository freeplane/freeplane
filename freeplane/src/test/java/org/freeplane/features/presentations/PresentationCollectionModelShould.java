package org.freeplane.features.presentations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

import javax.swing.ComboBoxModel;

import org.junit.Test;

public class PresentationCollectionModelShould {
@Test
public void containNoPresentationsInitially() throws Exception {
	final PresentationCollectionModel presentationCollectionModel = new PresentationCollectionModel();
	ComboBoxModel<PresentationModel> presentations = presentationCollectionModel.getPresentations();
	assertThat(presentations.getSize()).isEqualTo(0);
}
}
