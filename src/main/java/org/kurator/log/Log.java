package org.kurator.log;

public abstract class Log {

    public static String instance(String type, String instance) {
        return type + "<" + instance + ">"; 
    }
    
    public static String ACTOR(String name) {
        return instance("ACTOR", name);
    }
    
}
