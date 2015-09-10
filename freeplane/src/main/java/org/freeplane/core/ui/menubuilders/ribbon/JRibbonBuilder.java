package org.freeplane.core.ui.menubuilders.ribbon;

import java.awt.Frame;

import org.freeplane.core.ui.IUserInputListenerFactory;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;
import org.freeplane.features.mode.Controller;
import org.pushingpixels.flamingo.api.ribbon.JRibbon;
import org.pushingpixels.flamingo.api.ribbon.JRibbonFrame;

public class JRibbonBuilder implements EntryVisitor {
	private JRibbon ribbon = null;

	public JRibbonBuilder(IUserInputListenerFactory userInputListenerFactory) {
		super();
		Frame frame = UITools.getFrame();
		if(frame instanceof JRibbonFrame) {
			this.ribbon = ((JRibbonFrame)frame).getRibbon();
			Controller.getCurrentController().addAction(new ToggleRibbonAction(this.ribbon));
		}
		else {
			throw new RuntimeException("ribbons not activated");
		}
	}

	@Override
	public void visit(Entry target) {
		new EntryAccessor().setComponent(target, ribbon);
	}

	@Override
	public boolean shouldSkipChildren(Entry entry) {
		return false;
	}
}
