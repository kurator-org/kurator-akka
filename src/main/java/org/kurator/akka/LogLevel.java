package org.kurator.akka;

public enum LogLevel {
    
    ALL(0), OBJECT(1), DEBUG(2), INFO(3), WARNING(4), ERROR(5), CRITICAL(6), NONE(7);
    final int value;
    LogLevel(int v) { this.value = v; }
    
    public static LogLevel toLogLevel(Object level) throws Exception {
        
        if (level instanceof LogLevel) return (LogLevel)level;
        
        if (level instanceof String) {
            String levelString = (String)level; 
            if (levelString.equalsIgnoreCase("ALL"))        return ALL;
            if (levelString.equalsIgnoreCase("A"))          return ALL;
            if (levelString.equalsIgnoreCase("OBJECTS"))    return OBJECT;
            if (levelString.equalsIgnoreCase("O"))          return OBJECT;
            if (levelString.equalsIgnoreCase("DEBUG"))      return DEBUG;
            if (levelString.equalsIgnoreCase("INFO"))       return INFO;
            if (levelString.equalsIgnoreCase("I"))          return INFO;
            if (levelString.equalsIgnoreCase("WARNING"))    return WARNING;
            if (levelString.equalsIgnoreCase("W"))          return WARNING;
            if (levelString.equalsIgnoreCase("ERROR"))      return ERROR;
            if (levelString.equalsIgnoreCase("E"))          return ERROR;
            if (levelString.equalsIgnoreCase("CRITICAL"))   return CRITICAL;
            if (levelString.equalsIgnoreCase("C"))          return CRITICAL;
            if (levelString.equalsIgnoreCase("NONE"))       return NONE;
            if (levelString.equalsIgnoreCase("N"))          return NONE;
        }

        throw new Exception("Unrecognized LogLevel: " + level);
    }
}