package com.supermartijn642.tesseract.capabilities;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created 16/04/2023 by SuperMartijn642
 */
public class FlatMapIterator<S, T> implements Iterator<T> {

    private final Iterator<S> iterator;
    private final Function<S,Iterator<T>> mapper;
    private Iterator<T> currentEntry;

    private final Supplier<Boolean> pushCall;
    private final Runnable popCall;

    public FlatMapIterator(Iterator<S> iterator, Function<S,Iterator<T>> mapper, Supplier<Boolean> pushCall, Runnable popCall){
        this.iterator = iterator;
        this.mapper = mapper;
        this.pushCall = pushCall;
        this.popCall = popCall;
    }

    public FlatMapIterator(Iterator<S> iterator, Function<S,Iterator<T>> mapper){
        this(iterator, mapper, null, null);
    }

    @Override
    public boolean hasNext(){
        if(this.pushCall != null && this.pushCall.get())
            return false;

        while((this.currentEntry == null || !this.currentEntry.hasNext()) && this.iterator.hasNext())
            this.currentEntry = this.mapper.apply(this.iterator.next());
        boolean hasNext = this.currentEntry != null && this.currentEntry.hasNext();

        if(this.popCall != null)
            this.popCall.run();
        return hasNext;
    }

    @Override
    public T next(){
        if(this.pushCall != null && this.pushCall.get())
            throw new NoSuchElementException();

        T next = this.currentEntry.next();

        if(this.popCall != null)
            this.popCall.run();
        return next;
    }
}
