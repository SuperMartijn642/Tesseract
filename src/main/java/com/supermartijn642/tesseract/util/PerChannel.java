package com.supermartijn642.tesseract.util;

import com.supermartijn642.tesseract.EnumChannelType;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created 07/10/2026 by SuperMartijn642
 */
public class PerChannel<T> {

    @SuppressWarnings("unchecked")
    private final T[] values = (T[])new Object[EnumChannelType.values().length];
    private final Function<EnumChannelType,T> initializer;
    private List<T> view;

    public PerChannel(Function<EnumChannelType,T> initializer){
        this.initializer = initializer;
        this.reset();
    }

    public PerChannel(T defaultValue){
        this(o -> defaultValue);
    }

    public PerChannel(){
        this((T)null);
    }

    public T get(EnumChannelType type){
        return this.values[type.ordinal()];
    }

    /**
     * @return the old value for the given channel type
     */
    public T set(EnumChannelType type, T value){
        T old = this.values[type.ordinal()];
        this.values[type.ordinal()] = value;
        return old;
    }

    public T remove(EnumChannelType type){
        return this.set(type, null);
    }

    public T computeIfAbsent(EnumChannelType type, Supplier<T> supplier){
        T value = this.values[type.ordinal()];
        if(value == null)
            this.values[type.ordinal()] = value = supplier.get();
        return value;
    }

    /**
     * Resets the given channel to the default value.
     * @return the old value for the given channel type
     */
    public T reset(EnumChannelType type){
        return this.set(type, null);
    }

    /**
     * Resets all channels to the default value.
     */
    public void reset(){
        for(EnumChannelType type : EnumChannelType.values())
            this.values[type.ordinal()] = this.initializer.apply(type);
    }

    public List<T> values(){
        if(this.view == null)
            this.view = Arrays.asList(this.values);
        return this.view;
    }
}
