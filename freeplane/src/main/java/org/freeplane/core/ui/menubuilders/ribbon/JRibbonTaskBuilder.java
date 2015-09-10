package org.freeplane.core.ui.menubuilders.ribbon;

import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;
import org.freeplane.core.ui.menubuilders.generic.ResourceAccessor;
import org.freeplane.core.util.TextUtils;
import org.pushingpixels.flamingo.api.ribbon.JRibbon;
import org.pushingpixels.flamingo.api.ribbon.MutableRibbonTask;
import org.pushingpixels.flamingo.api.ribbon.RibbonTask;

public class JRibbonTaskBuilder implements EntryVisitor {

	private EntryAccessor accessor;

	public JRibbonTaskBuilder(ResourceAccessor resourceAccessor) {
		accessor = new EntryAccessor(resourceAccessor);
	}
	@Override
	public void visit(Entry target) {
		String title = TextUtils.removeMnemonic(accessor.getText(target));
		RibbonTask task = new MutableRibbonTask(title);
		accessor.setComponent(target, task);
		((JRibbon)accessor.getAncestorComponent(target)).addTask(task);
	}

	@Override
	public boolean shouldSkipChildren(Entry entry) {
		return false;
	}

}
