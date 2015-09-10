package org.kurator.akka;

import org.python.core.PyBoolean;


public class PythonClassActor extends PythonActor {

    String pythonClassConfig;
    String pythonClassModule;
    String pythonClassName;
    
    public PythonClassActor() {
        functionQualifier = "_PYTHON_CLASS_INSTANCE_.";
    }

    @Override
    protected void configureCustomCode() {

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
            interpreter.exec("from " + pythonClassModule + " import " + pythonClassName);
        }
        
        interpreter.exec("_PYTHON_CLASS_INSTANCE_=" + pythonClassName + "()");            
    }

    @Override
    protected void loadOnDataWrapper() throws Exception {

        String onDataConfig = (String)configuration.get("onData");
        
        if (onDataConfig != null) {
            
//            if (!isMember(pythonClass, onDataConfig)) {
//                throw new Exception("Custom onData handler '" + onDataConfig + "' not defined for actor");
//            }
            onData = onDataConfig;
        
        } else  { //if (isMember(pythonClass, DEFAULT_ON_DATA_FUNCTION)) {

            onData = DEFAULT_ON_DATA_FUNCTION;
        
        } 
//        else {
//            
//            return;
//        }
        
        interpreter.exec(String.format(onDataWrapperFormat, functionQualifier, onData));
    }
    
    private Boolean isMember(String c, String f) {
        String call = "_is_member('" + c + "','" + f + "')";
        PyBoolean result = (PyBoolean)interpreter.eval(call);
        return result.getBooleanValue();
    }
}
