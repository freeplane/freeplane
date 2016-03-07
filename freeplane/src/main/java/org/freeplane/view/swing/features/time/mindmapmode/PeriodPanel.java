/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2012 dimitry
 *
 *  This file author is dimitry
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
package org.freeplane.view.swing.features.time.mindmapmode;

import java.util.Calendar;
import java.util.Date;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import org.freeplane.core.resources.TranslatedObject;
import org.freeplane.core.ui.components.JComboBoxWithBorder;

/**
 * @author Dimitry Polivaev
 * Jan 15, 2012
 */
@SuppressWarnings("serial")
class PeriodPanel extends JPanel {
	private JSpinner periodComponent; 
	private JComboBox periodUnitBox;
	PeriodPanel(){
		SpinnerNumberModel periodModel = new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1);
		periodComponent = new  JSpinner(periodModel);
		periodUnitBox = new JComboBoxWithBorder(TranslatedObject.fromEnum(PeriodUnit.class));
		periodUnitBox.setEditable(false);
		periodUnitBox.setSelectedIndex(PeriodUnit.DAY.ordinal());
		add(periodComponent);
		add(periodUnitBox);
	}
	
	PeriodUnit getPeriodUnit(){
		final TranslatedObject selectedItem = (TranslatedObject)periodUnitBox.getSelectedItem();
		final PeriodUnit period = (PeriodUnit)selectedItem.getObject();
		return period;
	}
	
	void setPeriodUnit(PeriodUnit unit){
		periodUnitBox.setSelectedIndex(unit.ordinal());
	}
	
	
	int getPeriod(){
		return ((Number)periodComponent.getValue()).intValue();
	}

	void setPeriod(int period){
		periodComponent.setValue(period);
	}
	
	Date calculateNextTime(Date time) {
		final PeriodUnit periodUnit = getPeriodUnit();
		int period = getPeriod();
		final long timeInMillis = time.getTime();
		final Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timeInMillis);
		if(periodUnit.equals(PeriodUnit.WEEK))
			period *= 7;
		calendar.add(periodUnit.calendarField, period);
		return calendar.getTime();
	}	
}
