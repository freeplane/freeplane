package org.freeplane.features.styles.mindmapmode.styleeditorpanel;

import java.beans.PropertyChangeEvent;

import org.freeplane.api.LengthUnit;
import org.freeplane.api.Quantity;
import org.freeplane.core.resources.components.BooleanProperty;
import org.freeplane.core.resources.components.IPropertyControl;
import org.freeplane.core.resources.components.QuantityProperty;
import org.freeplane.features.icon.IconController;
import org.freeplane.features.icon.mindmapmode.MIconController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.styles.LogicalStyleController.StyleOption;

import com.jgoodies.forms.builder.DefaultFormBuilder;

class IconSizeControlGroup implements ControlGroup{
	private class IconSizeChangeListener extends ControlGroupChangeListener{

		public IconSizeChangeListener(final RevertingProperty mSet,final IPropertyControl mProperty) {
			super(mSet, mProperty);
		}
		@Override
		public void applyValue(boolean enabled, NodeModel node, PropertyChangeEvent evt) {
			final MIconController iconController = (MIconController) IconController.getController();
			iconController.changeIconSize(node, enabled ? mIconSize.getQuantifiedValue() : null);
		}
		@Override
		void setStyleOnExternalChange(NodeModel node) {
			final Quantity<LengthUnit> iconSize = node.getSharedData().getIcons().getIconSize();
			final Quantity<LengthUnit> viewedIconSize = IconController.getController().getIconSize(node, StyleOption.FOR_UNSELECTED_NODE);
			mSetIconSize.setValue(iconSize != null);
			mIconSize.setQuantifiedValue(viewedIconSize);
		}

        @Override
        void adjustForStyle(NodeModel node) {
            StylePropertyAdjuster.adjustPropertyControl(node, mSetIconSize);
            StylePropertyAdjuster.adjustPropertyControl(node, mIconSize);
        }
	}
	private static final String ICON_SIZE = "icon_size";
	private QuantityProperty<LengthUnit> mIconSize;
	private RevertingProperty mSetIconSize;
	private IconSizeChangeListener propertyChangeListener;

	@Override
	public void addControlGroup(DefaultFormBuilder formBuilder) {
			mSetIconSize = new RevertingProperty();
			mIconSize = new QuantityProperty<LengthUnit>(ICON_SIZE, 0, 256, 4, LengthUnit.px);
			propertyChangeListener = new IconSizeChangeListener(mSetIconSize, mIconSize);
			mSetIconSize.addPropertyChangeListener(propertyChangeListener);
			mIconSize.addPropertyChangeListener(propertyChangeListener);
			mIconSize.appendToForm(formBuilder);
			mSetIconSize.appendToForm(formBuilder);
	}
	@Override
	public void setStyle(NodeModel node, boolean canEdit) {
		propertyChangeListener.setStyle(node);
	}
}