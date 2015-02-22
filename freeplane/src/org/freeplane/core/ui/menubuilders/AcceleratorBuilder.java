package org.freeplane.core.ui.menubuilders;

import org.freeplane.core.util.Compat;

public class AcceleratorBuilder implements EntryVisitor{

	private static final String ACCELERATOR = "accelerator";
	private IDefaultAcceleratorMap map;

	public AcceleratorBuilder(IDefaultAcceleratorMap map) {
		this.map = map;
	}

	public void visit(Entry entry) {
		if(entry.getAction() != null){
			String accelerator = (String) entry.getAttribute(ACCELERATOR);
			if(accelerator != null) {
				if (isMacOsX()) {
			        accelerator = accelerator.replaceFirst("CONTROL", "META").replaceFirst("control", "meta");
			    }
				map.setDefaultAccelerator(entry.getName(), accelerator);
			}
		}
	}

	protected boolean isMacOsX() {
		return Compat.isMacOsX();
	}

	@Override
	public boolean shouldSkipChildren() {
		// TODO Auto-generated method stub
		return false;
	}

}
