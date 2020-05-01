/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2009 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.features.link;

import java.net.URI;

import org.freeplane.core.util.TextUtils;
import org.freeplane.features.filter.condition.ConditionFactory;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author Dimitry Polivaev
 * Mar 7, 2009
 */
public class HyperLinkExistsCondition extends HyperLinkCondition {
	public static final String NAME = "hyper_link_exists";

	public HyperLinkExistsCondition() {
		super(null, false, false, false);
	}

	@Override
	protected boolean checkLink(final URI nodeLink) {
		return true;
	}

	@Override
	protected String createDescription() {
		final String condition = TextUtils.getText(LinkConditionController.FILTER_LINK);
		final String simpleCondition = TextUtils.getText(ConditionFactory.FILTER_EXIST);
		return createDescription(condition, simpleCondition, getHyperlink());
	}

	@Override
    protected String getName() {
		return NAME;
	}

	@Override
    protected void fillXML(XMLElement element) {
    }
	
	
}
