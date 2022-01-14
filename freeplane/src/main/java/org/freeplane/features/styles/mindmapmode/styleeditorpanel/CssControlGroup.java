package org.freeplane.features.styles.mindmapmode.styleeditorpanel;

import java.beans.PropertyChangeEvent;

import javax.swing.JButton;

import org.freeplane.core.resources.components.ButtonProperty;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.nodestyle.NodeCss;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.nodestyle.mindmapmode.MNodeStyleController;
import org.freeplane.features.styles.LogicalStyleController.StyleOption;

import com.jgoodies.forms.builder.DefaultFormBuilder;
class CssControlGroup implements ControlGroup{
	static final String REVERT_CSS = "revert-css";
    static final String CSS = "css";

	final private RevertingProperty mSetCss;
	final private ButtonProperty mCssButton;
	final private CssChangeListener mPropertyListener;
	
	public CssControlGroup(ModeController modeController) {
		mSetCss = new RevertingProperty(REVERT_CSS);
		mCssButton = new ButtonProperty(CSS, new JButton(modeController.getAction("EditNodeCssAction")));
		mPropertyListener = new CssChangeListener(mSetCss);
		mSetCss.addPropertyChangeListener(mPropertyListener);
		mCssButton.addPropertyChangeListener(mPropertyListener);
	}

	private class CssChangeListener extends ControlGroupChangeListener {
		public CssChangeListener(final RevertingProperty mSet) {
			super(mSet);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node, final PropertyChangeEvent evt) {
			if (!enabled) {
				final MNodeStyleController styleController = (MNodeStyleController) Controller
						.getCurrentModeController().getExtension(NodeStyleController.class);
				styleController.setStyleSheet(node, null);
			}
		}

		@Override
		void setStyleOnExternalChange(NodeModel node) {
			mSetCss.setValue(node.getExtension(NodeCss.class) != null);
			final NodeStyleController styleController = Controller
					.getCurrentModeController().getExtension(NodeStyleController.class);
			NodeCss styleSheet = styleController.getStyleSheet(node, StyleOption.FOR_UNSELECTED_NODE);
			mCssButton.setToolTipText(styleSheet.css);
		}
        
        @Override
        void adjustForStyle(NodeModel node) {
            StylePropertyAdjuster.adjustPropertyControl(node, mSetCss);
            StylePropertyAdjuster.adjustPropertyControl(node, mCssButton);
        }
	}
	
	@Override
	public void addControlGroup(DefaultFormBuilder formBuilder) {
		addCssControl(formBuilder);
	}
	
	private void addCssControl(DefaultFormBuilder formBuilder) {
		mCssButton.appendToForm(formBuilder);
		mSetCss.appendToForm(formBuilder);
	}

	@Override
	public void setStyle(NodeModel node, boolean canEdit) {
		mPropertyListener.setStyle(node);
	}
}