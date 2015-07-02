package org.kurator.akka;


public class PythonClassActor extends PythonActor {

    public PythonClassActor() {
        functionQualifier = "_PYTHON_CLASS_INSTANCE_.";
    }

    @Override
    protected void onInitialize() throws Exception {
        super.onInitialize();
        int lastDotIndex = pythonClass.lastIndexOf(".");
        if (lastDotIndex == -1) {
            interpreter.exec("_PYTHON_CLASS_INSTANCE_=" + pythonClass + "()");            
        } else {
            String pythonClassModule = pythonClass.substring(0, lastDotIndex);
            String pythonClassName = pythonClass.substring(lastDotIndex + 1);
            interpreter.exec("from " + pythonClassModule + " import " + pythonClassName);
            interpreter.exec("_PYTHON_CLASS_INSTANCE_=" + pythonClassName + "()");            
        }
    }
}
