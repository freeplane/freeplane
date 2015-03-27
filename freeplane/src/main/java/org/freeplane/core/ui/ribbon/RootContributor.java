package org.freeplane.core.ui.ribbon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.pushingpixels.flamingo.api.ribbon.JRibbon;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenu;
import org.pushingpixels.flamingo.api.ribbon.RibbonTask;

public class RootContributor extends ARibbonContributor {
	private final JRibbon ribbon; 
	private final List<ComparableContributorHull<RibbonTask>> tasks = new ArrayList<ComparableContributorHull<RibbonTask>>();
	private final List<ComparableContributorHull<RibbonTaskBarComponent>> taskbar = new ArrayList<ComparableContributorHull<RibbonTaskBarComponent>>();
	
	public RootContributor(JRibbon ribbon) {
		this.ribbon = ribbon;
	}

	public String getKey() {
		return "/";
	}

	public void contribute(RibbonBuildContext context, ARibbonContributor parent) {
		ribbon.removeAllTaskbarComponents();
		ribbon.removeAllTasks();
		context.processChildren(context.getCurrentPath(), this);
		
		Collections.sort(tasks, comparator);
		for (ComparableContributorHull<RibbonTask> hull : tasks) {
			this.ribbon.addTask(hull.getObject());
		}

		Collections.sort(taskbar, comparator);
		for (ComparableContributorHull<RibbonTaskBarComponent> hull : taskbar) {
			this.ribbon.addTaskbarComponent(hull.getObject().getComponent());
		}
	}

	public void addChild(Object child, ChildProperties properties) {
		if(child instanceof RibbonTask) {
			tasks.add(new ComparableContributorHull<RibbonTask>((RibbonTask)child, properties.getOrderPriority()));
		}
		else if(child instanceof RibbonApplicationMenu) {
			this.ribbon.setApplicationMenu((RibbonApplicationMenu) child);
		}
		else if(child instanceof RibbonTaskBarComponent) {
			RibbonTaskBarComponent comp = (RibbonTaskBarComponent) child;
			taskbar.add(new ComparableContributorHull<RibbonTaskBarComponent>(comp, properties.getOrderPriority()));
		}
	}
}