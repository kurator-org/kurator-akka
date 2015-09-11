package org.kurator.akka;

import java.util.Map;

import org.python.core.PyBoolean;
import org.python.core.PyException;


public class PythonClassActor extends PythonActor {

    String pythonClassConfig;
    String pythonClassModule;
    String pythonClassName;
    
    public PythonClassActor() {
        functionQualifier = "_PYTHON_CLASS_INSTANCE_.";
    }

    @Override
    protected void configureCustomCode() throws Exception {

        pythonClassConfig = (String)configuration.get("pythonClass");
        
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
    protected void loadOnDataWrapper() throws Exception {

        String onDataConfig = (String)configuration.get("onData");
        
        if (onDataConfig != null) {

            try {
                PyBoolean isMethod = (PyBoolean)interpreter.eval("inspect.ismethod(" + functionQualifier + onDataConfig + ")");
                if (!isMethod.getBooleanValue()) {
                    throw new Exception("Error binding to onData method: '" + onDataConfig + 
                                        "' is not a method on " + pythonClassName);
                }

            } catch (PyException e) {
                throw new Exception("Error binding to onData method '" + onDataConfig + "': " + e.value);
            }
            onData = onDataConfig;
        
        } else  {

            try {
                PyBoolean isMethod = (PyBoolean)interpreter.eval("inspect.ismethod(" + functionQualifier + DEFAULT_ON_DATA_FUNCTION + ")");
                if (isMethod.getBooleanValue()) {
                    onData = DEFAULT_ON_DATA_FUNCTION;
                } else {                    
                    throw new Exception("Error binding to default onData method: '" + DEFAULT_ON_DATA_FUNCTION + 
                                        "' is not a method on " + pythonClassName);
                }
            } catch (PyException e) {
                return;
            }
        } 
        
        interpreter.exec(String.format(onDataWrapperFormat, functionQualifier, onData));
    }
    
    @Override
    protected void applySettings() {
        
        if (settings != null) {
            for(Map.Entry<String, Object> setting : settings.entrySet()) {
                String name = functionQualifier + setting.getKey();
                Object value = setting.getValue();
                if (value instanceof String) {
                    interpreter.exec(name + "='" + value + "'");
                } else {
                    interpreter.exec(name + "=" + value);
                }
            }
        }
    }   

}
