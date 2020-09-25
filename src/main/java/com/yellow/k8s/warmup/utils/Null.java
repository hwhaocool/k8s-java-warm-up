package com.yellow.k8s.warmup.utils;

import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Null {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(Null.class);
    
    
    public static final <T> T of(Supplier<T> expr, Supplier<T> defaultValue){
        try{
            T result = expr.get();
            
            if(result == null){
                return defaultValue.get();
            }
            return result;
        }catch (NullPointerException e) {
            return defaultValue.get();
        }catch (Exception e) {
            
            LOGGER.error("ObjectHelper get error.", e);
            throw new RuntimeException(e);
        }
    }
    
    public static final <T> T of(Supplier<T> expr, T defaultValue){
        Supplier<T> defaultValues =  ()-> defaultValue;
        return of(expr,  defaultValues);
    }
    
    public static final <T> T of(Supplier<T> expr){
        Supplier<T> defaultValues =  ()-> null;
        return of(expr, defaultValues);
    }
    
    public static final <T> T ofString(Supplier<T> expr){
        Supplier<T> defaultValues =  ()-> (T)"";
        return of(expr, defaultValues);
    }
    
    
}
