package org.freeplane.api;

import java.util.stream.Stream;

public enum LengthUnit implements PhysicalUnit{
/*
+---------+-------------+---------------+
| px      | Pixels      | Varies        | 
+---------+-------------+---------------+
| in      | Inches      | 1             | 
+---------+-------------+---------------+
| mm      | Millimeters | 25.4          | 
+---------+-------------+---------------+
| cm      | Centimeters | 2.54          | 
+---------+-------------+---------------+
| pt      | Points      | 72            | 
+---------+-------------+---------------+
		
 */
		px(1d), 
		in(72.0), 
		mm(72.0 / 25.4), 
		cm(72.0 / 2.54),
		pt(1d);
    
		LengthUnit(double factor){
			this.factor = factor;
			
		}
		private double factor;
		@Override
		public double factor() {
			return factor;
		}
		
		static public Quantity<LengthUnit> pixelsInPt(double value){
			return new Quantity<LengthUnit>(value, px).in(pt);
		}
		
		static public Quantity<LengthUnit> fromStringInPt(String value){
			return Quantity.fromString(value, px).in(pt);
		}

        public static void setScalingFactor(double newFactor) {
           double factor = newFactor / pt.factor;
           Stream.of(values()).skip(1).forEach(unit -> unit.factor *= factor);
        }
	}