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
                logger.debug("Importing Python module " + pythonClassModule);
                interpreter.exec("import " + pythonClassModule);
                interpreter.exec("from " + pythonClassModule + " import " + pythonClassName);
                String classModule = interpreter.eval(pythonClassName + ".__module__").toString();
                String modulePath = interpreter.eval(classModule + ".__file__").toString();
                logger.debug("Found module at " + reconstructPythonSourcePath(modulePath));
                logger.debug("Imported class " + pythonClassName + " from " + pythonClassModule);
            } catch (PyException e) {
                throw new Exception("Error importing class '" + pythonClassConfig + "': " + e.value);
            }
        }
            
        try {
            logger.trace("Instantiating Python class " + pythonClassName);
            interpreter.exec("_PYTHON_CLASS_INSTANCE_=" + pythonClassName + "()");
        } catch (PyException e) {
            String message = "Error instantiating class '" + pythonClassConfig + "': " + e.value;
            logger.error(message);
            throw new Exception(message);
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
