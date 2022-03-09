package org.freeplane.core.ui.flatlaf;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.text.Caret;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.ui.FlatCaret;
import com.formdev.flatlaf.ui.FlatEditorPaneUI;

public class NonSelectingFlatEditorPaneUI extends FlatEditorPaneUI{
	public static ComponentUI createUI( JComponent c ) {
		return new NonSelectingFlatEditorPaneUI();
	}

	@Override
	protected Caret createCaret() {
		return new FlatCaret( FlatClientProperties.SELECT_ALL_ON_FOCUS_POLICY_NEVER, false );
	}

}
