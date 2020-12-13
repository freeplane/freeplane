package org.freeplane.core.ui;

import org.freeplane.core.util.PhysicalUnit;

public enum TimePeriodUnits implements PhysicalUnit{
/*
+---------+-------------+---------------+
| ms      | Milliseconds| 1             | 
+---------+-------------+---------------+
| seconds | Seconds     | 1000 ms       | 
+---------+-------------+---------------+
| minutes | Minutes     | 60 seconds    | 
+---------+-------------+---------------+
| hours   | Hours       | 60 minutes    | 
+---------+-------------+---------------+
| days    | Days        | 24 hours      | 
+---------+-------------+---------------+
| weeks   | Weeks       | 7 days        | 
+---------+-------------+---------------+
		
 */
	    ms(1d), 
		seconds (1000 * ms.factor()), 
		minutes (60 * seconds.factor()), 
		hours(60 * minutes.factor()),
		days(24 * hours.factor()),
		weeks(7 * days.factor());
		
		TimePeriodUnits(double factor){
			this.factor = factor;
			
		}
		final private double factor;
		@Override
		public double factor() {
			return factor;
		}
		
	}