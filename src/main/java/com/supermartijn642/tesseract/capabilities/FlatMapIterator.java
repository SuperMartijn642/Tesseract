package com.supermartijn642.tesseract.capabilities;

import java.util.Iterator;
import java.util.function.Function;

/**
 * Created 16/04/2023 by SuperMartijn642
 */
public class FlatMapIterator<S, T> implements Iterator<T> {

    private final Iterator<S> iterator;
    private final Function<S,Iterator<T>> mapper;
    private Iterator<T> currentEntry;

    public FlatMapIterator(Iterator<S> iterator, Function<S,Iterator<T>> mapper){
        this.iterator = iterator;
        this.mapper = mapper;
    }

    @Override
    public boolean hasNext(){
        while((this.currentEntry == null || !this.currentEntry.hasNext()) && this.iterator.hasNext())
            this.currentEntry = this.mapper.apply(this.iterator.next());
        return this.currentEntry != null && this.currentEntry.hasNext();
    }

    @Override
    public T next(){
        return this.currentEntry.next();
    }
}
