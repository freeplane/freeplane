package org.freeplane.core.ui.ribbon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.freeplane.core.util.TextUtils;
import org.pushingpixels.flamingo.api.ribbon.AbstractRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.RibbonTask;

public class RibbonTaskContributorFactory implements IRibbonContributorFactory {

	public ARibbonContributor getContributor(final Properties attributes) {
		
		return new ARibbonContributor() {
			private List<ComparableContributorHull<AbstractRibbonBand<?>>> hulls = new ArrayList<ARibbonContributor.ComparableContributorHull<AbstractRibbonBand<?>>>();
			
			public String getKey() {
				return attributes.getProperty("name");				
			}
			
			public void contribute(RibbonBuildContext context, ARibbonContributor parent) {
				hulls.clear();
				context.processChildren(context.getCurrentPath(), this);
				if(!hulls.isEmpty()) {
					Collections.sort(hulls, comparator);
					AbstractRibbonBand<?>[] bands = new AbstractRibbonBand<?>[hulls.size()];
					int count = 0;
					for (ComparableContributorHull<AbstractRibbonBand<?>> hull : hulls) {
						bands[count++] = hull.getObject();
					}
					
					RibbonTask task = new RibbonTask(TextUtils.removeTranslateComment(TextUtils.getText("ribbon."+getKey())), bands);
					if(parent != null) {
						parent.addChild(task, new ChildProperties(parseOrderSettings(attributes.getProperty("orderPriority", ""))));
					}
				}
			}

			public void addChild(Object child, ChildProperties properties) {
				if(child instanceof AbstractRibbonBand) {
					hulls.add(new ComparableContributorHull<AbstractRibbonBand<?>>((AbstractRibbonBand<?>) child, properties.getOrderPriority()));
				}
				
			}
		};
	}

}
