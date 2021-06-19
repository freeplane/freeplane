package org.freeplane.core.util.collection;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

public class FilterIterator<T> implements Iterator<T> {
    final private Iterator<T> sourceIterator;
    final private Predicate<T> pred;

    public FilterIterator(Iterator<T> sourceIterator, Predicate<T> pred) {
        super();
        this.sourceIterator = sourceIterator;
        this.pred = pred;
    }

    private T current;
    private boolean hasCurrent;

    @Override
    public boolean hasNext() {
        while(!hasCurrent) {
            if(!sourceIterator.hasNext()) {
                return false;
            }
            T next = sourceIterator.next();
            if(pred.test(next)) {
                current = next;
                hasCurrent = true;
            }
        }
        return true;
    }

    @Override
    public T next() {
        if(!hasNext()) throw new NoSuchElementException();
        T next = current;
        current = null;
        hasCurrent = false;
        return next;
    }
}