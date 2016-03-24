package org.kurator.akka;

import org.python.core.PyBoolean;
import org.python.core.PyException;

public class PythonClassActor extends PythonActor {

    private volatile String pythonClassName;
    
    public PythonClassActor() {
        synchronized(this) {
            functionQualifier = "_PYTHON_CLASS_INSTANCE_.";
        }
    }

    @Override
    protected synchronized void configureCustomCode() throws Exception {

        String pythonClassConfig = (String)configuration.get("pythonClass");
        String pythonClassModule = null;
        
        int lastDotIndex = pythonClassConfig.lastIndexOf(".");
        if (lastDotIndex == -1) {
            pythonClassModule = null;
            pythonClassName = pythonClassConfig;
        } else {
            pythonClassModule = pythonClassConfig.substring(0, lastDotIndex);
            pythonClassName = pythonClassConfig.substring(lastDotIndex + 1);
        }
                
        if (pythonClassModule != null) {
            try {
                interpreter.exec("from " + pythonClassModule + " import " + pythonClassName);
            } catch (PyException e) {
                throw new Exception("Error importing class '" + pythonClassConfig + "': " + e.value);
            }
        }
            
        try {
            interpreter.exec("_PYTHON_CLASS_INSTANCE_=" + pythonClassName + "()");
        } catch (PyException e) {
            throw new Exception("Error instantiating class '" + pythonClassConfig + "': " + e.value);
        }
     }
    
    @Override
    protected synchronized String loadEventHandler(String handlerName, String defaultMethodName, int minArgumentCount, 
                                                   String statelessWrapperTemplate, String statefulWrapperTemplate) throws Exception {

        String actualMethodName = null;
        
        String customMethodName = (String)configuration.get(handlerName);
        if (customMethodName != null) {
            try {
                PyBoolean isMethod = (PyBoolean)interpreter.eval("inspect.ismethod(" + functionQualifier + customMethodName + ")");
                if (!isMethod.getBooleanValue()) {
                    throw new Exception("Error binding to " + handlerName + " method: '" + customMethodName + 
                                        "' is not a method on " + pythonClassName);
                }
            } catch (PyException e) {
                throw new Exception("Error binding to " + handlerName + " method '" + customMethodName + "': " + e.value);
            }
            actualMethodName = customMethodName;
        } else {
            try {
                PyBoolean isMethod = (PyBoolean)interpreter.eval("inspect.ismethod(" + functionQualifier + defaultMethodName + ")");
                if (isMethod.getBooleanValue()) {
                    actualMethodName = defaultMethodName;
                } else {                    
                    throw new Exception("Error binding to default " + handlerName + " method: '" + defaultMethodName + 
                                        "' is not a method on " + pythonClassName);
                }
            } catch (PyException e) {}
        } 
        
        if (actualMethodName != null) {
            
            int argCount = getArgCount(functionQualifier + actualMethodName);
            
            if (argCount == minArgumentCount + 1) {
                interpreter.exec(String.format(statelessWrapperTemplate, functionQualifier, actualMethodName));
            } else if (argCount == minArgumentCount + 2) {
                interpreter.exec(String.format(statefulWrapperTemplate, functionQualifier, actualMethodName));
            }
        }
        
        return actualMethodName;
    }  
}
