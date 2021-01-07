package org.freeplane.features.styles;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.stream.Stream;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.filter.condition.ICombinedCondition;
import org.freeplane.features.map.NodeModel;
import org.freeplane.n3.nanoxml.XMLElement;

public class ConditionalStyleModel implements IExtension, Iterable<ConditionalStyleModel.Item>{
	public static class Item{
		private ASelectableCondition condition;
		private IStyle style;
		private boolean isActive;
		private boolean isLast;
		private Item(boolean isActive, ASelectableCondition condition, IStyle style, boolean isLast) {
	        super();
	        this.isActive = isActive;
	        this.condition = condition;
	        this.style = style;
	        this.setLast(isLast);
        }

		public Item(Item prototype) {
			this(prototype.isActive, prototype.condition, prototype.style, prototype.isLast);
		}

		public void setCondition(ASelectableCondition condition) {
	        this.condition = condition;
        }
		public ASelectableCondition getCondition() {
	        return condition;
        }
		public void setStyle(IStyle style) {
	        this.style = style;
        }
		public IStyle getStyle() {
	        return style;
        }
		public void setActive(boolean isActive) {
	        this.isActive = isActive;
        }
		public boolean isActive() {
	        return isActive;
        }
		public void setLast(boolean isLast) {
	        this.isLast = isLast;
        }
		public boolean isLast() {
	        return isLast;
        }
		
		public void toXml(XMLElement conditionalStylesRoot)
		{
			final XMLElement itemElement = conditionalStylesRoot.createElement("conditional_style");
			conditionalStylesRoot.addChild(itemElement);
			itemElement.setAttribute("ACTIVE", Boolean.toString(isActive()));
			final IStyle style = getStyle();
			if (style instanceof StyleTranslatedObject) {
				final String referencedStyle = ((StyleTranslatedObject)style).getObject().toString();
				itemElement.setAttribute("LOCALIZED_STYLE_REF", referencedStyle);
			}
			else {
				final String referencedStyle = style.toString();
				itemElement.setAttribute("STYLE_REF", referencedStyle);
			}
			itemElement.setAttribute("LAST", Boolean.toString(isLast()));
			if(condition != null)
				condition.toXml(itemElement);

		}

		boolean dependOnCondition(ConditionPredicate predicate) {
			if (isActive())
				return dependOnConditionRecursively(condition, predicate);
			else
				return false;
		}

		private boolean dependOnConditionRecursively(ASelectableCondition condition, ConditionPredicate predicate) {
			if(condition instanceof ICombinedCondition){
				final Collection<ASelectableCondition> conditions = ((ICombinedCondition)condition).split();
				for(ASelectableCondition c : conditions)
					if(dependOnConditionRecursively(c, predicate))
						return true;
				return false;
			}
			else
				return  predicate.test(condition);
		}
		
	}
	private ArrayList<Item> styles;
	public ConditionalStyleModel() {
	    super();
	    this.styles = new ArrayList<Item>();
    }

	public ConditionalStyleModel(ConditionalStyleModel conditionalStyleModel) {
		super();
		final ArrayList<Item> prototypeStyles = conditionalStyleModel.styles;
		this.styles = new ArrayList<Item>(prototypeStyles.size());
		for (Item style : prototypeStyles)
			styles.add(new Item(style));
	}
	private boolean recursiveCall;
	
	public Collection<IStyle> getStyles(NodeModel node){
		if(recursiveCall){
			return Collections.emptyList();
		}
		try{
			recursiveCall = true;
			Collection<IStyle> matchingStyles = new LinkedHashSet<IStyle>();
			for(Item item : styles){
				final ASelectableCondition condition = item.getCondition();
				if( item.isActive() && (condition == null || condition.checkNodeInFormulaContext(node))){
					matchingStyles.add(item.style);
					if(item.isLast()){
						break;
					}
				}
			}
			return matchingStyles;
		}
		finally{
			recursiveCall = false;
		}
	}
	
	void addCondition(boolean isActive, ASelectableCondition condition, IStyle style, boolean isLast){
		styles.add(new Item(isActive, condition, style, isLast));
		if(table == null){
			return;
		}
		int index = styles.size() - 1;
		table.fireTableRowsInserted(index, index);
	}
	
	void insertCondition(int index, boolean isActive, ASelectableCondition condition, IStyle style, boolean isLast){
		styles.add(index, new Item(isActive, condition, style, isLast));
		if(table == null){
			return;
		}
		table.fireTableRowsInserted(index, index);
	}
	
	Item removeCondition(int index){
		final Item item = styles.remove(index);
		if(table == null){
			return item;
		}
		table.fireTableRowsDeleted(index, index);
		return item;
	}
	
	void swapConditions(int index1, int index2){
		final Item item1 = styles.get(index1);
		final Item item2 = styles.get(index2);
		styles.set(index1, item2);
		styles.set(index2, item1);
		if(table == null){
			return;
		}
		table.fireTableRowsUpdated(index1, index1);
		table.fireTableRowsUpdated(index2, index2);
	}
	
	void moveUp(int index){
		if(index == 0){
			return;
		}
		swapConditions(index, index - 1);
	}
	
	void moveDown(int index){
		if(index == styles.size() - 1){
			return;
		}
		swapConditions(index, index + 1);
	}
	
	void clear(){
		styles.clear();
	}

	public Iterator<Item> iterator() {
	    return styles.iterator();
    }
	private AbstractTableModel table;
	public TableModel asTableModel(){
		if(table != null){
			return table;
		}
		table = new AbstractTableModel() {
			/**
             * 
             */
            private static final long serialVersionUID = 1L;

			public Object getValueAt(int rowIndex, int columnIndex) {
				Item item = styles.get(rowIndex);
				switch(columnIndex){
					case 0:
						return item.isActive();
					case 1:
						return item.getCondition();
					case 2:
						return item.getStyle();
					case 3:
						return item.isLast();
						default:
							throw new ArrayIndexOutOfBoundsException();
				}
			}
			
			public int getRowCount() {
				return styles.size();
			}
			
			public int getColumnCount() {
				return 4;
			}
			
			

			@Override
            public Class<?> getColumnClass(int columnIndex) {
				switch(columnIndex){
					case 0:
					case 3: return Boolean.class;
					case 1: return ASelectableCondition.class;
				}
				return super.getColumnClass(columnIndex);
            }
			
			

			@Override
            public String getColumnName(int column) {
				switch(column){
					case 0: return TextUtils.getText("active");
					case 1: return TextUtils.getText("condition");
					case 2: return TextUtils.getText("style");
					case 3: return TextUtils.getText("stop_processing");
				}
				return super.getColumnName(column);
            }

			@Override
            public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
				Item item = styles.get(rowIndex);
				switch(columnIndex){
					case 0:
						item.setActive((Boolean) aValue);
						break;
					case 1:
						item.setCondition((ASelectableCondition) aValue);
						break;
					case 2:
						 item.setStyle((IStyle) aValue);
						 break;
					case 3:
						item.setLast((Boolean) aValue);
						break;
					default:
						throw new ArrayIndexOutOfBoundsException();
				}
				fireTableCellUpdated(rowIndex, columnIndex);
            }

			@Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
	            return true;
            }
		};
		return table;
	}

	public int getStyleCount() {
	    return styles.size();
    }

	public ConditionalStyleModel clone() {
		final ConditionalStyleModel conditionalStyleModel = new ConditionalStyleModel(this);
		return conditionalStyleModel;
	}

	boolean dependOnCondition(ConditionPredicate predicate) {
		for(Item item : styles){
			if(item.dependOnCondition(predicate))
				return true;
		}
		return false;
	}

    void addDifferentConditions(ConditionalStyleModel source) {
        Item[] differentConditions = source.styles.stream().filter(i1 -> !contains(i1)).toArray(Item[]::new);
        Stream.of(differentConditions).forEach(styles::add);
    }

    private boolean contains(Item item) {
        return styles.stream().anyMatch(own -> item.style.equals(own.style) && item.condition.equals(own.condition));
    }
}
